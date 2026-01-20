/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query;

/**
 * Represents a state field path expression used as an operand in a query condition.
 * <p>
 * A {@code QueryPath} does <strong>not</strong> represent a literal value. Instead,
 * it refers to a field or attribute of the query source and is typically used when
 * comparing one field against another.
 *
 * <p>Examples:</p>
 *
 * <pre>{@code
 * numBitsRequired = floorOfSquareRoot
 * order.total > order.discount
 * }</pre>
 *
 * Path expressions are resolved against the query model and evaluated at runtime.
 * They must not be quoted, bound as parameters, or treated as constant values.
 *
 * @see ValueType#PATH
 */
public record QueryPath(String value) implements QueryValue<String>  {

    @Override
    public ValueType type() {
        return ValueType.PATH;
    }

    @Override
    public String get() {
        return value;
    }

    /**
     * Creates a {@code QueryPath} for the given path expression.
     *
     * @param path the state field path
     * @return a new {@code QueryPath}
     */
    public static QueryPath of(String path) {
        return new QueryPath(path);
    }
}
