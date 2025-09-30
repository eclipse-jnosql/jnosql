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

import jakarta.data.Direction;
import jakarta.data.Sort;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectQueryParserTest {

    private final SelectQueryParser parser = new SelectQueryParser();

    private final DatabaseManager manager = Mockito.mock(DatabaseManager.class);

    private final CommunicationObserverParser observer = new CommunicationObserverParser() {
    };


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity ORDER BY name ASC"})
    void shouldReturnParserQuery3(String query) {
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        var selectQuery = captor.getValue();

        assertTrue(selectQuery.columns().isEmpty());
        assertThat(selectQuery.sorts()).contains(Sort.of("name", Direction.ASC, false));
        assertEquals(0L, selectQuery.limit());
        assertEquals(0L, selectQuery.skip());
        assertEquals("entity", selectQuery.name());
        assertFalse(selectQuery.condition().isPresent());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity ORDER BY name ASC"})
    void shouldReturnParserQuery4(String query) {
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        var selectQuery = captor.getValue();

        assertTrue(selectQuery.columns().isEmpty());
        assertThat(selectQuery.sorts()).contains(Sort.of("name", Direction.ASC, false));
        assertEquals(0L, selectQuery.limit());
        assertEquals(0L, selectQuery.skip());
        assertEquals("entity", selectQuery.name());
        assertFalse(selectQuery.condition().isPresent());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity ORDER BY name DESC"})
    void shouldReturnParserQuery5(String query) {
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        var selectQuery = captor.getValue();

        assertTrue(selectQuery.columns().isEmpty());
        assertThat(selectQuery.sorts()).contains(Sort.of("name", Direction.DESC, false));
        assertEquals(0L, selectQuery.limit());
        assertEquals(0L, selectQuery.skip());
        assertEquals("entity", selectQuery.name());
        assertFalse(selectQuery.condition().isPresent());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity ORDER BY name DESC, age ASC"})
    void shouldReturnParserQuery6(String query) {

        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        var selectQuery = captor.getValue();

        assertTrue(selectQuery.columns().isEmpty());
        assertThat(selectQuery.sorts()).contains(Sort.desc("name"),
                Sort.asc("age"));
        assertEquals(0L, selectQuery.limit());
        assertEquals(0L, selectQuery.skip());
        assertEquals("entity", selectQuery.name());
        assertFalse(selectQuery.condition().isPresent());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE age = 10"})
    void shouldReturnParserQuery10(String query) {
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        var selectQuery = captor.getValue();

        checkBaseQuery(selectQuery);
        assertTrue(selectQuery.condition().isPresent());
        CriteriaCondition condition = selectQuery.condition().get();

        assertEquals(Condition.EQUALS, condition.condition());
        assertEquals(Element.of("age", 10), condition.element());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE stamina > 10.23"})
    void shouldReturnParserQuery11(String query) {
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        var selectQuery = captor.getValue();

        checkBaseQuery(selectQuery);
        assertTrue(selectQuery.condition().isPresent());
        CriteriaCondition condition = selectQuery.condition().get();

        assertEquals(Condition.GREATER_THAN, condition.condition());
        assertEquals(Element.of("stamina", 10.23), condition.element());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE stamina >= -10.23"})
    void shouldReturnParserQuery12(String query) {
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();

        checkBaseQuery(selectQuery);
        assertTrue(selectQuery.condition().isPresent());
        CriteriaCondition condition = selectQuery.condition().get();

        assertEquals(Condition.GREATER_EQUALS_THAN, condition.condition());
        assertEquals(Element.of("stamina", -10.23), condition.element());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE stamina <= -10.23"})
    void shouldReturnParserQuery13(String query) {
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        var selectQuery = captor.getValue();

        checkBaseQuery(selectQuery);
        assertTrue(selectQuery.condition().isPresent());
        CriteriaCondition condition = selectQuery.condition().get();

        assertEquals(Condition.LESSER_EQUALS_THAN, condition.condition());
        assertEquals(Element.of("stamina", -10.23), condition.element());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE stamina < -10.23"})
    void shouldReturnParserQuery14(String query) {
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        var selectQuery = captor.getValue();

        checkBaseQuery(selectQuery);
        assertTrue(selectQuery.condition().isPresent());
        CriteriaCondition condition = selectQuery.condition().get();

        assertEquals(Condition.LESSER_THAN, condition.condition());
        assertEquals(Element.of("stamina", -10.23), condition.element());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE age BETWEEN 10 AND 30"})
    void shouldReturnParserQuery15(String query) {
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        var selectQuery = captor.getValue();

        checkBaseQuery(selectQuery);
        assertTrue(selectQuery.condition().isPresent());
        CriteriaCondition condition = selectQuery.condition().get();

        assertEquals(Condition.BETWEEN, condition.condition());
        assertEquals(Element.of("age", Arrays.asList(10, 30)), condition.element());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE name = \"diana\""})
    void shouldReturnParserQuery16(String query) {
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();

        checkBaseQuery(selectQuery);
        assertTrue(selectQuery.condition().isPresent());
        var condition = selectQuery.condition().get();

        assertEquals(Condition.EQUALS, condition.condition());
        assertEquals(Element.of("name", "diana"), condition.element());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE name IN (\"Ada\", \"Apollo\")"})
    void shouldReturnParserQuery20(String query) {
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();

        checkBaseQuery(selectQuery);
        assertTrue(selectQuery.condition().isPresent());
        var condition = selectQuery.condition().get();
        Element element = condition.element();
        assertEquals(Condition.IN, condition.condition());
        assertEquals("name", element.name());
        List<String> values = element.get(new TypeReference<>() {
        });
        assertThat(values).contains("Ada", "Apollo");
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE name LIKE \"Ada\""})
    void shouldReturnParserQuery21(String query) {
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();

        checkBaseQuery(selectQuery);
        assertTrue(selectQuery.condition().isPresent());
        CriteriaCondition condition = selectQuery.condition().get();
        Element element = condition.element();
        assertEquals(Condition.LIKE, condition.condition());
        assertEquals("name", element.name());
        assertEquals("Ada", element.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE name NOT LIKE \"Ada\""})
    void shouldReturnParserQuery22(String query) {
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();

        checkBaseQuery(selectQuery);
        assertTrue(selectQuery.condition().isPresent());
        CriteriaCondition condition = selectQuery.condition().get();
        Element element = condition.element();
        assertEquals(Condition.NOT, condition.condition());
        List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
        });
        CriteriaCondition criteriaCondition = conditions.getFirst();
        assertEquals(Condition.LIKE, criteriaCondition.condition());
        assertEquals(Element.of("name", "Ada"), criteriaCondition.element());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE name = \"Ada\" AND age = 20"})
    void shouldReturnParserQuery23(String query) {
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();

        checkBaseQuery(selectQuery);
        assertTrue(selectQuery.condition().isPresent());
        CriteriaCondition condition = selectQuery.condition().get();
        Element element = condition.element();
        assertEquals(Condition.AND, condition.condition());
        List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
        });
        Assertions.assertThat(conditions).contains(eq(Element.of("name", "Ada")),
                eq(Element.of("age", 20)));
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE name = \"Ada\" OR age = 20"})
    void shouldReturnParserQuery24(String query) {
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();

        checkBaseQuery(selectQuery);
        assertTrue(selectQuery.condition().isPresent());
        CriteriaCondition condition = selectQuery.condition().get();
        Element element = condition.element();
        assertEquals(Condition.OR, condition.condition());
        List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
        });
        Assertions.assertThat(conditions).contains(eq(Element.of("name", "Ada")),
                eq(Element.of("age", 20)));
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE age = :age"})
    void shouldReturnErrorWhenIsQueryWithParam(String query) {

        assertThrows(QueryException.class, () -> parser.query(query, null, manager, observer));
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE age = :age"})
    void shouldReturnErrorWhenDontBindParameters(String query) {

        CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, observer);
        assertThrows(QueryException.class, prepare::result);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE age = :age"})
    void shouldExecutePrepareStatement(String query) {
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);

        CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, observer);
        prepare.bind("age", 12);
        prepare.result();
        Mockito.verify(manager).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();
        CriteriaCondition criteriaCondition = selectQuery.condition().get();
        Element element = criteriaCondition.element();
        assertEquals(Condition.EQUALS, criteriaCondition.condition());
        assertEquals("age", element.name());
        assertEquals(12, element.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE age = ?1"})
    void shouldExecutePrepareStatementIndex(String query) {
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);

        CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, observer);
        prepare.bind(1, 12);
        prepare.result();
        Mockito.verify(manager).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();
        CriteriaCondition criteriaCondition = selectQuery.condition().get();
        Element element = criteriaCondition.element();
        assertEquals(Condition.EQUALS, criteriaCondition.condition());
        assertEquals("age", element.name());
        assertEquals(12, element.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE age = ?1 AND name = ?2"})
    void shouldExecutePrepareStatementIndex2(String query) {
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);

        CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, observer);
        prepare.bind(1, 12);
        prepare.bind(2, "Otavio");
        prepare.result();
        Mockito.verify(manager).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();
        CriteriaCondition criteriaCondition = selectQuery.condition().get();
        Element element = criteriaCondition.element();
        assertEquals(Condition.AND, criteriaCondition.condition());
        List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
        });
        var age = conditions.get(0).element();
        var name = conditions.get(1).element();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(conditions).hasSize(2);

            soft.assertThat(age.name()).isEqualTo("age");
            soft.assertThat(age.get()).isEqualTo(12);
            soft.assertThat(conditions.getFirst().condition()).isEqualTo(Condition.EQUALS);

            soft.assertThat(name.name()).isEqualTo("name");
            soft.assertThat(name.get()).isEqualTo("Otavio");
            soft.assertThat(conditions.get(1).condition()).isEqualTo(Condition.EQUALS);
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"select count(this) from entity"})
    void shouldCount(String query) {
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        var selectQuery = captor.getValue();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(selectQuery.columns()).isEmpty();
            softly.assertThat(selectQuery.isCount()).isTrue();
            softly.assertThat(selectQuery.name()).isEqualTo("entity");
            softly.assertThat(selectQuery.condition()).isEmpty();
            softly.assertThat(selectQuery.limit()).isZero();
            softly.assertThat(selectQuery.skip()).isZero();
            softly.assertThat(selectQuery.sorts()).isEmpty();
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"select count(this) FROM entity WHERE age = ?1 AND name = ?2"})
    void shouldCountExecutePrepareStatementIndex2(String query) {
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);

        CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, observer);
        prepare.bind(1, 12);
        prepare.bind(2, "Otavio");
        prepare.count();
        Mockito.verify(manager).count(captor.capture());
        SelectQuery selectQuery = captor.getValue();
        CriteriaCondition criteriaCondition = selectQuery.condition().get();
        Element element = criteriaCondition.element();
        assertEquals(Condition.AND, criteriaCondition.condition());
        List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
        });
        var age = conditions.get(0).element();
        var name = conditions.get(1).element();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.isCount()).isTrue();
            soft.assertThat(conditions).hasSize(2);

            soft.assertThat(age.name()).isEqualTo("age");
            soft.assertThat(age.get()).isEqualTo(12);
            soft.assertThat(conditions.getFirst().condition()).isEqualTo(Condition.EQUALS);

            soft.assertThat(name.name()).isEqualTo("name");
            soft.assertThat(name.get()).isEqualTo("Otavio");
            soft.assertThat(conditions.get(1).condition()).isEqualTo(Condition.EQUALS);
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"select count(this) FROM entity WHERE age = ?1 AND name = ?2"})
    void shouldGetIssueWhenCount(String query) {
        CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, observer);
        Assertions.assertThatThrownBy(() -> prepare.count());

    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"select count(this) FROM entity WHERE age = ?1 AND name = ?2"})
    void shouldReturnErrorWhenResultInsteadOfCount(String query) {

        CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, observer);
        prepare.bind(1, 12);
        prepare.bind(2, "Otavio");

        assertThrows(UnsupportedOperationException.class, prepare::result);
        assertThrows(UnsupportedOperationException.class, prepare::singleResult);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE name is null"})
    void shouldRunIsNull(String query) {
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        var selectQuery = captor.getValue();

        checkBaseQuery(selectQuery);
        assertTrue(selectQuery.condition().isPresent());
        var condition = selectQuery.condition().get();

        assertEquals(Condition.EQUALS, condition.condition());
        assertEquals(Element.of("name", null), condition.element());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE name is not null"})
    void shouldRunIsNotNull(String query) {
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        var selectQuery = captor.getValue();

        checkBaseQuery(selectQuery);
        assertTrue(selectQuery.condition().isPresent());
        var condition = selectQuery.condition().get();
        var criteriaCondition = condition.element().get(CriteriaCondition.class);
        assertEquals(Condition.NOT, condition.condition());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(criteriaCondition.element()).isEqualTo(Element.of("name", null));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE stamina >= -10.23"})
    void shouldReplaceQuery(String query) {
        CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, observer);
        prepare.setSelectMapper(q -> SelectQuery.select().from("entity").build());
        prepare.result();
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(manager).select(captor.capture());
        var selectQuery = captor.getValue();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(selectQuery.columns()).isEmpty();
            softly.assertThat(selectQuery.sorts()).isEmpty();
            softly.assertThat(selectQuery.limit()).isZero();
            softly.assertThat(selectQuery.skip()).isZero();
            softly.assertThat(selectQuery.name()).isEqualTo("entity");
            softly.assertThat(selectQuery.condition()).isEmpty();
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE stamina >= -10.23"})
    void shouldGetQuery(String query) {
        CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, observer);
        prepare.setSelectMapper(q -> SelectQuery.select().from("entity").build());
        prepare.result();
        Optional<SelectQuery> select = prepare.select();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(select).isPresent();
            softly.assertThat(select.orElseThrow().columns()).isEmpty();
            softly.assertThat(select.orElseThrow().sorts()).isEmpty();
            softly.assertThat(select.orElseThrow().limit()).isZero();
            softly.assertThat(select.orElseThrow().skip()).isZero();
            softly.assertThat(select.orElseThrow().name()).isEqualTo("entity");
            softly.assertThat(select.orElseThrow().condition()).isNotEmpty();
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE active = true"})
    void shouldReturnQuerySpecialTrue(String query) {
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        var selectQuery = captor.getValue();

        checkBaseQuery(selectQuery);
        assertTrue(selectQuery.condition().isPresent());
        CriteriaCondition condition = selectQuery.condition().get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(condition.element()).isEqualTo(Element.of("active", true));
        });
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE active = false"})
    void shouldReturnQuerySpecialFalse(String query) {
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        var selectQuery = captor.getValue();

        checkBaseQuery(selectQuery);
        assertTrue(selectQuery.condition().isPresent());
        CriteriaCondition condition = selectQuery.condition().get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(condition.element()).isEqualTo(Element.of("active", false));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE active IS NULL"})
    void shouldReturnQuerySpecialNull(String query) {
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        var selectQuery = captor.getValue();

        checkBaseQuery(selectQuery);
        assertTrue(selectQuery.condition().isPresent());
        CriteriaCondition condition = selectQuery.condition().get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(condition.element()).isEqualTo(Element.of("active", Value.ofNull()));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity WHERE active IS NOT NULL"})
    void shouldReturnQuerySpecialNotNull(String query) {
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, observer);
        Mockito.verify(manager).select(captor.capture());
        var selectQuery = captor.getValue();

        checkBaseQuery(selectQuery);
        assertTrue(selectQuery.condition().isPresent());
        CriteriaCondition condition = selectQuery.condition().get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            CriteriaCondition subCondition = condition.element().get(CriteriaCondition.class);
            softly.assertThat(subCondition.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(subCondition.element()).isEqualTo(Element.of("active", Value.ofNull()));
        });
    }

    @Test
    void shouldApply() {
        SelectQueryParser queryParser = new SelectQueryParser();
        org.eclipse.jnosql.communication.query.SelectQuery query = Mockito.mock(org.eclipse.jnosql.communication.query.SelectQuery.class);
        CommunicationObserverParser observer = Mockito.mock(CommunicationObserverParser.class);
        queryParser.apply(query, observer);
    }

    private void checkBaseQuery(SelectQuery selectQuery) {
        assertTrue(selectQuery.columns().isEmpty());
        assertTrue(selectQuery.sorts().isEmpty());
        assertEquals(0L, selectQuery.limit());
        assertEquals(0L, selectQuery.skip());
        assertEquals("entity", selectQuery.name());
    }
}
