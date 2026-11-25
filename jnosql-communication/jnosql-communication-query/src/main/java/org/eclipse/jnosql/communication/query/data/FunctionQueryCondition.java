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
import org.eclipse.jnosql.communication.query.Function;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryValue;

import java.util.Objects;
import java.util.Optional;

/**
 * A condition whose left operand is a function, such as {@code UPPER(name) = 'X'}.
 * The textual name is kept only for display; the function stays structured.
 */
record FunctionQueryCondition(String name, Condition condition, QueryValue<?> value, Function function)
        implements QueryCondition {

    FunctionQueryCondition {
        Objects.requireNonNull(function, "function is required");
    }

    @Override
    public Optional<Function> leftFunction() {
        return Optional.of(function);
    }

    @Override
    public String toString() {
        return name + " " + condition + " " + value;
    }
}
