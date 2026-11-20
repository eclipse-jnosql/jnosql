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
import org.eclipse.jnosql.mapping.core.repository.returns.RepositoryExecutorResolver.ResolvedExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.eclipse.jnosql.mapping.core.repository.returns.AsyncReturnFixtures.asynchronous;

class CompletionStageRepositoryReturnTest {

    private final ExecutorService executor = Executors.newSingleThreadExecutor(
            runnable -> new Thread(runnable, "test-async"));

    private final CompletionStageRepositoryReturn repositoryReturn = new CompletionStageRepositoryReturn(
            new RepositoryAsyncExecutor(() -> new ResolvedExecutor(executor, false)));

    @AfterEach
    void shutdown() {
        executor.shutdownNow();
    }

    @Nested
    @DisplayName("When selecting the return handler")
    class WhenTheSelection {

        @Test
        @DisplayName("Should be compatible only with CompletionStage")
        void shouldBeCompatibleOnlyWithCompletionStage() {
            assertSoftly(softly -> {
                softly.assertThat(repositoryReturn.isCompatible(Product.class, CompletionStage.class))
                        .as("CompletionStage").isTrue();
                softly.assertThat(repositoryReturn.isCompatible(Product.class, CompletableFuture.class))
                        .as("CompletableFuture").isFalse();
                softly.assertThat(repositoryReturn.isCompatible(Product.class, Optional.class))
                        .as("Optional").isFalse();
            });
        }
    }

    @Nested
    @DisplayName("When searching asynchronously")
    class WhenTheSearch {

        @Test
        @DisplayName("Should return the optional result on the executor thread")
        void shouldReturnOptional() throws Exception {
            var thread = new AtomicReference<String>();
            var dynamic = asynchronous(CompletionStage.class, Optional.class, () -> {
                thread.set(Thread.currentThread().getName());
                return Optional.of(new Product("Notebook"));
            }, Stream::empty);

            var stage = (CompletionStage<?>) repositoryReturn.convert(dynamic);

            assertSoftly(softly -> {
                softly.assertThat(join(stage)).as("result").isEqualTo(Optional.of(new Product("Notebook")));
                softly.assertThat(thread.get()).as("thread").isEqualTo("test-async");
            });
        }

        @Test
        @DisplayName("Should return an empty optional")
        void shouldReturnEmptyOptional() throws Exception {
            var dynamic = asynchronous(CompletionStage.class, Optional.class, Optional::empty, Stream::empty);

            var stage = (CompletionStage<?>) repositoryReturn.convert(dynamic);

            assertThat(join(stage)).isEqualTo(Optional.empty());
        }

        @Test
        @DisplayName("Should return the list result")
        void shouldReturnList() throws Exception {
            var dynamic = asynchronous(CompletionStage.class, List.class, Optional::empty,
                    () -> Stream.of(new Product("Notebook"), new Product("Pen")));

            var stage = (CompletionStage<?>) repositoryReturn.convert(dynamic);

            assertThat(join(stage)).isEqualTo(List.of(new Product("Notebook"), new Product("Pen")));
        }

        @Test
        @DisplayName("Should return the single instance")
        void shouldReturnInstance() throws Exception {
            var dynamic = asynchronous(CompletionStage.class, Product.class,
                    () -> Optional.of(new Product("Notebook")), Stream::empty);

            var stage = (CompletionStage<?>) repositoryReturn.convert(dynamic);

            assertThat(join(stage)).isEqualTo(new Product("Notebook"));
        }

        @Test
        @DisplayName("Should run the operation exactly once and without blocking the caller")
        void shouldRunOnceWithoutBlockingTheCaller() throws Exception {
            var calls = new AtomicInteger();
            var release = new CompletableFuture<Void>();
            var dynamic = asynchronous(CompletionStage.class, List.class, Optional::empty, () -> {
                calls.incrementAndGet();
                release.join();
                return Stream.of(new Product("Notebook"));
            });

            var stage = (CompletionStage<?>) repositoryReturn.convert(dynamic);
            boolean doneWhileBlocked = stage.toCompletableFuture().isDone();
            release.complete(null);

            assertSoftly(softly -> {
                softly.assertThat(doneWhileBlocked).as("caller returned before completion").isFalse();
                softly.assertThat(join(stage)).as("result").isEqualTo(List.of(new Product("Notebook")));
                softly.assertThat(calls).as("calls").hasValue(1);
            });
        }

        @Test
        @DisplayName("Should not let the caller complete the stage")
        void shouldNotLetTheCallerCompleteTheStage() {
            var dynamic = asynchronous(CompletionStage.class, Optional.class, Optional::empty, Stream::empty);

            var stage = (CompletableFuture<?>) repositoryReturn.convert(dynamic);

            assertThatThrownBy(() -> stage.complete(null)).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("When the asynchronous search fails")
    class WhenTheSearchFails {

        @Test
        @DisplayName("Should complete exceptionally with the original exception")
        void shouldCompleteExceptionally() {
            var failure = new IllegalArgumentException("invalid query");
            var dynamic = asynchronous(CompletionStage.class, Optional.class, () -> {
                throw failure;
            }, Stream::empty);

            var stage = (CompletionStage<?>) repositoryReturn.convert(dynamic);

            assertThatThrownBy(() -> stage.toCompletableFuture().get(5, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class)
                    .hasCause(failure);
        }

        @Test
        @DisplayName("Should allow recovering with exceptionally")
        void shouldRecover() throws Exception {
            var dynamic = asynchronous(CompletionStage.class, Optional.class, () -> {
                throw new IllegalStateException("down");
            }, Stream::empty);

            @SuppressWarnings("unchecked")
            var original = (CompletionStage<Object>) repositoryReturn.convert(dynamic);
            var stage = original.exceptionally(error -> Optional.empty());

            assertThat(join(stage)).isEqualTo(Optional.empty());
        }

        @Test
        @DisplayName("Should complete exceptionally when the executor rejects the task")
        void shouldCompleteExceptionallyOnRejection() {
            var rejecting = new CompletionStageRepositoryReturn(new RepositoryAsyncExecutor(
                    () -> new ResolvedExecutor(task -> {
                        throw new RejectedExecutionException("full");
                    }, false)));
            var dynamic = asynchronous(CompletionStage.class, Optional.class, Optional::empty, Stream::empty);

            var stage = (CompletionStage<?>) rejecting.convert(dynamic);

            assertThatThrownBy(() -> stage.toCompletableFuture().get(5, TimeUnit.SECONDS))
                    .hasCauseInstanceOf(RejectedExecutionException.class);
        }

        @Test
        @DisplayName("Should reject a missing asynchronous result type")
        void shouldRejectMissingResultType() {
            var dynamic = asynchronous(CompletionStage.class, null, Optional::empty, Stream::empty);

            assertThatThrownBy(() -> repositoryReturn.convert(dynamic))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("find");
        }

        @Test
        @DisplayName("Should reject nested asynchronous result types")
        void shouldRejectNestedAsynchronousTypes() {
            var dynamic = asynchronous(CompletionStage.class, CompletableFuture.class, Optional::empty, Stream::empty);

            assertThatThrownBy(() -> repositoryReturn.convert(dynamic))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Nested");
        }
    }

    private static Object join(CompletionStage<?> stage) {
        return stage.toCompletableFuture().orTimeout(5, TimeUnit.SECONDS).join();
    }
}
