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

import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.query.ConditionQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.TemporalQueryValue;
import org.eclipse.jnosql.communication.query.ValueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Temporal special expressions")
class TemporalExpressionTest {

    private static final LocalDateTime REFERENCE = LocalDateTime.of(2026, 11, 25, 23, 59, 58);

    static Stream<Arguments> expressions() {
        return Stream.of(
                Arguments.of("LOCAL DATE", TemporalQueryValue.LOCAL_DATE),
                Arguments.of("local date", TemporalQueryValue.LOCAL_DATE),
                Arguments.of("Local Date", TemporalQueryValue.LOCAL_DATE),
                Arguments.of("LOCAL   DATE", TemporalQueryValue.LOCAL_DATE),
                Arguments.of("LOCAL\tDATE", TemporalQueryValue.LOCAL_DATE),
                Arguments.of("LOCAL\nDATE", TemporalQueryValue.LOCAL_DATE),
                Arguments.of("LOCAL TIME", TemporalQueryValue.LOCAL_TIME),
                Arguments.of("local time", TemporalQueryValue.LOCAL_TIME),
                Arguments.of("LoCaL\t \nTiMe", TemporalQueryValue.LOCAL_TIME),
                Arguments.of("LOCAL DATETIME", TemporalQueryValue.LOCAL_DATETIME),
                Arguments.of("local datetime", TemporalQueryValue.LOCAL_DATETIME),
                Arguments.of("LOCAL  \t DATETIME", TemporalQueryValue.LOCAL_DATETIME)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("expressions")
    @DisplayName("Should parse the expression in SELECT, DELETE and UPDATE conditions")
    void shouldParseExpression(String expression, TemporalQueryValue expected) {
        var select = new SelectParser().apply("FROM Person WHERE birth < " + expression, "Person");
        var delete = new DeleteParser().apply("DELETE FROM Person WHERE birth < " + expression);
        var update = new UpdateParser().apply("UPDATE Person SET active = FALSE WHERE birth < " + expression);

        assertThat(select.where().orElseThrow().condition().value()).isSameAs(expected);
        assertThat(delete.where().orElseThrow().condition().value()).isSameAs(expected);
        assertThat(update.where().orElseThrow().condition().value()).isSameAs(expected);
    }

    @Test
    @DisplayName("Should keep the predicate name and comparison operator")
    void shouldKeepPredicate() {
        var condition = new SelectParser().apply("FROM Subscription WHERE expirationDate <= LOCAL DATE",
                "Subscription").where().orElseThrow().condition();

        assertThat(condition.name()).isEqualTo("expirationDate");
        assertThat(condition.condition()).isEqualTo(org.eclipse.jnosql.communication.Condition.LESSER_EQUALS_THAN);
        assertThat(condition.value()).isSameAs(TemporalQueryValue.LOCAL_DATE);
    }

    @Test
    @DisplayName("Should accept the expression as an UPDATE assignment")
    void shouldParseAssignment() {
        var update = new UpdateParser().apply(
                "UPDATE Task SET completedAt = LOCAL DATETIME WHERE scheduledAt < LOCAL  DATETIME");

        assertThat(update.set()).hasSize(1);
        assertThat(update.set().get(0).name()).isEqualTo("completedAt");
        assertThat(update.set().get(0).value()).isSameAs(TemporalQueryValue.LOCAL_DATETIME);
        assertThat(update.where().orElseThrow().condition().value()).isSameAs(TemporalQueryValue.LOCAL_DATETIME);
    }

    @Test
    @DisplayName("Should accept the expression as a BETWEEN endpoint")
    void shouldParseBetween() {
        var condition = new SelectParser().apply(
                "FROM Event WHERE eventDate BETWEEN :startDate AND LOCAL DATE", "Event")
                .where().orElseThrow().condition();

        var values = (QueryValue<?>[]) condition.value().get();
        assertThat(values).hasSize(2);
        assertThat(values[0].get()).isEqualTo("startDate");
        assertThat(values[1]).isSameAs(TemporalQueryValue.LOCAL_DATE);
    }

    @Test
    @DisplayName("Should preserve Boolean grouping and negation around temporal expressions")
    void shouldPreserveGrouping() {
        var grouped = new SelectParser().apply(
                "FROM Subscription WHERE (expirationDate < LOCAL DATE OR active = FALSE) AND suspended = FALSE",
                "Subscription").where().orElseThrow().condition();

        assertThat(BooleanOperatorPrecedenceTest.render(grouped.value() instanceof ConditionQueryValue c
                ? c.get().get(0) : grouped))
                .startsWith("OR(expirationDate LESSER_THAN ");
        var or = ((ConditionQueryValue) grouped.value()).get().get(0);
        assertThat(((ConditionQueryValue) or.value()).get().get(0).value())
                .isSameAs(TemporalQueryValue.LOCAL_DATE);

        var negated = new SelectParser().apply(
                "FROM Task WHERE NOT (scheduledAt < LOCAL DATETIME OR completed = TRUE)", "Task")
                .where().orElseThrow().condition();
        QueryCondition inner = ((ConditionQueryValue) negated.value()).get().get(0);
        assertThat(inner.condition()).isEqualTo(org.eclipse.jnosql.communication.Condition.OR);
        assertThat(((ConditionQueryValue) inner.value()).get().get(0).value())
                .isSameAs(TemporalQueryValue.LOCAL_DATETIME);
    }

    @Test
    @DisplayName("Should combine temporal expressions with named and positional parameters")
    void shouldCombineWithParameters() {
        var named = new SelectParser().apply(
                "FROM Event WHERE eventDate = LOCAL DATE AND name = :name", "Event").where().orElseThrow().condition();
        var positional = new SelectParser().apply(
                "FROM Event WHERE eventTime < LOCAL TIME AND name = ?1", "Event").where().orElseThrow().condition();

        var namedChildren = ((ConditionQueryValue) named.value()).get();
        var positionalChildren = ((ConditionQueryValue) positional.value()).get();
        assertThat(namedChildren.get(0).value()).isSameAs(TemporalQueryValue.LOCAL_DATE);
        assertThat(namedChildren.get(1).value().get()).isEqualTo("name");
        assertThat(positionalChildren.get(0).value()).isSameAs(TemporalQueryValue.LOCAL_TIME);
        assertThat(positionalChildren.get(1).value().get()).isEqualTo("?1");
    }

    @ParameterizedTest
    @DisplayName("Should reject incomplete, unknown and contiguous temporal expressions")
    @ValueSource(strings = {
            "FROM Person WHERE birth < LOCAL",
            "FROM Person WHERE birth < LOCAL UNKNOWN",
            "FROM Person WHERE birth < LOCAL DATE DATE",
            "FROM Person WHERE birth < LOCAL DATE extra",
            "FROM Person WHERE birth < DATE"
    })
    void shouldRejectInvalid(String query) {
        assertThatThrownBy(() -> new SelectParser().apply(query, "Person"))
                .isInstanceOfAny(QueryException.class, UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should not accept a contiguous LOCALDATE as a temporal expression")
    void shouldNotTreatContiguousKeywordAsTemporal() {
        assertThatThrownBy(() -> new SelectParser().apply("FROM Person WHERE birth < LOCALDATE", "Person"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should classify the expressions as temporal values")
    void shouldClassify() {
        for (var value : TemporalQueryValue.values()) {
            assertThat(value.type()).isEqualTo(ValueType.TEMPORAL);
        }
    }

    @Test
    @DisplayName("Should resolve each expression from the reference with the expected Java type")
    void shouldResolveFromReference() {
        assertThat(TemporalQueryValue.LOCAL_DATE.from(REFERENCE)).isEqualTo(LocalDate.of(2026, 11, 25));
        assertThat(TemporalQueryValue.LOCAL_TIME.from(REFERENCE)).isEqualTo(LocalTime.of(23, 59, 58));
        assertThat(TemporalQueryValue.LOCAL_DATETIME.from(REFERENCE)).isEqualTo(REFERENCE);
    }

    @Test
    @DisplayName("Should resolve to the current value each time it is read")
    void shouldBeDeferred() {
        var before = LocalDateTime.now();
        var first = (LocalDateTime) TemporalQueryValue.LOCAL_DATETIME.get();
        var second = (LocalDateTime) TemporalQueryValue.LOCAL_DATETIME.get();
        var after = LocalDateTime.now();

        assertThat(first).isBetween(before, after);
        assertThat(second).isBetween(first, after);
        assertThat(TemporalQueryValue.LOCAL_DATE.get()).isInstanceOf(LocalDate.class);
        assertThat(TemporalQueryValue.LOCAL_TIME.get()).isInstanceOf(LocalTime.class);
    }

    @Test
    @DisplayName("Should reject a null reference")
    void shouldRejectNullReference() {
        assertThatThrownBy(() -> TemporalQueryValue.LOCAL_DATE.from(null))
                .isInstanceOf(NullPointerException.class);
    }
}
