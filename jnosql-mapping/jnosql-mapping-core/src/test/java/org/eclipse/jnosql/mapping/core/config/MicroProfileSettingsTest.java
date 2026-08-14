/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.core.config;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.eclipse.jnosql.communication.Settings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class MicroProfileSettingsTest {
    @AfterAll
    public static void  afterAll() {
        System.clearProperty("jnosql.jnosql.key");
        System.clearProperty("jnosql.jnosql.host");
        System.clearProperty("key.jnosql");
        System.clearProperty("jnosql.key");
        System.clearProperty("jnosql.key-number");
        System.clearProperty("jnosql.host");
        System.clearProperty("jnosql.host.1");
        System.clearProperty("jnosql.host.2");
        System.clearProperty("jnosql.host.3");
        System.clearProperty("jnosql.server");
        System.clearProperty("jnosql.server.1");
        System.clearProperty("jnosql.server.2");
    }

    @BeforeAll
    public static void  beforeAll() {
        System.setProperty("jnosql.jnosql.key", "value");
        System.setProperty("jnosql.jnosql.host", "host");
        System.setProperty("key.jnosql", "value");
        System.setProperty("jnosql.key", "value");
        System.setProperty("jnosql.key-number", "12");
        System.setProperty("jnosql.host", "host");
        System.setProperty("jnosql.host.1",  "host-1");
        System.setProperty("jnosql.host.2",  "host-2");
        System.setProperty("jnosql.host.3",  "host-3");
        System.setProperty("jnosql.server", "server");
        System.setProperty("jnosql.server.1", "server-1");
        System.setProperty("jnosql.server.2", "server-2");
    }

































    @Nested
    @DisplayName("When the micro profile settings operates")
    class WhenTheMicroProfileSettingsOperates {

        @DisplayName("Should return npewhen instance is null")
        @Test
        void shouldReturnNPEWhenInstanceIsNull() {
            assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> Settings.of((Map<String, Object>) null));

        }
        @DisplayName("Should return new instance")
        @Test
        void shouldReturnNewInstance() {
            Settings settings = MicroProfileSettings.INSTANCE;
            assertThat(settings).isNotNull();
        }
        @DisplayName("Should create from map")
        @Test
        void shouldCreateFromMap() {
            Settings settings = MicroProfileSettings.INSTANCE;
            assertThat(settings.isEmpty()).isFalse();
        }
        @DisplayName("Should contains keys")
        @Test
        void shouldContainsKeys() {
            Settings settings = MicroProfileSettings.INSTANCE;
            assertThat(settings.containsKey("jnosql.key")).isTrue();
            assertThat(settings.containsKey("key2")).isFalse();
        }
        @DisplayName("Should get keys")
        @Test
        void shouldGetKeys() {
            Settings settings = MicroProfileSettings.INSTANCE;
            assertThat(settings.keySet()).contains("jnosql.key");
        }
        @DisplayName("Should size")
        @Test
        void shouldSize() {
            Settings settings = Settings.of(singletonMap("jnosql.key", "value"));
            assertThat(settings.isEmpty()).isFalse();

        }
        @DisplayName("Should is empty")
        @Test
        void shouldIsEmpty() {
            Settings settings = MicroProfileSettings.INSTANCE;
            assertThat(settings.isEmpty()).isFalse();
        }
        @DisplayName("Should get")
        @Test
        void shouldGet() {
            Settings settings = MicroProfileSettings.INSTANCE;
            Optional<Object> value = settings.get("jnosql.key-number");
            assertThat(value).isNotNull();
            assertThat(value.get()).isEqualTo("12");
        }
        @DisplayName("Should get supplier")
        @Test
        void shouldGetSupplier() {
            Settings settings = MicroProfileSettings.INSTANCE;
            Optional<Object> value = settings.get(() -> "jnosql.key-number");
            assertThat(value).isNotNull();
            assertThat(value.get()).isEqualTo("12");
        }
        @DisplayName("Should npeget")
        @Test
        void shouldNPEGet() {
            Settings settings = MicroProfileSettings.INSTANCE;
            assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> settings.get((String) null));
            assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> settings.get((Supplier<String>) null));
        }
        @DisplayName("Should get iterable")
        @Test
        void shouldGetIterable() {
            Settings settings = MicroProfileSettings.INSTANCE;
            Optional<Object> value = settings.get(Collections.singleton("jnosql.key-number"));
            assertThat(value).isNotNull();
            assertThat(value.get()).isEqualTo("12");
        }
        @DisplayName("Should get iterable supplier")
        @Test
        void shouldGetIterableSupplier() {
            Settings settings = MicroProfileSettings.INSTANCE;
            Optional<Object> value = settings.getSupplier(Collections.singleton(() -> "jnosql.key-number"));
            assertThat(value).isNotNull();
            assertThat(value.get()).isEqualTo("12");
        }
        @DisplayName("Should npeget iterable")
        @Test
        void shouldNPEGetIterable() {
            Settings settings = MicroProfileSettings.INSTANCE;
            assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> settings.get((Iterable<String>) null));
            assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> settings.getSupplier(null));
        }
        @DisplayName("Should get value class")
        @Test
        void shouldGetValueClass() {
            Settings settings = MicroProfileSettings.INSTANCE;

            Integer value = settings.get("jnosql.key-number", Integer.class).get();
            assertThat(value).isEqualTo(Integer.valueOf(12));
            assertThat(settings.get("jnosql.key2", Integer.class).isPresent()).isFalse();
        }
        @DisplayName("Should get value class supplier")
        @Test
        void shouldGetValueClassSupplier() {
            Settings settings = MicroProfileSettings.INSTANCE;

            Integer value = settings.get(() -> "jnosql.key-number", Integer.class).get();
            assertThat(value).isEqualTo(Integer.valueOf(12));
            assertThat(settings.get(() -> "key2", Integer.class).isPresent()).isFalse();
        }
        @DisplayName("Should get or default")
        @Test
        void shouldGetOrDefault() {
            Settings settings = MicroProfileSettings.INSTANCE;
            assertThat(settings.getOrDefault("jnosql.key-number", "13")).isEqualTo("12");
            assertThat(settings.getOrDefault("key-1", "13")).isEqualTo("13");
        }
        @DisplayName("Should get or default supplier")
        @Test
        void shouldGetOrDefaultSupplier() {
            Settings settings = MicroProfileSettings.INSTANCE;
            assertThat(settings.getOrDefault(() -> "jnosql.key-number", "13")).isEqualTo("12");
            assertThat(settings.getOrDefault(() -> "key-1", "13")).isEqualTo("13");
        }
        @DisplayName("Should return error when prefix is null")
        @Test
        void shouldReturnErrorWhenPrefixIsNull() {

            Settings settings = MicroProfileSettings.INSTANCE;

            assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> settings.prefix((String) null));
        }
        @DisplayName("Should find prefix")
        @Test
        void shouldFindPrefix() {
            Settings settings = MicroProfileSettings.INSTANCE;

            List<Object> hosts = settings.prefix("jnosql.host");
            assertThat(hosts)
                    .hasSize(4)
                    .contains("host", "host-1", "host-2", "host-3");
        }
        @DisplayName("Should find prefix supplier")
        @Test
        void shouldFindPrefixSupplier() {
            Settings settings = MicroProfileSettings.INSTANCE;

            List<Object> hosts = settings.prefix(() -> "jnosql.host");
            assertThat(hosts)
                    .hasSize(4)
                    .contains("host", "host-1", "host-2", "host-3");
        }
        @DisplayName("Should find prefix with order")
        @Test
        void shouldFindPrefixWithOrder() {
            Settings settings = MicroProfileSettings.INSTANCE;
            List<Object> hosts = settings.prefix("jnosql.host");
            assertThat(hosts).hasSize(4).contains("host", "host-1", "host-2", "host-3");
        }
        @DisplayName("Should return error when prefixes is null")
        @Test
        void shouldReturnErrorWhenPrefixesIsNull() {
            Settings settings = MicroProfileSettings.INSTANCE;
            assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> settings.prefix((Collection<String>) null));

        }
        @DisplayName("Should find prefixes")
        @Test
        void shouldFindPrefixes() {

            Settings settings = MicroProfileSettings.INSTANCE;

            List<Object> hosts = settings.prefix(Arrays.asList("jnosql.host", "jnosql.server"));
            assertThat(hosts).hasSize(7).contains("host", "host-1", "server", "server-1");
        }
        @DisplayName("Should find prefixes supplier")
        @Test
        void shouldFindPrefixesSupplier() {

            Settings settings = MicroProfileSettings.INSTANCE;
            List<Object> hosts = settings.prefixSupplier(Arrays.asList(() -> "jnosql.host", () -> "jnosql.server"));
            assertThat(hosts).hasSize(7).contains("host", "host-1", "server", "server-1");
        }
        @DisplayName("Should find prefixes sort")
        @Test
        void shouldFindPrefixesSort() {

            Settings settings = MicroProfileSettings.INSTANCE;

            List<Object> hosts = settings.prefix(Arrays.asList("jnosql.host", "jnosql.server"));
            assertThat(hosts).hasSize(7).contains("host", "host-1", "server", "server-1");
        }
    }
}
