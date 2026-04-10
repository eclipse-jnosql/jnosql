/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;

import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.query.ArrayQueryValue;
import org.eclipse.jnosql.communication.query.EnumQueryValue;
import org.eclipse.jnosql.communication.query.ParamQueryValue;
import org.eclipse.jnosql.communication.query.QueryPath;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.ValueType;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Utility class that provides methods for working with query values and parameters.
 * This class is not instantiable and serves as a container for static methods.
 * Its primary purpose is to process {@link QueryValue} objects and return the
 * appropriate values based on their types, as well as interact with {@link Params}.
 */
public final class Values {

    private Values() {
    }

    /**
     * Processes a {@link QueryValue} based on its type and returns the corresponding
     * processed object, interacting with {@link Params} when necessary.
     *
     * @param value the query value to be processed, whose type determines the specific logic to apply
     * @param parameters the parameter collection used to handle dynamic query parameters
     * @return the processed value, which could be the original value, a transformed value, or a reference token
     * @throws QueryException if the {@code value} type is unsupported
     */
    public static Object get(QueryValue<?> value, Params parameters) {

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
                        .map(v -> get(v, parameters))
                        .collect(toList());
            }
            case ENUM -> {
                return ((EnumQueryValue) value).get();
            }
            case NULL -> {
                return null;
            } case PATH -> {
                return new org.eclipse.jnosql.communication.ReferenceToken(((QueryPath) value).get());
            }
            default -> throw new QueryException("There is not support to the value: " + type);
        }
    }
}