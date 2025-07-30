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
import org.eclipse.jnosql.communication.query.EnumQueryValue;
import org.eclipse.jnosql.communication.query.NullQueryValue;
import org.eclipse.jnosql.communication.query.NumberQueryValue;
import org.eclipse.jnosql.communication.query.StringQueryValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DayOfWeek;

class DeleteJakartaDataQueryProviderTest {


    private DeleteParser deleteParser;

    @BeforeEach
    void setUp() {
        deleteParser = new DeleteParser();
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"DELETE FROM entity"})
    void shouldReturnParserQuery(String query) {
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.where()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE age = 10")
    void shouldEq(String query){
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("age");
            soft.assertThat(condition.value()).isEqualTo(NumberQueryValue.of(10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE salary = 10.15")
    void shouldEqDouble(String query){
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("salary");
            soft.assertThat(condition.value()).isEqualTo(NumberQueryValue.of(10.15));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE name = \"Otavio\"")
    void shouldEqString(String query){
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("name");
            soft.assertThat(condition.value()).isEqualTo(StringQueryValue.of("Otavio"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE name = 'Otavio'")
    void shouldEqStringSingleQuote(String query){
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("name");
            soft.assertThat(condition.value()).isEqualTo(StringQueryValue.of("Otavio"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE name = :name")
    void shouldEQQueryWithCondition(String query){
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("name");
            soft.assertThat(condition.value()).isEqualTo(DefaultQueryValue.of("name"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE name = ?1")
    void shouldEQQueryWithConditionPosition(String query){
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("name");
            soft.assertThat(condition.value()).isEqualTo(DefaultQueryValue.of("?1"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE active = TRUE")
    void shouldUseSpecialExpressionTrue(String query){
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("active");
            soft.assertThat(condition.value()).isEqualTo(BooleanQueryValue.TRUE);
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE active = FALSE")
    void shouldUseSpecialExpressionFalse(String query){
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("active");
            soft.assertThat(condition.value()).isEqualTo(BooleanQueryValue.FALSE);
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE age < 10")
    void shouldUseSpecialExpressionLesser(String query){
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.LESSER_THAN);
            soft.assertThat(condition.name()).isEqualTo("age");
            soft.assertThat(condition.value()).isEqualTo(NumberQueryValue.of(10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE age > 10")
    void shouldUseSpecialExpressionGreater(String query){
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.GREATER_THAN);
            soft.assertThat(condition.name()).isEqualTo("age");
            soft.assertThat(condition.value()).isEqualTo(NumberQueryValue.of(10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE age <= 10")
    void shouldUseSpecialExpressionLesserThanEquals(String query){
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.LESSER_EQUALS_THAN);
            soft.assertThat(condition.name()).isEqualTo("age");
            soft.assertThat(condition.value()).isEqualTo(NumberQueryValue.of(10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE age >= 10")
    void shouldUseSpecialExpressionGreaterThanEquals(String query){
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.GREATER_EQUALS_THAN);
            soft.assertThat(condition.name()).isEqualTo("age");
            soft.assertThat(condition.value()).isEqualTo(NumberQueryValue.of(10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE days = java.time.DayOfWeek.MONDAY")
    void shouldEqUsingEnumLiteral(String query){
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("days");
            soft.assertThat(condition.value()).isEqualTo(EnumQueryValue.of(DayOfWeek.MONDAY));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE hexadecimal IS NULL")
    void shouldQueryIsNull(String query) {
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("hexadecimal");
            soft.assertThat(condition.value()).isEqualTo(NullQueryValue.INSTANCE);
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE hexadecimal IS NOT NULL")
    void shouldQueryIsNotNull(String query) {

        var deleteQuery = deleteParser.apply(query);
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            var notCondition = (ConditionQueryValue) condition.value();
            var queryCondition = notCondition.get().get(0);
            soft.assertThat(queryCondition.name()).isEqualTo("hexadecimal");
            soft.assertThat(queryCondition.value()).isEqualTo(NullQueryValue.INSTANCE);
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE NOT age <> 10")
    void shouldUseNotNotEquals(String query) {
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("age");
            soft.assertThat(condition.value()).isEqualTo(NumberQueryValue.of(10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE age <> 10")
    void shouldUseNotEquals(String query) {
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            var notCondition = (ConditionQueryValue) condition.value();
            var queryCondition = notCondition.get().get(0);
            soft.assertThat(queryCondition.name()).isEqualTo("age");
            soft.assertThat(queryCondition.value()).isEqualTo(NumberQueryValue.of(10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "DELETE FROM entity WHERE hexadecimal <> ' ORDER BY isn''t a keyword when inside a literal' AND hexadecimal IN ('4a', '4b', '4c')")
    void shouldUseNotEqualsCombined(String query) {
        var deleteQuery = deleteParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(deleteQuery.fields()).isEmpty();
            soft.assertThat(deleteQuery.entity()).isEqualTo("entity");
            soft.assertThat(deleteQuery.where()).isNotEmpty();
            var where = deleteQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.AND);

            var conditions = (ConditionQueryValue) condition.value();
            var negation = (ConditionQueryValue)conditions.get().get(0).value();

            var queryCondition = negation.get().get(0);
            soft.assertThat(queryCondition.name()).isEqualTo("hexadecimal");
            soft.assertThat(queryCondition.value()).isEqualTo(StringQueryValue.of(" ORDER BY isn''t a keyword when inside a literal"));
            var in = conditions.get().get(1);
            soft.assertThat(in.condition()).isEqualTo(Condition.IN);
            var value = (DataArrayQueryValue) in.value();
            soft.assertThat(value.get()).contains(StringQueryValue.of("4a"), StringQueryValue.of("4b"), StringQueryValue.of("4c"));
        });
    }

}
