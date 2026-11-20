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

import org.eclipse.jnosql.mapping.core.repository.returns.AsyncReturnFixtures.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.eclipse.jnosql.mapping.core.repository.returns.AsyncReturnFixtures.asynchronous;

class AsyncExecutorProviderTest {

    public static final class StubProvider implements RepositoryAsyncExecutor {
        @Override
        public <T> CompletionStage<T> execute(Supplier<T> operation) {
            return CompletableFuture.completedFuture(operation.get());
        }
    }

    public static final class OtherProvider implements RepositoryAsyncExecutor {
        @Override
        public <T> CompletionStage<T> execute(Supplier<T> operation) {
            return CompletableFuture.completedFuture(operation.get());
        }
    }

    private static AsyncExecutorProvider fallbackOnly(AtomicInteger creations) {
        return new AsyncExecutorProvider(List::of, () -> {
            creations.incrementAndGet();
            return new ThreadPoolAsyncExecutor(Executors.newSingleThreadExecutor());
        });
    }

    @Nested
    @DisplayName("When selecting the provider")
    class WhenTheSelection {

        @Test
        @DisplayName("Should use the Java SE fallback when there is no provider")
        void shouldUseFallbackWithoutProvider() {
            var creations = new AtomicInteger();
            try (var provider = fallbackOnly(creations)) {
                assertSoftly(softly -> {
                    softly.assertThat(provider.get()).isInstanceOf(ThreadPoolAsyncExecutor.class);
                    softly.assertThat(creations).hasValue(1);
                });
            }
        }

        @Test
        @DisplayName("Should use the only registered provider and never create the fallback")
        void shouldUseSingleProvider() {
            var stub = new StubProvider();
            var creations = new AtomicInteger();
            var provider = new AsyncExecutorProvider(() -> List.of(stub), () -> {
                creations.incrementAndGet();
                return new ThreadPoolAsyncExecutor(Runnable::run);
            });

            assertSoftly(softly -> {
                softly.assertThat(provider.get()).isSameAs(stub);
                softly.assertThat(creations).hasValue(0);
            });
        }

        @Test
        @DisplayName("Should reuse the selection and look providers up once")
        void shouldReuseSelection() {
            var lookups = new AtomicInteger();
            var stub = new StubProvider();
            var provider = new AsyncExecutorProvider(() -> {
                lookups.incrementAndGet();
                return List.of(stub);
            }, () -> new ThreadPoolAsyncExecutor(Runnable::run));

            provider.get();
            provider.get();
            provider.get();

            assertSoftly(softly -> softly.assertThat(lookups).hasValue(1));
        }

        @Test
        @DisplayName("Should fail naming the providers when they conflict")
        void shouldFailOnConflict() {
            var provider = new AsyncExecutorProvider(() -> List.of(new StubProvider(), new OtherProvider()),
                    () -> new ThreadPoolAsyncExecutor(Runnable::run));

            assertThatThrownBy(provider::get).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining(StubProvider.class.getName())
                    .hasMessageContaining(OtherProvider.class.getName());
        }
    }

    @Nested
    @DisplayName("When discovering providers with the ServiceLoader")
    class WhenTheDiscovery {

        @Test
        @DisplayName("Should discover an external provider registered by the service file")
        void shouldDiscoverExternalProvider(@TempDir Path directory) throws IOException {
            register(directory, StubProvider.class.getName());

            var found = withLoader(directory, AsyncExecutorProvider::loadProviders);

            assertSoftly(softly -> softly.assertThat(found).singleElement().isInstanceOf(StubProvider.class));
        }

        @Test
        @DisplayName("Should find no provider when none is registered")
        void shouldFindNoProvider(@TempDir Path directory) {
            var found = withLoader(directory, AsyncExecutorProvider::loadProviders);

            assertSoftly(softly -> softly.assertThat(found).isEmpty());
        }

        @Test
        @DisplayName("Should fail clearly on an invalid provider registration")
        void shouldFailOnInvalidProvider(@TempDir Path directory) throws IOException {
            register(directory, "org.example.MissingProvider");

            assertThatThrownBy(() -> withLoader(directory, AsyncExecutorProvider::loadProviders))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining(RepositoryAsyncExecutor.class.getName());
        }

        private void register(Path directory, String className) throws IOException {
            var services = Files.createDirectories(directory.resolve("META-INF/services"));
            Files.writeString(services.resolve(RepositoryAsyncExecutor.class.getName()), className);
        }

