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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.nosql.QueryMapper;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;


final class MapperUpdate extends AbstractMapperQuery implements
        QueryMapper.MapperUpdateFrom,
        QueryMapper.MapperUpdateSetTo,
        QueryMapper.MapperUpdateSetStep,
        QueryMapper.MapperUpdateNameCondition,
        QueryMapper.MapperUpdateWhere,
        QueryMapper.MapperUpdateNotCondition,
        QueryMapper.MapperUpdateQueryBuild {

    private final List<Element> elements = new LinkedList<>();

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
    public <T> QueryMapper.MapperUpdateSetStep to(T value) {
        requireNonNull(this.name, "name is required");
        this.elements.add(Element.of(mapping.columnField(name), getValue(value)));
        return this;
    }

    @Override
    public QueryMapper.MapperUpdateNameCondition where(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        return this;
    }

    @Override
    public <T> QueryMapper.MapperUpdateWhere eq(T value) {
        eqImpl(value);
        return this;
    }

    @Override
    public QueryMapper.MapperUpdateWhere like(String value) {
        likeImpl(value);
        return this;
    }

    @Override
    public QueryMapper.MapperUpdateWhere contains(String value) {
        containsImpl(value);
        return this;
    }

    @Override
    public QueryMapper.MapperUpdateWhere startsWith(String value) {
        startWithImpl(value);
        return this;
    }

    @Override
    public QueryMapper.MapperUpdateWhere endsWith(String value) {
        endsWithImpl(value);
        return this;
    }

    @Override
    public <T> QueryMapper.MapperUpdateWhere gt(T value) {
        gtImpl(value);
        return this;
    }

    @Override
    public <T> QueryMapper.MapperUpdateWhere gte(T value) {
        gteImpl(value);
        return this;
    }

    @Override
    public <T> QueryMapper.MapperUpdateWhere lt(T value) {
        ltImpl(value);
        return this;
    }

    @Override
    public <T> QueryMapper.MapperUpdateWhere lte(T value) {
        lteImpl(value);
        return this;
    }

    @Override
    public <T> QueryMapper.MapperUpdateWhere between(T valueA, T valueB) {
        betweenImpl(valueA, valueB);
        return this;
    }

    @Override
    public <T> QueryMapper.MapperUpdateWhere in(Iterable<T> values) {
        inImpl(values);
        return this;
    }

    @Override
    public QueryMapper.MapperUpdateNotCondition not() {
        this.negate = true;
        return this;
    }

    @Override
    public QueryMapper.MapperUpdateNameCondition and(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        this.and = true;
        return this;
    }

    @Override
    public QueryMapper.MapperUpdateNameCondition or(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        this.and = false;
        return this;
    }

    @Override
    public void execute() {
        var query = new SemistructureUpdateQuery(entity, elements, condition);
        this.template.update(query);
    }
}
