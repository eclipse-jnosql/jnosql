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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReferenceTokenTest {

    @Nested
    @DisplayName("When the token is created")
    class WhenTheTokenIsCreated {

        @Test
        @DisplayName("Should keep the constructor value")
        void shouldCreateReferenceTokenWithValue() {
            ReferenceToken token = new ReferenceToken("status");

            assertThat(token.value()).isEqualTo("status");
        }

        @Test
        @DisplayName("Should keep the factory method value")
        void shouldCreateReferenceTokenUsingFactoryMethod() {
            ReferenceToken token = ReferenceToken.of("priority");

            assertThat(token.value()).isEqualTo("priority");
        }
    }

    @Nested
    @DisplayName("When the token value is null")
    class WhenTheTokenValueIsNull {

        @Test
        @DisplayName("Should reject null in the constructor")
        void shouldThrowNullPointerExceptionWhenValueIsNullInConstructor() {
            assertThatThrownBy(() -> new ReferenceToken(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("value must not be null");
        }

        @Test
        @DisplayName("Should reject null in the factory method")
        void shouldThrowNullPointerExceptionWhenValueIsNullInFactoryMethod() {
            assertThatThrownBy(() -> ReferenceToken.of(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("value must not be null");
        }
    }

    @Nested
    @DisplayName("When the token is compared")
    class WhenTheTokenIsCompared {

        @Test
        @DisplayName("Should be equal when values are equal")
        void shouldBeEqualWhenValuesAreEqual() {
            ReferenceToken first = new ReferenceToken("name");
            ReferenceToken second = new ReferenceToken("name");

            assertThat(first)
                    .isEqualTo(second)
                    .hasSameHashCodeAs(second);
        }

        @Test
        @DisplayName("Should not be equal when values are different")
        void shouldNotBeEqualWhenValuesAreDifferent() {
            ReferenceToken first = new ReferenceToken("name");
            ReferenceToken second = new ReferenceToken("other");

            assertThat(first).isNotEqualTo(second);
        }
    }
}