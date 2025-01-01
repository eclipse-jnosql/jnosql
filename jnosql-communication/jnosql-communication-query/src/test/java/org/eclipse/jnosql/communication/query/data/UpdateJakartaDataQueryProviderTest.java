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

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.query.NumberQueryValue;
import org.eclipse.jnosql.communication.query.StringQueryValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UpdateJakartaDataQueryProviderTest {

    private UpdateProvider updateProvider;

    @BeforeEach
    void setUp() {
        updateProvider = new UpdateProvider();
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "UPDATE entity SET name = 'Ada'")
    void shouldReturnParserQuery(String query) {
        var updateQuery = updateProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(updateQuery.where()).isEmpty();
            soft.assertThat(updateQuery.entity()).isEqualTo("entity");
            soft.assertThat(updateQuery.set()).isNotNull().hasSize(1)
                    .contains(JDQLUpdateItem.of("name", StringQueryValue.of("Ada")));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "UPDATE entity SET name = 'Ada', age = 10, salary = 10.15")
    void shouldReturnParserQueryAge(String query) {
        var updateQuery = updateProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(updateQuery.where()).isEmpty();
            soft.assertThat(updateQuery.entity()).isEqualTo("entity");
            soft.assertThat(updateQuery.set()).isNotNull().hasSize(3)
                    .contains(JDQLUpdateItem.of("name", StringQueryValue.of("Ada")),
                            JDQLUpdateItem.of("age", NumberQueryValue.of(10)),
                            JDQLUpdateItem.of("salary", NumberQueryValue.of(10.15)));
        });
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "UPDATE entity SET name = :param")
    void shouldReturnParserParam(String query) {
        var updateQuery = updateProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(updateQuery.where()).isEmpty();
            soft.assertThat(updateQuery.entity()).isEqualTo("entity");
            soft.assertThat(updateQuery.set()).isNotNull().hasSize(1)
                    .contains(JDQLUpdateItem.of("name", DefaultQueryValue.of("param")));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "UPDATE entity SET name = ?1")
    void shouldReturnParserParamIndex(String query) {
        var updateQuery = updateProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(updateQuery.where()).isEmpty();
            soft.assertThat(updateQuery.entity()).isEqualTo("entity");
            soft.assertThat(updateQuery.set()).isNotNull().hasSize(1)
                    .contains(JDQLUpdateItem.of("name", DefaultQueryValue.of("?1")));
        });
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "UPDATE entity SET name = 'Ada' WHERE age = 10")
    void shouldEq(String query){
        var updateQuery = updateProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(updateQuery.entity()).isEqualTo("entity");
            soft.assertThat(updateQuery.where()).isNotEmpty();
            var where = updateQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("age");
            soft.assertThat(condition.value()).isEqualTo(NumberQueryValue.of(10));

            soft.assertThat(updateQuery.set()).isNotNull().hasSize(1)
                    .contains(JDQLUpdateItem.of("name", StringQueryValue.of("Ada")));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "UPDATE entity SET name = 'Ada' WHERE salary = 10.15")
    void shouldEqDouble(String query){
        var updateQuery = updateProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(updateQuery.entity()).isEqualTo("entity");
            soft.assertThat(updateQuery.where()).isNotEmpty();
            var where = updateQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("salary");
            soft.assertThat(condition.value()).isEqualTo(NumberQueryValue.of(10.15));

            soft.assertThat(updateQuery.set()).isNotNull().hasSize(1)
                    .contains(JDQLUpdateItem.of("name", StringQueryValue.of("Ada")));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET name = 'Ada' WHERE name = \"Otavio\""})
    void shouldEqString(String query){
        var updateQuery = updateProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(updateQuery.entity()).isEqualTo("entity");
            soft.assertThat(updateQuery.where()).isNotEmpty();
            var where = updateQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("name");
            soft.assertThat(condition.value()).isEqualTo(StringQueryValue.of("Otavio"));

            soft.assertThat(updateQuery.set()).isNotNull().hasSize(1)
                    .contains(JDQLUpdateItem.of("name", StringQueryValue.of("Ada")));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET name = 'Ada' WHERE name = 'Otavio'"})
    void shouldEqStringSingleQuote(String query){
        var updateQuery = updateProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(updateQuery.entity()).isEqualTo("entity");
            soft.assertThat(updateQuery.where()).isNotEmpty();
            var where = updateQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("name");
            soft.assertThat(condition.value()).isEqualTo(StringQueryValue.of("Otavio"));

            soft.assertThat(updateQuery.set()).isNotNull().hasSize(1)
                    .contains(JDQLUpdateItem.of("name", StringQueryValue.of("Ada")));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE Box SET length = length + ?1"})
    void shouldReturnErrorAtPlusOperationInUpdate(String query) {
        Assertions.assertThatThrownBy(() ->updateProvider.apply(query))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE Box SET length = length - ?1"})
    void shouldReturnErrorAtMinusOperationInUpdate(String query) {
        Assertions.assertThatThrownBy(() ->updateProvider.apply(query))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE Box SET length = length * ?1"})
    void shouldReturnErrorAtMultiplyOperationInUpdate(String query) {
        Assertions.assertThatThrownBy(() ->updateProvider.apply(query))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE Box SET length = length / ?1"})
    void shouldReturnErrorAtDivOperationInUpdate(String query) {
        Assertions.assertThatThrownBy(() ->updateProvider.apply(query))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE Box SET length = length || ?1"})
    void shouldReturnErrorAtConcatOperationInUpdate(String query) {
        Assertions.assertThatThrownBy(() ->updateProvider.apply(query))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE Box SET length = (length)"})
    void shouldReturnErrorWhenHaveParenthesis(String query) {
        Assertions.assertThatThrownBy(() ->updateProvider.apply(query))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE Coordinate SET x = :newX, y = y / :yDivisor WHERE id = :id"})
    void shouldReturnErrorWhenHaveOperation(String query) {
        Assertions.assertThatThrownBy(() ->updateProvider.apply(query))
                .isInstanceOf(UnsupportedOperationException.class);
    }

}
