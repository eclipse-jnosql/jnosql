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
import org.eclipse.jnosql.mapping.core.repository.DynamicReturnConverter;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;

/**
 * Runs repository operations on the resolved executor. It is shared by the asynchronous
 * {@link org.eclipse.jnosql.mapping.core.repository.RepositoryReturn} handlers.
 *
 * <p>The executor is resolved on the first use and then reused. Failures, including resolution failures and
 * executor rejection, complete the returned future exceptionally with the original exception. Cancelling the
 * future only prevents an operation that has not started; a running database call is not interrupted.</p>
 */
final class RepositoryAsyncExecutor implements AutoCloseable {

    private static final class SharedHolder {
        private static final RepositoryAsyncExecutor INSTANCE =
                new RepositoryAsyncExecutor(RepositoryExecutorResolver.container()::resolve);
    }

    private final Supplier<RepositoryExecutorResolver.ResolvedExecutor> resolver;

    private volatile RepositoryExecutorResolver.ResolvedExecutor resolved;

    RepositoryAsyncExecutor(Supplier<RepositoryExecutorResolver.ResolvedExecutor> resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver is required");
    }

    static RepositoryAsyncExecutor shared() {
        return SharedHolder.INSTANCE;
    }

    /**
     * Converts the dynamic return as its asynchronous result type, using the ordinary synchronous handlers.
     *
     * @param dynamicReturn the asynchronous dynamic return
     * @return a future completed with the converted result
     * @throws IllegalStateException when the nested type is unknown or is itself asynchronous
     */
    CompletableFuture<Object> execute(DynamicReturn<?> dynamicReturn) {
        dynamicReturn.asyncResultType().filter(CompletionStage.class::isAssignableFrom).ifPresent(type -> {
            throw new IllegalStateException("Nested asynchronous return types are not supported at: "
                    + dynamicReturn.methodName());
        });
        var synchronous = dynamicReturn.unwrapAsync();
        return supply(() -> DynamicReturnConverter.INSTANCE.convert(synchronous));
    }

    <T> CompletableFuture<T> supply(Supplier<T> operation) {
        var future = new CompletableFuture<T>();
        RepositoryExecutorResolver.ResolvedExecutor current;
        try {
            current = executor();
        } catch (RuntimeException e) {
            future.completeExceptionally(e);
            return future;
        }
        try {
            current.executor().execute(() -> {
                if (future.isDone()) {
                    return;
                }
                try {
                    future.complete(operation.get());
                } catch (Throwable e) {
                    future.completeExceptionally(e);
                }
            });
        } catch (RejectedExecutionException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    /**
     * Shuts down the executor only when JNoSQL created it; container and application executors are left running.
     */
    @Override
    public synchronized void close() {
        var current = resolved;
        resolved = null;
        if (current != null && current.owned() && current.executor() instanceof ExecutorService service) {
            service.shutdown();
        }
    }

    private RepositoryExecutorResolver.ResolvedExecutor executor() {
        var current = resolved;
        if (current == null) {
            synchronized (this) {
                current = resolved;
                if (current == null) {
                    current = resolver.get();
                    resolved = current;
                }
            }
        }
        return current;
    }
}
