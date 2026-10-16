/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.loading;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Well known game directories. The game directory is the working directory, as ModuleLoader also finds the mods
 * folder relative to it.
 */
public enum FMLPaths {
    GAMEDIR(""),
    MODSDIR("mods"),
    CONFIGDIR("config");

    private final Path absolutePath;

    FMLPaths(String relativePath) {
        this.absolutePath = Path.of(relativePath).toAbsolutePath().normalize();
    }

    /** @return The absolute path of this directory, created if it doesn't exist yet */
    public Path get() {
        if (!Files.isDirectory(this.absolutePath)) {
            try {
                Files.createDirectories(this.absolutePath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return this.absolutePath;
    }
}
