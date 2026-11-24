/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.core.repository.returns;

import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.mapping.core.config.MappingConfigurations;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * The JNoSQL-owned execution strategies selected by {@link MappingConfigurations#REPOSITORY_ASYNC_EXECUTOR}.
 *
 * <p>Only the selected strategy reads its own settings: the platform pool reads the pool size and the queue
 * capacity, while the virtual strategy reads nothing else.</p>
 */
enum AsyncExecutorStrategy {

    PLATFORM,
    VIRTUAL;

    /**
     * Reads the configured strategy, which is {@link #PLATFORM} when the property is absent.
     *
     * @param settings the settings holding the optional executor property
     * @return the configured strategy
     * @throws IllegalStateException when the configured value is not supported
     */
    static AsyncExecutorStrategy from(Settings settings) {
        return settings.get(MappingConfigurations.REPOSITORY_ASYNC_EXECUTOR.get())
                .map(Object::toString)
                .map(AsyncExecutorStrategy::parse)
                .orElse(PLATFORM);
    }

    /**
     * Creates a new executor owned by the caller, which must close it to release its resources.
     *
     * @param settings the settings read by the strategy
     * @return the new executor
     * @throws IllegalStateException when a setting required by the strategy is invalid
     */
    ThreadPoolAsyncExecutor create(Settings settings) {
        return switch (this) {
            case PLATFORM -> ThreadPoolAsyncExecutor.owning(settings);
            case VIRTUAL -> new ThreadPoolAsyncExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }

    private static AsyncExecutorStrategy parse(String value) {
        var normalized = value.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(strategy -> strategy.name().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("'" + MappingConfigurations.REPOSITORY_ASYNC_EXECUTOR.get()
                        + "' must be one of " + Arrays.stream(values())
                        .map(strategy -> strategy.name().toLowerCase(Locale.ROOT)).toList()
                        + " but was '" + value + "'"));
    }
}
