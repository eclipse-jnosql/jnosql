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
 */
package org.eclipse.jnosql.mapping.semistructured;

import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.util.ConverterUtil;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

abstract class AbstractMapperQuery {

    protected final String entity;

    protected boolean negate;

    protected CriteriaCondition condition;

    protected boolean and;

    protected String name;

    protected transient final EntityMetadata mapping;

    protected transient final Converters converters;

    protected transient final SemiStructuredTemplate template;

    protected long start;

    protected long limit;


    AbstractMapperQuery(EntityMetadata mapping, Converters converters, SemiStructuredTemplate template) {
        this.mapping = mapping;
        this.converters = converters;
        this.entity = mapping.name();
        this.template = template;
        mapping.inheritance().ifPresent(i -> {
            if(!i.parent().equals(mapping.type())){
                this.condition = CriteriaCondition.eq(Element.of(i.discriminatorColumn(), i.discriminatorValue()));
                this.and = true;
            }
        });
    }

    protected void appendCondition(CriteriaCondition incomingCondition) {
        CriteriaCondition columnCondition = getCondition(incomingCondition);

        if (nonNull(condition)) {
            this.condition = and ? this.condition.and(columnCondition) : this.condition.or(columnCondition);
        } else {
            this.condition = columnCondition;
        }

        this.negate = false;
        this.name = null;
    }

    protected <T> void betweenImpl(T valueA, T valueB) {
        requireNonNull(valueA, "valueA is required");
        requireNonNull(valueB, "valueB is required");
        CriteriaCondition newCondition = CriteriaCondition
                .between(Element.of(mapping.columnField(name), asList(getValue(valueA), getValue(valueB))));
        appendCondition(newCondition);
    }


    protected <T> void inImpl(Iterable<T> values) {

        requireNonNull(values, "values is required");
        List<Object> convertedValues = StreamSupport.stream(values.spliterator(), false)
                .map(this::getValue).collect(toList());
        CriteriaCondition newCondition = CriteriaCondition
                .in(Element.of(mapping.columnField(name), convertedValues));
        appendCondition(newCondition);
    }

    protected <T> void eqImpl(T value) {
        requireNonNull(value, "value is required");

        CriteriaCondition newCondition = CriteriaCondition
                .eq(Element.of(mapping.columnField(name), getValue(value)));
        appendCondition(newCondition);
    }

    protected void likeImpl(String value) {
        requireNonNull(value, "value is required");
        CriteriaCondition newCondition = CriteriaCondition
                .like(Element.of(mapping.columnField(name), getValue(value)));
        appendCondition(newCondition);
    }

    protected <T> void gteImpl(T value) {
        requireNonNull(value, "value is required");
        CriteriaCondition newCondition = CriteriaCondition
                .gte(Element.of(mapping.columnField(name), getValue(value)));
        appendCondition(newCondition);
    }

    protected <T> void gtImpl(T value) {
        requireNonNull(value, "value is required");
        CriteriaCondition newCondition = CriteriaCondition
                .gt(Element.of(mapping.columnField(name), getValue(value)));
        appendCondition(newCondition);
    }

    protected <T> void ltImpl(T value) {
        requireNonNull(value, "value is required");
        CriteriaCondition newCondition = CriteriaCondition
                .lt(Element.of(mapping.columnField(name), getValue(value)));
        appendCondition(newCondition);
    }

    protected <T> void lteImpl(T value) {
        requireNonNull(value, "value is required");
        CriteriaCondition newCondition = CriteriaCondition
                .lte(Element.of(mapping.columnField(name), getValue(value)));
        appendCondition(newCondition);
    }

    protected Object getValue(Object value) {
        return ConverterUtil.getValue(value, mapping, name, converters);
    }

    private CriteriaCondition getCondition(CriteriaCondition newCondition) {
        if (negate) {
            return newCondition.negate();
        } else {
            return newCondition;
        }
    }
}
