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

import org.eclipse.jnosql.communication.Settings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
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
import static org.junit.jupiter.api.Assertions.*;

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


    @Test
    void shouldReturnNPEWhenInstanceIsNull() {
        assertThrows(NullPointerException.class, () -> Settings.of((Map<String, Object>) null));

    }

    @Test
    void shouldReturnNewInstance() {
        Settings settings = MicroProfileSettings.INSTANCE;
        Assertions.assertNotNull(settings);
    }

    @Test
    void shouldCreateFromMap() {
        Settings settings = MicroProfileSettings.INSTANCE;
        assertFalse(settings.isEmpty());
    }

    @Test
    void shouldContainsKeys() {
        Settings settings = MicroProfileSettings.INSTANCE;
        assertTrue(settings.containsKey("jnosql.key"));
        assertFalse(settings.containsKey("key2"));
    }


    @Test
    void shouldGetKeys() {
        Settings settings = MicroProfileSettings.INSTANCE;
        assertThat(settings.keySet()).contains("jnosql.key");
    }


    @Test
    void shouldSize() {
        Settings settings = Settings.of(singletonMap("jnosql.key", "value"));
        assertFalse(settings.isEmpty());

    }

    @Test
    void shouldIsEmpty() {
        Settings settings = MicroProfileSettings.INSTANCE;
        assertFalse(settings.isEmpty());
    }

    @Test
    void shouldGet() {
        Settings settings = MicroProfileSettings.INSTANCE;
        Optional<Object> value = settings.get("jnosql.key-number");
        Assertions.assertNotNull(value);
        Assertions.assertEquals("12", value.get());
    }

    @Test
    void shouldGetSupplier() {
        Settings settings = MicroProfileSettings.INSTANCE;
        Optional<Object> value = settings.get(() -> "jnosql.key-number");
        Assertions.assertNotNull(value);
        Assertions.assertEquals("12", value.get());
    }

    @Test
    void shouldNPEGet() {
        Settings settings = MicroProfileSettings.INSTANCE;
        Assertions.assertThrows(NullPointerException.class, () -> settings.get((String) null));
        Assertions.assertThrows(NullPointerException.class, () -> settings.get((Supplier<String>) null));
    }

    @Test
    void shouldGetIterable() {
        Settings settings = MicroProfileSettings.INSTANCE;
        Optional<Object> value = settings.get(Collections.singleton("jnosql.key-number"));
        Assertions.assertNotNull(value);
        Assertions.assertEquals("12", value.get());
    }

    @Test
    void shouldGetIterableSupplier() {
        Settings settings = MicroProfileSettings.INSTANCE;
        Optional<Object> value = settings.getSupplier(Collections.singleton(() -> "jnosql.key-number"));
        Assertions.assertNotNull(value);
        Assertions.assertEquals("12", value.get());
    }

    @Test
    void shouldNPEGetIterable() {
        Settings settings = MicroProfileSettings.INSTANCE;
        Assertions.assertThrows(NullPointerException.class, () -> settings.get((Iterable<String>) null));
        Assertions.assertThrows(NullPointerException.class, () -> settings.getSupplier(null));
    }

    @Test
    void shouldGetValueClass() {
        Settings settings = MicroProfileSettings.INSTANCE;

        Integer value = settings.get("jnosql.key-number", Integer.class).get();
        assertEquals(Integer.valueOf(12), value);
        assertFalse(settings.get("jnosql.key2", Integer.class).isPresent());
    }

    @Test
    void shouldGetValueClassSupplier() {
        Settings settings = MicroProfileSettings.INSTANCE;

        Integer value = settings.get(() -> "jnosql.key-number", Integer.class).get();
        assertEquals(Integer.valueOf(12), value);
        assertFalse(settings.get(() -> "key2", Integer.class).isPresent());
    }

    @Test
    void shouldGetOrDefault() {
        Settings settings = MicroProfileSettings.INSTANCE;
        assertEquals("12", settings.getOrDefault("jnosql.key-number", "13"));
        assertEquals("13", settings.getOrDefault("key-1", "13"));
    }

    @Test
    void shouldGetOrDefaultSupplier() {
        Settings settings = MicroProfileSettings.INSTANCE;
        assertEquals("12", settings.getOrDefault(() -> "jnosql.key-number", "13"));
        assertEquals("13", settings.getOrDefault(() -> "key-1", "13"));
    }

    @Test
    void shouldReturnErrorWhenPrefixIsNull() {

        Settings settings = MicroProfileSettings.INSTANCE;

        assertThrows(NullPointerException.class, () -> settings.prefix((String) null));
    }

    @Test
    void shouldFindPrefix() {
        Settings settings = MicroProfileSettings.INSTANCE;

        List<Object> hosts = settings.prefix("jnosql.host");
        assertThat(hosts)
                .hasSize(4)
                .contains("host", "host-1", "host-2", "host-3");
    }

    @Test
    void shouldFindPrefixSupplier() {
        Settings settings = MicroProfileSettings.INSTANCE;

        List<Object> hosts = settings.prefix(() -> "jnosql.host");
        assertThat(hosts)
                .hasSize(4)
                .contains("host", "host-1", "host-2", "host-3");
    }

    @Test
    void shouldFindPrefixWithOrder() {
        Settings settings = MicroProfileSettings.INSTANCE;
        List<Object> hosts = settings.prefix("jnosql.host");
        assertThat(hosts).hasSize(4).contains("host", "host-1", "host-2", "host-3");
    }


    @Test
    void shouldReturnErrorWhenPrefixesIsNull() {
        Settings settings = MicroProfileSettings.INSTANCE;
        assertThrows(NullPointerException.class, () -> settings.prefix((Collection<String>) null));

    }

    @Test
    void shouldFindPrefixes() {

        Settings settings = MicroProfileSettings.INSTANCE;

        List<Object> hosts = settings.prefix(Arrays.asList("jnosql.host", "jnosql.server"));
        assertThat(hosts).hasSize(7).contains("host", "host-1", "server", "server-1");
    }

    @Test
    void shouldFindPrefixesSupplier() {

        Settings settings = MicroProfileSettings.INSTANCE;
        List<Object> hosts = settings.prefixSupplier(Arrays.asList(() -> "jnosql.host", () -> "jnosql.server"));
        assertThat(hosts).hasSize(7).contains("host", "host-1", "server", "server-1");
    }

    @Test
    void shouldFindPrefixesSort() {

        Settings settings = MicroProfileSettings.INSTANCE;

        List<Object> hosts = settings.prefix(Arrays.asList("jnosql.host", "jnosql.server"));
        assertThat(hosts).hasSize(7).contains("host", "host-1", "server", "server-1");
    }



}