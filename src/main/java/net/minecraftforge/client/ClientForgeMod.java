/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.client;

import ga.ozli.novamodloader.api.ModuleLoaderMod;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterNamedRenderTypesEvent;
import net.minecraftforge.client.model.DynamicFluidContainerModel;
import net.minecraftforge.client.model.data.ModelDataManager;
import net.minecraftforge.client.model.obj.ObjLoader;
import net.minecraftforge.event.level.ChunkEvent;

public final class ClientForgeMod implements ModuleLoaderMod.Client {
    public ClientForgeMod() {
        ModelEvent.RegisterGeometryLoaders.BUS.addListener(ClientForgeMod::onRegisterGeometryLoaders);
        RegisterClientReloadListenersEvent.BUS.addListener(ClientForgeMod::onRegisterReloadListeners);
        RegisterNamedRenderTypesEvent.BUS.addListener(ClientForgeMod::onRegisterNamedRenderTypes);

        ChunkEvent.Unload.BUS.addListener(ModelDataManager::onChunkUnload);
    }

    private static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(forgeRL("empty"), (_, _) -> UnbakedGeometry.EMPTY);
        event.register(forgeRL("obj"), ObjLoader.INSTANCE);
        event.register(forgeRL("fluid_container"), DynamicFluidContainerModel.Loader.INSTANCE);
    }

    private static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(ObjLoader.INSTANCE);
    }

    private static void onRegisterNamedRenderTypes(RegisterNamedRenderTypesEvent event) {
        event.register(forgeRL("item_unlit"), ChunkSectionLayer.TRANSLUCENT, ForgeRenderTypes.ITEM_UNSORTED_UNLIT_TRANSLUCENT.get());
    }

    private static Identifier forgeRL(String path) {
        return Identifier.fromNamespaceAndPath("forge", path);
    }
}
