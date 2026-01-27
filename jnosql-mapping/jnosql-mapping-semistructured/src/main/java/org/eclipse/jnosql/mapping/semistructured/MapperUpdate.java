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
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.nosql.QueryMapper;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;


final class MapperUpdate extends AbstractMapperQuery implements
        QueryMapper.MapperUpdateFrom,
        QueryMapper.MapperUpdateSetTo,
        QueryMapper.MapperUpdateSetStep,
        QueryMapper.MapperUpdateWhereStep,
        QueryMapper.MapperUpdateQueryBuild,
        QueryMapper.MapperUpdateConditionStep {

    private final List<Element> elements = new ArrayList<>();

    MapperUpdate(EntityMetadata mapping, Converters converters, SemiStructuredTemplate template) {
        super(mapping, converters, template);
    }

    @Override
    public QueryMapper.MapperUpdateSetTo set(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        return this;
    }

    @Override
    public QueryMapper.MapperUpdateWhereStep where(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        return this;
    }

    @Override
    public <T> QueryMapper.MapperUpdateSetStep to(T value) {
        requireNonNull(value, "value is required");
        this.elements.add(Element.of(name, getValue(value)));
        return this;
    }


    @Override
    public <T> QueryMapper.MapperUpdateConditionStep eq(T value) {
        eqImpl(value);
        return this;
    }

    @Override
    public QueryMapper.MapperUpdateConditionStep like(String value) {
        likeImpl(value);
        return this;
    }

    @Override
    public QueryMapper.MapperUpdateConditionStep contains(String value) {
        containsImpl(value);
        return this;
    }

    @Override
    public QueryMapper.MapperUpdateConditionStep startsWith(String value) {
        startWithImpl(value);
        return this;
    }

    @Override
    public QueryMapper.MapperUpdateConditionStep endsWith(String value) {
        endsWithImpl(value);
        return this;
    }

    @Override
    public <T> QueryMapper.MapperUpdateConditionStep gt(T value) {
        gtImpl(value);
        return this;
    }

    @Override
    public <T> QueryMapper.MapperUpdateConditionStep gte(T value) {
        gteImpl(value);
        return this;
    }

    @Override
    public <T> QueryMapper.MapperUpdateConditionStep lt(T value) {
        ltImpl(value);
        return this;
    }

    @Override
    public <T> QueryMapper.MapperUpdateConditionStep lte(T value) {
        lteImpl(value);
        return this;
    }

    @Override
    public <T> QueryMapper.MapperUpdateConditionStep between(T valueA, T valueB) {
        betweenImpl(valueA, valueB);
        return this;
    }

    @Override
    public <T> QueryMapper.MapperUpdateConditionStep in(Iterable<T> values) {
        inImpl(values);
        return this;
    }

    @Override
    public QueryMapper.MapperUpdateWhereStep and(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        this.and = true;
        return this;
    }

    @Override
    public QueryMapper.MapperUpdateWhereStep or(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        this.and = false;
        return this;
    }

    @Override
    public QueryMapper.MapperUpdateWhereStep not() {
        this.negate = true;
        return this;
    }

    @Override
    public void execute() {
        var query = new SemistructureUpdateQuery(entity, elements, condition);
        this.template.update(query);
    }
}
