/*
 *  Copyright (c) 2022,2025 Contributors to the Eclipse Foundation
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

import jakarta.data.Direction;
import jakarta.data.Sort;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

final class MapperSelect extends AbstractMapperQuery implements SemiStructuredMapperSelect {

    private final List<Sort<?>> sorts = new ArrayList<>();

    MapperSelect(EntityMetadata mapping, Converters converters, SemiStructuredTemplate template) {
        super(mapping, converters, template);
    }

    @Override
    public SemiStructuredMapperSelect and(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        this.nameForCondition = null;
        this.and = true;
        return this;
    }

    @Override
    public SemiStructuredMapperSelect or(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        this.nameForCondition = null;
        this.and = false;
        return this;
    }

    @Override
    public SemiStructuredMapperSelect where(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        this.nameForCondition = null;
        return this;
    }

    @Override
    public SemiStructuredMapperSelect skip(long start) {
        this.start = start;
        return this;
    }

    @Override
    public SemiStructuredMapperSelect limit(long limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public SemiStructuredMapperSelect orderBy(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        this.nameForCondition = null;
        return this;
    }

    @Override
    public SemiStructuredMapperSelect not() {
        this.negate = true;
        return this;
    }

    @Override
    public <T> SemiStructuredMapperSelect eq(T value) {
        eqImpl(value);
        return this;
    }

    @Override
    public SemiStructuredMapperSelect like(String value) {
        likeImpl(value);
        return this;
    }

    @Override
    public SemiStructuredMapperSelect contains(String value) {
        containsImpl(value);
        return this;
    }

    @Override
    public SemiStructuredMapperSelect startsWith(String value) {
        startWithImpl(value);
        return this;
    }

    @Override
    public SemiStructuredMapperSelect endsWith(String value) {
        endsWithImpl(value);
        return this;
    }

    @Override
    public <T> SemiStructuredMapperSelect gt(T value) {
        gtImpl(value);
        return this;
    }

    @Override
    public <T> SemiStructuredMapperSelect gte(T value) {
        gteImpl(value);
        return this;
    }

    @Override
    public <T> SemiStructuredMapperSelect lt(T value) {
        ltImpl(value);
        return this;
    }

    @Override
    public <T> SemiStructuredMapperSelect lte(T value) {
        lteImpl(value);
        return this;
    }

    @Override
    public <T> SemiStructuredMapperSelect between(T valueA, T valueB) {
        betweenImpl(valueA, valueB);
        return this;
    }

    @Override
    public <T> SemiStructuredMapperSelect in(Iterable<T> values) {
        inImpl(values);
        return this;
    }

    @Override
    public SemiStructuredMapperSelect asc() {
        this.sorts.add(Sort.of(resolveColumnName(), Direction.ASC, false));
        this.nameForCondition = null;
        return this;
    }

    @Override
    public SemiStructuredMapperSelect desc() {
        this.sorts.add(Sort.of(resolveColumnName(), Direction.DESC, false));
        this.nameForCondition = null;
        return this;
    }

    @Override
    public SemiStructuredMapperSelect where(Function function) {
        requireNonNull(function, "function is required");
        setFunction(function);
        return this;
    }

    @Override
    public SemiStructuredMapperSelect and(Function function) {
        requireNonNull(function, "function is required");
        setFunction(function);
        this.and = true;
        return this;
    }

    @Override
    public SemiStructuredMapperSelect or(Function function) {
        requireNonNull(function, "function is required");
        setFunction(function);
        this.and = false;
        return this;
    }

    @Override
    public SemiStructuredMapperSelect orderBy(Function function) {
        requireNonNull(function, "function is required");
        this.nameForCondition = toFunctionExpression(function);
        return this;
    }

    private SelectQuery build() {
        return new MappingQuery(sorts, limit, start, condition, entity, List.of());
    }

    @Override
    public long count() {
        var query = build();
        return this.template.count(query);
    }

    @Override
    public <T> List<T> result() {
        SelectQuery query = build();
        return this.template.<T>select(query)
                .toList();
    }

    @Override
    public <T> Stream<T> stream() {
        SelectQuery query = build();
        return this.template.select(query);
    }

    @Override
    public <T> Optional<T> singleResult() {
        SelectQuery query = build();
        return this.template.singleResult(query);
    }

}
