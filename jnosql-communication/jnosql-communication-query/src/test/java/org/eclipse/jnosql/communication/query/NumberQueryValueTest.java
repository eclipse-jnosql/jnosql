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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@DisplayName("NumberQueryValue")
class NumberQueryValueTest {

    @Test
    @DisplayName("should expose the numeric value through the Supplier contract")
    void shouldExposeNumericValue() {
        NumberQueryValue queryValue = NumberQueryValue.of(42);

        assertThat(queryValue.get())
                .isEqualTo(42);
    }

    @Test
    @DisplayName("should preserve the numeric type")
    void shouldPreserveNumericType() {
        NumberQueryValue queryValue = NumberQueryValue.of(9.99);

        assertThat(queryValue.get())
                .isInstanceOf(Double.class)
                .isEqualTo(9.99);
    }

    @Test
    @DisplayName("should report NUMBER as its ValueType")
    void shouldExposeNumberValueType() {
        NumberQueryValue queryValue = NumberQueryValue.of(1);

        assertThat(queryValue.type())
                .isEqualTo(ValueType.NUMBER);
    }

    @Test
    @DisplayName("should implement the QueryValue contract")
    void shouldImplementQueryValueContract() {
        NumberQueryValue queryValue = NumberQueryValue.of(10);

        assertThat(queryValue)
                .isInstanceOf(QueryValue.class);
    }

    @Test
    @DisplayName("factory method should create an equivalent instance")
    void factoryMethodShouldCreateEquivalentInstance() {
        NumberQueryValue fromFactory = NumberQueryValue.of(5);
        NumberQueryValue fromConstructor = new NumberQueryValue(5);

        assertThat(fromFactory)
                .isEqualTo(fromConstructor);
    }
}