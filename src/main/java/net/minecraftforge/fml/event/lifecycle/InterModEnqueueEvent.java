/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.event.lifecycle;

import net.minecraftforge.eventbus.api.bus.EventBus;
import org.jetbrains.annotations.ApiStatus;

/**
 * This is the third of four commonly called events during mod core startup.
 *
 * Called before {@link InterModProcessEvent}
 * Called after {@link FMLClientSetupEvent} or {@link FMLDedicatedServerSetupEvent}
 *
 *
 * Enqueue {@link net.minecraftforge.fml.InterModComms} messages to other mods with this event.
 *
 * This is a parallel dispatch event.
 */
public final class InterModEnqueueEvent extends ParallelDispatchEvent {
    public static final EventBus<InterModEnqueueEvent> BUS = EventBus.create(InterModEnqueueEvent.class);

    @ApiStatus.Internal
    public InterModEnqueueEvent() {}
}
