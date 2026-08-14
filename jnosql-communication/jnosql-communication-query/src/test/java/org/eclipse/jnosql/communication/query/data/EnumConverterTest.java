/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.data;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.QueryException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EnumConverterTest {

    @Nested
    @DisplayName("When the enum converter is used")
    class WhenTheEnumConverter {
    }
    @Test
    @DisplayName("Successfully convert string to enum")
    void shouldSuccessfulConversion() {
        String input = "java.time.DayOfWeek.MONDAY";
        Enum<?> result = EnumConverter.INSTANCE.apply(input);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result)
                .as("Check if MONDAY is correctly converted")
                .isEqualTo(java.time.DayOfWeek.MONDAY);
        softly.assertAll();
    }

    @Test
    @DisplayName("Handle non-existent enum constant")
    void shouldNonExistentEnumConstant() {
        String input = "java.time.DayOfWeek.NOTADAY";
        SoftAssertions softly = new SoftAssertions();
        softly.assertThatThrownBy(() -> EnumConverter.INSTANCE.apply(input))
                .as("Check handling of non-existent enum constants")
                .isInstanceOf(QueryException.class)
                .hasMessageContaining("There is an issue to load class because: " + input);
        softly.assertAll();
    }

    @Test
    @DisplayName("Handle invalid enum class name")
    void shouldInvalidEnumClassName() {
        String input = "com.example.NonExistentEnum.SOME_VALUE";
        SoftAssertions softly = new SoftAssertions();
        softly.assertThatThrownBy(() -> EnumConverter.INSTANCE.apply(input))
                .as("Check handling of invalid enum class names")
                .isInstanceOf(QueryException.class)
                .hasMessageContaining("There is an issue to load class because: " + input);
        softly.assertAll();
    }

    @Test
    @DisplayName("Successfully convert inner class")
    void shouldUserInnerClass() {
        String input = "org.eclipse.jnosql.communication.query.data.OuterClass.InnerEnum.VALUE1";
        Enum<?> result = EnumConverter.INSTANCE.apply(input);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result)
                .as("Check if MONDAY is correctly converted")
                .isEqualTo(OuterClass.InnerEnum.VALUE1);
        softly.assertAll();
    }

}