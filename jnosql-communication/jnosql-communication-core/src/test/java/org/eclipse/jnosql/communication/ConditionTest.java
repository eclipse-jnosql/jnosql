/*
 *
 *  Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation
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
 *   Elias Nogueira
 *
 */
package org.eclipse.jnosql.communication;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.eclipse.jnosql.communication.Condition.AND;
import static org.eclipse.jnosql.communication.Condition.CONTAINS;
import static org.eclipse.jnosql.communication.Condition.ENDS_WITH;
import static org.eclipse.jnosql.communication.Condition.EQUALS;
import static org.eclipse.jnosql.communication.Condition.GREATER_EQUALS_THAN;
import static org.eclipse.jnosql.communication.Condition.IGNORE_CASE;
import static org.eclipse.jnosql.communication.Condition.IN;
import static org.eclipse.jnosql.communication.Condition.LESSER_THAN;
import static org.eclipse.jnosql.communication.Condition.NOT;
import static org.eclipse.jnosql.communication.Condition.OR;
import static org.eclipse.jnosql.communication.Condition.STARTS_WITH;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("Condition Enum Tests")
class ConditionTest {

    @Nested
    @DisplayName("When retrieving the field name")
    class GetNameField {

        @DisplayName("Should return the correct name field for each condition")
        @ParameterizedTest(name = "Condition {0} should map to field value {1}")
        @MethodSource("org.eclipse.jnosql.communication.ConditionTest#data")
        void shouldReturnNameField(Condition condition, String fieldName) {
            assertThat(condition.getNameField()).isEqualTo(fieldName);
        }
    }

    @Nested
    @DisplayName("When parsing a condition from string")
    class ParseCondition {

        @DisplayName("Should parse condition enum constant from value")
        @ParameterizedTest(name = "Value {1} should parse to Condition {0}")
        @MethodSource("org.eclipse.jnosql.communication.ConditionTest#data")
        void shouldParseCondition(Condition condition, String value) {
            assertThat(Condition.parse(value)).isEqualTo(condition);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when condition is not found")
        void shouldThrowIllegalArgumentException() {
            String nonExistentCondition = "_NON_EXISTENT";

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Condition.parse(nonExistentCondition))
                    .withMessage(String.format("The condition %s is not found", nonExistentCondition));
        }
    }

    static Stream<Arguments> data() {
        return Stream.of(
                arguments(AND, "_AND"),
                arguments(EQUALS, "_EQUALS"),
                arguments(GREATER_EQUALS_THAN, "_GREATER_EQUALS_THAN"),
                arguments(LESSER_THAN, "_LESSER_THAN"),
                arguments(IN, "_IN"),
                arguments(NOT, "_NOT"),
                arguments(OR, "_OR"),
                arguments(CONTAINS, "_CONTAINS"),
                arguments(STARTS_WITH, "_STARTS_WITH"),
                arguments(ENDS_WITH, "_ENDS_WITH"),
                arguments(IGNORE_CASE, "_IGNORE_CASE")
        );
    }
}
