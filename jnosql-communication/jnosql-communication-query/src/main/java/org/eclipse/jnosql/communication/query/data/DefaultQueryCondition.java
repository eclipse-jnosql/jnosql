/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryPath;
import org.eclipse.jnosql.communication.query.QueryValue;

/**
 * Default immutable representation of a query condition.
 *
 * @param leftOperand the attribute or function on the left
 * @param condition the condition operator
 * @param rightOperand the operand on the right
 */
public record DefaultQueryCondition(QueryValue<?> leftOperand, Condition condition, QueryValue<?> rightOperand)
        implements QueryCondition {

    /**
     * Creates a condition whose left operand is the attribute with the given name.
     *
     * @param name the attribute name
     * @param condition the condition operator
     * @param rightOperand the operand on the right
     */
    public DefaultQueryCondition(String name, Condition condition, QueryValue<?> rightOperand) {
        this(QueryPath.of(name), condition, rightOperand);
    }

    public DefaultQueryCondition {
        java.util.Objects.requireNonNull(leftOperand, "leftOperand is required");
        java.util.Objects.requireNonNull(condition, "condition is required");
        java.util.Objects.requireNonNull(rightOperand, "rightOperand is required");
    }

    @Override
    public String toString() {
        return leftOperand + " " + condition + " " + rightOperand;
    }
}
