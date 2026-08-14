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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.eclipse.jnosql.communication.semistructured.SelectQuery.select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class DefaultElementQueryTest {


    @Nested
    @DisplayName("When the default element query is used")
    class WhenTheDefaultElementQueryIsUsed {

        private SelectQuery query;

        @BeforeEach
        public void setUp() {
            query = select().from("entity").build();
        }

        @DisplayName("Should Not Remove Columns")
        @Test
        void shouldNotRemoveColumns() {
            assertThatThrownBy(() -> {
                List<String> columns = query.columns();
                assertThat(columns).isEmpty();
                columns.clear();
            }).isInstanceOf(UnsupportedOperationException.class);
        }

        @DisplayName("Should Not Remove Sort")
        @Test
        void shouldNotRemoveSort() {
            assertThatThrownBy(() -> {
                List<Sort<?>> sorts = query.sorts();
                assertThat(sorts).isEmpty();
                sorts.clear();
            }).isInstanceOf(UnsupportedOperationException.class);
        }

        @DisplayName("Should Convert County By")
        @Test
        void shouldConvertCountyBy() {
            SelectQuery query = SelectQuery.select().from("entity")
                    .where("name").eq("predicate")
                    .orderBy("name").asc().build();

            SelectQuery countQuery = DefaultSelectQuery.countBy(query);
            assertThat(countQuery).isNotNull();
            assertThat(countQuery.name()).isEqualTo("entity");
            assertThat(countQuery.limit()).isEqualTo(0);
            assertThat(countQuery.skip()).isEqualTo(0);
            assertThat(countQuery.sorts().isEmpty()).isTrue();
           CriteriaCondition condition = countQuery.condition().orElseThrow();
           assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
        }

        @DisplayName("Should Convert Exists By")
        @Test
        void shouldConvertExistsBy() {
            SelectQuery query = SelectQuery.select().from("entity")
                    .where("name").eq("predicate")
                    .orderBy("name").asc().build();

            SelectQuery countQuery = DefaultSelectQuery.existsBy(query);
            assertThat(countQuery).isNotNull();
            assertThat(countQuery.name()).isEqualTo("entity");
            assertThat(countQuery.limit()).isEqualTo(1);
            assertThat(countQuery.skip()).isEqualTo(0);
            assertThat(countQuery.sorts().isEmpty()).isTrue();
            CriteriaCondition condition = countQuery.condition().orElseThrow();
            assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
        }

        @DisplayName("Should Has Code")
        @Test
        void shouldHasCode(){
            SelectQuery query = SelectQuery.select().from("entity")
                    .where("name").eq("predicate")
                    .orderBy("name").asc().build();
            SelectQuery query2 = SelectQuery.select().from("entity")
                    .where("name").eq("predicate")
                    .orderBy("name").asc().build();

            assertThat(query2.hashCode()).isEqualTo(query.hashCode());
        }

        @DisplayName("Should Equals")
        @Test
        void shouldEquals(){
            SelectQuery query = SelectQuery.select().from("entity")
                    .where("name").eq("predicate")
                    .orderBy("name").asc().build();
            SelectQuery query2 = SelectQuery.select().from("entity")
                    .where("name").eq("predicate")
                    .orderBy("name").asc().build();

            assertThat(query2).isEqualTo(query);
            assertThat(query).isEqualTo(query);
            assertThat(query).isNotEqualTo("query");
        }
    }

}
