/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;

import java.util.function.BooleanSupplier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.LogicalSide;

public abstract class TickEvent extends Event {
    public enum Type {
        LEVEL, PLAYER, CLIENT, SERVER, RENDER
    }

    public final Type type;
    public final LogicalSide side;

    public TickEvent(Type type, LogicalSide side) {
        this.type = type;
        this.side = side;
    }

    public static abstract class ServerTickEvent extends TickEvent {
        private final BooleanSupplier haveTime;
        private final MinecraftServer server;

        public ServerTickEvent(BooleanSupplier haveTime, MinecraftServer server) {
            super(Type.SERVER, LogicalSide.SERVER);
            this.haveTime = haveTime;
            this.server = server;
        }

        /**
         * @return {@code true} whether the server has enough time to perform any
         *         additional tasks (usually IO related) during the current tick,
         *         otherwise {@code false}
         */
        public boolean haveTime()
        {
            return this.haveTime.getAsBoolean();
        }
        
        /**
         * {@return the server instance}
         */
        public MinecraftServer getServer()
        {
            return server;
        }

        public static class Pre extends ServerTickEvent {
            public Pre(BooleanSupplier haveTime, MinecraftServer server) {
                super(haveTime, server);
            }
        }

        public static class Post extends ServerTickEvent {
            public Post(BooleanSupplier haveTime, MinecraftServer server) {
                super(haveTime, server);
            }
        }
    }

    public static abstract class ClientTickEvent extends TickEvent {
        public ClientTickEvent() {
            super(Type.CLIENT, LogicalSide.CLIENT);
        }

        public static class Pre extends ClientTickEvent {
            public Pre() {}
        }

        public static class Post extends ClientTickEvent {
            public Post() {}
        }
    }

    public static abstract class LevelTickEvent extends TickEvent {
        public final Level level;
        private final BooleanSupplier haveTime;

        public LevelTickEvent(LogicalSide side, Level level, BooleanSupplier haveTime) {
            super(Type.LEVEL, side);
            this.level = level;
            this.haveTime = haveTime;
        }

        /**
         * @return {@code true} whether the server has enough time to perform any
         *         additional tasks (usually IO related) during the current tick,
         *         otherwise {@code false}
         *
         * @see ServerTickEvent#haveTime()
         */
        public boolean haveTime()
        {
            return this.haveTime.getAsBoolean();
        }

        public static class Pre extends LevelTickEvent {
            public Pre(LogicalSide side, Level level, BooleanSupplier haveTime) {
                super(side, level, haveTime);
            }
        }

        public static class Post extends LevelTickEvent {
            public Post(LogicalSide side, Level level, BooleanSupplier haveTime) {
                super(side, level, haveTime);
            }
        }
    }

    public static abstract class PlayerTickEvent extends TickEvent {
        public final Player player;

        public PlayerTickEvent(Player player) {
            super(Type.PLAYER, player instanceof ServerPlayer ? LogicalSide.SERVER : LogicalSide.CLIENT);
            this.player = player;
        }

        public static class Pre extends PlayerTickEvent {
            public Pre(Player player) {
                super(player);
            }
        }

        public static class Post extends PlayerTickEvent {
            public Post(Player player) {
                super(player);
            }
        }
    }

    public static abstract class RenderTickEvent extends TickEvent {
        public final float renderTickTime;

        public RenderTickEvent(float renderTickTime) {
            super(Type.RENDER, LogicalSide.CLIENT);
            this.renderTickTime = renderTickTime;
        }

        public static class Pre extends RenderTickEvent {
            public Pre(float renderTickTime) {
                super(renderTickTime);
            }
        }

        public static class Post extends RenderTickEvent {
            public Post(float renderTickTime) {
                super(renderTickTime);
            }
        }
    }
}
