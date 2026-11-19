/*
 *
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.communication;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Carries full generic type information, such as {@code List<String>}, which is otherwise lost to type erasure.
 * Create an anonymous subclass and pass it to {@link Value#get(TypeSupplier)}; the result is converted by the
 * {@link TypeReferenceReader} that supports the type, and a type with no reader throws
 * {@link UnsupportedOperationException}.
 *
 * <pre>{@code
 * List<String> names = Value.of(List.of("Ada", "Grace"))
 *         .get(new TypeReference<List<String>>() {});
 * }</pre>
 *
 * @param <T> the generic type to be captured
 */
public abstract class TypeReference<T> implements TypeSupplier<T> {

    private final Type type;

    protected TypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class<?>) { // sanity check, should never happen
            throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
        }
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    /**
     * Returns the captured generic type.
     *
     * @return the type
     */
    @Override
    public Type get() {
        return type;
    }

    @Override
    public String toString() {
        return "TypeReference{" +
                "type=" + type +
                '}';
    }
}
