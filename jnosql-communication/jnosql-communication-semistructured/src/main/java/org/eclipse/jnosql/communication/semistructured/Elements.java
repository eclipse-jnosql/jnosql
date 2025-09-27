/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;



import org.eclipse.jnosql.communication.Value;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * Utility class for creating and manipulating {@link Element} instances.
 */
public final class Elements {

    private static final Predicate<Map.Entry<String, ?>> IS_VALUE_NULL = e -> Objects.nonNull(e.getValue());

    private Elements() {
    }

    /**
     * Creates a new element with the specified name and value.
     *
     * @param name  the name of the element
     * @param value the value of the element
     * @return a new element instance
     * @throws NullPointerException if the specified name is {@code null}
     */
    public static Element of(String name, Object value) {
        return Element.of(name, Value.of(value));
    }

    /**
     * Converts a map to a list of elements, where each entry in the map represents an element.
     * Only entries with non-null values will be included.
     *
     * @param values the map to be converted
     * @return a list of elements representing the columns in the map
     * @throws NullPointerException if the specified map is {@code null}
     */
    public static List<Element> of(Map<String, ?> values) {
        Objects.requireNonNull(values, "values is required");
        return values.entrySet().stream()
                .filter(IS_VALUE_NULL)
                .map(e -> Element.of(e.getKey(), getValue(e.getValue())))
                .collect(toList());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object getValue(Object value) {

        if (value instanceof Map) {
            List list = of(Map.class.cast(value));
            if(list.size() == 1) {
                return list.getFirst();
            }
            return list;
        }
        if (value instanceof Iterable) {
            return stream(Iterable.class.cast(value).spliterator(), false)
                    .map(Elements::getValue).collect(toList());
        }
        return value;
    }
}
