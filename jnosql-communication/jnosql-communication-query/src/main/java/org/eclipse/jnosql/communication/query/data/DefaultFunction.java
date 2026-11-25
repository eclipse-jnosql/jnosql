/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Matheus Oliveira
 */
package org.eclipse.jnosql.communication.query.data;

import org.eclipse.jnosql.communication.query.Function;

import java.util.Arrays;
import java.util.Objects;

/**
 * The default implementation of {@link Function}.
 *
 * @param name   the function name (e.g., {@code "UPPER"})
 * @param params the function parameters
 */
record DefaultFunction(String name, Object... params) implements Function {

    public DefaultFunction {
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(params, "params is required");
    }

    @Override
    public String toString() {
        return name + "(" + Arrays.toString(params) + ")";
    }
}
