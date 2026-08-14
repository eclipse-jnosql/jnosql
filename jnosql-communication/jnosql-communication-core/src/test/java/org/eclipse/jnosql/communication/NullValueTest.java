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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NullValueTest {

    @Nested
    @DisplayName("When the value is read")
    class WhenTheValueIsRead {

        @Test
        @DisplayName("Should return null from the raw value accessor")
        void shouldReturnNullForGet() {
            assertThat(NullValue.INSTANCE.get()).isNull();
        }

        @Test
        @DisplayName("Should return null from the typed value accessor")
        void shouldReturnNullForGetWithType() {
            assertThat(NullValue.INSTANCE.get(String.class)).isNull();
        }

        @Test
        @DisplayName("Should return null from the type supplier accessor")
        void shouldReturnNullForGetWithTypeSupplier() {
            String value = NullValue.INSTANCE.get(new TypeReference<>() {
            });

            assertThat(value).isNull();
        }
    }

    @Nested
    @DisplayName("When the null marker is inspected")
    class WhenTheNullMarkerIsInspected {

        @Test
        @DisplayName("Should not report any requested type as an instance")
        void shouldReturnFalseForIsInstanceOf() {
            assertThat(NullValue.INSTANCE.isInstanceOf(String.class)).isFalse();
        }

        @Test
        @DisplayName("Should report that it represents null")
        void shouldReturnTrueForIsNull() {
            assertThat(NullValue.INSTANCE.isNull()).isTrue();
        }
    }
}