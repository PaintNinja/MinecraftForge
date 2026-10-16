/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.event.lifecycle;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Lifecycle events that FML dispatched to mods in parallel. They're now posted on the main thread, so work
 * enqueued here runs immediately.
 */
// TODO [ModuleLoader] Remove enqueueWork once these become service loaded interfaces called in parallel
public abstract class ParallelDispatchEvent extends ModLifecycleEvent {
    protected ParallelDispatchEvent() {}

    public CompletableFuture<Void> enqueueWork(Runnable work) {
        work.run();
        return CompletableFuture.completedFuture(null);
    }

    public <T> CompletableFuture<T> enqueueWork(Supplier<T> work) {
        return CompletableFuture.completedFuture(work.get());
    }
}
