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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class SettingsBuilderTest {

    @Test
    @DisplayName("Should throw NullPointerException when key is null")
    void shouldReturnErrorWhenKeyIsNUll() {
        assertSoftly(softly -> {
            assertThatNullPointerException().as("key is required")
                    .isThrownBy(() -> Settings.builder().put((String) null, "value")).withMessage("key is required");

            assertThatNullPointerException().as("supplier is required")
                    .isThrownBy(() -> Settings.builder().put((Supplier<String>) null, "value")).withMessage("supplier is required");
        });
    }

    @Test
    @DisplayName("Should return empty DefaultSettings when settings() is called")
    void shouldReturnEmptyDefaultSettings() {
        Settings settings = Settings.settings();

        assertSoftly(softly -> {
            softly.assertThat(settings).as("Is instance of DefaultSettings").isInstanceOf(DefaultSettings.class);
            softly.assertThat(settings.keySet()).as("Has expected size").hasSize(0);
        });
    }

    @Test
    @DisplayName("Should throw NullPointerException when key and value are null")
    void shouldReturnErrorWhenValueIsNUll() {
        assertSoftly(softly -> {
            softly.assertThatNullPointerException().as("value is required")
                    .isThrownBy(() -> Settings.builder().put("key", null)).withMessage("value is required");

            softly.assertThatNullPointerException().as("value is required")
                    .isThrownBy(() -> Settings.builder().put(() -> "key", null)).withMessage("value is required");
        });
    }

    @Test
    @DisplayName("Should throw NullPointerException when key is null")
    void shouldReturnErrorWhenMapHasNullKey() {
        assertThatNullPointerException().isThrownBy(() -> {
            Map<String, Object> map = Collections.singletonMap(null, "value");
            Settings.builder().putAll(map);
        }).withMessage("key is required");
    }

    @Test
    @DisplayName("Should throw NullPointerException when value is null")
    void shouldReturnErrorWhenMapHasNullValue() {
        assertThatNullPointerException().isThrownBy(() -> {
            Map<String, Object> map = Collections.singletonMap("key", null);
            Settings.builder().putAll(map);
        }).withMessage("value is required");
    }

    @Test
    @DisplayName("Should be able to create the settings using builder")
    void shouldCreateSettingsBuilder() {
        Settings settings = Settings.builder().put("key", "value").build();

        assertSoftly(softly -> {
            softly.assertThat(settings).as("Settings are not null").isNotNull();
            softly.assertThat(settings.get("key"))
                    .as("Can retrieve the settings value").isPresent().get().isEqualTo("value");
        });
    }

    @Test
    @DisplayName("Should be able to create settings using builder with supplier")
    void shouldCreateSettingsBuilderWithSupplier() {
        Settings settings = Settings.builder().put(() -> "key", "value").build();

        assertSoftly(softly -> {
            softly.assertThat(settings).as("Settings are not null").isNotNull();
            softly.assertThat(settings.get("key"))
                    .as("Can retrieve the settings value").isPresent().get().isEqualTo("value");
        });
    }

    @Test
    void shouldToString() {
        Settings settings = Settings.builder().put("key", "value").build();
        String expected = "DefaultSettings{configurations={key=value}}";
        assertSoftly(softly -> softly.assertThat(settings.toString()).isEqualTo(expected));
    }

    @Test
    void shouldEquals() {
        Settings settings = Settings.builder().put("key", "value").build();
        Settings other = Settings.builder().put("key", "value").build();
        assertSoftly(softly -> {
            softly.assertThat(settings).isEqualTo(other);
            softly.assertThat(settings).isEqualTo(settings);
            softly.assertThat(settings).isNotEqualTo(null);
            softly.assertThat(settings).isNotEqualTo("String");
        });
    }

    @Test
    void shouldHashCode() {
        Settings settings = Settings.builder().put("key", "value").build();
        assertSoftly(softly -> softly.assertThat(settings.hashCode()).isEqualTo(settings.hashCode()));
    }
}

