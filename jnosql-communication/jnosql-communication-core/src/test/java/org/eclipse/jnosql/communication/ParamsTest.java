/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Maximillian Arruda
 *  Elias Nogueira
 */
package org.eclipse.jnosql.communication;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class ParamsTest {

    @Test
    @DisplayName("Should create a new Params from a method factory Params.newParams()")
    void shouldNewParams() {
        Params params = Params.newParams();
        assertThat(params).isInstanceOf(Params.class).isNotNull();
    }

    @Test
    @DisplayName("Should not use Value when is invalid")
    void shouldNotUseValueWhenIsInvalid() {
        String paramValue = UUID.randomUUID().toString();

        Params params = Params.newParams();
        var param = params.add(paramValue);

        assertSoftly(softly -> {
            softly.assertThatThrownBy(param::get).isInstanceOf(QueryException.class)
                    .hasMessage(String.format("The value of parameter %s cannot be null", paramValue));

            softly.assertThatThrownBy(() -> param.get(String.class)).isInstanceOf(QueryException.class)
                    .hasMessage(String.format("The value of parameter %s cannot be null", paramValue));
        });
    }

    @Test
    void shouldPrefix(){
        Params params = Params.newParams();
        Value value = params.add("name_2342342");
        params.prefix("name", "Ada");
        Assertions.assertTrue(params.isNotEmpty());
        Assertions.assertEquals("Ada", value.get());
    }

    @Nested
    @DisplayName("Given an empty Params")
    class GivenEmptyParamsTest {

        @Test
        @DisplayName("then toString() should return an empty string")
        void shouldToString() {
            Params params = Params.newParams();
            assertThat(params.toString()).isEmpty();
        }

        @Test
        @DisplayName("and isNotEmpty() returns false")
        void shouldIsNotEmpty() {
            Params params = Params.newParams();
            assertThat(params.isNotEmpty()).isFalse();
        }

        @Test
        @DisplayName("and getParametersNames() should returns an empty List<String>")
        void shouldGetParametersNames() {
            Params params = Params.newParams();
            assertThat(params.getParametersNames()).isNotNull().isEmpty();
        }

        @Nested
        @DisplayName("when add method is called")
        class WhenAddMethodIsCalledTest {

            @Test
            @DisplayName("with any param name then it should returns a Value")
            void testAdd() {
                Params params = Params.newParams();
                var paramName = UUID.randomUUID().toString();
                var value = params.add(paramName);

                assertThat(value).isNotNull();
            }

            @Test
            @DisplayName("multiple times with any param name then should return a different Value objects")
            void testAddMultipleTimes() {
                Params params = Params.newParams();
                var paramName = UUID.randomUUID().toString();
                var value1 = params.add(paramName);
                assertThat(value1).isNotNull();

                var value2 = params.add(paramName);
                assertSoftly(softly -> {
                    softly.assertThat(value2).as("value is not null").isNotNull();
                    softly.assertThat(Objects.equals(value1, value2)).as("values are not equals").isFalse();
                });
            }
        }

        @Nested
        @DisplayName("when bind method is called")
        class WhenBindIsCalled {

            @Test
            @DisplayName("must causes no side effects on the parameters")
            void testBind() {
                Params params = Params.newParams();
                params.bind(UUID.randomUUID().toString(), new Object());

                assertSoftly(softly -> {
                    softly.assertThat(params.isNotEmpty()).as("params are not empty").isFalse();
                    softly.assertThat(params.getParametersNames().isEmpty()).as("parameter names are empty").isTrue();
                });
            }
        }
    }

    @Nested
    @DisplayName("given a filled Params")
    class GivenFilledParamsTest {

        static class Scenario {

            final Params params;
            final List<String> parameterNameList;
            final List<Map<String, Value>> parameterList;

            Scenario() {
                this.params = Params.newParams();

                final List<String> parameterNameList = new ArrayList<>();
                final List<Map<String, Value>> parameterList = new ArrayList<>();

                IntStream.range(0, 3)
                        .boxed()
                        .map(idx -> UUID.randomUUID().toString())
                        .forEach(key -> {
                            parameterNameList.add(key);
                            Value value = params.add(key);
                            parameterList.add(Map.of(key, value));
                        });

                this.parameterList = Collections.unmodifiableList(parameterList);
                this.parameterNameList = Collections.unmodifiableList(parameterNameList);
            }
        }

        Scenario newScenario() {
            return new Scenario();
        }

        @Test
        @DisplayName("then toString() should return a string with all parameter names delimited by comma")
        void testToString() {
            Scenario scenario = newScenario();
            Params params = scenario.params;

            assertThat(params).hasToString(String.join(",", scenario.parameterNameList));
        }

        @Test
        @DisplayName("should isNotEmpty() returns true")
        void testIsNotEmpty() {
            Params params = newScenario().params;
            assertThat(params.isNotEmpty()).isTrue();
        }

        @Test
        @DisplayName("should getParametersNames() returns an non empty List<String>")
        void testGetParametersNames() {
            Params params = newScenario().params;
            assertThat(params.getParametersNames()).isNotNull().isNotEmpty();
        }

        @Nested
        @DisplayName("when add method is called")
        class WhenAddMethodIsCalledTest {

            @Test
            @DisplayName("with any param name then it should returns a Value")
            void testAdd() {
                Params params = newScenario().params;
                var paramName = UUID.randomUUID().toString();
                var value = params.add(paramName);

                assertThat(value).isNotNull();
            }

            @Test
            @DisplayName("multiple times with any param name then should return a different Value objects")
            void testAddMultipleTimes() {
                Params params = newScenario().params;
                var paramName = UUID.randomUUID().toString();
                var value1 = params.add(paramName);
                assertThat(value1).isNotNull();

                var value2 = params.add(paramName);
                assertSoftly(softly -> {
                    softly.assertThat(value2).as("value is not null").isNotNull();
                    softly.assertThat(Objects.equals(value1, value2)).as("values are not equal").isFalse();
                });
            }
        }

        @Nested
        @DisplayName("when bind method is called")
        class WhenBindIsCalled {

            @Test
            @DisplayName("must cause no side effects on the parameters when params instance doesn't have the param for the given param name")
            void testBindCausesNoSideEffects() {
                Scenario scenario = newScenario();
                Params params = scenario.params;
                String newParameter = UUID.randomUUID().toString();
                params.bind(newParameter, new Object());

                assertThat(params.getParametersNames()).doesNotContain(newParameter);
            }

            @Test
            @DisplayName("must cause side effects on the parameters when params instance have the param for the given param name")
            void testBindCausesSideEffects() {
                Scenario scenario = newScenario();
                Params params = scenario.params;

                String existParamName = params.getParametersNames().get(0);

                Object newParamValue = UUID.randomUUID().toString();
                params.bind(existParamName, newParamValue);

                assertThat(params.isNotEmpty()).isTrue();

                Object actualParamValue = scenario.parameterList.stream()
                        .filter(item -> item.containsKey(existParamName))
                        .map(item -> item.get(existParamName))
                        .findFirst()
                        .map(Value::get)
                        .get();

                assertThat(newParamValue).isEqualTo(actualParamValue);
            }
        }
    }
}
