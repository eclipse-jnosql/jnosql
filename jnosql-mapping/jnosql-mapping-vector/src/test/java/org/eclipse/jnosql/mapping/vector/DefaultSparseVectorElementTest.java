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
package org.eclipse.jnosql.mapping.vector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DefaultSparseVectorElement")
class DefaultSparseVectorElementTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create element")
        void shouldCreateElement() {
            SparseVector.Element element = SparseVector.element(2, 7.0f);

            assertThat(element.index()).isEqualTo(2);
        }

        @Test
        @DisplayName("should return value")
        void shouldReturnValue() {
            SparseVector.Element element = SparseVector.element(2, 7.0f);

            assertThat(element.value()).isEqualTo(7.0f);
        }

        @Test
        @DisplayName("should accept zero index")
        void shouldAcceptZeroIndex() {
            SparseVector.Element element = SparseVector.element(0, 7.0f);

            assertThat(element.index()).isZero();
        }

        @Test
        @DisplayName("should accept maximum integer index")
        void shouldAcceptMaximumIntegerIndex() {
            SparseVector.Element element =
                    SparseVector.element(Integer.MAX_VALUE, 7.0f);

            assertThat(element.index()).isEqualTo(Integer.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("Index validation")
    class IndexValidation {

        @Test
        @DisplayName("should reject negative index")
        void shouldRejectNegativeIndex() {
            assertThatThrownBy(() -> SparseVector.element(-1, 7.0f))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("index must not be negative");
        }
    }

    @Nested
    @DisplayName("Value validation")
    class ValueValidation {

        @Test
        @DisplayName("should reject NaN")
        void shouldRejectNaN() {
            assertThatThrownBy(() -> SparseVector.element(2, Float.NaN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("value must be finite");
        }

        @Test
        @DisplayName("should reject positive infinity")
        void shouldRejectPositiveInfinity() {
            assertThatThrownBy(() ->
                    SparseVector.element(2, Float.POSITIVE_INFINITY))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("value must be finite");
        }

        @Test
        @DisplayName("should reject negative infinity")
        void shouldRejectNegativeInfinity() {
            assertThatThrownBy(() ->
                    SparseVector.element(2, Float.NEGATIVE_INFINITY))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("value must be finite");
        }

        @Test
        @DisplayName("should reject zero")
        void shouldRejectZero() {
            assertThatThrownBy(() -> SparseVector.element(2, 0.0f))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("sparse vector element value must be non-zero");
        }

        @Test
        @DisplayName("should reject negative zero")
        void shouldRejectNegativeZero() {
            assertThatThrownBy(() -> SparseVector.element(2, -0.0f))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("sparse vector element value must be non-zero");
        }

        @Test
        @DisplayName("should accept smallest positive value")
        void shouldAcceptSmallestPositiveValue() {
            SparseVector.Element element =
                    SparseVector.element(2, Float.MIN_VALUE);

            assertThat(element.value()).isEqualTo(Float.MIN_VALUE);
        }

        @Test
        @DisplayName("should accept negative finite value")
        void shouldAcceptNegativeFiniteValue() {
            SparseVector.Element element =
                    SparseVector.element(2, -7.0f);

            assertThat(element.value()).isEqualTo(-7.0f);
        }

        @Test
        @DisplayName("should accept maximum finite value")
        void shouldAcceptMaximumFiniteValue() {
            SparseVector.Element element =
                    SparseVector.element(2, Float.MAX_VALUE);

            assertThat(element.value()).isEqualTo(Float.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal when index and value are equal")
        void shouldBeEqualWhenIndexAndValueAreEqual() {
            SparseVector.Element first = SparseVector.element(2, 7.0f);
            SparseVector.Element second = SparseVector.element(2, 7.0f);

            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("should not be equal when index differs")
        void shouldNotBeEqualWhenIndexDiffers() {
            SparseVector.Element first = SparseVector.element(2, 7.0f);
            SparseVector.Element second = SparseVector.element(3, 7.0f);

            assertThat(first).isNotEqualTo(second);
        }

        @Test
        @DisplayName("should not be equal when value differs")
        void shouldNotBeEqualWhenValueDiffers() {
            SparseVector.Element first = SparseVector.element(2, 7.0f);
            SparseVector.Element second = SparseVector.element(2, 8.0f);

            assertThat(first).isNotEqualTo(second);
        }

        @Test
        @DisplayName("should have same hash code when equal")
        void shouldHaveSameHashCodeWhenEqual() {
            SparseVector.Element first = SparseVector.element(2, 7.0f);
            SparseVector.Element second = SparseVector.element(2, 7.0f);

            assertThat(first.hashCode()).isEqualTo(second.hashCode());
        }
    }

    @Nested
    @DisplayName("String representation")
    class StringRepresentation {

        @Test
        @DisplayName("should represent element as string")
        void shouldRepresentElementAsString() {
            SparseVector.Element element = SparseVector.element(2, 7.0f);

            assertThat(element.toString())
                    .isEqualTo("DefaultSparseVectorElement[index=2, value=7.0]");
        }
    }
}