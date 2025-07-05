/*
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
 */
package org.eclipse.jnosql.mapping.core.util;

import jakarta.nosql.AttributeConverter;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;

public final class ConverterUtil {

    private static final Logger LOGGER = Logger.getLogger(ConverterUtil.class.getName());

    private ConverterUtil() {

    }

    /**
     * Converts the value to database format
     *
     * @param value      the value
     * @param mapping    the class mapped
     * @param name       the java fieldName
     * @param converters the collection of converter
     * @return the value converted
     */
    public static Object getValue(Object value, EntityMetadata mapping, String name, Converters converters) {
        Optional<FieldMetadata> fieldOptional = mapping.fieldMapping(name);
        if (fieldOptional.isPresent()) {
            FieldMetadata field = fieldOptional.get();
            return getValue(value, converters, field);
        }
        return value;
    }

    /**
     * Converts the value from the field with {@link FieldMetadata} to database format
     *
     * @param value      the value to be converted
     * @param converters the converter
     * @param field      the field
     * @return tje value converted
     */
    public static Object getValue(Object value, Converters converters, FieldMetadata field) {
        if (!field.type().equals(value.getClass())) {
            return field.converter()
                    .map(c -> converters.get(field))
                    .map(useConverter(value))
                    .orElseGet(getSupplier(value, field.type()));
        }

        return field.converter()
                .map(c -> converters.get(field))
                .map(useConverter(value))
                .orElse(value);
    }

    private static Supplier<Object> getSupplier(Object value, Class<?> type) {
        return () -> {
            if (Iterable.class.isAssignableFrom(type)) {
                return value;
            }
            try {
                return Value.of(value).get(type);
            } catch (UnsupportedOperationException ex) {
                LOGGER.fine(String.format("There is an error when try to convert the type %s to the type %s",
                        value, type));
                return value;
            }

        };
    }

    private static Function<AttributeConverter, Object> useConverter(Object value) {
        return a -> {
            if (isNative(value).test(a)) {
                return value;
            }
            return a.convertToDatabaseColumn(value);
        };
    }

    private static Predicate<AttributeConverter> isNative(Object value) {
        return a -> getGenericInterface(a).getActualTypeArguments()[1].equals(value.getClass());
    }

    private static ParameterizedType getGenericInterface(AttributeConverter a) {
        for (Type genericInterface : a.getClass().getGenericInterfaces()) {
            if (ParameterizedType.class.isAssignableFrom(genericInterface.getClass()) &&
                    ((ParameterizedType) genericInterface).getRawType().equals(AttributeConverter.class)) {
                return (ParameterizedType) genericInterface;
            }
        }
        throw new IllegalArgumentException("It does not found AttributeConverter implementation to this converter");
    }
}
