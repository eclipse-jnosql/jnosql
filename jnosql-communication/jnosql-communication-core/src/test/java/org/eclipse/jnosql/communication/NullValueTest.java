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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NullValueTest {
    @Test
    void shouldReturnNullForGet() {
        assertNull(NullValue.INSTANCE.get());
    }

    @Test
    void shouldReturnNullForGetWithType() {
        assertNull(NullValue.INSTANCE.get(String.class));
    }

    @Test
    void shouldReturnNullForGetWithTypeSupplier() {
        assertNull(NullValue.INSTANCE.get(new TypeReference<>() {
        }));
    }

    @Test
    void shouldReturnFalseForIsInstanceOf() {
        assertFalse(NullValue.INSTANCE.isInstanceOf(String.class));
    }

    @Test
    void shouldReturnTrueForIsNull() {
        assertTrue(NullValue.INSTANCE.isNull());
    }
}