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

import java.util.Arrays;
import java.util.Objects;

final class DefaultDenseVector implements DenseVector {

    private final float[] values;

    DefaultDenseVector(float[] values) {
        Objects.requireNonNull(values, "values is required");
        if (values.length == 0) {
            throw new IllegalArgumentException("A dense vector must have at least one dimension");
        }
        this.values = values.clone();
        for (int index = 0; index < this.values.length; index++) {
            if (!Float.isFinite(this.values[index])) {
                throw new IllegalArgumentException("The vector component at index " + index + " must be finite");
            }
        }
    }

    @Override
    public int dimensions() {
        return values.length;
    }

    @Override
    public float[] values() {
        return values.clone();
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof DefaultDenseVector vector && Arrays.equals(values, vector.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public String toString() {
        return "DenseVector" + Arrays.toString(values);
    }
}
