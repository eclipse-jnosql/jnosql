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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class RepositoryExecutorResolverTest {

    private static final Executor CDI_EXECUTOR = Runnable::run;
    private static final Executor JNDI_EXECUTOR = Runnable::run;
    private static final Executor NAMED_EXECUTOR = Runnable::run;

    private static final String EXECUTOR_KEY = "jnosql.repository.async.executor";
    private static final String POOL_SIZE_KEY = "jnosql.repository.async.pool-size";

    private static class FakeLookup implements ExecutorLookup {
        private Optional<Executor> cdi = Optional.empty();
        private Optional<Executor> jndi = Optional.empty();
        private Optional<Executor> named = Optional.empty();
        private int jndiCalls;

        @Override
        public Optional<Executor> managedFromCdi() {
            return cdi;
        }

        @Override
        public Optional<Executor> managedFromJndi() {
            jndiCalls++;
            return jndi;
        }

        @Override
        public Optional<Executor> named(String name) {
            return "reports".equals(name) ? named : Optional.empty();
        }
    }

    private static RepositoryExecutorResolver resolver(FakeLookup lookup, Map<String, Object> settings) {
        return new RepositoryExecutorResolver(Settings.of(settings), lookup);
    }

    @Nested
    @DisplayName("When a Jakarta Concurrency executor is available")
    class WhenTheManagedExecutorIsAvailable {

        @Test
        @DisplayName("Should prefer the CDI executor and not own it")
        void shouldPreferCdiExecutor() {
            var lookup = new FakeLookup();
            lookup.cdi = Optional.of(CDI_EXECUTOR);
            lookup.jndi = Optional.of(JNDI_EXECUTOR);

            var resolved = resolver(lookup, Map.of()).resolve();

            assertSoftly(softly -> {
                softly.assertThat(resolved.executor()).as("executor").isSameAs(CDI_EXECUTOR);
                softly.assertThat(resolved.owned()).as("owned").isFalse();
                softly.assertThat(lookup.jndiCalls).as("jndi lookups").isZero();
            });
        }

        @Test
        @DisplayName("Should use the container default when there is no CDI executor")
        void shouldUseContainerDefault() {
            var lookup = new FakeLookup();
            lookup.jndi = Optional.of(JNDI_EXECUTOR);

            var resolved = resolver(lookup, Map.of(EXECUTOR_KEY, "auto")).resolve();

            assertSoftly(softly -> {
                softly.assertThat(resolved.executor()).as("executor").isSameAs(JNDI_EXECUTOR);
                softly.assertThat(resolved.owned()).as("owned").isFalse();
            });
        }

        @Test
        @DisplayName("Should fail when the CDI selection is ambiguous")
        void shouldFailWhenAmbiguous() {
            var lookup = new FakeLookup() {
                @Override
                public Optional<Executor> managedFromCdi() {
                    throw new IllegalStateException("ambiguous");
                }
            };

            assertThatThrownBy(() -> resolver(lookup, Map.of()).resolve())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("ambiguous");
        }
    }

    @Nested
    @DisplayName("When an executor is configured")
    class WhenTheExecutorIsConfigured {

        @Test
        @DisplayName("Should use the configured executor even when a managed executor exists")
        void shouldUseConfiguredExecutor() {
            var lookup = new FakeLookup();
            lookup.cdi = Optional.of(CDI_EXECUTOR);
            lookup.named = Optional.of(NAMED_EXECUTOR);

            var resolved = resolver(lookup, Map.of(EXECUTOR_KEY, "reports")).resolve();

            assertSoftly(softly -> {
                softly.assertThat(resolved.executor()).as("executor").isSameAs(NAMED_EXECUTOR);
                softly.assertThat(resolved.owned()).as("owned").isFalse();
            });
        }

        @Test
        @DisplayName("Should fail instead of falling back when the configured executor is missing")
        void shouldFailWhenConfiguredExecutorIsMissing() {
            var lookup = new FakeLookup();
            lookup.cdi = Optional.of(CDI_EXECUTOR);

            assertThatThrownBy(() -> resolver(lookup, Map.of(EXECUTOR_KEY, "missing")).resolve())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("missing")
                    .hasMessageContaining(EXECUTOR_KEY);
        }
    }

    @Nested
    @DisplayName("When Jakarta Concurrency is unavailable")
    class WhenTheFallbackIsNeeded {

        @Test
        @DisplayName("Should create an owned bounded executor")
        void shouldCreateOwnedExecutor() {
            var resolved = resolver(new FakeLookup(), Map.of(POOL_SIZE_KEY, "3")).resolve();
            var pool = (ThreadPoolExecutor) resolved.executor();
            try {
                assertSoftly(softly -> {
                    softly.assertThat(resolved.owned()).as("owned").isTrue();
                    softly.assertThat(pool.getMaximumPoolSize()).as("pool size").isEqualTo(3);
                    softly.assertThat(pool.getQueue().remainingCapacity()).as("bounded queue")
                            .isEqualTo(RepositoryExecutorResolver.QUEUE_CAPACITY);
                });
            } finally {
                pool.shutdownNow();
            }
        }

        @Test
        @DisplayName("Should default the pool size to twice the processors")
        void shouldDefaultPoolSize() {
            var resolved = resolver(new FakeLookup(), Map.of()).resolve();
            var pool = (ThreadPoolExecutor) resolved.executor();
            try {
                assertThat(pool.getMaximumPoolSize()).isEqualTo(2 * Runtime.getRuntime().availableProcessors());
            } finally {
                pool.shutdownNow();
            }
        }

        @Test
        @DisplayName("Should run tasks on daemon threads")
        void shouldUseDaemonThreads() throws Exception {
            var resolved = resolver(new FakeLookup(), Map.of()).resolve();
            var pool = (ThreadPoolExecutor) resolved.executor();
            try {
                var thread = new java.util.concurrent.CompletableFuture<Thread>();
                pool.execute(() -> thread.complete(Thread.currentThread()));
                assertSoftly(softly -> {
                    softly.assertThat(thread.join().isDaemon()).as("daemon").isTrue();
                    softly.assertThat(thread.join().getName()).as("name").startsWith("jnosql-repository-async-");
                });
            } finally {
                pool.shutdownNow();
            }
        }

        @Test
        @DisplayName("Should fail when the pool size is invalid")
        void shouldFailWhenPoolSizeIsInvalid() {
            assertSoftly(softly -> {
                for (var value : new String[]{"0", "-1", "many"}) {
                    softly.assertThatThrownBy(() -> resolver(new FakeLookup(), Map.of(POOL_SIZE_KEY, value)).resolve())
                            .as(value)
                            .isInstanceOf(IllegalStateException.class)
                            .hasMessageContaining(POOL_SIZE_KEY);
                }
            });
        }
    }
}
