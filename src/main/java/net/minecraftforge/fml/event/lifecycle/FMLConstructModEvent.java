/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.event.lifecycle;

import net.minecraftforge.eventbus.api.bus.EventBus;
import org.jetbrains.annotations.ApiStatus;
import net.minecraftforge.eventbus.api.event.characteristic.SelfDestructing;

/**
 * Supplied as a param to your mod's constructor to get access
 * to the mod EventBus and various mod-specific objects.
 */
public final class FMLConstructModEvent extends ParallelDispatchEvent implements SelfDestructing {
    public static final EventBus<FMLConstructModEvent> BUS = EventBus.create(FMLConstructModEvent.class);

    @ApiStatus.Internal
    public FMLConstructModEvent() {}
}
