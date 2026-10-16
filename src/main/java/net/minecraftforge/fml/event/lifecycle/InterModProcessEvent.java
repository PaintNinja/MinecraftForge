/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.event.lifecycle;

import net.minecraftforge.eventbus.api.bus.EventBus;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

/**
 * This is the fourth of four commonly called events during mod core startup.
 *
 * Called after {@link InterModEnqueueEvent}
 *
 * Retrieve {@link net.minecraftforge.fml.InterModComms} {@link net.minecraftforge.fml.InterModComms.IMCMessage} suppliers
 * and process them as you wish with this event.
 *
 * This is a parallel dispatch event.
 *
 * @see #getIMCStream()
 * @see #getIMCStream(Predicate)
 */
public final class InterModProcessEvent extends ParallelDispatchEvent {
    public static final EventBus<InterModProcessEvent> BUS = EventBus.create(InterModProcessEvent.class);

    @ApiStatus.Internal
    public InterModProcessEvent() {}
}
