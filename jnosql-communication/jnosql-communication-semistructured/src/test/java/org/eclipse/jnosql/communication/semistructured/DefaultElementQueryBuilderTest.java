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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.eq;
import static org.eclipse.jnosql.communication.semistructured.SelectQuery.builder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultElementQueryBuilderTest {

    @Test
    void shouldReturnErrorWhenHasNullElementInSelect() {
        assertThrows(NullPointerException.class, () -> builder("document", "document'", null));
    }

    @Test
    void shouldBuilder() {
        String documentCollection = "documentCollection";
        SelectQuery query = builder().from(documentCollection).build();
        assertTrue(query.columns().isEmpty());
        assertFalse(query.condition().isPresent());
        assertEquals(documentCollection, query.name());
    }

    @Test
    void shouldSelectDocument() {
        String documentCollection = "documentCollection";
        SelectQuery query = builder("document", "document2").from(documentCollection).build();
        assertThat(query.columns()).contains("document", "document2");
        assertFalse(query.condition().isPresent());
        assertEquals(documentCollection, query.name());
    }

    @Test
    void shouldReturnErrorWhenFromIsNull() {
        assertThrows(NullPointerException.class, () -> builder().from(null));
    }


    @Test
    void shouldSelectOrderAsc() {
        String documentCollection = "documentCollection";
        SelectQuery query = builder().from(documentCollection).sort(Sort.asc("name")).build();
        assertTrue(query.columns().isEmpty());
        assertFalse(query.condition().isPresent());
        assertEquals(documentCollection, query.name());
        assertThat(query.sorts()).contains(Sort.asc("name"));
    }

    @Test
    void shouldSelectOrderDesc() {
        String documentCollection = "documentCollection";
        SelectQuery query = builder().from(documentCollection).sort(Sort.desc("name")).build();
        assertTrue(query.columns().isEmpty());
        assertFalse(query.condition().isPresent());
        assertEquals(documentCollection, query.name());
        assertThat(query.sorts()).contains(Sort.desc("name"));
    }


    @Test
    void shouldReturnErrorSelectWhenOrderIsNull() {
        assertThrows(NullPointerException.class,() -> {
            String documentCollection = "documentCollection";
            builder().from(documentCollection).sort((Sort<?>) null);
        });
    }

    @Test
    void shouldSelectLimit() {
        String documentCollection = "documentCollection";
        SelectQuery query = builder().from(documentCollection).limit(10).build();
        assertTrue(query.columns().isEmpty());
        assertFalse(query.condition().isPresent());
        assertEquals(documentCollection, query.name());
        assertEquals(10L, query.limit());
    }

    @Test
    void shouldReturnErrorWhenLimitIsNegative() {
        String documentCollection = "documentCollection";
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder().from(documentCollection).limit(-1));
    }

    @Test
    void shouldSelectSkip() {
        String documentCollection = "documentCollection";
        SelectQuery query = builder().from(documentCollection).skip(10).build();
        assertTrue(query.columns().isEmpty());
        assertFalse(query.condition().isPresent());
        assertEquals(documentCollection, query.name());
        assertEquals(10L, query.skip());
    }

    @Test
    void shouldReturnErrorWhenSkipIsNegative() {
        String documentCollection = "documentCollection";
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder().from(documentCollection).skip(-1));
    }

    @Test
    void shouldSelectWhereNameEq() {
        String documentCollection = "documentCollection";
        String name = "Ada Lovelace";

        SelectQuery query = builder().from(documentCollection)
                .where(eq("name", name))
                .build();
        CriteriaCondition condition = query.condition().get();

        Element element = condition.element();

        assertTrue(query.columns().isEmpty());
        assertEquals(documentCollection, query.name());
        assertEquals(Condition.EQUALS, condition.condition());
        assertEquals("name", element.name());
        assertEquals(name, element.get());

    }

    @Test
    void shouldSelectWhereNameLike() {
        String documentCollection = "documentCollection";
        String name = "Ada Lovelace";
        SelectQuery query = builder().from(documentCollection)
                .where(CriteriaCondition.like("name", name))
                .build();
        CriteriaCondition condition = query.condition().get();

        Element element = condition.element();

        assertTrue(query.columns().isEmpty());
        assertEquals(documentCollection, query.name());
        assertEquals(Condition.LIKE, condition.condition());
        assertEquals("name", element.name());
        assertEquals(name, element.get());
    }

    @Test
    void shouldSelectWhereNameGt() {
        String documentCollection = "documentCollection";
        Number value = 10;

        SelectQuery query = builder().from(documentCollection).where(CriteriaCondition.gt("name", 10))
                .build();

        CriteriaCondition condition = query.condition().get();

        Element element = condition.element();

        assertTrue(query.columns().isEmpty());
        assertEquals(documentCollection, query.name());
        assertEquals(Condition.GREATER_THAN, condition.condition());
        assertEquals("name", element.name());
        assertEquals(value, element.get());
    }

    @Test
    void shouldSelectWhereNameGte() {
        String documentCollection = "documentCollection";
        Number value = 10;
        CriteriaCondition gteName = CriteriaCondition.gte("name", value);
        SelectQuery query = builder().from(documentCollection).where(gteName).build();
        CriteriaCondition condition = query.condition().get();

        Element element = condition.element();

        assertTrue(query.columns().isEmpty());
        assertEquals(documentCollection, query.name());
        assertEquals(Condition.GREATER_EQUALS_THAN, condition.condition());
        assertEquals("name", element.name());
        assertEquals(value, element.get());
    }

    @Test
    void shouldSelectWhereNameLt() {
        String documentCollection = "documentCollection";
        Number value = 10;

        SelectQuery query = builder().from(documentCollection).where(CriteriaCondition.lt("name", value))
                .build();
        CriteriaCondition condition = query.condition().get();

        Element element = condition.element();

        assertTrue(query.columns().isEmpty());
        assertEquals(documentCollection, query.name());
        assertEquals(Condition.LESSER_THAN, condition.condition());
        assertEquals("name", element.name());
        assertEquals(value, element.get());
    }

    @Test
    void shouldSelectWhereNameLte() {
        String documentCollection = "documentCollection";
        Number value = 10;
        SelectQuery query = builder().from(documentCollection).where(CriteriaCondition.lte("name", value))
                .build();
        CriteriaCondition condition = query.condition().get();

        Element element = condition.element();

        assertTrue(query.columns().isEmpty());
        assertEquals(documentCollection, query.name());
        assertEquals(Condition.LESSER_EQUALS_THAN, condition.condition());
        assertEquals("name", element.name());
        assertEquals(value, element.get());
    }

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

        assertTrue(query.columns().isEmpty());
        assertEquals(documentCollection, query.name());
        assertEquals(Condition.BETWEEN, condition.condition());
        assertEquals("name", element.name());
        assertThat(element.get(new TypeReference<List<Number>>() {
        })).contains(10, 20);
    }

    @Test
    void shouldSelectWhereNameNot() {
        String documentCollection = "documentCollection";
        String name = "Ada Lovelace";

        SelectQuery query = builder().from(documentCollection).where(eq("name", name).negate())
                .build();
        CriteriaCondition condition = query.condition().get();

        Element element = condition.element();
        CriteriaCondition negate = element.get(CriteriaCondition.class);
        assertTrue(query.columns().isEmpty());
        assertEquals(documentCollection, query.name());
        assertEquals(Condition.NOT, condition.condition());
        assertEquals(Condition.EQUALS, negate.condition());
        assertEquals("name", negate.element().name());
        assertEquals(name, negate.element().get());
    }


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
        assertEquals(Condition.AND, condition.condition());
        org.assertj.core.api.Assertions.assertThat(conditions).contains(eq(Element.of("name", name)),
                CriteriaCondition.gt(Element.of("age", 10)));
    }

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
        assertEquals(Condition.OR, condition.condition());
        org.assertj.core.api.Assertions.assertThat(conditions).contains(eq(Element.of("name", name)),
                CriteriaCondition.gt(Element.of("age", 10)));
    }


    @Test
    void shouldSelectNegate() {
        String columnFamily = "columnFamily";
        CriteriaCondition nameNotEqualsLucas = eq("name", "Lucas").negate();
        SelectQuery query = builder().from(columnFamily)
                .where(nameNotEqualsLucas).build();

        CriteriaCondition condition = query.condition().orElseThrow(RuntimeException::new);
        assertEquals(columnFamily, query.name());
        Element element = condition.element();
        List<CriteriaCondition> conditions = element.get(new TypeReference<>() {
        });

        assertEquals(Condition.NOT, condition.condition());
        org.assertj.core.api.Assertions.assertThat(conditions).contains(eq(Element.of("name", "Lucas")));

    }


    @Test
    void shouldExecuteManager() {
        DatabaseManager manager = Mockito.mock(DatabaseManager.class);
        ArgumentCaptor<SelectQuery> queryCaptor = ArgumentCaptor.forClass(SelectQuery.class);
        String collection = "collection";
        Stream<CommunicationEntity> entities = builder().from(collection).getResult(manager);
        Mockito.verify(manager).select(queryCaptor.capture());
        checkQuery(queryCaptor, collection);
    }

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
        assertTrue(query.columns().isEmpty());
        assertFalse(query.condition().isPresent());
        assertEquals(collection, query.name());
    }
}