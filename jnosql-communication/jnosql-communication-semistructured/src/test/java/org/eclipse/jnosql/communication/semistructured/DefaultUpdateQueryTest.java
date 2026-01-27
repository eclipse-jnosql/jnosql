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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;


class DefaultUpdateQueryTest {


    @Test
    void shouldConvertQuery(){
        UpdateQuery updateQuery = new DefaultUpdateQuery("person", List.of(Element.of("name", "Ada")),
                CriteriaCondition.eq(Element.of("age", 10)));

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(updateQuery.where()).isPresent();
            soft.assertThat(updateQuery.name()).isEqualTo("person");
            soft.assertThat(updateQuery.set()).hasSize(1)
                    .contains(Element.of("name", "Ada"));

            soft.assertThat(updateQuery.where().orElseThrow())
                    .isEqualTo(CriteriaCondition.eq(Element.of("age", 10)));
        });
    }

    @Test
    void shouldReturnSelectQuery(){
        UpdateQuery updateQuery = new DefaultUpdateQuery("person", List.of(Element.of("name", "Ada")),
                CriteriaCondition.eq(Element.of("age", 10)));

        var selectQuery = updateQuery.toSelectQuery();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(selectQuery.columns()).isEmpty();
            soft.assertThat(selectQuery.name()).isEqualTo("person");
            soft.assertThat(selectQuery.sorts()).isEmpty();
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            var where = selectQuery.condition().orElseThrow();
            soft.assertThat(where).isEqualTo(CriteriaCondition.eq(Element.of("age", 10)));
        });
    }
}