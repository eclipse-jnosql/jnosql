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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class DefaultElementQueryParserTest {
    

    @Nested
    @DisplayName("When the default element query parser is used")
    class WhenTheDefaultElementQueryParserIsUsed {

        private final QueryParser parser = new QueryParser();


        private final DatabaseManager manager = Mockito.mock(DatabaseManager.class);

        @DisplayName("Should Return NPEWhen There Is Null Parameter")
        @Test
        void shouldReturnNPEWhenThereIsNullParameter() {
            assertThatThrownBy(() -> parser.query(null, null, manager, CommunicationObserverParser.EMPTY)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> parser.query("select * from God", null, null, CommunicationObserverParser.EMPTY)).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Return Error When Has Invalid Query")
        @Test
        void shouldReturnErrorWhenHasInvalidQuery() {
            assertThatThrownBy(() -> parser.query("inva", null,  manager, CommunicationObserverParser.EMPTY)).isInstanceOf(Exception.class);
            assertThatThrownBy(() -> parser.query("invalid", null, manager, CommunicationObserverParser.EMPTY)).isInstanceOf(Exception.class);
        }

        @DisplayName("Should Return Parsed Select Query")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM God"})
        void shouldReturnParsedSelectQuery(String query) {
            ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, CommunicationObserverParser.EMPTY);
            Mockito.verify(manager).select(captor.capture());
            SelectQuery selectQuery = captor.getValue();

            assertThat(selectQuery.columns().isEmpty()).isTrue();
            assertThat(selectQuery.sorts().isEmpty()).isTrue();
            assertThat(selectQuery.limit()).isEqualTo(0L);
            assertThat(selectQuery.skip()).isEqualTo(0L);
            assertThat(selectQuery.name()).isEqualTo("God");
            assertThat(selectQuery.condition().isPresent()).isFalse();

        }

        @DisplayName("Should Return Parsed Delete Query")
        @ParameterizedTest(name = "Should parser the query {0} FROM God")
        @ValueSource(strings = {"DELETE", "delete", "DeLeTe", "dElEtE", "DElete", "deLETE", "DeleTE", "DELete"})
        void shouldReturnParsedDeleteQuery(String queryCommand) {
            var query = queryCommand + " FROM God";
            ArgumentCaptor<DeleteQuery> captor = ArgumentCaptor.forClass(DeleteQuery.class);
            parser.query(query, null, manager, CommunicationObserverParser.EMPTY);
            Mockito.verify(manager).delete(captor.capture());
            DeleteQuery deleteQuery = captor.getValue();

            assertThat(deleteQuery.columns().isEmpty()).isTrue();
            assertThat(deleteQuery.name()).isEqualTo("God");
            assertThat(deleteQuery.condition().isPresent()).isFalse();
        }


        @DisplayName("Should Return Parser Query3")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"update God (name = \"Diana\")"})
        @Disabled
        void shouldReturnParserQuery3(String query) {
            ArgumentCaptor<CommunicationEntity> captor = ArgumentCaptor.forClass(CommunicationEntity.class);
            parser.query(query, null, manager, CommunicationObserverParser.EMPTY);
            Mockito.verify(manager).update(captor.capture());
            CommunicationEntity entity = captor.getValue();


            assertThat(entity.name()).isEqualTo("God");
            assertThat(entity.find("name").get()).isEqualTo(Element.of("name", "Diana"));
        }

        @DisplayName("Should Execute Prepare Statement")
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

        @DisplayName("Should Execute Prepare Statement2")
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
            assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(element.name()).isEqualTo("age");
            assertThat(element.get()).isEqualTo(12);
        }


        @DisplayName("Should Execute Prepare Statement3")
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
            assertThat(entity.name()).isEqualTo("God");
            assertThat(entity.find("name").get()).isEqualTo(Element.of("name", "Diana"));
        }

        @DisplayName("Should Single Result")
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
            assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(element.name()).isEqualTo("age");
            assertThat(element.get()).isEqualTo(12);
            assertThat(result.isPresent()).isTrue();
        }

        @DisplayName("Should Return Empty Single Result")
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
            assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(element.name()).isEqualTo("age");
            assertThat(element.get()).isEqualTo(12);
            assertThat(result.isPresent()).isFalse();
        }

        @DisplayName("Should Return Error Single Result")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM God WHERE age = :age"})
        void shouldReturnErrorSingleResult(String query) {
            ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);

            Mockito.when(manager.select(Mockito.any(SelectQuery.class)))
                    .thenReturn(Stream.of(Mockito.mock(CommunicationEntity.class), Mockito.mock(CommunicationEntity.class)));

            CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, CommunicationObserverParser.EMPTY);
            prepare.bind("age", 12);
           assertThatThrownBy(prepare::singleResult).isInstanceOf(NonUniqueResultException.class);
        }
    }

}
