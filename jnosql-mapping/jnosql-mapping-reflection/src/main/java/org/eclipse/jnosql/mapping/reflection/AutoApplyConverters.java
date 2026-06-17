/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.reflection;

import jakarta.nosql.AttributeConverter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

final class AutoApplyConverters {

    private static final Logger LOGGER = Logger.getLogger(AutoApplyConverters.class.getName());


    private final Map<Class<?>, Class<? extends AttributeConverter<?,?>>> converters;

    AutoApplyConverters() {
        this.converters = new HashMap<>();

        for (Class<? extends AttributeConverter<?, ?>> converter : ClassGraphClassScanner.INSTANCE.autoApplyConverters()) {
            converters.put(attributeType(converter), converter);
        }

        LOGGER.fine(() -> "Auto apply converters found, the auto apply quantity: " + converters.size());
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
