/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.transformers;

import ga.ozli.moduleloader.core.api.transform.ClassFileTransformer;

import java.lang.classfile.AccessFlags;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.MethodModel;
import java.lang.classfile.Signature;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.constant.ModuleDesc;

/**
 * Implements {@code getType()} in subclasses of Forge's {@code CapabilityToken}, using the generic type in the
 * subclass' signature.
 * <p>For example, {@code new CapabilityToken<String>(){}} gets a {@code getType()} that returns
 * {@code "java/lang/String"}.</p>
 * <p>Forge's classes are in the game layer, which this transformer's layer can't see, so they're only referred to by
 * name.</p>
 */
public final class CapabilityTokenTransformer implements ClassFileTransformer {
    private static final String CAP_TOKEN = "net/minecraftforge/common/capabilities/CapabilityToken";
    private static final ClassDesc CD_CAP_TOKEN = ClassDesc.ofInternalName(CAP_TOKEN);
    private static final String GET_TYPE = "getType";
    private static final MethodTypeDesc MTD_GET_TYPE = MethodTypeDesc.of(ConstantDescs.CD_String);

    // getType() is final in source so mods don't override it, but each subclass needs its own implementation
    private static final ClassTransform UNFINAL_GET_TYPE = ClassTransform.transformingMethods(
        CapabilityTokenTransformer::isGetType,
        (builder, element) -> {
            if (element instanceof AccessFlags flags)
                builder.withFlags(flags.flagsMask() & ~ClassFile.ACC_FINAL);
            else
                builder.with(element);
        }
    );

    @Override
    public boolean handlesModule(ModuleDesc moduleDesc) {
        // Tokens can be in any mod, and Forge's own tokens and CapabilityToken itself are in the minecraft module
        return true;
    }

    @Override
    public boolean handlesClass(ModuleDesc moduleDesc, ClassDesc classDesc) {
        var name = classDesc.descriptorString();
        if (name.startsWith("Lnet/minecraft/") || name.startsWith("Lcom/mojang/"))
            return false;

        // Tokens are anonymous subclasses, so only nested classes need a closer look
        return CD_CAP_TOKEN.equals(classDesc) || name.indexOf('$') != -1;
    }

    @Override
    public ClassTransform transform(ModuleDesc moduleDesc, ClassDesc classDesc, ClassModel classModel) {
        if (CD_CAP_TOKEN.equals(classDesc))
            return UNFINAL_GET_TYPE;

        var superclass = classModel.superclass();
        if (superclass.isEmpty() || !superclass.get().name().equalsString(CAP_TOKEN))
            return ClassTransform.ACCEPT_ALL;

        var type = findTokenType(classModel);
        return ClassTransform.endHandler(builder -> builder.withMethodBody(GET_TYPE, MTD_GET_TYPE, ClassFile.ACC_PUBLIC,
            code -> code.ldc(type).areturn()
        ));
    }

    private static boolean isGetType(MethodModel method) {
        return method.methodName().equalsString(GET_TYPE) && method.methodTypeSymbol().equals(MTD_GET_TYPE);
    }

    /** @return The internal name of the class in the token's type argument, such as {@code java/lang/String} */
    private static String findTokenType(ClassModel classModel) {
        var signature = classModel.findAttribute(Attributes.signature())
            .map(attribute -> attribute.asClassSignature().superclassSignature())
            .orElse(null);

        if (signature != null && signature.typeArgs().size() == 1
                && signature.typeArgs().getFirst() instanceof Signature.TypeArg.Bounded bounded
                && bounded.wildcardIndicator() == Signature.TypeArg.Bounded.WildcardIndicator.NONE
                && bounded.boundType() instanceof Signature.ClassTypeSig typeSig) {
            return internalName(typeSig);
        }

        throw new IllegalStateException("Could not find signature for CapabilityToken on " + classModel.thisClass().asInternalName()
            + " from " + classModel.findAttribute(Attributes.signature()).map(Object::toString).orElse("null"));
    }

    private static String internalName(Signature.ClassTypeSig typeSig) {
        return typeSig.outerType()
            .map(outer -> internalName(outer) + '$' + typeSig.className())
            .orElse(typeSig.className());
    }
}
