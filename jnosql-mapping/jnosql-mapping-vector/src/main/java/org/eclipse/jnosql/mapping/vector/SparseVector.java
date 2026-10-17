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
 * Represents a sparse vector, where only the non-zero values and their positions are stored.
 * <p>
 * A dense vector such as:
 * </p>
 *
 * <pre>
 * [0, 0, 7, 0, 0, 0, 0, 0, 3, 0]
 * </pre>
 *
 * <p>
 * can be represented as a sparse vector with:
 * </p>
 *
 * <pre>
 * dimensions: 10
 * indices:    [2, 8]
 * values:     [7, 3]
 * </pre>
 *
 * <p>
 * The vector still has {@code 10} dimensions, but only the values at indices
 * {@code 2} and {@code 8} are stored explicitly. All other positions are
 * implicitly {@code 0}.
 * </p>
 *
 * <p>
 * In Java, the same sparse vector can be created as:
 * </p>
 *
 * <pre>{@code
 * SparseVector vector = SparseVector.of(
 *         10,
 *         SparseVector.element(2, 7.0f),
 *         SparseVector.element(8, 3.0f)
 * );
 * }</pre>
 *
 * <p>
 * Sparse vectors are useful when most dimensions contain zero values, allowing
 * the vector to be represented without storing every position explicitly.
 * </p>
 *
 * <p>
 * A sparse vector does not define similarity, distance, normalization, or
 * indexing semantics. Those concerns belong to the vector database or provider.
 * </p>
 *
 * @since 1.1
 */
public interface SparseVector extends Vector {
    /**
     * Returns the total number of dimensions in this vector.
     * <p>
     * Given the sparse representation:
     * </p>
     *
     * <pre>
     * dimensions: 10
     * indices:    [2, 8]
     * values:     [7, 3]
     * </pre>
     *
     * <p>
     * this method returns {@code 10}.
     * </p>
     *
     * @return the total number of dimensions
     */
    int dimensions();

    /**
     * Returns the indices of the explicitly stored values.
     * <p>
     * Given the sparse representation:
     * </p>
     *
     * <pre>
     * dimensions: 10
     * indices:    [2, 8]
     * values:     [7, 3]
     * </pre>
     *
     * <p>
     * this method returns {@code [2, 8]}. Each index corresponds to the value at
     * the same position returned by {@link #values()}.
     * </p>
     *
     * @return the indices of the explicitly stored values
     */
    int[] indices();

    /**
     * Returns the explicitly stored values.
     * <p>
     * Given the sparse representation:
     * </p>
     *
     * <pre>
     * dimensions: 10
     * indices:    [2, 8]
     * values:     [7, 3]
     * </pre>
     *
     * <p>
     * This method returns {@code [7, 3]}. Each value corresponds to the index at
     * the same position returned by {@link #indices()}.
     * </p>
     *
     * @return the explicitly stored values
     */
    float[] values();

    /**
     * Creates a sparse vector with the given number of dimensions and explicitly
     * stored elements.
     * <p>
     * The following vector:
     * </p>
     *
     * <pre>
     * dimensions: 10
     * indices:    [2, 8]
     * values:     [7, 3]
     * </pre>
     *
     * <p>
     * can be created as:
     * </p>
     *
     * <pre>{@code
     * SparseVector vector = SparseVector.of(
     *         10,
     *         SparseVector.element(2, 7.0f),
     *         SparseVector.element(8, 3.0f)
     * );
     * }</pre>
     *
     * @param dimensions the total number of dimensions
     * @param elements the explicitly stored elements
     * @return a sparse vector
     * @throws IllegalArgumentException if the dimensions are invalid, an index is
     *         outside the vector bounds, an index is duplicated, or a value is not finite
     * @throws NullPointerException if {@code elements} or any element is {@code null}
     */
    static SparseVector of(int dimensions, Element... elements) {
        return new DefaultSparseVector(dimensions, elements);
    }

    /**
     * Creates an explicitly stored element at the given index.
     * <p>
     * The call:
     * </p>
     *
     * <pre>{@code
     * SparseVector.Element element = SparseVector.element(2, 7.0f);
     * }</pre>
     *
     * <p>
     * represents the value {@code 7} at index {@code 2}.
     * </p>
     *
     * @param index the vector index
     * @param value the value at that index
     * @return the sparse vector element
     * @throws IllegalArgumentException if the index is negative or the value is not finite
     */
    static Element element(int index, float value) {
        return new DefaultSparseVectorElement(index, value);
    }

    /**
     * Represents an explicitly stored value at a specific index of a sparse vector.
     */
    interface Element {

        /**
         * Returns the position of this element in the vector.
         * <p>
         * For {@code SparseVector.element(2, 7.0f)}, this method returns {@code 2}.
         * </p>
         *
         * @return the vector index
         */
        int index();

        /**
         * Returns the value stored at this element's index.
         * <p>
         * For {@code SparseVector.element(2, 7.0f)}, this method returns {@code 7.0f}.
         * </p>
         *
         * @return the stored value
         */
        float value();
    }
}
