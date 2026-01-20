/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StringQueryValue")
class StringQueryValueTest {

    @Test
    @DisplayName("should expose the string value through the Supplier contract")
    void shouldExposeStringValue() {
        StringQueryValue queryValue = StringQueryValue.of("Otavio");

        assertThat(queryValue.get())
                .isEqualTo("Otavio");
    }

    @Test
    @DisplayName("should preserve the string content exactly")
    void shouldPreserveStringContent() {
        StringQueryValue queryValue = StringQueryValue.of("  value with spaces  ");

        assertThat(queryValue.get())
                .isEqualTo("  value with spaces  ");
    }

    @Test
    @DisplayName("should report STRING as its ValueType")
    void shouldExposeStringValueType() {
        StringQueryValue queryValue = StringQueryValue.of("test");

        assertThat(queryValue.type())
                .isEqualTo(ValueType.STRING);
    }

    @Test
    @DisplayName("should implement the QueryValue contract")
    void shouldImplementQueryValueContract() {
        StringQueryValue queryValue = StringQueryValue.of("contract");

        assertThat(queryValue)
                .isInstanceOf(QueryValue.class);
    }

    @Test
    @DisplayName("factory method should create an equivalent instance")
    void factoryMethodShouldCreateEquivalentInstance() {
        StringQueryValue fromFactory = StringQueryValue.of("same");
        StringQueryValue fromConstructor = new StringQueryValue("same");

        assertThat(fromFactory)
                .isEqualTo(fromConstructor);
    }
}