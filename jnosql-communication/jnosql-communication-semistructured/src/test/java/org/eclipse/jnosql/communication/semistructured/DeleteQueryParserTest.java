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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.eq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeleteQueryParserTest {


    @Nested
    @DisplayName("When the delete query parser is used")
    class WhenTheDeleteQueryParserIsUsed {

        private final DeleteQueryParser parser = new DeleteQueryParser();

        private final DatabaseManager manager = Mockito.mock(DatabaseManager.class);

        private final CommunicationObserverParser observer = new CommunicationObserverParser() {
        };


        @DisplayName("Should Return Parser Query")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity"})
        void shouldReturnParserQuery(String query) {
            var captor = ArgumentCaptor.forClass(DeleteQuery.class);
            parser.query(query, manager, observer);
            Mockito.verify(manager).delete(captor.capture());
            var deleteQuery = captor.getValue();

            assertThat(deleteQuery.columns().isEmpty()).isTrue();
            assertThat(deleteQuery.name()).isEqualTo("entity");
            assertThat(deleteQuery.condition().isPresent()).isFalse();
        }


        @DisplayName("Should Return Parser Query11")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE stamina > 10.23"})
        void shouldReturnParserQuery11(String query) {
            var captor = ArgumentCaptor.forClass(DeleteQuery.class);
            parser.query(query, manager, observer);
            Mockito.verify(manager).delete(captor.capture());
            var deleteQuery = captor.getValue();

            checkBaseQuery(deleteQuery);
            assertThat(deleteQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = deleteQuery.condition().get();

            assertThat(condition.condition()).isEqualTo(Condition.GREATER_THAN);
            assertThat(condition.element()).isEqualTo(Element.of("stamina", 10.23));
        }

        @DisplayName("Should Return Parser Query12")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE stamina >= -10.23"})
        void shouldReturnParserQuery12(String query) {
            var captor = ArgumentCaptor.forClass(DeleteQuery.class);
            parser.query(query, manager, observer);
            Mockito.verify(manager).delete(captor.capture());
            var deleteQuery = captor.getValue();

            checkBaseQuery(deleteQuery);
            assertThat(deleteQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = deleteQuery.condition().get();

            assertThat(condition.condition()).isEqualTo(Condition.GREATER_EQUALS_THAN);
            assertThat(condition.element()).isEqualTo(Element.of("stamina", -10.23));
        }

        @DisplayName("Should Return Parser Query13")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE stamina <= -10.23"})
        void shouldReturnParserQuery13(String query) {
            var captor = ArgumentCaptor.forClass(DeleteQuery.class);
            parser.query(query, manager, observer);
            Mockito.verify(manager).delete(captor.capture());
            var updateQuery = captor.getValue();

            checkBaseQuery(updateQuery);
            assertThat(updateQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = updateQuery.condition().get();

            assertThat(condition.condition()).isEqualTo(Condition.LESSER_EQUALS_THAN);
            assertThat(condition.element()).isEqualTo(Element.of("stamina", -10.23));
        }

        @DisplayName("Should Return Parser Query14")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE stamina < -10.23"})
        void shouldReturnParserQuery14(String query) {
            var captor = ArgumentCaptor.forClass(DeleteQuery.class);
            parser.query(query, manager, observer);
            Mockito.verify(manager).delete(captor.capture());
            var updateQuery = captor.getValue();

            checkBaseQuery(updateQuery);
            assertThat(updateQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = updateQuery.condition().get();

            assertThat(condition.condition()).isEqualTo(Condition.LESSER_THAN);
            assertThat(condition.element()).isEqualTo(Element.of("stamina", -10.23));
        }

        @DisplayName("Should Return Parser Query15")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE age BETWEEN 10 AND 30"})
        void shouldReturnParserQuery15(String query) {
            var captor = ArgumentCaptor.forClass(DeleteQuery.class);
            parser.query(query, manager, observer);
            Mockito.verify(manager).delete(captor.capture());
            var updateQuery = captor.getValue();

            checkBaseQuery(updateQuery);
            assertThat(updateQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = updateQuery.condition().get();

            assertThat(condition.condition()).isEqualTo(Condition.BETWEEN);
            assertThat(condition.element()).isEqualTo(Element.of("age", Arrays.asList(10, 30)));
        }

        @DisplayName("Should Return Parser Query16")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE name = \"diana\""})
        void shouldReturnParserQuery16(String query) {
            var captor = ArgumentCaptor.forClass(DeleteQuery.class);
            parser.query(query, manager, observer);
            Mockito.verify(manager).delete(captor.capture());
            var updateQuery = captor.getValue();

            checkBaseQuery(updateQuery);
            assertThat(updateQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = updateQuery.condition().get();

            assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(condition.element()).isEqualTo(Element.of("name", "diana"));
        }


        @DisplayName("Should Return Parser Query20")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE name IN (\"Ada\", \"Apollo\")"})
        void shouldReturnParserQuery20(String query) {
            var captor = ArgumentCaptor.forClass(DeleteQuery.class);
            parser.query(query, manager, observer);
            Mockito.verify(manager).delete(captor.capture());
            var updateQuery = captor.getValue();

            checkBaseQuery(updateQuery);
            assertThat(updateQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = updateQuery.condition().get();
            Element element = condition.element();
            assertThat(condition.condition()).isEqualTo(Condition.IN);
            assertThat(element.name()).isEqualTo("name");
            List<String> values = element.get(new TypeReference<>() {
            });
            assertThat(values).contains("Ada", "Apollo");
        }

        @DisplayName("Should Return Parser Query21")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE name LIKE \"Ada\""})
        void shouldReturnParserQuery21(String query) {
            var captor = ArgumentCaptor.forClass(DeleteQuery.class);
            parser.query(query, manager, observer);
            Mockito.verify(manager).delete(captor.capture());
            var updateQuery = captor.getValue();

            checkBaseQuery(updateQuery);
            assertThat(updateQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = updateQuery.condition().get();
            Element element = condition.element();
            assertThat(condition.condition()).isEqualTo(Condition.LIKE);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get()).isEqualTo("Ada");
        }

        @DisplayName("Should Return Parser Query22")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE name NOT LIKE \"Ada\""})
        void shouldReturnParserQuery22(String query) {
            var captor = ArgumentCaptor.forClass(DeleteQuery.class);
            parser.query(query, manager, observer);
            Mockito.verify(manager).delete(captor.capture());
            var updateQuery = captor.getValue();

            checkBaseQuery(updateQuery);
            assertThat(updateQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = updateQuery.condition().get();
            Element element = condition.element();
            assertThat(condition.condition()).isEqualTo(Condition.NOT);
            List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
            });
            CriteriaCondition criteriaCondition = conditions.getFirst();
            assertThat(criteriaCondition.condition()).isEqualTo(Condition.LIKE);
            assertThat(criteriaCondition.element()).isEqualTo(Element.of("name", "Ada"));
        }

        @DisplayName("Should Return Parser Query23")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE name = \"Ada\" AND age = 20"})
        void shouldReturnParserQuery23(String query) {
            var captor = ArgumentCaptor.forClass(DeleteQuery.class);
            parser.query(query, manager, observer);
            Mockito.verify(manager).delete(captor.capture());
            var updateQuery = captor.getValue();

            checkBaseQuery(updateQuery);
            assertThat(updateQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = updateQuery.condition().get();
            Element element = condition.element();
            assertThat(condition.condition()).isEqualTo(Condition.AND);
            List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
            });
            assertThat(conditions).contains(eq(Element.of("name", "Ada")),
                    eq(Element.of("age", 20)));
        }

        @DisplayName("Should Return Parser Query24")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE name = \"Ada\" OR age = 20"})
        void shouldReturnParserQuery24(String query) {
            var captor = ArgumentCaptor.forClass(DeleteQuery.class);
            parser.query(query, manager, observer);
            Mockito.verify(manager).delete(captor.capture());
            var updateQuery = captor.getValue();

            checkBaseQuery(updateQuery);
            assertThat(updateQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = updateQuery.condition().get();
            Element element = condition.element();
            assertThat(condition.condition()).isEqualTo(Condition.OR);
            List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
            });
            assertThat(conditions).contains(eq(Element.of("name", "Ada")),
                    eq(Element.of("age", 20)));
        }


        @DisplayName("Should Return Error When Need Prepare Statement")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE age = :age"})
        void shouldReturnErrorWhenNeedPrepareStatement(String query) {
            assertThatThrownBy(() -> parser.query(query, manager, observer)).isInstanceOf(QueryException.class);
        }

        @DisplayName("Should Return Error When Is Query With Param")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE age = :age"})
        void shouldReturnErrorWhenIsQueryWithParam(String query) {

            assertThatThrownBy(() -> parser.query(query, manager, observer)).isInstanceOf(QueryException.class);

        }

        @DisplayName("Should Return Error When Dont Bind Parameters")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE age = :age"})
        void shouldReturnErrorWhenDontBindParameters(String query) {

            var prepare = parser.prepare(query, manager, observer);
            assertThatThrownBy(prepare::result).isInstanceOf(QueryException.class);
            assertThatThrownBy(prepare::count).isInstanceOf(QueryException.class);
        }

        @DisplayName("Should Return Error When Dont Bind Parameters Position")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE age = ?1"})
        void shouldReturnErrorWhenDontBindParametersPosition(String query) {

            var prepare = parser.prepare(query, manager, observer);
            assertThatThrownBy(prepare::result).isInstanceOf(QueryException.class);
        }

        @DisplayName("Should Execute Prepare Statement")
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
            assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(element.name()).isEqualTo("age");
            assertThat(element.get()).isEqualTo(12);
        }

        @DisplayName("Should Delegate Prepared Delete Count")
        @Test
        void shouldDelegatePreparedDeleteCount() {
            var query = "DELETE FROM entity WHERE age = :age";
            Mockito.when(manager.deleteAndCount(Mockito.any(DeleteQuery.class))).thenReturn(3L);

            var prepare = parser.prepare(query, manager, observer);
            prepare.bind("age", 12);

            assertThat(prepare.count()).isEqualTo(3L);

            var deleteCaptor = ArgumentCaptor.forClass(DeleteQuery.class);
            Mockito.verify(manager).deleteAndCount(deleteCaptor.capture());
            Mockito.verify(manager, Mockito.never()).count(Mockito.any(SelectQuery.class));
            Mockito.verify(manager, Mockito.never()).delete(Mockito.any(DeleteQuery.class));
            var deleteQuery = deleteCaptor.getValue();
            assertThat(deleteQuery.name()).isEqualTo("entity");
            var condition = deleteQuery.condition().orElseThrow();
            assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(condition.element().name()).isEqualTo("age");
            assertThat(condition.element().get()).isEqualTo(12);
        }

        @DisplayName("Should Execute Prepare Statement Position")
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
            assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(element.name()).isEqualTo("age");
            assertThat(element.get()).isEqualTo(12);
        }

        @DisplayName("Should Return Query Special True")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE active = true"})
        void shouldReturnQuerySpecialTrue(String query) {
            var captor = ArgumentCaptor.forClass(DeleteQuery.class);
             parser.query(query, manager, observer);
            Mockito.verify(manager).delete(captor.capture());
            var deleteQuery = captor.getValue();

            checkBaseQuery(deleteQuery);
            assertThat(deleteQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = deleteQuery.condition().get();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
                softly.assertThat(condition.element()).isEqualTo(Element.of("active", true));
            });
        }


        @DisplayName("Should Return Query Special False")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE active = false"})
        void shouldReturnQuerySpecialFalse(String query) {
            var captor = ArgumentCaptor.forClass(DeleteQuery.class);
            parser.query(query, manager, observer);
            Mockito.verify(manager).delete(captor.capture());
            var deleteQuery = captor.getValue();

            checkBaseQuery(deleteQuery);
            assertThat(deleteQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = deleteQuery.condition().get();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
                softly.assertThat(condition.element()).isEqualTo(Element.of("active", false));
            });
        }

        @DisplayName("Should Return Query Special Null")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE active IS NULL"})
        void shouldReturnQuerySpecialNull(String query) {
            var captor = ArgumentCaptor.forClass(DeleteQuery.class);
            parser.query(query, manager, observer);
            Mockito.verify(manager).delete(captor.capture());
            var deleteQuery = captor.getValue();

            checkBaseQuery(deleteQuery);
            assertThat(deleteQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = deleteQuery.condition().get();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
                softly.assertThat(condition.element()).isEqualTo(Element.of("active", Value.ofNull()));
            });
        }

        @DisplayName("Should Return Query Special Not Null")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"DELETE FROM entity WHERE active IS NOT NULL"})
        void shouldReturnQuerySpecialNotNull(String query) {
            var captor = ArgumentCaptor.forClass(DeleteQuery.class);
            parser.query(query, manager, observer);
            Mockito.verify(manager).delete(captor.capture());
            var deleteQuery = captor.getValue();

            checkBaseQuery(deleteQuery);
            assertThat(deleteQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = deleteQuery.condition().get();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(condition.condition()).isEqualTo(Condition.NOT);
                CriteriaCondition subCondition = condition.element().get(CriteriaCondition.class);
                softly.assertThat(subCondition.condition()).isEqualTo(Condition.EQUALS);
                softly.assertThat(subCondition.element()).isEqualTo(Element.of("active", Value.ofNull()));
            });
        }

        private void checkBaseQuery(DeleteQuery columnQuery) {
            assertThat(columnQuery.columns().isEmpty()).isTrue();
            assertThat(columnQuery.name()).isEqualTo("entity");
        }
    }

}
