/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.event.lifecycle;

import net.minecraftforge.eventbus.api.bus.EventBus;
import org.jetbrains.annotations.ApiStatus;
import net.minecraftforge.eventbus.api.event.characteristic.SelfDestructing;

import java.util.function.Consumer;

/**
 * This is the first of four commonly called events during mod initialization.
 * <br><br>
 * Called after {@link net.minecraftforge.registries.RegisterEvent} events have been fired and before 
 * {@link FMLClientSetupEvent} or {@link FMLDedicatedServerSetupEvent} during mod startup.
 * <br><br>
 * Register your listener with {@link net.minecraftforge.eventbus.api.bus.EventBus#addListener(Consumer)} on
 * {@link #BUS} in your mod's entrypoint constructor.
 * <br><br>
 * Most non-specific mod setup will be performed here.
 */
public final class FMLCommonSetupEvent extends ParallelDispatchEvent implements SelfDestructing {
    public static final EventBus<FMLCommonSetupEvent> BUS = EventBus.create(FMLCommonSetupEvent.class);

    @ApiStatus.Internal
    public FMLCommonSetupEvent() {}
}
