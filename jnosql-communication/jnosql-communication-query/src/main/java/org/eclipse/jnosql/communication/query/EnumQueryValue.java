/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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


public record EnumQueryValue(Enum<?> value) implements QueryValue<Enum<?>> {

    @Override
    public Enum<?> get() {
        return value;
    }

    @Override
    public ValueType type() {
        return ValueType.STRING;
    }

    public static EnumQueryValue of(Enum<?> value) {
        return new EnumQueryValue(value);
    }

}