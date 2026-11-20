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

import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;

import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * The only class that references the optional Jakarta Concurrency API.
 * It is loaded lazily, so a missing API surfaces as a {@link LinkageError} that callers treat as "not available".
 */
final class ManagedExecutorLookup {

    private ManagedExecutorLookup() {
    }

    /**
     * Selects the default-qualified {@link ManagedExecutorService} bean.
     *
     * @return the executor, or empty when there is no CDI container or no such bean
     * @throws IllegalStateException when more than one bean is eligible
     */
    static Optional<Executor> fromCdi() {
        Instance<ManagedExecutorService> instance;
        try {
            instance = CDI.current().select(ManagedExecutorService.class);
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
        if (instance.isAmbiguous()) {
            throw new IllegalStateException("More than one " + ManagedExecutorService.class.getName()
                    + " bean is eligible to run asynchronous repository methods. Select one with the "
                    + "'jnosql.repository.async.executor' property or an alternative.");
        }
        return instance.isResolvable() ? Optional.of(instance.get()) : Optional.empty();
    }
}
