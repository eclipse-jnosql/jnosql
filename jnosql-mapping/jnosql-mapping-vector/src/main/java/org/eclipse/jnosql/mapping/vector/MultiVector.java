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

import java.util.List;

/**
 * Represents a vector composed of multiple dense vectors for the same logical value.
 * <p>
 * While a {@link DenseVector} represents an item with a single vector:
 * </p>
 *
 * <pre>
 * [0.2, 0.8, 0.1]
 * </pre>
 *
 * <p>
 * a multi-vector represents the same item using multiple vectors:
 * </p>
 *
 * <pre>
 * [
 *   [0.2, 0.8, 0.1],
 *   [0.7, 0.1, 0.4],
 *   [0.3, 0.6, 0.9]
 * ]
 * </pre>
 *
 * <p>
 * This representation is useful when a single item requires more than one
 * embedding, such as a document represented by multiple passages, an image
 * represented by multiple regions, or models that produce multiple embeddings
 * for a single value.
 * </p>
 *
 * <p>
 * In Java, the same multi-vector can be created as:
 * </p>
 *
 * <pre>{@code
 * MultiVector vector = MultiVector.of(
 *         DenseVector.of(0.2f, 0.8f, 0.1f),
 *         DenseVector.of(0.7f, 0.1f, 0.4f),
 *         DenseVector.of(0.3f, 0.6f, 0.9f)
 * );
 * }</pre>
 *
 * <p>
 * All vectors in a multi-vector have the same number of dimensions.
 * A multi-vector does not define similarity, distance, aggregation, or
 * normalization semantics. Those concerns belong to the vector database or
 * provider using the vector.
 * </p>
 *
 * @since 1.1
 */
public interface MultiVector extends Vector {

    /**
     * Returns the number of dense vectors represented by this multi-vector.
     *
     * <p>
     * Given:
     * </p>
     *
     * <pre>
     * [
     *   [0.2, 0.8, 0.1],
     *   [0.7, 0.1, 0.4],
     *   [0.3, 0.6, 0.9]
     * ]
     * </pre>
     *
     * <p>
     * this method returns {@code 3}.
     * </p>
     *
     * @return the number of vectors
     */
    int size();

    /**
     * Returns the number of dimensions of each dense vector.
     *
     * <p>
     * Given:
     * </p>
     *
     * <pre>
     * [
     *   [0.2, 0.8, 0.1],
     *   [0.7, 0.1, 0.4],
     *   [0.3, 0.6, 0.9]
     * ]
     * </pre>
     *
     * <p>
     * this method returns {@code 3}.
     * </p>
     *
     * @return the number of dimensions of each vector
     */
    int dimensions();

    /**
     * Returns the dense vectors represented by this multi-vector.
     *
     * <pre>{@code
     * MultiVector vector = MultiVector.of(
     *         DenseVector.of(0.2f, 0.8f, 0.1f),
     *         DenseVector.of(0.7f, 0.1f, 0.4f)
     * );
     *
     * List<DenseVector> vectors = vector.vectors();
     * }</pre>
     *
     * @return the dense vectors
     */
    List<DenseVector> vectors();

    /**
     * Creates a multi-vector from the given dense vectors.
     *
     * <pre>{@code
     * MultiVector vector = MultiVector.of(
     *         DenseVector.of(0.2f, 0.8f, 0.1f),
     *         DenseVector.of(0.7f, 0.1f, 0.4f),
     *         DenseVector.of(0.3f, 0.6f, 0.9f)
     * );
     * }</pre>
     *
     * @param vectors the dense vectors
     * @return a multi-vector
     * @throws IllegalArgumentException if no vectors are provided or the vectors
     *                                  have different dimensions
     * @throws NullPointerException if {@code vectors} or any vector is {@code null}
     */
    static MultiVector of(DenseVector... vectors) {
        return new DefaultMultiVector(vectors);
    }
}