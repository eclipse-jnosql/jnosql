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

import jakarta.data.Limit;
import jakarta.nosql.Template;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

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
 * <p>
 * The default template is available through CDI with {@code @Database(DatabaseType.VECTOR)}.
 * Applications can also use {@link VectorTemplateProducer} with a programmatically created manager.
 * The default implementation reuses semi-structured persistence; all three vector search
 * methods currently throw {@link UnsupportedOperationException}. Native search and
 * vector-specific mapping validation are deferred to a later implementation.
 * </p>
 *
 * @see Vector
 * @see DenseVector
 * @see Template
 */
public interface VectorTemplate extends SemiStructuredTemplate {

    /**
     * Finds the nearest entities to the supplied query vector.
     *
     * Results are ordered according to the similarity or distance metric configured by
     * the underlying vector database.
     *
     * <pre>{@code
     * Vector queryVector = DenseVector.of(
     *         0.12F,
     *         0.45F,
     *         0.78F
     * );
     *
     * List<Article> articles = vectorTemplate.searchNearestNeighbors(
     *         Article.class,
     *         queryVector,
     *         Limit.of(10)
     * );
     * }</pre>
     *
     * @param entityClass the mapped entity type
     * @param queryVector the vector used as the search reference
     * @param limit the maximum number of results to return
     * @param <T> the entity type
     * @return the matching entities ordered from nearest to farthest
     * @throws NullPointerException if {@code entityClass}, {@code queryVector},
     *         or {@code limit} is {@code null}
     * @throws IllegalArgumentException if the vector dimensions are incompatible
     *         with the configured vector space
     * @throws UnsupportedOperationException if the vector representation or
     *         requested limit configuration is not supported by the provider
     */
    <T> List<T> searchNearestNeighbors(Class<T> entityClass, Vector queryVector, Limit limit);

    /**
     * Finds the nearest entities to the supplied query vector while applying payload
     * attribute filters.
     * Filter keys represent mapped entity attribute names. Filters are combined using
     * logical {@code AND}. An empty map applies no additional payload filtering.
     *
     * <pre>{@code
     * Vector queryVector = DenseVector.of(
     *         0.12F,
     *         0.45F,
     *         0.78F
     * );
     *
     * List<Article> articles = vectorTemplate.searchNearestNeighbors(
     *         Article.class,
     *         queryVector,
     *         Map.of(
     *                 "author", "Otavio Santana",
     *                 "year", 2026
     *         ),
     *         Limit.of(10)
     * );
     * }</pre>
     *
     * @param entityClass the mapped entity type
     * @param queryVector the vector used as the search reference
     * @param filters the payload attribute filters
     * @param limit the maximum number of results to return
     * @param <T> the entity type
     * @return the matching entities ordered from nearest to farthest
     * @throws NullPointerException if {@code entityClass}, {@code queryVector},
     *         {@code filters}, or {@code limit} is {@code null}
     * @throws IllegalArgumentException if the vector dimensions are incompatible
     *         with the configured vector space or a filter does not reference
     *         a valid payload attribute
     * @throws UnsupportedOperationException if the vector representation, filter,
     *         or requested limit configuration is not supported by the provider
     */
    <T> List<T> searchNearestNeighbors(
            Class<T> entityClass,
            Vector queryVector,
            Map<String, Object> filters,
            Limit limit);

    /**
     * Finds entities whose vectors satisfy the supplied threshold according to the
     * similarity or distance metric configured by the underlying vector database.
     * For distance-based metrics, the threshold typically represents an upper bound.
     * For similarity-based metrics, it typically represents a lower bound.
     * Threshold values are therefore specific to the configured metric and may not
     * be portable between providers.

     *
     * <pre>{@code
     * Vector queryVector = DenseVector.of(
     *         0.12F,
     *         0.45F,
     *         0.78F
     * );
     *
     * List<Article> articles = vectorTemplate.searchWithinThreshold(
     *         Article.class,
     *         queryVector,
     *         0.85F
     * );
     * }</pre>
     *
     * @param entityClass the mapped entity type
     * @param queryVector the vector used as the search reference
     * @param threshold the threshold used by the configured similarity or distance metric
     * @param <T> the entity type
     * @return the matching entities ordered from nearest to farthest
     * @throws NullPointerException if {@code entityClass} or {@code queryVector}
     *         is {@code null}
     * @throws IllegalArgumentException if {@code threshold} is not finite or the
     *         vector dimensions are incompatible with the configured vector space
     * @throws UnsupportedOperationException if the vector representation or
     *         threshold search is not supported by the provider
     */
    <T> List<T> searchWithinThreshold(
            Class<T> entityClass,
            Vector queryVector,
            float threshold);
}
