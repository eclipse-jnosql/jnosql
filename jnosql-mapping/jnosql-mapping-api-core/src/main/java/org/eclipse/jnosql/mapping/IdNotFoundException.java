/*
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
 */
package org.eclipse.jnosql.mapping;

import jakarta.nosql.Id;
import jakarta.nosql.NoSQLException;

import java.util.function.Supplier;

/**
 * When The Entity is converted to communication layer,
 * this entity must have a field with {@link Id} annotation. If this entity
 * hasn't this information an exception will be launched.
 */
public class IdNotFoundException extends NoSQLException {

    public static final Supplier<IdNotFoundException> KEY_NOT_FOUND_EXCEPTION_SUPPLIER = ()
            -> new IdNotFoundException("To use this resource you must annotated a field with @Id");
    /**
     * New exception instance with the exception message
     *
     * @param message the exception message
     */
    public IdNotFoundException(String message) {
        super(message);
    }


    /**
     * Creates an exception for a type without identifier metadata.
     *
     * @param type the entity type
     * @return the exception
     */
    public static IdNotFoundException newInstance(Class<?> type) {
        String message = "The entity " + type.getName() + " must have a field annotated with @Id";
        return new IdNotFoundException(message);
    }
}
