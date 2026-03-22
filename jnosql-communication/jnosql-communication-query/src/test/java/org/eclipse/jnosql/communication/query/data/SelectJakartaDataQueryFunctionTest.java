/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Matheus Oliveira
 */
package org.eclipse.jnosql.communication.query.data;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.query.Function;
import org.eclipse.jnosql.communication.query.FunctionQueryValue;
import org.eclipse.jnosql.communication.query.ParamQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.SelectQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class SelectJakartaDataQueryFunctionTest {

    private SelectParser selectParser;

    @BeforeEach
    void setUp() {
        selectParser = new SelectParser();
    }

    @ParameterizedTest
    @DisplayName("Should parse function expressions in WHERE clause")
    @MethodSource("functionsProvider")
    void shouldHandleFunctions(String query, String fieldName, String functionName) {
        SelectQuery selectQuery = selectParser.apply(query, "Customer");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.where()).as("where clause is present").isPresent();
            QueryCondition condition = selectQuery.where().get().condition();
            soft.assertThat(condition.name()).as("condition field name").isEqualTo(fieldName);
            soft.assertThat(condition.condition()).as("condition type is EQUALS").isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.value()).as("condition value is a FunctionQueryValue").isInstanceOf(FunctionQueryValue.class);
            Function function = ((FunctionQueryValue) condition.value()).get();
            soft.assertThat(function.name()).as("function name").isEqualTo(functionName);
            soft.assertThat(function.params()).as("function has parameters").isNotEmpty();
        });
    }

    static Stream<Arguments> functionsProvider() {
        return Stream.of(
                Arguments.of("FROM Customer WHERE name = LOWER('JOHN')", "name", "LOWER"),
                Arguments.of("FROM Customer WHERE name = lower('JOHN')", "name", "LOWER"),
                Arguments.of("FROM Customer WHERE name = UPPER('john')", "name", "UPPER"),
                Arguments.of("FROM Customer WHERE name = LENGTH('john')", "name", "LENGTH"),
                Arguments.of("FROM Customer WHERE age = ABS(-10)", "age", "ABS"),
                Arguments.of("FROM Customer WHERE name = LEFT('Jonathan', 2)", "name", "LEFT"),
                Arguments.of("FROM Customer WHERE name = RIGHT('Jonathan', 1)", "name", "RIGHT")
        );
    }

    @ParameterizedTest
    @DisplayName("Should parse nested function expressions")
    @MethodSource("nestedFunctionsProvider")
    void shouldHandleNestedFunctions(String query, String outerFunc, String innerFunc) {
        SelectQuery selectQuery = selectParser.apply(query, "Customer");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.where()).as("where clause is present").isPresent();
            QueryCondition condition = selectQuery.where().get().condition();
            soft.assertThat(condition.value()).as("outer value is a FunctionQueryValue").isInstanceOf(FunctionQueryValue.class);
            Function outer = ((FunctionQueryValue) condition.value()).get();
            soft.assertThat(outer.name()).as("outer function name").isEqualTo(outerFunc);
            soft.assertThat(outer.params()[0]).as("inner param is a FunctionQueryValue").isInstanceOf(FunctionQueryValue.class);
            Function inner = ((FunctionQueryValue) outer.params()[0]).get();
            soft.assertThat(inner.name()).as("inner function name").isEqualTo(innerFunc);
        });
    }

    static Stream<Arguments> nestedFunctionsProvider() {
        return Stream.of(
                Arguments.of("FROM Customer WHERE name = UPPER(LOWER(:name))", "UPPER", "LOWER"),
                Arguments.of("FROM Customer WHERE name = LOWER(UPPER('john'))", "LOWER", "UPPER")
        );
    }

    @ParameterizedTest
    @DisplayName("Should parse function expressions with named parameters")
    @MethodSource("parametersProvider")
    void shouldHandleFunctionsWithParameters(String query, String functionName, String paramName) {
        SelectQuery selectQuery = selectParser.apply(query, "Customer");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.where()).as("where clause is present").isPresent();
            QueryCondition condition = selectQuery.where().get().condition();
            soft.assertThat(condition.condition()).as("condition type is EQUALS").isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.value()).as("condition value is a FunctionQueryValue").isInstanceOf(FunctionQueryValue.class);
            Function function = ((FunctionQueryValue) condition.value()).get();
            soft.assertThat(function.name()).as("function name").isEqualTo(functionName);
            soft.assertThat(function.params()[0]).as("first param is a ParamQueryValue").isInstanceOf(ParamQueryValue.class);
            soft.assertThat(((ParamQueryValue) function.params()[0]).get()).as("param name").isEqualTo(paramName);
        });
    }

    static Stream<Arguments> parametersProvider() {
        return Stream.of(
                Arguments.of("FROM Customer WHERE name = LOWER(:name)", "LOWER", "name"),
                Arguments.of("FROM Customer WHERE name = UPPER(?1)", "UPPER", "?1"),
                Arguments.of("FROM Customer WHERE age = ABS((:age))", "ABS", "age")
        );
    }

    @ParameterizedTest
    @DisplayName("Should allow function keywords as field names")
    @MethodSource("fieldCollisionProvider")
    void shouldHandleFieldNamesSameAsFunctionNames(String query, String fieldName) {
        SelectQuery selectQuery = selectParser.apply(query, "Box");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.where()).as("where clause is present").isPresent();
            QueryCondition condition = selectQuery.where().get().condition();
            soft.assertThat(condition.name()).as("condition field name").isEqualTo(fieldName);
        });
    }

    static Stream<Arguments> fieldCollisionProvider() {
        return Stream.of(
                Arguments.of("FROM Box WHERE length = 10", "length"),
                Arguments.of("FROM Box WHERE ABS(length) = 10", "ABS(length)"),
                Arguments.of("FROM Box WHERE left = 'a'", "left")
        );
    }
}
