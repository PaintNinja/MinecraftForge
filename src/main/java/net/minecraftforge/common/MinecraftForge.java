/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.DualStackUtils;
import net.minecraftforge.versions.forge.ForgeVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.function.BiFunction;
import java.util.function.Function;

public class MinecraftForge {
    /**
     * A helper for partial source-level back-compat with the old event system used in 1.21.5 and older, that acts as
     * a wrapper around {@link BusGroup#DEFAULT}.
     *
     * <p>Please refer to the javadocs for {@link EventBusMigrationHelper} for further information and resources on how
     * to properly migrate your mod to the new event system.</p>
     */
    public static final EventBusMigrationHelper EVENT_BUS = EventBusMigrationHelper.INSTANCE;

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker FORGE = MarkerManager.getMarker("FORGE");

   /**
    * Method invoked by FML before any other mods are loaded.
    */
   public static void initialize() {
       LOGGER.info(FORGE,"MinecraftForge v{} Initialized", ForgeVersion.getVersion());

       UsernameCache.load();
       if (FMLEnvironment.dist == Dist.CLIENT) ClientCommandHandler.init();
       DualStackUtils.initialise();
       TagConventionMigrationHelper.init();
   }

    /**
     * Register a config screen for the active mod container.
     * @param screenFunction A function that takes the mods screen as an argument and returns your config screen to
     *                       show when the player clicks the config button for your mod on the mods screen.
     *                       <p>You should call {@link Minecraft#setScreen(Screen)} with the provided mods screen for the
     *                       action of your close button, using {@link Screen#getMinecraft()} to get the client instance.</p>
     */
    public static void registerConfigScreen(Function<Screen, Screen> screenFunction) {
        registerConfigScreen((_, modsScreen) -> screenFunction.apply(modsScreen));
    }

    /**
     * Register a config screen for the active mod container.
     * @param screenFunction A function that takes the {@link Minecraft} client instance and the mods screen as
     *                       arguments and returns your config screen to show when the player clicks the config button
     *                       for your mod on the mods screen.
     *                       <p>You should call {@link Minecraft#setScreen(Screen)} with the provided client instance
     *                       and mods screen for the action of your close button.</p>
     */
    public static void registerConfigScreen(BiFunction<Minecraft, Screen, Screen> screenFunction) {
        // TODO [ModuleLoader] Config screen registration, pending the mods screen redesign
        //ModLoadingContext.get().registerExtensionPoint(
        //       ConfigScreenHandler.ConfigScreenFactory.class,
        //       () -> new ConfigScreenHandler.ConfigScreenFactory(screenFunction)
        //);
    }
}
