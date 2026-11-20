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

import org.eclipse.jnosql.mapping.core.config.MicroProfileSettings;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturnConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * Selects the {@link RepositoryAsyncExecutor} once and runs asynchronous repository operations on it.
 *
 * <p>Providers are discovered with {@link ServiceLoader} on first use and the choice is then reused: a single
 * provider is used as is, no provider selects the JNoSQL-owned Java SE pool, and several providers or an
 * unloadable provider fail with an {@link IllegalStateException}. Failures are not cached, so a corrected
 * environment can succeed later. Only the fallback pool is shut down by {@link #close()}.</p>
 */
final class AsyncExecutorProvider implements AutoCloseable {

    private static final class SharedHolder {
        private static final AsyncExecutorProvider INSTANCE = new AsyncExecutorProvider(
                AsyncExecutorProvider::loadProviders, () -> ThreadPoolAsyncExecutor.owning(MicroProfileSettings.INSTANCE));
    }

    private final Supplier<List<RepositoryAsyncExecutor>> providers;
    private final Supplier<ThreadPoolAsyncExecutor> fallback;

    private volatile RepositoryAsyncExecutor selected;
    private ThreadPoolAsyncExecutor owned;

    AsyncExecutorProvider(Supplier<List<RepositoryAsyncExecutor>> providers,
                          Supplier<ThreadPoolAsyncExecutor> fallback) {
        this.providers = Objects.requireNonNull(providers, "providers is required");
        this.fallback = Objects.requireNonNull(fallback, "fallback is required");
    }

    static AsyncExecutorProvider shared() {
        return SharedHolder.INSTANCE;
    }

    /**
     * @return the selected executor, resolved on the first call
     * @throws IllegalStateException when the providers conflict or cannot be loaded
     */
    RepositoryAsyncExecutor get() {
        var current = selected;
        if (current == null) {
            synchronized (this) {
                current = selected;
                if (current == null) {
                    current = select();
                    selected = current;
                }
            }
        }
        return current;
    }

    /**
     * Converts the dynamic return as its asynchronous result type, using the ordinary synchronous handlers.
     * Failures, including provider selection failures, complete the future exceptionally.
     *
     * @param dynamicReturn the asynchronous dynamic return
     * @return a future completed with the converted result
     * @throws IllegalStateException when the nested type is unknown or is itself asynchronous
     */
    CompletableFuture<Object> execute(DynamicReturn<?> dynamicReturn) {
        var synchronous = dynamicReturn.unwrap();
        synchronous.returnDescriptor().filter(type -> type.isA(CompletionStage.class)).ifPresent(type -> {
            throw new IllegalStateException("Nested asynchronous return types are not supported at: "
                    + dynamicReturn.methodName());
        });
        try {
            var stage = get().<Object>execute(() -> DynamicReturnConverter.INSTANCE.convert(synchronous));
            return toFuture(stage);
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public synchronized void close() {
        selected = null;
        if (owned != null) {
            owned.close();
            owned = null;
        }
    }

    private RepositoryAsyncExecutor select() {
        var found = providers.get();
        if (found.size() > 1) {
            var names = found.stream().map(provider -> provider.getClass().getName()).sorted().toList();
            throw new IllegalStateException("Only one " + RepositoryAsyncExecutor.class.getName()
                    + " provider is allowed but found: " + names);
        }
        if (found.size() == 1) {
            return found.get(0);
        }
        owned = fallback.get();
        return owned;
    }

    // subclasses such as the minimal stage reject mutating methods, so only a plain future is exposed as is
    @SuppressWarnings("unchecked")
    private static CompletableFuture<Object> toFuture(CompletionStage<Object> stage) {
        if (stage.getClass() == CompletableFuture.class) {
            return (CompletableFuture<Object>) stage;
        }
        var future = new CompletableFuture<Object>();
        stage.whenComplete((value, error) -> {
            if (error == null) {
                future.complete(value);
            } else {
                future.completeExceptionally(error);
            }
        });
        return future;
    }

    static List<RepositoryAsyncExecutor> loadProviders() {
        var found = new ArrayList<RepositoryAsyncExecutor>();
        try {
            ServiceLoader.load(RepositoryAsyncExecutor.class).forEach(found::add);
        } catch (ServiceConfigurationError e) {
            throw new IllegalStateException("Invalid " + RepositoryAsyncExecutor.class.getName()
                    + " provider configuration", e);
        }
        return found;
    }
}
