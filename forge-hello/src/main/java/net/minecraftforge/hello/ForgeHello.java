/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.hello;

import com.mojang.logging.LogUtils;
import ga.ozli.novamodloader.api.ModuleLoaderMod;
import org.slf4j.Logger;

public final class ForgeHello implements ModuleLoaderMod.Common {
    public static final String MODID = "forge_hello";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ForgeHello() {
        LOGGER.info("FORGE HELLO: mod constructed");
    }
}