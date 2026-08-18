/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

class LazyLongSupplierTest {

    @Test
    void shouldRejectNullDelegate() {
        assertThatThrownBy(() -> LazyLongSupplier.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("delegate is required");
    }

    @Test
    void shouldLoadAndCacheValueLazily() {
        AtomicInteger invocations = new AtomicInteger();
        LazyLongSupplier supplier = LazyLongSupplier.of(() -> {
            invocations.incrementAndGet();
            return 42L;
        });

        assertThat(invocations.get()).isZero();
        assertThat(supplier.getAsLong()).isEqualTo(42L);
        assertThat(supplier.getAsLong()).isEqualTo(42L);
        assertThat(invocations.get()).isEqualTo(1);
    }

    @Test
    void shouldCacheRuntimeException() {
        AtomicInteger invocations = new AtomicInteger();
        IllegalStateException failure = new IllegalStateException("Database failure");
        LazyLongSupplier supplier = LazyLongSupplier.of(() -> {
            invocations.incrementAndGet();
            throw failure;
        });

        Throwable first = catchThrowable(supplier::getAsLong);
        Throwable second = catchThrowable(supplier::getAsLong);

        assertThat(first).isSameAs(failure);
        assertThat(second).isSameAs(failure);
        assertThat(invocations.get()).isEqualTo(1);
    }

    @Test
    void shouldCacheError() {
        AtomicInteger invocations = new AtomicInteger();
        AssertionError failure = new AssertionError("Database failure");
        LongSupplier delegate = () -> {
            invocations.incrementAndGet();
            throw failure;
        };
        LazyLongSupplier supplier = LazyLongSupplier.of(delegate);

        Throwable first = catchThrowable(supplier::getAsLong);
        Throwable second = catchThrowable(supplier::getAsLong);

        assertThat(first).isSameAs(failure);
        assertThat(second).isSameAs(failure);
        assertThat(invocations.get()).isEqualTo(1);
    }

    @Test
    void shouldExecuteDelegateOnlyOnceUnderConcurrentAccess() throws Exception {
        int callerCount = 8;
        AtomicInteger invocations = new AtomicInteger();
        CountDownLatch ready = new CountDownLatch(callerCount);
        CountDownLatch start = new CountDownLatch(1);
        LazyLongSupplier supplier = LazyLongSupplier.of(() -> {
            invocations.incrementAndGet();
            return 99L;
        });
        ExecutorService executor = Executors.newFixedThreadPool(callerCount);
        List<Future<Long>> results = new ArrayList<>(callerCount);

        try {
            for (int index = 0; index < callerCount; index++) {
                results.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    return supplier.getAsLong();
                }));
            }

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            for (Future<Long> result : results) {
                assertThat(result.get(5, TimeUnit.SECONDS)).isEqualTo(99L);
            }
            assertThat(invocations.get()).isEqualTo(1);
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }
}
