/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 *  Elias Nogueira
 */
package org.eclipse.jnosql.communication;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class ParamValueTest {

    private static Params params;
    private static Value name;

    @BeforeAll
    static void preCondition() {
        params = Params.newParams();
        name = params.add("name");
    }

    @Test
    @DisplayName("Should be able to retrieve the parameters")
    void shouldAddParameter() {
        assertThat(params.getParametersNames()).contains("name");
    }

    @Test
    @DisplayName("Should throw QueryException when value is invalid")
    void shouldNotUseValueWhenIsInvalid() {
        Params params = Params.newParams();
        Value surname = params.add("surname");

        assertSoftly(softly -> {
            softly.assertThatThrownBy(surname::get).isInstanceOf(QueryException.class)
                    .hasMessage("The value of parameter surname cannot be null");

            softly.assertThatThrownBy(() -> surname.get(String.class)).isInstanceOf(QueryException.class)
                    .hasMessage("The value of parameter surname cannot be null");
        });
    }

    @Test
    @DisplayName("Should be abe to bind he parameter")
    void shouldSetParameter() {
        params.bind("name", "Ada Lovelace");
        assertThat(name.get()).isEqualTo("Ada Lovelace");
    }

    @Test
    @DisplayName("Should be instance of Integer when value is empty")
    void shouldReturnsTrueWhenValueIsEmpty() {
        Params params = Params.newParams();
        Value surname = params.add("surname");

        assertThat(surname.isInstanceOf(Integer.class)).isTrue();
    }

    @Test
    @DisplayName("Should change the value instance")
    void shouldInstanceOf() {
        assertThat(name.isInstanceOf(Integer.class)).isTrue();

        params.bind("name", "Ada Lovelace");

        assertSoftly(softly -> {
            assertThat(name.isInstanceOf(String.class)).as("has the correct instance").isTrue();
            assertThat(name.isInstanceOf(Integer.class)).as("has incorrect instance").isFalse();
        });
    }

    @Test
    @DisplayName("Should have custom toString")
    void testToString() {
        String paramName = "name";
        String paramValue = "12";

        Params params = Params.newParams();
        Value value = params.add(paramName);

        assertThat(paramName + "= ?").isEqualTo(value.toString());

        params.bind(paramName, paramValue);
        assertThat(paramName + "= " + paramValue).isEqualTo(value.toString());
    }

    @Test
    void shouldCheckIsNull() {
        Params params = Params.newParams();
        Value value = params.add("name");
        SoftAssertions.assertSoftly(softly -> softly.assertThat(value.isNull()).isFalse());
    }

    @Test
    void shouldCheckIsEmpty() {
        ParamValue params = new ParamValue("name");

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(params.isEmpty()).isTrue();
            softly.assertThat(params.isEmpty()).isTrue();
            params.setValue("Test");
            softly.assertThat(params.isEmpty()).isFalse();
            softly.assertThat(params.isEmpty()).isFalse();

        });
    }

    @Test
    void shouldGet() {
        ParamValue params = new ParamValue("name");
        params.setValue("123");

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(params.get()).isEqualTo("123");
            softly.assertThat(params.get(Integer.class)).isEqualTo(123);
        });
    }
}
