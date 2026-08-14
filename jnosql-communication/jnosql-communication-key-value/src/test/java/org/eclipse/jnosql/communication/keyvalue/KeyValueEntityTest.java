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

import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayName("Key-value entity")
class KeyValueEntityTest {

    public static final String KEY = "key";
    public static final String VALUE = "VALUE";

    @Nested
    @DisplayName("When creating an entity")
    class WhenTheCreation {

        @Test
        @DisplayName("Should require a key")
        void shouldRequireKey() {

            // When / Then
            assertThatNullPointerException()
                    .isThrownBy(() -> KeyValueEntity.of(null, VALUE))
                    .withMessage("key is required");
        }

        @Test
        @DisplayName("Should require a value")
        void shouldRequireValue() {

            // When / Then
            assertThatNullPointerException()
                    .isThrownBy(() -> KeyValueEntity.of(KEY, null))
                    .withMessage("value is required");
        }

        @Test
        @DisplayName("Should create an entity with key and value")
        void shouldCreateEntityWithKeyAndValue() {

            // When
            KeyValueEntity entity = KeyValueEntity.of(KEY, VALUE);

            // Then
            assertSoftly(softly -> {
                softly.assertThat(entity.key()).as("key is required").isEqualTo(KEY);
                softly.assertThat(entity.value()).as("value is required").isEqualTo(VALUE);
            });
        }

        @Test
        @DisplayName("Should unwrap a value used as the key")
        void shouldUnwrapValueUsedAsKey() {

            // When
            KeyValueEntity entity = KeyValueEntity.of(Value.of(KEY), VALUE);

            // Then
            assertThat(entity.key()).isEqualTo(KEY);
        }
    }

    @Nested
    @DisplayName("When reading the value")
    class WhenTheValueReading {

        @Test
        @DisplayName("Should return the raw value")
        void shouldReturnRawValue() {

            // Given
            Value value = Value.of(VALUE);
            KeyValueEntity entity = KeyValueEntity.of(KEY, value);

            // When
            Object result = entity.value();

            // Then
            assertThat(result).isEqualTo(value.get());
        }

        @Test
        @DisplayName("Should return the value as a class")
        void shouldReturnValueAsClass() {

            // Given
            KeyValueEntity entity = KeyValueEntity.of(KEY, VALUE);

            // When
            var result = entity.value(String.class);

            // Then
            assertThat(result).isEqualTo(VALUE);
        }

        @Test
        @DisplayName("Should return the value as a type reference")
        void shouldReturnValueAsTypeReference() {

            // Given
            String value = "10";
            KeyValueEntity entity = KeyValueEntity.of(value, value);

            // When
            var result = entity.value(new TypeReference<List<Integer>>() {
            });

            // Then
            assertThat(result).isEqualTo(singletonList(10));
        }
    }

    @Nested
    @DisplayName("When converting the key")
    class WhenTheKeyConversion {

        @Test
        @DisplayName("Should return the key as a class")
        void shouldReturnKeyAsClass() {

            // Given
            Value value = Value.of(VALUE);
            KeyValueEntity entity = KeyValueEntity.of("10", value);

            // When
            var result = entity.key(Long.class);

            // Then
            assertThat(result).isEqualTo(10L);
        }

        @Test
        @DisplayName("Should require a class")
        void shouldRequireClass() {

            // Given
            Value value = Value.of(VALUE);
            KeyValueEntity entity = KeyValueEntity.of("10", value);

            // When / Then
            assertThatNullPointerException()
                    .isThrownBy(() -> entity.key((Class<Object>) null))
                    .withMessage("type is required");
        }

        @Test
        @DisplayName("Should require a type reference")
        void shouldRequireTypeReference() {

            // Given
            Value value = Value.of("value");
            KeyValueEntity entity = KeyValueEntity.of("10", value);

            // When / Then
            assertThatNullPointerException()
                    .isThrownBy(() -> entity.key((TypeReference<Object>) null))
                    .withMessage("supplier is required");
        }
    }

    @Nested
    @DisplayName("When comparing entities")
    class WhenTheComparison {

        @Test
        @DisplayName("Should compare entities by key and value")
        void shouldCompareByKeyAndValue() {

            // Given
            KeyValueEntity entity1 = KeyValueEntity.of(KEY, VALUE);
            KeyValueEntity entity2 = KeyValueEntity.of(KEY, VALUE);
            KeyValueEntity entity3 = KeyValueEntity.of("anotherKey", "anotherValue");

            // Then
            assertSoftly(softly -> {
                softly.assertThat(entity1).isEqualTo(entity1);
                softly.assertThat(entity1).isEqualTo(entity2);
                softly.assertThat(entity1).isNotEqualTo(entity3);
                softly.assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
                softly.assertThat(entity1.hashCode()).isNotEqualTo(entity3.hashCode());
            });
        }
    }

    @Nested
    @DisplayName("When formatting an entity")
    class WhenTheFormatting {

        @Test
        @DisplayName("Should include the key and value")
        void shouldIncludeKeyAndValue() {

            // Given
            KeyValueEntity entity = KeyValueEntity.of(KEY, VALUE);

            // When
            String result = entity.toString();

            // Then
            assertThat(result).isEqualTo("DefaultKeyValueEntity{key=key, value=DefaultValue[value=VALUE]}");
        }
    }
}
