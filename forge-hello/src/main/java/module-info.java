/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

module net.minecraftforge.forge.hello {
    requires minecraft;
    requires ga.ozli.novamodloader;
    requires org.spongepowered.mixin;
    requires org.slf4j;
    requires logging;

    exports net.minecraftforge.hello;

    provides ga.ozli.novamodloader.api.ModuleLoaderMod.Common with net.minecraftforge.hello.ForgeHello;
}