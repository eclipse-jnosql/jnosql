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

import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.ValueReader;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code ArrayReader} is a {@link ValueReader} that reads an array of
 * elements.
 *
 */
public final class ArrayReader implements ValueReader {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T read(Class<T> type, Object value) {
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return (T) value;
        }
        if (value instanceof Iterable<?>) {
            List<Object> items = new ArrayList<>();
            Iterable.class.cast(value).forEach(items::add);
            return convert(type, items);
        }
        if (value.getClass().isArray()) {
            List<Object> items = new ArrayList<>();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                items.add(Array.get(value, i));
            }
            return convert(type, items);
        }
        Class<?> componentType = type.getComponentType();
        Object array = Array.newInstance(componentType, 1);

        Array.set(array, 0, Value.of(value).get(componentType));
        return (T) array;
    }

    @Override
    public boolean test(Class<?> type) {
        return type.isArray();
    }

    @SuppressWarnings("unchecked")
    private <T> T convert(Class<T> type, List<Object> items) {
        Class<?> componentType = type.getComponentType();
        Object array = Array.newInstance(componentType, items.size());
        for (int index = 0; index < items.size(); index++) {
            var value = Value.of(items.get(index));
            Object item = value.get(componentType);
            Array.set(array, index, item);
        }
        return (T) array;
    }

}
