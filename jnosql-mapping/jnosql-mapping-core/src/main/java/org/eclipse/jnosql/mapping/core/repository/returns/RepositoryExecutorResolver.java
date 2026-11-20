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
import org.eclipse.jnosql.mapping.core.config.MicroProfileSettings;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Selects the executor that runs asynchronous repository methods.
 *
 * <p>With {@code jnosql.repository.async.executor} unset or {@code auto}, the order is:</p>
 * <ol>
 *     <li>the CDI-provided Jakarta Concurrency {@code ManagedExecutorService}, which already honors CDI
 *     qualifiers and alternatives;</li>
 *     <li>the container default, {@code java:comp/DefaultManagedExecutorService};</li>
 *     <li>a JNoSQL-owned bounded thread pool, used only because those infrastructures are absent.</li>
 * </ol>
 * <p>Any other value names an executor (CDI {@code @Named} bean or JNDI name). When it cannot be found the
 * resolution fails; it never falls back to another executor.</p>
 */
final class RepositoryExecutorResolver {

    static final String AUTO = "auto";
    static final int QUEUE_CAPACITY = 1_000;
    private static final long KEEP_ALIVE_SECONDS = 60;

    private final Settings settings;
    private final ExecutorLookup lookup;

    RepositoryExecutorResolver(Settings settings, ExecutorLookup lookup) {
        this.settings = Objects.requireNonNull(settings, "settings is required");
        this.lookup = Objects.requireNonNull(lookup, "lookup is required");
    }

    static RepositoryExecutorResolver container() {
        return new RepositoryExecutorResolver(MicroProfileSettings.INSTANCE, ContainerExecutorLookup.INSTANCE);
    }

    /**
     * Resolves the executor.
     *
     * @return the executor and whether JNoSQL owns it
     * @throws IllegalStateException when the configuration is invalid or the selection is ambiguous
     */
    ResolvedExecutor resolve() {
        var configured = settings.get(MappingConfigurations.REPOSITORY_ASYNC_EXECUTOR.get())
                .map(Object::toString)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .orElse(AUTO);

        if (!AUTO.equals(configured.toLowerCase(Locale.ROOT))) {
            var executor = lookup.named(configured).orElseThrow(() -> new IllegalStateException(
                    "The executor '" + configured + "' configured by '"
                            + MappingConfigurations.REPOSITORY_ASYNC_EXECUTOR.get() + "' could not be resolved"));
            return new ResolvedExecutor(executor, false);
        }
        return lookup.managedFromCdi()
                .or(lookup::managedFromJndi)
                .map(executor -> new ResolvedExecutor(executor, false))
                .orElseGet(() -> new ResolvedExecutor(newBoundedPool(), true));
    }

    private Executor newBoundedPool() {
        int size = settings.get(MappingConfigurations.REPOSITORY_ASYNC_POOL_SIZE.get())
                .map(Object::toString)
                .map(this::parsePoolSize)
                .orElseGet(() -> 2 * Runtime.getRuntime().availableProcessors());
        var pool = new ThreadPoolExecutor(size, size, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY), new DaemonThreadFactory());
        pool.allowCoreThreadTimeOut(true);
        return pool;
    }

    private int parsePoolSize(String value) {
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

    /**
     * The selected executor.
     *
     * @param executor the executor
     * @param owned    {@code true} when JNoSQL created it and is therefore responsible for shutting it down
     */
    record ResolvedExecutor(Executor executor, boolean owned) {
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
