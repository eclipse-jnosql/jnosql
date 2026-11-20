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

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * Service provider interface that decides how asynchronous repository methods, those returning
 * {@link CompletionStage} or {@link java.util.concurrent.CompletableFuture}, are scheduled.
 *
 * <p>Implementations are discovered once with {@link java.util.ServiceLoader} by registering the fully
 * qualified class name in {@code META-INF/services/org.eclipse.jnosql.mapping.core.repository.returns.RepositoryAsyncExecutor}.
 * The implementation needs a public no-argument constructor. When no provider is registered, JNoSQL uses a
 * bounded Java SE thread pool; when more than one provider is registered, the first asynchronous call fails
 * with an {@link IllegalStateException} naming the conflicting providers.</p>
 *
 * <p>A provider owns its resources: JNoSQL never shuts a provider down. The operation already contains the
 * repository query and the result mapping, so a provider only schedules it. For example, a provider backed
 * by an {@link java.util.concurrent.ExecutorService}:</p>
 *
 * <pre>{@code
 * public final class PoolAsyncExecutor implements RepositoryAsyncExecutor {
 *
 *     private final ExecutorService pool = Executors.newFixedThreadPool(4);
 *
 *     public <T> CompletionStage<T> execute(Supplier<T> operation) {
 *         return CompletableFuture.supplyAsync(operation, pool);
 *     }
 * }
 * }</pre>
 *
 * <p>Implementations must run the operation exactly once, must not run it on the calling thread, and should
 * complete the returned stage exceptionally, with the original exception, when the operation fails or the
 * task cannot be scheduled. Asynchronous execution is not native non-blocking I/O, and transactions or caller
 * context are propagated only if the provider does it.</p>
 */
public interface RepositoryAsyncExecutor {

    /**
     * Schedules the repository operation without blocking the caller.
     *
     * @param operation the repository operation, executed at most once
     * @param <T>       the result type
     * @return a stage completed with the operation result, or exceptionally when it fails
     * @throws NullPointerException when the operation is null
     */
    <T> CompletionStage<T> execute(Supplier<T> operation);
}
