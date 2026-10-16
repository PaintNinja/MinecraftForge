/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.event.lifecycle;

import net.minecraftforge.eventbus.api.bus.EventBus;
import org.jetbrains.annotations.ApiStatus;
import net.minecraftforge.eventbus.api.event.characteristic.SelfDestructing;

/**
 * This is a mostly internal event fired to mod containers that indicates that loading is complete. Mods should not
 * in general override or otherwise attempt to implement this event.
 *
 * @author cpw
 */
public final class FMLLoadCompleteEvent extends ParallelDispatchEvent implements SelfDestructing {
    public static final EventBus<FMLLoadCompleteEvent> BUS = EventBus.create(FMLLoadCompleteEvent.class);

    @ApiStatus.Internal
    public FMLLoadCompleteEvent() {}
}
