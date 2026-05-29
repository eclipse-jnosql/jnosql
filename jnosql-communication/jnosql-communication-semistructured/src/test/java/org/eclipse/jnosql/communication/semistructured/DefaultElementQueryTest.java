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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.eclipse.jnosql.communication.semistructured.SelectQuery.select;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class DefaultElementQueryTest {

    private SelectQuery query;

    @BeforeEach
    public void setUp() {
        query = select().from("entity").build();
    }

    @Test
    void shouldNotRemoveColumns() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            List<String> columns = query.columns();
            assertTrue(columns.isEmpty());
            columns.clear();
        });
    }

    @Test
    void shouldNotRemoveSort() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            List<Sort<?>> sorts = query.sorts();
            assertTrue(sorts.isEmpty());
            sorts.clear();
        });
    }

    @Test
    void shouldConvertCountyBy() {
        SelectQuery query = SelectQuery.select().from("entity")
                .where("name").eq("predicate")
                .orderBy("name").asc().build();

        SelectQuery countQuery = DefaultSelectQuery.countBy(query);
        Assertions.assertNotNull(countQuery);
        assertEquals("entity", countQuery.name());
        assertEquals(0, countQuery.limit());
        assertEquals(0, countQuery.skip());
        assertTrue(countQuery.sorts().isEmpty());
       CriteriaCondition condition = countQuery.condition().orElseThrow();
       Assertions.assertEquals(Condition.EQUALS, condition.condition());
    }

    @Test
    void shouldConvertExistsBy() {
        SelectQuery query = SelectQuery.select().from("entity")
                .where("name").eq("predicate")
                .orderBy("name").asc().build();

        SelectQuery countQuery = DefaultSelectQuery.existsBy(query);
        Assertions.assertNotNull(countQuery);
        assertEquals("entity", countQuery.name());
        assertEquals(1, countQuery.limit());
        assertEquals(0, countQuery.skip());
        assertTrue(countQuery.sorts().isEmpty());
        CriteriaCondition condition = countQuery.condition().orElseThrow();
        Assertions.assertEquals(Condition.EQUALS, condition.condition());
    }

    @Test
    void shouldHasCode(){
        SelectQuery query = SelectQuery.select().from("entity")
                .where("name").eq("predicate")
                .orderBy("name").asc().build();
        SelectQuery query2 = SelectQuery.select().from("entity")
                .where("name").eq("predicate")
                .orderBy("name").asc().build();

        Assertions.assertEquals(query.hashCode(), query2.hashCode());
    }

    @Test
    void shouldEquals(){
        SelectQuery query = SelectQuery.select().from("entity")
                .where("name").eq("predicate")
                .orderBy("name").asc().build();
        SelectQuery query2 = SelectQuery.select().from("entity")
                .where("name").eq("predicate")
                .orderBy("name").asc().build();

        Assertions.assertEquals(query, query2);
        Assertions.assertEquals(query, query);
        Assertions.assertNotEquals("query", query);
    }
}