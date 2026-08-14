/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.metadata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultFieldValueTest {

    @Nested
    @DisplayName("When the field value is created")
    class WhenTheFieldValueIsCreated {

        @Test
        @DisplayName("Should expose the value and field metadata")
        void shouldExposeTheValueAndFieldMetadata() {
            FieldMetadata fieldMetadata = mock(FieldMetadata.class);
            when(fieldMetadata.name()).thenReturn("fieldName");

            Object value = "testValue";

            DefaultFieldValue fieldValue = new DefaultFieldValue(value, fieldMetadata);

            assertSoftly(softly -> {
                softly.assertThat(fieldValue.value()).isEqualTo(value);
                softly.assertThat(fieldValue.field()).isEqualTo(fieldMetadata);
            });
        }

        @Test
        @DisplayName("Should reject null field metadata")
        void shouldRejectNullFieldMetadata() {
            Object value = "testValue";

            assertThatNullPointerException().isThrownBy(() -> new DefaultFieldValue(value, null));
        }
    }

    @Nested
    @DisplayName("When the field value is checked for content")
    class WhenTheFieldValueIsCheckedForContent {

        @Test
        @DisplayName("Should be not empty when value is present")
        void shouldBeNotEmptyWhenValueIsPresent() {
            FieldMetadata fieldMetadata = mock(FieldMetadata.class);
            when(fieldMetadata.name()).thenReturn("fieldName");
            DefaultFieldValue fieldValue = new DefaultFieldValue("testValue", fieldMetadata);

            assertThat(fieldValue.isNotEmpty()).isTrue();
        }

        @Test
        @DisplayName("Should be empty when value is null")
        void shouldBeEmptyWhenValueIsNull() {
            FieldMetadata fieldMetadata = mock(FieldMetadata.class);
            when(fieldMetadata.name()).thenReturn("fieldName");
            DefaultFieldValue fieldValue = new DefaultFieldValue(null, fieldMetadata);

            assertThat(fieldValue.isNotEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("When the field value is represented as text")
    class WhenTheFieldValueIsRepresentedAsText {

        @Test
        @DisplayName("Should return non-blank text")
        void shouldReturnNonBlankText() {
            FieldMetadata fieldMetadata = mock(FieldMetadata.class);
            when(fieldMetadata.name()).thenReturn("fieldName");
            DefaultFieldValue fieldValue = new DefaultFieldValue("testValue", fieldMetadata);

            assertThat(fieldValue.toString()).isNotEmpty().isNotBlank().isNotNull();
        }
    }
}