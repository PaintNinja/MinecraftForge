/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.network.packets;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraftforge.fml.ModList;

/**
 * Prefixes S2CModList by sending additional data about the mods installed on the server to the client
 * The mod data is stored as follows: [modId -> [modName, modVersion]]
 */
public record ModVersions(Map<String, Info> mods) {
    private static final StreamCodec<ByteBuf, String> STRING_CODEC = ByteBufCodecs.stringUtf8(0x100);
    public static final StreamCodec<FriendlyByteBuf, ModVersions> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, STRING_CODEC, StreamCodec.composite(
            STRING_CODEC, Info::name, STRING_CODEC, Info::version, Info::new
        )), ModVersions::mods,
        ModVersions::new
    );

    public static ModVersions create() {
        // TODO [ModuleLoader] Send display names again once mods can declare one, the id stands in for now
        return new ModVersions(ModList.getModIds().stream().collect(Collectors.toMap(
            modId -> modId,
            modId -> new Info(modId, ModList.getVersion(modId).orElse(""))
        )));
    }

    public record Info(String name, String version) { }
}