/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
 */
package org.eclipse.jnosql.mapping.core.repository;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DynamicReturnConverterTest {


















    @Nested
    @DisplayName("When the dynamic return converter operates")
    class WhenTheDynamicReturnConverterOperates {

        @DisplayName("Should return false when null")
        @Test
        void shouldReturnFalseWhenNull() {
            boolean result = DynamicReturnConverter.queryContainsNamedParameters(null);
            assertThat(result).isFalse();
        }
        @DisplayName("Should return false when empty")
        @Test
        void shouldReturnFalseWhenEmpty() {
            boolean result = DynamicReturnConverter.queryContainsNamedParameters("");
            assertThat(result).isFalse();
        }
        @DisplayName("Should detect simple named parameter")
        @Test
        void shouldDetectSimpleNamedParameter() {
            boolean result = DynamicReturnConverter.queryContainsNamedParameters(
                    "select * from Person where age = :age");
            assertThat(result).isTrue();
        }
        @DisplayName("Should detect named parameter when it appears before ordinal")
        @Test
        void shouldDetectNamedParameterWhenItAppearsBeforeOrdinal() {
            boolean result = DynamicReturnConverter.queryContainsNamedParameters(
                    "select * from Person where name = :name and id = ?1");
            assertThat(result).isTrue();
        }
        @DisplayName("Should prefer first token and return false when ordinal appears first")
        @Test
        void shouldPreferFirstTokenAndReturnFalseWhenOrdinalAppearsFirst() {
            boolean result = DynamicReturnConverter.queryContainsNamedParameters(
                    "select * from Person where id = ?1 and name = :name");
            assertThat(result).isFalse();
        }
        @DisplayName("Should return false when only ordinal parameters present")
        @Test
        void shouldReturnFalseWhenOnlyOrdinalParametersPresent() {
            boolean result = DynamicReturnConverter.queryContainsNamedParameters(
                    "select * from Person where id = ?1 and age > ?2");
            assertThat(result).isFalse();
        }
        @DisplayName("Should ignore bare question mark without digits")
        @Test
        void shouldIgnoreBareQuestionMarkWithoutDigits() {
            boolean result = DynamicReturnConverter.queryContainsNamedParameters(
                    "select * from T where flag = ? and name = 'x'");
            assertThat(result).isFalse();
        }
        @DisplayName("Should return false for invalid named starting with digit")
        @Test
        void shouldReturnFalseForInvalidNamedStartingWithDigit() {
            boolean result = DynamicReturnConverter.queryContainsNamedParameters(
                    "select * from T where a = :1abc");
            assertThat(result).isFalse();
        }
        @DisplayName("Should return false for colon at end without identifier")
        @Test
        void shouldReturnFalseForColonAtEndWithoutIdentifier() {
            boolean result = DynamicReturnConverter.queryContainsNamedParameters(
                    "select * from T where a = :");
            assertThat(result).isFalse();
        }
        @DisplayName("Should return false for colon followed by non identifier char")
        @Test
        void shouldReturnFalseForColonFollowedByNonIdentifierChar() {
            boolean result = DynamicReturnConverter.queryContainsNamedParameters(
                    "select * from T where a = :.");
            assertThat(result).isFalse();
        }
        @DisplayName("Should support underscore and dollar as first identifier char")
        @Test
        void shouldSupportUnderscoreAndDollarAsFirstIdentifierChar() {
            boolean result = DynamicReturnConverter.queryContainsNamedParameters(
                    "select * from T where a = :_x or b = :$y");
            assertThat(result).isTrue();
        }
        @DisplayName("Should support unicode letter as first identifier char")
        @Test
        void shouldSupportUnicodeLetterAsFirstIdentifierChar() {
            boolean result = DynamicReturnConverter.queryContainsNamedParameters(
                    "select * from T where owner = :área");
            assertThat(result).isTrue();
        }
        @DisplayName("Should return false when no parameters present")
        @Test
        void shouldReturnFalseWhenNoParametersPresent() {
            boolean result = DynamicReturnConverter.queryContainsNamedParameters(
                    "select id, name from Person");
            assertThat(result).isFalse();
        }
        @DisplayName("Should treat dotted identifier as named by prefix")
        @Test
        void shouldTreatDottedIdentifierAsNamedByPrefix() {
            // The method only checks the first char after ':'; dot later is irrelevant.
            boolean result = DynamicReturnConverter.queryContainsNamedParameters(
                    "select * from T where user = :account.name");
            assertThat(result).isTrue();
        }
        @DisplayName("Should ignore multiple invalid colons until avalid named appears")
        @Test
        void shouldIgnoreMultipleInvalidColonsUntilAValidNamedAppears() {
            boolean result = DynamicReturnConverter.queryContainsNamedParameters(
                    "select * from T where a = :. and b = :$ok");
            assertThat(result).isTrue();
        }
        @DisplayName("Should return false when ordinal appears before any valid named even if invalid colons exist")
        @Test
        void shouldReturnFalseWhenOrdinalAppearsBeforeAnyValidNamedEvenIfInvalidColonsExist() {
            boolean result = DynamicReturnConverter.queryContainsNamedParameters(
                    "select * from T where a = :. and id = ?10 and b = :name");
            assertThat(result).isFalse();
        }
    }
}
