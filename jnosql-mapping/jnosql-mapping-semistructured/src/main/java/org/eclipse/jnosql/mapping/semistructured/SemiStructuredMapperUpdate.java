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
 * A fluent update query builder for semi-structured databases that extends the standard
 * {@link QueryMapper} fluent API with support for {@link Function} expressions.
 *
 * <p>Returned by {@link AbstractSemiStructuredTemplate#update(Class)}, this interface
 * combines update query-building concerns with function expression support.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * template.update(Word.class)
 *         .set("meaning").to("new meaning")
 *         .where(Function.upper("term")).eq("JAVA")
 *         .execute();
 * }</pre>
 *
 * @since 1.1.0
 * @see Function
 */
public interface SemiStructuredMapperUpdate extends
        QueryMapper.MapperUpdateFrom,
        QueryMapper.MapperUpdateSetStep,
        QueryMapper.MapperUpdateSetTo,
        QueryMapper.MapperUpdateNameCondition,
        QueryMapper.MapperUpdateWhere,
        QueryMapper.MapperUpdateNotCondition,
        QueryMapper.MapperUpdateQueryBuild {

    @Override
    SemiStructuredMapperUpdate set(String name);

    @Override
    <T> SemiStructuredMapperUpdate to(T value);

    @Override
    SemiStructuredMapperUpdate where(String name);

    @Override
    <T> SemiStructuredMapperUpdate eq(T value);

    @Override
    <T> SemiStructuredMapperUpdate gt(T value);

    @Override
    <T> SemiStructuredMapperUpdate gte(T value);

    @Override
    <T> SemiStructuredMapperUpdate lt(T value);

    @Override
    <T> SemiStructuredMapperUpdate lte(T value);

    @Override
    SemiStructuredMapperUpdate like(String value);

    @Override
    SemiStructuredMapperUpdate contains(String value);

    @Override
    SemiStructuredMapperUpdate startsWith(String value);

    @Override
    SemiStructuredMapperUpdate endsWith(String value);

    @Override
    <T> SemiStructuredMapperUpdate between(T valueA, T valueB);

    @Override
    SemiStructuredMapperUpdate not();

    @Override
    SemiStructuredMapperUpdate and(String name);

    @Override
    SemiStructuredMapperUpdate or(String name);

    /**
     * Starts a WHERE clause using a {@link Function} expression.
     *
     * @param function the function expression; must not be {@code null}
     * @return this builder
     * @throws NullPointerException if {@code function} is {@code null}
     */
    SemiStructuredMapperUpdate where(Function function);

    /**
     * Adds an AND condition using a {@link Function} expression.
     *
     * @param function the function expression; must not be {@code null}
     * @return this builder
     * @throws NullPointerException if {@code function} is {@code null}
     */
    SemiStructuredMapperUpdate and(Function function);

    /**
     * Adds an OR condition using a {@link Function} expression.
     *
     * @param function the function expression; must not be {@code null}
     * @return this builder
     * @throws NullPointerException if {@code function} is {@code null}
     */
    SemiStructuredMapperUpdate or(Function function);
}
