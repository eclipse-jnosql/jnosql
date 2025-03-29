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

import java.time.YearMonth;

/**
 * Class to reads and converts to {@link YearMonth}, first it verify if is YearMonth if yes return itself
 * otherwise convert to {@link String} and then {@link YearMonth}
 */
public final class YearMonthReader implements ValueReader {

    @Override
    public boolean test(Class<?> type) {
        return YearMonth.class.equals(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T read(Class<T> type, Object value) {
        if (value instanceof YearMonth) {
            return (T) value;
        }
        return (T) YearMonth.parse(value.toString());
    }
}
