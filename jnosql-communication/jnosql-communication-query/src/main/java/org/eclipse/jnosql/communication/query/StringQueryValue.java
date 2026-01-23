/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import java.util.Objects;

/**
 * Represents a string literal used as a {@link QueryValue} in a query condition.
 * <p>
 * This type models textual values as <em>literals</em>, typically appearing on the right-hand side of a conditional
 * operator, for example:
 *
 * <pre>{@code
 * name = "Otavio"
 * }
 * </pre>
 * <p>
 * The value is treated as an immutable literal and does not represent a path, parameter, or expression.
 */
public final class StringQueryValue implements QueryValue<String> {

    private final String value;

    StringQueryValue(String value) {
        this.value = value;
    }

    @Override
    public String get() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StringQueryValue that)) {
            return false;
        }
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }

    @Override
    public ValueType type() {
        return ValueType.STRING;
    }

    public static StringQueryValue of(String text) {
        return new StringQueryValue(text);
    }


}