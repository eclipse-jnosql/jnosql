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
 * A nonempty, ordered sequence of finite floating-point components.
 * The number of dimensions equals the number of components.
 * <p>
 * No normalization or distance metric is implied. Database-specific dimension and
 * metric requirements are the provider's responsibility.
 * </p>
 */
public interface DenseVector extends Vector {

    /**
     * Returns the number of components.
     *
     * @return the positive dimension count
     */
    int dimensions();

    /**
     * Returns the components in dimension order.
     * Changing the returned array must not change this vector.
     *
     * @return a copy of the components
     */
    float[] values();

    /**
     * Creates an immutable dense vector by copying the supplied components.
     * For example, {@code DenseVector.of(0.12F, 0.45F, 0.78F)} has three dimensions.
     *
     * @param values the components in dimension order
     * @return an immutable dense vector
     * @throws NullPointerException when values is {@code null}
     * @throws IllegalArgumentException when values is empty or contains NaN or infinity
     */
    static DenseVector of(float... values) {
        return new DefaultDenseVector(values);
    }
}
