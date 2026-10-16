/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.mdk;

import ga.ozli.novamodloader.api.ModuleLoaderMod;
import net.minecraft.DetectedVersion;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraftforge.data.event.GatherDataEvent;

public class Datagen implements ModuleLoaderMod.Common {
    public Datagen() {
        GatherDataEvent.BUS.addListener(event -> {
            if (event.isFor(this)) gatherData(event);
        });
    }

    private static void gatherData(GatherDataEvent event) {
        var gen = event.getGenerator();
        var packOutput = gen.getPackOutput();
        gen.addProvider(true, new PackMetadataGenerator(packOutput)
            .add(PackMetadataSection.SERVER_TYPE, new PackMetadataSection(
                Component.literal("${mod_id} resources"),
                DetectedVersion.BUILT_IN.packVersion(PackType.SERVER_DATA).minorRange()
            ))
        );
    }
}
