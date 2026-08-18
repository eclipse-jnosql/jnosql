/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 */
package org.eclipse.jnosql.mapping.core.config;

import org.eclipse.jnosql.communication.Settings;

import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * An optional provider for the {@link Settings} used by mapping internals.
 *
 * <p>Providers are discovered with the class loader that defined this
 * interface; the thread context class loader is not consulted. When multiple
 * providers are visible, the first provider in
 * {@link ServiceLoader} iteration order supplies the settings; installing a
 * single provider is recommended. When no provider is visible, mapping lazily
 * retains its existing {@link MicroProfileSettings#INSTANCE} behavior.</p>
 *
 * <p>A provider must return non-null settings. Service configuration and
 * provider-construction failures, including {@link ServiceConfigurationError},
 * and provider-invocation failures propagate without falling back.</p>
 *
 * @since 1.1.17
 */
public interface MappingSettingsProvider {

    /**
     * Returns the settings to use for the current mapping context.
     *
     * @return the non-null settings
     */
    Settings getSettings();

    /**
     * Resolves settings from the first provider visible to the class loader
     * that defined this interface, or lazily falls back to
     * {@link MicroProfileSettings#INSTANCE} when no provider is available.
     *
     * @return the resolved settings
     * @throws NullPointerException when a provider returns {@code null}
     * @throws ServiceConfigurationError when provider configuration is invalid
     *         or a provider cannot be constructed
     */
    static Settings resolve() {
        return MappingSettingsResolver.resolve(MappingSettingsProvider.class.getClassLoader());
    }
}

final class MappingSettingsResolver {

    private MappingSettingsResolver() {
    }

    static Settings resolve(ClassLoader classLoader) {
        for (MappingSettingsProvider provider : ServiceLoader.load(MappingSettingsProvider.class, classLoader)) {
            return Objects.requireNonNull(provider.getSettings(),
                    "MappingSettingsProvider.getSettings() must not return null");
        }
        return MicroProfileSettings.INSTANCE;
    }
}
