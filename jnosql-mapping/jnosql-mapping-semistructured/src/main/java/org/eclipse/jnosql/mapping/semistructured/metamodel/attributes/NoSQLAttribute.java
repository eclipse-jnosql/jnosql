/*
 *  Copyright (c) 2024 Otávio Santana and others
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
package org.eclipse.jnosql.mapping.semistructured.metamodel.attributes;

import jakarta.data.Sort;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;

import java.util.List;
import java.util.Objects;

/**
 * A representation of an attribute in a NoSQL entity
 *
 * @param <T> the type of the attribute
 */
public record NoSQLAttribute<T>(String name) implements BooleanAttribute<T>, CriteriaAttribute<T>, StringAttribute<T> {
    @Override
    public Sort<T> asc() {
        return Sort.asc(name);
    }

    @Override
    public Sort<T> desc() {
        return Sort.desc(name);
    }

    @Override
    public CriteriaCondition isTrue() {
        return CriteriaCondition.eq(name, true);
    }

    @Override
    public CriteriaCondition isFalse() {
        return CriteriaCondition.eq(name, false);
    }

    @Override
    public CriteriaCondition eq(Object value) {
        Objects.requireNonNull(value, "value is required");
        return CriteriaCondition.eq(name, value);
    }

    @Override
    public CriteriaCondition gt(Object value) {
        Objects.requireNonNull(value, "value is required");
        return CriteriaCondition.gt(name, value);
    }

    @Override
    public CriteriaCondition gte(Object value) {
        Objects.requireNonNull(value, "value is required");
        return CriteriaCondition.gte(name, value);
    }

    @Override
    public CriteriaCondition lt(Object value) {
        Objects.requireNonNull(value, "value is required");
        return CriteriaCondition.lt(name, value);
    }

    @Override
    public CriteriaCondition lte(Object value) {
        Objects.requireNonNull(value, "value is required");
        return CriteriaCondition.lte(name, value);
    }

    @Override
    public CriteriaCondition between(Object valueA, Object valueB) {
        Objects.requireNonNull(valueA, "valueA is required");
        Objects.requireNonNull(valueB, "valueB is required");
        return CriteriaCondition.between(name, List.of(valueA, valueB));
    }

    @Override
    public CriteriaCondition like(String value) {
        Objects.requireNonNull(value, "value is required");
        return CriteriaCondition.like(name, value);
    }
}