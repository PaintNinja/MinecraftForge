/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.CompletableFuture;

import static net.minecraftforge.common.Tags.EntityTypes.*;

@ApiStatus.Internal
public final class ForgeEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public ForgeEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, "forge", existingFileHelper);
    }

    @Override
    public void addTags(HolderLookup.Provider lookupProvider) {
        tag(BOSSES)
            .add(EntityType.ENDER_DRAGON, EntityType.WITHER)
            .addOptionalTag(forgeTagKey("bosses"));
        tag(MINECARTS).add(
            EntityType.MINECART,
            EntityType.CHEST_MINECART,
            EntityType.FURNACE_MINECART,
            EntityType.HOPPER_MINECART,
            EntityType.SPAWNER_MINECART,
            EntityType.TNT_MINECART,
            EntityType.COMMAND_BLOCK_MINECART
        );
        tag(BOATS).add(
            EntityType.ACACIA_BOAT,
            EntityType.ACACIA_CHEST_BOAT,
            EntityType.BIRCH_BOAT,
            EntityType.BIRCH_CHEST_BOAT,
            EntityType.CHERRY_BOAT,
            EntityType.CHERRY_CHEST_BOAT,
            EntityType.DARK_OAK_BOAT,
            EntityType.DARK_OAK_CHEST_BOAT,
            EntityType.JUNGLE_BOAT,
            EntityType.JUNGLE_CHEST_BOAT,
            EntityType.MANGROVE_BOAT,
            EntityType.MANGROVE_CHEST_BOAT,
            EntityType.OAK_BOAT,
            EntityType.OAK_CHEST_BOAT,
            EntityType.PALE_OAK_BOAT,
            EntityType.PALE_OAK_CHEST_BOAT,
            EntityType.SPRUCE_BOAT,
            EntityType.SPRUCE_CHEST_BOAT
        );
        tag(ANIMALS).add(
                EntityType.ARMADILLO,
                EntityType.AXOLOTL,
                EntityType.BEE,
                EntityType.CAMEL,
                EntityType.CAT,
                EntityType.CHICKEN,
                EntityType.COD,
                EntityType.COW,
                EntityType.DOLPHIN,
                EntityType.DONKEY,
                EntityType.FOX,
                EntityType.FROG,
                EntityType.GLOW_SQUID,
                EntityType.GOAT,
                EntityType.HOGLIN,
                EntityType.HORSE,
                EntityType.LLAMA,
                EntityType.MOOSHROOM,
                EntityType.MULE,
                EntityType.OCELOT,
                EntityType.PANDA,
                EntityType.PARROT,
                EntityType.PIG,
                EntityType.POLAR_BEAR,
                EntityType.PUFFERFISH,
                EntityType.RABBIT,
                EntityType.SALMON,
                EntityType.SHEEP,
                EntityType.SKELETON_HORSE,
                EntityType.SNIFFER,
                EntityType.SQUID,
                EntityType.STRIDER,
                EntityType.TADPOLE,
                EntityType.TRADER_LLAMA,
                EntityType.TROPICAL_FISH,
                EntityType.TURTLE,
                EntityType.WOLF,
                EntityType.ZOMBIE_HORSE
        );
        tag(CAPTURING_NOT_SUPPORTED);
        tag(TELEPORTING_NOT_SUPPORTED);

        // Backwards compat definitions for pre-1.21 legacy `forge:` tags.
        // TODO: Remove backwards compat tag entries in 1.22
        tag(forgeTagKey("bosses")).add(EntityType.ENDER_DRAGON, EntityType.WITHER);
    }

    private static TagKey<EntityType<?>> forgeTagKey(String path) {
        return EntityTypeTags.create(ResourceLocation.fromNamespaceAndPath("forge", path));
    }

    @Override
    public String getName() {
        return "Forge EntityType Tags";
    }
}
