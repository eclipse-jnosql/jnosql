/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.data;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.query.ArrayQueryValue;
import org.eclipse.jnosql.communication.query.ConditionQueryValue;
import org.eclipse.jnosql.communication.query.EnumQueryValue;
import org.eclipse.jnosql.communication.query.NumberQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.StringQueryValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DayOfWeek;

class DeleteJakartaDataQueryProviderInTest {


    private DeleteProvider deleteProvider;

    @BeforeEach
    void setUp() {
        deleteProvider = new DeleteProvider();
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE age IN (10, 12.12, 'otavio', ?1, :param)"})
    void shouldIn(String query){
        var deleteQuery = deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            QueryValue<?> value = condition.value();
            soft.assertThat(condition.name()).isEqualTo("age");
            soft.assertThat(condition.condition()).isEqualTo(Condition.IN);
            soft.assertThat(value).isInstanceOf(ArrayQueryValue.class);
            soft.assertThat(ArrayQueryValue.class.cast(value).get()).hasSize(5)
                    .contains(NumberQueryValue.of(10), NumberQueryValue.of(12.12),
                            StringQueryValue.of("otavio"), DefaultQueryValue.of("?1"),
                            DefaultQueryValue.of("param"));

        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE days IN (java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.SUNDAY)"})
    void shouldInEnumLiteral(String query){
        var deleteQuery = deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            QueryValue<?> value = condition.value();
            soft.assertThat(condition.name()).isEqualTo("days");
            soft.assertThat(condition.condition()).isEqualTo(Condition.IN);
            soft.assertThat(value).isInstanceOf(ArrayQueryValue.class);
            soft.assertThat(ArrayQueryValue.class.cast(value).get()).hasSize(2)
                    .contains(EnumQueryValue.of(DayOfWeek.MONDAY), EnumQueryValue.of(DayOfWeek.SUNDAY));

        });
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE age NOT IN (10, 20)"})
    void shouldNegateBetween(String query){
        var deleteQuery = deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);

            var values = (ConditionQueryValue) condition.value();
            var conditions = values.get();
            soft.assertThat(conditions).hasSize(1);
            soft.assertThat(conditions.get(0).name()).isEqualTo("age");
            soft.assertThat(conditions.get(0).value()).isInstanceOf(ArrayQueryValue.class);
            soft.assertThat(ArrayQueryValue.class.cast(conditions.get(0).value()).get()).hasSize(2)
                    .contains(NumberQueryValue.of(10), NumberQueryValue.of(20));

        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE name = 'Otavio' AND age IN (10, 20)"})
    void shouldCombineAnd(String query){
        var deleteQuery = deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.AND);

            var values = (ConditionQueryValue) condition.value();
            var conditions = values.get();
            soft.assertThat(conditions).hasSize(2);

            QueryCondition equalsCondition = conditions.get(0);
            soft.assertThat(equalsCondition.name()).isEqualTo("name");
            soft.assertThat(equalsCondition.value()).isEqualTo(StringQueryValue.of("Otavio"));

            QueryCondition betweenCondition = conditions.get(1);
            soft.assertThat(betweenCondition.condition()).isEqualTo(Condition.IN);
            soft.assertThat(betweenCondition.value()).isInstanceOf(ArrayQueryValue.class);
            soft.assertThat(ArrayQueryValue.class.cast(betweenCondition.value()).get()).hasSize(2)
                    .contains(NumberQueryValue.of(10), NumberQueryValue.of(20));

        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE age IN (10, 20) AND name = 'Otavio'")
    void shouldCombineAnd2(String query){
        var deleteQuery = deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.AND);

            var values = (ConditionQueryValue) condition.value();
            var conditions = values.get();
            QueryCondition equalsCondition = conditions.get(1);
            soft.assertThat(equalsCondition.name()).isEqualTo("name");
            soft.assertThat(equalsCondition.value()).isEqualTo(StringQueryValue.of("Otavio"));

            QueryCondition betweenCondition = conditions.get(0);
            soft.assertThat(betweenCondition.condition()).isEqualTo(Condition.IN);
            soft.assertThat(betweenCondition.value()).isInstanceOf(ArrayQueryValue.class);
            soft.assertThat(ArrayQueryValue.class.cast(betweenCondition.value()).get()).hasSize(2)
                    .contains(NumberQueryValue.of(10), NumberQueryValue.of(20));
        });
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE  name = 'Otavio' OR name IN (10, 20)"})
    void shouldCombineOr(String query){
        var deleteQuery = deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.OR);

            var values = (ConditionQueryValue) condition.value();
            var conditions = values.get();
            QueryCondition equalsCondition = conditions.get(0);
            soft.assertThat(equalsCondition.name()).isEqualTo("name");
            soft.assertThat(equalsCondition.value()).isEqualTo(StringQueryValue.of("Otavio"));

            QueryCondition betweenCondition = conditions.get(1);
            soft.assertThat(betweenCondition.condition()).isEqualTo(Condition.IN);
            soft.assertThat(betweenCondition.value()).isInstanceOf(ArrayQueryValue.class);
            soft.assertThat(ArrayQueryValue.class.cast(betweenCondition.value()).get()).hasSize(2)
                    .contains(NumberQueryValue.of(10), NumberQueryValue.of(20));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE age IN (10, 20) OR name = 'Otavio'")
    void shouldCombineOr2(String query){
        var deleteQuery = deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.OR);

            var values = (ConditionQueryValue) condition.value();
            var conditions = values.get();
            QueryCondition equalsCondition = conditions.get(1);
            soft.assertThat(equalsCondition.name()).isEqualTo("name");
            soft.assertThat(equalsCondition.value()).isEqualTo(StringQueryValue.of("Otavio"));

            QueryCondition betweenCondition = conditions.get(0);
            soft.assertThat(betweenCondition.condition()).isEqualTo(Condition.IN);
            soft.assertThat(betweenCondition.value()).isInstanceOf(ArrayQueryValue.class);
            soft.assertThat(ArrayQueryValue.class.cast(betweenCondition.value()).get()).hasSize(2)
                    .contains(NumberQueryValue.of(10), NumberQueryValue.of(20));
        });
    }


}
