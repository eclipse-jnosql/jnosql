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
import java.util.List;
import java.util.Objects;

final class DefaultMultiVector implements MultiVector {

    private final List<DenseVector> vectors;
    private final int dimensions;

    DefaultMultiVector(DenseVector... vectors) {
        Objects.requireNonNull(vectors, "vectors must not be null");

        if (vectors.length == 0) {
            throw new IllegalArgumentException("vectors must not be empty");
        }

        DenseVector first = Objects.requireNonNull(
                vectors[0],
                "vector must not be null"
        );

        int expectedDimensions = first.dimensions();

        this.vectors = Arrays.stream(vectors)
                .map(vector -> validate(vector, expectedDimensions))
                .toList();

        this.dimensions = expectedDimensions;
    }

    private DenseVector validate(DenseVector vector, int expectedDimensions) {
        DenseVector current = Objects.requireNonNull(
                vector,
                "vector must not be null"
        );

        if (current.dimensions() != expectedDimensions) {
            throw new IllegalArgumentException(
                    "all vectors must have the same number of dimensions"
            );
        }

        return current;
    }

    @Override
    public int size() {
        return vectors.size();
    }

    @Override
    public int dimensions() {
        return dimensions;
    }

    @Override
    public List<DenseVector> vectors() {
        return List.copyOf(vectors);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof MultiVector other)) {
            return false;
        }

        return dimensions == other.dimensions()
                && vectors.equals(other.vectors());
    }

    @Override
    public int hashCode() {
        return Objects.hash(vectors, dimensions);
    }

    @Override
    public String toString() {
        return "MultiVector{"
                + "vectors=" + vectors
                + ", dimensions=" + dimensions
                + '}';
    }
}