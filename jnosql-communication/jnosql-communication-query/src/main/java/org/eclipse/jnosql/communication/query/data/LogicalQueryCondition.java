/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.data;

import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.query.ConditionQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryValue;

import java.util.Objects;

/**
 * A logical predicate ({@code AND}, {@code OR}, {@code NOT}). It has no left operand; its right operand holds the
 * nested conditions.
 *
 * @param condition the logical operator
 * @param rightOperand the nested conditions
 */
record LogicalQueryCondition(Condition condition, ConditionQueryValue rightOperand) implements QueryCondition {

    LogicalQueryCondition {
        Objects.requireNonNull(condition, "condition is required");
        Objects.requireNonNull(rightOperand, "rightOperand is required");
    }

    @Override
    public QueryValue<?> leftOperand() {
        throw new UnsupportedOperationException("A logical condition has no left operand: " + condition);
    }

    @Override
    public String name() {
        return condition.getNameField();
    }

    @Override
    public String toString() {
        return condition + " " + rightOperand;
    }
}
