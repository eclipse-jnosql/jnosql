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

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * Converts repository results to {@link CompletionStage} return values.
 *
 * <p>The repository method returns immediately and the operation is scheduled by the registered
 * {@link RepositoryAsyncExecutor} provider, or by a JNoSQL-owned bounded thread pool when there is none; see
 * {@link org.eclipse.jnosql.mapping.core.config.MappingConfigurations#REPOSITORY_ASYNC_POOL_SIZE}.
 * The type argument is mapped exactly as the synchronous method would be:</p>
 *
 * <pre>{@code
 * CompletionStage<Optional<Product>> findByName(String name);
 *
 * repository.findByName("Notebook")
 *         .thenAccept(product -> product.ifPresent(System.out::println));
 * }</pre>
 *
 * <p>Failures complete the stage exceptionally. The work is asynchronous, but the database call is still
 * blocking and no transaction or caller context is propagated beyond what the provider offers.
 * This is a JNoSQL feature, not part of the Jakarta Data specification.</p>
 */
public class CompletionStageRepositoryReturn extends AbstractRepositoryReturn {

    private final Supplier<AsyncExecutorProvider> asyncExecutor;

    /**
     * Creates a handler that uses the {@link RepositoryAsyncExecutor} of the current application class loader.
     */
    public CompletionStageRepositoryReturn() {
        super(CompletionStage.class);
        this.asyncExecutor = AsyncExecutorProvider::shared;
    }

    CompletionStageRepositoryReturn(AsyncExecutorProvider asyncExecutor) {
        super(CompletionStage.class);
        this.asyncExecutor = () -> asyncExecutor;
    }

    @Override
    public <T> Object convert(DynamicReturn<T> dynamicReturn) {
        return asyncExecutor.get().execute(dynamicReturn).minimalCompletionStage();
    }

    @Override
    public <T> Object convertPageRequest(DynamicReturn<T> dynamicReturn) {
        return convert(dynamicReturn);
    }
}
