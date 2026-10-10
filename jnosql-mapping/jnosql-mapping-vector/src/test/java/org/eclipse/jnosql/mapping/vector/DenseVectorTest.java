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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayName("Dense vector")
class DenseVectorTest {

    @Nested
    @DisplayName("When creating a dense vector")
    class WhenTheCreation {

        @Test
        @DisplayName("Should preserve the dimension count and component order")
        void shouldPreserveComponents() {
            float[] values = {0.12F, 0.45F, 0.78F};

            DenseVector vector = DenseVector.of(values);

            assertSoftly(softly -> {
                softly.assertThat(vector).as("vector type").isInstanceOf(Vector.class);
                softly.assertThat(vector.dimensions()).as("dimension count").isEqualTo(3);
                softly.assertThat(vector.values()).as("ordered components").containsExactly(values);
            });
        }

        @Test
        @DisplayName("Should accept finite boundary values without normalization")
        void shouldPreserveFiniteValues() {
            float[] values = {0F, -0F, -1F, Float.MIN_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE};

            DenseVector vector = DenseVector.of(values);

            assertThat(vector.values()).as("unnormalized components").containsExactly(values);
        }

        @Test
        @DisplayName("Should accept a single zero component")
        void shouldAcceptSingleDimension() {
            DenseVector vector = DenseVector.of(0F);

            assertSoftly(softly -> {
                softly.assertThat(vector.dimensions()).as("minimum dimension count").isEqualTo(1);
                softly.assertThat(vector.values()).as("zero component").containsExactly(0F);
            });
        }
    }

    @Nested
    @DisplayName("When validating dense vector components")
    class WhenTheValidation {

        @Test
        @DisplayName("Should reject a null component array")
        void shouldRejectNullValues() {
            assertThatNullPointerException().isThrownBy(() -> DenseVector.of((float[]) null))
                    .withMessage("values is required");
        }

        @Test
        @DisplayName("Should reject an empty component array")
        void shouldRejectEmptyValues() {
            assertThatIllegalArgumentException().isThrownBy(DenseVector::of)
                    .withMessageContaining("at least one dimension");
        }

        @ParameterizedTest(name = "{0} at component {1}")
        @CsvSource({
                "NaN, 0", "NaN, 1", "NaN, 2",
                "Infinity, 0", "Infinity, 1", "Infinity, 2",
                "-Infinity, 0", "-Infinity, 1", "-Infinity, 2"
        })
        @DisplayName("Should reject non-finite components at any position")
        void shouldRejectNonFiniteComponents(float value, int index) {
            float[] values = {1F, 2F, 3F};
            values[index] = value;

            assertThatIllegalArgumentException().isThrownBy(() -> DenseVector.of(values))
                    .withMessageContaining("index " + index);
        }
    }

    @Nested
    @DisplayName("When modifying arrays supplied to or returned by a vector")
    class WhenTheArrayMutation {

        @Test
        @DisplayName("Should remain unchanged when the input array is modified")
        void shouldIsolateInput() {
            float[] values = {1F, 2F};
            DenseVector vector = DenseVector.of(values);
            int hashCode = vector.hashCode();

            values[0] = 99F;

            assertSoftly(softly -> {
                softly.assertThat(vector.values()).as("stored components").containsExactly(1F, 2F);
                softly.assertThat(vector.hashCode()).as("stable hash code").isEqualTo(hashCode);
            });
        }

        @Test
        @DisplayName("Should remain unchanged when a returned array is modified")
        void shouldIsolateOutput() {
            DenseVector vector = DenseVector.of(1F, 2F);
            float[] values = vector.values();

            values[0] = 99F;

            assertSoftly(softly -> {
                softly.assertThat(vector.values()).as("fresh component copy").isNotSameAs(values).containsExactly(1F, 2F);
                softly.assertThat(vector.dimensions()).as("unchanged dimension count").isEqualTo(2);
            });
        }
    }

    @Nested
    @DisplayName("When comparing dense vectors")
    class WhenTheComparison {

        @Test
        @DisplayName("Should treat equal components as equal values with matching hash codes")
        void shouldCompareEqualValues() {
            DenseVector vector = DenseVector.of(1F, 2F);
            DenseVector equal = DenseVector.of(1F, 2F);

            assertSoftly(softly -> {
                softly.assertThat(vector).as("reflexive and value equality").isEqualTo(vector).isEqualTo(equal);
                softly.assertThat(equal).as("symmetric equality").isEqualTo(vector);
                softly.assertThat(vector.hashCode()).as("equal-value hash code").isEqualTo(equal.hashCode());
            });
        }

        @Test
        @DisplayName("Should distinguish different component orders")
        void shouldDistinguishComponentOrder() {
            DenseVector vector = DenseVector.of(1F, 2F);
            DenseVector reversed = DenseVector.of(2F, 1F);

            assertThat(vector).as("component order affects equality").isNotEqualTo(reversed);
        }

        @Test
        @DisplayName("Should distinguish different dimension counts")
        void shouldDistinguishDimensions() {
            DenseVector vector = DenseVector.of(1F, 2F);
            DenseVector shorter = DenseVector.of(1F);

            assertThat(vector).as("dimension count affects equality").isNotEqualTo(shorter);
        }

        @Test
        @DisplayName("Should not equal null or a raw component array")
        void shouldDistinguishNonVectorValues() {
            DenseVector vector = DenseVector.of(1F, 2F);
            float[] values = {1F, 2F};

            assertSoftly(softly -> {
                softly.assertThat(vector).as("null is not a vector").isNotEqualTo(null);
                softly.assertThat(vector).as("raw arrays are not vectors").isNotEqualTo(values);
            });
        }
    }

    @Nested
    @DisplayName("When describing a dense vector")
    class WhenTheDescription {

        @Test
        @DisplayName("Should include the components in dimension order")
        void shouldDescribeComponents() {
            DenseVector vector = DenseVector.of(1F, 2F);

            String description = vector.toString();

            assertThat(description).as("vector description").isEqualTo("DenseVector[1.0, 2.0]");
        }
    }
}
