/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.mapping.semistructured;

import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DefaultSelectQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SemistructureUpdateQueryTest {


    @Test
    @DisplayName("Should return condition when criteria condition is provided")
    void shouldReturnConditionWhenCriteriaConditionIsProvided() {
        CriteriaCondition condition = CriteriaCondition.eq("author", "Ada");

        SemistructureUpdateQuery query =
                new SemistructureUpdateQuery("Book", List.of(), condition);

        Optional<CriteriaCondition> result = query.condition();

        assertThat(result)
                .isPresent()
                .contains(condition);
    }

    @Test
    @DisplayName("Should return empty condition when criteria condition is null")
    void shouldReturnEmptyConditionWhenCriteriaConditionIsNull() {
        SemistructureUpdateQuery query =
                new SemistructureUpdateQuery("Book", List.of(), null);

        Optional<CriteriaCondition> result = query.condition();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should convert update query to select query preserving name and condition")
    void shouldConvertToSelectQueryWithSameNameAndCondition() {
        CriteriaCondition condition = CriteriaCondition.eq("author", "Ada");

        SemistructureUpdateQuery query =
                new SemistructureUpdateQuery("Book", List.of(), condition);

        SelectQuery selectQuery = query.toSelectQuery();

        assertThat(selectQuery)
                .isInstanceOf(DefaultSelectQuery.class);

        assertThat(selectQuery.name()).isEqualTo("Book");
        assertThat(selectQuery.condition())
                .isPresent()
                .contains(condition);
    }

    @Test
    @DisplayName("Should convert update query to select query without condition when condition is null")
    void shouldConvertToSelectQueryWithoutConditionWhenConditionIsNull() {
        SemistructureUpdateQuery query =
                new SemistructureUpdateQuery("Book", List.of(), null);

        SelectQuery selectQuery = query.toSelectQuery();

        assertThat(selectQuery.condition()).isEmpty();
    }
}