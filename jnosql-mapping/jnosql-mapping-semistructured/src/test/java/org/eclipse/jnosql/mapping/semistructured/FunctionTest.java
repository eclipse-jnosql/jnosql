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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FunctionTest {

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

    @Test
    void shouldThrowNullPointerExceptionWhenFieldIsNull() {
        assertThrows(NullPointerException.class, () -> Function.upper(null));
        assertThrows(NullPointerException.class, () -> Function.lower(null));
        assertThrows(NullPointerException.class, () -> Function.length(null));
        assertThrows(NullPointerException.class, () -> Function.abs(null));
        assertThrows(NullPointerException.class, () -> Function.left(null, 3));
        assertThrows(NullPointerException.class, () -> Function.right(null, 3));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenLengthIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> Function.left("name", -1));
        assertThrows(IllegalArgumentException.class, () -> Function.right("name", -1));
    }

    @Test
    void shouldReturnZeroAsValidLength() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThatCode(() -> Function.left("name", 0)).doesNotThrowAnyException();
            soft.assertThatCode(() -> Function.right("name", 0)).doesNotThrowAnyException();
        });
    }

    @Test
    void shouldReturnDefensiveCopyOfArguments() {
        Function f = Function.left("name", 3);
        Object[] args = f.arguments();
        args[0] = 99;
        assertThat(f.arguments()[0]).isEqualTo(3);
    }
}
