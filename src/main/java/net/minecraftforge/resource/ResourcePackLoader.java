/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.resource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.server.packs.repository.Pack.Position;
import net.minecraft.DetectedVersion;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.fml.Logging;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ResourcePackLoader {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void loadResourcePacks(PackRepository resourcePacks, boolean client) {
        resourcePacks.addPackFinder(repo -> findPacks(repo, client));
    }

    /**
     * The mods that may have resources. A module with a module-info that doesn't require Minecraft is a library, so
     * only automatic modules and modules that require Minecraft are searched.
     */
    private static List<String> getPackModIds() {
        return ModList.getModIds().stream()
                .filter(id -> ModList.getModule(id)
                        .map(Module::getDescriptor)
                        .filter(descriptor -> descriptor.isAutomatic() || descriptor.requires().stream().anyMatch(r -> r.name().equals("minecraft")))
                        .isPresent())
                .toList();
    }

    public static List<String> getPackNames() {
        return getPackModIds().stream()
                .filter(id -> !id.equals("minecraft"))
                .map(id -> "mod:" + id)
                .toList();
    }

    public static <V> Comparator<Map.Entry<String,V>> getSorter() {
        List<String> order = new ArrayList<>();
        order.add("vanilla");
        order.add("mod_resources");

        for (var id : getPackModIds()) {
            if (!id.equals("minecraft"))
                order.add("mod:" + id);
        }

        final Object2IntMap<String> order_f = new Object2IntOpenHashMap<>(order.size());
        for (int x = 0; x < order.size(); x++)
            order_f.put(order.get(x), x);

        return (e1, e2) -> {
            final String s1 = e1.getKey();
            final String s2 = e2.getKey();
            final int i1 = order_f.getOrDefault(s1, -1);
            final int i2 = order_f.getOrDefault(s2, -1);

            if (i1 == i2 && i1 == -1)
                return s1.compareTo(s2);
            if (i1 == -1) return 1;
            if (i2 == -1) return -1;
            return i2 - i1;
        };
    }

    private static void findPacks(Consumer<Pack> packAcceptor, boolean client) {
        var type = client ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA;
        var version = DetectedVersion.BUILT_IN.packVersion(type);
        var hiddenPacks = new ArrayList<PackResources>();

        // Vanilla's resources come from VanillaPackResources. Forge's id points at the same merged jar for now, so its
        // pack also contains vanilla's files.
        // TODO [ModuleLoader] Revisit once Forge is split into its own module
        for (var modId : getPackModIds()) {
            if (modId.equals("minecraft"))
                continue;

            var root = ModList.getRoot(modId).orElse(null);
            if (root == null)
                continue;

            var supplier = new PathPackResources.PathResourcesSupplier(root);

            var name = "mod:" + modId;
            var info = new PackLocationInfo(name, Component.literal(modId), PackSource.DEFAULT, Optional.empty());
            var meta = Pack.readPackMetadata(info, supplier, version, type);
            Pack pack = null;
            if (meta != null)
                pack = new Pack(info, supplier, meta, new PackSelectionConfig(false, Position.BOTTOM, false));

            if (pack == null) {
                // Vanilla only logs an error, instead of propagating, so handle null and warn that something went wrong
                LOGGER.warn(Logging.CORE, "Could not find resource pack for mod {}", modId);
                continue;
            }

            LOGGER.debug(Logging.CORE, "Generating PackInfo named {} for mod {}", name, modId);
            // TODO [ModuleLoader] Let mods show up as their own resource pack again, mods.toml had a showAsResourcePack option
            if (!client)
                packAcceptor.accept(pack);
            else
                pack.open().forEach(hiddenPacks::add);
        }

        if (!hiddenPacks.isEmpty()) {
            var info = new PackLocationInfo("mod_resources", Component.translatable("fml.resources.modresources", hiddenPacks.size()), PackSource.DEFAULT, Optional.empty());
            @SuppressWarnings("resource")
            var delegating = new DelegatingPackResources(info,
                new PackMetadataSection(
                    Component.translatable("fml.resources.modresources", hiddenPacks.size()),
                    version.minorRange()
                ),
                hiddenPacks
            );

            var supplier = delegating.supplier();

            // Create a resource pack merging all mod resources that should be hidden
            var wrapper = new PackLocationInfo("mod_resources", Component.literal("Mod Resources"), PackSource.DEFAULT, Optional.empty());
            var wrapperCfg = new PackSelectionConfig(true, Position.BOTTOM, false);
            var modResourcesPack = Pack.readMetaAndCreate(wrapper, supplier, type, wrapperCfg);
            packAcceptor.accept(modResourcesPack);
        }
    }
}
