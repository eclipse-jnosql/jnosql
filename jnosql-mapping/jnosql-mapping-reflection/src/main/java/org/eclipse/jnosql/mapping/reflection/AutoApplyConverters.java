/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 */
package org.eclipse.jnosql.mapping.reflection;

import jakarta.nosql.AttributeConverter;
import jakarta.nosql.Convert;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

final class AutoApplyConverters {

    private static final Logger LOGGER = Logger.getLogger(AutoApplyConverters.class.getName());

    private final Map<Class<?>, Class<? extends AttributeConverter<?,?>>> converters;

    /**
     * Loads the converters from the {@link ClassScanner} on every creation instead of keeping them in a static
     * field, because the converters belong to the application, and in an application server one JNoSQL serves
     * several applications.
     */
    AutoApplyConverters() {
        this.converters = new HashMap<>();

        for (Class<? extends AttributeConverter<?, ?>> converter : ClassScanner.load().autoApplyConverters()) {
            converters.put(attributeType(converter), converter);
        }

        LOGGER.finest(() -> "Auto apply converters found, the auto apply quantity: " + converters.size());
    }


    Class<? extends AttributeConverter<?, ?>> converter(Convert convert, Class<?> type) {
        if (convert != null) {
            return convert.value();
        }
        return converters.get(type);
    }

    Optional<Class<? extends AttributeConverter<?, ?>>> getConverter(Class<?> type) {
        return Optional.ofNullable(converters.get(type));
    }

    private static Class<?> attributeType(Class<? extends AttributeConverter<?, ?>> converter) {

        for (Type type : converter.getGenericInterfaces()) {
            if (type instanceof ParameterizedType parameterizedType
                    && parameterizedType.getRawType().equals(AttributeConverter.class)) {

                Type argument = parameterizedType.getActualTypeArguments()[0];

                if (argument instanceof Class<?> clazz) {
                    return clazz;
                }
            }
        }

        throw new IllegalArgumentException(
                "Unable to determine attribute type from converter: "
                        + converter.getName());
    }

}
