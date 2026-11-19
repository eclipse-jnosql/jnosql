/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 * and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;


import org.eclipse.jnosql.communication.CommunicationException;
import org.eclipse.jnosql.communication.Settings;

import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Stream;


/**
 * Entry point of a semi-structured database driver: reads {@link Settings} and creates a
 * {@link DatabaseManagerFactory}. It should throw a {@link NullPointerException} when the {@link Settings}
 * parameter is null.
 *
 * <p>Drivers are discovered through the Java {@link ServiceLoader}; use {@link #getConfiguration()} for the first
 * one found or {@link #getConfiguration(Class)} for a specific driver.</p>
 *
 * <pre>{@code
 * Settings settings = Settings.builder()
 *         .put(Configurations.HOST, "localhost:27017")
 *         .build();
 * DatabaseConfiguration configuration = DatabaseConfiguration.getConfiguration();
 * DatabaseManagerFactory factory = configuration.apply(settings);
 * }</pre>
 *
 * @see DatabaseManagerFactory
 * @see DatabaseManager
 */
public interface DatabaseConfiguration extends Function<Settings, DatabaseManagerFactory> {

    /**
     * Creates and returns a {@link DatabaseConfiguration} instance using Java's {@link ServiceLoader} mechanism.
     *
     * <p>It returns the first implementation found.</p>
     *
     * @param <R> the {@link DatabaseManagerFactory} type
     * @param <T> the configuration type
     * @return a {@link DatabaseConfiguration} instance
     * @throws CommunicationException if no implementation of {@link DatabaseConfiguration} is found
     */
    @SuppressWarnings("unchecked")
    static <R extends DatabaseManagerFactory, T extends DatabaseConfiguration> T getConfiguration() {
        return (T) configurations()
                .findFirst().orElseThrow(() -> new CommunicationException("No DatabaseConfiguration implementation found!"));
    }

    /**
     * Creates and returns a {@link DatabaseConfiguration} instance using Java's {@link ServiceLoader} mechanism,
     * filtered by a particular provider implementation.
     *
     * <p>It returns the first implementation that is an instance of the given type.</p>
     *
     * @param <R> the {@link DatabaseManagerFactory} type
     * @param <T> the configuration type
     * @param type the particular provider type
     * @return a {@link DatabaseConfiguration} instance
     * @throws CommunicationException if no implementation of {@link DatabaseConfiguration} is found for the specified type
     */
    @SuppressWarnings("unchecked")
    static <R extends DatabaseManagerFactory, T extends DatabaseConfiguration> T getConfiguration(Class<T> type) {
        return (T) configurations()
                .filter(type::isInstance)
                .findFirst()
                .orElseThrow(() -> new CommunicationException("No DatabaseConfiguration implementation found for type: " + type.getName()));
    }

    // Looked up on every call instead of kept in a static field: in an application server each application
    // brings its own driver, while this interface is shared by all of them
    private static Stream<DatabaseConfiguration> configurations() {
        return ServiceLoader.load(DatabaseConfiguration.class).stream()
                .map(ServiceLoader.Provider::get);
    }
}