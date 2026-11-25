/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 * and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;

import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.query.ArrayQueryValue;
import org.eclipse.jnosql.communication.query.EnumQueryValue;
import org.eclipse.jnosql.communication.query.ParamQueryValue;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.TemporalQueryValue;
import org.eclipse.jnosql.communication.query.ValueType;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

final class Values {

    private Values() {
    }

    static Object get(QueryValue<?> value, Params parameters) {
        return get(value, parameters, TemporalReference.live());
    }

    static Object get(QueryValue<?> value, Params parameters, TemporalReference reference) {

        ValueType type = value.type();
        switch (type) {
            case NUMBER, STRING, BOOLEAN -> {
                return value.get();
            }
            case PARAMETER -> {
                return parameters.add(((ParamQueryValue) value).get());
            }
            case ARRAY -> {
                return Stream.of(((ArrayQueryValue) value).get())
                        .map(v -> get(v, parameters, reference))
                        .collect(toList());
            }
            case ENUM -> {
                return ((EnumQueryValue) value).get().name();
            }
            case TEMPORAL -> {
                return new TemporalValue((TemporalQueryValue) value, reference);
            }
            case NULL -> {
                return null;
            }
            default -> throw new QueryException("There is not support to the value: " + type);
        }
    }
}