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

import org.eclipse.jnosql.mapping.core.repository.returns.RepositoryExecutorResolver.ResolvedExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class RepositoryAsyncExecutorTest {

    @Nested
    @DisplayName("When resolving the executor")
    class WhenTheExecutorIsResolved {

        @Test
        @DisplayName("Should resolve once and reuse the executor")
        void shouldResolveOnce() {
            var resolutions = new AtomicInteger();
            var service = Executors.newSingleThreadExecutor();
            try (var async = new RepositoryAsyncExecutor(() -> {
                resolutions.incrementAndGet();
                return new ResolvedExecutor(service, false);
            })) {
                async.supply(() -> 1).join();
                async.supply(() -> 2).join();
                async.supply(() -> 3).join();

                assertSoftly(softly -> softly.assertThat(resolutions).as("resolutions").hasValue(1));
            } finally {
                service.shutdownNow();
            }
        }

        @Test
        @DisplayName("Should complete exceptionally when the executor cannot be resolved")
        void shouldCompleteExceptionallyWhenResolutionFails() {
            var failure = new IllegalStateException("not configured");
            try (var async = new RepositoryAsyncExecutor(() -> {
                throw failure;
            })) {
                var future = async.supply(() -> 1);

                assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class)
                        .hasCause(failure);
            }
        }
    }

    @Nested
    @DisplayName("When shutting down")
    class WhenTheShutdown {

        @Test
        @DisplayName("Should not shut down an executor it does not own")
        void shouldKeepForeignExecutor() {
            var service = Executors.newSingleThreadExecutor();
            try {
                var async = new RepositoryAsyncExecutor(() -> new ResolvedExecutor(service, false));
                async.supply(() -> 1).join();

                async.close();

                assertSoftly(softly -> softly.assertThat(service.isShutdown()).as("shutdown").isFalse());
            } finally {
                service.shutdownNow();
            }
        }

        @Test
        @DisplayName("Should shut down an executor it owns")
        void shouldShutdownOwnedExecutor() {
            ExecutorService service = Executors.newSingleThreadExecutor();
            var async = new RepositoryAsyncExecutor(() -> new ResolvedExecutor(service, true));
            async.supply(() -> 1).join();

            async.close();

            assertSoftly(softly -> softly.assertThat(service.isShutdown()).as("shutdown").isTrue());
        }
    }

    @Nested
    @DisplayName("When the bounded executor is saturated")
    class WhenTheExecutorIsSaturated {

        @Test
        @DisplayName("Should complete exceptionally with the rejection instead of running on the caller")
        void shouldCompleteExceptionallyOnRejection() {
            var pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(1));
            var release = new CompletableFuture<Void>();
            try (var async = new RepositoryAsyncExecutor(() -> new ResolvedExecutor(pool, false))) {
                async.supply(release::join);
                async.supply(release::join);

                var rejected = async.supply(() -> "never runs");

                assertSoftly(softly -> {
                    softly.assertThat(rejected.isCompletedExceptionally()).as("rejected").isTrue();
                    softly.assertThatThrownBy(rejected::join).as("cause")
                            .hasCauseInstanceOf(RejectedExecutionException.class);
                });
                release.complete(null);
            } finally {
                pool.shutdownNow();
            }
        }
    }
}
