/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.loading;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Reads the specification and implementation versions from a package's section in its jar's manifest.
 * <p>Packages in named modules don't get this info from the JDK, so the manifest is read from the class' module.</p>
 */
public final class JarVersionLookupHandler {
    private JarVersionLookupHandler() {}

    public record Version(Optional<String> title, Optional<String> vendor, Optional<String> version) {}

    public record Info(Version spec, Version impl) {}

    public static Info getInfo(Class<?> clazz) {
        var attributes = new Attributes();
        try (var in = clazz.getModule().getResourceAsStream("META-INF/MANIFEST.MF")) {
            if (in != null) {
                var section = new Manifest(in).getAttributes(clazz.getPackageName().replace('.', '/') + '/');
                if (section != null)
                    attributes = section;
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read the manifest of " + clazz.getModule(), e);
        }

        return new Info(
            version(attributes, Attributes.Name.SPECIFICATION_TITLE, Attributes.Name.SPECIFICATION_VENDOR, Attributes.Name.SPECIFICATION_VERSION),
            version(attributes, Attributes.Name.IMPLEMENTATION_TITLE, Attributes.Name.IMPLEMENTATION_VENDOR, Attributes.Name.IMPLEMENTATION_VERSION)
        );
    }

    private static Version version(Attributes attributes, Attributes.Name title, Attributes.Name vendor, Attributes.Name version) {
        return new Version(
            Optional.ofNullable(attributes.getValue(title)),
            Optional.ofNullable(attributes.getValue(vendor)),
            Optional.ofNullable(attributes.getValue(version))
        );
    }
}
