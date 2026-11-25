/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */

package org.eclipse.jnosql.communication.query;

/**
 * The function is a structured representation of the activities or processes.
 */
public interface Function {

    /**
     * The function name
     * @return the name
     */
    String name();

    /**
     * The params of this function
     * @return the params
     */
    Object[] params();

    /**
     * Classifies this function by the meaning of its arguments, without evaluating it.
     * A function is native when any argument, at any depth, refers to an attribute of a stored record.
     * Literals, parameters and nested evaluable functions can be resolved independently of the database,
     * for example {@code UPPER(LEFT(:name, 19))}, while {@code UPPER(name)} or {@code LEFT(name, LENGTH(:prefix))}
     * require native execution.
     *
     * @return {@code true} when the function depends on a stored attribute
     */
    default boolean isNative() {
        for (Object param : params()) {
            if (param instanceof QueryPath) {
                return true;
            }
            if (param instanceof QueryValue<?> value && value.type() == ValueType.FUNCTION
                    && ((Function) value.get()).isNative()) {
                return true;
            }
        }
        return false;
    }
}
