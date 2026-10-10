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
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.util.ConverterUtil;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

import java.util.List;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

final class MapperVectorSearch<T>
        implements VectorSearch.MapperFrom<T>,
        VectorSearch.MapperVector<T>,
        VectorSearch.MapperNameCondition<T>,
        VectorSearch.MapperWhere<T>,
        VectorSearch.MapperThreshold<T>,
        VectorSearch.MapperLimit<T> {

    private final Class<T> entityClass;

    private final EntityMetadata mapping;

    private final Converters converters;

    private final DefaultVectorTemplate template;

    private Vector vector;

    private Float threshold;

    private Limit limit;

    private String name;

    private CriteriaCondition condition;

    private boolean and;

    MapperVectorSearch(Class<T> entityClass,
                       EntityMetadata mapping,
                       Converters converters,
                       DefaultVectorTemplate template) {

        this.entityClass = requireNonNull(entityClass, "entityClass is required");
        this.mapping = requireNonNull(mapping, "mapping is required");
        this.converters = requireNonNull(converters, "converters is required");
        this.template = requireNonNull(template, "template is required");

        mapping.inheritance().ifPresent(inheritance -> {
            if (!inheritance.parent().equals(mapping.type())) {
                this.condition = CriteriaCondition.eq(
                        Element.of(
                                inheritance.discriminatorColumn(),
                                inheritance.discriminatorValue()
                        )
                );
                this.and = true;
            }
        });
    }

    @Override
    public VectorSearch.MapperVector<T> vector(Vector vector) {
        this.vector = requireNonNull(vector, "vector is required");
        return this;
    }

    @Override
    public VectorSearch.MapperNameCondition<T> where(String name) {
        this.name = requireNonNull(name, "name is required");
        this.and = true;
        return this;
    }

    @Override
    public VectorSearch.MapperNameCondition<T> and(String name) {
        this.name = requireNonNull(name, "name is required");
        this.and = true;
        return this;
    }

    @Override
    public VectorSearch.MapperNameCondition<T> or(String name) {
        this.name = requireNonNull(name, "name is required");
        this.and = false;
        return this;
    }

    @Override
    public VectorSearch.MapperWhere<T> eq(Object value) {
        requireNonNull(value, "value is required");

        appendCondition(CriteriaCondition.eq(
                Element.of(
                        mapping.columnField(name),
                        getValue(value)
                )
        ));

        return this;
    }

    @Override
    public VectorSearch.MapperWhere<T> ne(Object value) {
        requireNonNull(value, "value is required");

        appendCondition(
                CriteriaCondition.eq(
                        Element.of(
                                mapping.columnField(name),
                                getValue(value)
                        )
                ).negate()
        );

        return this;
    }

    @Override
    public VectorSearch.MapperWhere<T> gt(Object value) {
        requireNonNull(value, "value is required");

        appendCondition(CriteriaCondition.gt(
                Element.of(
                        mapping.columnField(name),
                        getValue(value)
                )
        ));

        return this;
    }

    @Override
    public VectorSearch.MapperWhere<T> gte(Object value) {
        requireNonNull(value, "value is required");

        appendCondition(CriteriaCondition.gte(
                Element.of(
                        mapping.columnField(name),
                        getValue(value)
                )
        ));

        return this;
    }

    @Override
    public VectorSearch.MapperWhere<T> lt(Object value) {
        requireNonNull(value, "value is required");

        appendCondition(CriteriaCondition.lt(
                Element.of(
                        mapping.columnField(name),
                        getValue(value)
                )
        ));

        return this;
    }

    @Override
    public VectorSearch.MapperWhere<T> lte(Object value) {
        requireNonNull(value, "value is required");

        appendCondition(CriteriaCondition.lte(
                Element.of(
                        mapping.columnField(name),
                        getValue(value)
                )
        ));

        return this;
    }

    @Override
    public VectorSearch.MapperThreshold<T> threshold(float threshold) {
        if (!Float.isFinite(threshold)) {
            throw new IllegalArgumentException("threshold must be finite");
        }

        this.threshold = threshold;
        return this;
    }

    @Override
    public VectorSearch.MapperLimit<T> limit(Limit limit) {
        this.limit = requireNonNull(limit, "limit is required");
        return this;
    }

    @Override
    public List<T> result() {
        VectorSelectQuery query = new DefaultVectorSelectQuery(
                mapping.name(),
                condition,
                limit,
                vector,
                threshold
        );

        return template.search(entityClass, query);
    }

    private void appendCondition(CriteriaCondition incomingCondition) {
        if (nonNull(condition)) {
            condition = and
                    ? condition.and(incomingCondition)
                    : condition.or(incomingCondition);
        } else {
            condition = incomingCondition;
        }

        name = null;
    }

    private Object getValue(Object value) {
        return ConverterUtil.getValue(
                value,
                mapping,
                name,
                converters
        );
    }
}