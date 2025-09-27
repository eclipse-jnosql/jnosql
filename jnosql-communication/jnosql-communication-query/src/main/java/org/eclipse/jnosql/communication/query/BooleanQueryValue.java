/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
 * Boolean is a data type that has one of two possible values (usually denoted true and false) which is intended to
 * represent the two truth values of logic and Boolean algebra.
 */
public record BooleanQueryValue(boolean value) implements QueryValue<Boolean> {

    public static final BooleanQueryValue TRUE = new BooleanQueryValue(true);
    public static final BooleanQueryValue FALSE = new BooleanQueryValue(false);

    @Override
    public ValueType type() {
        return ValueType.BOOLEAN;
    }

    @Override
    public Boolean get() {
        return value;
    }

}
