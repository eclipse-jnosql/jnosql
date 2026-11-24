/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.data;

import org.eclipse.jnosql.communication.query.ConditionQueryValue;
import org.eclipse.jnosql.communication.query.NullQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.Where;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Boolean operator precedence: NOT > AND > OR")
class BooleanOperatorPrecedenceTest {

    private static final String A = "age = 10";
    private static final String B = "salary = 1000";
    private static final String C = "name = 'John'";
    private static final String D = "city = 'Rome'";

    static Stream<Arguments> expressions() {
        return Stream.of(
                Arguments.of("age = 10 AND salary = 1000", "AND(age EQUALS 10, salary EQUALS 1000)"),
                Arguments.of("age = 10 OR salary = 1000", "OR(age EQUALS 10, salary EQUALS 1000)"),
                Arguments.of(A + " AND " + B + " OR " + C,
                        "OR(AND(age EQUALS 10, salary EQUALS 1000), name EQUALS John)"),
                Arguments.of(A + " OR " + B + " AND " + C,
                        "OR(age EQUALS 10, AND(salary EQUALS 1000, name EQUALS John))"),
                Arguments.of(A + " AND " + B + " OR " + C + " AND " + D,
                        "OR(AND(age EQUALS 10, salary EQUALS 1000), AND(name EQUALS John, city EQUALS Rome))"),
                Arguments.of(A + " OR " + B + " AND " + C + " OR " + D,
                        "OR(age EQUALS 10, AND(salary EQUALS 1000, name EQUALS John), city EQUALS Rome)"),
                Arguments.of("NOT " + A + " AND " + B,
                        "AND(NOT(age EQUALS 10), salary EQUALS 1000)"),
                Arguments.of("NOT " + A + " OR " + B,
                        "OR(NOT(age EQUALS 10), salary EQUALS 1000)"),
                Arguments.of(A + " AND NOT " + B + " OR " + C,
                        "OR(AND(age EQUALS 10, NOT(salary EQUALS 1000)), name EQUALS John)"),
                Arguments.of(A + " AND " + B + " AND " + C,
                        "AND(age EQUALS 10, salary EQUALS 1000, name EQUALS John)"),
                Arguments.of(A + " OR " + B + " OR " + C,
                        "OR(age EQUALS 10, salary EQUALS 1000, name EQUALS John)"),
                Arguments.of(A + " AND " + B + " AND " + C + " OR " + D,
                        "OR(AND(age EQUALS 10, salary EQUALS 1000, name EQUALS John), city EQUALS Rome)"),
                Arguments.of(A + " OR " + B + " OR " + C + " AND " + D,
                        "OR(age EQUALS 10, salary EQUALS 1000, AND(name EQUALS John, city EQUALS Rome))"),
                Arguments.of("age = ?1 AND salary = ?2 OR name = ?3",
                        "OR(AND(age EQUALS ?1, salary EQUALS ?2), name EQUALS ?3)"),
                Arguments.of("age = :age OR salary = :salary AND name = :name",
                        "OR(age EQUALS age, AND(salary EQUALS salary, name EQUALS name))"),
                Arguments.of("age <> 10 OR salary = 1000 AND name = 'John'",
                        "OR(NOT(age EQUALS 10), AND(salary EQUALS 1000, name EQUALS John))")
        );
    }

