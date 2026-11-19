/*
 *
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
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
 *
 * <ul>
 *   <li>a {@link java.util.UUID} is returned as is;</li>
 *   <li>a {@link CharSequence} is parsed with {@link java.util.UUID#fromString(String)}, so it must use the
 *   canonical text form, and a malformed text throws {@link CommunicationException};</li>
 *   <li>any other type produces {@code null}.</li>
 * </ul>
 *
 * <p>A {@code null} {@link org.eclipse.jnosql.communication.Value} converts to {@code null} before reaching this
 * reader. There is no matching {@link org.eclipse.jnosql.communication.ValueWriter}: a {@link java.util.UUID} is
 * sent to the database unchanged, and how it is stored is up to the driver.</p>
 *
 * <pre>{@code
 * UUID id = Value.of("123e4567-e89b-12d3-a456-426614174000").get(UUID.class);
 * }</pre>
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
