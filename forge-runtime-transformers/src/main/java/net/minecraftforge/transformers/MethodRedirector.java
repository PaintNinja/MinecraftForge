/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.transformers;

import com.google.gson.JsonParser;
import ga.ozli.moduleloader.core.api.transform.ClassFileTransformer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.MethodModel;
import java.lang.classfile.MethodTransform;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.constant.ModuleDesc;
import java.lang.reflect.AccessFlag;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Redirects calls to {@code Mob.finalizeSpawn} to {@code ForgeEventFactory.onFinalizeSpawn}, so the
 * {@code MobSpawnEvent.FinalizeSpawn} event fires wherever vanilla finalizes a spawn.
 * <p>The methods to redirect calls in are listed in {@code coremods/finalize_spawn_targets.json}, which the
 * {@code findFinalizeSpawnTargets} task generates. Synthetic methods in those classes, such as lambdas, are always
 * included.</p>
 */
public final class MethodRedirector implements ClassFileTransformer {
    private static final System.Logger LOGGER = System.getLogger(MethodRedirector.class.getName());

    private static final String FINALIZE_SPAWN = "finalizeSpawn";
    private static final MethodTypeDesc MTD_FINALIZE_SPAWN = MethodTypeDesc.ofDescriptor("(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;");

    private static final ClassDesc CD_FORGE_EVENT_FACTORY = ClassDesc.of("net.minecraftforge.event.ForgeEventFactory");
    private static final String ON_FINALIZE_SPAWN = "onFinalizeSpawn";
    private static final MethodTypeDesc MTD_ON_FINALIZE_SPAWN = MethodTypeDesc.ofDescriptor("(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;");

    // The mob is already on the stack as the receiver, so it becomes the static method's first argument
    private static final CodeTransform REDIRECT = (builder, element) -> {
        if (element instanceof InvokeInstruction insn && insn.opcode() == Opcode.INVOKEVIRTUAL
                && insn.name().equalsString(FINALIZE_SPAWN) && insn.typeSymbol().equals(MTD_FINALIZE_SPAWN)) {
            builder.invokestatic(CD_FORGE_EVENT_FACTORY, ON_FINALIZE_SPAWN, MTD_ON_FINALIZE_SPAWN);
        } else {
            builder.with(element);
        }
    };

    /** Class to the methods in it to redirect calls in, as name + descriptor */
    private final Map<ClassDesc, Set<String>> targets = readTargets();

    @Override
    public boolean handlesModule(ModuleDesc moduleDesc) {
        return "minecraft".equals(moduleDesc.name());
    }

    @Override
    public boolean handlesClass(ModuleDesc moduleDesc, ClassDesc classDesc) {
        return targets.containsKey(classDesc);
    }

    @Override
    public ClassTransform transform(ModuleDesc moduleDesc, ClassDesc classDesc, ClassModel classModel) {
        var methods = targets.get(classDesc);

        var found = new HashSet<String>();
        for (var method : classModel.methods()) {
            var id = id(method);
            if (methods.contains(id))
                found.add(id);
        }
        for (var method : methods) {
            if (!found.contains(method))
                LOGGER.log(System.Logger.Level.ERROR, "Failed to redirect method call for {0}! Method {1} not found in class {2}! This is a Forge bug, and is likely due to a Minecraft update changing something.", FINALIZE_SPAWN, method, classDesc.displayName());
        }

        return ClassTransform.transformingMethods(
            method -> method.flags().has(AccessFlag.SYNTHETIC) || methods.contains(id(method)),
            MethodTransform.transformingCode(REDIRECT)
        );
    }

    private static String id(MethodModel method) {
        return method.methodName().stringValue() + method.methodType().stringValue();
    }

    private static Map<ClassDesc, Set<String>> readTargets() {
        try (var stream = MethodRedirector.class.getModule().getResourceAsStream("coremods/finalize_spawn_targets.json")) {
            if (stream == null)
                throw new IllegalStateException("Missing coremods/finalize_spawn_targets.json");

            var ret = new HashMap<ClassDesc, Set<String>>();
            for (var element : JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonArray()) {
                var target = element.getAsJsonObject();
                var methods = new HashSet<String>();
                for (var method : target.getAsJsonArray("methods"))
                    methods.add(method.getAsString());
                ret.put(ClassDesc.ofInternalName(target.get("class").getAsString()), Set.copyOf(methods));
            }
            return Map.copyOf(ret);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
