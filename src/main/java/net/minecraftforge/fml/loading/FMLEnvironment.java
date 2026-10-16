/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.loading;

import net.minecraftforge.api.distmarker.Dist;

/**
 * Facts about the environment the game was launched in, read from system properties set by the launcher.
 */
public final class FMLEnvironment {
    /** The physical side, from the {@code moduleloader.dedicatedServer} property that ModuleLoader also reads */
    public static final Dist dist = Boolean.getBoolean("moduleloader.dedicatedServer") ? Dist.DEDICATED_SERVER : Dist.CLIENT;

    /** If this isn't a development environment, which sets the {@code forge.dev} property */
    public static final boolean production = !Boolean.getBoolean("forge.dev");

    private FMLEnvironment() {}
}
