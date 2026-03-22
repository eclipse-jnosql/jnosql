/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *   Matheus Oliveira
 */
package org.eclipse.jnosql.mapping.semistructured;

import java.util.Objects;

/**
 * Represents a scalar function expression that can be applied to entity fields in queries.
 * Function expressions allow operations such as UPPER, LOWER, LEFT, RIGHT, LENGTH, and ABS
 * to be used in the fluent query API.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * List<Word> words = template.select(Word.class)
 *         .where(Function.upper("term"))
 *         .eq("JAVA")
 *         .result();
 *
 * List<Word> result = template.select(Word.class)
 *         .where(Function.left("term", 2)).eq("Ja")
 *         .and(Function.length("term")).gt(5)
 *         .result();
 * }</pre>
 *
 * <p>When a function is not supported by the underlying database, an
 * {@link UnsupportedFunctionException} may be thrown by the database driver.
 * Drivers that support scalar functions should override
 * {@link AbstractSemiStructuredTemplate#checkFunctionSupport(org.eclipse.jnosql.mapping.semistructured.Function)}.</p>
 *
 * @since 1.1.0
 */
public interface Function {

    /**
     * Returns the name of the function (e.g., {@code "UPPER"}, {@code "LEFT"}, {@code "ABS"}).
     *
     * @return the function name, never {@code null}
     */
    String name();

    /**
     * Returns the entity field name this function operates on.
     *
     * @return the field name, never {@code null}
     */
    String field();

    /**
     * Returns additional arguments passed to this function.
     * For single-argument functions (e.g., UPPER, LOWER), this returns an empty array.
     *
     * @return an array of function arguments, never {@code null}
     */
    Object[] arguments();

    /**
     * Creates a {@code LEFT(field, length)} function expression.
     *
     * @param field  the entity field name
     * @param length the number of characters to extract from the left
     * @return a LEFT function expression
     * @throws NullPointerException     if {@code field} is {@code null}
     * @throws IllegalArgumentException if {@code length} is negative
     */
    static Function left(String field, int length) {
        Objects.requireNonNull(field, "field is required");
        if (length < 0) {
            throw new IllegalArgumentException("length must be non-negative");
        }
        return new DefaultFunction("LEFT", field, length);
    }

    /**
     * Creates a {@code RIGHT(field, length)} function expression.
     *
     * @param field  the entity field name
     * @param length the number of characters to extract from the right
     * @return a RIGHT function expression
     * @throws NullPointerException     if {@code field} is {@code null}
     * @throws IllegalArgumentException if {@code length} is negative
     */
    static Function right(String field, int length) {
        Objects.requireNonNull(field, "field is required");
        if (length < 0) {
            throw new IllegalArgumentException("length must be non-negative");
        }
        return new DefaultFunction("RIGHT", field, length);
    }

    /**
     * Creates an {@code UPPER(field)} function expression.
     *
     * @param field the entity field name
     * @return an UPPER function expression
     * @throws NullPointerException if {@code field} is {@code null}
     */
    static Function upper(String field) {
        Objects.requireNonNull(field, "field is required");
        return new DefaultFunction("UPPER", field);
    }

    /**
     * Creates a {@code LOWER(field)} function expression.
     *
     * @param field the entity field name
     * @return a LOWER function expression
     * @throws NullPointerException if {@code field} is {@code null}
     */
    static Function lower(String field) {
        Objects.requireNonNull(field, "field is required");
        return new DefaultFunction("LOWER", field);
    }

    /**
     * Creates a {@code LENGTH(field)} function expression.
     *
     * @param field the entity field name
     * @return a LENGTH function expression
     * @throws NullPointerException if {@code field} is {@code null}
     */
    static Function length(String field) {
        Objects.requireNonNull(field, "field is required");
        return new DefaultFunction("LENGTH", field);
    }

    /**
     * Creates an {@code ABS(field)} function expression.
     *
     * @param field the entity field name
     * @return an ABS function expression
     * @throws NullPointerException if {@code field} is {@code null}
     */
    static Function abs(String field) {
        Objects.requireNonNull(field, "field is required");
        return new DefaultFunction("ABS", field);
    }

    /**
     * Default immutable implementation of {@link Function} using a Java record.
     *
     * @param name      the function name
     * @param field     the entity field name
     * @param arguments optional additional arguments
     */
    record DefaultFunction(String name, String field, Object... arguments) implements Function {

        public DefaultFunction {
            Objects.requireNonNull(name, "name is required");
            Objects.requireNonNull(field, "field is required");
            arguments = arguments == null ? new Object[0] : arguments.clone();
        }

        @Override
        public Object[] arguments() {
            return arguments.clone();
        }

        @Override
        public String toString() {
            var sb = new StringBuilder(name).append('(').append(field);
            for (var arg : arguments) {
                sb.append(", ").append(arg);
            }
            return sb.append(')').toString();
        }
    }
}
