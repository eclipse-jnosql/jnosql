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

/**
 * Represents a non-empty, ordered sequence of finite floating-point values.
 *
 * <p>
 * The number of dimensions is equal to the number of values in the vector.
 * No normalization or similarity metric is implied by this type.
 * Database-specific dimension and metric requirements are defined by the provider.
 * </p>
 */
public interface DenseVector extends Vector {

    /**
     * Returns the number of dimensions of this vector.
     *
     * @return the positive number of dimensions
     */
    int dimensions();

    /**
     * Returns the values of this vector in dimension order.
     *
     * <p>
     * Modifying the returned array does not modify this vector.
     * </p>
     *
     * @return a copy of the vector values
     */
    float[] values();

    /**
     * Creates an immutable dense vector from the supplied values.
     *
     * <pre>{@code
     * DenseVector vector = DenseVector.of(
     *         0.12F,
     *         0.45F,
     *         0.78F
     * );
     * }</pre>
     *
     * @param values the values in dimension order
     * @return an immutable dense vector
     * @throws NullPointerException if {@code values} is {@code null}
     * @throws IllegalArgumentException if {@code values} is empty or contains
     *         {@link Float#NaN}, positive infinity, or negative infinity
     */
    static DenseVector of(float... values) {
        return new DefaultDenseVector(values);
    }
}
