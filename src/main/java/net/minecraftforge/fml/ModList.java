/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml;

import ga.ozli.moduleloader.core.api.LayerInfo;
import net.minecraftforge.versions.forge.ForgeVersion;
import net.minecraftforge.versions.mcp.MCPVersion;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The mods that are loaded, which are the modules in ModuleLoader's current layer. LWJGL has to be in that layer with
 * Minecraft, so it's left out, but other libraries there count as mods.
 * <p>A mod's id is its module name. Forge is the exception. Its code is in the {@code minecraft} module, but it keeps
 * the {@code forge} id so registries, tags, network channels and saved worlds still match.</p>
 */
// TODO [ModuleLoader] Display names and other metadata, read from annotations on the module
public final class ModList {
    private static final String MINECRAFT = "minecraft";
    private static final Map<URI, Path> ROOTS = new ConcurrentHashMap<>();

    private ModList() {}

    /** @return The ids of all loaded mods, sorted by name so the order is stable */
    // TODO [ModuleLoader] Sort by the module graph instead, so mods come after the mods they require
    public static List<String> getModIds() {
        var modules = layer().modules();
        var ids = new ArrayList<String>(modules.size() + 1);
        for (var module : modules) {
            if (isMod(module))
                ids.add(module.getName());
        }
        ids.add(ForgeVersion.MOD_ID);
        ids.sort(null);
        return List.copyOf(ids);
    }

    public static int size() {
        return getModIds().size();
    }

    public static boolean isLoaded(String modId) {
        return getModule(modId).isPresent();
    }

    /** @return The module the mod's code is in. For {@code forge}, that's {@code minecraft}. */
    public static Optional<Module> getModule(String modId) {
        var name = ForgeVersion.MOD_ID.equals(modId) ? MINECRAFT : modId;
        var layer = layer();
        return layer.findModule(name).filter(module -> module.getLayer() == layer && isMod(module));
    }

    /** @return The mod's version, which for most mods comes from its module descriptor */
    public static Optional<String> getVersion(String modId) {
        return switch (modId) {
            case ForgeVersion.MOD_ID -> Optional.of(ForgeVersion.getVersion());
            case MINECRAFT -> Optional.of(MCPVersion.getMCVersion());
            default -> getModule(modId).flatMap(module -> module.getDescriptor().rawVersion());
        };
    }

    /** @return The root directory of the mod's jar, for reading its resources */
    public static Optional<Path> getRoot(String modId) {
        // In dev, Minecraft and Forge are merged into one jar where vanilla's resources win, so Forge's own resources are
        // read from Forge's jar instead
        // TODO [ModuleLoader] Revisit once Forge is split into its own module
        var devResources = System.getProperty("forge.devResources");
        if (devResources != null && ForgeVersion.MOD_ID.equals(modId))
            return Optional.of(ROOTS.computeIfAbsent(Path.of(devResources).toUri(), ModList::openRoot));

        return getModule(modId)
                .flatMap(module -> module.getLayer().configuration().findModule(module.getName()))
                .flatMap(resolved -> resolved.reference().location())
                .map(location -> ROOTS.computeIfAbsent(location, ModList::openRoot));
    }

    private static Path openRoot(URI location) {
        try {
            if (!"file".equals(location.getScheme()))
                return FileSystems.newFileSystem(location, Map.of()).getPath("/");

            var path = Path.of(location);
            return Files.isDirectory(path) ? path : FileSystems.newFileSystem(path).getPath("/");
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to open " + location, e);
        }
    }

    private static boolean isMod(Module module) {
        return !module.getName().startsWith("org.lwjgl");
    }

    private static ModuleLayer layer() {
        return LayerInfo.current().moduleLayer();
    }
}
