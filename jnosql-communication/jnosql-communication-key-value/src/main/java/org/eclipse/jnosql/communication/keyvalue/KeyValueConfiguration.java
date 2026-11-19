/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 *  Mohan Lal
 */
package org.eclipse.jnosql.communication.keyvalue;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.jnosql.communication.CommunicationException;
import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.util.ServiceDiscovery;

/**
 * Entry point of a key-value database driver: reads {@link Settings} and creates a {@link BucketManagerFactory}.
 * Implementations are discovered through the Java {@link java.util.ServiceLoader}.
 * It should throw a {@link NullPointerException} when the {@link Settings} parameter is null.
 *
 * <pre>{@code
 * Settings settings = Settings.builder()
 *         .put(Configurations.HOST, "localhost:6379")
 *         .build();
 * KeyValueConfiguration configuration = KeyValueConfiguration.getConfiguration();
 * try (BucketManagerFactory factory = configuration.apply(settings);
 *      BucketManager bucket = factory.apply("people")) {
 *     bucket.put("key", "value");
 * }
 * }</pre>
 *
 * @see BucketManagerFactory
 * @see BucketManager
 */
public interface KeyValueConfiguration extends Function<Settings, BucketManagerFactory> {

    List<KeyValueConfiguration> CONFIGURATIONS =
            ServiceDiscovery.of(KeyValueConfiguration.class, KeyValueConfiguration.class)
                    .all();

    /**
     * Returns the first {@link KeyValueConfiguration} found by the {@link java.util.ServiceLoader}.
     *
     * @param <T> the configuration type
     * @return the {@link KeyValueConfiguration} instance
     * @throws CommunicationException when no implementation is found
     */
    @SuppressWarnings("unchecked")
    static <T extends KeyValueConfiguration> T getConfiguration() {
        return (T) CONFIGURATIONS
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new CommunicationException("No KeyValueConfiguration implementation found!"));
    }

    /**
     * Returns the first {@link KeyValueConfiguration} found by the {@link java.util.ServiceLoader} that is an instance of
     * the given provider type.
     *
     * @param <T>  the configuration type
     * @param type the provider implementation class
     * @return the {@link KeyValueConfiguration} instance
     * @throws NullPointerException   when the type is null
     * @throws CommunicationException when no implementation of that type is found
     */
    @SuppressWarnings("unchecked")
    static <T extends KeyValueConfiguration> T getConfiguration(Class<T> type) {
        Objects.requireNonNull(type, "service is required");
        return (T) CONFIGURATIONS
                .stream()
                .filter(type::isInstance)
                .findFirst()
                .orElseThrow(() ->
                        new CommunicationException(
                                "No KeyValueConfiguration implementation found!"));
    }
}
