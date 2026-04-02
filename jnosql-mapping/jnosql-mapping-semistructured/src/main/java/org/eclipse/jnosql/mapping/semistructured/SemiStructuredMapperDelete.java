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
 * A fluent delete query builder for semi-structured databases that extends the standard
 * {@link QueryMapper} fluent API with support for {@link Function} expressions.
 *
 * <p>Returned by {@link AbstractSemiStructuredTemplate#delete(Class)}, this interface
 * combines delete query-building concerns with function expression support.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * template.delete(Word.class)
 *         .where(Function.upper("term")).eq("JAVA")
 *         .execute();
 * }</pre>
 *
 * @since 1.1.0
 * @see Function
 */
public interface SemiStructuredMapperDelete extends
        QueryMapper.MapperDeleteFrom,
        QueryMapper.MapperDeleteWhere,
        QueryMapper.MapperDeleteNameCondition,
        QueryMapper.MapperDeleteNotCondition,
        QueryMapper.MapperDeleteQueryBuild {

    @Override
    SemiStructuredMapperDelete where(String name);

    @Override
    <T> SemiStructuredMapperDelete eq(T value);

    @Override
    <T> SemiStructuredMapperDelete gt(T value);

    @Override
    <T> SemiStructuredMapperDelete gte(T value);

    @Override
    <T> SemiStructuredMapperDelete lt(T value);

    @Override
    <T> SemiStructuredMapperDelete lte(T value);

    @Override
    SemiStructuredMapperDelete like(String value);

    @Override
    SemiStructuredMapperDelete contains(String value);

    @Override
    SemiStructuredMapperDelete startsWith(String value);

    @Override
    SemiStructuredMapperDelete endsWith(String value);

    @Override
    <T> SemiStructuredMapperDelete between(T valueA, T valueB);

    @Override
    SemiStructuredMapperDelete not();

    @Override
    SemiStructuredMapperDelete and(String name);

    @Override
    SemiStructuredMapperDelete or(String name);

    /**
     * Starts a WHERE clause using a {@link Function} expression.
     *
     * @param function the function expression; must not be {@code null}
     * @return this builder
     * @throws NullPointerException if {@code function} is {@code null}
     */
    SemiStructuredMapperDelete where(Function function);

    /**
     * Adds an AND condition using a {@link Function} expression.
     *
     * @param function the function expression; must not be {@code null}
     * @return this builder
     * @throws NullPointerException if {@code function} is {@code null}
     */
    SemiStructuredMapperDelete and(Function function);

    /**
     * Adds an OR condition using a {@link Function} expression.
     *
     * @param function the function expression; must not be {@code null}
     * @return this builder
     * @throws NullPointerException if {@code function} is {@code null}
     */
    SemiStructuredMapperDelete or(Function function);
}
