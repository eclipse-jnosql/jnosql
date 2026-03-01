/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Matheus Oliveira
 */
package org.eclipse.jnosql.communication.query;

import java.util.Objects;

/**
 * A query value that represents a function.
 */
public record FunctionQueryValue(Function function) implements QueryValue<Function> {

    public FunctionQueryValue {
        Objects.requireNonNull(function, "function is required");
    }

    @Override
    public Function get() {
        return function;
    }

    @Override
    public ValueType type() {
        return ValueType.FUNCTION;
    }

    @Override
    public String toString() {
        return function.toString();
    }
}