    static Stream<Arguments> predicateOperators() {
        return Stream.of(
                Arguments.of("age BETWEEN 10 AND 20 OR name = 'John'",
                        "OR(age BETWEEN [10, 20], name EQUALS John)"),
                Arguments.of("name = 'John' OR age BETWEEN 10 AND 20 AND salary = 1000",
                        "OR(name EQUALS John, AND(age BETWEEN [10, 20], salary EQUALS 1000))"),
                Arguments.of("age NOT BETWEEN 10 AND 20 AND salary = 1000 OR name = 'John'",
                        "OR(AND(NOT(age BETWEEN [10, 20]), salary EQUALS 1000), name EQUALS John)"),
                Arguments.of("age NOT IN (10, 20) AND salary = 1000 OR name = 'John'",
                        "OR(AND(NOT(age IN [10, 20]), salary EQUALS 1000), name EQUALS John)"),
                Arguments.of("name NOT LIKE 'Jo%' OR age = 10 AND salary = 1000",
                        "OR(NOT(name LIKE Jo%), AND(age EQUALS 10, salary EQUALS 1000))"),
                Arguments.of("name IS NOT NULL AND age = 10 OR salary = 1000",
                        "OR(AND(NOT(name EQUALS null), age EQUALS 10), salary EQUALS 1000)"),
                Arguments.of("name IS NULL OR age = 10 AND salary = 1000",
                        "OR(name EQUALS null, AND(age EQUALS 10, salary EQUALS 1000))")
        );
    }

    @ParameterizedTest(name = "SELECT: {0}")
    @MethodSource({"expressions", "predicateOperators"})
    @DisplayName("Should resolve precedence in SELECT")
    void shouldResolveSelect(String predicate, String expected) {
        var query = new SelectParser().apply("FROM Person WHERE " + predicate, "Person");
        assertThat(render(query.where().orElseThrow())).isEqualTo(expected);
    }

    @ParameterizedTest(name = "DELETE: {0}")
    @MethodSource({"expressions", "predicateOperators"})
    @DisplayName("Should resolve precedence in DELETE")
    void shouldResolveDelete(String predicate, String expected) {
        var query = new DeleteParser().apply("DELETE FROM Person WHERE " + predicate);
        assertThat(render(query.where().orElseThrow())).isEqualTo(expected);
    }

    @ParameterizedTest(name = "UPDATE: {0}")
    @MethodSource({"expressions", "predicateOperators"})
    @DisplayName("Should resolve precedence in UPDATE")
    void shouldResolveUpdate(String predicate, String expected) {
        var query = new UpdateParser().apply("UPDATE Person SET active = true WHERE " + predicate);
        assertThat(render(query.where().orElseThrow())).isEqualTo(expected);
    }

    @ParameterizedTest(name = "Parser instance is reusable: {0}")
    @MethodSource("expressions")
    @DisplayName("Should not leak state between executions of the same parser")
    void shouldNotLeakState(String predicate, String expected) {
        var parser = new SelectParser();
        parser.apply("FROM Person WHERE name = 'x' AND age = 1 OR city = 'y'", "Person");
        var query = parser.apply("FROM Person WHERE " + predicate, "Person");
        assertThat(render(query.where().orElseThrow())).isEqualTo(expected);
    }

    @DisplayName("Should keep rejecting parentheses")
    @ParameterizedTest
    @MethodSource("parenthesis")
    void shouldRejectParenthesis(String predicate) {
        assertThatThrownBy(() -> new SelectParser().apply("FROM Person WHERE " + predicate, "Person"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    static Stream<String> parenthesis() {
        return Stream.of("(age = 10 OR salary = 1000) AND name = 'John'", "NOT (age = 10)");
    }

    static String render(Where where) {
        return render(where.condition());
    }

    static String render(QueryCondition condition) {
        return switch (condition.condition()) {
            case AND, OR -> condition.condition() + children(condition);
            case NOT -> "NOT" + children(condition);
            default -> condition.name() + " " + condition.condition() + " " + renderValue(condition.value());
        };
    }

    private static String renderValue(QueryValue<?> value) {
        if (value instanceof NullQueryValue) {
            return "null";
        }
        if (value.get() instanceof QueryValue<?>[] values) {
            return Arrays.stream(values).map(v -> String.valueOf(v.get()))
                    .collect(Collectors.joining(", ", "[", "]"));
        }
        return String.valueOf(value.get());
    }

    private static String children(QueryCondition condition) {
        var values = (ConditionQueryValue) condition.value();
        return values.get().stream().map(BooleanOperatorPrecedenceTest::render)
                .collect(Collectors.joining(", ", "(", ")"));
    }
}
