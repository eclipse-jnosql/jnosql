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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntityPostPersistTest {

    @Test
    void shouldGet() {
        Object value = new Object();
        EntityPostPersist entity = new EntityPostPersist(value);
        assertEquals(value, entity.get());
    }

    @Test
    void shouldEqualsAndHashCode() {
        Object value1 = new Object();
        Object value2 = new Object();

        EntityPostPersist entity1 = new EntityPostPersist(value1);
        EntityPostPersist entity2 = new EntityPostPersist(value1);
        EntityPostPersist entity3 = new EntityPostPersist(value2);

        assertEquals(entity1, entity1);
        assertEquals(entity1, entity2);
        assertEquals(entity2, entity1);
        assertEquals(entity1, entity2);
        assertNotEquals(entity1, null);
    }

    @Test
    void shouldToString() {
        Object value = new Object();
        EntityPostPersist entity = new EntityPostPersist(value);
        String expected = "EntityPostPersist{value=" + value + "}";
        assertEquals(expected, entity.toString());
    }

    @Test
    void shouldOf() {
        Object value = new Object();
        EntityPostPersist entity = EntityPostPersist.of(value);
        assertEquals(value, entity.get());
    }

    @Test
    void shouldOfWithNullValue() {
        assertThrows(NullPointerException.class, () -> EntityPostPersist.of(null));
    }
}
