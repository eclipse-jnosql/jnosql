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
 * Converts a Java value to the representation that is sent to the database.
 *
 * <p>When {@link ValueUtil} prepares a {@link Value} for a database, the first writer found through the Java
 * {@link java.util.ServiceLoader} whose {@link java.util.function.Predicate#test(Object) test} accepts the value's class converts it. The built-in
 * writers convert an {@link Enum} to its {@link Enum#name()}, a {@link java.time.temporal.Temporal} to its
 * {@code toString()} text, and an {@link java.util.Optional} to its content (or {@code null} when empty).
 * A value with no matching writer, such as a {@link java.util.UUID}, is passed unchanged.</p>
 *
 * <p>Implement this interface to change how an additional Java type is stored, for example in a database driver.
 * {@link java.util.function.Predicate#test(Object) test} must return {@code true} only for the types the writer can convert.</p>
 *
 * @param <T> the Java type to be converted
 * @param <S> the converted type sent to the database
 * @see Value
 * @see ValueReader
 */
public interface ValueWriter<T, S> extends Predicate<Class<?>> {

    /**
     * Converts the instance to the database representation.
     *
     * @param object the instance to be converted; not {@code null}
     * @return the converted instance
     */
    S write(T object);

}
