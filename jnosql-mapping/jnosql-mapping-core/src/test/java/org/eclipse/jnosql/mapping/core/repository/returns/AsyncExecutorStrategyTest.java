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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class AsyncExecutorStrategyTest {

    private static final String EXECUTOR = "jnosql.repository.async.executor";
    private static final String POOL_SIZE = "jnosql.repository.async.pool-size";
    private static final String QUEUE_CAPACITY = "jnosql.repository.async.queue-capacity";

    private static Settings settings(String... keyValues) {
        var map = new HashMap<String, Object>();
        for (int index = 0; index < keyValues.length; index += 2) {
            map.put(keyValues[index], keyValues[index + 1]);
        }
        return Settings.of(map);
    }

    private static final class RecordingSettings {
        private final List<String> accesses = new ArrayList<>();
        private final Settings settings;

        private RecordingSettings(Settings delegate) {
            this.settings = (Settings) Proxy.newProxyInstance(Settings.class.getClassLoader(),
                    new Class<?>[]{Settings.class}, (proxy, method, args) -> {
                        accesses.add(method.getName() + Arrays.toString(args));
                        return method.invoke(delegate, args);
                    });
        }

        private boolean accessed(String key) {
            return accesses.stream().anyMatch(access -> access.contains(key));
        }
    }

    @Nested
    @DisplayName("When selecting the strategy")
    class WhenTheSelection {

        @Test
        @DisplayName("Should select platform threads by default")
        void shouldDefaultToPlatform() {
            assertSoftly(softly -> softly.assertThat(AsyncExecutorStrategy.from(Settings.of(Map.of())))
                    .isEqualTo(AsyncExecutorStrategy.PLATFORM));
        }

        @Test
        @DisplayName("Should select platform threads when configured")
        void shouldSelectPlatform() {
            assertSoftly(softly -> softly.assertThat(AsyncExecutorStrategy.from(settings(EXECUTOR, "platform")))
                    .isEqualTo(AsyncExecutorStrategy.PLATFORM));
        }

        @ParameterizedTest
        @ValueSource(strings = {"virtual", "VIRTUAL", " Virtual "})
        @DisplayName("Should select virtual threads when configured")
        void shouldSelectVirtual(String value) {
            assertSoftly(softly -> softly.assertThat(AsyncExecutorStrategy.from(settings(EXECUTOR, value)))
                    .isEqualTo(AsyncExecutorStrategy.VIRTUAL));
        }

        @ParameterizedTest
        @ValueSource(strings = {"fixed", "", "virtual,platform", "cached"})
        @DisplayName("Should reject an unsupported value naming the property, the value and the supported values")
        void shouldRejectUnsupported(String value) {
            var settings = settings(EXECUTOR, value);

            assertThatThrownBy(() -> AsyncExecutorStrategy.from(settings))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining(EXECUTOR)
                    .hasMessageContaining("'" + value + "'")
                    .hasMessageContaining("platform")
                    .hasMessageContaining("virtual");
        }
    }

    @Nested
    @DisplayName("When running on platform threads")
    class WhenThePlatformExecution {

        @Test
        @DisplayName("Should run on a named platform thread other than the caller")
        void shouldRunOnPlatformThread() {
            var caller = Thread.currentThread();
            try (var async = AsyncExecutorStrategy.PLATFORM.create(settings(EXECUTOR, "platform"))) {
                var thread = async.execute(Thread::currentThread).join();

                assertSoftly(softly -> {
                    softly.assertThat(thread).isNotSameAs(caller);
                    softly.assertThat(thread.isVirtual()).isFalse();
                    softly.assertThat(thread.getName()).startsWith("jnosql-repository-async-");
                });
            }
        }

        @Test
        @DisplayName("Should read the platform specific settings")
        void shouldReadPlatformSettings() {
            var recording = new RecordingSettings(settings(POOL_SIZE, "2", QUEUE_CAPACITY, "5"));

            try (var async = AsyncExecutorStrategy.PLATFORM.create(recording.settings)) {
                assertSoftly(softly -> {
                    softly.assertThat(recording.accessed(POOL_SIZE)).as("pool size").isTrue();
                    softly.assertThat(recording.accessed(QUEUE_CAPACITY)).as("queue capacity").isTrue();
                });
            }
        }

        @Test
        @DisplayName("Should reject the pool once closed")
        void shouldShutDownOnClose() {
            var async = AsyncExecutorStrategy.PLATFORM.create(Settings.of(Map.of()));

            async.close();

            assertSoftly(softly -> softly.assertThat(isRejected(async.execute(() -> "late"))).isTrue());
        }
    }

    @Nested
    @DisplayName("When running on virtual threads")
    class WhenTheVirtualExecution {

        @Test
        @DisplayName("Should run on a virtual thread other than the caller")
        void shouldRunOnVirtualThread() {
            var caller = Thread.currentThread();
            try (var async = AsyncExecutorStrategy.VIRTUAL.create(settings(EXECUTOR, "virtual"))) {
                var thread = async.execute(Thread::currentThread).join();

                assertSoftly(softly -> {
                    softly.assertThat(thread).isNotSameAs(caller);
                    softly.assertThat(thread.isVirtual()).isTrue();
                });
            }
        }

        @Test
        @DisplayName("Should use a new virtual thread for every operation")
        void shouldUseOneThreadPerOperation() {
            try (var async = AsyncExecutorStrategy.VIRTUAL.create(Settings.of(Map.of()))) {
                var first = async.execute(Thread::currentThread).join();
                var second = async.execute(Thread::currentThread).join();

                assertSoftly(softly -> softly.assertThat(first).isNotSameAs(second));
            }
        }

        @Test
        @DisplayName("Should ignore invalid platform specific settings")
        void shouldIgnoreInvalidPlatformSettings() {
            var settings = settings(EXECUTOR, "virtual", POOL_SIZE, "invalid", QUEUE_CAPACITY, "-1");

            try (var async = AsyncExecutorStrategy.VIRTUAL.create(settings)) {
                assertSoftly(softly -> softly.assertThat(async.execute(() -> Thread.currentThread().isVirtual())
                        .join()).isTrue());
            }
        }

        @Test
        @DisplayName("Should never access the platform specific settings")
        void shouldNeverAccessPlatformSettings() {
            var recording = new RecordingSettings(settings(EXECUTOR, "virtual", POOL_SIZE, "invalid",
                    QUEUE_CAPACITY, "-1"));

            var strategy = AsyncExecutorStrategy.from(recording.settings);
            try (var async = strategy.create(recording.settings)) {
                async.execute(() -> "done").join();

                assertSoftly(softly -> {
                    softly.assertThat(strategy).isEqualTo(AsyncExecutorStrategy.VIRTUAL);
                    softly.assertThat(recording.accessed(EXECUTOR)).as("executor").isTrue();
                    softly.assertThat(recording.accessed(POOL_SIZE)).as("pool size").isFalse();
                    softly.assertThat(recording.accessed(QUEUE_CAPACITY)).as("queue capacity").isFalse();
                    softly.assertThat(recording.accesses).as("bulk access")
                            .noneMatch(access -> access.startsWith("toMap"));
                });
            }
        }

        @Test
        @DisplayName("Should run more operations concurrently than the default platform pool could")
        void shouldRunConcurrently() throws Exception {
            int operations = 4 * Runtime.getRuntime().availableProcessors() + 8;
            var started = new CountDownLatch(operations);
            try (var async = AsyncExecutorStrategy.VIRTUAL.create(Settings.of(Map.of()))) {
                var futures = IntStream.range(0, operations).mapToObj(index -> async.execute(() -> {
                    started.countDown();
                    try {
                        return started.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                })).toList();
                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).get(15, TimeUnit.SECONDS);

                assertSoftly(softly -> softly.assertThat(futures).allSatisfy(future ->
                        softly.assertThat(future.join()).isTrue()));
            }
        }

        @Test
        @DisplayName("Should reject operations once closed")
        void shouldShutDownOnClose() {
            var async = AsyncExecutorStrategy.VIRTUAL.create(Settings.of(Map.of()));

            async.close();

            assertSoftly(softly -> softly.assertThat(isRejected(async.execute(() -> "late"))).isTrue());
        }
    }

    private static boolean isRejected(CompletableFuture<?> future) {
        try {
            future.join();
            return false;
        } catch (CompletionException e) {
            return e.getCause() instanceof RejectedExecutionException;
        }
    }
}
