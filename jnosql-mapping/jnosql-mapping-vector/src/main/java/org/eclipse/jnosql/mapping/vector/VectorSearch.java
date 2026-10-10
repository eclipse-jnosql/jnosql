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
         * Starts a payload condition.
         *
         * @param name the mapped entity attribute name
         * @return the condition step
         */
        MapperNameCondition<T> where(String name);

        /**
         * Defines the similarity or distance threshold.
         *
         * @param threshold the threshold
         * @return the threshold step
         */
        MapperThreshold<T> threshold(float threshold);

        /**
         * Defines the maximum number of results.
         *
         * @param limit the result limit
         * @return the executable search
         */
        MapperLimit<T> limit(Limit limit);
    }

    /**
     * Represents a condition associated with a mapped entity attribute.
     *
     * @param <T> the entity type
     */
    interface MapperNameCondition<T> {

        MapperWhere<T> eq(Object value);

        MapperWhere<T> ne(Object value);

        MapperWhere<T> gt(Object value);

        MapperWhere<T> gte(Object value);

        MapperWhere<T> lt(Object value);

        MapperWhere<T> lte(Object value);
    }

    /**
     * Represents a search after a payload condition has been defined.
     *
     * @param <T> the entity type
     */
    interface MapperWhere<T> {

        /**
         * Adds another condition using logical {@code AND}.
         *
         * @param name the mapped entity attribute name
         * @return the condition step
         */
        MapperNameCondition<T> and(String name);

        /**
         * Adds another condition using logical {@code OR}.
         *
         * @param name the mapped entity attribute name
         * @return the condition step
         */
        MapperNameCondition<T> or(String name);

        /**
         * Defines the similarity or distance threshold.
         *
         * @param threshold the threshold
         * @return the threshold step
         */
        MapperThreshold<T> threshold(float threshold);

        /**
         * Defines the maximum number of results.
         *
         * @param limit the result limit
         * @return the executable search
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
         * Defines the maximum number of results.
         *
         * @param limit the result limit
         * @return the executable search
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
         * @return the matching entities ordered according to the configured
         * similarity or distance metric
         */
        List<T> result();
    }
}