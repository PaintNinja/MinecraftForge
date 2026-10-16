/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.server.loading;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.internal.ModLifecycle;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.server.LanguageHook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ServerModLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean hasErrors = false;

    public static void load() {
        LogicalSidedProvider.setServer(()-> {
            throw new IllegalStateException("Unable to access server yet");
        });
        LanguageHook.loadForgeAndMCLangs();
        // TODO [ModuleLoader] FML wrote a mod loading crash report here and logged the loading warnings
        try {
            ModLifecycle.gather();
            ModLifecycle.load(Dist.DEDICATED_SERVER);
            ModLifecycle.finish();
        } catch (RuntimeException | Error e) {
            ServerModLoader.hasErrors = true;
            throw e;
        }
        BusGroup.DEFAULT.startup();
    }

    public static boolean hasErrors() {
        return ServerModLoader.hasErrors;
    }
}
