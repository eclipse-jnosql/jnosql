/*
 *
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import org.eclipse.jnosql.communication.CommunicationException;
import org.eclipse.jnosql.communication.ValueReader;

import java.util.UUID;

/**
 * Converts values to {@link java.util.UUID} instances.
 */
public class UUIDValueReader implements ValueReader {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T read(Class<T> type, Object value) {
        if (value instanceof UUID) {
            return (T) value;
        }
        if (value instanceof CharSequence) {
            return (T) getUuid(value);
        }
        return null;
    }

    private static UUID getUuid(Object value) {
        try {
            return UUID.fromString(value.toString());
        } catch (IllegalArgumentException exp) {
            throw new CommunicationException("There is an error to convert to UUID, because the value is not UUID format: " + value, exp);
        }
    }

    @Override
    public boolean test(Class<?> type) {
        return UUID.class.equals(type);
    }
}
