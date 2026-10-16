/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.forge.build

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar

/**
 * Sets up runs to launch through ModuleLoader instead of FML's Bootstrap.
 * <p>
 * ModuleLoader boots from the module path and then loads the game into its own module layer, from the paths in the
 * {@code moduleloader.gameModulePath} system property. So the run classpath is split in two:
 * <ul>
 *     <li>Boot: libraries, including ModuleLoader core, plus SlimeLauncher which sets up assets and calls into core</li>
 *     <li>Game: Minecraft and Forge, mods, LWJGL and ModuleLoader's loader, which is where Mixin and transformers apply</li>
 * </ul>
 * ModuleLoader discovers game modules with {@link java.lang.module.ModuleFinder#of}, which ignores directories without
 * a module-info. So project outputs are swapped for their jars, and Forge's output is swapped for the merged
 * Minecraft + Forge jar set with {@link ConventionExtension#gameJar}.
 */
@CompileStatic
final class ModuleLoaderLaunch {
    // Libraries that go on the game layer, everything else goes on the boot layer where Mixin and other transformers
    // can't touch it. LWJGL stays with Minecraft, and the loader requires the minecraft module.
    private static final Set<String> GAME_LAYER_GROUPS = ['org.lwjgl', 'ga.ozli.moduleloader.loader'] as Set<String>

    private ModuleLoaderLaunch() {}

    static void apply(Project project) {
        project.pluginManager.apply('org.gradlex.extra-java-module-info')
        configureModuleInfo(project)
    }

    // Lets libraries without a module-info be loaded as named modules
    @CompileDynamic
    private static void configureModuleInfo(Project project) {
        project.extraJavaModuleInfo {
            deriveAutomaticModuleNamesFromFileNames = true
            skipLocalJars = true

            // ModuleLoader core requires this name, MixinExtras doesn't ship an Automatic-Module-Name
            automaticModule('io.github.llamalad7:mixinextras-common', 'mixinextras.common')
        }
    }

    static void configure(Project project, SourceSet sourceSet, TaskProvider<? extends JavaExec> run) {
        final isRoot = project == project.rootProject
        final ownJar = project.tasks.named('jar', Jar)

        run.configure { task ->
            final rootConvention = project.rootProject.extensions.getByType(ConventionExtension)
            if (rootConvention.gameJar != null)
                task.dependsOn(rootConvention.gameJar)
            if (!isRoot)
                task.dependsOn(ownJar)

            final dedicatedServer = task.name.toLowerCase(Locale.ENGLISH).contains('server')

            task.doFirst {
                final runtime = sourceSet.runtimeClasspath.files
                // SlimeLauncher's own classpath, the task starts with it and then adds the source set's runtime classpath
                final tool = task.classpath.files - runtime

                final boot = new LinkedHashSet<File>(tool)
                boot.addAll(bootLibraries(project, sourceSet))

                final game = new LinkedHashSet<File>()
                final replaced = new HashSet<File>(sourceSet.output.files)
                if (rootConvention.gameJar != null) {
                    game.add(rootConvention.gameJar.get().archiveFile.get().asFile)
                    replaced.addAll(rootConvention.gameJarReplaces.files)
                    // The game jar gives vanilla's resources priority, so Forge's resource pack reads Forge's own jar.
                    // Otherwise Forge's copies of files that stack across packs, like atlases, replace vanilla's.
                    task.systemProperty('forge.devResources', project.rootProject.tasks.named('jar', Jar).get().archiveFile.get().asFile.absolutePath)
                }
                if (!isRoot)
                    game.add(ownJar.get().archiveFile.get().asFile)

                // extraJavaModuleInfo hands out renamed copies of jars it turns into modules, so match those by name too
                final replacedNames = new HashSet<String>()
                for (file in replaced)
                    replacedNames.add(file.name.endsWith('.jar') ? file.name[0..-5] + '-module.jar' : file.name)

                for (file in runtime) {
                    if (!boot.contains(file) && !replaced.contains(file) && !replacedNames.contains(file.name) && file.exists())
                        game.add(file)
                }

                final modulePath = boot.collect { it.absolutePath }.join(File.pathSeparator)
                task.classpath = project.files(boot)
                task.jvmArgs(
                    '--module-path', modulePath,
                    '--add-modules', 'ALL-MODULE-PATH',
                    '--add-modules', 'jdk.crypto.ec',
                    // JNA is on the boot layer with the other libraries, so ModuleLoader can't enable this for it
                    '--enable-native-access=ga.ozli.moduleloader.core,com.sun.jna',
                    '-Dmoduleloader.gameModulePath=' + game.collect { it.absolutePath }.join(File.pathSeparator),

                    // Mixin only supports modular environments through its hardcoded ModLauncher support, ModuleLoader's
                    // own build patches these into Mixin's module-info, which consumers of the published jar don't get
                    '--add-exports=org.spongepowered.mixin/org.spongepowered.asm.mixin.transformer=ga.ozli.moduleloader.core,mixinextras.common',
                    '--add-exports=org.spongepowered.mixin/org.spongepowered.asm.transformers=ga.ozli.moduleloader.core,mixinextras.common',
                    '--add-opens=org.spongepowered.mixin/org.spongepowered.asm.mixin=ga.ozli.moduleloader.core',
                    // MixinExtras deep-reflects into these when registering its extensions
                    '--add-opens=org.spongepowered.mixin/org.spongepowered.asm.mixin.transformer=mixinextras.common',
                    '--add-opens=org.spongepowered.mixin/org.spongepowered.asm.mixin.transformer.ext=mixinextras.common',
                    '--add-opens=org.spongepowered.mixin/org.spongepowered.asm.mixin.transformer.ext.extensions=mixinextras.common',
                    '--add-opens=org.spongepowered.mixin/org.spongepowered.asm.mixin.injection=mixinextras.common',
                    '--add-opens=org.spongepowered.mixin/org.spongepowered.asm.mixin.injection.struct=mixinextras.common',
                    '--add-opens=org.spongepowered.mixin/org.spongepowered.asm.mixin.injection.modify=mixinextras.common',
                )
                if (dedicatedServer)
                    task.systemProperty('moduleloader.dedicatedServer', 'true')
            }
        }
    }

    // Every library from a Maven repository except the ones in GAME_LAYER_GROUPS. This includes ModuleLoader core and
    // its dependencies, which have to be on the boot layer.
    private static Set<File> bootLibraries(Project project, SourceSet sourceSet) {
        final config = project.configurations.getByName(sourceSet.runtimeClasspathConfigurationName)

        final ret = new LinkedHashSet<File>()
        for (artifact in config.incoming.artifacts.artifacts) {
            final id = artifact.id.componentIdentifier
            if (id instanceof ModuleComponentIdentifier && !GAME_LAYER_GROUPS.contains(id.group))
                ret.add(artifact.file)
        }
        return ret
    }
}
