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

import jakarta.data.Sort;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.query.BooleanQueryValue;
import org.eclipse.jnosql.communication.query.ConditionQueryValue;
import org.eclipse.jnosql.communication.query.EnumQueryValue;
import org.eclipse.jnosql.communication.query.NullQueryValue;
import org.eclipse.jnosql.communication.query.NumberQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryPath;
import org.eclipse.jnosql.communication.query.SelectQuery;
import org.eclipse.jnosql.communication.query.StringQueryValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DayOfWeek;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SelectJakartaDataQueryProvider")
class SelectJakartaDataQueryProviderTest {


    private SelectParser selectParser;

    @BeforeEach
    void setUp() {
        selectParser = new SelectParser();
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity"})
    @DisplayName("Should Return Parser Query")
    void shouldReturnParserQuery(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity"})
    @DisplayName("Should Overwrite The Entity")
    void shouldOverwriteTheEntity(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "newEntity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"", " "})
    @DisplayName("Should Keep Entity From Parameter")
    void shouldKeepEntityFromParameter(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"", " "})
    @DisplayName("Should Return Error When Entity Is Missing")
    void shouldReturnErrorWhenEntityIsMissing(String query) {
        assertThatThrownBy(() -> selectParser.apply(query, null)).isInstanceOf(IllegalArgumentException.class);
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity ORDER BY name ASC", "ORDER BY name ASC", "ORDER BY name"})
    @DisplayName("Should Query Order")
    void shouldQueryOrder(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.where()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).hasSize(1).contains(Sort.asc("name"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity ORDER BY name DESC", "ORDER BY name DESC"})
    @DisplayName("Should Query Order Desc")
    void shouldQueryOrderDesc(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.where()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).hasSize(1).contains(Sort.desc("name"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"FROM entity ORDER BY name ASC, age DESC", "ORDER BY name ASC, age DESC"})
    @DisplayName("Should Query Orders")
    void shouldQueryOrders(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.where()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).hasSize(2).contains(Sort.asc("name"), Sort.desc("age"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"SELECT name, age FROM entity", "SELECT name, age"})
    @DisplayName("Should Select Fields")
    void shouldSelectFields(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).hasSize(2).contains("name", "age");
            soft.assertThat(selectQuery.where()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
        });
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE age = 10", "FROM entity WHERE age = 10"})
    @DisplayName("Should Eq")
    void shouldEq(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("age");
            soft.assertThat(condition.value()).isEqualTo(NumberQueryValue.of(10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE salary = 10.15", "FROM entity WHERE salary = 10.15"})
    @DisplayName("Should Eq Double")
    void shouldEqDouble(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("salary");
            soft.assertThat(condition.value()).isEqualTo(NumberQueryValue.of(10.15));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE name = \"Otavio\"", "FROM entity WHERE name = \"Otavio\""})
    @DisplayName("Should Eq String")
    void shouldEqString(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("name");
            soft.assertThat(condition.value()).isEqualTo(StringQueryValue.of("Otavio"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE name = 'Otavio'", "FROM entity WHERE name = 'Otavio'"})
    @DisplayName("Should Eq String Single Quote")
    void shouldEqStringSingleQuote(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("name");
            soft.assertThat(condition.value()).isEqualTo(StringQueryValue.of("Otavio"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE name = :name", "FROM entity WHERE name = :name"})
    @DisplayName("Should EQQuery With Condition")
    void shouldEQQueryWithCondition(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("name");
            soft.assertThat(condition.value()).isEqualTo(DefaultQueryValue.of("name"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE name = ?1", "FROM entity WHERE name = ?1"})
    @DisplayName("Should EQQuery With Condition Position")
    void shouldEQQueryWithConditionPosition(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("name");
            soft.assertThat(condition.value()).isEqualTo(DefaultQueryValue.of("?1"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE active = TRUE", "FROM entity WHERE active = TRUE"})
    @DisplayName("Should Use Special Expression True")
    void shouldUseSpecialExpressionTrue(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("active");
            soft.assertThat(condition.value()).isEqualTo(BooleanQueryValue.TRUE);
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE active = FALSE", "FROM entity WHERE active = FALSE"})
    @DisplayName("Should Use Special Expression False")
    void shouldUseSpecialExpressionFalse(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("active");
            soft.assertThat(condition.value()).isEqualTo(BooleanQueryValue.FALSE);
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE age < 10", "FROM entity WHERE age < 10"})
    @DisplayName("Should Use Special Expression Lesser")
    void shouldUseSpecialExpressionLesser(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.LESSER_THAN);
            soft.assertThat(condition.name()).isEqualTo("age");
            soft.assertThat(condition.value()).isEqualTo(NumberQueryValue.of(10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE age > 10", "FROM entity WHERE age > 10"})
    @DisplayName("Should Use Special Expression Greater")
    void shouldUseSpecialExpressionGreater(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.GREATER_THAN);
            soft.assertThat(condition.name()).isEqualTo("age");
            soft.assertThat(condition.value()).isEqualTo(NumberQueryValue.of(10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE age <= 10", "FROM entity WHERE age <= 10"})
    @DisplayName("Should Use Special Expression Lesser Than Equals")
    void shouldUseSpecialExpressionLesserThanEquals(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.LESSER_EQUALS_THAN);
            soft.assertThat(condition.name()).isEqualTo("age");
            soft.assertThat(condition.value()).isEqualTo(NumberQueryValue.of(10));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE age >= 10", "FROM entity WHERE age >= 10"})
    @DisplayName("Should Use Special Expression Greater Than Equals")
    void shouldUseSpecialExpressionGreaterThanEquals(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.GREATER_EQUALS_THAN);
            soft.assertThat(condition.name()).isEqualTo("age");
            soft.assertThat(condition.value()).isEqualTo(NumberQueryValue.of(10));
            soft.assertThat(selectQuery.isCount()).isFalse();
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"WHERE days = java.time.DayOfWeek.MONDAY", "FROM entity WHERE days = java.time.DayOfWeek.MONDAY"})
    @DisplayName("Should Eq Using Enum Literal")
    void shouldEqUsingEnumLiteral(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("days");
            soft.assertThat(condition.value()).isEqualTo(EnumQueryValue.of(DayOfWeek.MONDAY));
            soft.assertThat(selectQuery.isCount()).isFalse();
        });
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "SELECT COUNT (THIS) WHERE age = 10")
    @DisplayName("Should Aggregate")
    void shouldAggregate(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("age");
            soft.assertThat(condition.value()).isEqualTo(NumberQueryValue.of(10));
            soft.assertThat(selectQuery.isCount()).isTrue();
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "SELECT hexadecimal WHERE hexadecimal IS NULL")
    @DisplayName("Should Query Is Null")
    void shouldQueryIsNull(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).hasSize(1).contains("hexadecimal");
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("hexadecimal");
            soft.assertThat(condition.value()).isEqualTo(NullQueryValue.INSTANCE);
            soft.assertThat(selectQuery.isCount()).isFalse();
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "SELECT hexadecimal WHERE hexadecimal IS NOT NULL")
    @DisplayName("Should Query Is Not Null")
    void shouldQueryIsNotNull(String query) {
        var selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).hasSize(1).contains("hexadecimal");
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            var notCondition = (ConditionQueryValue) condition.value();
            var queryCondition = notCondition.get().getFirst();
            soft.assertThat(queryCondition.name()).isEqualTo("hexadecimal");
            soft.assertThat(queryCondition.value()).isEqualTo(NullQueryValue.INSTANCE);
            soft.assertThat(selectQuery.isCount()).isFalse();
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "WHERE isOdd = false AND numType = java.time.DayOfWeek.MONDAY")
    @DisplayName("Should Query Condition Enum")
    void shouldQueryConditionEnum(String query) {
        var selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.AND);
            var andCondition = (ConditionQueryValue) condition.value();
            var first = andCondition.get().get(0);
            var second = andCondition.get().get(1);
            soft.assertThat(first.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(first.name()).isEqualTo("isOdd");
            soft.assertThat(first.value()).isEqualTo(BooleanQueryValue.FALSE);
            soft.assertThat(second.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(second.name()).isEqualTo("numType");
            soft.assertThat(second.value().get()).isEqualTo(DayOfWeek.MONDAY);
            soft.assertThat(selectQuery.isCount()).isFalse();
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "WHERE NOT age <> 10")
    @DisplayName("Should Use Not Not Equals")
    void shouldUseNotNotEquals(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.name()).isEqualTo("age");
            soft.assertThat(condition.value()).isEqualTo(NumberQueryValue.of(10));
            soft.assertThat(selectQuery.isCount()).isFalse();
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "WHERE age <> 10")
    @DisplayName("Should Use Not Equals")
    void shouldUseNotEquals(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            var notCondition = (ConditionQueryValue) condition.value();
            var queryCondition = notCondition.get().getFirst();
            soft.assertThat(queryCondition.name()).isEqualTo("age");
            soft.assertThat(queryCondition.value()).isEqualTo(NumberQueryValue.of(10));
            soft.assertThat(selectQuery.isCount()).isFalse();
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "WHERE hexadecimal <> ' ORDER BY isn''t a keyword when inside a literal' AND hexadecimal IN ('4a', '4b', '4c')")
    @DisplayName("Should Use Not Equals Combined")
    void shouldUseNotEqualsCombined(String query) {
        SelectQuery selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.AND);

            var conditions = (ConditionQueryValue) condition.value();
            var negation = (ConditionQueryValue)conditions.get().getFirst().value();

            var queryCondition = negation.get().getFirst();
            soft.assertThat(queryCondition.name()).isEqualTo("hexadecimal");
            soft.assertThat(queryCondition.value()).isEqualTo(StringQueryValue.of(" ORDER BY isn''t a keyword when inside a literal"));
            var in = conditions.get().get(1);
            soft.assertThat(in.condition()).isEqualTo(Condition.IN);
            var value = (DataArrayQueryValue) in.value();
            soft.assertThat(value.get()).contains(StringQueryValue.of("4a"), StringQueryValue.of("4b"), StringQueryValue.of("4c"));
            soft.assertThat(selectQuery.isCount()).isFalse();
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "Select id Where isOdd = true and (id = :id or id < :exclusiveMax) Order by id Desc")
    @DisplayName("Should Return Error When Use Parenthesis")
    void shouldReturnErrorWhenUseParenthesis(String query) {
        assertThatThrownBy(() -> selectParser.apply(query, "entity")).isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "where employeeName LIKE ?1")
    @DisplayName("Should Use Like")
    void shouldUseLike(String query) {
        var selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.LIKE);
            soft.assertThat(condition.name()).isEqualTo("employeeName");
            soft.assertThat(condition.value()).isEqualTo(DefaultQueryValue.of("?1"));

        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "where employeeName LIKE  :employeeName")
    @DisplayName("Should Use Like2")
    void shouldUseLike2(String query) {
        var selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.LIKE);
            soft.assertThat(condition.name()).isEqualTo("employeeName");
            soft.assertThat(condition.value()).isEqualTo(DefaultQueryValue.of(":employeeName"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "where employeeName LIKE 'employeeName'")
    @DisplayName("Should Use Like3")
    void shouldUseLike3(String query) {
        var selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.LIKE);
            soft.assertThat(condition.name()).isEqualTo("employeeName");
            soft.assertThat(condition.value()).isEqualTo(StringQueryValue.of("employeeName"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "where employeeName NOT LIKE 'employeeName'")
    @DisplayName("Should Use Not Like")
    void shouldUseNotLike(String query) {
        var selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            var notCondition = (ConditionQueryValue) condition.value();
            QueryCondition queryCondition = notCondition.get().getFirst();
            soft.assertThat(queryCondition.name()).isEqualTo("employeeName");
            soft.assertThat(queryCondition.value()).isEqualTo(StringQueryValue.of("employeeName"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "where employeeName NOT LIKE ?1")
    @DisplayName("Should Use Not Like2")
    void shouldUseNotLike2(String query) {
        var selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            var notCondition = (ConditionQueryValue) condition.value();
            QueryCondition queryCondition = notCondition.get().getFirst();
            soft.assertThat(queryCondition.condition()).isEqualTo(Condition.LIKE);
            soft.assertThat(queryCondition.name()).isEqualTo("employeeName");
            soft.assertThat(queryCondition.value()).isEqualTo(DefaultQueryValue.of("?1"));

        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "where employeeName NOT LIKE :employeeName")
    @DisplayName("Should Use Not Like3")
    void shouldUseNotLike3(String query) {
        var selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            var notCondition = (ConditionQueryValue) condition.value();
            QueryCondition queryCondition = notCondition.get().getFirst();
            soft.assertThat(queryCondition.condition()).isEqualTo(Condition.LIKE);
            soft.assertThat(queryCondition.name()).isEqualTo("employeeName");
            soft.assertThat(queryCondition.value()).isEqualTo(DefaultQueryValue.of(":employeeName"));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = "WHERE quantity >= attribute")
    @DisplayName("Should Query Reference Path")
    void shouldQueryReferencePath(String query) {
        var selectQuery = selectParser.apply(query, "entity");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.fields()).isEmpty();
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            soft.assertThat(selectQuery.orderBy()).isEmpty();
            soft.assertThat(selectQuery.where()).isNotEmpty();
            var where = selectQuery.where().orElseThrow();
            var condition = where.condition();
            soft.assertThat(condition.condition()).isEqualTo(Condition.GREATER_EQUALS_THAN);
            soft.assertThat(condition.name()).isEqualTo("quantity");
            soft.assertThat(condition.value()).isEqualTo(QueryPath.of("attribute"));
        });
    }
}
