/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *  All rights reserved. This program OR the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  OR Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  OR the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.data;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.query.ConditionQueryValue;
import org.eclipse.jnosql.communication.query.NumberQueryValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DeleteJakartaDataQueryProviderOrTest {


    private DeleteParser deleteParser;

    @BeforeEach
    void setUp() {
        deleteParser = new DeleteParser();
    }



    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE age = 10 OR salary = 10.15")
    void shouldORTwoConditions(String query){
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.OR);

            var values = (ConditionQueryValue) condition.value();
            var conditions = values.get();
            soft.assertThat(conditions).hasSize(2);
            soft.assertThat(conditions.get(0).name()).isEqualTo("age");
            soft.assertThat(conditions.get(0).value()).isEqualTo(NumberQueryValue.of(10));

            soft.assertThat(conditions.get(1).name()).isEqualTo("salary");
            soft.assertThat(conditions.get(1).value()).isEqualTo(NumberQueryValue.of(10.15));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE age = 10 OR salary = 10.15 OR name =?1")
    void shouldORThreeConditions(String query){
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.OR);

            var values = (ConditionQueryValue) condition.value();
            var conditions = values.get();
            soft.assertThat(conditions).hasSize(3);
            soft.assertThat(conditions.get(0).name()).isEqualTo("age");
            soft.assertThat(conditions.get(0).value()).isEqualTo(NumberQueryValue.of(10));

            soft.assertThat(conditions.get(1).name()).isEqualTo("salary");
            soft.assertThat(conditions.get(1).value()).isEqualTo(NumberQueryValue.of(10.15));

            soft.assertThat(conditions.get(2).name()).isEqualTo("name");
            soft.assertThat(conditions.get(2).value()).isEqualTo(DefaultQueryValue.of("?1"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE age = 10 OR salary = 10.15 AND name =?1")
    void shouldORMixConditions(String query){
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.OR);

            var values = (ConditionQueryValue) condition.value();
            var conditions = values.get();
            soft.assertThat(conditions).hasSize(3);
            soft.assertThat(conditions.get(0).name()).isEqualTo("age");
            soft.assertThat(conditions.get(0).value()).isEqualTo(NumberQueryValue.of(10));

            soft.assertThat(conditions.get(1).name()).isEqualTo("salary");
            soft.assertThat(conditions.get(1).value()).isEqualTo(NumberQueryValue.of(10.15));

            condition = conditions.get(2);
            values = (ConditionQueryValue) condition.value();
            conditions = values.get();
            soft.assertThat(condition.condition()).isEqualTo(Condition.AND);
            soft.assertThat(conditions).hasSize(1);

            soft.assertThat(conditions.getFirst().name()).isEqualTo("name");
            soft.assertThat(conditions.getFirst().value()).isEqualTo(DefaultQueryValue.of("?1"));

        });
    }
}
