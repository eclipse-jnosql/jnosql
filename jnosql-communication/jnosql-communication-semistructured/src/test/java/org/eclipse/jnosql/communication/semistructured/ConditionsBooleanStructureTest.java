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
package org.eclipse.jnosql.communication.semistructured;

import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.query.data.DeleteParser;
import org.eclipse.jnosql.communication.query.data.SelectParser;
import org.eclipse.jnosql.communication.query.data.UpdateParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Boolean structure preserved by the condition conversion")
class ConditionsBooleanStructureTest {

    private final CommunicationObserverParser observer = mock(CommunicationObserverParser.class);

    private final Element a = Element.of("a", 1);
    private final Element b = Element.of("b", 2);
    private final Element c = Element.of("c", 3);
    private final Element d = Element.of("d", 4);

    @BeforeEach
    void setUp() {
        when(observer.fireConditionField(any(), anyString())).thenAnswer(invocation -> invocation.getArgument(1));
    }

    static Stream<Arguments> parsed() {
        return Stream.of(
                Arguments.of("a = 1 AND b = 2 OR c = 3", "OR(AND(a EQUALS 1, b EQUALS 2), c EQUALS 3)"),
                Arguments.of("a = 1 OR b = 2 AND c = 3", "OR(a EQUALS 1, AND(b EQUALS 2, c EQUALS 3))"),
                Arguments.of("a = 1 AND b = 2 OR c = 3 AND d = 4",
                        "OR(AND(a EQUALS 1, b EQUALS 2), AND(c EQUALS 3, d EQUALS 4))"),
                Arguments.of("NOT a = 1 AND b = 2", "AND(NOT(a EQUALS 1), b EQUALS 2)"),
                Arguments.of("a = 1 AND NOT b = 2 OR c = 3", "OR(AND(a EQUALS 1, NOT(b EQUALS 2)), c EQUALS 3)"),
                Arguments.of("a = 1 AND b = 2 AND c = 3", "AND(a EQUALS 1, b EQUALS 2, c EQUALS 3)"),
                Arguments.of("(a = 1 OR b = 2) AND c = 3", "AND(OR(a EQUALS 1, b EQUALS 2), c EQUALS 3)"),
                Arguments.of("a = 1 AND (b = 2 OR c = 3)", "AND(a EQUALS 1, OR(b EQUALS 2, c EQUALS 3))"),
                Arguments.of("(a = 1 OR b = 2) AND (c = 3 OR d = 4)",
                        "AND(OR(a EQUALS 1, b EQUALS 2), OR(c EQUALS 3, d EQUALS 4))"),
                Arguments.of("NOT (a = 1 AND b = 2)", "NOT(AND(a EQUALS 1, b EQUALS 2))"),
                Arguments.of("NOT (a = 1 OR b = 2)", "NOT(OR(a EQUALS 1, b EQUALS 2))")
        );
    }

    @ParameterizedTest(name = "SELECT {0}")
    @MethodSource("parsed")
    @DisplayName("Should keep the grouping of a parsed SELECT")
    void shouldConvertSelect(String predicate, String expected) {
        var where = new SelectParser().apply("FROM Person WHERE " + predicate, "Person").where().orElseThrow();
        assertThat(render(Conditions.getCondition(where, Params.newParams(), observer, "Person")))
                .isEqualTo(expected);
    }

    @ParameterizedTest(name = "DELETE {0}")
    @MethodSource("parsed")
    @DisplayName("Should keep the grouping of a parsed DELETE")
    void shouldConvertDelete(String predicate, String expected) {
        var where = new DeleteParser().apply("DELETE FROM Person WHERE " + predicate).where().orElseThrow();
        assertThat(render(Conditions.getCondition(where, Params.newParams(), observer, "Person")))
                .isEqualTo(expected);
    }

    @ParameterizedTest(name = "UPDATE {0}")
    @MethodSource("parsed")
    @DisplayName("Should keep the grouping of a parsed UPDATE")
    void shouldConvertUpdate(String predicate, String expected) {
        var where = new UpdateParser().apply("UPDATE Person SET x = 1 WHERE " + predicate).where().orElseThrow();
        assertThat(render(Conditions.getCondition(where, Params.newParams(), observer, "Person")))
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("Should keep AND(OR(A, B), C) built programmatically")
    void shouldKeepOrInsideAnd() {
        var condition = CriteriaCondition.and(CriteriaCondition.or(CriteriaCondition.eq(a), CriteriaCondition.eq(b)),
                CriteriaCondition.eq(c));
        assertThat(render(condition)).isEqualTo("AND(OR(a EQUALS 1, b EQUALS 2), c EQUALS 3)");
    }

    @Test
    @DisplayName("Should keep AND(OR(A, B), OR(C, D)) built programmatically")
    void shouldKeepTwoOrsInsideAnd() {
        var condition = CriteriaCondition.and(
                CriteriaCondition.or(CriteriaCondition.eq(a), CriteriaCondition.eq(b)),
                CriteriaCondition.or(CriteriaCondition.eq(c), CriteriaCondition.eq(d)));
        assertThat(render(condition))
                .isEqualTo("AND(OR(a EQUALS 1, b EQUALS 2), OR(c EQUALS 3, d EQUALS 4))");
    }

    @Test
    @DisplayName("Should keep OR(AND(A, B), C) built with the fluent API")
    void shouldKeepAndInsideOr() {
        var condition = CriteriaCondition.eq(a).and(CriteriaCondition.eq(b)).or(CriteriaCondition.eq(c));
        assertThat(render(condition)).isEqualTo("OR(AND(a EQUALS 1, b EQUALS 2), c EQUALS 3)");
    }

    @Test
    @DisplayName("Should keep negation of a nested condition")
    void shouldKeepNegation() {
        var condition = CriteriaCondition.or(CriteriaCondition.eq(a).and(CriteriaCondition.eq(b)).negate(),
                CriteriaCondition.eq(c));
        assertThat(render(condition)).isEqualTo("OR(NOT(AND(a EQUALS 1, b EQUALS 2)), c EQUALS 3)");
    }

    private static String render(CriteriaCondition condition) {
        return switch (condition.condition()) {
            case AND, OR -> condition.condition() + condition.element().get(new TypeReference<List<CriteriaCondition>>() {
                    }).stream().map(ConditionsBooleanStructureTest::render)
                    .collect(Collectors.joining(", ", "(", ")"));
            case NOT -> "NOT(" + render(condition.element().get(CriteriaCondition.class)) + ")";
            default -> condition.element().name() + " " + condition.condition() + " " + condition.element().get();
        };
    }
}
