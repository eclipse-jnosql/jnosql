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
 * Default immutable implementation of {@link Function} using a Java record.
 *
 * @param name      the function name
 * @param field     the entity field name
 * @param arguments optional additional arguments
 */
record DefaultFunction(String name, String field, Object... arguments) implements Function {

    DefaultFunction {
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
