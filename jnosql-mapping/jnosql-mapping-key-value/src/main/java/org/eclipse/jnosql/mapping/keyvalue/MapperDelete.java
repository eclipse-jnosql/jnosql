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

import jakarta.nosql.MappingException;
import jakarta.nosql.QueryMapper;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.util.ConverterUtil;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

final class MapperDelete implements QueryMapper.MapperDeleteFrom,
        QueryMapper.MapperDeleteWhere,
        QueryMapper.MapperDeleteNameCondition,
        QueryMapper.MapperDeleteNotCondition {

    private final transient EntityMetadata mapping;

    private final transient Converters converters;

    private final transient KeyValueTemplate template;

    private final transient FieldMetadata id;
    private String name;

    private final List<Object> keys = new ArrayList<>();

    MapperDelete(EntityMetadata mapping, Converters converters, KeyValueTemplate template) {
        this.mapping = mapping;
        this.converters = converters;
        this.template = template;
        this.id = mapping.id().orElseThrow(() -> new MappingException("The entity " + mapping.name() + " does not have an attribute with id annotation"));
    }

    @Override
    public QueryMapper.MapperDeleteWhere like(String value) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support like condition");
    }

    @Override
    public QueryMapper.MapperDeleteWhere contains(String value) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support contains condition");
    }

    @Override
    public QueryMapper.MapperDeleteWhere startsWith(String value) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support startsWith condition");
    }

    @Override
    public QueryMapper.MapperDeleteWhere endsWith(String value) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support endsWith condition");
    }

    @Override
    public <T> QueryMapper.MapperDeleteWhere gt(T value) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support gt condition");
    }

    @Override
    public <T> QueryMapper.MapperDeleteWhere gte(T value) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support gte condition");
    }

    @Override
    public <T> QueryMapper.MapperDeleteWhere lt(T value) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support lt condition");
    }

    @Override
    public <T> QueryMapper.MapperDeleteWhere lte(T value) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support lte condition");
    }

    @Override
    public <T> QueryMapper.MapperDeleteWhere between(T valueA, T valueB) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support between condition");
    }

    @Override
    public QueryMapper.MapperDeleteNotCondition not() {
        throw new UnsupportedOperationException("Key-value Mapper query does not support not condition");
    }

    @Override
    public QueryMapper.MapperDeleteNameCondition and(String name) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support and condition");
    }

    @Override
    public QueryMapper.MapperDeleteNameCondition or(String name) {
        throw new UnsupportedOperationException("Key-value Mapper query does not support or condition");
    }

    @Override
    public QueryMapper.MapperDeleteNameCondition where(String name) {
        requireNonNull(name, "name is required");
        if (!id.fieldName().equals(name)) {
            throw new UnsupportedOperationException("Key-value Mapper query only support the id attribute: " + id.name() + " at the entity: " + mapping.name());
        }
        this.name = name;
        return this;
    }

    @Override
    public <T> QueryMapper.MapperDeleteWhere eq(T value) {
        requireNonNull(value, "value is required");
        keys.add(ConverterUtil.getValue(value, mapping, name, converters));
        return this;
    }

    @Override
    public <T> QueryMapper.MapperDeleteWhere in(Iterable<T> values) {
        requireNonNull(values, "value is required");
        values.forEach(v -> keys.add(ConverterUtil.getValue(v, mapping, name, converters)));
        return this;
    }

    @Override
    public void execute() {
        validatedCondition();
        template.deleteByKeys(keys);
    }

    private void validatedCondition() {
        if (keys.isEmpty()) {
            throw new UnsupportedOperationException("On Key-value Mapper it requires to have at least one condition either eq or in");
        }
    }
}
