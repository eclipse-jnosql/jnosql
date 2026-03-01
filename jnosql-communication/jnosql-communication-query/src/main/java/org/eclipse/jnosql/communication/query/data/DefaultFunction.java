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
package org.eclipse.jnosql.communication.query.data;

import org.eclipse.jnosql.communication.query.Function;

import java.util.Arrays;
import java.util.Objects;

/**
 * The default implementation of {@link Function}
 * @param name the function name
 * @param params the parameters
 */
public record DefaultFunction(String name, Object... params) implements Function {

    public DefaultFunction {
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(params, "params is required");
    }

    @Override
    public String toString() {
        return name + "(" + Arrays.toString(params) + ")";
    }
}
