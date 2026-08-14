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

import jakarta.data.Sort;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.eq;
import static org.eclipse.jnosql.communication.semistructured.SelectQuery.builder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultElementQueryBuilderTest {


    @Nested
    @DisplayName("When the default element query builder is used")
    class WhenTheDefaultElementQueryBuilderIsUsed {

        @DisplayName("Should Return Error When Has Null Element In Select")
        @Test
        void shouldReturnErrorWhenHasNullElementInSelect() {
            assertThatThrownBy(() -> builder("document", "document'", null)).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Builder")
        @Test
        void shouldBuilder() {
            String documentCollection = "documentCollection";
            SelectQuery query = builder().from(documentCollection).build();
            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.condition().isPresent()).isFalse();
            assertThat(query.name()).isEqualTo(documentCollection);
        }

        @DisplayName("Should Select Document")
        @Test
        void shouldSelectDocument() {
            String documentCollection = "documentCollection";
            SelectQuery query = builder("document", "document2").from(documentCollection).build();
            assertThat(query.columns()).contains("document", "document2");
            assertThat(query.condition().isPresent()).isFalse();
            assertThat(query.name()).isEqualTo(documentCollection);
        }

        @DisplayName("Should Return Error When From Is Null")
        @Test
        void shouldReturnErrorWhenFromIsNull() {
            assertThatThrownBy(() -> builder().from(null)).isInstanceOf(NullPointerException.class);
        }


        @DisplayName("Should Select Order Asc")
        @Test
        void shouldSelectOrderAsc() {
            String documentCollection = "documentCollection";
            SelectQuery query = builder().from(documentCollection).sort(Sort.asc("name")).build();
            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.condition().isPresent()).isFalse();
            assertThat(query.name()).isEqualTo(documentCollection);
            assertThat(query.sorts()).contains(Sort.asc("name"));
        }

        @DisplayName("Should Select Order Desc")
        @Test
        void shouldSelectOrderDesc() {
            String documentCollection = "documentCollection";
            SelectQuery query = builder().from(documentCollection).sort(Sort.desc("name")).build();
            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.condition().isPresent()).isFalse();
            assertThat(query.name()).isEqualTo(documentCollection);
            assertThat(query.sorts()).contains(Sort.desc("name"));
        }


        @DisplayName("Should Return Error Select When Order Is Null")
        @Test
        void shouldReturnErrorSelectWhenOrderIsNull() {
            assertThatThrownBy(() -> {
                String documentCollection = "documentCollection";
                builder().from(documentCollection).sort((Sort<?>) null);
            }).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Select Limit")
        @Test
        void shouldSelectLimit() {
            String documentCollection = "documentCollection";
            SelectQuery query = builder().from(documentCollection).limit(10).build();
            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.condition().isPresent()).isFalse();
            assertThat(query.name()).isEqualTo(documentCollection);
            assertThat(query.limit()).isEqualTo(10L);
        }

        @DisplayName("Should Return Error When Limit Is Negative")
        @Test
        void shouldReturnErrorWhenLimitIsNegative() {
            String documentCollection = "documentCollection";
            assertThatThrownBy(() -> builder().from(documentCollection).limit(-1)).isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("Should Select Skip")
        @Test
        void shouldSelectSkip() {
            String documentCollection = "documentCollection";
            SelectQuery query = builder().from(documentCollection).skip(10).build();
            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.condition().isPresent()).isFalse();
            assertThat(query.name()).isEqualTo(documentCollection);
            assertThat(query.skip()).isEqualTo(10L);
        }

        @DisplayName("Should Return Error When Skip Is Negative")
        @Test
        void shouldReturnErrorWhenSkipIsNegative() {
            String documentCollection = "documentCollection";
            assertThatThrownBy(() -> builder().from(documentCollection).skip(-1)).isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("Should Select Where Name Eq")
        @Test
        void shouldSelectWhereNameEq() {
            String documentCollection = "documentCollection";
            String name = "Ada Lovelace";

            SelectQuery query = builder().from(documentCollection)
                    .where(eq("name", name))
                    .build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();

            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(documentCollection);
            assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get()).isEqualTo(name);

        }

        @DisplayName("Should Select Where Name Like")
        @Test
        void shouldSelectWhereNameLike() {
            String documentCollection = "documentCollection";
            String name = "Ada Lovelace";
            SelectQuery query = builder().from(documentCollection)
                    .where(CriteriaCondition.like("name", name))
                    .build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();

            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(documentCollection);
            assertThat(condition.condition()).isEqualTo(Condition.LIKE);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get()).isEqualTo(name);
        }

        @DisplayName("Should Select Where Name Gt")
        @Test
        void shouldSelectWhereNameGt() {
            String documentCollection = "documentCollection";
            Number value = 10;

            SelectQuery query = builder().from(documentCollection).where(CriteriaCondition.gt("name", 10))
                    .build();

            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();

            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(documentCollection);
            assertThat(condition.condition()).isEqualTo(Condition.GREATER_THAN);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get()).isEqualTo(value);
        }

        @DisplayName("Should Select Where Name Gte")
        @Test
        void shouldSelectWhereNameGte() {
            String documentCollection = "documentCollection";
            Number value = 10;
            CriteriaCondition gteName = CriteriaCondition.gte("name", value);
            SelectQuery query = builder().from(documentCollection).where(gteName).build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();

            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(documentCollection);
            assertThat(condition.condition()).isEqualTo(Condition.GREATER_EQUALS_THAN);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get()).isEqualTo(value);
        }

        @DisplayName("Should Select Where Name Lt")
        @Test
        void shouldSelectWhereNameLt() {
            String documentCollection = "documentCollection";
            Number value = 10;

            SelectQuery query = builder().from(documentCollection).where(CriteriaCondition.lt("name", value))
                    .build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();

            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(documentCollection);
            assertThat(condition.condition()).isEqualTo(Condition.LESSER_THAN);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get()).isEqualTo(value);
        }

        @DisplayName("Should Select Where Name Lte")
        @Test
        void shouldSelectWhereNameLte() {
            String documentCollection = "documentCollection";
            Number value = 10;
            SelectQuery query = builder().from(documentCollection).where(CriteriaCondition.lte("name", value))
                    .build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();

            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(documentCollection);
            assertThat(condition.condition()).isEqualTo(Condition.LESSER_EQUALS_THAN);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get()).isEqualTo(value);
        }

        @DisplayName("Should Select Where Name Between")
        @Test
        void shouldSelectWhereNameBetween() {
            String documentCollection = "documentCollection";
            Number valueA = 10;
            Number valueB = 20;

            SelectQuery query = builder().from(documentCollection)
                    .where(CriteriaCondition.between("name", Arrays.asList(valueA, valueB)))
                    .build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();

            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(documentCollection);
            assertThat(condition.condition()).isEqualTo(Condition.BETWEEN);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get(new TypeReference<List<Number>>() {
            })).contains(10, 20);
        }

        @DisplayName("Should Select Where Name Not")
        @Test
        void shouldSelectWhereNameNot() {
            String documentCollection = "documentCollection";
            String name = "Ada Lovelace";

            SelectQuery query = builder().from(documentCollection).where(eq("name", name).negate())
                    .build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();
            CriteriaCondition negate = element.get(CriteriaCondition.class);
            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(documentCollection);
            assertThat(condition.condition()).isEqualTo(Condition.NOT);
            assertThat(negate.condition()).isEqualTo(Condition.EQUALS);
            assertThat(negate.element().name()).isEqualTo("name");
            assertThat(negate.element().get()).isEqualTo(name);
        }


        @DisplayName("Should Select Where Name And")
        @Test
        void shouldSelectWhereNameAnd() {
            String documentCollection = "documentCollection";
            String name = "Ada Lovelace";
            CriteriaCondition nameEqualsAda = eq("name", name);
            CriteriaCondition ageOlderTen = CriteriaCondition.gt("age", 10);
            SelectQuery query = builder().from(documentCollection)
                    .where(CriteriaCondition.and(nameEqualsAda, ageOlderTen))
                    .build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();
            List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
            });
            assertThat(condition.condition()).isEqualTo(Condition.AND);
            assertThat(conditions).contains(eq(Element.of("name", name)),
                    CriteriaCondition.gt(Element.of("age", 10)));
        }

        @DisplayName("Should Select Where Name Or")
        @Test
        void shouldSelectWhereNameOr() {
            String documentCollection = "documentCollection";
            String name = "Ada Lovelace";
            CriteriaCondition nameEqualsAda = eq("name", name);
            CriteriaCondition ageOlderTen = CriteriaCondition.gt("age", 10);
            SelectQuery query = builder().from(documentCollection).where(CriteriaCondition.or(nameEqualsAda, ageOlderTen))
                    .build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();
            List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
            });
            assertThat(condition.condition()).isEqualTo(Condition.OR);
            assertThat(conditions).contains(eq(Element.of("name", name)),
                    CriteriaCondition.gt(Element.of("age", 10)));
        }


        @DisplayName("Should Select Negate")
        @Test
        void shouldSelectNegate() {
            String columnFamily = "columnFamily";
            CriteriaCondition nameNotEqualsLucas = eq("name", "Lucas").negate();
            SelectQuery query = builder().from(columnFamily)
                    .where(nameNotEqualsLucas).build();

            CriteriaCondition condition = query.condition().orElseThrow(RuntimeException::new);
            assertThat(query.name()).isEqualTo(columnFamily);
            Element element = condition.element();
            List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
            });

            assertThat(condition.condition()).isEqualTo(Condition.NOT);
            assertThat(conditions).contains(eq(Element.of("name", "Lucas")));

        }


        @DisplayName("Should Execute Manager")
        @Test
        void shouldExecuteManager() {
            DatabaseManager manager = Mockito.mock(DatabaseManager.class);
            ArgumentCaptor<SelectQuery> queryCaptor = ArgumentCaptor.forClass(SelectQuery.class);
            String collection = "collection";
            Stream<CommunicationEntity> entities = builder().from(collection).getResult(manager);
            Mockito.verify(manager).select(queryCaptor.capture());
            checkQuery(queryCaptor, collection);
        }

        @DisplayName("Should Execute Single Result Manager")
        @Test
        void shouldExecuteSingleResultManager() {
            DatabaseManager manager = Mockito.mock(DatabaseManager.class);
            ArgumentCaptor<SelectQuery> queryCaptor = ArgumentCaptor.forClass(SelectQuery.class);
            String collection = "collection";
            Optional<CommunicationEntity> entities = builder().from(collection).getSingleResult(manager);
            Mockito.verify(manager).singleResult(queryCaptor.capture());
            checkQuery(queryCaptor, collection);
        }

        private void checkQuery(ArgumentCaptor<SelectQuery> queryCaptor, String collection) {
            SelectQuery query = queryCaptor.getValue();
            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.condition().isPresent()).isFalse();
            assertThat(query.name()).isEqualTo(collection);
        }
    }

}
