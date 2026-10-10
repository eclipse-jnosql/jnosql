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

import java.util.List;
/**
 * Defines the fluent API for vector similarity searches.
 */
public interface VectorSearch {

    /**
     * Represents the first step of a vector search.
     *
     * @param <T> the entity type
     */
    interface MapperFrom<T> {

        /**
         * Defines the vector used as the search reference.
         *
         * <pre>{@code
         * Vector queryVector = DenseVector.of(
         *         0.10F,
         *         0.42F,
         *         0.80F
         * );
         *
         * vectorTemplate.search(Article.class)
         *         .vector(queryVector);
         * }</pre>
         *
         * @param vector the query vector
         * @return the next search step
         * @throws NullPointerException if {@code vector} is {@code null}
         */
        MapperVector<T> vector(Vector vector);
    }

    /**
     * Represents a search after the query vector has been defined.
     *
     * @param <T> the entity type
     */
    interface MapperVector<T> {

        /**
         * Starts a condition using a mapped entity attribute.
         *
         * <pre>{@code
         * vectorTemplate.search(Article.class)
         *         .vector(queryVector)
         *         .where("author");
         * }</pre>
         *
         * @param name the mapped entity attribute name
         * @return the condition step
         * @throws NullPointerException if {@code name} is {@code null}
         */
        MapperNameCondition<T> where(String name);

        /**
         * Defines the similarity or distance threshold.
         *
         * <pre>{@code
         * vectorTemplate.search(Article.class)
         *         .vector(queryVector)
         *         .threshold(0.85F);
         * }</pre>
         *
         * @param threshold the similarity or distance threshold
         * @return the threshold step
         * @throws IllegalArgumentException if {@code threshold} is not finite
         */
        MapperThreshold<T> threshold(float threshold);

        /**
         * Defines the result limit.
         *
         * <pre>{@code
         * vectorTemplate.search(Article.class)
         *         .vector(queryVector)
         *         .limit(Limit.of(10));
         * }</pre>
         *
         * @param limit the result limit
         * @return the executable search
         * @throws NullPointerException if {@code limit} is {@code null}
         */
        MapperLimit<T> limit(Limit limit);
    }

    /**
     * Represents a condition associated with a mapped entity attribute.
     *
     * @param <T> the entity type
     */
    interface MapperNameCondition<T> {

        /**
         * Defines an equality condition.
         *
         * <pre>{@code
         * vectorTemplate.search(Article.class)
         *         .vector(queryVector)
         *         .where("author").eq("Otavio Santana");
         * }</pre>
         *
         * @param value the value
         * @return the next search step
         * @throws NullPointerException if {@code value} is {@code null}
         */
        MapperWhere<T> eq(Object value);

        /**
         * Defines a not-equal condition.
         *
         * <pre>{@code
         * vectorTemplate.search(Article.class)
         *         .vector(queryVector)
         *         .where("author").ne("Otavio Santana");
         * }</pre>
         *
         * @param value the value
         * @return the next search step
         * @throws NullPointerException if {@code value} is {@code null}
         */
        MapperWhere<T> ne(Object value);

        /**
         * Defines a greater-than condition.
         *
         * <pre>{@code
         * vectorTemplate.search(Article.class)
         *         .vector(queryVector)
         *         .where("year").gt(2020);
         * }</pre>
         *
         * @param value the value
         * @return the next search step
         * @throws NullPointerException if {@code value} is {@code null}
         */
        MapperWhere<T> gt(Object value);

        /**
         * Defines a greater-than-or-equal condition.
         *
         * <pre>{@code
         * vectorTemplate.search(Article.class)
         *         .vector(queryVector)
         *         .where("year").gte(2020);
         * }</pre>
         *
         * @param value the value
         * @return the next search step
         * @throws NullPointerException if {@code value} is {@code null}
         */
        MapperWhere<T> gte(Object value);

        /**
         * Defines a less-than condition.
         *
         * <pre>{@code
         * vectorTemplate.search(Article.class)
         *         .vector(queryVector)
         *         .where("year").lt(2026);
         * }</pre>
         *
         * @param value the value
         * @return the next search step
         * @throws NullPointerException if {@code value} is {@code null}
         */
        MapperWhere<T> lt(Object value);

        /**
         * Defines a less-than-or-equal condition.
         *
         * <pre>{@code
         * vectorTemplate.search(Article.class)
         *         .vector(queryVector)
         *         .where("year").lte(2026);
         * }</pre>
         *
         * @param value the value
         * @return the next search step
         * @throws NullPointerException if {@code value} is {@code null}
         */
        MapperWhere<T> lte(Object value);
    }

    /**
     * Represents a search after a condition has been defined.
     *
     * @param <T> the entity type
     */
    interface MapperWhere<T> {

        /**
         * Adds another condition using logical {@code AND}.
         *
         * <pre>{@code
         * vectorTemplate.search(Article.class)
         *         .vector(queryVector)
         *         .where("author").eq("Otavio Santana")
         *         .and("year").gte(2024);
         * }</pre>
         *
         * @param name the mapped entity attribute name
         * @return the condition step
         * @throws NullPointerException if {@code name} is {@code null}
         */
        MapperNameCondition<T> and(String name);

        /**
         * Adds another condition using logical {@code OR}.
         *
         * <pre>{@code
         * vectorTemplate.search(Article.class)
         *         .vector(queryVector)
         *         .where("author").eq("Otavio Santana")
         *         .or("year").gte(2024);
         * }</pre>
         *
         * @param name the mapped entity attribute name
         * @return the condition step
         * @throws NullPointerException if {@code name} is {@code null}
         */
        MapperNameCondition<T> or(String name);

        /**
         * Defines the similarity or distance threshold.
         *
         * <pre>{@code
         * vectorTemplate.search(Article.class)
         *         .vector(queryVector)
         *         .where("author").eq("Otavio Santana")
         *         .threshold(0.85F);
         * }</pre>
         *
         * @param threshold the similarity or distance threshold
         * @return the threshold step
         * @throws IllegalArgumentException if {@code threshold} is not finite
         */
        MapperThreshold<T> threshold(float threshold);

        /**
         * Defines the result limit.
         *
         * <pre>{@code
         * vectorTemplate.search(Article.class)
         *         .vector(queryVector)
         *         .where("author").eq("Otavio Santana")
         *         .limit(Limit.of(10));
         * }</pre>
         *
         * @param limit the result limit
         * @return the executable search
         * @throws NullPointerException if {@code limit} is {@code null}
         */
        MapperLimit<T> limit(Limit limit);
    }

    /**
     * Represents a search after a threshold has been defined.
     *
     * @param <T> the entity type
     */
    interface MapperThreshold<T> {

        /**
         * Defines the result limit.
         *
         * <pre>{@code
         * vectorTemplate.search(Article.class)
         *         .vector(queryVector)
         *         .threshold(0.85F)
         *         .limit(Limit.of(10));
         * }</pre>
         *
         * @param limit the result limit
         * @return the executable search
         * @throws NullPointerException if {@code limit} is {@code null}
         */
        MapperLimit<T> limit(Limit limit);
    }

    /**
     * Represents a complete vector search that can be executed.
     *
     * @param <T> the entity type
     */
    interface MapperLimit<T> {

        /**
         * Executes the vector search.
         *
         * <pre>{@code
         * List<Article> articles = vectorTemplate.search(Article.class)
         *         .vector(queryVector)
         *         .limit(Limit.of(10))
         *         .result();
         * }</pre>
         *
         * @return the matching entities ordered according to the configured
         * similarity or distance metric
         */
        List<T> result();
    }
}