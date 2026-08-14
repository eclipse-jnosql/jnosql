/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */

package org.eclipse.jnosql.communication.semistructured;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElementDeleteQueryTest {


    @Nested
    @DisplayName("When the element delete query is used")
    class WhenTheElementDeleteQueryIsUsed {

        @DisplayName("Should Builder Thrown Exception")
        @Test
        void shouldBuilderThrownException() {
            assertThatThrownBy(() -> DeleteQuery.builder(new String[]{null})).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Build Returns AValid Builder")
        @ParameterizedTest(name = "{0} passed to build method then a valid builder should be returned")
        @MethodSource("testScenarios")
        void shouldBuildReturnsAValidBuilder(String scenario, String[] documents) {
            var builder = Objects.isNull(documents) ? DeleteQuery.builder() : DeleteQuery.builder(documents);
            assertThat(builder).isNotNull();
        }

        @DisplayName("Should Delete Thrown Exception")
        @Test
        void shouldDeleteThrownException() {
            assertThatThrownBy(() -> DeleteQuery.delete(new String[]{null})).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Delete Returns AValid Builder")
        @ParameterizedTest(name = "{0} passed to delete method then a valid builder should be returned")
        @MethodSource("testScenarios")
        void shouldDeleteReturnsAValidBuilder(String scenario, String[] documents) {
            var builder = Objects.isNull(documents) ? DeleteQuery.delete() : DeleteQuery.delete(documents);
            assertThat(builder).isNotNull();
        }

        static Stream<Arguments> testScenarios() {
            return Stream.of(
                    arguments("when an empty array", new String[0]),
                    arguments("when a non empty array", new String[]{"doc1", "doc2", "doc2"}),
                    arguments("when zero arguments", null)
            );
        }
    }

}
