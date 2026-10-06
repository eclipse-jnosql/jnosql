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
 * Specializes {@link Template} for vector databases.
 *
 * <p>
 * A vector database stores and retrieves entities using vector representations and
 * similarity-based search. Unlike traditional lexical or exact-value queries, vector
 * search evaluates how close one vector is to another according to a configured
 * similarity or distance metric.
 * </p>
 *
 * <p>
 * An entity handled by this template must contain a persisted {@link Vector} attribute
 * annotated with {@code @Column}. For example:
 * </p>
 *
 * <pre>{@code
 * @Entity
 * public class Article {
 *
 *     @Id
 *     private String id;
 *
 *     @Column
 *     private String content;
 *
 *     @Column
 *     private String author;
 *
 *     @Column
 *     private int year;
 *
 *     @Column
 *     private DenseVector embedding;
 * }
 * }</pre>
 *
 * <p>
 * The {@code @Id} attribute represents the vector record identifier, the persisted
 * {@link Vector} attribute represents the vector used by the database, and the remaining
 * persisted attributes may be mapped by the provider as payload or metadata.
 * The vector property does not need to use a predefined name such as {@code embedding}.
 * </p>
 *
 * <p>
 * Standard persistence operations such as insertion, update, identifier lookup, and
 * deletion are inherited from {@link Template}. However, vector databases primarily
 * expose similarity-based operations rather than lexical or exact-value queries.
 * Consequently, some regular query operations inherited from {@link Template} may not
 * be supported by a provider and may result in {@link UnsupportedOperationException}.
 * </p>
 *
 * <p>
 * This API does not generate vectors, select indexes, normalize vector values, or define
 * the similarity metric. Vector generation and database-specific configuration remain
 * outside the responsibility of this template.
 * </p>
 *
 * @see Vector
 * @see DenseVector
 * @see Template
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
