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
import org.eclipse.jnosql.communication.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Temporal expressions in prepared statements")
class PreparedTemporalLifecycleTest {

    private static final LocalDateTime T1 = LocalDateTime.of(2026, 3, 4, 23, 59, 59);
    private static final LocalDateTime T2 = LocalDateTime.of(2026, 3, 5, 0, 0, 1);
    private static final LocalDateTime T3 = LocalDateTime.of(2026, 3, 6, 8, 30, 0);

    private final CommunicationObserverParser observer = CommunicationObserverParser.EMPTY;
    private DatabaseManager manager;
    private TemporalValueTest.MutableClock clock;

    @BeforeEach
    void setUp() {
        manager = Mockito.mock(DatabaseManager.class);
        clock = new TemporalValueTest.MutableClock(T1);
    }

    private CommunicationPreparedStatement select(String query) {
        return new SelectQueryParser().prepare(query, null, manager, observer, clock);
    }

    private SelectQuery lastSelect() {
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(manager, Mockito.atLeastOnce()).select(captor.capture());
        return captor.getAllValues().getLast();
    }

    private static Object valueOf(CriteriaCondition condition) {
        return condition.element().value().get();
    }

    @Test
    @DisplayName("should resolve at execution and not at preparation")
    void shouldResolveAtExecution() {
        var statement = select("FROM Subscription WHERE expirationDate < LOCAL DATE");
        clock.set(T2);
        statement.result();
        var condition = lastSelect().condition().orElseThrow();
        assertThat(condition.condition()).isEqualTo(Condition.LESSER_THAN);
        assertThat(condition.element().name()).isEqualTo("expirationDate");
        assertThat(valueOf(condition)).isEqualTo(LocalDate.of(2026, 3, 5));
    }

    @Test
    @DisplayName("should give each execution its own value across a date boundary")
    void shouldRefreshEachExecution() {
        var statement = select("FROM Subscription WHERE expirationDate < LOCAL DATE");

        clock.set(T1);
        statement.result();
        var first = lastSelect().condition().orElseThrow();
        assertThat(valueOf(first)).isEqualTo(LocalDate.of(2026, 3, 4));

        clock.set(T2);
        statement.result();
        assertThat(valueOf(first)).isEqualTo(LocalDate.of(2026, 3, 5));

        clock.set(T3);
        statement.result();
        assertThat(valueOf(first)).isEqualTo(LocalDate.of(2026, 3, 6));
    }

    @Test
    @DisplayName("should keep a consistent value during one execution")
    void shouldBeConsistentWithinExecution() {
        var statement = select("FROM Event WHERE eventDate = LOCAL DATE AND eventTime < LOCAL TIME"
                + " AND createdAt <= LOCAL DATETIME");
        clock.set(T2);
        statement.result();
        var and = lastSelect().condition().orElseThrow();
        @SuppressWarnings("unchecked")
        var children = (List<CriteriaCondition>) and.element().value().get();
        var date = (Value) children.get(0).element().value();
        var time = (Value) children.get(1).element().value();
        var dateTime = (Value) children.get(2).element().value();

        clock.set(T3);
        assertThat(date.get()).isEqualTo(T2.toLocalDate());
        assertThat(time.get()).isEqualTo(T2.toLocalTime());
        assertThat(dateTime.get()).isEqualTo(T2);
        assertThat(date.get()).isEqualTo(date.get());
    }

    @Test
    @DisplayName("should not share state between independent statements")
    void shouldIsolateStatements() {
        var first = select("FROM Task WHERE scheduledAt < LOCAL DATETIME");
        clock.set(T1);
        first.result();
        var firstCondition = lastSelect().condition().orElseThrow();

        var second = select("FROM Task WHERE scheduledAt < LOCAL DATETIME");
        clock.set(T3);
        second.result();

        assertThat(valueOf(firstCondition)).isEqualTo(T1);
        assertThat(valueOf(lastSelect().condition().orElseThrow())).isEqualTo(T3);
    }

