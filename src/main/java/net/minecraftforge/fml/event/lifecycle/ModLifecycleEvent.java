/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.event.lifecycle;

import net.minecraftforge.eventbus.api.event.InheritableEvent;

/**
 * Parent type to all mod lifecycle events. Each is posted once on its {@code BUS}, in the order set by
 * {@link net.minecraftforge.internal.ModLifecycle}.
 */
// TODO [ModuleLoader] The IMC streams (getIMCStream) were removed with FML's InterModComms
public abstract class ModLifecycleEvent implements InheritableEvent {
    protected ModLifecycleEvent() {}

    public final String description() {
       String cn = getClass().getName();
       return cn.substring(cn.lastIndexOf('.')+1);
    }

    @Override
    public String toString() {
        return description();
    }
}
