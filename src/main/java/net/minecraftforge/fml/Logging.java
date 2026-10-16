/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/** Log4j markers carried over from FML, so log filters that use them keep working */
public final class Logging {
    public static final Marker CORE = MarkerManager.getMarker("CORE");
    public static final Marker LOADING = MarkerManager.getMarker("LOADING");
    public static final Marker CAPABILITIES = MarkerManager.getMarker("CAPABILITIES");
    public static final Marker FORGEMOD = MarkerManager.getMarker("FORGEMOD").addParents(LOADING);

    private Logging() {}
}
