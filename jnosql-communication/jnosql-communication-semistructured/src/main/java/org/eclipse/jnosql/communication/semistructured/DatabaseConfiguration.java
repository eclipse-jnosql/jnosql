/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;


import org.eclipse.jnosql.communication.CommunicationException;
import org.eclipse.jnosql.communication.ServiceProviderLoader;
import org.eclipse.jnosql.communication.Settings;

import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Function;


/**
 * This interface represents a function that reads from {@link Settings} and then creates a manager factory instance.
 * It should throw a {@link NullPointerException} when the {@link Settings} parameter is null.
 *
 * <p>This interface extends {@link java.util.function.Function}, providing a way to create database manager factory instances
 * based on settings. Implementations of this interface are expected to provide the necessary logic to read settings and
 * instantiate appropriate database manager factories.</p>
 *
 * <p>Two static factory methods are provided for obtaining instances of {@link DatabaseConfiguration}: {@link #getConfiguration()}
 * and {@link #getConfiguration(Class)}. These methods utilize Java's {@link ServiceLoader} mechanism to discover available
 * implementations of {@link DatabaseConfiguration}.</p>
 *
 * <p>Implementations of this interface should be aware of the underlying provider implementation they represent,
 * and ensure that the correct configuration is returned based on the provided type.</p>
 *
 * @see DatabaseManagerFactory
 * @see DatabaseManager
 */
public interface DatabaseConfiguration extends Function<Settings, DatabaseManagerFactory> {

    List<DatabaseConfiguration> CONFIGURATIONS = ServiceProviderLoader.loadAll(DatabaseConfiguration.class);
    /**
     * Creates and returns a {@link DatabaseConfiguration} instance using Java's {@link ServiceLoader} mechanism.
     *
     * <p>This method discovers and loads an implementation of {@link DatabaseConfiguration} using the {@link ServiceLoader}.
     * It returns the first implementation found. If no implementation is found, it throws a {@link CommunicationException}.</p>
     *
     * @param <R> the {@link DatabaseManagerFactory} type
     * @param <T> the configuration type
     * @return a {@link DatabaseConfiguration} instance
     * @throws CommunicationException if no implementation of {@link DatabaseConfiguration} is found
     */
    @SuppressWarnings("unchecked")
    static <R extends DatabaseManagerFactory, T extends DatabaseConfiguration> T getConfiguration() {
        return (T) CONFIGURATIONS
                .stream()
                .findFirst().orElseThrow(() -> new CommunicationException("No DatabaseConfiguration implementation found!"));
    }

    /**
     * Creates and returns a {@link DatabaseConfiguration} instance using Java's {@link ServiceLoader} mechanism,
     * filtered by a particular provider implementation.
     *
     * <p>This method discovers and loads an implementation of {@link DatabaseConfiguration} using the {@link ServiceLoader},
     * filtering by the provided type. It returns the first implementation found matching the specified type.
     * If no implementation is found, it throws a {@link CommunicationException}.</p>
     *
     * @param <R> the {@link DatabaseManagerFactory} type
     * @param <T> the configuration type
     * @param type the particular provider type
     * @return a {@link DatabaseConfiguration} instance
     * @throws CommunicationException if no implementation of {@link DatabaseConfiguration} is found for the specified type
     */
    @SuppressWarnings("unchecked")
    static <R extends DatabaseManagerFactory, T extends DatabaseConfiguration> T getConfiguration(Class<T> type) {
        return (T) CONFIGURATIONS
                .stream()
                .filter(type::isInstance)
                .findFirst()
                .orElseThrow(() -> new CommunicationException("No DatabaseConfiguration implementation found for type: " + type.getName()));
    }
}