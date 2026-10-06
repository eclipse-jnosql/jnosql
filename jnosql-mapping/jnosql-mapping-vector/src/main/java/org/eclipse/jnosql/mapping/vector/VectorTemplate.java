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

import jakarta.nosql.Template;

import java.util.List;
import java.util.Map;

/**
 * Specializes {@link Template} with vector-native search operations.
 * Standard insertion, update, identifier lookup, and deletion are inherited.
 * <p>
 * Entities use the existing Jakarta NoSQL annotations and contain a single persisted
 * {@link Vector}. Providers must initially support {@link DenseVector}, identify the
 * vector independently of its property name, and map the other columns as payload.
 * Providers are responsible for rejecting unsupported representations and incompatible
 * dimensions rather than silently converting them.
 * </p>
 * <p>
 * Search uses the database's configured metric; this API does not select an index,
 * generate vectors, or normalize them. A provider supplies the implementation.
 * </p>
 */
public interface VectorTemplate extends Template {

    /**
     * Finds the nearest entities without payload filtering.
     *
     * @param entityClass the mapped entity type
     * @param queryVector the query vector
     * @param limit the positive maximum number of results
     * @param <T> the entity type
     * @return at most {@code limit} entities, nearest first; an empty list when none match
     * @throws NullPointerException when entityClass or queryVector is {@code null}
     * @throws IllegalArgumentException when limit is not positive or vector dimensions are incompatible
     * @throws UnsupportedOperationException when the vector representation is unsupported
     */
    <T> List<T> searchNearestNeighbors(Class<T> entityClass, Vector queryVector, int limit);

    /**
     * Finds the nearest entities satisfying all supplied payload equality filters.
     * Filter keys are mapped Java attribute names, not database-native field names.
     * An empty map applies no filtering. Providers must reject unsupported filters
     * rather than ignore them.
     *
     * @param entityClass the mapped entity type
     * @param queryVector the query vector
     * @param filters payload attribute names and non-null values, combined with logical AND
     * @param limit the positive maximum number of results
     * @param <T> the entity type
     * @return at most {@code limit} entities, nearest first; an empty list when none match
     * @throws NullPointerException when entityClass, queryVector, filters, or any filter key or value is {@code null}
     * @throws IllegalArgumentException when limit is not positive, dimensions are incompatible, or an attribute is not payload
     * @throws UnsupportedOperationException when the vector representation or a filter is unsupported
     */
    <T> List<T> searchNearestNeighbors(Class<T> entityClass, Vector queryVector, Map<String, Object> filters, int limit);

    /**
     * Finds entities meeting a threshold under the database's configured metric.
     * For distance metrics the threshold is an inclusive upper bound; for similarity
     * scores it is an inclusive lower bound. Thresholds are not portable between metrics
     * or providers, and providers must document the metric and its valid threshold range.
     *
     * @param entityClass the mapped entity type
     * @param queryVector the query vector
     * @param threshold a finite threshold in the configured metric's range
     * @param <T> the entity type
     * @return matching entities, nearest first; an empty list when none match
     * @throws NullPointerException when entityClass or queryVector is {@code null}
     * @throws IllegalArgumentException when threshold is invalid or vector dimensions are incompatible
     * @throws UnsupportedOperationException when the vector representation or threshold search is unsupported
     */
    <T> List<T> searchWithinRadius(Class<T> entityClass, Vector queryVector, float threshold);
}
