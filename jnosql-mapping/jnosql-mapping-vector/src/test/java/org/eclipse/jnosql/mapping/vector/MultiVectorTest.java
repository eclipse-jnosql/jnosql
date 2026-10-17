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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MultiVector")
class MultiVectorTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("should create multi vector")
        void shouldCreateMultiVector() {
            MultiVector vector = MultiVector.of(
                    DenseVector.of(0.2f, 0.8f, 0.1f),
                    DenseVector.of(0.7f, 0.1f, 0.4f)
            );

            assertThat(vector.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("should preserve dimensions")
        void shouldPreserveDimensions() {
            MultiVector vector = MultiVector.of(
                    DenseVector.of(0.2f, 0.8f, 0.1f),
                    DenseVector.of(0.7f, 0.1f, 0.4f)
            );

            assertThat(vector.dimensions()).isEqualTo(3);
        }

        @Test
        @DisplayName("should preserve vectors")
        void shouldPreserveVectors() {
            DenseVector first = DenseVector.of(0.2f, 0.8f, 0.1f);
            DenseVector second = DenseVector.of(0.7f, 0.1f, 0.4f);

            MultiVector vector = MultiVector.of(first, second);

            assertThat(vector.vectors()).containsExactly(first, second);
        }

        @Test
        @DisplayName("should create multi vector with one vector")
        void shouldCreateMultiVectorWithOneVector() {
            MultiVector vector = MultiVector.of(
                    DenseVector.of(0.2f, 0.8f, 0.1f)
            );

            assertThat(vector.size()).isOne();
        }

        @Test
        @DisplayName("should implement Vector")
        void shouldImplementVector() {
            Vector vector = MultiVector.of(
                    DenseVector.of(0.2f, 0.8f, 0.1f)
            );

            assertThat(vector).isInstanceOf(MultiVector.class);
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("should reject null vectors")
        void shouldRejectNullVectors() {
            assertThatThrownBy(() -> MultiVector.of((DenseVector[]) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("vectors must not be null");
        }

        @Test
        @DisplayName("should reject empty vectors")
        void shouldRejectEmptyVectors() {
            assertThatThrownBy(MultiVector::of)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("vectors must not be empty");
        }

        @Test
        @DisplayName("should reject null first vector")
        void shouldRejectNullFirstVector() {
            assertThatThrownBy(() -> MultiVector.of(
                    null,
                    DenseVector.of(0.7f, 0.1f, 0.4f)
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("vector must not be null");
        }

        @Test
        @DisplayName("should reject null vector after first")
        void shouldRejectNullVectorAfterFirst() {
            assertThatThrownBy(() -> MultiVector.of(
                    DenseVector.of(0.2f, 0.8f, 0.1f),
                    null
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("vector must not be null");
        }

        @Test
        @DisplayName("should reject vectors with different dimensions")
        void shouldRejectVectorsWithDifferentDimensions() {
            assertThatThrownBy(() -> MultiVector.of(
                    DenseVector.of(0.2f, 0.8f, 0.1f),
                    DenseVector.of(0.7f, 0.1f)
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("all vectors must have the same number of dimensions");
        }
    }

    @Nested
    @DisplayName("contract")
    class Contract {

        @Test
        @DisplayName("should create equal multi vectors")
        void shouldCreateEqualMultiVectors() {
            MultiVector first = MultiVector.of(
                    DenseVector.of(0.2f, 0.8f, 0.1f),
                    DenseVector.of(0.7f, 0.1f, 0.4f)
            );

            MultiVector second = MultiVector.of(
                    DenseVector.of(0.2f, 0.8f, 0.1f),
                    DenseVector.of(0.7f, 0.1f, 0.4f)
            );

            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("should preserve vector order")
        void shouldPreserveVectorOrder() {
            DenseVector first = DenseVector.of(0.2f, 0.8f, 0.1f);
            DenseVector second = DenseVector.of(0.7f, 0.1f, 0.4f);

            MultiVector vector = MultiVector.of(first, second);

            assertThat(vector.vectors()).containsExactly(first, second);
        }
    }

    @Nested
    @DisplayName("immutability")
    class Immutability {

        @Test
        @DisplayName("should return immutable vectors")
        void shouldReturnImmutableVectors() {
            MultiVector vector = MultiVector.of(
                    DenseVector.of(0.2f, 0.8f, 0.1f)
            );

            List<DenseVector> vectors = vector.vectors();

            assertThatThrownBy(() -> vectors.add(DenseVector.of(0.4f, 0.5f, 0.6f)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}