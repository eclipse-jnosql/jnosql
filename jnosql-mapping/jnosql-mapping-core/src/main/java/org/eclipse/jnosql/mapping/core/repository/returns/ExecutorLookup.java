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

import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * The places an executor for asynchronous repository methods can come from.
 * An empty result means the source is not available; an invalid or ambiguous source must raise
 * {@link IllegalStateException} instead.
 */
interface ExecutorLookup {

    /**
     * Looks up the CDI-provided Jakarta Concurrency managed executor.
     *
     * @return the executor, or empty when Jakarta Concurrency or CDI is unavailable or no bean exists
     * @throws IllegalStateException when the selection is ambiguous
     */
    Optional<Executor> managedFromCdi();

    /**
     * Looks up the container default managed executor, {@code java:comp/DefaultManagedExecutorService}.
     *
     * @return the executor, or empty when there is no naming context or no such resource
     */
    Optional<Executor> managedFromJndi();

    /**
     * Looks up an explicitly configured executor by {@code @Named} CDI bean name, or by JNDI name when
     * the name starts with {@code java:}.
     *
     * @param name the configured name
     * @return the executor, or empty when it cannot be found
     * @throws IllegalStateException when the selection is ambiguous or the resource is not an executor
     */
    Optional<Executor> named(String name);
}
