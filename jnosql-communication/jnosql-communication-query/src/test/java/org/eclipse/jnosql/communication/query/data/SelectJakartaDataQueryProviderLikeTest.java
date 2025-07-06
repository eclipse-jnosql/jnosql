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
import org.eclipse.jnosql.communication.query.ConditionQueryValue;
import org.eclipse.jnosql.communication.query.NumberQueryValue;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.SelectQuery;
import org.eclipse.jnosql.communication.query.StringQueryValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SelectJakartaDataQueryProviderLikeTest {


    private SelectProvider selectProvider;

    @BeforeEach
    void setUp() {
        selectProvider = new SelectProvider();
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE  name LIKE 'A%'", "FROM entity WHERE name LIKE 'A%'"})
    void shouldLike(String query){
        SelectQuery selectQuery = selectProvider.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            QueryValue<?> value = condition.value();
            soft.assertThat(condition.condition()).isEqualTo(Condition.LIKE);
            soft.assertThat(value).isEqualTo(StringQueryValue.of("A%"));

        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE  name LIKE \"A%\"", "FROM entity WHERE name LIKE \"A%\""})
    void shouldLikeDoubleQuote(String query){
        SelectQuery selectQuery = selectProvider.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            QueryValue<?> value = condition.value();
            soft.assertThat(condition.condition()).isEqualTo(Condition.LIKE);
            soft.assertThat(value).isEqualTo(StringQueryValue.of("A%"));

        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE  name NOT LIKE 'A%'", "FROM entity WHERE name NOT LIKE 'A%'"})
    void shouldNegateLike(String query){
        SelectQuery selectQuery = selectProvider.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);

            var values = (ConditionQueryValue) condition.value();
            var conditions = values.get();
            soft.assertThat(conditions).hasSize(1);
            soft.assertThat(conditions.get(0).name()).isEqualTo("name");
            soft.assertThat(conditions.get(0).value()).isEqualTo(StringQueryValue.of("A%"));

        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE  age = 10 AND name LIKE 'test'", "FROM entity WHERE  age = 10 AND name LIKE 'test'"})
    void shouldCombineAnd(String query){
        SelectQuery selectQuery = selectProvider.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.AND);

            var values = (ConditionQueryValue) condition.value();
            var conditions = values.get();
            soft.assertThat(conditions).hasSize(2);
            soft.assertThat(conditions.get(0).name()).isEqualTo("age");
            soft.assertThat(conditions.get(0).value()).isEqualTo(NumberQueryValue.of(10));

            soft.assertThat(conditions.get(1).name()).isEqualTo("name");
            soft.assertThat(conditions.get(1).value()).isEqualTo(StringQueryValue.of("test"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE name LIKE 'test' AND age = 10", "FROM entity WHERE name LIKE 'test' AND age = 10"})
    void shouldCombineAnd2(String query){
        SelectQuery selectQuery = selectProvider.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.AND);

            var values = (ConditionQueryValue) condition.value();
            var conditions = values.get();
            soft.assertThat(conditions).hasSize(2);
            soft.assertThat(conditions.get(1).name()).isEqualTo("age");
            soft.assertThat(conditions.get(1).value()).isEqualTo(NumberQueryValue.of(10));

            soft.assertThat(conditions.get(0).name()).isEqualTo("name");
            soft.assertThat(conditions.get(0).value()).isEqualTo(StringQueryValue.of("test"));
        });
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE  age = 10 OR name LIKE 'test'", "FROM entity WHERE  age = 10 OR name LIKE 'test'"})
    void shouldCombineOr(String query){
        SelectQuery selectQuery = selectProvider.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.OR);

            var values = (ConditionQueryValue) condition.value();
            var conditions = values.get();
            soft.assertThat(conditions).hasSize(2);
            soft.assertThat(conditions.get(0).name()).isEqualTo("age");
            soft.assertThat(conditions.get(0).value()).isEqualTo(NumberQueryValue.of(10));

            soft.assertThat(conditions.get(1).name()).isEqualTo("name");
            soft.assertThat(conditions.get(1).value()).isEqualTo(StringQueryValue.of("test"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE name LIKE 'test' OR age = 10", "FROM entity WHERE name LIKE 'test' OR age = 10"})
    void shouldCombineOr2(String query){
        SelectQuery selectQuery = selectProvider.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.OR);

            var values = (ConditionQueryValue) condition.value();
            var conditions = values.get();
            soft.assertThat(conditions).hasSize(2);
            soft.assertThat(conditions.get(1).name()).isEqualTo("age");
            soft.assertThat(conditions.get(1).value()).isEqualTo(NumberQueryValue.of(10));

            soft.assertThat(conditions.get(0).name()).isEqualTo("name");
            soft.assertThat(conditions.get(0).value()).isEqualTo(StringQueryValue.of("test"));
        });
    }


}
