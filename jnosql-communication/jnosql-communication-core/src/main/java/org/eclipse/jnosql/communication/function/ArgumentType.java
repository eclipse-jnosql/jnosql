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
package org.eclipse.jnosql.communication.function;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * The kind of value a function parameter accepts, following the argument types of the Jakarta Query function table.
 * <p>
 * The kinds are strict: no implicit conversion between strings, numbers and booleans is part of a function
 * signature. For example {@code UPPER(123)} is invalid even though an integer could be converted to a string by
 * the {@link org.eclipse.jnosql.communication.Value} conversion mechanism. That conversion only applies to the
 * <em>result</em> of an evaluable function.
 */
public enum ArgumentType {

    /**
     * A character string, represented by {@link String}.
     */
    STRING {
        @Override
        boolean matches(Object value) {
            return value instanceof String;
        }
    },

    /**
     * An integral number: {@link Byte}, {@link Short}, {@link Integer}, {@link Long} or {@link BigInteger}.
     * Fractional numbers are never accepted and are never truncated.
     */
    INTEGER {
        @Override
        boolean matches(Object value) {
            return value instanceof Byte || value instanceof Short || value instanceof Integer
                    || value instanceof Long || value instanceof BigInteger;
        }
    },

    /**
     * Any numeric type: the {@link #INTEGER} types plus {@link Float}, {@link Double} and {@link BigDecimal}.
     * Strings are never accepted. Other {@link Number} implementations are an outstanding design decision.
     */
    NUMERIC {
        @Override
        boolean matches(Object value) {
            return INTEGER.matches(value) || value instanceof Float || value instanceof Double
                    || value instanceof BigDecimal;
        }
    };

    /**
     * Checks whether a known Java value satisfies this argument type.
     * A {@code null} value is accepted because null follows null-propagation semantics at evaluation time.
     *
     * @param value the value, possibly {@code null}
     * @return {@code true} when the value is {@code null} or has a supported Java type
     */
    public final boolean accepts(Object value) {
        return value == null || matches(value);
    }

    abstract boolean matches(Object value);
}
