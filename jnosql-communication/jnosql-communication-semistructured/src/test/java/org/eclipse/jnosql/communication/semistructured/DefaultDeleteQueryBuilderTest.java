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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.eq;
import static org.eclipse.jnosql.communication.semistructured.DeleteQuery.delete;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

class DefaultDeleteQueryBuilderTest {

    @Test
    void shouldReturnErrorWhenHasNullElementInSelect() {
        Assertions.assertThrows(NullPointerException.class, () -> delete("column", "column", null));
    }

    @Test
    void shouldDelete() {
        String columnFamily = "columnFamily";
        DeleteQuery query = delete().from(columnFamily).build();
        assertTrue(query.columns().isEmpty());
        assertFalse(query.condition().isPresent());
        assertEquals(columnFamily, query.name());
    }

    @Test
    void shouldDeleteColumns() {
        String columnFamily = "columnFamily";
        DeleteQuery query = delete("column", "column2").from(columnFamily).build();
        assertThat(query.columns()).contains("column", "column2");
        assertFalse(query.condition().isPresent());
        assertEquals(columnFamily, query.name());
    }


    @Test
    void shouldReturnErrorWhenFromIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> delete().from(null));
    }


    @Test
    void shouldSelectWhereNameEq() {
        String columnFamily = "columnFamily";
        String name = "Ada Lovelace";
        DeleteQuery query = delete().from(columnFamily).where("name").eq(name).build();
        CriteriaCondition condition = query.condition().get();

        Element element = condition.element();

        assertTrue(query.columns().isEmpty());
        assertEquals(columnFamily, query.name());
        assertEquals(Condition.EQUALS, condition.condition());
        assertEquals("name", element.name());
        assertEquals(name, element.get());

    }

    @Test
    void shouldSelectWhereNameLike() {
        String columnFamily = "columnFamily";
        String name = "Ada Lovelace";
        DeleteQuery query = delete().from(columnFamily).where("name").like(name).build();
        CriteriaCondition condition = query.condition().get();

        Element element = condition.element();

        assertTrue(query.columns().isEmpty());
        assertEquals(columnFamily, query.name());
        assertEquals(Condition.LIKE, condition.condition());
        assertEquals("name", element.name());
        assertEquals(name, element.get());
    }

    @Test
    void shouldSelectWhereNameGt() {
        String columnFamily = "columnFamily";
        Number value = 10;
        DeleteQuery query = delete().from(columnFamily).where("name").gt(value).build();
        CriteriaCondition condition = query.condition().get();

        Element element = condition.element();

        assertTrue(query.columns().isEmpty());
        assertEquals(columnFamily, query.name());
        assertEquals(Condition.GREATER_THAN, condition.condition());
        assertEquals("name", element.name());
        assertEquals(value, element.get());
    }

    @Test
    void shouldSelectWhereNameGte() {
        String columnFamily = "columnFamily";
        Number value = 10;
        DeleteQuery query = delete().from(columnFamily).where("name").gte(value).build();
        CriteriaCondition condition = query.condition().get();

        Element element = condition.element();

        assertTrue(query.columns().isEmpty());
        assertEquals(columnFamily, query.name());
        assertEquals(Condition.GREATER_EQUALS_THAN, condition.condition());
        assertEquals("name", element.name());
        assertEquals(value, element.get());
    }

    @Test
    void shouldSelectWhereNameLt() {
        String columnFamily = "columnFamily";
        Number value = 10;
        DeleteQuery query = delete().from(columnFamily).where("name").lt(value).build();
        CriteriaCondition criteriaCondition = query.condition().get();

        Element element = criteriaCondition.element();

        assertTrue(query.columns().isEmpty());
        assertEquals(columnFamily, query.name());
        assertEquals(Condition.LESSER_THAN, criteriaCondition.condition());
        assertEquals("name", element.name());
        assertEquals(value, element.get());
    }

    @Test
    void shouldSelectWhereNameLte() {
        String columnFamily = "columnFamily";
        Number value = 10;
        DeleteQuery query = delete().from(columnFamily).where("name").lte(value).build();
        CriteriaCondition criteriaCondition = query.condition().get();

        Element element = criteriaCondition.element();

        assertTrue(query.columns().isEmpty());
        assertEquals(columnFamily, query.name());
        assertEquals(Condition.LESSER_EQUALS_THAN, criteriaCondition.condition());
        assertEquals("name", element.name());
        assertEquals(value, element.get());
    }

    @Test
    void shouldSelectWhereNameBetween() {
        String columnFamily = "columnFamily";
        Number valueA = 10;
        Number valueB = 20;
        DeleteQuery query = delete().from(columnFamily).where("name").between(valueA, valueB).build();
        CriteriaCondition condition = query.condition().get();

        Element element = condition.element();

        assertTrue(query.columns().isEmpty());
        assertEquals(columnFamily, query.name());
        assertEquals(Condition.BETWEEN, condition.condition());
        assertEquals("name", element.name());
        assertThat(element.get(new TypeReference<List<Number>>() {
        })).contains(10, 20);
    }

    @Test
    void shouldSelectWhereNameIn() {
        String columnFamily = "columnFamily";
        Number valueA = 10;
        Number valueB = 20;
        DeleteQuery query = delete().from(columnFamily).where("name").in(List.of(valueA, valueB)).build();
        CriteriaCondition condition = query.condition().get();

        Element element = condition.element();

        assertTrue(query.columns().isEmpty());
        assertEquals(columnFamily, query.name());
        assertEquals(Condition.IN, condition.condition());
        assertEquals("name", element.name());
        assertThat(element.get(new TypeReference<List<Number>>() {
        })).contains(10, 20);
    }

    @Test
    void shouldSelectWhereNameNot() {
        String columnFamily = "columnFamily";
        String name = "Ada Lovelace";
        DeleteQuery query = delete().from(columnFamily).where("name").not().eq(name).build();
        CriteriaCondition condition = query.condition().get();

        Element element = condition.element();
        CriteriaCondition negate = element.get(CriteriaCondition.class);
        assertTrue(query.columns().isEmpty());
        assertEquals(columnFamily, query.name());
        assertEquals(Condition.NOT, condition.condition());
        assertEquals(Condition.EQUALS, negate.condition());
        assertEquals("name", negate.element().name());
        assertEquals(name, negate.element().get());
    }


    @Test
    void shouldSelectWhereNameAnd() {
        String columnFamily = "columnFamily";
        String name = "Ada Lovelace";
        DeleteQuery query = delete().from(columnFamily).where("name").eq(name).and("age").gt(10).build();
        CriteriaCondition condition = query.condition().get();

        Element element = condition.element();
        List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
        });
        assertEquals(Condition.AND, condition.condition());
        org.assertj.core.api.Assertions.assertThat(conditions).contains(eq(Element.of("name", name)),
                CriteriaCondition.gt(Element.of("age", 10)));
    }

    @Test
    void shouldSelectWhereNameOr() {
        String columnFamily = "columnFamily";
        String name = "Ada Lovelace";
        DeleteQuery query = delete().from(columnFamily).where("name").eq(name).or("age").gt(10).build();
        CriteriaCondition condition = query.condition().get();

        Element element = condition.element();
        List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
        });
        assertEquals(Condition.OR, condition.condition());
        org.assertj.core.api.Assertions.assertThat(conditions).contains(eq(Element.of("name", name)),
                CriteriaCondition.gt(Element.of("age", 10)));
    }

    @Test
    void shouldDeleteNegate() {
        String columnFamily = "columnFamily";
        DeleteQuery query = delete().from(columnFamily).where("city").not().eq("Assis")
                .and("name").not().eq("Lucas").build();

        CriteriaCondition condition = query.condition().orElseThrow(RuntimeException::new);
        assertEquals(columnFamily, query.name());
        Element element = condition.element();
        List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
        });

        assertEquals(Condition.AND, condition.condition());
        org.assertj.core.api.Assertions.assertThat(conditions).contains(eq(Element.of("city", "Assis")).negate(),
                eq(Element.of("name", "Lucas")).negate());


    }

    @Test
    void shouldExecuteDelete() {
        String columnFamily = "columnFamily";
        DatabaseManager manager = Mockito.mock(DatabaseManager.class);
        ArgumentCaptor<DeleteQuery> queryCaptor = ArgumentCaptor.forClass(DeleteQuery.class);
        delete().from(columnFamily).delete(manager);
        verify(manager).delete(queryCaptor.capture());

        DeleteQuery query = queryCaptor.getValue();
        assertTrue(query.columns().isEmpty());
        assertFalse(query.condition().isPresent());
        assertEquals(columnFamily, query.name());
    }

}
