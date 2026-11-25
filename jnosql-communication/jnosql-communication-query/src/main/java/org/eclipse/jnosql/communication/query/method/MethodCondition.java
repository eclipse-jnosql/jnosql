/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */

package org.eclipse.jnosql.communication.query.method;

import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.query.ConditionQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryPath;
import org.eclipse.jnosql.communication.query.QueryValue;

import java.util.Objects;

final class MethodCondition implements QueryCondition {

    private final String name;

    private final Condition condition;

    private final QueryValue<?> value;

    MethodCondition(String name, Condition condition) {
        this.name = name;
        this.condition = condition;
        this.value = new MethodParamQueryValue(name);
    }

    MethodCondition(String name, Condition condition, QueryValue<?> value) {
        this.name = name;
        this.condition = condition;
        this.value = value;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Condition condition() {
        return condition;
    }

    @Override
    public QueryValue<?> leftOperand() {
        if (value instanceof ConditionQueryValue) {
            throw new UnsupportedOperationException("A logical condition has no left operand: " + condition);
        }
        return QueryPath.of(name);
    }

    @Override
    public QueryValue<?> rightOperand() {
        return value;
    }

    @Override
    public String toString() {
        return name + " " + condition + " " + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MethodCondition that = (MethodCondition) o;
        return Objects.equals(name, that.name)
                && condition == that.condition
                && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, condition, value);
    }
}
