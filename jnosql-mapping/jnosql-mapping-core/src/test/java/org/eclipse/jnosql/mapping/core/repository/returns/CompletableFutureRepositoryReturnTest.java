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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.eclipse.jnosql.mapping.core.repository.returns.AsyncReturnFixtures.asynchronous;

class CompletableFutureRepositoryReturnTest {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final CompletableFutureRepositoryReturn repositoryReturn = new CompletableFutureRepositoryReturn(
            new AsyncExecutorProvider(List::of, () -> new ThreadPoolAsyncExecutor(executor)));

    @AfterEach
    void shutdown() {
        executor.shutdownNow();
    }

    @Nested
    @DisplayName("When selecting the return handler")
    class WhenTheSelection {

        @Test
        @DisplayName("Should be compatible only with CompletableFuture")
        void shouldBeCompatibleOnlyWithCompletableFuture() {
            assertSoftly(softly -> {
                softly.assertThat(repositoryReturn.isCompatible(Product.class, CompletableFuture.class))
                        .as("CompletableFuture").isTrue();
                softly.assertThat(repositoryReturn.isCompatible(Product.class, CompletionStage.class))
                        .as("CompletionStage").isFalse();
            });
        }
    }

    @Nested
    @DisplayName("When searching asynchronously")
    class WhenTheSearch {

        @Test
        @DisplayName("Should return the list result")
        void shouldReturnList() {
            var dynamic = asynchronous(CompletableFuture.class, List.class, Optional::empty,
                    () -> Stream.of(new Product("Notebook")));

            var future = (CompletableFuture<?>) repositoryReturn.convert(dynamic);

            assertThat(future.orTimeout(5, TimeUnit.SECONDS).join()).isEqualTo(List.of(new Product("Notebook")));
        }

        @Test
        @DisplayName("Should return the optional result")
        void shouldReturnOptional() {
            var dynamic = asynchronous(CompletableFuture.class, Optional.class,
                    () -> Optional.of(new Product("Notebook")), Stream::empty);

            var future = (CompletableFuture<?>) repositoryReturn.convert(dynamic);

            assertThat(future.orTimeout(5, TimeUnit.SECONDS).join()).isEqualTo(Optional.of(new Product("Notebook")));
        }

        @Test
        @DisplayName("Should compose independent futures")
        void shouldCompose() {
            var products = asynchronous(CompletableFuture.class, List.class, Optional::empty,
                    () -> Stream.of(new Product("Notebook")));
            var featured = asynchronous(CompletableFuture.class, Optional.class,
                    () -> Optional.of(new Product("Notebook")), Stream::empty);

            var list = (CompletableFuture<?>) repositoryReturn.convert(products);
            var item = (CompletableFuture<?>) repositoryReturn.convert(featured);
            var combined = list.thenCombine(item, (l, i) -> !((List<?>) l).isEmpty() && ((Optional<?>) i).isPresent());

            assertThat(combined.orTimeout(5, TimeUnit.SECONDS).join()).isTrue();
        }
    }

    @Nested
    @DisplayName("When the asynchronous search is cancelled or fails")
    class WhenTheSearchDoesNotComplete {

        @Test
        @DisplayName("Should skip an operation cancelled before it starts")
        void shouldSkipCancelledOperation() throws Exception {
            var release = new CompletableFuture<Void>();
            var calls = new AtomicInteger();
            executor.execute(release::join);
            var dynamic = asynchronous(CompletableFuture.class, Optional.class, () -> {
                calls.incrementAndGet();
                return Optional.empty();
            }, Stream::empty);

            var future = (CompletableFuture<?>) repositoryReturn.convert(dynamic);
            future.cancel(true);
            release.complete(null);
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            assertSoftly(softly -> {
                softly.assertThat(future.isCancelled()).as("cancelled").isTrue();
                softly.assertThat(calls).as("calls").hasValue(0);
            });
        }

        @Test
        @DisplayName("Should complete exceptionally with the original exception")
        void shouldCompleteExceptionally() {
            var failure = new IllegalStateException("down");
            var dynamic = asynchronous(CompletableFuture.class, Optional.class, () -> {
                throw failure;
            }, Stream::empty);

            var future = (CompletableFuture<?>) repositoryReturn.convert(dynamic);

            assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS)).hasCause(failure);
        }
    }
}
