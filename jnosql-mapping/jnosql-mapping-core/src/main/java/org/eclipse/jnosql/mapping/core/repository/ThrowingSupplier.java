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
 *   Georg Leber
 */
package org.eclipse.jnosql.mapping.core.repository;

/**
 * Supplier contract whose operation may throw an exception.
 *
 * @param <T> the supplied value type
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {

    /**
     * Gets a supplied value.
     *
     * @return the supplied value
     * @throws Throwable when supplying the value fails
     */
    T get() throws Throwable;
}
