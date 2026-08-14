/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *   Matheus Oliveira
 */
package org.eclipse.jnosql.mapping.semistructured;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FunctionTest {

    @DisplayName("Should create upper function")
    @Test
    void shouldCreateUpperFunction() {
        Function f = Function.upper("name");
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(f.name()).isEqualTo("UPPER");
            soft.assertThat(f.field()).isEqualTo("name");
            soft.assertThat(f.arguments()).isEmpty();
            soft.assertThat(f.toString()).isEqualTo("UPPER(name)");
        });
    }

    @DisplayName("Should create lower function")
    @Test
    void shouldCreateLowerFunction() {
        Function f = Function.lower("name");
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(f.name()).isEqualTo("LOWER");
            soft.assertThat(f.field()).isEqualTo("name");
            soft.assertThat(f.arguments()).isEmpty();
            soft.assertThat(f.toString()).isEqualTo("LOWER(name)");
        });
    }

    @DisplayName("Should create length function")
    @Test
    void shouldCreateLengthFunction() {
        Function f = Function.length("description");
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(f.name()).isEqualTo("LENGTH");
            soft.assertThat(f.field()).isEqualTo("description");
            soft.assertThat(f.arguments()).isEmpty();
            soft.assertThat(f.toString()).isEqualTo("LENGTH(description)");
        });
    }

    @DisplayName("Should create abs function")
    @Test
    void shouldCreateAbsFunction() {
        Function f = Function.abs("age");
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(f.name()).isEqualTo("ABS");
            soft.assertThat(f.field()).isEqualTo("age");
            soft.assertThat(f.arguments()).isEmpty();
            soft.assertThat(f.toString()).isEqualTo("ABS(age)");
        });
    }

    @DisplayName("Should create left function")
    @Test
    void shouldCreateLeftFunction() {
        Function f = Function.left("name", 3);
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(f.name()).isEqualTo("LEFT");
            soft.assertThat(f.field()).isEqualTo("name");
            soft.assertThat(f.arguments()).containsExactly(3);
            soft.assertThat(f.toString()).isEqualTo("LEFT(name, 3)");
        });
    }

    @DisplayName("Should create right function")
    @Test
    void shouldCreateRightFunction() {
        Function f = Function.right("name", 2);
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(f.name()).isEqualTo("RIGHT");
            soft.assertThat(f.field()).isEqualTo("name");
            soft.assertThat(f.arguments()).containsExactly(2);
            soft.assertThat(f.toString()).isEqualTo("RIGHT(name, 2)");
        });
    }

    @DisplayName("Should throw null pointer exception when field is null")
    @Test
    void shouldThrowNullPointerExceptionWhenFieldIsNull() {
        assertThatThrownBy(() -> Function.upper(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Function.lower(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Function.length(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Function.abs(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Function.left(null, 3)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Function.right(null, 3)).isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Should throw illegal argument exception when length is negative")
    @Test
    void shouldThrowIllegalArgumentExceptionWhenLengthIsNegative() {
        assertThatThrownBy(() -> Function.left("name", -1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Function.right("name", -1)).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("Should return zero as valid length")
    @Test
    void shouldReturnZeroAsValidLength() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThatCode(() -> Function.left("name", 0)).doesNotThrowAnyException();
            soft.assertThatCode(() -> Function.right("name", 0)).doesNotThrowAnyException();
        });
    }

    @DisplayName("Should return defensive copy of arguments")
    @Test
    void shouldReturnDefensiveCopyOfArguments() {
        Function f = Function.left("name", 3);
        Object[] args = f.arguments();
        args[0] = 99;
        assertThat(f.arguments()[0]).isEqualTo(3);
    }

    @Nested
    @DisplayName("When the function is tested")
    class WhenTheFunctionIsTested {
    }
}
