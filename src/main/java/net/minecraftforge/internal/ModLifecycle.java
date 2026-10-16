/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.internal;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.ApiStatus;

/**
 * Runs the mod loading lifecycle, in the same order FML's state machine did. Mods have already been constructed by
 * ModuleLoader through their entrypoints by the time this runs.
 * <p>
 * Each phase is called by the side's loader, {@code ClientModLoader} or {@code ServerModLoader}, on the main thread.
 */
// TODO [ModuleLoader] Replace the lifecycle events with service loaded interfaces that can be called in parallel
@ApiStatus.Internal
public final class ModLifecycle {
    private static volatile boolean registriesLoaded;

    private ModLifecycle() {}

    /** Formerly FML's GATHER phase */
    public static void gather() {
        FMLConstructModEvent.BUS.post(new FMLConstructModEvent());
        RegistryManager.postNewRegistryEvent();
        CapabilityManager.injectCapabilities();
        GameData.unfreezeData();
        GameData.postRegisterEvents();
        registriesLoaded = true;
    }

    /** Formerly FML's LOAD phase */
    public static void load(Dist dist) {
        // TODO [ModuleLoader] Load the client and common configs here, pending the config system redesign
        FMLCommonSetupEvent.BUS.post(new FMLCommonSetupEvent());
        if (dist.isClient())
            FMLClientSetupEvent.BUS.post(new FMLClientSetupEvent());
        else
            FMLDedicatedServerSetupEvent.BUS.post(new FMLDedicatedServerSetupEvent());
    }

    /** Formerly FML's COMPLETE phase */
    public static void finish() {
        InterModEnqueueEvent.BUS.post(new InterModEnqueueEvent());
        InterModProcessEvent.BUS.post(new InterModProcessEvent());
        FMLLoadCompleteEvent.BUS.post(new FMLLoadCompleteEvent());
        GameData.freezeData();
        NetworkRegistry.lock();
    }

    /** @return If the registry events have been posted */
    public static boolean hasLoadedRegistries() {
        return registriesLoaded;
    }
}
