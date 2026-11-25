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

import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JDQL function integration")
class FunctionIntegrationTest {

    private final CommunicationObserverParser observer = CommunicationObserverParser.EMPTY;
    private DatabaseManager manager;

    @BeforeEach
    void setUp() {
        manager = Mockito.mock(DatabaseManager.class);
    }

    private CommunicationPreparedStatement select(String query) {
        return new SelectQueryParser().prepare(query, null, manager, observer);
    }

    private CommunicationPreparedStatement delete(String query) {
        return new DeleteQueryParser().prepare(query, manager, observer);
    }

    private CommunicationPreparedStatement update(String query) {
        return new UpdateQueryParser().prepare(query, manager, observer);
    }

    private SelectQuery lastSelect() {
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(manager, Mockito.atLeastOnce()).select(captor.capture());
        return captor.getAllValues().getLast();
    }

    private UpdateQuery lastUpdate() {
        var captor = ArgumentCaptor.forClass(UpdateQuery.class);
        Mockito.verify(manager, Mockito.atLeastOnce()).update(captor.capture());
        return captor.getAllValues().getLast();
    }

    private static Value valueOf(CriteriaCondition condition) {
        return condition.element().value();
    }

    @SuppressWarnings("unchecked")
    private static List<CriteriaCondition> children(CriteriaCondition condition) {
        return (List<CriteriaCondition>) condition.element().value().get();
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "UPPER('otavio')|OTAVIO",
            "upper('otavio')|OTAVIO",
            "UpPeR('otavio')|OTAVIO",
            "LOWER('OTAVIO')|otavio",
            "LEFT('otavio', 3)|ota",
            "LEFT('otavio', 19)|otavio",
            "RIGHT('otavio', 3)|vio",
            "UPPER(LEFT('otavio', 19))|OTAVIO",
            "upper(LeFt('otavio', 19))|OTAVIO"
    })
    @DisplayName("should deliver evaluated string literals to the driver")
    void shouldEvaluateStringLiterals(String expression, String expected) {
        select("FROM Person WHERE name = " + expression).result();
        var condition = lastSelect().condition().orElseThrow();
        assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
        assertThat(condition.element().name()).isEqualTo("name");
        assertThat(valueOf(condition).get()).isEqualTo(expected);
        assertThat(valueOf(condition).get(String.class)).isEqualTo(expected);
        assertThat(valueOf(condition).isInstanceOf(String.class)).isTrue();
        assertThat(valueOf(condition).isNull()).isFalse();
    }

    @Test
    @DisplayName("should deliver numeric results with exact Java types")
    void shouldEvaluateNumbers() {
        select("FROM Person WHERE nameLength = LENGTH('otavio')").result();
        assertThat(valueOf(lastSelect().condition().orElseThrow()).get()).isEqualTo(6);
        select("FROM Person WHERE age = ABS(-30)").result();
        assertThat(valueOf(lastSelect().condition().orElseThrow()).get()).isEqualTo(30);
    }

    @Test
    @DisplayName("should be equivalent to a literal and to a bound parameter")
    void shouldBeEquivalentToLiteralAndParameter() {
        select("FROM Person WHERE name = 'OTAVIO'").result();
        var literal = lastSelect().condition().orElseThrow();
        select("FROM Person WHERE name = :name").bind("name", "OTAVIO").result();
        var parameter = lastSelect().condition().orElseThrow();
        select("FROM Person WHERE name = UPPER(:name)").bind("name", "otavio").result();
        var function = lastSelect().condition().orElseThrow();

        for (var condition : List.of(literal, parameter, function)) {
            assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(condition.element().name()).isEqualTo("name");
            assertThat(valueOf(condition).get()).isEqualTo("OTAVIO");
        }
    }

    @Test
    @DisplayName("should not evaluate at preparation nor at binding")
    void shouldNotEvaluatePrematurely() {
        var statement = select("FROM Person WHERE name = UPPER(:name)");
        statement.bind("name", "otavio");
        Mockito.verifyNoInteractions(manager);
    }

    @Test
    @DisplayName("should build a fresh function tree per execution with no stale results")
    void shouldRefreshNestedTreePerExecution() {
        var statement = select("FROM Person WHERE name = UPPER(LEFT(:name, :length))");

        statement.bind("name", "otavio").bind("length", 3).result();
        var first = valueOf(lastSelect().condition().orElseThrow());
        statement.bind("name", "jakarta").bind("length", 4).result();
        var second = valueOf(lastSelect().condition().orElseThrow());
        statement.bind("name", "java").bind("length", 19).result();
        var third = valueOf(lastSelect().condition().orElseThrow());

        assertThat(first.get()).isEqualTo("OTA");
        assertThat(second.get()).isEqualTo("JAKA");
        assertThat(third.get()).isEqualTo("JAVA");
        assertThat(first.get()).isEqualTo("OTA");
    }

    @Test
    @DisplayName("should not let a delayed value observe a later rebind")
    void shouldIsolateDelayedConsumption() {
        var statement = select("FROM Person WHERE name = UPPER(:name)");
        statement.bind("name", "otavio").result();
        var delayed = valueOf(lastSelect().condition().orElseThrow());
        statement.bind("name", "jakarta").result();
        assertThat(delayed.get()).isEqualTo("OTAVIO");
        assertThat(valueOf(lastSelect().condition().orElseThrow()).get()).isEqualTo("JAKARTA");
    }

    @Test
    @DisplayName("should not share state between prepared statements")
    void shouldIsolateStatements() {
        var one = select("FROM Person WHERE name = UPPER(:name)");
        var two = select("FROM Person WHERE name = UPPER(:name)");
        one.bind("name", "one").result();
        var first = valueOf(lastSelect().condition().orElseThrow());
        two.bind("name", "two").result();
        var second = valueOf(lastSelect().condition().orElseThrow());
        assertThat(first.get()).isEqualTo("ONE");
        assertThat(second.get()).isEqualTo("TWO");
    }

    @Test
    @DisplayName("should support positional parameters and rebinding")
    void shouldSupportPositional() {
        var statement = select("FROM Person WHERE name = LEFT(?1, ?2)");
        statement.bind(1, "otavio").bind(2, 3).result();
        var first = valueOf(lastSelect().condition().orElseThrow());
        statement.bind(1, "jakarta").bind(2, 4).result();
        assertThat(first.get()).isEqualTo("ota");
        assertThat(valueOf(lastSelect().condition().orElseThrow()).get()).isEqualTo("jaka");
    }

    @Test
    @DisplayName("should support a parameter shared between predicates and functions")
    void shouldShareParameters() {
        var statement = select("FROM Person WHERE name = :name AND alias = UPPER(:name) AND title = LEFT(:name, LENGTH(:name))");
        statement.bind("name", "ota").result();
        var kids = children(lastSelect().condition().orElseThrow());
        assertThat(valueOf(kids.get(0)).get()).isEqualTo("ota");
        assertThat(valueOf(kids.get(1)).get()).isEqualTo("OTA");
        assertThat(valueOf(kids.get(2)).get()).isEqualTo("ota");
    }

    @Test
    @DisplayName("should fail for a missing parameter and recover on the next execution")
    void shouldRecoverAfterFailure() {
        var statement = select("FROM Person WHERE name = UPPER(:name)");
        assertThatThrownBy(statement::result).isInstanceOf(QueryException.class);
        Mockito.verifyNoInteractions(manager);
        statement.bind("name", "ok").result();
        assertThat(valueOf(lastSelect().condition().orElseThrow()).get()).isEqualTo("OK");
    }

    @Test
    @DisplayName("should surface evaluation errors as QueryException keeping the cause")
    void shouldWrapEvaluationErrors() {
        var statement = select("FROM Person WHERE name = LEFT(:name, :length)");
        statement.bind("name", "abc").bind("length", -1).result();
        var value = valueOf(lastSelect().condition().orElseThrow());
        assertThatThrownBy(value::get).isInstanceOf(QueryException.class).hasCauseInstanceOf(IllegalArgumentException.class);

        statement.bind("length", 2).result();
        assertThat(valueOf(lastSelect().condition().orElseThrow()).get()).isEqualTo("ab");
    }

    @Test
    @DisplayName("should report a negative literal length when the value is read")
    void shouldReportNegativeLiteralLength() {
        select("FROM Person WHERE name = LEFT('abc', -1)").result();
        var value = valueOf(lastSelect().condition().orElseThrow());
        assertThatThrownBy(value::get).isInstanceOf(QueryException.class);
    }

    @Test
    @DisplayName("should reject a wrongly typed bound parameter when read")
    void shouldRejectWrongType() {
        var statement = select("FROM Person WHERE name = UPPER(:name)");
        statement.bind("name", 10).result();
        var value = valueOf(lastSelect().condition().orElseThrow());
        assertThatThrownBy(value::get).isInstanceOf(QueryException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "name = UPPER(123)", "name = LOWER(TRUE)", "name = LEFT('abc', '2')",
            "name = LEFT('abc', 1.5)", "age = ABS('10')", "name = LEFT('abc', LOCAL DATE)",
            "name = UPPER(LOCAL DATE)"
    })
    @DisplayName("should reject structurally invalid functions with QueryException")
    void shouldRejectInvalidFunctions(String predicate) {
        assertThatThrownBy(() -> select("FROM Person WHERE " + predicate))
                .isInstanceOf(QueryException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"name = UPPER()", "name = LEFT('abc')", "name = UPPER('abc', 'def')"})
    @DisplayName("should reject wrong arity at parsing")
    void shouldRejectArity(String predicate) {
        assertThatThrownBy(() -> select("FROM Person WHERE " + predicate))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("should reject native assignments explicitly because they are not supported yet")
    void shouldRejectNative() {
        assertThatThrownBy(() -> update("UPDATE Person SET name = UPPER(other) WHERE id = :id"))
                .isInstanceOf(QueryException.class).hasMessageContaining("Native function");
        assertThatThrownBy(() -> update("UPDATE Person SET name = UPPER(name) WHERE id = :id"))
                .isInstanceOf(QueryException.class);
        Mockito.verifyNoInteractions(manager);
    }

    @Test
    @DisplayName("should execute DELETE with the evaluated predicate")
    void shouldDelete() {
        var statement = delete("DELETE FROM Person WHERE name = UPPER(:name)");
        statement.bind("name", "otavio").result();
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        Mockito.verify(manager).delete(captor.capture());
        var condition = captor.getValue().condition().orElseThrow();
        assertThat(valueOf(condition).get()).isEqualTo("OTAVIO");
    }

    @Test
    @DisplayName("should not delete when a parameter is missing")
    void shouldNotDeleteUnrestricted() {
        var statement = delete("DELETE FROM Person WHERE name = UPPER(:name)");
        assertThatThrownBy(statement::result).isInstanceOf(QueryException.class);
        Mockito.verifyNoInteractions(manager);
    }

    @Test
    @DisplayName("should evaluate functions in UPDATE SET and WHERE independently")
    void shouldUpdateSetAndWhere() {
        var statement = update("UPDATE Person SET name = UPPER(LEFT(:newName, 19)) WHERE name = LOWER(:oldName)");
        statement.bind("newName", "otavio").bind("oldName", "JAKARTA").result();
        var first = lastUpdate();
        statement.bind("newName", "java").bind("oldName", "EE").result();
        var second = lastUpdate();

        assertThat(first.sets().getFirst().value().get()).isEqualTo("OTAVIO");
        assertThat(valueOf(first.where().orElseThrow()).get()).isEqualTo("jakarta");
        assertThat(second.sets().getFirst().value().get()).isEqualTo("JAVA");
        assertThat(valueOf(second.where().orElseThrow()).get()).isEqualTo("ee");
    }

    @Test
    @DisplayName("should evaluate COUNT executions freshly")
    void shouldCount() {
        var statement = select("SELECT COUNT(this) FROM Person WHERE name = UPPER(:name)");
        statement.bind("name", "one").count();
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(manager).count(captor.capture());
        var first = valueOf(captor.getValue().condition().orElseThrow());
        statement.bind("name", "two").count();
        Mockito.verify(manager, Mockito.times(2)).count(captor.capture());
        assertThat(first.get()).isEqualTo("ONE");
        assertThat(valueOf(captor.getValue().condition().orElseThrow()).get()).isEqualTo("TWO");
    }

    @Test
    @DisplayName("should preserve Boolean structure around function leaves")
    void shouldPreserveBooleanStructure() {
        select("FROM Person WHERE name = UPPER(:name) OR (name = LOWER(:other) AND active = TRUE)")
                .bind("name", "a").bind("other", "B").result();
        var or = lastSelect().condition().orElseThrow();
        assertThat(or.condition()).isEqualTo(Condition.OR);
        var branches = children(or);
        assertThat(valueOf(branches.get(0)).get()).isEqualTo("A");
        assertThat(branches.get(1).condition()).isEqualTo(Condition.AND);
        assertThat(valueOf(children(branches.get(1)).get(0)).get()).isEqualTo("b");

        select("FROM Person WHERE NOT (name = UPPER(:name) OR age = ABS(:age))")
                .bind("name", "a").bind("age", -4).result();
        var not = lastSelect().condition().orElseThrow();
        assertThat(not.condition()).isEqualTo(Condition.NOT);
        var inner = (CriteriaCondition) not.element().value().get();
        assertThat(inner.condition()).isEqualTo(Condition.OR);
        assertThat(valueOf(children(inner).get(0)).get()).isEqualTo("A");
        assertThat(valueOf(children(inner).get(1)).get()).isEqualTo(4);
    }

    @Test
    @DisplayName("should support BETWEEN with function operands")
    void shouldSupportBetween() {
        select("FROM Person WHERE age BETWEEN ABS(:low) AND ABS(10)").bind("low", -3).result();
        var between = lastSelect().condition().orElseThrow();
        assertThat(between.condition()).isEqualTo(Condition.BETWEEN);
        var values = (List<?>) valueOf(between).get();
        assertThat(((Value) values.get(0)).get()).isEqualTo(3);
        assertThat(((Value) values.get(1)).get()).isEqualTo(10);
    }

    @Test
    @DisplayName("should keep temporal expressions working next to functions")
    void shouldCombineTemporal() {
        var clock = new TemporalValueTest.MutableClock(LocalDateTime.of(2026, 3, 4, 10, 0));
        var statement = new SelectQueryParser().prepare(
                "FROM Person WHERE name = UPPER(:name) AND birthday = LOCAL DATE", null, manager, observer, clock);
        statement.bind("name", "a").result();
        var first = children(lastSelect().condition().orElseThrow());
        clock.set(LocalDateTime.of(2026, 3, 5, 10, 0));
        statement.bind("name", "b").result();
        var second = children(lastSelect().condition().orElseThrow());
        assertThat(valueOf(first.get(0)).get()).isEqualTo("A");
        assertThat(valueOf(first.get(1)).get()).isEqualTo(LocalDate.of(2026, 3, 4));
        assertThat(valueOf(second.get(0)).get()).isEqualTo("B");
        assertThat(valueOf(second.get(1)).get()).isEqualTo(LocalDate.of(2026, 3, 5));
    }

    @Test
    @DisplayName("should reuse the memoized result within one value")
    void shouldMemoizeWithinValue() {
        select("FROM Person WHERE name = UPPER(:name)").bind("name", "x").result();
        var value = valueOf(lastSelect().condition().orElseThrow());
        assertThat(value.get()).isSameAs(value.get());
    }

    @Test
    @DisplayName("should reject direct execution of parameterized function queries")
    void shouldRejectDirectWithParameters() {
        assertThatThrownBy(() -> new SelectQueryParser().query("FROM Person WHERE name = UPPER(:name)", null, manager, observer))
                .isInstanceOf(QueryException.class);
    }

    @Test
    @DisplayName("should execute direct queries with literal functions")
    void shouldExecuteDirect() {
        new SelectQueryParser().query("FROM Person WHERE name = UPPER('otavio')", null, manager, observer);
        assertThat(valueOf(lastSelect().condition().orElseThrow()).get()).isEqualTo("OTAVIO");
    }

    @Test
    @DisplayName("should inspect operands and metadata without evaluating the function")
    void shouldInspectWithoutEvaluating() {
        var query = org.eclipse.jnosql.communication.query.data.SelectProvider.INSTANCE
                .apply("FROM Person WHERE UPPER(name) = UPPER(:name) AND alias = LEFT(:alias, 3)", "Person");
        var and = query.where().orElseThrow().condition();
        var operands = ((org.eclipse.jnosql.communication.query.ConditionQueryValue) and.rightOperand()).get();
        var left = (org.eclipse.jnosql.communication.query.Function) operands.get(0).leftOperand().get();
        var right = (org.eclipse.jnosql.communication.query.Function) operands.get(0).rightOperand().get();
        assertThat(left.isNative()).isTrue();
        assertThat(right.isNative()).isFalse();

        var statement = select("FROM Person WHERE alias = LEFT(:alias, 3)");
        var snapshot = statement.select().orElseThrow().condition().orElseThrow();
        assertThat(valueOf(snapshot)).isNotNull();
        assertThat(valueOf(snapshot).toString()).contains("LEFT");
        Mockito.verifyNoInteractions(manager);
    }
}
