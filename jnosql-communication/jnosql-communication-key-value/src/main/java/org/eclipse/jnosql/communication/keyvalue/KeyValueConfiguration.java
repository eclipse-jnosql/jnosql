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
 */
package org.eclipse.jnosql.communication.keyvalue;


import org.eclipse.jnosql.communication.CommunicationException;
import org.eclipse.jnosql.communication.Settings;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * It is a function that reads from {@link Settings} and then creates a manager factory instance.
 * It should return a {@link NullPointerException} when the {@link Settings} parameter is null.
 *
 * @see BucketManagerFactory
 * @see BucketManager
 */
public interface KeyValueConfiguration extends Function<Settings, BucketManagerFactory> {

    /**
     * creates and returns a  {@link KeyValueConfiguration}  instance from {@link ServiceLoader}
     *
     * @param <T> the configuration type
     * @return {@link KeyValueConfiguration} instance
     */
    @SuppressWarnings("unchecked")
    static <T extends KeyValueConfiguration> T getConfiguration() {
       return (T) configurations()
               .findFirst()
               .orElseThrow(() -> new CommunicationException("No KeyValueConfiguration implementation found!"));
    }

    /**
     * creates and returns a  {@link KeyValueConfiguration} instance from {@link ServiceLoader}
     * for a particular provider implementation.
     *
     * @param <T>      the configuration type
     * @param type the particular provider
     * @return {@link KeyValueConfiguration} instance
     */
    @SuppressWarnings("unchecked")
    static <T extends KeyValueConfiguration> T getConfiguration(Class<T> type) {
        Objects.requireNonNull(type, "service is required");
        return (T) configurations()
                .filter(type::isInstance)
                .findFirst()
                .orElseThrow(() -> new CommunicationException("No KeyValueConfiguration implementation found!"));
    }

    // Looked up on every call instead of kept in a static field: in an application server each application
    // brings its own driver, while this interface is shared by all of them
    private static Stream<KeyValueConfiguration> configurations() {
        return ServiceLoader.load(KeyValueConfiguration.class).stream()
                .map(ServiceLoader.Provider::get);
    }
}
