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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * The Java SE {@link RepositoryAsyncExecutor} used when no provider is registered.
 *
 * <p>It runs operations on a bounded pool of daemon threads whose size comes from
 * {@link MappingConfigurations#REPOSITORY_ASYNC_POOL_SIZE}. Failures, including executor rejection, complete the
 * returned future exceptionally with the original exception, and nothing runs on the calling thread. Cancelling
 * the future only prevents an operation that has not started; a running database call is not interrupted.</p>
 */
final class ThreadPoolAsyncExecutor implements RepositoryAsyncExecutor, AutoCloseable {

    static final int QUEUE_CAPACITY = 1_000;
    private static final long KEEP_ALIVE_SECONDS = 60;

    private final Executor executor;

    ThreadPoolAsyncExecutor(Executor executor) {
        this.executor = Objects.requireNonNull(executor, "executor is required");
    }

    /**
     * Creates an executor that owns a new bounded pool.
     *
     * @param settings the settings holding the optional pool size
     * @return the executor, which must be closed to release its threads
     * @throws IllegalStateException when the configured pool size is not a positive integer
     */
    static ThreadPoolAsyncExecutor owning(Settings settings) {
        int size = settings.get(MappingConfigurations.REPOSITORY_ASYNC_POOL_SIZE.get())
                .map(Object::toString)
                .map(ThreadPoolAsyncExecutor::parsePoolSize)
                .orElseGet(() -> 2 * Runtime.getRuntime().availableProcessors());
        var pool = new ThreadPoolExecutor(size, size, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY), new DaemonThreadFactory());
        pool.allowCoreThreadTimeOut(true);
        return new ThreadPoolAsyncExecutor(pool);
    }

    @Override
    public <T> CompletableFuture<T> execute(Supplier<T> operation) {
        Objects.requireNonNull(operation, "operation is required");
        var future = new CompletableFuture<T>();
        try {
            executor.execute(() -> {
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

    @Override
    public void close() {
        if (executor instanceof ExecutorService service) {
            service.shutdown();
        }
    }

    private static int parsePoolSize(String value) {
        try {
            int size = Integer.parseInt(value.trim());
            if (size > 0) {
                return size;
            }
        } catch (NumberFormatException e) {
            // reported below
        }
        throw new IllegalStateException("'" + MappingConfigurations.REPOSITORY_ASYNC_POOL_SIZE.get()
                + "' must be a positive integer but was '" + value + "'");
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            var thread = new Thread(runnable, "jnosql-repository-async-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }
}
