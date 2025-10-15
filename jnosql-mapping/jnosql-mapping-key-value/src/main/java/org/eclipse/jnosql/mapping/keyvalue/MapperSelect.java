/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.keyvalue;


import jakarta.data.exceptions.NonUniqueResultException;
import jakarta.nosql.MappingException;
import jakarta.nosql.QueryMapper;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.util.ConverterUtil;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

final class MapperSelect implements QueryMapper.MapperFrom, QueryMapper.MapperLimit,
        QueryMapper.MapperSkip, QueryMapper.MapperOrder, QueryMapper.MapperNameCondition,
        QueryMapper.MapperNotCondition, QueryMapper.MapperNameOrder, QueryMapper.MapperWhere {

    private final transient EntityMetadata mapping;

    private final transient Converters converters;

    private final transient KeyValueTemplate template;

    private final transient FieldMetadata id;
    private String name;

    private final List<Object> keys = new ArrayList<>();


    MapperSelect(EntityMetadata mapping, Converters converters, KeyValueTemplate template) {
        this.mapping = mapping;
        this.converters = converters;
        this.template = template;
        this.id = mapping.id().orElseThrow(() -> new MappingException("The entity " + mapping.name() + " does not have an attribute with id annotation"));
    }

    @Override
    public QueryMapper.MapperNameCondition and(String name) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support and condition");
    }

    @Override
    public QueryMapper.MapperNameCondition or(String name) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support or condition");
    }

    @Override
    public QueryMapper.MapperSkip skip(long start) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support skip");
    }

    @Override
    public QueryMapper.MapperLimit limit(long limit) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support limit");
    }

    @Override
    public QueryMapper.MapperOrder orderBy(String name) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support order by");
    }

    @Override
    public QueryMapper.MapperNotCondition not() {
        throw new UnsupportedOperationException("Key-value Mapper query does not support not condition");
    }

    @Override
    public QueryMapper.MapperWhere like(String value) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support like condition");
    }

    @Override
    public <T> QueryMapper.MapperWhere gt(T value) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support gt condition");
    }

    @Override
    public <T> QueryMapper.MapperWhere gte(T value) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support gte condition");
    }

    @Override
    public <T> QueryMapper.MapperWhere lt(T value) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support lt condition");
    }

    @Override
    public <T> QueryMapper.MapperWhere lte(T value) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support lte condition");
    }

    @Override
    public <T> QueryMapper.MapperWhere between(T valueA, T valueB) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support between condition");
    }


    @Override
    public QueryMapper.MapperNameOrder asc() {
        throw new UnsupportedOperationException("Key-value Mapper query does not support asc order");
    }

    @Override
    public QueryMapper.MapperNameOrder desc() {
        throw new UnsupportedOperationException("Key-value Mapper query does not support desc order");
    }

    @Override
    public <T> QueryMapper.MapperWhere eq(T value) {
        requireNonNull(value, "value is required");
        keys.add(ConverterUtil.getValue(value, mapping, name, converters));
        return this;
    }

    @Override
    public <T> QueryMapper.MapperWhere in(Iterable<T> values) {
        requireNonNull(values, "value is required");
        values.forEach(v -> keys.add(ConverterUtil.getValue(v, mapping, name, converters)));
        return this;
    }

    @Override
    public QueryMapper.MapperNameCondition where(String name) {
        requireNonNull(name, "name is required");
        if (!id.fieldName().equals(name)) {
            throw new UnsupportedOperationException("Key-value Mapper query only support the id attribute: " + id.name() + " at the entity: " + mapping.name());
        }
        this.name = name;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> result() {
        validatedCondition();
        List<T> entities = new ArrayList<>();
        this.template.get(keys, (Class<T>) mapping.type()).forEach(entities::add);
        return entities;
    }



    @Override
    public <T> Stream<T> stream() {
        return this.<T>result().stream();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> singleResult() {
        validatedCondition();
        if (keys.size() == 1) {
            return this.template.get(keys.getFirst(), (Class<T>) this.mapping.type());
        } else {
            List<T> values = result();
            if(values.size() == 1) {
                return Optional.of(values.getFirst());
            } else if(values.isEmpty()) {
                return Optional.empty();
            } else {
                throw new NonUniqueResultException("Expected one result but found: " + values.size());
            }
        }
    }

    private void validatedCondition() {
        if (keys.isEmpty()) {
            throw new UnsupportedOperationException("On Key-value Mapper it requires to have at least one condition either eq or in");
        }
    }

}