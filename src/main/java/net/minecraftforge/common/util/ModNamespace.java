/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.util;

import ga.ozli.novamodloader.api.ModuleLoaderMod;
import net.minecraft.resources.Identifier;

/**
 * A mod's namespace is the name of its module. The exception is Forge, which keeps the {@code forge} namespace
 * for compatibility with existing registries, tags, network channels and worlds.
 */
public final class ModNamespace {
    public static final String FORGE = "forge";

    private static final Module FORGE_MODULE = ModNamespace.class.getModule();

    private ModNamespace() {}

    public static String of(ModuleLoaderMod entrypoint) {
        return of(entrypoint.getClass().getModule());
    }

    /**
     * @throws IllegalArgumentException if the module's name isn't a valid {@link Identifier} namespace
     */
    public static String of(Module module) {
        if (module == FORGE_MODULE)
            return FORGE;

        var name = module.getName();
        if (name == null || !Identifier.isValidNamespace(name))
            throw new IllegalArgumentException("Module name " + name + " is not a valid namespace, it may only contain [a-z0-9_.-]");

        return name;
    }
}
