/*
 *
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
 *   Elias Nogueira
 *
 */
package org.eclipse.jnosql.communication;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class DefaultSettingsTest {

    private static final String KEY = "key";
    private static final String VALUE = "12";
    private static final Settings COMMON_SETTINGS = Settings.builder().put("host", "host").put("host.1", "host-1")
            .put("host.2", "host-2").put("host.3", "host-3").build();

    @Test
    @DisplayName("Should throw NullPointerException when Settings is null")
    void shouldReturnNPEWhenInstanceIsNull() {
        assertThatNullPointerException().isThrownBy(() -> Settings.of((Map<String, Object>) null));
    }

    @Test
    @DisplayName("Should return new instance of DefaultSettings")
    void shouldReturnNewInstance() {
        Settings settings = Settings.of();

        assertSoftly(softly -> {
            assertThat(settings).as("Is instance of DefaultSettings").isInstanceOf(DefaultSettings.class);
            assertThat(settings.isEmpty()).as("Have empty settings").isTrue();
        });
    }

    @Test
    @DisplayName("Should be able to create the settings by a Map")
    void shouldCreateFromMap() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));
        assertThat(settings).isNotNull();
    }

    @Test
    @DisplayName("Should be able retrieve the settings as a Map")
    void shouldRetrieveAsMap() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));
        Map<String, Object> map = settings.toMap();

        assertThat(map).containsKey(KEY).containsValue(VALUE);
    }

    @Test
    @DisplayName("Should contains the specified key")
    void shouldContainsKeys() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));
        assertThat(settings.containsKey(KEY)).isTrue();
    }

    @Test
    @DisplayName("Should be able to retrieve the key")
    void shouldGetKeys() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));
        assertThat(settings.keySet()).contains(KEY);
    }

    @Test
    @DisplayName("Should have a size equals to 1")
    void shouldSize() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));
        assertThat(settings.keySet()).hasSize(1);
    }

    @Test
    @DisplayName("Should have a size equals to 1")
    void shouldBeEmpty() {
        Settings settings = new DefaultSettings(Collections.emptyMap());
        assertThat(settings.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Should have a valid not empty list")
    void shouldIsEmpty() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));
        assertThat(settings.keySet()).isNotEmpty();
    }

    @Test
    @DisplayName("Should be able to get the value")
    void shouldGet() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));
        Optional<Object> value = settings.get(KEY);

        assertThat(value).isPresent().get().isEqualTo(VALUE);
    }

    @Test
    @DisplayName("Should be able to get supplier")
    void shouldGetSupplier() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));
        assertThat(settings.get(() -> KEY)).isPresent().get().isEqualTo(VALUE);
    }

    @Test
    @DisplayName("Should throw NullPointerException when there's no key")
    void shouldNPEGet() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));

        assertSoftly(softly -> {
            softly.assertThatNullPointerException().isThrownBy(() -> settings.get((String) null)).withMessage("key is required");
            softly.assertThatNullPointerException().isThrownBy(() -> settings.get((Supplier<String>) null)).withMessage("supplier is required");
        });
    }

    @Test
    @DisplayName("Should be able to get Iterable")
    void shouldGetIterable() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));
        Optional<Object> value = settings.get(Collections.singleton(KEY));

        assertThat(value).get().isEqualTo(VALUE);
    }

    @Test
    @DisplayName("Should be able to get Iterable Supplier")
    void shouldGetIterableSupplier() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));
        Optional<Object> value = settings.getSupplier(Collections.singleton(() -> "key"));

        assertThat(value).isPresent().get().isEqualTo(VALUE);
    }

    @Test
    @DisplayName("Should throw NullPointerException when there's no key using Iterable")
    void shouldNPEGetIterable() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));

        assertSoftly(softly -> {
            softly.assertThatNullPointerException().isThrownBy(() -> settings.get((Iterable<String>) null)).withMessage("keys is required");
            softly.assertThatNullPointerException().isThrownBy(() -> settings.getSupplier(null)).withMessage("supplier is required");
        });
    }

    @Test
    @DisplayName("Should get value class")
    void shouldGetValueClass() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));
        assertThat(settings.get(KEY, Integer.class)).isPresent().get().isEqualTo(12);
    }

    @Test
    @DisplayName("Should throw NullPointerException when Class(type) is null")
    void shouldThrowNullPointerWhenClassIsNull() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));
        assertThatNullPointerException().isThrownBy(() -> settings.get(KEY, null)).withMessage("type is required");
    }

    @Test
    @DisplayName("Should get value class using supplier")
    void shouldGetValueClassSupplier() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));
        assertThat(settings.get(() -> "key", Integer.class)).isPresent().get().isEqualTo(12);
    }

    @Test
    @DisplayName("Should get default value")
    void shouldGetDefault() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));
        assertThat(settings.getOrDefault("key-1", "13")).as("Should get default value").isEqualTo("13");
    }

    @Test
    @DisplayName("Should get original value")
    void shouldGetOriginal() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));
        assertThat(settings.getOrDefault(KEY, "13")).as("Should get original value").isEqualTo("12");
    }

    @Test
    @DisplayName("Should throw NullPointerException when default value is null")
    void shouldThrowNPEWhenDefaultIsNull() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));
        assertThatNullPointerException().isThrownBy(() -> settings.getOrDefault(KEY, null)).withMessage("defaultValue is required");
    }

    @Test
    @DisplayName("Should get default value")
    void shouldGetDefaultSupplier() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));
        assertThat(settings.getOrDefault(() -> "key-1", "13")).as("Should get default value").isEqualTo("13");
    }

    @Test
    @DisplayName("Should get original value")
    void shouldGetOriginalSupplier() {
        Settings settings = Settings.of(singletonMap(KEY, VALUE));
        assertThat(settings.getOrDefault(() -> KEY, "13")).as("Should get original value").isEqualTo("12");
    }

    @Test
    @DisplayName("Should throw NullPointerException when prefix is null")
    void shouldReturnErrorWhenPrefixIsNull() {
        assertThatNullPointerException().isThrownBy(() -> COMMON_SETTINGS.prefix((String) null)).withMessage("prefix is required");
    }

    @Test
    @DisplayName("Should find prefix")
    void shouldFindPrefix() {
        List<Object> hosts = COMMON_SETTINGS.prefix("host");
        assertThat(hosts).hasSize(4).contains("host", "host-3", "host-2", "host-1");
    }

    @Test
    @DisplayName("Should return empty list when prefix is not found")
    void shouldReturnEmptyListWhenPrefixIsNotFound() {
        Settings settings = Settings.builder()
                .put("host.1", "host-1")
                .put("host.2", "host-2")
                .build();

        List<Object> hosts = settings.prefix("address");
        assertThat(hosts).isEmpty();
    }

    @Test
    @DisplayName("Should find prefix using Supplier")
    void shouldFindPrefixSupplier() {
        List<Object> hosts = COMMON_SETTINGS.prefix(() -> "host");
        assertThat(hosts).hasSize(4).contains("host-3", "host-1", "host-2", "host");
    }

    @Test
    @DisplayName("Should find and retrieve prefix in the correct order")
    void shouldFindPrefixWithOrder() {
        Settings settings = Settings.builder()
                .put("host", "host")
                .put("host.3", "host-3")
                .put("host.2", "host-2")
                .put("host.1", "host-1")
                .build();

        List<Object> hosts = settings.prefix("host");
        assertThat(hosts).hasSize(4).containsExactly("host", "host-1", "host-2", "host-3");
    }

    @Test
    @DisplayName("Should throw NullPointerException when prefixes using Supplier is null")
    void shouldReturnErrorWhenPrefixesIsNull() {
        assertThatNullPointerException().isThrownBy(() -> COMMON_SETTINGS.prefix((Collection<String>) null)).withMessage("prefixes is required");
    }

    @Test
    @DisplayName("Should find prefixes")
    void shouldFindPrefixes() {
        Settings settings = Settings.builder()
                .put("host", "host")
                .put("host.1", "host-1")
                .put("server", "server")
                .put("server.1", "server-1")
                .build();

        List<Object> hosts = settings.prefix(singletonList("server"));
        assertThat(hosts).hasSize(2).contains("server", "server-1");
    }

    @Test
    @DisplayName("Should find prefixes using Supplier")
    void shouldFindPrefixesSupplier() {
        Settings settings = Settings.builder()
                .put("host", "host")
                .put("host.1", "host-1")
                .put("server", "server")
                .put("server.1", "server-1")
                .build();

        List<Object> hosts = settings.prefixSupplier(Arrays.asList(() -> "host", () -> "server"));
        assertThat(hosts).hasSize(4).contains("host", "host-1", "server", "server-1");
    }

    @Test
    @DisplayName("Should find and retrieve prefixed sorted")
    void shouldFindPrefixesSort() {
        Settings settings = Settings.builder()
                .put("host.99", "host-99")
                .put("server.99", "server-99")
                .put("host", "host")
                .put("server", "server")
                .build();

        List<Object> hosts = settings.prefix(Arrays.asList("host", "server"));
        assertThat(hosts).hasSize(4).containsExactly("host", "host-99", "server", "server-99");
    }

    @Test
    void shouldSizeBehavior() {
        Settings settings = Settings.builder()
                .put("host", "host")
                .put("host.1", "host-1")
                .put("server", "server")
                .put("server.1", "server-1")
                .build();
    assertThat(settings.size()).isEqualTo(4);
    }

    @Test
    void shouldEquals(){
        Settings settings = Settings.builder()
                .put("host", "host")
                .put("host.1", "host-1")
                .put("server", "server")
                .put("server.1", "server-1")
                .build();

        Settings settings2 = Settings.builder()
                .put("host", "host")
                .put("host.1", "host-1")
                .put("server", "server")
                .put("server.1", "server-1")
                .build();

        Settings settings3 = Settings.builder()
                .put("host", "host")
                .put("server.1", "server-1")
                .build();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(settings).isEqualTo(settings2);
            softly.assertThat(settings).isEqualTo(settings);
            softly.assertThat(settings).isNotEqualTo(null);
            softly.assertThat(settings).isNotEqualTo("234");
            softly.assertThat(settings).isNotEqualTo(settings3);
        });

    }

    @Test
    void shouldHashCode(){
        Settings settings = Settings.builder()
                .put("host", "host")
                .put("host.1", "host-1")
                .put("server", "server")
                .put("server.1", "server-1")
                .build();

        assertThat(settings.hashCode()).isEqualTo(settings.hashCode());
    }

    @Test
    void shouldToString(){
        var settings = Settings.builder()
                .put("host", "host")
                .put("host.1", "host-1")
                .put("server", "server")
                .put("server.1", "server-1")
                .build();

        assertThat(settings.toString()).isNotNull().isNotEmpty();
    }
}
