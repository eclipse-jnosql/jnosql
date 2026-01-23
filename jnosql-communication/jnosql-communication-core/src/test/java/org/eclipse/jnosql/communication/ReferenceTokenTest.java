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

import static org.assertj.core.api.Assertions.*;

class ReferenceTokenTest {

    @Test
    void shouldCreateReferenceTokenWithValue() {
        ReferenceToken token = new ReferenceToken("status");

        assertThat(token.value()).isEqualTo("status");
    }

    @Test
    void shouldCreateReferenceTokenUsingFactoryMethod() {
        ReferenceToken token = ReferenceToken.of("priority");

        assertThat(token.value()).isEqualTo("priority");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenValueIsNullInConstructor() {
        assertThatThrownBy(() -> new ReferenceToken(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("value must not be null");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenValueIsNullInFactoryMethod() {
        assertThatThrownBy(() -> ReferenceToken.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("value must not be null");
    }

    @Test
    void shouldBeEqualWhenValuesAreEqual() {
        ReferenceToken first = new ReferenceToken("name");
        ReferenceToken second = new ReferenceToken("name");

        assertThat(first)
                .isEqualTo(second)
                .hasSameHashCodeAs(second);
    }

    @Test
    void shouldNotBeEqualWhenValuesAreDifferent() {
        ReferenceToken first = new ReferenceToken("name");
        ReferenceToken second = new ReferenceToken("other");

        assertThat(first).isNotEqualTo(second);
    }
}