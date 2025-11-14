/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.reflection.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionRepositorySupplierTest {

    private ReflectionRepositorySupplier supplier = new ReflectionRepositorySupplier();

    @Test
    @DisplayName("Should return an error when its not an interface")
    void shouldReturnErrorWhenItsNotAnInterface() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> supplier.apply(ReflectionRepositorySupplierTest.class));
        assertEquals("The type " + ReflectionRepositorySupplierTest.class.getName() + " is not an interface",
                exception.getMessage());
    }

    @Test
    @DisplayName("Should return an error when its null")
    void shouldReturnErrorWhenItsNull() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> supplier.apply(null));
        assertEquals("type is required", exception.getMessage());
    }

    //the test list
    //should get the entity
    //should get the

}