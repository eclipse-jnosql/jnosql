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
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
class EntityPrePersistTest {

    @Test
    void shouldGet() {
        Object value = new Object();
        EntityPrePersist entity = new EntityPrePersist(value);
        assertEquals(value, entity.get());
    }

    @Test
    void shouldEqualsAndHashCode() {
        Object value1 = new Object();
        Object value2 = new Object();

        EntityPrePersist entity1 = new EntityPrePersist(value1);
        EntityPrePersist entity2 = new EntityPrePersist(value1);
        EntityPrePersist entity3 = new EntityPrePersist(value2);

        assertEquals(entity1, entity1);
        assertEquals(entity1, entity2);
        assertEquals(entity2, entity1);
        assertEquals(entity1, entity2);
        assertNotEquals(entity1, null);
    }

    @Test
    void shouldToString() {
        Object value = new Object();
        EntityPrePersist entity = new EntityPrePersist(value);
        String expected = "DefaultEntityPrePersist{value=" + value + "}";
        assertEquals(expected, entity.toString());
    }

    @Test
    void shouldOf() {
        Object value = new Object();
        EntityPrePersist entity = EntityPrePersist.of(value);
        assertEquals(value, entity.get());
    }

    @Test
    void shouldOfWithNullValue() {
        assertThrows(NullPointerException.class, () -> EntityPrePersist.of(null));
    }
}
