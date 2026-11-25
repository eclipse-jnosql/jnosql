/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.method;


import org.eclipse.jnosql.communication.query.ArrayQueryValue;
import org.eclipse.jnosql.communication.query.QueryValue;

import java.util.Arrays;

record MethodArrayValue(QueryValue<?>[] values) implements ArrayQueryValue {

    @Override
    public QueryValue<?>[] get() {
        return values;
    }

    static ArrayQueryValue of(String name) {
        return new MethodArrayValue(new QueryValue[] {new MethodParamQueryValue(name),
                new MethodParamQueryValue(name)});
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodArrayValue other)) {
            return false;
        }
        return Arrays.equals(this.values, other.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.values);
    }

    @Override
    public String toString() {
        return "{values=" + Arrays.toString(this.values) + '}';
    }
}
