/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
 *   Otavio Santana
 *  Matheus Oliveira
 */
package org.eclipse.jnosql.mapping.semistructured;

import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

import static java.util.Objects.requireNonNull;

final class MapperDelete extends AbstractMapperQuery implements SemiStructuredMapperDelete {

    MapperDelete(EntityMetadata mapping, Converters converters, SemiStructuredTemplate template) {
        super(mapping, converters, template);
    }

    @Override
    public SemiStructuredMapperDelete where(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        this.nameForCondition = null;
        return this;
    }

    @Override
    public SemiStructuredMapperDelete and(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        this.nameForCondition = null;
        this.and = true;
        return this;
    }

    @Override
    public SemiStructuredMapperDelete or(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        this.nameForCondition = null;
        this.and = false;
        return this;
    }

    @Override
    public SemiStructuredMapperDelete not() {
        this.negate = true;
        return this;
    }

    @Override
    public <T> SemiStructuredMapperDelete eq(T value) {
        eqImpl(value);
        return this;
    }

    @Override
    public SemiStructuredMapperDelete like(String value) {
        likeImpl(value);
        return this;
    }

    @Override
    public SemiStructuredMapperDelete contains(String value) {
        containsImpl(value);
        return this;
    }

    @Override
    public SemiStructuredMapperDelete startsWith(String value) {
        startWithImpl(value);
        return this;
    }

    @Override
    public SemiStructuredMapperDelete endsWith(String value) {
        endsWithImpl(value);
        return this;
    }

    @Override
    public <T> SemiStructuredMapperDelete gt(T value) {
        gtImpl(value);
        return this;
    }

    @Override
    public <T> SemiStructuredMapperDelete gte(T value) {
        gteImpl(value);
        return this;
    }

    @Override
    public <T> SemiStructuredMapperDelete lt(T value) {
        ltImpl(value);
        return this;
    }

    @Override
    public <T> SemiStructuredMapperDelete lte(T value) {
        lteImpl(value);
        return this;
    }

    @Override
    public <T> SemiStructuredMapperDelete between(T valueA, T valueB) {
        betweenImpl(valueA, valueB);
        return this;
    }

    @Override
    public <T> SemiStructuredMapperDelete in(Iterable<T> values) {
        inImpl(values);
        return this;
    }

    @Override
    public SemiStructuredMapperDelete where(Function function) {
        requireNonNull(function, "function is required");
        setFunction(function);
        return this;
    }

    @Override
    public SemiStructuredMapperDelete and(Function function) {
        requireNonNull(function, "function is required");
        setFunction(function);
        this.and = true;
        return this;
    }

    @Override
    public SemiStructuredMapperDelete or(Function function) {
        requireNonNull(function, "function is required");
        setFunction(function);
        this.and = false;
        return this;
    }

    private DeleteQuery build() {
        return new MappingDeleteQuery(entity, condition);
    }

    @Override
    public void execute() {
        DeleteQuery query = build();
        this.template.delete(query);
    }

}
