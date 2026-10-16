/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.logging;

public final class CrashReportAnalyser {
    private CrashReportAnalyser() {}

    // TODO [ModuleLoader] Bring back the suspected mods section, which mapped stack frames and mixins to mods through ModList
    public static String appendSuspectedMods(Throwable throwable, StackTraceElement[] uncategorizedStackTrace) {
        return "";
    }
}
