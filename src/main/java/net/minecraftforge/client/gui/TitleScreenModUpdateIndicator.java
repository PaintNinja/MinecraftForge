/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

// TODO [ModuleLoader] Draw the update icon on the mods button again once VersionChecker is reworked. It only
//  supported the "recommended" and "latest" channels and picked one based on your Forge build.
public class TitleScreenModUpdateIndicator extends Screen {
    private final Button modButton;

    public TitleScreenModUpdateIndicator(Button modButton) {
        super(Component.translatable("forge.menu.updatescreen.title"));
        this.modButton = modButton;
    }

    @Override
    public void init() {}

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {}

    public static TitleScreenModUpdateIndicator init(TitleScreen guiMainMenu, Button modButton) {
        TitleScreenModUpdateIndicator titleScreenModUpdateIndicator = new TitleScreenModUpdateIndicator(modButton);
        //titleScreenModUpdateIndicator.resize(guiMainMenu.getMinecraft(), guiMainMenu.width, guiMainMenu.height);
        titleScreenModUpdateIndicator.init();
        return titleScreenModUpdateIndicator;
    }

}
