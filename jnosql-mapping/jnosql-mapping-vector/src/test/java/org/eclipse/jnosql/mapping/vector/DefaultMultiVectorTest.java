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

@DisplayName("DefaultMultiVector")
class DefaultMultiVectorTest {

    @Nested
    @DisplayName("creation")
    class Creation {

        @Test
        @DisplayName("should return size")
        void shouldReturnSize() {
            DefaultMultiVector vector = new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f, 0.1f),
                    DenseVector.of(0.7f, 0.1f, 0.4f),
                    DenseVector.of(0.3f, 0.6f, 0.9f)
            );

            assertThat(vector.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("should return dimensions")
        void shouldReturnDimensions() {
            DefaultMultiVector vector = new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f, 0.1f),
                    DenseVector.of(0.7f, 0.1f, 0.4f)
            );

            assertThat(vector.dimensions()).isEqualTo(3);
        }

        @Test
        @DisplayName("should return vectors")
        void shouldReturnVectors() {
            DenseVector first = DenseVector.of(0.2f, 0.8f, 0.1f);
            DenseVector second = DenseVector.of(0.7f, 0.1f, 0.4f);

            DefaultMultiVector vector = new DefaultMultiVector(first, second);

            assertThat(vector.vectors()).containsExactly(first, second);
        }

        @Test
        @DisplayName("should support a single vector")
        void shouldSupportSingleVector() {
            DefaultMultiVector vector = new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f, 0.1f)
            );

            assertThat(vector.size()).isOne();
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("should reject null array")
        void shouldRejectNullArray() {
            assertThatThrownBy(() -> new DefaultMultiVector((DenseVector[]) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("vectors must not be null");
        }

        @Test
        @DisplayName("should reject empty array")
        void shouldRejectEmptyArray() {
            assertThatThrownBy(DefaultMultiVector::new)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("vectors must not be empty");
        }

        @Test
        @DisplayName("should reject null first vector")
        void shouldRejectNullFirstVector() {
            assertThatThrownBy(() -> new DefaultMultiVector(
                    null,
                    DenseVector.of(0.7f, 0.1f, 0.4f)
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("vector must not be null");
        }

        @Test
        @DisplayName("should reject null middle vector")
        void shouldRejectNullMiddleVector() {
            assertThatThrownBy(() -> new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f, 0.1f),
                    null,
                    DenseVector.of(0.7f, 0.1f, 0.4f)
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("vector must not be null");
        }

        @Test
        @DisplayName("should reject vector with fewer dimensions")
        void shouldRejectVectorWithFewerDimensions() {
            assertThatThrownBy(() -> new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f, 0.1f),
                    DenseVector.of(0.7f, 0.1f)
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("all vectors must have the same number of dimensions");
        }

        @Test
        @DisplayName("should reject vector with more dimensions")
        void shouldRejectVectorWithMoreDimensions() {
            assertThatThrownBy(() -> new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f),
                    DenseVector.of(0.7f, 0.1f, 0.4f)
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("all vectors must have the same number of dimensions");
        }
    }

    @Nested
    @DisplayName("immutability")
    class Immutability {

        @Test
        @DisplayName("should not be affected when source array changes")
        void shouldNotBeAffectedWhenSourceArrayChanges() {
            DenseVector first = DenseVector.of(0.2f, 0.8f, 0.1f);
            DenseVector second = DenseVector.of(0.7f, 0.1f, 0.4f);
            DenseVector[] source = {first, second};

            DefaultMultiVector vector = new DefaultMultiVector(source);

            source[0] = DenseVector.of(9.0f, 9.0f, 9.0f);

            assertThat(vector.vectors()).containsExactly(first, second);
        }

        @Test
        @DisplayName("should return immutable list")
        void shouldReturnImmutableList() {
            DefaultMultiVector vector = new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f, 0.1f)
            );

            List<DenseVector> vectors = vector.vectors();

            assertThatThrownBy(() -> vectors.add(DenseVector.of(0.4f, 0.5f, 0.6f)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("equality")
    class Equality {

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToItself() {
            DefaultMultiVector vector = new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f, 0.1f)
            );

            assertThat(vector).isEqualTo(vector);
        }

        @Test
        @DisplayName("should be equal when vectors and dimensions are equal")
        void shouldBeEqualWhenVectorsAndDimensionsAreEqual() {
            DefaultMultiVector first = new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f, 0.1f),
                    DenseVector.of(0.7f, 0.1f, 0.4f)
            );

            DefaultMultiVector second = new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f, 0.1f),
                    DenseVector.of(0.7f, 0.1f, 0.4f)
            );

            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("should not be equal when vector value differs")
        void shouldNotBeEqualWhenVectorValueDiffers() {
            DefaultMultiVector first = new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f, 0.1f),
                    DenseVector.of(0.7f, 0.1f, 0.4f)
            );

            DefaultMultiVector second = new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f, 0.1f),
                    DenseVector.of(0.7f, 0.1f, 0.5f)
            );

            assertThat(first).isNotEqualTo(second);
        }

        @Test
        @DisplayName("should not be equal when vector order differs")
        void shouldNotBeEqualWhenVectorOrderDiffers() {
            DenseVector firstVector = DenseVector.of(0.2f, 0.8f, 0.1f);
            DenseVector secondVector = DenseVector.of(0.7f, 0.1f, 0.4f);

            DefaultMultiVector first = new DefaultMultiVector(firstVector, secondVector);
            DefaultMultiVector second = new DefaultMultiVector(secondVector, firstVector);

            assertThat(first).isNotEqualTo(second);
        }

        @Test
        @DisplayName("should not be equal when number of vectors differs")
        void shouldNotBeEqualWhenNumberOfVectorsDiffers() {
            DefaultMultiVector first = new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f, 0.1f)
            );

            DefaultMultiVector second = new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f, 0.1f),
                    DenseVector.of(0.7f, 0.1f, 0.4f)
            );

            assertThat(first).isNotEqualTo(second);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            DefaultMultiVector vector = new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f, 0.1f)
            );

            assertThat(vector).isNotEqualTo(null);
        }

        @Test
        @DisplayName("should not be equal to another type")
        void shouldNotBeEqualToAnotherType() {
            DefaultMultiVector vector = new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f, 0.1f)
            );

            assertThat(vector).isNotEqualTo("vector");
        }

        @Test
        @DisplayName("should have same hash code when equal")
        void shouldHaveSameHashCodeWhenEqual() {
            DefaultMultiVector first = new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f, 0.1f),
                    DenseVector.of(0.7f, 0.1f, 0.4f)
            );

            DefaultMultiVector second = new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f, 0.1f),
                    DenseVector.of(0.7f, 0.1f, 0.4f)
            );

            assertThat(first.hashCode()).isEqualTo(second.hashCode());
        }
    }

    @Nested
    @DisplayName("string representation")
    class StringRepresentation {

        @Test
        @DisplayName("should include dimensions")
        void shouldIncludeDimensions() {
            DefaultMultiVector vector = new DefaultMultiVector(
                    DenseVector.of(0.2f, 0.8f, 0.1f)
            );

            assertThat(vector.toString()).contains("dimensions=3");
        }

        @Test
        @DisplayName("should include vectors")
        void shouldIncludeVectors() {
            DenseVector dense = DenseVector.of(0.2f, 0.8f, 0.1f);
            DefaultMultiVector vector = new DefaultMultiVector(dense);

            assertThat(vector.toString()).contains(dense.toString());
        }
    }
}