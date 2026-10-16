/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.loading;

import net.minecraftforge.api.distmarker.Dist;

import java.nio.file.Path;

/**
 * What's left of FML's loader entrypoint, kept for the environment queries the game still makes.
 */
public final class FMLLoader {
    private FMLLoader() {}

    public static Dist getDist() {
        return FMLEnvironment.dist;
    }

    public static boolean isProduction() {
        return FMLEnvironment.production;
    }

    public static Path getGamePath() {
        return FMLPaths.GAMEDIR.get();
    }
}
