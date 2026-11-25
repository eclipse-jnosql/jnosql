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

import java.util.Optional;

/**
 * Condition performs different computations or actions depending on whether a boolean query
 * condition evaluates to true or false.
 * The conditions are composed of three elements.
 * The condition's name
 * The Operator
 * The Value
 *
 * @see QueryCondition#name() ()
 * @see QueryCondition#condition() ()
 * @see QueryCondition#value()
 */
public interface QueryCondition {

    /**
     * the data source or target, to apply the operator
     *
     * @return the name
     */
    String name();

    /**
     * that defines comparing process between the name and the value.
     *
     * @return the operator
     */
    Condition condition();

    /**
     * that data that receives the operation.
     *
     * @return the value
     */
    QueryValue<?> value();

    /**
     * The function used as the left operand of the condition, for example {@code UPPER(name) = 'X'}.
     * In that case {@link #name()} only holds the textual form of the expression and must not be used as an attribute.
     *
     * @return the left operand function, or empty when the left operand is an ordinary attribute
     */
    default Optional<Function> leftFunction() {
        return Optional.empty();
    }
}
