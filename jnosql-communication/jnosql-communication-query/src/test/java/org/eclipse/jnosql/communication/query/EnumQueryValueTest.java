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
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnumQueryValue")
class EnumQueryValueTest {

    private enum SampleEnum {
        FIRST, SECOND
    }

    @Test
    @DisplayName("should expose the enum value through the Supplier contract")
    void shouldReturnEnumValueFromSupplier() {
        EnumQueryValue queryValue = EnumQueryValue.of(SampleEnum.FIRST);

        assertThat(queryValue.get())
                .isEqualTo(SampleEnum.FIRST);
    }

    @Test
    @DisplayName("should report ENUM as its ValueType")
    void shouldExposeEnumValueType() {
        EnumQueryValue queryValue = EnumQueryValue.of(SampleEnum.SECOND);

        assertThat(queryValue.type())
                .isEqualTo(ValueType.ENUM);
    }

    @Test
    @DisplayName("should implement the QueryValue contract")
    void shouldImplementQueryValueContract() {
        EnumQueryValue queryValue = EnumQueryValue.of(SampleEnum.FIRST);

        assertThat(queryValue)
                .isInstanceOf(QueryValue.class);
    }

    @Test
    @DisplayName("factory method should create an equivalent instance")
    void factoryMethodShouldCreateEquivalentInstance() {
        EnumQueryValue first = EnumQueryValue.of(SampleEnum.FIRST);
        EnumQueryValue second = new EnumQueryValue(SampleEnum.FIRST);

        assertThat(first)
                .isEqualTo(second);
    }
}