        private <R> R withLoader(Path directory, Supplier<R> action) {
            var thread = Thread.currentThread();
            var previous = thread.getContextClassLoader();
            try (var loader = new URLClassLoader(new URL[]{directory.toUri().toURL()}, previous)) {
                thread.setContextClassLoader(loader);
                return action.get();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            } finally {
                thread.setContextClassLoader(previous);
            }
        }
    }

    @Nested
    @DisplayName("When running a repository operation")
    class WhenTheExecution {

        @Test
        @DisplayName("Should map the result on the selected provider exactly once")
        void shouldRunOnProvider() {
            var runs = new AtomicInteger();
            var provider = new AsyncExecutorProvider(() -> List.of(new StubProvider()),
                    () -> new ThreadPoolAsyncExecutor(Runnable::run));
            var dynamic = asynchronous(CompletionStage.class, Optional.class, () -> {
                runs.incrementAndGet();
                return Optional.of(new Product("Notebook"));
            }, Stream::empty);

            var result = provider.execute(dynamic).orTimeout(5, TimeUnit.SECONDS).join();

            assertSoftly(softly -> {
                softly.assertThat(result).isEqualTo(Optional.of(new Product("Notebook")));
                softly.assertThat(runs).hasValue(1);
            });
        }

        @Test
        @DisplayName("Should complete exceptionally when the provider fails to schedule")
        void shouldCompleteExceptionallyWhenProviderThrows() {
            var failure = new IllegalStateException("closed");
            var provider = new AsyncExecutorProvider(() -> List.of(new RepositoryAsyncExecutor() {
                @Override
                public <T> CompletionStage<T> execute(Supplier<T> operation) {
                    throw failure;
                }
            }), () -> new ThreadPoolAsyncExecutor(Runnable::run));
            var dynamic = asynchronous(CompletionStage.class, Optional.class, Optional::empty, Stream::empty);

            var future = provider.execute(dynamic);

            assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class)
                    .hasCause(failure);
        }

        @Test
        @DisplayName("Should complete exceptionally when providers conflict")
        void shouldCompleteExceptionallyOnConflict() {
            var provider = new AsyncExecutorProvider(() -> List.of(new StubProvider(), new OtherProvider()),
                    () -> new ThreadPoolAsyncExecutor(Runnable::run));
            var dynamic = asynchronous(CompletionStage.class, Optional.class, Optional::empty, Stream::empty);

            var future = provider.execute(dynamic);

            assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS)).hasCauseInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Should not run a provider that returns a stage without toCompletableFuture support")
        void shouldAdaptForeignStages() {
            var provider = new AsyncExecutorProvider(() -> List.of(new RepositoryAsyncExecutor() {
                @Override
                public <T> CompletionStage<T> execute(Supplier<T> operation) {
                    return CompletableFuture.completedFuture(operation.get()).minimalCompletionStage();
                }
            }), () -> new ThreadPoolAsyncExecutor(Runnable::run));
            var dynamic = asynchronous(CompletionStage.class, Optional.class,
                    () -> Optional.of(new Product("Pen")), Stream::empty);

            assertSoftly(softly -> softly.assertThat(provider.execute(dynamic).orTimeout(5, TimeUnit.SECONDS).join())
                    .isEqualTo(Optional.of(new Product("Pen"))));
        }
    }

    @Nested
    @DisplayName("When closing")
    class WhenTheClose {

        @Test
        @DisplayName("Should shut down only the fallback it owns")
        void shouldShutDownOwnedFallback() {
            var service = Executors.newSingleThreadExecutor();
            var provider = new AsyncExecutorProvider(List::of, () -> new ThreadPoolAsyncExecutor(service));
            provider.get();

            provider.close();

            assertSoftly(softly -> softly.assertThat(service.isShutdown()).isTrue());
        }

        @Test
        @DisplayName("Should leave an external provider running")
        void shouldNotCloseExternalProvider() {
            var closed = new AtomicInteger();
            var external = new AutoCloseableProvider(closed);
            var provider = new AsyncExecutorProvider(() -> List.of(external),
                    () -> new ThreadPoolAsyncExecutor(Runnable::run));
            provider.get();

            provider.close();

            assertSoftly(softly -> softly.assertThat(closed).hasValue(0));
        }
    }

    private static final class AutoCloseableProvider implements RepositoryAsyncExecutor, AutoCloseable {
        private final AtomicInteger closed;

        private AutoCloseableProvider(AtomicInteger closed) {
            this.closed = closed;
        }

        @Override
        public <T> CompletionStage<T> execute(Supplier<T> operation) {
            return CompletableFuture.completedFuture(operation.get());
        }

        @Override
        public void close() {
            closed.incrementAndGet();
        }
    }
}
