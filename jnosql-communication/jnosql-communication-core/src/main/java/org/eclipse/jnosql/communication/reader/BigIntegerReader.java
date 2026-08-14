/*
 *
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
 *   Otavio Santana
 *
 */

package org.eclipse.jnosql.communication.reader;


import org.eclipse.jnosql.communication.ValueReader;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Reads and converts values to {@link BigInteger}. {@link BigInteger} values are returned unchanged,
 * {@link BigDecimal} values retain their arbitrary precision, other {@link Number} values use
 * {@link Number#longValue()}, and remaining values are parsed from their string representation.
 *
 */
public final class BigIntegerReader implements ValueReader {

    @Override
    public boolean test(Class<?> type) {
        return BigInteger.class.equals(type);
    }

    @Override
    public <T> T read(Class<T> type, Object value) {

        if (BigInteger.class.isInstance(value)) {
            return (T) value;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return (T) bigDecimal.toBigInteger();
        }
        if (value instanceof Number number) {
            return (T) BigInteger.valueOf(number.longValue());
        } else {
            return (T) new BigInteger(value.toString());
        }
    }
}
