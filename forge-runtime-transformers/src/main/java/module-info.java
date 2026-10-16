/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

import ga.ozli.moduleloader.core.api.transform.ClassFileTransformer;
import net.minecraftforge.transformers.CapabilityTokenTransformer;
import net.minecraftforge.transformers.FieldToMethodTransformer;
import net.minecraftforge.transformers.MethodRedirector;

module net.minecraftforge.transformers {
    requires ga.ozli.moduleloader.core;
    requires com.google.gson;

    provides ClassFileTransformer with
        CapabilityTokenTransformer,
        FieldToMethodTransformer,
        MethodRedirector;
}
