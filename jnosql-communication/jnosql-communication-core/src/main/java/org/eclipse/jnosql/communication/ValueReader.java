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


import java.util.function.Predicate;

/**
 * Converts a raw value, as returned by a database, to a requested Java type.
 *
 * <p>{@link Value#get(Class)} delegates to the readers found through the Java {@link java.util.ServiceLoader}
 * (see {@code module-info} {@code provides} or {@code META-INF/services}). The first reader whose
 * {@link java.util.function.Predicate#test(Object) test} accepts the requested type performs the conversion. A value that is already an instance of
 * the requested type is returned as is, without calling any reader. If no reader supports the type,
 * {@link Value#get(Class)} throws {@link UnsupportedOperationException}.</p>
 *
 * <p>Example, using the built-in readers:</p>
 * <pre>{@code
 * UUID id = Value.of("123e4567-e89b-12d3-a456-426614174000").get(UUID.class);
 * Integer age = Value.of("10").get(Integer.class);
 * }</pre>
 *
 * <p>Implement this interface to support an additional target type, for example in a database driver.
 * {@link java.util.function.Predicate#test(Object) test} must return {@code true} only for the types the reader can produce.</p>
 *
 * @see Value
 * @see Value#get(Class)
 * @see ValueWriter
 */
public interface ValueReader extends Predicate<Class<?>> {

    /**
     * Converts the value to the target type.
     *
     * <p>How a value that cannot be converted is handled is defined by each implementation. For example,
     * the UUID reader throws {@link CommunicationException} for malformed text.</p>
     *
     * @param type  the target class; a type accepted by {@link java.util.function.Predicate#test(Object) test}
     * @param value the instance to be converted
     * @param <T>   the target type
     * @return the converted instance
     */
    <T> T read(Class<T> type, Object value);

}
