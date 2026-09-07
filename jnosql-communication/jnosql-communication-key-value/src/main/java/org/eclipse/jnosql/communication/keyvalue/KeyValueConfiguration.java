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
 * It is a function that reads from {@link Settings} and then creates a manager factory instance.
 * It should return a {@link NullPointerException} when the {@link Settings} parameter is null.
 *
 * @see BucketManagerFactory
 * @see BucketManager
 */
public interface KeyValueConfiguration extends Function<Settings, BucketManagerFactory> {

    List<KeyValueConfiguration> CONFIGURATIONS =
            ServiceDiscovery.of(KeyValueConfiguration.class, KeyValueConfiguration.class)
                    .all();

    /**
     * Creates and returns a {@link KeyValueConfiguration} instance.
     *
     * @param <T> the configuration type
     * @return {@link KeyValueConfiguration} instance
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
     * Creates and returns a {@link KeyValueConfiguration} instance
     * for a particular provider implementation.
     *
     * @param <T>  the configuration type
     * @param type the particular provider
     * @return {@link KeyValueConfiguration} instance
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
