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

import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;

import java.util.concurrent.CompletableFuture;

/**
 * Converts repository results to {@link CompletableFuture} return values.
 *
 * <p>It behaves like {@link CompletionStageRepositoryReturn} but exposes the {@link CompletableFuture}.
 * Cancelling the future prevents an operation that has not started yet; it does not interrupt a database call
 * that is already running.</p>
 *
 * <pre>{@code
 * CompletableFuture<List<Product>> findByCategory(String category);
 * }</pre>
 */
public class CompletableFutureRepositoryReturn extends AbstractRepositoryReturn {

    private final RepositoryAsyncExecutor asyncExecutor;

    /**
     * Creates a handler that uses the shared executor.
     */
    public CompletableFutureRepositoryReturn() {
        this(RepositoryAsyncExecutor.shared());
    }

    CompletableFutureRepositoryReturn(RepositoryAsyncExecutor asyncExecutor) {
        super(CompletableFuture.class);
        this.asyncExecutor = asyncExecutor;
    }

    @Override
    public <T> Object convert(DynamicReturn<T> dynamicReturn) {
        return asyncExecutor.execute(dynamicReturn);
    }

    @Override
    public <T> Object convertPageRequest(DynamicReturn<T> dynamicReturn) {
        return convert(dynamicReturn);
    }
}
