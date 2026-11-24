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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
        @DisplayName("Should propagate a failure from a foreign stage")
        void shouldPropagateForeignFailure() {
            var failure = new IllegalStateException("boom");
            var provider = new AsyncExecutorProvider(() -> List.of(new RepositoryAsyncExecutor() {
                @Override
                public <T> CompletionStage<T> execute(Supplier<T> operation) {
                    return CompletableFuture.<T>failedFuture(failure).minimalCompletionStage();
                }
            }), () -> new ThreadPoolAsyncExecutor(Runnable::run));
            var dynamic = asynchronous(CompletionStage.class, Optional.class, Optional::empty, Stream::empty);

            var future = provider.execute(dynamic);

            assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS)).hasCause(failure);
        }

        @Test
        @DisplayName("Should skip the query when the future is cancelled before a foreign executor runs it")
        void shouldSkipCancelledBeforeStart() {
            var runs = new AtomicInteger();
            var queued = new java.util.ArrayList<Supplier<?>>();
            var provider = new AsyncExecutorProvider(() -> List.of(new RepositoryAsyncExecutor() {
                @Override
                public <T> CompletionStage<T> execute(Supplier<T> operation) {
                    queued.add(operation);
                    return new CompletableFuture<>();
                }
            }), () -> new ThreadPoolAsyncExecutor(Runnable::run));
            var dynamic = asynchronous(CompletableFuture.class, Optional.class, () -> {
                runs.incrementAndGet();
                return Optional.of(new Product("Pen"));
            }, Stream::empty);

            var future = provider.execute(dynamic);
            future.cancel(true);
            var result = queued.get(0).get();

            assertSoftly(softly -> {
                softly.assertThat(result).isNull();
                softly.assertThat(runs).hasValue(0);
            });
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
    @DisplayName("When sharing the provider")
    class WhenTheSharing {

        @Test
        @DisplayName("Should scope the shared provider to the context class loader")
        void shouldScopeToClassLoader() throws IOException {
            var original = Thread.currentThread().getContextClassLoader();
            try (var loader = new URLClassLoader(new URL[0], original)) {
                var outer = AsyncExecutorProvider.shared();
                Thread.currentThread().setContextClassLoader(loader);
                var inner = AsyncExecutorProvider.shared();
                assertSoftly(softly -> {
                    softly.assertThat(inner).isNotSameAs(outer);
                    softly.assertThat(AsyncExecutorProvider.shared()).isSameAs(inner);
                });
            } finally {
                Thread.currentThread().setContextClassLoader(original);
            }
        }

        @Test
        @DisplayName("Should fall back to the system class loader without a context class loader")
        void shouldUseSystemClassLoaderWithoutContext() {
            var original = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(null);
                assertSoftly(softly -> softly.assertThat(AsyncExecutorProvider.shared())
                        .isSameAs(AsyncExecutorProvider.shared()));
            } finally {
                Thread.currentThread().setContextClassLoader(original);
            }
        }

        @Test
        @DisplayName("Should share one lazily selected fallback when no provider is registered")
        void shouldShareSelection() {
            var shared = AsyncExecutorProvider.shared();

            assertSoftly(softly -> {
                softly.assertThat(AsyncExecutorProvider.shared()).isSameAs(shared);
                softly.assertThat(shared.get()).isInstanceOf(ThreadPoolAsyncExecutor.class);
                softly.assertThat(shared.get()).isSameAs(shared.get());
            });
        }

        @Test
        @DisplayName("Should select once when callers race")
        void shouldSelectOnceWhenRacing() throws Exception {
            var lookups = new AtomicInteger();
            var entered = new CountDownLatch(1);
            var release = new CountDownLatch(1);
            var provider = new AsyncExecutorProvider(() -> {
                lookups.incrementAndGet();
                entered.countDown();
                await(release);
                return List.of(new StubProvider());
            }, () -> new ThreadPoolAsyncExecutor(Runnable::run));
            var first = new Thread(provider::get);
            var second = new Thread(provider::get);

            first.start();
            entered.await();
            second.start();
            while (second.getState() != Thread.State.BLOCKED) {
                Thread.onSpinWait();
            }
            release.countDown();
            first.join();
            second.join();

            assertSoftly(softly -> softly.assertThat(lookups).hasValue(1));
        }

        private void await(CountDownLatch latch) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
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
        @DisplayName("Should close safely before any selection")
        void shouldCloseBeforeSelection() {
            var creations = new AtomicInteger();
            var provider = fallbackOnly(creations);

            provider.close();

            assertSoftly(softly -> softly.assertThat(creations).hasValue(0));
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

    @Nested
    @DisplayName("When resolving the built-in strategy")
    class WhenTheStrategyResolution {

        private static final String EXECUTOR = "jnosql.repository.async.executor";

        private AsyncExecutorProvider configured(Settings settings, AtomicInteger creations,
                                                 List<RepositoryAsyncExecutor> found) {
            return new AsyncExecutorProvider(() -> found, () -> {
                creations.incrementAndGet();
                return AsyncExecutorStrategy.from(settings).create(settings);
            });
        }

        @Test
        @DisplayName("Should use platform threads when no executor is configured")
        void shouldDefaultToPlatform() {
            try (var provider = configured(Settings.of(Map.of()), new AtomicInteger(), List.of())) {
                var thread = provider.get().execute(Thread::currentThread).toCompletableFuture().join();

                assertSoftly(softly -> softly.assertThat(thread.isVirtual()).isFalse());
            }
        }

        @Test
        @DisplayName("Should use platform threads when configured")
        void shouldUsePlatform() {
            try (var provider = configured(Settings.of(Map.of(EXECUTOR, "platform")), new AtomicInteger(), List.of())) {
                var thread = provider.get().execute(Thread::currentThread).toCompletableFuture().join();

                assertSoftly(softly -> softly.assertThat(thread.isVirtual()).isFalse());
            }
        }

        @Test
        @DisplayName("Should use virtual threads when configured")
        void shouldUseVirtual() {
            try (var provider = configured(Settings.of(Map.of(EXECUTOR, "virtual")), new AtomicInteger(), List.of())) {
                var thread = provider.get().execute(Thread::currentThread).toCompletableFuture().join();

                assertSoftly(softly -> softly.assertThat(thread.isVirtual()).isTrue());
            }
        }

        @Test
        @DisplayName("Should fail on an unsupported executor and retry once it is corrected")
        void shouldFailOnUnsupportedExecutor() {
            var value = new AtomicReference<String>("fixed");
            var provider = new AsyncExecutorProvider(List::of, () -> {
                var settings = Settings.of(Map.of(EXECUTOR, value.get()));
                return AsyncExecutorStrategy.from(settings).create(settings);
            });

            assertThatThrownBy(provider::get).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining(EXECUTOR).hasMessageContaining("fixed");
            value.set("virtual");
            try (provider) {
                assertSoftly(softly -> softly.assertThat(provider.get()).isInstanceOf(ThreadPoolAsyncExecutor.class));
            }
        }

        @Test
        @DisplayName("Should give a custom provider precedence without reading any built-in setting")
        void shouldPreferCustomProvider() {
            var stub = new StubProvider();
            var settings = new RecordingSettings(Settings.of(Map.of(EXECUTOR, "invalid",
                    "jnosql.repository.async.pool-size", "invalid", "jnosql.repository.async.queue-capacity", "-1")));
            var creations = new AtomicInteger();

            var provider = configured(settings.settings, creations, List.of(stub));

            assertSoftly(softly -> {
                softly.assertThat(provider.get()).isSameAs(stub);
                softly.assertThat(creations).hasValue(0);
                softly.assertThat(settings.accesses).isEmpty();
            });
        }

        @Test
        @DisplayName("Should keep failing on conflicting providers without reading any built-in setting")
        void shouldKeepConflictBehaviour() {
            var settings = new RecordingSettings(Settings.of(Map.of(EXECUTOR, "virtual")));
            var provider = configured(settings.settings, new AtomicInteger(),
                    List.of(new StubProvider(), new OtherProvider()));

            assertSoftly(softly -> {
                softly.assertThatThrownBy(provider::get).isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining(StubProvider.class.getName())
                        .hasMessageContaining(OtherProvider.class.getName());
                softly.assertThat(settings.accesses).isEmpty();
            });
        }

        @Test
        @DisplayName("Should shut the owned virtual executor down on close")
        void shouldShutDownOwnedVirtualExecutor() {
            var provider = configured(Settings.of(Map.of(EXECUTOR, "virtual")), new AtomicInteger(), List.of());
            var executor = provider.get();

            provider.close();

            assertSoftly(softly -> softly.assertThat(executor.execute(() -> "late").toCompletableFuture()
                    .isCompletedExceptionally()).isTrue());
        }

        @Test
        @DisplayName("Should shut the owned platform executor down on close")
        void shouldShutDownOwnedPlatformExecutor() {
            var provider = configured(Settings.of(Map.of(EXECUTOR, "platform")), new AtomicInteger(), List.of());
            var executor = provider.get();

            provider.close();

            assertSoftly(softly -> softly.assertThat(executor.execute(() -> "late").toCompletableFuture()
                    .isCompletedExceptionally()).isTrue());
        }

        @Test
        @DisplayName("Should resolve one executor when callers race")
        void shouldResolveOnceWhenRacing() throws Exception {
            int callers = 8;
            var creations = new AtomicInteger();
            var start = new CountDownLatch(1);
            var provider = configured(Settings.of(Map.of(EXECUTOR, "virtual")), creations, List.of());
            try (provider; var pool = Executors.newFixedThreadPool(callers)) {
                var futures = Stream.generate(() -> pool.submit(() -> {
                    start.await();
                    return provider.get();
                })).limit(callers).toList();
                start.countDown();
                var resolved = new java.util.HashSet<RepositoryAsyncExecutor>();
                for (var future : futures) {
                    resolved.add(future.get(5, TimeUnit.SECONDS));
                }

                assertSoftly(softly -> {
                    softly.assertThat(resolved).hasSize(1);
                    softly.assertThat(creations).hasValue(1);
                });
            }
        }
    }

    private static final class RecordingSettings {
        private final List<String> accesses = new java.util.concurrent.CopyOnWriteArrayList<>();
        private final Settings settings;

        private RecordingSettings(Settings delegate) {
            this.settings = (Settings) java.lang.reflect.Proxy.newProxyInstance(Settings.class.getClassLoader(),
                    new Class<?>[]{Settings.class}, (proxy, method, args) -> {
                        accesses.add(method.getName());
                        return method.invoke(delegate, args);
                    });
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
