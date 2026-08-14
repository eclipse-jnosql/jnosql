/*
 *
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.communication.reader;

import org.eclipse.jnosql.communication.CommunicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class UUIDValueReaderTest {
    private final UUIDValueReader valueReader = new UUIDValueReader();

    @Nested
    @DisplayName("When the value is read")
    class WhenTheValueIsRead {

        @Test
        @DisplayName("Should return the same UUID instance")
        void shouldReadUUIDFromUUIDInstance() {
            UUID uuid = UUID.randomUUID();
            UUID result = valueReader.read(UUID.class, uuid);

            assertThat(result).isEqualTo(uuid);
        }

        @Test
        @DisplayName("Should parse UUID text")
        void shouldReadUUIDFromString() {
            UUID uuid = UUID.randomUUID();
            UUID result = valueReader.read(UUID.class, uuid.toString());

            assertThat(result).isEqualTo(uuid);
        }

        @Test
        @DisplayName("Should reject invalid UUID text")
        void shouldThrowExceptionForInvalidString() {
            String invalidUUID = "invalid-uuid";

            assertThatThrownBy(() -> valueReader.read(UUID.class, invalidUUID))
                    .isInstanceOf(CommunicationException.class)
                    .hasMessageContaining("value is not UUID format: " + invalidUUID);
        }

        @Test
        @DisplayName("Should return null for unsupported value types")
        void shouldReturnNullForUnsupportedType() {
            Integer unsupportedValue = 42;
            UUID result = valueReader.read(UUID.class, unsupportedValue);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("When the supported type is checked")
    class WhenTheSupportedTypeIsChecked {

        @Test
        @DisplayName("Should accept only UUID")
        void shouldTestUUIDType() {
            assertSoftly(softly -> {
                softly.assertThat(valueReader.test(UUID.class)).isTrue();
                softly.assertThat(valueReader.test(String.class)).isFalse();
            });
        }
    }
}
