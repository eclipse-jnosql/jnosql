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
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

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
     * The default implementation reuses semi-structured persistence and delegates vector search
     * operations to the configured {@link VectorManager}. Unsupported representations or search
     * capabilities may result in {@link UnsupportedOperationException} from the provider.
 * </p>
 *
 * @see Vector
 * @see DenseVector
 * @see Template
 */
public interface VectorTemplate extends SemiStructuredTemplate {

    /**
     * Creates a vector search for the supplied entity type.
     *
     * <pre>{@code
     * List<Article> articles = vectorTemplate.search(Article.class)
     *         .vector(queryVector)
     *         .limit(Limit.of(10))
     *         .result();
     * }</pre>
     *
     * @param entityClass the mapped entity type
     * @param <T> the entity type
     * @return a fluent vector search builder
     * @throws NullPointerException if {@code entityClass} is {@code null}
     */
    <T> VectorSearch.MapperFrom<T>  search(Class<T> entityClass);
}
