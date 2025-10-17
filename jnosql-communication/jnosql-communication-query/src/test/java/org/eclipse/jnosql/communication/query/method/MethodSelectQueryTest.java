/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.method;

import jakarta.data.Sort;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.eclipse.jnosql.communication.query.Where;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;

@ExtendWith(SoftAssertionsExtension.class)
class MethodSelectQueryTest {


    @Test
    @DisplayName("should return correct values from getters and methods")
    void shouldReturnExpectedValues() {
        Where where = mock(Where.class);
        Sort<String> sort = mock(Sort.class);
        List<Sort<?>> sorts = Collections.singletonList(sort);

        MethodSelectQuery query = new MethodSelectQuery("Person", sorts, where, 10, true);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.fields()).isEmpty();
            soft.assertThat(query.entity()).isEqualTo("Person");
            soft.assertThat(query.where()).isPresent();
            soft.assertThat(query.where().get()).isEqualTo(where);
            soft.assertThat(query.limit()).isEqualTo(10);
            soft.assertThat(query.skip()).isZero();
            soft.assertThat(query.isCount()).isTrue();
            soft.assertThat(query.orderBy()).containsExactly(sort);
            soft.assertThatThrownBy(() -> query.orderBy().add(sort))
                    .isInstanceOf(UnsupportedOperationException.class);
            soft.assertThat(query.toString())
                    .contains("Person")
                    .contains("where")
                    .contains("limit 10")
                    .contains("count true");
        });
    }

    @Test
    @DisplayName("should handle null where and empty sorts correctly")
    void shouldHandleNullWhereAndEmptySorts() {
        MethodSelectQuery query = new MethodSelectQuery("City", Collections.emptyList(), null, 5, false);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.where()).isEmpty();
            soft.assertThat(query.limit()).isEqualTo(5);
            soft.assertThat(query.isCount()).isFalse();
            soft.assertThat(query.orderBy()).isEmpty();
            soft.assertThat(query.toString())
                    .contains("City")
                    .contains("limit 5")
                    .contains("count false");
        });
    }

    @Test
    @DisplayName("should test equals and hashCode for equality and difference")
    void shouldTestEqualsAndHashCode() {
        Where where = mock(Where.class);
        List<Sort<?>> sorts = Collections.singletonList(mock(Sort.class));

        MethodSelectQuery query1 = new MethodSelectQuery("Entity", sorts, where, 1, false);
        MethodSelectQuery query2 = new MethodSelectQuery("Entity", sorts, where, 1, false);
        MethodSelectQuery differentEntity = new MethodSelectQuery("Other", sorts, where, 1, false);
        MethodSelectQuery differentWhere = new MethodSelectQuery("Entity", sorts, mock(Where.class), 1, false);
        MethodSelectQuery differentSort = new MethodSelectQuery("Entity", Collections.emptyList(), where, 1, false);
        MethodSelectQuery differentLimit = new MethodSelectQuery("Entity", sorts, where, 2, false);
        MethodSelectQuery differentCount = new MethodSelectQuery("Entity", sorts, where, 1, true);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query1).isEqualTo(query1);
            soft.assertThat(query1).isEqualTo(query2);
            soft.assertThat(query1.hashCode()).isEqualTo(query2.hashCode());
            soft.assertThat(query1).isNotEqualTo(differentEntity);
            soft.assertThat(query1).isNotEqualTo(differentWhere);
            soft.assertThat(query1).isNotEqualTo(differentSort);
            soft.assertThat(query1).isNotEqualTo(differentLimit);
            soft.assertThat(query1).isNotEqualTo(differentCount);
            soft.assertThat(query1).isNotEqualTo(null);
            soft.assertThat(query1).isNotEqualTo("String");
        });
    }
}