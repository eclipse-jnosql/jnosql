/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReferenceTokenTest {

    @Test
    void shouldCreateReferenceTokenWithValue() {
        ReferenceToken token = new ReferenceToken("status");

        assertEquals("status", token.value());
    }

    @Test
    void shouldCreateReferenceTokenUsingFactoryMethod() {
        ReferenceToken token = ReferenceToken.of("priority");

        assertEquals("priority", token.value());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenValueIsNullInConstructor() {
        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> new ReferenceToken(null));

        assertEquals("value must not be null", exception.getMessage());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenValueIsNullInFactoryMethod() {
        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> ReferenceToken.of(null));

        assertEquals("value must not be null", exception.getMessage());
    }

    @Test
    void shouldBeEqualWhenValuesAreEqual() {
        ReferenceToken first = new ReferenceToken("name");
        ReferenceToken second = new ReferenceToken("name");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenValuesAreDifferent() {
        ReferenceToken first = new ReferenceToken("name");
        ReferenceToken second = new ReferenceToken("other");

        assertNotEquals(first, second);
    }
}