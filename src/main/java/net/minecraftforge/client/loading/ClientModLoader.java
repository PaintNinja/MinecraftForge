/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.client.loading;

import java.util.concurrent.*;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.PreparableReloadListener.SharedState;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.DataPackConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.internal.BrandingControl;
import net.minecraftforge.internal.ModLifecycle;
import net.minecraftforge.common.util.LogicalSidedProvider;
import org.apache.logging.log4j.LogManager;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.resource.ResourcePackLoader;
import net.minecraftforge.server.LanguageHook;

public final class ClientModLoader {
    private static boolean loading;
    private static Minecraft mc;
    private static boolean loadingComplete;

    public static void begin(final Minecraft minecraft, final PackRepository defaultResourcePacks, final ReloadableResourceManager mcResourceManager) {
        // force log4j to shutdown logging in a shutdown hook. This is because we disable default shutdown hook so the server properly logs it's shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(LogManager::shutdown));
        loading = true;
        ClientModLoader.mc = minecraft;
        LogicalSidedProvider.setClient(()->minecraft);
        LanguageHook.loadForgeAndMCLangs();
        // TODO [ModuleLoader] Errors thrown by mods crash the game for now. FML collected them for LoadingErrorScreen.
        ModLifecycle.gather();
        ResourcePackLoader.loadResourcePacks(defaultResourcePacks, true);
        net.minecraftforge.event.AddPackFindersEvent.BUS.post(new AddPackFindersEvent(PackType.CLIENT_RESOURCES, defaultResourcePacks::addPackFinder));
        DataPackConfig.DEFAULT.addModPacks(ResourcePackLoader.getPackNames());
        mcResourceManager.registerReloadListener(ClientModLoader::onResourceReload);
        mcResourceManager.registerReloadListener(BrandingControl.resourceManagerReloadListener());
    }

    // The rest of mod loading happens during the first resource reload, on the main thread
    private static CompletableFuture<Void> onResourceReload(SharedState state, final Executor asyncExecutor, final PreparableReloadListener.PreparationBarrier stage, final Executor syncExecutor) {
        return CompletableFuture.runAsync(() -> {
                    if (!loadingComplete) ModLifecycle.load(Dist.CLIENT);
                }, syncExecutor)
                .thenCompose(stage::wait)
                .thenRunAsync(ClientModLoader::finishModLoading, syncExecutor);
    }

    private static void finishModLoading() {
        if (loadingComplete) return;
        ModLifecycle.finish();
        loading = false;
        loadingComplete = true;
        // reload game settings now that mods have added their key bindings and the like
        mc.options.load(true);
    }

    /** @return If an error screen was shown instead of the title screen */
    // TODO [ModuleLoader] Show LoadingErrorScreen again once there's a replacement for FML's loading errors and warnings
    public static boolean completeModLoading() {
        return false;
    }

    public static boolean isLoading() {
        return loading;
    }
}
