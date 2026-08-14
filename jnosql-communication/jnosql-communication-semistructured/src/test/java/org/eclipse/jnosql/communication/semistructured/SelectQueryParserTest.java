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

import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.eq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SelectQueryParserTest {


    @Nested
    @DisplayName("When the select query parser is used")
    class WhenTheSelectQueryParserIsUsed {

        private final SelectQueryParser parser = new SelectQueryParser();

        private final DatabaseManager manager = Mockito.mock(DatabaseManager.class);

        private final CommunicationObserverParser observer = new CommunicationObserverParser() {
        };


        @DisplayName("Should Return Parser Query3")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity ORDER BY name ASC"})
        void shouldReturnParserQuery3(String query) {
            var captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            var selectQuery = captor.getValue();

            assertThat(selectQuery.columns().isEmpty()).isTrue();
            assertThat(selectQuery.sorts()).contains(Sort.of("name", Direction.ASC, false));
            assertThat(selectQuery.limit()).isEqualTo(0L);
            assertThat(selectQuery.skip()).isEqualTo(0L);
            assertThat(selectQuery.name()).isEqualTo("entity");
            assertThat(selectQuery.condition().isPresent()).isFalse();
        }

        @DisplayName("Should Return Parser Query4")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity ORDER BY name ASC"})
        void shouldReturnParserQuery4(String query) {
            var captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            var selectQuery = captor.getValue();

            assertThat(selectQuery.columns().isEmpty()).isTrue();
            assertThat(selectQuery.sorts()).contains(Sort.of("name", Direction.ASC, false));
            assertThat(selectQuery.limit()).isEqualTo(0L);
            assertThat(selectQuery.skip()).isEqualTo(0L);
            assertThat(selectQuery.name()).isEqualTo("entity");
            assertThat(selectQuery.condition().isPresent()).isFalse();
        }

        @DisplayName("Should Return Parser Query5")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity ORDER BY name DESC"})
        void shouldReturnParserQuery5(String query) {
            var captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            var selectQuery = captor.getValue();

            assertThat(selectQuery.columns().isEmpty()).isTrue();
            assertThat(selectQuery.sorts()).contains(Sort.of("name", Direction.DESC, false));
            assertThat(selectQuery.limit()).isEqualTo(0L);
            assertThat(selectQuery.skip()).isEqualTo(0L);
            assertThat(selectQuery.name()).isEqualTo("entity");
            assertThat(selectQuery.condition().isPresent()).isFalse();
        }

        @DisplayName("Should Return Parser Query6")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity ORDER BY name DESC, age ASC"})
        void shouldReturnParserQuery6(String query) {

            var captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            var selectQuery = captor.getValue();

            assertThat(selectQuery.columns().isEmpty()).isTrue();
            assertThat(selectQuery.sorts()).contains(Sort.desc("name"),
                    Sort.asc("age"));
            assertThat(selectQuery.limit()).isEqualTo(0L);
            assertThat(selectQuery.skip()).isEqualTo(0L);
            assertThat(selectQuery.name()).isEqualTo("entity");
            assertThat(selectQuery.condition().isPresent()).isFalse();
        }

        @DisplayName("Should Return Parser Query10")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE age = 10"})
        void shouldReturnParserQuery10(String query) {
            var captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            var selectQuery = captor.getValue();

            checkBaseQuery(selectQuery);
            assertThat(selectQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = selectQuery.condition().get();

            assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(condition.element()).isEqualTo(Element.of("age", 10));
        }

        @DisplayName("Should Return Parser Query11")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE stamina > 10.23"})
        void shouldReturnParserQuery11(String query) {
            var captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            var selectQuery = captor.getValue();

            checkBaseQuery(selectQuery);
            assertThat(selectQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = selectQuery.condition().get();

            assertThat(condition.condition()).isEqualTo(Condition.GREATER_THAN);
            assertThat(condition.element()).isEqualTo(Element.of("stamina", 10.23));
        }

        @DisplayName("Should Return Parser Query12")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE stamina >= -10.23"})
        void shouldReturnParserQuery12(String query) {
            var captor = ArgumentCaptor.forClass(SelectQuery.class);
        
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            SelectQuery selectQuery = captor.getValue();

            checkBaseQuery(selectQuery);
            assertThat(selectQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = selectQuery.condition().get();

            assertThat(condition.condition()).isEqualTo(Condition.GREATER_EQUALS_THAN);
            assertThat(condition.element()).isEqualTo(Element.of("stamina", -10.23));
        }

        @DisplayName("Should Return Parser Query13")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE stamina <= -10.23"})
        void shouldReturnParserQuery13(String query) {
            var captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            var selectQuery = captor.getValue();

            checkBaseQuery(selectQuery);
            assertThat(selectQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = selectQuery.condition().get();

            assertThat(condition.condition()).isEqualTo(Condition.LESSER_EQUALS_THAN);
            assertThat(condition.element()).isEqualTo(Element.of("stamina", -10.23));
        }

        @DisplayName("Should Return Parser Query14")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE stamina < -10.23"})
        void shouldReturnParserQuery14(String query) {
            var captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            var selectQuery = captor.getValue();

            checkBaseQuery(selectQuery);
            assertThat(selectQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = selectQuery.condition().get();

            assertThat(condition.condition()).isEqualTo(Condition.LESSER_THAN);
            assertThat(condition.element()).isEqualTo(Element.of("stamina", -10.23));
        }

        @DisplayName("Should Return Parser Query15")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE age BETWEEN 10 AND 30"})
        void shouldReturnParserQuery15(String query) {
            var captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            var selectQuery = captor.getValue();

            checkBaseQuery(selectQuery);
            assertThat(selectQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = selectQuery.condition().get();

            assertThat(condition.condition()).isEqualTo(Condition.BETWEEN);
            assertThat(condition.element()).isEqualTo(Element.of("age", Arrays.asList(10, 30)));
        }

        @DisplayName("Should Return Parser Query16")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE name = \"diana\""})
        void shouldReturnParserQuery16(String query) {
            ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            SelectQuery selectQuery = captor.getValue();

            checkBaseQuery(selectQuery);
            assertThat(selectQuery.condition().isPresent()).isTrue();
            var condition = selectQuery.condition().get();

            assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(condition.element()).isEqualTo(Element.of("name", "diana"));
        }

        @DisplayName("Should Return Parser Query20")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE name IN (\"Ada\", \"Apollo\")"})
        void shouldReturnParserQuery20(String query) {
            ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            SelectQuery selectQuery = captor.getValue();

            checkBaseQuery(selectQuery);
            assertThat(selectQuery.condition().isPresent()).isTrue();
            var condition = selectQuery.condition().get();
            Element element = condition.element();
            assertThat(condition.condition()).isEqualTo(Condition.IN);
            assertThat(element.name()).isEqualTo("name");
            List<String> values = element.get(new TypeReference<>() {
            });
            assertThat(values).contains("Ada", "Apollo");
        }

        @DisplayName("Should Return Parser Query21")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE name LIKE \"Ada\""})
        void shouldReturnParserQuery21(String query) {
            ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            SelectQuery selectQuery = captor.getValue();

            checkBaseQuery(selectQuery);
            assertThat(selectQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = selectQuery.condition().get();
            Element element = condition.element();
            assertThat(condition.condition()).isEqualTo(Condition.LIKE);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get()).isEqualTo("Ada");
        }

        @DisplayName("Should Return Parser Query22")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE name NOT LIKE \"Ada\""})
        void shouldReturnParserQuery22(String query) {
            ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            SelectQuery selectQuery = captor.getValue();

            checkBaseQuery(selectQuery);
            assertThat(selectQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = selectQuery.condition().get();
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
        @ValueSource(strings = {"FROM entity WHERE name = \"Ada\" AND age = 20"})
        void shouldReturnParserQuery23(String query) {
            ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            SelectQuery selectQuery = captor.getValue();

            checkBaseQuery(selectQuery);
            assertThat(selectQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = selectQuery.condition().get();
            Element element = condition.element();
            assertThat(condition.condition()).isEqualTo(Condition.AND);
            List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
            });
            assertThat(conditions).contains(eq(Element.of("name", "Ada")),
                    eq(Element.of("age", 20)));
        }

        @DisplayName("Should Return Parser Query24")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE name = \"Ada\" OR age = 20"})
        void shouldReturnParserQuery24(String query) {
            ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            SelectQuery selectQuery = captor.getValue();

            checkBaseQuery(selectQuery);
            assertThat(selectQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = selectQuery.condition().get();
            Element element = condition.element();
            assertThat(condition.condition()).isEqualTo(Condition.OR);
            List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
            });
            assertThat(conditions).contains(eq(Element.of("name", "Ada")),
                    eq(Element.of("age", 20)));
        }


        @DisplayName("Should Return Error When Is Query With Param")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE age = :age"})
        void shouldReturnErrorWhenIsQueryWithParam(String query) {

            assertThatThrownBy(() -> parser.query(query, null, manager, observer)).isInstanceOf(QueryException.class);
        }

        @DisplayName("Should Return Error When Dont Bind Parameters")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE age = :age"})
        void shouldReturnErrorWhenDontBindParameters(String query) {

            CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, observer);
            assertThatThrownBy(prepare::result).isInstanceOf(QueryException.class);
        }

        @DisplayName("Should Execute Prepare Statement")
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
            assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(element.name()).isEqualTo("age");
            assertThat(element.get()).isEqualTo(12);
        }

        @DisplayName("Should Execute Prepare Statement Index")
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
            assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(element.name()).isEqualTo("age");
            assertThat(element.get()).isEqualTo(12);
        }

        @DisplayName("Should Execute Prepare Statement Index2")
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
            assertThat(criteriaCondition.condition()).isEqualTo(Condition.AND);
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

        @DisplayName("Should Count")
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

        @DisplayName("Should Count Execute Prepare Statement Index2")
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
            assertThat(criteriaCondition.condition()).isEqualTo(Condition.AND);
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

        @DisplayName("Should Get Issue When Count")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"select count(this) FROM entity WHERE age = ?1 AND name = ?2"})
        void shouldGetIssueWhenCount(String query) {
            CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, observer);
            assertThatThrownBy(prepare::count);

        }

        @DisplayName("Should Return Error When Result Instead Of Count")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"select count(this) FROM entity WHERE age = ?1 AND name = ?2"})
        void shouldReturnErrorWhenResultInsteadOfCount(String query) {

            CommunicationPreparedStatement prepare = parser.prepare(query, null, manager, observer);
            prepare.bind(1, 12);
            prepare.bind(2, "Otavio");

            assertThatThrownBy(prepare::result).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(prepare::singleResult).isInstanceOf(UnsupportedOperationException.class);
        }

        @DisplayName("Should Run Is Null")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE name is null"})
        void shouldRunIsNull(String query) {
            ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            var selectQuery = captor.getValue();

            checkBaseQuery(selectQuery);
            assertThat(selectQuery.condition().isPresent()).isTrue();
            var condition = selectQuery.condition().get();

            assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(condition.element()).isEqualTo(Element.of("name", null));
        }

        @DisplayName("Should Run Is Not Null")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE name is not null"})
        void shouldRunIsNotNull(String query) {
            ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            var selectQuery = captor.getValue();

            checkBaseQuery(selectQuery);
            assertThat(selectQuery.condition().isPresent()).isTrue();
            var condition = selectQuery.condition().get();
            var criteriaCondition = condition.element().get(CriteriaCondition.class);
            assertThat(condition.condition()).isEqualTo(Condition.NOT);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
                softly.assertThat(criteriaCondition.element()).isEqualTo(Element.of("name", null));
            });
        }

        @DisplayName("Should Replace Query")
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

        @DisplayName("Should Get Query")
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

        @DisplayName("Should Return Query Special True")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE active = true"})
        void shouldReturnQuerySpecialTrue(String query) {
            var captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            var selectQuery = captor.getValue();

            checkBaseQuery(selectQuery);
            assertThat(selectQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = selectQuery.condition().get();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
                softly.assertThat(condition.element()).isEqualTo(Element.of("active", true));
            });
        }


        @DisplayName("Should Return Query Special False")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE active = false"})
        void shouldReturnQuerySpecialFalse(String query) {
            var captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            var selectQuery = captor.getValue();

            checkBaseQuery(selectQuery);
            assertThat(selectQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = selectQuery.condition().get();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
                softly.assertThat(condition.element()).isEqualTo(Element.of("active", false));
            });
        }

        @DisplayName("Should Return Query Special Null")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE active IS NULL"})
        void shouldReturnQuerySpecialNull(String query) {
            var captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            var selectQuery = captor.getValue();

            checkBaseQuery(selectQuery);
            assertThat(selectQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = selectQuery.condition().get();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
                softly.assertThat(condition.element()).isEqualTo(Element.of("active", Value.ofNull()));
            });
        }

        @DisplayName("Should Return Query Special Not Null")
        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"FROM entity WHERE active IS NOT NULL"})
        void shouldReturnQuerySpecialNotNull(String query) {
            var captor = ArgumentCaptor.forClass(SelectQuery.class);
            parser.query(query, null, manager, observer);
            Mockito.verify(manager).select(captor.capture());
            var selectQuery = captor.getValue();

            checkBaseQuery(selectQuery);
            assertThat(selectQuery.condition().isPresent()).isTrue();
            CriteriaCondition condition = selectQuery.condition().get();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(condition.condition()).isEqualTo(Condition.NOT);
                CriteriaCondition subCondition = condition.element().get(CriteriaCondition.class);
                softly.assertThat(subCondition.condition()).isEqualTo(Condition.EQUALS);
                softly.assertThat(subCondition.element()).isEqualTo(Element.of("active", Value.ofNull()));
            });
        }

        @DisplayName("Should Apply")
        @Test
        void shouldApply() {
            SelectQueryParser queryParser = new SelectQueryParser();
            org.eclipse.jnosql.communication.query.SelectQuery query = Mockito.mock(org.eclipse.jnosql.communication.query.SelectQuery.class);
            CommunicationObserverParser observer = Mockito.mock(CommunicationObserverParser.class);
            queryParser.apply(query, observer);
        }

        private void checkBaseQuery(SelectQuery selectQuery) {
            assertThat(selectQuery.columns().isEmpty()).isTrue();
            assertThat(selectQuery.sorts().isEmpty()).isTrue();
            assertThat(selectQuery.limit()).isEqualTo(0L);
            assertThat(selectQuery.skip()).isEqualTo(0L);
            assertThat(selectQuery.name()).isEqualTo("entity");
        }
    }

}
