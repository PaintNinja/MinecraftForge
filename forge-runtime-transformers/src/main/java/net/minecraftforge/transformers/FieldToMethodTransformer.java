/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.transformers;

import ga.ozli.moduleloader.core.api.transform.ClassFileTransformer;

import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.FieldModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.MethodTransform;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.FieldInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.constant.ModuleDesc;
import java.lang.reflect.AccessFlag;
import java.util.Map;

/**
 * Redirects reads of some private fields in Minecraft classes to their getters, so Forge's patched getters are used
 * everywhere in the class.
 * <p>For example, {@code LiquidBlock} can be made from a fluid supplier, in which case its {@code fluid} field is null
 * and only {@code getFluid()} returns the fluid.</p>
 */
public final class FieldToMethodTransformer implements ClassFileTransformer {
    // TODO [Forge][Transformer] Make this properly data driven or configurable.
    //      It was hard-coded like this before when using JS CoreMods, though.
    private static final Map<ClassDesc, Map<String, String>> TARGETS = Map.of(
        ClassDesc.of("net.minecraft.world.effect.MobEffectInstance"), Map.of("effect", "getEffect"),
        ClassDesc.of("net.minecraft.world.level.block.LiquidBlock"), Map.of("fluid", "getFluid"),
        ClassDesc.of("net.minecraft.world.item.BucketItem"), Map.of("content", "getFluid"),
        ClassDesc.of("net.minecraft.world.level.block.FlowerPotBlock"), Map.of("potted", "getPotted")
    );

    @Override
    public boolean handlesModule(ModuleDesc moduleDesc) {
        return "minecraft".equals(moduleDesc.name());
    }

    @Override
    public boolean handlesClass(ModuleDesc moduleDesc, ClassDesc classDesc) {
        return TARGETS.containsKey(classDesc);
    }

    @Override
    public ClassTransform transform(ModuleDesc moduleDesc, ClassDesc classDesc, ClassModel classModel) {
        var transform = ClassTransform.ACCEPT_ALL;
        for (var entry : TARGETS.get(classDesc).entrySet())
            transform = transform.andThen(redirectFieldToMethod(classDesc, classModel, entry.getKey(), entry.getValue()));
        return transform;
    }

    /**
     * Rewrites reads of a private instance field to calls to a getter that takes no parameters and returns the
     * field's type. The getter itself still reads the field.
     */
    private static ClassTransform redirectFieldToMethod(ClassDesc owner, ClassModel classModel, String fieldName, String methodName) {
        FieldModel field = null;
        for (var candidate : classModel.fields()) {
            if (candidate.fieldName().equalsString(fieldName)) {
                if (field != null)
                    throw new IllegalStateException("Found multiple fields with name " + fieldName);
                field = candidate;
            }
        }
        if (field == null)
            throw new IllegalStateException("No field with name " + fieldName + " found");

        var flags = field.flags();
        if (!flags.has(AccessFlag.PRIVATE) || flags.has(AccessFlag.STATIC))
            throw new IllegalStateException("Field " + owner.displayName() + '.' + fieldName + " is not private and an instance field");

        var fieldType = field.fieldTypeSymbol();
        var methodType = MethodTypeDesc.of(fieldType);
        var hasGetter = classModel.methods().stream().anyMatch(method -> isMethod(method, methodName, methodType));
        if (!hasGetter)
            throw new IllegalStateException("Unable to find method " + methodName + methodType.descriptorString());

        CodeTransform redirect = (builder, element) -> {
            if (element instanceof FieldInstruction insn && insn.opcode() == Opcode.GETFIELD
                    && insn.name().equalsString(fieldName) && insn.owner().asSymbol().equals(owner)) {
                builder.invokevirtual(owner, methodName, methodType);
            } else {
                builder.with(element);
            }
        };

        return ClassTransform.transformingMethods(
            method -> !isMethod(method, methodName, methodType),
            MethodTransform.transformingCode(redirect)
        );
    }

    private static boolean isMethod(MethodModel method, String name, MethodTypeDesc type) {
        return method.methodName().equalsString(name) && method.methodTypeSymbol().equals(type);
    }
}
