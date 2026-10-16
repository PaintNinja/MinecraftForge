/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.hello.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
abstract class MinecraftServerMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "runServer", at = @At("HEAD"))
    private void forgeHello$onRunServer(CallbackInfo ci) {
        LOGGER.info("FORGE HELLO: mixin applied to MinecraftServer.runServer");
    }
}
