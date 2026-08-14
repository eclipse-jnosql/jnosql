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
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.eq;
import static org.eclipse.jnosql.communication.semistructured.SelectQuery.builder;
import static org.eclipse.jnosql.communication.semistructured.SelectQuery.select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultSelectQueryBuilderTest {



    @Nested
    @DisplayName("When the default select query builder is used")
    class WhenTheDefaultSelectQueryBuilderIsUsed {

        @DisplayName("Should Return Error When Has Null Element In Select")
        @Test
        void shouldReturnErrorWhenHasNullElementInSelect() {
            assertThatThrownBy(() -> select("column", "column", null)).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Select")
        @Test
        void shouldSelect() {
            String columnFamily = "columnFamily";
            SelectQuery query = select().from(columnFamily).build();
            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.condition().isPresent()).isFalse();
            assertThat(query.name()).isEqualTo(columnFamily);
        }

        @DisplayName("Should Select Columns")
        @Test
        void shouldSelectColumns() {
            String columnFamily = "columnFamily";
            SelectQuery query = select("column", "column2").from(columnFamily).build();
            assertThat(query.columns()).contains("column", "column2");
            assertThat(query.condition().isPresent()).isFalse();
            assertThat(query.name()).isEqualTo(columnFamily);
        }

        @DisplayName("Should Return Error When From Is Null")
        @Test
        void shouldReturnErrorWhenFromIsNull() {
            assertThatThrownBy(() -> select().from(null)).isInstanceOf(NullPointerException.class);
        }


        @DisplayName("Should Select Order Asc")
        @Test
        void shouldSelectOrderAsc() {
            String columnFamily = "columnFamily";
            SelectQuery query = select().from(columnFamily).orderBy("name").asc().build();
            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.condition().isPresent()).isFalse();
            assertThat(query.name()).isEqualTo(columnFamily);
            assertThat(query.sorts()).contains(Sort.of("name", Direction.ASC, false));
        }

        @DisplayName("Should Select Order Desc")
        @Test
        void shouldSelectOrderDesc() {
            String columnFamily = "columnFamily";
            SelectQuery query = select().from(columnFamily).orderBy("name").desc().build();
            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.condition().isPresent()).isFalse();
            assertThat(query.name()).isEqualTo(columnFamily);
            assertThat(query.sorts()).contains(Sort.of("name", Direction.DESC, false));
        }

        @DisplayName("Should Return Error Select When Order Is Null")
        @Test
        void shouldReturnErrorSelectWhenOrderIsNull() {
            assertThatThrownBy(() -> {
                String columnFamily = "columnFamily";
                select().from(columnFamily).orderBy(null);
            }).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Select Limit")
        @Test
        void shouldSelectLimit() {
            String columnFamily = "columnFamily";
            SelectQuery query = select().from(columnFamily).limit(10).build();
            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.condition().isPresent()).isFalse();
            assertThat(query.name()).isEqualTo(columnFamily);
            assertThat(query.limit()).isEqualTo(10L);
        }

        @DisplayName("Should Return Error When Limit Is Negative")
        @Test
        void shouldReturnErrorWhenLimitIsNegative() {
            String columnFamily = "columnFamily";
            assertThatThrownBy(() -> builder().from(columnFamily).limit(-1)).isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("Should Select Skip")
        @Test
        void shouldSelectSkip() {
            String columnFamily = "columnFamily";
            SelectQuery query = select().from(columnFamily).skip(10).build();
            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.condition().isPresent()).isFalse();
            assertThat(query.name()).isEqualTo(columnFamily);
            assertThat(query.skip()).isEqualTo(10L);
        }

        @DisplayName("Should Return Error When Skip Is Negative")
        @Test
        void shouldReturnErrorWhenSkipIsNegative() {
            String columnFamily = "columnFamily";
            assertThatThrownBy(() -> builder().from(columnFamily).skip(-1)).isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("Should Select Where Name Eq")
        @Test
        void shouldSelectWhereNameEq() {
            String columnFamily = "columnFamily";
            String name = "Ada Lovelace";
            SelectQuery query = select().from(columnFamily).where("name").eq(name).build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();

            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(columnFamily);
            assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get()).isEqualTo(name);

        }

        @DisplayName("Should Select Where Name Like")
        @Test
        void shouldSelectWhereNameLike() {
            String columnFamily = "columnFamily";
            String name = "Ada Lovelace";
            SelectQuery query = select().from(columnFamily).where("name").like(name).build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();

            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(columnFamily);
            assertThat(condition.condition()).isEqualTo(Condition.LIKE);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get()).isEqualTo(name);
        }

        @DisplayName("Should Select Where Name Gt")
        @Test
        void shouldSelectWhereNameGt() {
            String columnFamily = "columnFamily";
            Number value = 10;
            SelectQuery query = select().from(columnFamily).where("name").gt(value).build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();

            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(columnFamily);
            assertThat(condition.condition()).isEqualTo(Condition.GREATER_THAN);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get()).isEqualTo(value);
        }

        @DisplayName("Should Select Where Name Gte")
        @Test
        void shouldSelectWhereNameGte() {
            String columnFamily = "columnFamily";
            Number value = 10;
            SelectQuery query = select().from(columnFamily).where("name").gte(value).build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();

            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(columnFamily);
            assertThat(condition.condition()).isEqualTo(Condition.GREATER_EQUALS_THAN);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get()).isEqualTo(value);
        }

        @DisplayName("Should Select Where Name Lt")
        @Test
        void shouldSelectWhereNameLt() {
            String columnFamily = "columnFamily";
            Number value = 10;
            SelectQuery query = select().from(columnFamily).where("name").lt(value).build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();

            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(columnFamily);
            assertThat(condition.condition()).isEqualTo(Condition.LESSER_THAN);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get()).isEqualTo(value);
        }

        @DisplayName("Should Select Where Name Lte")
        @Test
        void shouldSelectWhereNameLte() {
            String columnFamily = "columnFamily";
            Number value = 10;
            SelectQuery query = select().from(columnFamily).where("name").lte(value).build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();

            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(columnFamily);
            assertThat(condition.condition()).isEqualTo(Condition.LESSER_EQUALS_THAN);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get()).isEqualTo(value);
        }

        @DisplayName("Should Select Where Name Between")
        @Test
        void shouldSelectWhereNameBetween() {
            String columnFamily = "columnFamily";
            Number valueA = 10;
            Number valueB = 20;
            SelectQuery query = select().from(columnFamily).where("name").between(valueA, valueB).build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();

            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(columnFamily);
            assertThat(condition.condition()).isEqualTo(Condition.BETWEEN);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get(new TypeReference<List<Number>>() {
            })).contains(10, 20);
        }

        @DisplayName("Should Select Where Name Not")
        @Test
        void shouldSelectWhereNameNot() {
            String columnFamily = "columnFamily";
            String name = "Ada Lovelace";
            SelectQuery query = select().from(columnFamily).where("name").not().eq(name).build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();
            CriteriaCondition negate = element.get(CriteriaCondition.class);
            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(columnFamily);
            assertThat(condition.condition()).isEqualTo(Condition.NOT);
            assertThat(negate.condition()).isEqualTo(Condition.EQUALS);
            assertThat(negate.element().name()).isEqualTo("name");
            assertThat(negate.element().get()).isEqualTo(name);
        }


        @DisplayName("Should Select Where Name And")
        @Test
        void shouldSelectWhereNameAnd() {
            String columnFamily = "columnFamily";
            String name = "Ada Lovelace";
            SelectQuery query = select().from(columnFamily).where("name").eq(name).and("age").gt(10).build();
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
            String columnFamily = "columnFamily";
            String name = "Ada Lovelace";
            SelectQuery query = select().from(columnFamily).where("name").eq(name).or("age").gt(10).build();
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
            SelectQuery query = select().from(columnFamily).where("city").not().eq("Assis")
                    .and("name").not().eq("Lucas").build();

            CriteriaCondition condition = query.condition().orElseThrow(RuntimeException::new);
            assertThat(query.name()).isEqualTo(columnFamily);
            Element element = condition.element();
            List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
            });

            assertThat(condition.condition()).isEqualTo(Condition.AND);
            assertThat(conditions).contains(eq(Element.of("city", "Assis")).negate(),
                    eq(Element.of("name", "Lucas")).negate());


        }

        @DisplayName("Should Execute Manager")
        @Test
        void shouldExecuteManager() {
            DatabaseManager manager = Mockito.mock(DatabaseManager.class);
            ArgumentCaptor<SelectQuery> queryCaptor = ArgumentCaptor.forClass(SelectQuery.class);
            String columnFamily = "columnFamily";
            Stream<CommunicationEntity> entities = select().from(columnFamily).getResult(manager);
            entities.toList();
            Mockito.verify(manager).select(queryCaptor.capture());
            checkQuery(queryCaptor, columnFamily);
        }

        @DisplayName("Should Execute Single Result Manager")
        @Test
        void shouldExecuteSingleResultManager() {
            DatabaseManager manager = Mockito.mock(DatabaseManager.class);
            ArgumentCaptor<SelectQuery> queryCaptor = ArgumentCaptor.forClass(SelectQuery.class);
            String columnFamily = "columnFamily";
            Optional<CommunicationEntity> entities = select().from(columnFamily).getSingleResult(manager);
            Mockito.verify(manager).singleResult(queryCaptor.capture());
            checkQuery(queryCaptor, columnFamily);
        }

        @DisplayName("Should Select Fields")
        @Test
        void shouldSelectFields() {
            var query = new DefaultQueryBuilder().select("name", "age").from("person").build();
            SoftAssertions.assertSoftly(soft-> {
                soft.assertThat(query.columns()).contains("name", "age");
                soft.assertThat(query.condition()).isEmpty();
                soft.assertThat(query.name()).isEqualTo("person");
            });
        }

        @DisplayName("Should Select Sorts")
        @Test
        void shouldSelectSorts() {
            var query = new DefaultQueryBuilder().select("name", "age")
                    .from("person")
                    .sort(Sort.asc("name"), Sort.asc("age")).build();
            SoftAssertions.assertSoftly(soft-> {
                soft.assertThat(query.columns()).contains("name", "age");
                soft.assertThat(query.condition()).isEmpty();
                soft.assertThat(query.name()).isEqualTo("person");
                soft.assertThat(query.isCount()).isFalse();
                soft.assertThat(query.sorts()).contains(Sort.asc("name"), Sort.asc("age"));
            });
        }

        @DisplayName("Should To String")
        @Test
        void shouldToString() {
            var builder = new DefaultQueryBuilder().select("name", "age")
                    .from("person");
            assertThat(builder.toString()).isNotNull().isNotBlank();
        }

        @DisplayName("Should Hash Code")
        @Test
        void shouldHashCode() {
            var builder = new DefaultQueryBuilder().select("name", "age")
                    .from("person");
            assertThat(builder.hashCode()).isNotZero();
        }

        @DisplayName("Should Equals")
        @Test
        void shouldEquals() {
            var builder = new DefaultQueryBuilder().select("name", "age")
                    .from("person");
            var builder2 = new DefaultQueryBuilder().select("name", "age")
                    .from("person");
            var builder3 = new DefaultQueryBuilder().select("name", "age")
                    .from("animal");

            SoftAssertions.assertSoftly(soft-> {
               soft.assertThat(builder).isEqualTo(builder2);
               soft.assertThat(builder).isNotEqualTo(builder3);
               soft.assertThat(builder2).isEqualTo(builder);
               soft.assertThat(builder).isEqualTo(builder2);
               soft.assertThat(builder).isEqualTo(builder);
               soft.assertThat(builder).isNotEqualTo(null);
               soft.assertThat(builder).isNotEqualTo(new Object());
               soft.assertThat(builder).isEqualTo(builder);
            });
        }

        @DisplayName("Should Return Error When Entity Is Null")
        @Test
        void shouldReturnErrorWhenEntityIsNull() {
            assertThatThrownBy(() -> select().from(null)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new DefaultQueryBuilder().build()).isInstanceOf(IllegalArgumentException.class);
        }

        private void checkQuery(ArgumentCaptor<SelectQuery> queryCaptor, String columnFamily) {
            SelectQuery query = queryCaptor.getValue();
            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.condition().isPresent()).isFalse();
            assertThat(query.name()).isEqualTo(columnFamily);
        }
    }

}
