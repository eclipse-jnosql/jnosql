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
import org.eclipse.jnosql.communication.query.BooleanQueryValue;
import org.eclipse.jnosql.communication.query.ConditionQueryValue;
import org.eclipse.jnosql.communication.query.NumberQueryValue;
import org.eclipse.jnosql.communication.query.StringQueryValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DeleteJakartaDataQueryProviderNotConditionTest {


    private DeleteProvider deleteProvider;

    @BeforeEach
    void setUp() {
        deleteProvider = new DeleteProvider();
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE NOT age = 10"})
    void shouldEq(String query){
        var deleteQuery = deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            soft.assertThat(condition.name()).isEqualTo("_NOT");
            var notCondition = (ConditionQueryValue) condition.value();
            var queryCondition = notCondition.get().get(0);
            soft.assertThat(queryCondition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(queryCondition.name()).isEqualTo("age");
            soft.assertThat(queryCondition.value()).isEqualTo(NumberQueryValue.of(10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE NOT salary = 10.15"})
    void shouldEqDouble(String query){
        var deleteQuery = deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            soft.assertThat(condition.name()).isEqualTo("_NOT");
            var notCondition = (ConditionQueryValue) condition.value();
            var queryCondition = notCondition.get().get(0);
            soft.assertThat(queryCondition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(queryCondition.name()).isEqualTo("salary");
            soft.assertThat(queryCondition.value()).isEqualTo(NumberQueryValue.of(10.15));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE NOT name = \"Otavio\""})
    void shouldEqString(String query){
        var deleteQuery = deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            soft.assertThat(condition.name()).isEqualTo("_NOT");
            var notCondition = (ConditionQueryValue) condition.value();
            var queryCondition = notCondition.get().get(0);
            soft.assertThat(queryCondition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(queryCondition.name()).isEqualTo("name");
            soft.assertThat(queryCondition.value()).isEqualTo(StringQueryValue.of("Otavio"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE NOT name = 'Otavio'"})
    void shouldEqStringSingleQuote(String query){
        var deleteQuery = deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            soft.assertThat(condition.name()).isEqualTo("_NOT");
            var notCondition = (ConditionQueryValue) condition.value();
            var queryCondition = notCondition.get().get(0);
            soft.assertThat(queryCondition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(queryCondition.name()).isEqualTo("name");
            soft.assertThat(queryCondition.value()).isEqualTo(StringQueryValue.of("Otavio"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE NOT name = :name"})
    void shouldEQQueryWithCondition(String query){
        var deleteQuery = deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            soft.assertThat(condition.name()).isEqualTo("_NOT");
            var notCondition = (ConditionQueryValue) condition.value();
            var queryCondition = notCondition.get().get(0);
            soft.assertThat(queryCondition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(queryCondition.name()).isEqualTo("name");
            soft.assertThat(queryCondition.value()).isEqualTo(DefaultQueryValue.of("name"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE NOT name = ?1"})
    void shouldEQQueryWithConditionPosition(String query){
        var deleteQuery = deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            soft.assertThat(condition.name()).isEqualTo("_NOT");
            var notCondition = (ConditionQueryValue) condition.value();
            var queryCondition = notCondition.get().get(0);
            soft.assertThat(queryCondition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(queryCondition.name()).isEqualTo("name");
            soft.assertThat(queryCondition.value()).isEqualTo(DefaultQueryValue.of("?1"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE NOT active = TRUE"})
    void shouldUseSpecialExpressionTrue(String query){
        var deleteQuery = deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            soft.assertThat(condition.name()).isEqualTo("_NOT");
            var notCondition = (ConditionQueryValue) condition.value();
            var queryCondition = notCondition.get().get(0);
            soft.assertThat(queryCondition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(queryCondition.name()).isEqualTo("active");
            soft.assertThat(queryCondition.value()).isEqualTo(BooleanQueryValue.TRUE);
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE NOT active = FALSE"})
    void shouldUseSpecialExpressionFalse(String query){
        var deleteQuery = deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            soft.assertThat(condition.name()).isEqualTo("_NOT");
            var notCondition = (ConditionQueryValue) condition.value();
            var queryCondition = notCondition.get().get(0);
            soft.assertThat(queryCondition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(queryCondition.name()).isEqualTo("active");
            soft.assertThat(queryCondition.value()).isEqualTo(BooleanQueryValue.FALSE);
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE NOT age < 10"})
    void shouldUseSpecialExpressionLesser(String query){
        var deleteQuery = deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            soft.assertThat(condition.name()).isEqualTo("_NOT");
            var notCondition = (ConditionQueryValue) condition.value();
            var queryCondition = notCondition.get().get(0);
            soft.assertThat(queryCondition.condition()).isEqualTo(Condition.LESSER_THAN);
            soft.assertThat(queryCondition.name()).isEqualTo("age");
            soft.assertThat(queryCondition.value()).isEqualTo(NumberQueryValue.of(10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity WHERE NOT age > 10"})
    void shouldUseSpecialExpressionGreater(String query){
        var deleteQuery = deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            soft.assertThat(condition.name()).isEqualTo("_NOT");
            var notCondition = (ConditionQueryValue) condition.value();
            var queryCondition = notCondition.get().get(0);
            soft.assertThat(queryCondition.condition()).isEqualTo(Condition.GREATER_THAN);
            soft.assertThat(queryCondition.name()).isEqualTo("age");
            soft.assertThat(queryCondition.value()).isEqualTo(NumberQueryValue.of(10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE NOT age <= 10")
    void shouldUseSpecialExpressionLesserThanEquals(String query){
        var deleteQuery = deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            soft.assertThat(condition.name()).isEqualTo("_NOT");
            var notCondition = (ConditionQueryValue) condition.value();
            var queryCondition = notCondition.get().get(0);
            soft.assertThat(queryCondition.condition()).isEqualTo(Condition.LESSER_EQUALS_THAN);
            soft.assertThat(queryCondition.name()).isEqualTo("age");
            soft.assertThat(queryCondition.value()).isEqualTo(NumberQueryValue.of(10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE NOT age >= 10")
    void shouldUseSpecialExpressionGreaterThanEquals(String query){
        var deleteQuery= deleteProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            soft.assertThat(condition.name()).isEqualTo("_NOT");
            var notCondition = (ConditionQueryValue) condition.value();
            var queryCondition = notCondition.get().get(0);
            soft.assertThat(queryCondition.condition()).isEqualTo(Condition.GREATER_EQUALS_THAN);
            soft.assertThat(queryCondition.name()).isEqualTo("age");
            soft.assertThat(queryCondition.value()).isEqualTo(NumberQueryValue.of(10));
        });
    }
}
