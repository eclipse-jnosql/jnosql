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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@DisplayName("Dense vector")
class DenseVectorTest {

    @Test
    void shouldPreserveDimensionsAndComponentOrder() {
        DenseVector vector = DenseVector.of(0.12F, 0.45F, 0.78F);

        assertThat(vector).isInstanceOf(Vector.class);
        assertThat(vector.dimensions()).isEqualTo(3);
        assertThat(vector.values()).containsExactly(0.12F, 0.45F, 0.78F);
    }

    @Test
    void shouldCopyInput() {
        float[] values = {1F, 2F};
        DenseVector vector = DenseVector.of(values);
        int hashCode = vector.hashCode();
        values[0] = 99F;

        assertThat(vector.values()).containsExactly(1F, 2F);
        assertThat(vector.hashCode()).isEqualTo(hashCode);
    }

    @Test
    void shouldCopyOutput() {
        DenseVector vector = DenseVector.of(1F, 2F);
        float[] values = vector.values();
        values[0] = 99F;

        assertThat(vector.values()).isNotSameAs(values).containsExactly(1F, 2F);
        assertThat(vector.dimensions()).isEqualTo(2);
    }

    @Test
    void shouldRequireValues() {
        assertThatNullPointerException().isThrownBy(() -> DenseVector.of((float[]) null))
                .withMessage("values is required");
    }

    @Test
    void shouldRejectEmptyValues() {
        assertThatIllegalArgumentException().isThrownBy(DenseVector::of)
                .withMessageContaining("at least one dimension");
    }

    @ParameterizedTest
    @ValueSource(floats = {Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
    void shouldRejectNonFiniteComponents(float value) {
        assertThatIllegalArgumentException().isThrownBy(() -> DenseVector.of(1F, value, 2F))
                .withMessageContaining("index 1");
        assertThatIllegalArgumentException().isThrownBy(() -> DenseVector.of(value))
                .withMessageContaining("index 0");
    }

    @Test
    void shouldAcceptFiniteValuesWithoutNormalization() {
        float[] values = {0F, -0F, -1F, Float.MIN_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE};

        assertThat(DenseVector.of(values).values()).containsExactly(values);
        assertThat(DenseVector.of(0F).dimensions()).isEqualTo(1);
    }

    @Test
    void shouldCompareDefaultVectorsByComponents() {
        DenseVector vector = DenseVector.of(1F, 2F);
        DenseVector equal = DenseVector.of(1F, 2F);

        assertThat(vector).isEqualTo(vector).isEqualTo(equal);
        assertThat(equal).isEqualTo(vector);
        assertThat(vector.hashCode()).isEqualTo(equal.hashCode());
        assertThat(vector).isNotEqualTo(DenseVector.of(2F, 1F))
                .isNotEqualTo(DenseVector.of(1F))
                .isNotEqualTo(null)
                .isNotEqualTo(new float[]{1F, 2F});
    }

    @Test
    void shouldDescribeComponents() {
        assertThat(DenseVector.of(1F, 2F)).hasToString("DenseVector[1.0, 2.0]");
    }
}
