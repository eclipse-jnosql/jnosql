/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Matheus Oliveira
 */
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.nosql.QueryMapper;

/**
 * A fluent select query builder for semi-structured databases that extends the standard
 * {@link QueryMapper} fluent API with support for {@link Function} expressions.
 *
 * <p>Returned by {@link SemiStructuredTemplate#select(Class)} (and
 * {@link AbstractSemiStructuredTemplate}), this interface combines all query-building
 * concerns — field conditions, logical operators, ordering, pagination, and
 * function expressions — into a single fluent type.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * List<Word> result = template.select(Word.class)
 *         .where(Function.upper("term")).eq("JAVA")
 *         .and(Function.length("term")).gt(5)
 *         .result();
 * }</pre>
 *
 * @since 1.1.0
 * @see Function
 */
public interface SemiStructuredMapperSelect extends
        QueryMapper.MapperFrom,
        QueryMapper.MapperWhere,
        QueryMapper.MapperNameCondition,
        QueryMapper.MapperNotCondition,
        QueryMapper.MapperNameOrder,
        QueryMapper.MapperOrder,
        QueryMapper.MapperSkip,
        QueryMapper.MapperLimit,
        QueryMapper.MapperQueryBuild {

    @Override
    SemiStructuredMapperSelect where(String name);

    @Override
    <T> SemiStructuredMapperSelect eq(T value);

    @Override
    <T> SemiStructuredMapperSelect gt(T value);

    @Override
    <T> SemiStructuredMapperSelect gte(T value);

    @Override
    <T> SemiStructuredMapperSelect lt(T value);

    @Override
    <T> SemiStructuredMapperSelect lte(T value);

    @Override
    SemiStructuredMapperSelect like(String value);

    @Override
    SemiStructuredMapperSelect contains(String value);

    @Override
    SemiStructuredMapperSelect startsWith(String value);

    @Override
    SemiStructuredMapperSelect endsWith(String value);

    @Override
    <T> SemiStructuredMapperSelect between(T valueA, T valueB);

    @Override
    <T> SemiStructuredMapperSelect in(Iterable<T> values);

    @Override
    SemiStructuredMapperSelect not();

    @Override
    SemiStructuredMapperSelect and(String name);

    @Override
    SemiStructuredMapperSelect or(String name);

    @Override
    SemiStructuredMapperSelect skip(long skip);

    @Override
    SemiStructuredMapperSelect limit(long limit);

    @Override
    SemiStructuredMapperSelect orderBy(String name);

    @Override
    SemiStructuredMapperSelect asc();

    @Override
    SemiStructuredMapperSelect desc();

    /**
     * Starts a WHERE clause using a {@link Function} expression.
     *
     * @param function the function expression; must not be {@code null}
     * @return this builder
     * @throws NullPointerException if {@code function} is {@code null}
     */
    SemiStructuredMapperSelect where(Function function);

    /**
     * Adds an AND condition using a {@link Function} expression.
     *
     * @param function the function expression; must not be {@code null}
     * @return this builder
     * @throws NullPointerException if {@code function} is {@code null}
     */
    SemiStructuredMapperSelect and(Function function);

    /**
     * Adds an OR condition using a {@link Function} expression.
     *
     * @param function the function expression; must not be {@code null}
     * @return this builder
     * @throws NullPointerException if {@code function} is {@code null}
     */
    SemiStructuredMapperSelect or(Function function);

    /**
     * Adds an ORDER BY clause using a {@link Function} expression.
     *
     * @param function the function expression; must not be {@code null}
     * @return this builder
     * @throws NullPointerException if {@code function} is {@code null}
     */
    SemiStructuredMapperSelect orderBy(Function function);
}
