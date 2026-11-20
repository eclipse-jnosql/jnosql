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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class ThreadPoolAsyncExecutorTest {

    @Nested
    @DisplayName("When running operations")
    class WhenTheExecution {

        @Test
        @DisplayName("Should run on another thread and complete with the result")
        void shouldRunOnAnotherThread() {
            var caller = Thread.currentThread();
            try (var async = ThreadPoolAsyncExecutor.owning(Settings.of(Map.of()))) {
                var thread = async.execute(Thread::currentThread).join();

                assertSoftly(softly -> {
                    softly.assertThat(thread).isNotSameAs(caller);
                    softly.assertThat(thread.getName()).startsWith("jnosql-repository-async-");
                    softly.assertThat(thread.isDaemon()).isTrue();
                });
            }
        }

        @Test
        @DisplayName("Should complete exceptionally with the original failure")
        void shouldKeepOriginalFailure() {
            var failure = new IllegalArgumentException("invalid");
            try (var async = ThreadPoolAsyncExecutor.owning(Settings.of(Map.of()))) {
                var future = async.execute(() -> {
                    throw failure;
                });

                assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class)
                        .hasCause(failure);
            }
        }

        @Test
        @DisplayName("Should skip an operation cancelled before it starts")
        void shouldSkipCancelledOperation() {
            var gate = new CompletableFuture<Void>();
            var ran = new CompletableFuture<Boolean>();
            var pool = Executors.newSingleThreadExecutor();
            try (var async = new ThreadPoolAsyncExecutor(pool)) {
                async.execute(gate::join);
                var cancelled = async.execute(() -> ran.complete(true));
                cancelled.cancel(false);
                gate.complete(null);
                var marker = async.execute(() -> "done").orTimeout(5, TimeUnit.SECONDS).join();

                assertSoftly(softly -> {
                    softly.assertThat(marker).isEqualTo("done");
                    softly.assertThat(ran.isDone()).isFalse();
                });
            }
        }
    }

    @Nested
    @DisplayName("When configuring the pool")
    class WhenTheConfiguration {

        @Test
        @DisplayName("Should size the pool from the settings")
        void shouldUseConfiguredSize() {
            try (var async = ThreadPoolAsyncExecutor.owning(Settings.of(Map.of(
                    "jnosql.repository.async.pool-size", "3")))) {
                assertSoftly(softly -> softly.assertThat(async.execute(() -> 1).join()).isEqualTo(1));
            }
        }

        @Test
        @DisplayName("Should reject an invalid pool size")
        void shouldRejectInvalidSize() {
            var settings = Settings.of(Map.of("jnosql.repository.async.pool-size", "zero"));

            assertThatThrownBy(() -> ThreadPoolAsyncExecutor.owning(settings))
                    .isInstanceOf(IllegalStateException.class).hasMessageContaining("positive integer");
        }
    }

    @Nested
    @DisplayName("When shutting down")
    class WhenTheShutdown {

        @Test
        @DisplayName("Should shut the wrapped executor service down on close")
        void shouldShutDownOnClose() {
            var pool = Executors.newSingleThreadExecutor();

            new ThreadPoolAsyncExecutor(pool).close();

            assertSoftly(softly -> softly.assertThat(pool.isShutdown()).isTrue());
        }
    }

    @Nested
    @DisplayName("When the bounded executor is saturated")
    class WhenTheExecutorIsSaturated {

        @Test
        @DisplayName("Should complete exceptionally with the rejection instead of running on the caller")
        void shouldCompleteExceptionallyOnRejection() {
            var pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1));
            var release = new CompletableFuture<Void>();
            try (var async = new ThreadPoolAsyncExecutor(pool)) {
                async.execute(release::join);
                async.execute(release::join);

                var rejected = async.execute(() -> "never runs");

                assertSoftly(softly -> {
                    softly.assertThat(rejected.isCompletedExceptionally()).as("rejected").isTrue();
                    softly.assertThatThrownBy(rejected::join).as("cause")
                            .hasCauseInstanceOf(RejectedExecutionException.class);
                });
                release.complete(null);
            }
        }
    }
}
