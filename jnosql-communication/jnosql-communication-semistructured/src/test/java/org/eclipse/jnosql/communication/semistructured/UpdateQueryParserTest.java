/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.TypeReference;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateQueryParserTest {

    private final UpdateQueryParser parser = new UpdateQueryParser();

    private final DatabaseManager manager = Mockito.mock(DatabaseManager.class);

    private final CommunicationObserverParser observer = new CommunicationObserverParser() {
    };


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET name = 'Ada'"})
    void shouldReturnParserQuery(String query) {
        var captor = ArgumentCaptor.forClass(UpdateQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).update(captor.capture());
        var updateQuery = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(updateQuery.condition()).isEmpty();
            soft.assertThat(updateQuery.name()).isEqualTo("entity");
            soft.assertThat(updateQuery.set()).isNotNull().hasSize(1)
                    .contains(Element.of("name", "Ada"));
        });
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = 10 WHERE stamina > 10.23"})
    void shouldReturnParserQuery11(String query) {
        var captor = ArgumentCaptor.forClass(UpdateQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).update(captor.capture());
        var updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(condition.condition()).isEqualTo(Condition.GREATER_THAN);
            soft.assertThat(condition.element()).isEqualTo(Element.of("stamina", 10.23));
            soft.assertThat(updateQuery.set()).isNotNull().hasSize(1)
                    .contains(Element.of("age", 10));
        });

    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = 10 WHERE stamina >= -10.23"})
    void shouldReturnParserQuery12(String query) {
        var captor = ArgumentCaptor.forClass(UpdateQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).update(captor.capture());
        var updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        var condition = updateQuery.condition().get();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(condition.condition()).isEqualTo(Condition.GREATER_EQUALS_THAN);
            soft.assertThat(condition.element()).isEqualTo(Element.of("stamina", -10.23));
            soft.assertThat(updateQuery.set()).isNotNull().hasSize(1)
                    .contains(Element.of("age", 10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = 10 WHERE stamina <= -10.23"})
    void shouldReturnParserQuery13(String query) {
        var captor = ArgumentCaptor.forClass(UpdateQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).update(captor.capture());
        var updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(condition.condition()).isEqualTo(Condition.LESSER_EQUALS_THAN);
            soft.assertThat(condition.element()).isEqualTo(Element.of("stamina", -10.23));
            soft.assertThat(updateQuery.set()).isNotNull().hasSize(1)
                    .contains(Element.of("age", 10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = 10 WHERE stamina < -10.23"})
    void shouldReturnParserQuery14(String query) {
        var captor = ArgumentCaptor.forClass(UpdateQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).update(captor.capture());
        UpdateQuery updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(condition.condition()).isEqualTo(Condition.LESSER_THAN);
            soft.assertThat(condition.element()).isEqualTo(Element.of("stamina", -10.23));
            soft.assertThat(updateQuery.set()).isNotNull().hasSize(1)
                    .contains(Element.of("age", 10));
        });

    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = 10 WHERE age BETWEEN 10 AND 30"})
    void shouldReturnParserQuery15(String query) {
        var captor = ArgumentCaptor.forClass(UpdateQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).update(captor.capture());
        var updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(condition.condition()).isEqualTo(Condition.BETWEEN);
            soft.assertThat(condition.element()).isEqualTo(Element.of("age", Arrays.asList(10, 30)));
            soft.assertThat(updateQuery.set()).isNotNull().hasSize(1)
                    .contains(Element.of("age", 10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = 10 WHERE name = \"diana\""})
    void shouldReturnParserQuery16(String query) {
        var captor = ArgumentCaptor.forClass(UpdateQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).update(captor.capture());
        UpdateQuery updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.element()).isEqualTo(Element.of("name", "diana"));
            soft.assertThat(updateQuery.set()).isNotNull().hasSize(1)
                    .contains(Element.of("age", 10));
        });

    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = 10 WHERE name IN (\"Ada\", \"Apollo\")"})
    void shouldReturnParserQuery20(String query) {
        var captor = ArgumentCaptor.forClass(UpdateQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).update(captor.capture());
        var updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();


        SoftAssertions.assertSoftly(soft -> {
            Element element = condition.element();
            List<String> values = element.get(new TypeReference<>() {
            });
            soft.assertThat(condition.condition()).isEqualTo(Condition.IN);
            soft.assertThat(condition.element().name()).isEqualTo("name");
            soft.assertThat(values).contains("Ada", "Apollo");
            soft.assertThat(updateQuery.set()).isNotNull().hasSize(1)
                    .contains(Element.of("age", 10));
        });

    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = 10 WHERE name LIKE \"Ada\""})
    void shouldReturnParserQuery21(String query) {
        var captor = ArgumentCaptor.forClass(UpdateQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).update(captor.capture());
        var updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();

        SoftAssertions.assertSoftly(soft -> {
            Element element = condition.element();
            soft.assertThat(condition.condition()).isEqualTo(Condition.LIKE);
            soft.assertThat(element.name()).isEqualTo("name");
            soft.assertThat(element.get()).isEqualTo("Ada");
            soft.assertThat(updateQuery.set()).isNotNull().hasSize(1)
                    .contains(Element.of("age", 10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = 10 WHERE name NOT LIKE \"Ada\""})
    void shouldReturnParserQuery22(String query) {
        var captor = ArgumentCaptor.forClass(UpdateQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).update(captor.capture());
        UpdateQuery updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();

        SoftAssertions.assertSoftly(soft -> {
            var element = condition.element();
            List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
            });
            var criteriaCondition = conditions.getFirst();

            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.LIKE);
            soft.assertThat(conditions.getFirst().element().name()).isEqualTo("name");
            soft.assertThat(conditions.getFirst().element().get()).isEqualTo("Ada");
            soft.assertThat(updateQuery.set()).isNotNull().hasSize(1)
                    .contains(Element.of("age", 10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = 10 WHERE name = \"Ada\" AND age = 20"})
    void shouldReturnParserQuery23(String query) {
        var captor = ArgumentCaptor.forClass(UpdateQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).update(captor.capture());
        var updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();

        SoftAssertions.assertSoftly(soft ->{
            Element element = condition.element();
            soft.assertThat(condition.condition()).isEqualTo(Condition.AND);
            List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
            });
            soft.assertThat(conditions).contains(eq(Element.of("name", "Ada")),
                    eq(Element.of("age", 20)));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = 10 WHERE name = \"Ada\" OR age = 20"})
    void shouldReturnParserQuery24(String query) {
        var captor = ArgumentCaptor.forClass(UpdateQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).update(captor.capture());
        var updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();

        SoftAssertions.assertSoftly(soft -> {
            Element element = condition.element();
            soft.assertThat(condition.condition()).isEqualTo(Condition.OR);
            List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
            });
            soft.assertThat(conditions).contains(eq(Element.of("name", "Ada")),
                    eq(Element.of("age", 20)));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = 10 WHERE age = :age"})
    void shouldReturnErrorWhenNeedPrepareStatement(String query) {
        assertThrows(QueryException.class, () -> parser.query(query, manager, observer));
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = 10 WHERE age = :age"})
    void shouldReturnErrorWhenIsQueryWithParam(String query) {
        assertThrows(QueryException.class, () -> parser.query(query, manager, observer));
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = 10 WHERE age = ?1"})
    void shouldReturnErrorWhenIsQueryWithParamPosition(String query) {
        assertThrows(QueryException.class, () -> parser.query(query, manager, observer));
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = ?1"})
    void shouldReturnErrorWhenIsQueryWithParamSetPosition(String query) {
        assertThrows(QueryException.class, () -> parser.query(query, manager, observer));
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = :age"})
    void shouldReturnErrorWhenIsQueryWithParamSet(String query) {
        assertThrows(QueryException.class, () -> parser.query(query, manager, observer));
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = 10 WHERE age = :age"})
    void shouldReturnErrorWhenDontBindParameters(String query) {

        CommunicationPreparedStatement prepare = parser.prepare(query, manager, observer);
        assertThrows(QueryException.class, prepare::result);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = 10 WHERE age = :age"})
    void shouldExecutePrepareStatement(String query) {
        var captor = ArgumentCaptor.forClass(UpdateQuery.class);

        CommunicationPreparedStatement prepare = parser.prepare(query, manager, observer);
        prepare.bind("age", 12);
        prepare.result();
        Mockito.verify(manager).update(captor.capture());
        var updateQuery = captor.getValue();
        CriteriaCondition criteriaCondition = updateQuery.condition().get();
        SoftAssertions.assertSoftly(soft -> {
            Element element = criteriaCondition.element();
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(element.name()).isEqualTo("age");
            soft.assertThat(element.get()).isEqualTo(12);
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET age = 10 WHERE age = ?1"})
    void shouldExecutePrepareStatementPosition(String query) {
        var captor = ArgumentCaptor.forClass(UpdateQuery.class);

        CommunicationPreparedStatement prepare = parser.prepare(query, manager, observer);
        prepare.bind(1, 12);
        prepare.result();
        Mockito.verify(manager).update(captor.capture());
        var updateQuery = captor.getValue();
        CriteriaCondition criteriaCondition = updateQuery.condition().get();
        SoftAssertions.assertSoftly(soft -> {
            Element element = criteriaCondition.element();
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(element.name()).isEqualTo("age");
            soft.assertThat(element.get()).isEqualTo(12);
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET name = :name WHERE age = :age"})
    void shouldExecutePrepareStatementSet(String query) {
        var captor = ArgumentCaptor.forClass(UpdateQuery.class);

        CommunicationPreparedStatement prepare = parser.prepare(query, manager, observer);
        prepare.bind("age", 12);
        prepare.bind("name", "Ada");
        prepare.result();
        Mockito.verify(manager).update(captor.capture());
        var updateQuery = captor.getValue();
        CriteriaCondition criteriaCondition = updateQuery.condition().get();
        SoftAssertions.assertSoftly(soft -> {
            Element element = criteriaCondition.element();
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(element.name()).isEqualTo("age");
            soft.assertThat(element.get()).isEqualTo(12);
            soft.assertThat(updateQuery.set()).hasSize(1);
            var setItem = updateQuery.set().getFirst();
            soft.assertThat(setItem.name()).isEqualTo("name");
            soft.assertThat(setItem.value().get()).isEqualTo("Ada");
        });
    }

    private void checkBaseQuery(UpdateQuery updateQuery) {
        assertEquals("entity", updateQuery.name());
    }
}
