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
package org.eclipse.jnosql.communication.query;


import org.eclipse.jnosql.communication.Condition;

/**
 * A query predicate in an operand-oriented form:
 * <pre>
 * left operand → operator → right operand
 * </pre>
 * Both operands are {@link QueryValue}s whose {@link QueryValue#type()} gives their semantic meaning, never the
 * content of a string:
 * <ul>
 *   <li>{@link ValueType#PATH}: an attribute reference ({@link QueryPath});</li>
 *   <li>{@link ValueType#STRING}, {@link ValueType#NUMBER}, {@link ValueType#BOOLEAN}, {@link ValueType#ENUM}:
 *   literals;</li>
 *   <li>{@link ValueType#PARAMETER}: a named or positional parameter;</li>
 *   <li>{@link ValueType#TEMPORAL}: a temporal expression such as {@code LOCAL DATE};</li>
 *   <li>{@link ValueType#FUNCTION}: a structured {@link Function}, evaluable or native, see
 *   {@link Function#isNative()}.</li>
 * </ul>
 * Examples:
 * <pre>{@code
 * WHERE name = 'Otavio'                          -- PATH, STRING
 * WHERE name = UPPER(:name)                      -- PATH, FUNCTION (evaluable)
 * WHERE UPPER(name) = 'OTAVIO'                   -- FUNCTION (native), STRING
 * WHERE UPPER(name) = UPPER(LEFT(:name, 19))     -- FUNCTION (native), FUNCTION (evaluable)
 * }</pre>
 * Inspecting an operand never evaluates a function: this model is only a structured representation of the query.
 * Evaluation happens later, lazily and memoized per execution, when the communication layer exposes the value to
 * a driver. Being representable here does not mean being executable: native function predicates are not yet
 * supported by the communication API and are rejected explicitly when the query is converted.
 * <p>
 * Not every predicate is a binary comparison. Logical predicates ({@code AND}, {@code OR}, {@code NOT}) have no left
 * operand; their {@link #rightOperand()} is a {@link ConditionQueryValue} holding the nested conditions and
 * {@link #leftOperand()} throws {@link UnsupportedOperationException}. {@code IS NULL} uses a {@link NullQueryValue}
 * right operand, and {@code BETWEEN} and {@code IN} an {@link ArrayQueryValue}.
 *
 * @see #leftOperand()
 * @see #condition()
 * @see #rightOperand()
 */
public interface QueryCondition {

    /**
     * The left operand: an attribute ({@link ValueType#PATH}) or a structured function
     * ({@link ValueType#FUNCTION}).
     *
     * @return the left operand
     * @throws UnsupportedOperationException for logical predicates, which have no left operand
     */
    QueryValue<?> leftOperand();

    /**
     * The operator that defines how the operands are compared or combined.
     *
     * @return the operator
     */
    Condition condition();

    /**
     * The right operand: a literal, parameter, temporal expression, function, attribute or, for logical
     * predicates, a {@link ConditionQueryValue} with the nested conditions.
     *
     * @return the right operand
     */
    QueryValue<?> rightOperand();

    /**
     * Compatibility accessor derived from {@link #leftOperand()}.
     *
     * @return the attribute name when the left operand is an attribute
     * @throws UnsupportedOperationException when the left operand is not an attribute, such as a function,
     * or when the predicate is logical
     */
    default String name() {
        var left = leftOperand();
        if (left instanceof QueryPath path) {
            return path.get();
        }
        throw new UnsupportedOperationException("The left operand is not an attribute: " + left.type());
    }

    /**
     * Compatibility accessor equivalent to {@link #rightOperand()}.
     *
     * @return the right operand
     */
    default QueryValue<?> value() {
        return rightOperand();
    }
}
