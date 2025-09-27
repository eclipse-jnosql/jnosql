/*
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
 */
package org.eclipse.jnosql.communication;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * Utilitarian class to {@link Value}
 */
public final class ValueUtil {
    private static final String PARAM_CLASS_NAME = "org.eclipse.jnosql.communication.ParamValue";
    @SuppressWarnings("rawtypes")
    private static final ValueWriter VALUE_WRITER = ValueWriterDecorator.getInstance();
    private ValueUtil() {
    }

    /**
     * Converts a {@link Value} to Object
     *
     * @param value the value
     * @return an object converted
     */
    public static Object convert(Value value) {
        return convert(value, VALUE_WRITER);
    }

    /**
     * Converts a {@link Value} using a custom {@link ValueWriter}.
     *
     * @param value       the value to be converted
     * @param valueWriter the custom ValueWriter to use for conversion
     * @return the converted object
     */
    public static Object convert(Value value, ValueWriter<?, ?> valueWriter) {
        Objects.requireNonNull(value, "value is required");
        Objects.requireNonNull(valueWriter, "valueWriter is required");
        Object val = value.get();
        if (val instanceof Iterable) {
            return getObjects(val, valueWriter);
        }
        return getObject(val, valueWriter);
    }

    /**
     * Converts a {@link Value} to {@link List}
     *
     * @param value the value
     * @return a list object
     */
    public static List<Object> convertToList(Value value) {
        return convertToList(value, VALUE_WRITER);
    }

    /**
     * Converts a {@link Value} to {@link List} using a custom {@link ValueWriter}.
     *
     * @param value       the value
     * @param valueWriter the custom ValueWriter to use for conversion
     * @return a list object
     */
    @SuppressWarnings("unchecked")
    public static List<Object> convertToList(Value value, ValueWriter<?, ?> valueWriter) {
        Objects.requireNonNull(value, "value is required");
        Objects.requireNonNull(valueWriter, "valueWriter is required");
        Object val = value.get();
        if (val instanceof Iterable) {
            List<Object> items = new ArrayList<>();
            Iterable.class.cast(val).forEach(items::add);
            if (items.size() == 1) {
                Object item = items.getFirst();
                // check if it is dynamic params
                if (PARAM_CLASS_NAME.equals(item.getClass().getName())) {
                    Object params = Value.class.cast(item).get();
                    if (params instanceof Iterable) {
                        return getObjects(Iterable.class.cast(params), valueWriter);
                    } else {
                        return Collections.singletonList(getObject(params, valueWriter));
                    }
                }
            }
            return (List<Object>) items.stream().map(o -> getObject(o, valueWriter)).collect(toList());
        }
        return Collections.singletonList(getObject(val, valueWriter));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<Object> getObjects(Object val, ValueWriter<?, ?> valueWriter) {
        return (List<Object>) StreamSupport.stream(((Iterable) val).spliterator(), false)
                .map(o -> {
                    if (o instanceof Value) {
                        return convert((Value) o, valueWriter);
                    }
                    return getObject(o, valueWriter);
                })
                .collect(toList());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object getObject(Object val, ValueWriter valueWriter) {
        if (val == null) {
            return null;
        } else if (val instanceof Value) {
            return convert(Value.class.cast(val), valueWriter);
        } else if (valueWriter.test(val.getClass())) {
            return valueWriter.write(val);
        }
        return val;
    }
}

