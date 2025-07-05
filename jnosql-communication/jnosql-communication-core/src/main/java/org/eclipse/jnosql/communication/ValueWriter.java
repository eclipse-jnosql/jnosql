/*
 *
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.communication;

import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This interface represents the writer on the {@link Value} instance.
 * Before sending the information to the NoSQL database, it will load the implementations from SPI,
 * Java Service Provider, and write to the proper format.
 * The {@link Predicate} verifies if the writer has the support of instance from this class.
 *
 * @param <T> current type
 * @param <S> the converted type
 */
public interface ValueWriter<T, S> extends Predicate<Class<?>> {

    /**
     * Converts a specific structure to a new one.
     *
     * @param object the instance to be converted
     * @return a new instance with the new class
     */
    S write(T object);

}
