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


import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.eq;
import static org.eclipse.jnosql.communication.semistructured.DeleteQuery.delete;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultDeleteQueryBuilderTest {


    @Nested
    @DisplayName("When the default delete query builder is used")
    class WhenTheDefaultDeleteQueryBuilderIsUsed {

        @DisplayName("Should Return Error When Has Null Element In Select")
        @Test
        void shouldReturnErrorWhenHasNullElementInSelect() {
            assertThatThrownBy(() -> delete("column", "column", null)).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Delete")
        @Test
        void shouldDelete() {
            String columnFamily = "columnFamily";
            DeleteQuery query = delete().from(columnFamily).build();
            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.condition().isPresent()).isFalse();
            assertThat(query.name()).isEqualTo(columnFamily);
        }

        @DisplayName("Should Delete Columns")
        @Test
        void shouldDeleteColumns() {
            String columnFamily = "columnFamily";
            DeleteQuery query = delete("column", "column2").from(columnFamily).build();
            assertThat(query.columns()).contains("column", "column2");
            assertThat(query.condition().isPresent()).isFalse();
            assertThat(query.name()).isEqualTo(columnFamily);
        }


        @DisplayName("Should Return Error When From Is Null")
        @Test
        void shouldReturnErrorWhenFromIsNull() {
            assertThatThrownBy(() -> delete().from(null)).isInstanceOf(NullPointerException.class);
        }


        @DisplayName("Should Select Where Name Eq")
        @Test
        void shouldSelectWhereNameEq() {
            String columnFamily = "columnFamily";
            String name = "Ada Lovelace";
            DeleteQuery query = delete().from(columnFamily).where("name").eq(name).build();
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
            DeleteQuery query = delete().from(columnFamily).where("name").like(name).build();
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
            DeleteQuery query = delete().from(columnFamily).where("name").gt(value).build();
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
            DeleteQuery query = delete().from(columnFamily).where("name").gte(value).build();
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
            DeleteQuery query = delete().from(columnFamily).where("name").lt(value).build();
            CriteriaCondition criteriaCondition = query.condition().get();

            Element element = criteriaCondition.element();

            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(columnFamily);
            assertThat(criteriaCondition.condition()).isEqualTo(Condition.LESSER_THAN);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get()).isEqualTo(value);
        }

        @DisplayName("Should Select Where Name Lte")
        @Test
        void shouldSelectWhereNameLte() {
            String columnFamily = "columnFamily";
            Number value = 10;
            DeleteQuery query = delete().from(columnFamily).where("name").lte(value).build();
            CriteriaCondition criteriaCondition = query.condition().get();

            Element element = criteriaCondition.element();

            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(columnFamily);
            assertThat(criteriaCondition.condition()).isEqualTo(Condition.LESSER_EQUALS_THAN);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get()).isEqualTo(value);
        }

        @DisplayName("Should Select Where Name Between")
        @Test
        void shouldSelectWhereNameBetween() {
            String columnFamily = "columnFamily";
            Number valueA = 10;
            Number valueB = 20;
            DeleteQuery query = delete().from(columnFamily).where("name").between(valueA, valueB).build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();

            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(columnFamily);
            assertThat(condition.condition()).isEqualTo(Condition.BETWEEN);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get(new TypeReference<List<Number>>() {
            })).contains(10, 20);
        }

        @DisplayName("Should Select Where Name In")
        @Test
        void shouldSelectWhereNameIn() {
            String columnFamily = "columnFamily";
            Number valueA = 10;
            Number valueB = 20;
            DeleteQuery query = delete().from(columnFamily).where("name").in(List.of(valueA, valueB)).build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();

            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.name()).isEqualTo(columnFamily);
            assertThat(condition.condition()).isEqualTo(Condition.IN);
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get(new TypeReference<List<Number>>() {
            })).contains(10, 20);
        }

        @DisplayName("Should Select Where Name Not")
        @Test
        void shouldSelectWhereNameNot() {
            String columnFamily = "columnFamily";
            String name = "Ada Lovelace";
            DeleteQuery query = delete().from(columnFamily).where("name").not().eq(name).build();
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
            DeleteQuery query = delete().from(columnFamily).where("name").eq(name).and("age").gt(10).build();
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
            DeleteQuery query = delete().from(columnFamily).where("name").eq(name).or("age").gt(10).build();
            CriteriaCondition condition = query.condition().get();

            Element element = condition.element();
            List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
            });
            assertThat(condition.condition()).isEqualTo(Condition.OR);
            assertThat(conditions).contains(eq(Element.of("name", name)),
                    CriteriaCondition.gt(Element.of("age", 10)));
        }

        @DisplayName("Should Delete Negate")
        @Test
        void shouldDeleteNegate() {
            String columnFamily = "columnFamily";
            DeleteQuery query = delete().from(columnFamily).where("city").not().eq("Assis")
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

        @DisplayName("Should Execute Delete")
        @Test
        void shouldExecuteDelete() {
            String columnFamily = "columnFamily";
            DatabaseManager manager = Mockito.mock(DatabaseManager.class);
            ArgumentCaptor<DeleteQuery> queryCaptor = ArgumentCaptor.forClass(DeleteQuery.class);
            delete().from(columnFamily).delete(manager);
            verify(manager).delete(queryCaptor.capture());

            DeleteQuery query = queryCaptor.getValue();
            assertThat(query.columns().isEmpty()).isTrue();
            assertThat(query.condition().isPresent()).isFalse();
            assertThat(query.name()).isEqualTo(columnFamily);
        }
    }

}
