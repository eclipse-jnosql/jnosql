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

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeleteQueryParserTest {

    private final DeleteQueryParser parser = new DeleteQueryParser();

    private final DatabaseManager manager = Mockito.mock(DatabaseManager.class);

    private final CommunicationObserverParser observer = new CommunicationObserverParser() {
    };


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity"})
    void shouldReturnParserQuery(String query) {
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).delete(captor.capture());
        var deleteQuery = captor.getValue();

        assertTrue(deleteQuery.columns().isEmpty());
        assertEquals("entity", deleteQuery.name());
        assertFalse(deleteQuery.condition().isPresent());
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE stamina > 10.23"})
    void shouldReturnParserQuery11(String query) {
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).delete(captor.capture());
        var deleteQuery = captor.getValue();

        checkBaseQuery(deleteQuery);
        assertTrue(deleteQuery.condition().isPresent());
        CriteriaCondition condition = deleteQuery.condition().get();

        assertEquals(Condition.GREATER_THAN, condition.condition());
        assertEquals(Element.of("stamina", 10.23), condition.element());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE stamina >= -10.23"})
    void shouldReturnParserQuery12(String query) {
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).delete(captor.capture());
        var deleteQuery = captor.getValue();

        checkBaseQuery(deleteQuery);
        assertTrue(deleteQuery.condition().isPresent());
        CriteriaCondition condition = deleteQuery.condition().get();

        assertEquals(Condition.GREATER_EQUALS_THAN, condition.condition());
        assertEquals(Element.of("stamina", -10.23), condition.element());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE stamina <= -10.23"})
    void shouldReturnParserQuery13(String query) {
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).delete(captor.capture());
        var updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();

        assertEquals(Condition.LESSER_EQUALS_THAN, condition.condition());
        assertEquals(Element.of("stamina", -10.23), condition.element());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE stamina < -10.23"})
    void shouldReturnParserQuery14(String query) {
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).delete(captor.capture());
        var updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();

        assertEquals(Condition.LESSER_THAN, condition.condition());
        assertEquals(Element.of("stamina", -10.23), condition.element());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE age BETWEEN 10 AND 30"})
    void shouldReturnParserQuery15(String query) {
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).delete(captor.capture());
        var updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();

        assertEquals(Condition.BETWEEN, condition.condition());
        assertEquals(Element.of("age", Arrays.asList(10, 30)), condition.element());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE name = \"diana\""})
    void shouldReturnParserQuery16(String query) {
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).delete(captor.capture());
        var updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();

        assertEquals(Condition.EQUALS, condition.condition());
        assertEquals(Element.of("name", "diana"), condition.element());
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE name IN (\"Ada\", \"Apollo\")"})
    void shouldReturnParserQuery20(String query) {
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).delete(captor.capture());
        var updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();
        Element element = condition.element();
        assertEquals(Condition.IN, condition.condition());
        assertEquals("name", element.name());
        List<String> values = element.get(new TypeReference<>() {
        });
        assertThat(values).contains("Ada", "Apollo");
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE name LIKE \"Ada\""})
    void shouldReturnParserQuery21(String query) {
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).delete(captor.capture());
        var updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();
        Element element = condition.element();
        assertEquals(Condition.LIKE, condition.condition());
        assertEquals("name", element.name());
        assertEquals("Ada", element.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE name NOT LIKE \"Ada\""})
    void shouldReturnParserQuery22(String query) {
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).delete(captor.capture());
        var updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();
        Element element = condition.element();
        assertEquals(Condition.NOT, condition.condition());
        List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
        });
        CriteriaCondition criteriaCondition = conditions.get(0);
        assertEquals(Condition.LIKE, criteriaCondition.condition());
        assertEquals(Element.of("name", "Ada"), criteriaCondition.element());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE name = \"Ada\" AND age = 20"})
    void shouldReturnParserQuery23(String query) {
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).delete(captor.capture());
        var updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();
        Element element = condition.element();
        assertEquals(Condition.AND, condition.condition());
        List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
        });
        Assertions.assertThat(conditions).contains(eq(Element.of("name", "Ada")),
                eq(Element.of("age", 20)));
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE name = \"Ada\" OR age = 20"})
    void shouldReturnParserQuery24(String query) {
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).delete(captor.capture());
        var updateQuery = captor.getValue();

        checkBaseQuery(updateQuery);
        assertTrue(updateQuery.condition().isPresent());
        CriteriaCondition condition = updateQuery.condition().get();
        Element element = condition.element();
        assertEquals(Condition.OR, condition.condition());
        List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
        });
        Assertions.assertThat(conditions).contains(eq(Element.of("name", "Ada")),
                eq(Element.of("age", 20)));
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE age = :age"})
    void shouldReturnErrorWhenNeedPrepareStatement(String query) {
        assertThrows(QueryException.class, () -> parser.query(query, manager, observer));
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE age = :age"})
    void shouldReturnErrorWhenIsQueryWithParam(String query) {

        assertThrows(QueryException.class, () -> parser.query(query, manager, observer));

    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE age = :age"})
    void shouldReturnErrorWhenDontBindParameters(String query) {

        var prepare = parser.prepare(query, manager, observer);
        assertThrows(QueryException.class, prepare::result);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE age = ?1"})
    void shouldReturnErrorWhenDontBindParametersPosition(String query) {

        var prepare = parser.prepare(query, manager, observer);
        assertThrows(QueryException.class, prepare::result);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE age = :age"})
    void shouldExecutePrepareStatement(String query) {
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);

        var prepare = parser.prepare(query, manager, observer);
        prepare.bind("age", 12);
        prepare.result();
        Mockito.verify(manager).delete(captor.capture());
        var updateQuery = captor.getValue();
        var criteriaCondition = updateQuery.condition().get();
        Element element = criteriaCondition.element();
        assertEquals(Condition.EQUALS, criteriaCondition.condition());
        assertEquals("age", element.name());
        assertEquals(12, element.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE age = ?1"})
    void shouldExecutePrepareStatementPosition(String query) {
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);

        var prepare = parser.prepare(query, manager, observer);
        prepare.bind(1, 12);
        prepare.result();
        Mockito.verify(manager).delete(captor.capture());
        var updateQuery = captor.getValue();
        var criteriaCondition = updateQuery.condition().get();
        Element element = criteriaCondition.element();
        assertEquals(Condition.EQUALS, criteriaCondition.condition());
        assertEquals("age", element.name());
        assertEquals(12, element.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE active = true"})
    void shouldReturnQuerySpecialTrue(String query) {
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).delete(captor.capture());
        var deleteQuery = captor.getValue();

        checkBaseQuery(deleteQuery);
        assertTrue(deleteQuery.condition().isPresent());
        CriteriaCondition condition = deleteQuery.condition().get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(condition.element()).isEqualTo(Element.of("active", true));
        });
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE active = false"})
    void shouldReturnQuerySpecialFalse(String query) {
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).delete(captor.capture());
        var deleteQuery = captor.getValue();

        checkBaseQuery(deleteQuery);
        assertTrue(deleteQuery.condition().isPresent());
        CriteriaCondition condition = deleteQuery.condition().get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(condition.element()).isEqualTo(Element.of("active", false));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE active IS NULL"})
    void shouldReturnQuerySpecialNull(String query) {
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).delete(captor.capture());
        var deleteQuery = captor.getValue();

        checkBaseQuery(deleteQuery);
        assertTrue(deleteQuery.condition().isPresent());
        CriteriaCondition condition = deleteQuery.condition().get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(condition.element()).isEqualTo(Element.of("active", Value.ofNull()));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE active IS NOT NULL"})
    void shouldReturnQuerySpecialNotNull(String query) {
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        parser.query(query, manager, observer);
        Mockito.verify(manager).delete(captor.capture());
        var deleteQuery = captor.getValue();

        checkBaseQuery(deleteQuery);
        assertTrue(deleteQuery.condition().isPresent());
        CriteriaCondition condition = deleteQuery.condition().get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            CriteriaCondition subCondition = condition.element().get(CriteriaCondition.class);
            softly.assertThat(subCondition.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(subCondition.element()).isEqualTo(Element.of("active", Value.ofNull()));
        });
    }


    private void checkBaseQuery(DeleteQuery columnQuery) {
        assertTrue(columnQuery.columns().isEmpty());
        assertEquals("entity", columnQuery.name());
    }
}