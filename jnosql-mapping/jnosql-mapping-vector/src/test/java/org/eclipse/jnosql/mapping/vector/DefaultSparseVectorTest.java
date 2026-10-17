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

@DisplayName("DefaultSparseVector")
class DefaultSparseVectorTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create sparse vector")
        void shouldCreateSparseVector() {
            SparseVector vector = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f),
                    SparseVector.element(8, 3.0f)
            );

            assertThat(vector.dimensions()).isEqualTo(10);
        }

        @Test
        @DisplayName("should return indices")
        void shouldReturnIndices() {
            SparseVector vector = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f),
                    SparseVector.element(8, 3.0f)
            );

            assertThat(vector.indices()).containsExactly(2, 8);
        }

        @Test
        @DisplayName("should return values")
        void shouldReturnValues() {
            SparseVector vector = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f),
                    SparseVector.element(8, 3.0f)
            );

            assertThat(vector.values()).containsExactly(7.0f, 3.0f);
        }

        @Test
        @DisplayName("should create sparse vector without elements")
        void shouldCreateSparseVectorWithoutElements() {
            SparseVector vector = SparseVector.of(10);

            assertThat(vector.indices()).isEmpty();
        }

        @Test
        @DisplayName("should return empty values when no elements exist")
        void shouldReturnEmptyValuesWhenNoElementsExist() {
            SparseVector vector = SparseVector.of(10);

            assertThat(vector.values()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Dimensions validation")
    class DimensionsValidation {

        @Test
        @DisplayName("should reject zero dimensions")
        void shouldRejectZeroDimensions() {
            assertThatThrownBy(() -> SparseVector.of(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("dimensions must be greater than zero");
        }

        @Test
        @DisplayName("should reject negative dimensions")
        void shouldRejectNegativeDimensions() {
            assertThatThrownBy(() -> SparseVector.of(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("dimensions must be greater than zero");
        }
    }

    @Nested
    @DisplayName("Elements validation")
    class ElementsValidation {

        @Test
        @DisplayName("should reject null elements array")
        void shouldRejectNullElementsArray() {
            assertThatThrownBy(() -> SparseVector.of(10, (SparseVector.Element[]) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("elements must not be null");
        }

        @Test
        @DisplayName("should reject null element")
        void shouldRejectNullElement() {
            assertThatThrownBy(() -> SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f),
                    null
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("element must not be null");
        }

        @Test
        @DisplayName("should reject index equal to dimensions")
        void shouldRejectIndexEqualToDimensions() {
            SparseVector.Element element = element(10, 1.0f);

            assertThatThrownBy(() -> new DefaultSparseVector(10, element))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("index must be between 0 and 9");
        }

        @Test
        @DisplayName("should reject index greater than dimensions")
        void shouldRejectIndexGreaterThanDimensions() {
            SparseVector.Element element = element(11, 1.0f);

            assertThatThrownBy(() -> new DefaultSparseVector(10, element))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("index must be between 0 and 9");
        }

        @Test
        @DisplayName("should reject negative index")
        void shouldRejectNegativeIndex() {
            SparseVector.Element element = element(-1, 1.0f);

            assertThatThrownBy(() -> new DefaultSparseVector(10, element))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("index must be between 0 and 9");
        }

        @Test
        @DisplayName("should reject duplicated index")
        void shouldRejectDuplicatedIndex() {
            assertThatThrownBy(() -> SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f),
                    SparseVector.element(2, 3.0f)
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("indices must be unique and strictly increasing");
        }

        @Test
        @DisplayName("should reject descending indices")
        void shouldRejectDescendingIndices() {
            assertThatThrownBy(() -> SparseVector.of(
                    10,
                    SparseVector.element(8, 7.0f),
                    SparseVector.element(2, 3.0f)
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("indices must be unique and strictly increasing");
        }

        @Test
        @DisplayName("should reject NaN value")
        void shouldRejectNaNValue() {
            SparseVector.Element element = element(2, Float.NaN);

            assertThatThrownBy(() -> new DefaultSparseVector(10, element))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("value must be finite");
        }

        @Test
        @DisplayName("should reject positive infinity")
        void shouldRejectPositiveInfinity() {
            SparseVector.Element element = element(2, Float.POSITIVE_INFINITY);

            assertThatThrownBy(() -> new DefaultSparseVector(10, element))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("value must be finite");
        }

        @Test
        @DisplayName("should reject negative infinity")
        void shouldRejectNegativeInfinity() {
            SparseVector.Element element = element(2, Float.NEGATIVE_INFINITY);

            assertThatThrownBy(() -> new DefaultSparseVector(10, element))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("value must be finite");
        }

        @Test
        @DisplayName("should reject zero value")
        void shouldRejectZeroValue() {
            SparseVector.Element element = element(2, 0.0f);

            assertThatThrownBy(() -> new DefaultSparseVector(10, element))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("sparse vector elements must have a non-zero value");
        }

        @Test
        @DisplayName("should reject negative zero value")
        void shouldRejectNegativeZeroValue() {
            SparseVector.Element element = element(2, -0.0f);

            assertThatThrownBy(() -> new DefaultSparseVector(10, element))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("sparse vector elements must have a non-zero value");
        }

        private SparseVector.Element element(int index, float value) {
            return new SparseVector.Element() {
                @Override
                public int index() {
                    return index;
                }

                @Override
                public float value() {
                    return value;
                }
            };
        }
    }

    @Nested
    @DisplayName("Immutability")
    class Immutability {

        @Test
        @DisplayName("should return a defensive copy of indices")
        void shouldReturnDefensiveCopyOfIndices() {
            SparseVector vector = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f),
                    SparseVector.element(8, 3.0f)
            );

            int[] indices = vector.indices();
            indices[0] = 5;

            assertThat(vector.indices()).containsExactly(2, 8);
        }

        @Test
        @DisplayName("should return a defensive copy of values")
        void shouldReturnDefensiveCopyOfValues() {
            SparseVector vector = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f),
                    SparseVector.element(8, 3.0f)
            );

            float[] values = vector.values();
            values[0] = 9.0f;

            assertThat(vector.values()).containsExactly(7.0f, 3.0f);
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToItself() {
            SparseVector vector = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f)
            );

            assertThat(vector).isEqualTo(vector);
        }

        @Test
        @DisplayName("should be equal when dimensions indices and values are equal")
        void shouldBeEqualWhenContentIsEqual() {
            SparseVector first = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f),
                    SparseVector.element(8, 3.0f)
            );

            SparseVector second = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f),
                    SparseVector.element(8, 3.0f)
            );

            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("should not be equal when dimensions differ")
        void shouldNotBeEqualWhenDimensionsDiffer() {
            SparseVector first = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f)
            );

            SparseVector second = SparseVector.of(
                    11,
                    SparseVector.element(2, 7.0f)
            );

            assertThat(first).isNotEqualTo(second);
        }

        @Test
        @DisplayName("should not be equal when indices differ")
        void shouldNotBeEqualWhenIndicesDiffer() {
            SparseVector first = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f)
            );

            SparseVector second = SparseVector.of(
                    10,
                    SparseVector.element(3, 7.0f)
            );

            assertThat(first).isNotEqualTo(second);
        }

        @Test
        @DisplayName("should not be equal when values differ")
        void shouldNotBeEqualWhenValuesDiffer() {
            SparseVector first = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f)
            );

            SparseVector second = SparseVector.of(
                    10,
                    SparseVector.element(2, 8.0f)
            );

            assertThat(first).isNotEqualTo(second);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            SparseVector vector = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f)
            );

            assertThat(vector).isNotEqualTo(null);
        }

        @Test
        @DisplayName("should not be equal to another type")
        void shouldNotBeEqualToAnotherType() {
            SparseVector vector = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f)
            );

            assertThat(vector).isNotEqualTo("vector");
        }

        @Test
        @DisplayName("should have same hash code when equal")
        void shouldHaveSameHashCodeWhenEqual() {
            SparseVector first = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f),
                    SparseVector.element(8, 3.0f)
            );

            SparseVector second = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f),
                    SparseVector.element(8, 3.0f)
            );

            assertThat(first.hashCode()).isEqualTo(second.hashCode());
        }

        @Test
        @DisplayName("should have different hash code when dimensions differ")
        void shouldHaveDifferentHashCodeWhenDimensionsDiffer() {
            SparseVector first = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f)
            );

            SparseVector second = SparseVector.of(
                    11,
                    SparseVector.element(2, 7.0f)
            );

            assertThat(first.hashCode()).isNotEqualTo(second.hashCode());
        }
    }

    @Nested
    @DisplayName("String representation")
    class StringRepresentation {

        @Test
        @DisplayName("should represent sparse vector as string")
        void shouldRepresentSparseVectorAsString() {
            SparseVector vector = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f),
                    SparseVector.element(8, 3.0f)
            );

            assertThat(vector.toString())
                    .isEqualTo("SparseVector{dimensions=10, indices=[2, 8], values=[7.0, 3.0]}");
        }
    }
}