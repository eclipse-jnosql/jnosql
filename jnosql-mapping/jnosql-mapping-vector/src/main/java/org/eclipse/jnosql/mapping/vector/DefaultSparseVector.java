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

final class DefaultSparseVector implements SparseVector {

    private final int dimensions;
    private final int[] indices;
    private final float[] values;

    DefaultSparseVector(int dimensions, SparseVector.Element... elements) {
        if (dimensions <= 0) {
            throw new IllegalArgumentException("dimensions must be greater than zero");
        }

        Objects.requireNonNull(elements, "elements must not be null");

        this.dimensions = dimensions;
        this.indices = new int[elements.length];
        this.values = new float[elements.length];

        int previousIndex = -1;

        for (int index = 0; index < elements.length; index++) {
            SparseVector.Element element =
                    Objects.requireNonNull(elements[index], "element must not be null");

            int elementIndex = element.index();
            float elementValue = element.value();

            if (elementIndex < 0 || elementIndex >= dimensions) {
                throw new IllegalArgumentException(
                        "index must be between 0 and " + (dimensions - 1));
            }

            if (elementIndex <= previousIndex) {
                throw new IllegalArgumentException(
                        "indices must be unique and strictly increasing");
            }

            if (!Float.isFinite(elementValue)) {
                throw new IllegalArgumentException("value must be finite");
            }

            if (elementValue == 0.0f) {
                throw new IllegalArgumentException(
                        "sparse vector elements must have a non-zero value");
            }

            indices[index] = elementIndex;
            values[index] = elementValue;
            previousIndex = elementIndex;
        }
    }

    @Override
    public int dimensions() {
        return dimensions;
    }

    @Override
    public int[] indices() {
        return indices.clone();
    }

    @Override
    public float[] values() {
        return values.clone();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof SparseVector other)) {
            return false;
        }

        return dimensions == other.dimensions()
                && Arrays.equals(indices, other.indices())
                && Arrays.equals(values, other.values());
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(dimensions);
        result = 31 * result + Arrays.hashCode(indices);
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }

    @Override
    public String toString() {
        return "SparseVector{"
                + "dimensions=" + dimensions
                + ", indices=" + Arrays.toString(indices)
                + ", values=" + Arrays.toString(values)
                + '}';
    }
}