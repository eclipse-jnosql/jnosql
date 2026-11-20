/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.metadata.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class ReturnTypeTest {

    private final ReturnType nested = ReturnType.of(CompletionStage.class,
            ReturnType.of(Optional.class, ReturnType.of(String.class)));

    @Nested
    @DisplayName("When describing a plain class")
    class WhenThePlainClass {

        @Test
        @DisplayName("Should have no arguments and be its own element")
        void shouldBeItsOwnElement() {
            var type = ReturnType.of(String.class);

            assertSoftly(softly -> {
                softly.assertThat(type.rawType()).isEqualTo(String.class);
                softly.assertThat(type.arguments()).isEmpty();
                softly.assertThat(type.argument()).isEmpty();
                softly.assertThat(type.element()).isSameAs(type);
            });
        }
    }

    @Nested
    @DisplayName("When describing nested types")
    class WhenTheNesting {

        @Test
        @DisplayName("Should expose the specialization of each level")
        void shouldExposeEachLevel() {
            assertSoftly(softly -> {
                softly.assertThat(nested.rawType()).isEqualTo(CompletionStage.class);
                softly.assertThat(nested.argument().orElseThrow().rawType()).isEqualTo(Optional.class);
                softly.assertThat(nested.argument().orElseThrow().argument().orElseThrow().rawType())
                        .isEqualTo(String.class);
            });
        }

        @Test
        @DisplayName("Should find the innermost element")
        void shouldFindTheElement() {
            assertSoftly(softly -> softly.assertThat(nested.element().rawType()).isEqualTo(String.class));
        }

        @Test
        @DisplayName("Should keep every argument of a type with several")
        void shouldKeepSeveralArguments() {
            var type = ReturnType.of(java.util.Map.class, ReturnType.of(String.class), ReturnType.of(Integer.class));

            assertSoftly(softly -> {
                softly.assertThat(type.arguments()).hasSize(2);
                softly.assertThat(type.argument().orElseThrow().rawType()).isEqualTo(String.class);
            });
        }
    }

    @Nested
    @DisplayName("When comparing the generic type")
    class WhenTheComparison {

        @Test
        @DisplayName("Should be assignable to its own type and supertypes only")
        void shouldCheckAssignability() {
            var list = ReturnType.of(ArrayList.class);

            assertSoftly(softly -> {
                softly.assertThat(list.isA(List.class)).isTrue();
                softly.assertThat(list.isA(CompletionStage.class)).isFalse();
            });
        }

        @Test
        @DisplayName("Should require a type to compare")
        void shouldRequireType() {
            assertThatThrownBy(() -> nested.isA(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("When validating components")
    class WhenTheValidation {

        @Test
        @DisplayName("Should reject missing components")
        void shouldRejectNulls() {
            assertSoftly(softly -> {
                softly.assertThatThrownBy(() -> ReturnType.of(null)).isInstanceOf(NullPointerException.class);
                softly.assertThatThrownBy(() -> new ReturnType(String.class, null))
                        .isInstanceOf(NullPointerException.class);
                softly.assertThatThrownBy(() -> ReturnType.of(String.class, (ReturnType[]) null))
                        .isInstanceOf(NullPointerException.class);
            });
        }

        @Test
        @DisplayName("Should not be affected by later changes of the given list")
        void shouldBeImmutable() {
            var arguments = new ArrayList<>(List.of(ReturnType.of(String.class)));
            var type = new ReturnType(List.class, arguments);

            arguments.clear();

            assertSoftly(softly -> {
                softly.assertThat(type.arguments()).hasSize(1);
                softly.assertThatThrownBy(() -> type.arguments().add(ReturnType.of(String.class)))
                        .isInstanceOf(UnsupportedOperationException.class);
            });
        }

        @Test
        @DisplayName("Should compare by value")
        void shouldCompareByValue() {
            assertSoftly(softly -> softly.assertThat(nested).isEqualTo(ReturnType.of(CompletionStage.class,
                    ReturnType.of(Optional.class, ReturnType.of(String.class)))));
        }
    }
}
