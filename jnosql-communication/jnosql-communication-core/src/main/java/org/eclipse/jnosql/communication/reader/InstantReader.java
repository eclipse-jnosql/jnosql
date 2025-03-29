/*
 *
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

public class InstantReader implements ValueReader {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T read(Class<T> type, Object value) {
        return (T) getInstant(value);
    }

    @Override
    public boolean test(Class<?> aClass) {
        return Instant.class.isAssignableFrom(aClass);
    }

    private Instant getInstant(Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }

        if (value instanceof Calendar calendar) {
            return calendar.toInstant();
        }

        if (value instanceof Date date) {
            return date.toInstant();
        }

        if (value instanceof Number number) {
            return Instant.ofEpochMilli(number.longValue());
        }

        return Instant.parse(value.toString());
    }
}

