/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 * and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.mapping.vector;

import jakarta.data.Limit;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;

import java.util.List;
import java.util.Map;

/**
 * Defines vector-native operations over {@link CommunicationEntity} instances.
 *
 * <p>
 * Standard persistence operations are inherited from {@link DatabaseManager}.
 * This interface adds operations that are specific to vector databases, such as
 * nearest-neighbor and threshold-based searches.
 * </p>
 *
 * <p>
 * The entity name identifies the vector collection or equivalent database structure.
 * A {@link CommunicationEntity} is expected to contain a single persisted
 * {@link Vector}; all remaining fields represent payload or metadata.
 * </p>
 *
 * <p>
 * The provider is responsible for converting the {@link Vector} to its native
 * representation and for applying the configured similarity or distance metric.
 * </p>
 */
public interface VectorManager extends DatabaseManager {

    /**
     * Finds the nearest communication entities to the supplied query vector.
     *
     * <p>
     * Results are ordered according to the similarity or distance metric configured
     * by the underlying vector database.
     * </p>
     *
     * <pre>{@code
     * Vector queryVector = DenseVector.of(
     *         0.12F,
     *         0.45F,
     *         0.78F
     * );
     *
     * List<CommunicationEntity> entities = manager.searchNearestNeighbors(
     *         "Article",
     *         queryVector,
     *         Limit.of(10)
     * );
     * }</pre>
     *
     * @param entityName the communication entity name
     * @param queryVector the vector used as the search reference
     * @param limit the maximum number of results to return
     * @return the matching entities ordered from nearest to farthest
     * @throws NullPointerException if {@code entityName}, {@code queryVector},
     *         or {@code limit} is {@code null}
     * @throws IllegalArgumentException if {@code entityName} is empty or the
     *         vector dimensions are incompatible with the configured vector space
     * @throws UnsupportedOperationException if the vector representation or
     *         requested limit configuration is not supported by the provider
     */
    List<CommunicationEntity> searchNearestNeighbors(
            String entityName,
            Vector queryVector,
            Limit limit);

    /**
     * Finds the nearest communication entities to the supplied query vector while
     * applying equality-based payload filters.
     *
     * <p>
     * Each map entry represents an equality predicate over a persisted payload field.
     * Multiple entries are combined using logical {@code AND}. An empty map applies
     * no additional payload filtering.
     * </p>
     *
     * <pre>{@code
     * Vector queryVector = DenseVector.of(
     *         0.12F,
     *         0.45F,
     *         0.78F
     * );
     *
     * List<CommunicationEntity> entities = manager.searchNearestNeighbors(
     *         "Article",
     *         queryVector,
     *         Map.of(
     *                 "author", "Otavio Santana",
     *                 "year", 2026
     *         ),
     *         Limit.of(10)
     * );
     * }</pre>
     *
     * @param entityName the communication entity name
     * @param queryVector the vector used as the search reference
     * @param filters the payload field names and values to match
     * @param limit the maximum number of results to return
     * @return the matching entities ordered from nearest to farthest
     * @throws NullPointerException if {@code entityName}, {@code queryVector},
     *         {@code filters}, or {@code limit} is {@code null}
     * @throws IllegalArgumentException if {@code entityName} is empty, the vector
     *         dimensions are incompatible with the configured vector space, or a
     *         filter does not represent a valid payload field
     * @throws UnsupportedOperationException if the vector representation, filter,
     *         or requested limit configuration is not supported by the provider
     */
    List<CommunicationEntity> searchNearestNeighbors(
            String entityName,
            Vector queryVector,
            Map<String, Object> filters,
            Limit limit);

    /**
     * Finds communication entities whose vectors satisfy the supplied threshold
     * according to the similarity or distance metric configured by the underlying
     * vector database.
     *
     * <p>
     * For distance-based metrics, the threshold typically represents an upper bound.
     * For similarity-based metrics, it typically represents a lower bound. Threshold
     * values are therefore specific to the configured metric and may not be portable
     * between providers.
     * </p>
     *
     * <pre>{@code
     * Vector queryVector = DenseVector.of(
     *         0.12F,
     *         0.45F,
     *         0.78F
     * );
     *
     * List<CommunicationEntity> entities = manager.searchWithinThreshold(
     *         "Article",
     *         queryVector,
     *         0.85F
     * );
     * }</pre>
     *
     * @param entityName the communication entity name
     * @param queryVector the vector used as the search reference
     * @param threshold the threshold used by the configured similarity or distance metric
     * @return the matching entities ordered from nearest to farthest
     * @throws NullPointerException if {@code entityName} or {@code queryVector}
     *         is {@code null}
     * @throws IllegalArgumentException if {@code entityName} is empty,
     *         {@code threshold} is not finite, or the vector dimensions are
     *         incompatible with the configured vector space
     * @throws UnsupportedOperationException if the vector representation or
     *         threshold search is not supported by the provider
     */
    List<CommunicationEntity> searchWithinThreshold(
            String entityName,
            Vector queryVector,
            float threshold);
}