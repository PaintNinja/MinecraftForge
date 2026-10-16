/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.logging;

import net.minecraft.SystemReport;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CrashReportExtender {
    private static final String LINE_SEPARATOR = System.lineSeparator();

    // TODO [ModuleLoader] Let mods add their own system report details again. FML's CrashReportCallables is gone.
    public static void extendSystemReport(final SystemReport systemReport) {}

    public static String generateEnhancedStackTrace(final Throwable throwable) {
        return generateEnhancedStackTrace(throwable, true);
    }

    public static String generateEnhancedStackTrace(final StackTraceElement[] stacktrace) {
        final Throwable t = new Throwable();
        t.setStackTrace(stacktrace);
        return generateEnhancedStackTrace(t, false);
    }

    // TODO [ModuleLoader] ModLauncher's converter also listed the transformers that touched each frame's class
    public static String generateEnhancedStackTrace(final Throwable throwable, boolean header) {
        final StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        final String s = writer.toString().stripTrailing();
        return header ? s : s.substring(s.indexOf(LINE_SEPARATOR));
    }
}
