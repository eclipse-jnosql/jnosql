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

@DisplayName("SparseVector")
class SparseVectorTest {

    @Nested
    @DisplayName("of")
    class Of {

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
        @DisplayName("should preserve indices")
        void shouldPreserveIndices() {
            SparseVector vector = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f),
                    SparseVector.element(8, 3.0f)
            );

            assertThat(vector.indices()).containsExactly(2, 8);
        }

        @Test
        @DisplayName("should preserve values")
        void shouldPreserveValues() {
            SparseVector vector = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f),
                    SparseVector.element(8, 3.0f)
            );

            assertThat(vector.values()).containsExactly(7.0f, 3.0f);
        }

        @Test
        @DisplayName("should create vector without explicit elements")
        void shouldCreateVectorWithoutExplicitElements() {
            SparseVector vector = SparseVector.of(10);

            assertThat(vector.indices()).isEmpty();
        }

        @Test
        @DisplayName("should reject invalid dimensions")
        void shouldRejectInvalidDimensions() {
            assertThatThrownBy(() -> SparseVector.of(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("dimensions must be greater than zero");
        }

        @Test
        @DisplayName("should reject duplicated indices")
        void shouldRejectDuplicatedIndices() {
            assertThatThrownBy(() -> SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f),
                    SparseVector.element(2, 3.0f)
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("indices must be unique and strictly increasing");
        }

        @Test
        @DisplayName("should reject unordered indices")
        void shouldRejectUnorderedIndices() {
            assertThatThrownBy(() -> SparseVector.of(
                    10,
                    SparseVector.element(8, 3.0f),
                    SparseVector.element(2, 7.0f)
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("indices must be unique and strictly increasing");
        }

        @Test
        @DisplayName("should reject index outside dimensions")
        void shouldRejectIndexOutsideDimensions() {
            assertThatThrownBy(() -> SparseVector.of(
                    10,
                    SparseVector.element(10, 7.0f)
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("index must be between 0 and 9");
        }
    }

    @Nested
    @DisplayName("element")
    class ElementFactory {

        @Test
        @DisplayName("should create element")
        void shouldCreateElement() {
            SparseVector.Element element = SparseVector.element(2, 7.0f);

            assertThat(element.index()).isEqualTo(2);
        }

        @Test
        @DisplayName("should preserve element value")
        void shouldPreserveElementValue() {
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
        @DisplayName("should reject negative index")
        void shouldRejectNegativeIndex() {
            assertThatThrownBy(() -> SparseVector.element(-1, 7.0f))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("index must not be negative");
        }

        @Test
        @DisplayName("should reject zero value")
        void shouldRejectZeroValue() {
            assertThatThrownBy(() -> SparseVector.element(2, 0.0f))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("sparse vector element value must be non-zero");
        }

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
        @DisplayName("should accept negative finite value")
        void shouldAcceptNegativeFiniteValue() {
            SparseVector.Element element = SparseVector.element(2, -7.0f);

            assertThat(element.value()).isEqualTo(-7.0f);
        }
    }

    @Nested
    @DisplayName("representation")
    class Representation {

        @Test
        @DisplayName("should represent dense equivalent as sparse vector")
        void shouldRepresentDenseEquivalentAsSparseVector() {
            SparseVector vector = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f),
                    SparseVector.element(8, 3.0f)
            );

            assertThat(vector.indices()).containsExactly(2, 8);
        }

        @Test
        @DisplayName("should preserve values of dense equivalent")
        void shouldPreserveValuesOfDenseEquivalent() {
            SparseVector vector = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f),
                    SparseVector.element(8, 3.0f)
            );

            assertThat(vector.values()).containsExactly(7.0f, 3.0f);
        }

        @Test
        @DisplayName("should preserve full dimension of dense equivalent")
        void shouldPreserveFullDimensionOfDenseEquivalent() {
            SparseVector vector = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f),
                    SparseVector.element(8, 3.0f)
            );

            assertThat(vector.dimensions()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("contract")
    class Contract {

        @Test
        @DisplayName("should implement Vector")
        void shouldImplementVector() {
            Vector vector = SparseVector.of(
                    10,
                    SparseVector.element(2, 7.0f)
            );

            assertThat(vector).isInstanceOf(SparseVector.class);
        }

        @Test
        @DisplayName("should create equal vectors from the same sparse representation")
        void shouldCreateEqualVectorsFromSameRepresentation() {
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
        @DisplayName("should create equal elements")
        void shouldCreateEqualElements() {
            SparseVector.Element first = SparseVector.element(2, 7.0f);
            SparseVector.Element second = SparseVector.element(2, 7.0f);

            assertThat(first).isEqualTo(second);
        }
    }
}