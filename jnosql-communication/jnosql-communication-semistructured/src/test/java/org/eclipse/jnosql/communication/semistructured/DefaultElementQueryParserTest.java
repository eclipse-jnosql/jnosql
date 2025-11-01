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

import jakarta.data.exceptions.NonUniqueResultException;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultElementQueryParserTest {
    
    private final QueryParser parser = new QueryParser();


    private final DatabaseManager manager = Mockito.mock(DatabaseManager.class);

    @Test
    void shouldReturnNPEWhenThereIsNullParameter() {
        assertThrows(NullPointerException.class, () -> parser.query(null, null, manager, CommunicationObserverParser.EMPTY));
        assertThrows(NullPointerException.class, () -> parser.query("select * from God", null, null, CommunicationObserverParser.EMPTY));
    }

    @Test
    void shouldReturnErrorWhenHasInvalidQuery() {
        assertThrows(Exception.class, () -> parser.query("inva", null,  manager, CommunicationObserverParser.EMPTY));
        assertThrows(Exception.class, () -> parser.query("invalid", null, manager, CommunicationObserverParser.EMPTY));
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM God"})
    void shouldReturnParsedSelectQuery(String query) {
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        parser.query(query, null, manager, CommunicationObserverParser.EMPTY);
        Mockito.verify(manager).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();

        assertTrue(selectQuery.columns().isEmpty());
        assertTrue(selectQuery.sorts().isEmpty());
        assertEquals(0L, selectQuery.limit());
        assertEquals(0L, selectQuery.skip());
        assertEquals("God", selectQuery.name());
        assertFalse(selectQuery.condition().isPresent());

    }

    @ParameterizedTest(name = "Should parser the query {0} FROM God")
    @ValueSource(strings = {"DELETE", "delete", "DeLeTe", "dElEtE", "DElete", "deLETE", "DeleTE", "DELete"})
    void shouldReturnParsedDeleteQuery(String queryCommand) {
        var query = queryCommand + " FROM God";
        ArgumentCaptor<DeleteQuery> captor = ArgumentCaptor.forClass(DeleteQuery.class);
        parser.query(query, null, manager, CommunicationObserverParser.EMPTY);
        Mockito.verify(manager).delete(captor.capture());
        DeleteQuery deleteQuery = captor.getValue();

        assertTrue(deleteQuery.columns().isEmpty());
        assertEquals("God", deleteQuery.name());
        assertFalse(deleteQuery.condition().isPresent());
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"update God (name = \"Diana\")"})
    @Disabled
    void shouldReturnParserQuery3(String query) {
        ArgumentCaptor<CommunicationEntity> captor = ArgumentCaptor.forClass(CommunicationEntity.class);
        parser.query(query, null, manager, CommunicationObserverParser.EMPTY);
        Mockito.verify(manager).update(captor.capture());
        CommunicationEntity entity = captor.getValue();


        assertEquals("God", entity.name());
        assertEquals(Element.of("name", "Diana"), entity.find("name").get());
    }

    @ParameterizedTest(name = "Should parser the query {0} FROM God WHERE age = :age")
    @ValueSource(strings = {"DELETE", "delete", "DeLeTe", "dElEtE", "DElete", "deLETE", "DeleTE", "DELete"})
    void shouldExecutePrepareStatement(String queryCommand) {
        var query = queryCommand + " FROM God WHERE age = :age";
        ArgumentCaptor<DeleteQuery> captor = ArgumentCaptor.forClass(DeleteQuery.class);

        CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, CommunicationObserverParser.EMPTY);
        prepare.bind("age", 12);
        prepare.result();
        Mockito.verify(manager).delete(captor.capture());
        DeleteQuery deleteQuery = captor.getValue();
        CriteriaCondition criteriaCondition = deleteQuery.condition().get();
        Element element = criteriaCondition.element();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(element.name()).isEqualTo("age");
            softly.assertThat(element.get()).isEqualTo(12);
            softly.assertThat(prepare.getType()).isNotNull();
        });

    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM God WHERE age = :age"})
    void shouldExecutePrepareStatement2(String query) {
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);

        CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, CommunicationObserverParser.EMPTY);
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
    @ValueSource(strings = {"update God (name = @name)"})
    @Disabled
    void shouldExecutePrepareStatement3(String query) {
        ArgumentCaptor<CommunicationEntity> captor = ArgumentCaptor.forClass(CommunicationEntity.class);
        CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, CommunicationObserverParser.EMPTY);
        prepare.bind("name", "Diana");
        prepare.result();
        Mockito.verify(manager).update(captor.capture());
        CommunicationEntity entity = captor.getValue();
        assertEquals("God", entity.name());
        assertEquals(Element.of("name", "Diana"), entity.find("name").get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM God WHERE age = :age"})
    void shouldSingleResult(String query) {
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);

        Mockito.when(manager.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.of(Mockito.mock(CommunicationEntity.class)));

        CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, CommunicationObserverParser.EMPTY);
        prepare.bind("age", 12);
        final Optional<CommunicationEntity> result = prepare.singleResult();
        Mockito.verify(manager).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();
        CriteriaCondition criteriaCondition = selectQuery.condition().get();
        Element element = criteriaCondition.element();
        assertEquals(Condition.EQUALS, criteriaCondition.condition());
        assertEquals("age", element.name());
        assertEquals(12, element.get());
        assertTrue(result.isPresent());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM God WHERE age = :age"})
    void shouldReturnEmptySingleResult(String query) {
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);

        Mockito.when(manager.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.empty());

        CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, CommunicationObserverParser.EMPTY);
        prepare.bind("age", 12);
        final Optional<CommunicationEntity> result = prepare.singleResult();
        Mockito.verify(manager).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();
        CriteriaCondition criteriaCondition = selectQuery.condition().get();
        Element element = criteriaCondition.element();
        assertEquals(Condition.EQUALS, criteriaCondition.condition());
        assertEquals("age", element.name());
        assertEquals(12, element.get());
        assertFalse(result.isPresent());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM God WHERE age = :age"})
    void shouldReturnErrorSingleResult(String query) {
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);

        Mockito.when(manager.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.of(Mockito.mock(CommunicationEntity.class), Mockito.mock(CommunicationEntity.class)));

        CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, CommunicationObserverParser.EMPTY);
        prepare.bind("age", 12);
       assertThrows(NonUniqueResultException.class, prepare::singleResult);
    }

}