    @Test
    @DisplayName("should keep binding parameters")
    void shouldBindParameters() {
        var statement = select("FROM Event WHERE eventDate BETWEEN :start AND LOCAL DATE");
        statement.bind("start", LocalDate.of(2026, 1, 1));
        clock.set(T2);
        statement.result();
        @SuppressWarnings("unchecked")
        var values = (List<Value>) lastSelect().condition().orElseThrow().element().value().get();
        assertThat(values.get(0).get()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(values.get(1).get()).isEqualTo(LocalDate.of(2026, 3, 5));
    }

    @Test
    @DisplayName("should match the equivalent parameterized query")
    void shouldMatchParameter() {
        var temporal = select("FROM Subscription WHERE expirationDate < LOCAL DATE");
        var parameter = select("FROM Subscription WHERE expirationDate < :today");
        clock.set(T2);
        parameter.bind("today", T2.toLocalDate());
        temporal.result();
        var temporalCondition = lastSelect().condition().orElseThrow();
        parameter.result();
        var parameterCondition = lastSelect().condition().orElseThrow();

        assertThat(temporalCondition.condition()).isEqualTo(parameterCondition.condition());
        assertThat(valueOf(temporalCondition)).isEqualTo(valueOf(parameterCondition));
        assertThat(valueOf(temporalCondition)).isInstanceOf(LocalDate.class);
    }

    @Test
    @DisplayName("should preserve Boolean grouping and negation")
    void shouldPreserveGrouping() {
        var statement = select("FROM Task WHERE NOT (scheduledAt < LOCAL DATETIME OR completed = TRUE)");
        clock.set(T3);
        statement.result();
        var not = lastSelect().condition().orElseThrow();
        assertThat(not.condition()).isEqualTo(Condition.NOT);
        var or = (CriteriaCondition) not.element().value().get();
        assertThat(or.condition()).isEqualTo(Condition.OR);
        @SuppressWarnings("unchecked")
        var children = (List<CriteriaCondition>) or.element().value().get();
        assertThat(valueOf(children.get(0))).isEqualTo(T3);
        assertThat(valueOf(children.get(1))).isEqualTo(true);
    }

    @Test
    @DisplayName("should resolve at each delete execution")
    void shouldRefreshDelete() {
        var statement = new DeleteQueryParser().prepare("DELETE FROM Task WHERE scheduledAt < LOCAL DATETIME",
                manager, observer, clock);
        clock.set(T2);
        statement.result();
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        Mockito.verify(manager).delete(captor.capture());
        var condition = captor.getValue().condition().orElseThrow();
        assertThat(valueOf(condition)).isEqualTo(T2);

        clock.set(T3);
        statement.result();
        assertThat(valueOf(condition)).isEqualTo(T3);
    }

    @Test
    @DisplayName("should resolve assignments and predicates with one reference on update")
    void shouldRefreshUpdate() {
        var statement = new UpdateQueryParser().prepare(
                "UPDATE Task SET completedAt = LOCAL DATETIME WHERE scheduledAt < LOCAL DATETIME",
                manager, observer, clock);
        clock.set(T2);
        statement.result();
        var captor = ArgumentCaptor.forClass(UpdateQuery.class);
        Mockito.verify(manager).update(captor.capture());
        var update = captor.getValue();
        var set = update.sets().getFirst();
        var condition = update.where().orElseThrow();
        assertThat(set.name()).isEqualTo("completedAt");

        clock.set(T3);
        assertThat(set.value().get()).isEqualTo(T2);
        assertThat(valueOf(condition)).isEqualTo(T2);

        statement.result();
        assertThat(set.value().get()).isEqualTo(T3);
        assertThat(valueOf(condition)).isEqualTo(T3);
    }

    @Test
    @DisplayName("should resolve at each count execution")
    void shouldRefreshCount() {
        var statement = select("SELECT COUNT(this) FROM Task WHERE scheduledAt < LOCAL TIME");
        clock.set(T2);
        statement.count();
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(manager).count(captor.capture());
        var condition = captor.getValue().condition().orElseThrow();
        assertThat(valueOf(condition)).isEqualTo(LocalTime.of(0, 0, 1));
        clock.set(T3);
        statement.count();
        assertThat(valueOf(condition)).isEqualTo(T3.toLocalTime());
    }
}
