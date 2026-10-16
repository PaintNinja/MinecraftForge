/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.server.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

import java.util.Locale;
import java.util.stream.Collectors;

class ModListCommand {
    static ArgumentBuilder<CommandSourceStack, ?> register()
    {
        return Commands.literal("mods")
                .requires(Commands.hasPermission(Commands.LEVEL_ALL))
                .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.translatable("commands.forge.mods.list",
                                    ModList.getModIds().stream().map(modId ->
                                            // modid (version)
                                            String.format(Locale.ROOT, "%s (%s)", modId, ModList.getVersion(modId).orElse("?"))).
                                        collect(Collectors.joining("\n\u2022 ","\n\u2022 ", ""))),
                                    false);
                            return 0;
                        }
                );
    }

}
