/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

import ga.ozli.novamodloader.api.ModuleLoaderMod;

// The template ships its own copy of this, without the Datagen entrypoint
module examplemod {
    requires minecraft;
    requires ga.ozli.novamodloader;
    requires net.minecraftforge.eventbus;
    requires org.slf4j;
    requires logging;

    provides ModuleLoaderMod.Common with com.example.examplemod.ExampleMod, net.minecraftforge.mdk.Datagen;
    provides ModuleLoaderMod.Client with com.example.examplemod.ExampleMod.Client;
}
