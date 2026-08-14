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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

class LazyLongSupplierTest {

    @Nested
    @DisplayName("getAsLong")
    class GetAsLong {

        @DisplayName("Should load value lazily only once")
        @Test
        void shouldLoadValueLazilyOnlyOnce() throws Exception {

            // given
            AtomicInteger counter = new AtomicInteger();

            LongSupplier delegate = () -> {
                counter.incrementAndGet();
                return 42L;
            };

            LazyLongSupplier supplier = LazyLongSupplier.of(delegate);

            // when
            long first = supplier.getAsLong();
            long second = supplier.getAsLong();
            long third = supplier.getAsLong();

            // then
            assertThat(first).isEqualTo(42L);
            assertThat(second).isEqualTo(42L);
            assertThat(third).isEqualTo(42L);
            assertThat(counter.get()).isEqualTo(1);
        }

        @DisplayName("Should not execute supplier before first access")
        @Test
        void shouldNotExecuteSupplierBeforeFirstAccess() {

            // given
            AtomicInteger counter = new AtomicInteger();

            LongSupplier delegate = () -> {
                counter.incrementAndGet();
                return 10L;
            };

            LazyLongSupplier.of(delegate);

            // then
            assertThat(counter.get()).isZero();
        }

        @DisplayName("Should cache computed value")
        @Test
        void shouldCacheComputedValue() {

            // given
            AtomicInteger counter = new AtomicInteger();

            LongSupplier delegate = () -> {
                counter.incrementAndGet();
                return counter.get();
            };

            LazyLongSupplier supplier = LazyLongSupplier.of(delegate);

            // when
            long first = supplier.getAsLong();
            long second = supplier.getAsLong();

            // then
            assertThat(first).isEqualTo(1L);
            assertThat(second).isEqualTo(1L);
            assertThat(counter.get()).isEqualTo(1);
        }

        @DisplayName("Should execute supplier only once under concurrent access")
        @Test
        void shouldExecuteSupplierOnlyOnceUnderConcurrentAccess() throws Exception {

            // given
            AtomicInteger counter = new AtomicInteger();

            LongSupplier delegate = () -> {
                counter.incrementAndGet();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }

                return 99L;
            };

            LazyLongSupplier supplier = LazyLongSupplier.of(delegate);

            var executor = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(1);

            // when
            Future<Long>[] futures = new Future[10];

            for (int index = 0; index < futures.length; index++) {

                futures[index] = executor.submit(() -> {
                    latch.await();
                    return supplier.getAsLong();
                });
            }

            latch.countDown();

            // then
            for (Future<Long> future : futures) {
                assertThat(future.get()).isEqualTo(99L);
            }

            assertThat(counter.get()).isEqualTo(1);

            executor.shutdown();
        }
    }

    @Nested
    @DisplayName("error handling")
    class ErrorHandling {

        @DisplayName("Should cache unsupported operation exception")
        @Test
        void shouldCacheUnsupportedOperationException() {

            // given
            AtomicInteger counter = new AtomicInteger();

            LongSupplier delegate = () -> {
                counter.incrementAndGet();
                throw new UnsupportedOperationException("Totals are not supported");
            };

            LazyLongSupplier supplier = (LazyLongSupplier)
                    LazyLongSupplier.of(delegate);

            // when
            Throwable first = catchThrowable(supplier::getAsLong);
            Throwable second = catchThrowable(supplier::getAsLong);

            // then
            assertThat(first)
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Totals are not supported");

            assertThat(second)
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Totals are not supported");

            assertThat(counter.get()).isEqualTo(1);
        }

        @DisplayName("Should cache runtime exception")
        @Test
        void shouldCacheRuntimeException() {

            // given
            AtomicInteger counter = new AtomicInteger();

            LongSupplier delegate = () -> {
                counter.incrementAndGet();
                throw new IllegalStateException("Database failure");
            };

            LazyLongSupplier supplier = (LazyLongSupplier)
                    LazyLongSupplier.of(delegate);

            // when
            Throwable first = catchThrowable(supplier::getAsLong);
            Throwable second = catchThrowable(supplier::getAsLong);

            // then
            assertThat(first)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Database failure");

            assertThat(second)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Database failure");

            assertThat(counter.get()).isEqualTo(1);
        }

        @DisplayName("Should execute supplier only once when exception happens concurrently")
        @Test
        void shouldExecuteSupplierOnlyOnceWhenExceptionHappensConcurrently() throws Exception {

            // given
            AtomicInteger counter = new AtomicInteger();

            LongSupplier delegate = () -> {
                counter.incrementAndGet();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }

                throw new UnsupportedOperationException("Totals unsupported");
            };

            LazyLongSupplier supplier = (LazyLongSupplier)
                    LazyLongSupplier.of(delegate);

            var executor = Executors.newFixedThreadPool(10);

            CountDownLatch latch = new CountDownLatch(1);

            Future<Throwable>[] futures = new Future[10];

            // when
            for (int index = 0; index < futures.length; index++) {

                futures[index] = executor.submit(() -> {
                    latch.await();
                    return catchThrowable(supplier::getAsLong);
                });
            }

            latch.countDown();

            // then
            for (Future<Throwable> future : futures) {

                assertThat(future.get())
                        .isInstanceOf(UnsupportedOperationException.class)
                        .hasMessage("Totals unsupported");
            }

            assertThat(counter.get()).isEqualTo(1);

            executor.shutdown();
        }
    }
}
