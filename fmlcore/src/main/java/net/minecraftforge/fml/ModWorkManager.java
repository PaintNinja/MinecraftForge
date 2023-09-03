/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml;

import net.minecraftforge.fml.loading.FMLConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

import static net.minecraftforge.fml.Logging.LOADING;

public class ModWorkManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final long PARK_TIME = TimeUnit.MILLISECONDS.toNanos(1);
    public interface DrivenExecutor extends Executor {
        boolean selfDriven();
        boolean driveOne();

        default void drive(Runnable ticker) {
            if (!selfDriven()) {
                ticker.run();
                while (true) {
                    if (!driveOne()) break;
                }
            } else {
                // park for a bit so other threads can schedule
                LockSupport.parkNanos(PARK_TIME);
            }
        }
    }
    private static class SyncExecutor implements DrivenExecutor {
        private ConcurrentLinkedDeque<Runnable> tasks = new ConcurrentLinkedDeque<>();

        @Override
        public boolean driveOne() {
            final Runnable task = tasks.pollFirst();
            if (task != null) {
                task.run();
            }
            return task != null;
        }

        @Override
        public boolean selfDriven() {
            return false;
        }

        @Override
        public void execute(final Runnable command) {
            tasks.addLast(command);
        }
    }

    private static class WrappingExecutor implements DrivenExecutor {
        private final Executor wrapped;

        public WrappingExecutor(final Executor executor) {
            this.wrapped = executor;
        }

        @Override
        public boolean selfDriven() {
            return true;
        }

        @Override
        public boolean driveOne() {
            return false;
        }

        @Override
        public void execute(final Runnable command) {
            wrapped.execute(command);
        }
    }

    private static SyncExecutor syncExecutor;

    public static DrivenExecutor syncExecutor() {
        if (syncExecutor == null)
            syncExecutor = new SyncExecutor();
        return syncExecutor;
    }

    public static DrivenExecutor wrappedExecutor(Executor executor) {
        return new WrappingExecutor(executor);
    }

    private static ExecutorService parallelThreadPool;
    public static Executor parallelExecutor() {
        if (parallelThreadPool == null) {
            if (FMLConfig.getBoolConfigValue(FMLConfig.ConfigValue.VIRTUAL_THREADS) && Runtime.version().feature() >= 19) {
                try {
                    parallelThreadPool = (ExecutorService) MethodHandles.lookup()
                            .findStatic(Executors.class, "newVirtualThreadPerTaskExecutor", MethodType.methodType(ExecutorService.class))
                            .invokeExact();
                    LOGGER.debug(LOADING, "Using virtual threads for parallel mod-loading (experimental)");
                } catch (Throwable e) {
                    LOGGER.warn(LOADING, "Unable to use virtual threads for parallel mod-loading, falling back to normal threads...");
                    LOGGER.warn(LOADING, "", e);
                }
            }

            if (parallelThreadPool == null) {
                final int loadingThreadCount = FMLConfig.getIntConfigValue(FMLConfig.ConfigValue.MAX_THREADS);
                LOGGER.debug(LOADING, "Using {} threads for parallel mod-loading", loadingThreadCount);

                parallelThreadPool = new ForkJoinPool(loadingThreadCount, ModWorkManager::newForkJoinWorkerThread, null, false);
            }
        }
        return parallelThreadPool;
    }

    private static ForkJoinWorkerThread newForkJoinWorkerThread(ForkJoinPool pool) {
        ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
        thread.setName("modloading-worker-" + thread.getPoolIndex());
        // The default sets it to the SystemClassloader, so copy the current one.
        thread.setContextClassLoader(Thread.currentThread().getContextClassLoader());
        return thread;
    }

}
