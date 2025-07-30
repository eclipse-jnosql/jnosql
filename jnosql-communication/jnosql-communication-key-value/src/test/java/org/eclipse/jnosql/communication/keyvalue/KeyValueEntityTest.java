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
package org.eclipse.jnosql.communication.keyvalue;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class KeyValueEntityTest {

    public static final String KEY = "key";
    public static final String VALUE = "VALUE";

    @Test
    @DisplayName("Should throw NullPointerException when key is null")
    void shouldReturnErrorWhenKeyIsNull() {
        assertThatNullPointerException().isThrownBy(() -> KeyValueEntity.of(null, VALUE)).withMessage("key is required");
    }

    @Test
    @DisplayName("Should throw NullPointerException when value is null")
    void shouldReturnErrorWhenValueIsNull() {
        assertThatNullPointerException().isThrownBy(() -> KeyValueEntity.of(KEY, null)).withMessage("value is required");
    }

    @Test
    @DisplayName("Should be able to create a KeyValueEntity")
    void shouldCreateInstance() {
        KeyValueEntity entity = KeyValueEntity.of(KEY, VALUE);

        assertSoftly(softly -> {
            softly.assertThat(entity.key()).as("key is required").isEqualTo(KEY);
            softly.assertThat(entity.value()).as("value is required").isEqualTo(VALUE);
        });
    }

    @Test
    @DisplayName("Should be able to get the value")
    void shouldGetValue() {
        Value value = Value.of(VALUE);
        KeyValueEntity entity = KeyValueEntity.of(KEY, value);

        assertThat(entity.value()).isEqualTo(value.get());
    }

    @Test
    @DisplayName("Should be able to get the key by class")
    void shouldGetKeyClass() {
        Value value = Value.of(VALUE);
        KeyValueEntity entity = KeyValueEntity.of("10", value);

        assertThat(entity.key(Long.class)).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should throw NullPointerException when Class is null")
    void shouldReturnErrorWhenGetKeyClassIsNull() {
        Value value = Value.of(VALUE);
        KeyValueEntity entity = KeyValueEntity.of("10", value);

        assertThatNullPointerException().isThrownBy(() -> entity.key((Class<Object>) null)).withMessage("type is required");
    }

    @Test
    @DisplayName("Should be able to get the key by TypeSupplier")
    void shouldGetKeyValueSupplier() {
        String value = "10";
        KeyValueEntity entity = KeyValueEntity.of(value, value);

        assertThat(entity.value(new TypeReference<List<Integer>>() {
        })).isEqualTo(singletonList(10));
    }

    @Test
    @DisplayName("Should throw NullPointerException when TypeSupplier is null")
    void shouldReturnErrorWhenGetKeySupplierIsNull() {
        Value value = Value.of("value");
        KeyValueEntity entity = KeyValueEntity.of("10", value);

        assertThatNullPointerException().isThrownBy(() -> entity.key((TypeReference<Object>) null)).withMessage("supplier is required");
    }

    @Test
    void shouldToString() {
        KeyValueEntity entity = KeyValueEntity.of(KEY, VALUE);
        String expected = "KeyValueEntity{key=key, value=VALUE}";

        assertThat(entity.toString()).isNotNull();
    }

    @Test
    void shouldHashCodeAndEquals() {
        KeyValueEntity entity1 = KeyValueEntity.of(KEY, VALUE);
        KeyValueEntity entity2 = KeyValueEntity.of(KEY, VALUE);
        KeyValueEntity entity3 = KeyValueEntity.of("anotherKey", "anotherValue");

        assertThat(entity1).isEqualTo(entity1);
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1).isNotEqualTo(entity3);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
        assertThat(entity1.hashCode()).isNotEqualTo(entity3.hashCode());
    }

    @Test
    void shouldValue() {
        KeyValueEntity entity1 = KeyValueEntity.of(Value.of(KEY), VALUE);
        var value = entity1.value(String.class);

        Assertions.assertThat(value).isEqualTo(VALUE);

    }
}
