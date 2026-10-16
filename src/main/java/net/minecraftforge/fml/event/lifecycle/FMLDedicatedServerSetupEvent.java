/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.event.lifecycle;

import net.minecraftforge.eventbus.api.bus.EventBus;
import org.jetbrains.annotations.ApiStatus;
import net.minecraftforge.eventbus.api.event.characteristic.SelfDestructing;

/**
 * This is the second of four commonly called events during mod core startup.
 *
 * Called before {@link InterModEnqueueEvent}
 * Called after {@link FMLCommonSetupEvent}
 *
 * Called on {@link net.minecraftforge.api.distmarker.Dist#DEDICATED_SERVER} - the dedicated game server.
 *
 * Alternative to {@link FMLClientSetupEvent}.
 *
 * Do dedicated server specific activities with this event.
 *
 * <em>This event is fired before construction of the dedicated server. Use {@code FMLServerAboutToStartEvent}
 * or {@code FMLServerStartingEvent} to do stuff with the server, in both dedicated
 * and integrated server contexts</em>
 *
 * This is a parallel dispatch event.
 */
public final class FMLDedicatedServerSetupEvent extends ParallelDispatchEvent implements SelfDestructing {
    public static final EventBus<FMLDedicatedServerSetupEvent> BUS = EventBus.create(FMLDedicatedServerSetupEvent.class);

    @ApiStatus.Internal
    public FMLDedicatedServerSetupEvent() {}
}
