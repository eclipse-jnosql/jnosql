/*
 *  Copyright (c) 2022,2025 Contributors to the Eclipse Foundation
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

import jakarta.data.Direction;
import jakarta.data.Sort;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.query.BooleanQueryValue;
import org.eclipse.jnosql.communication.query.ConditionQueryValue;
import org.eclipse.jnosql.communication.query.ParamQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.SelectQuery;
import org.eclipse.jnosql.communication.query.StringQueryValue;
import org.eclipse.jnosql.communication.query.Where;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.eclipse.jnosql.communication.Condition.NOT;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SelectMethodQueryProvider")
class SelectMethodQueryProviderTest {

    private final SelectMethodQueryParser queryProvider = new SelectMethodQueryParser();


    @Nested
    @DisplayName("WhenTheBasicMethodQueryIsParsed")
    class WhenTheBasicMethodQueryIsParsed {

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findBy", "existsBy"})
        @DisplayName("Should Return Parser Query")
        void shouldReturnParserQuery(String query) {
            String entity = "entity";
            SelectQuery selectQuery = queryProvider.apply(query, entity);
            assertThat(selectQuery).isNotNull();
            assertThat(selectQuery.entity()).isEqualTo(entity);
            assertThat(selectQuery.fields().isEmpty()).isTrue();
            assertThat(selectQuery.orderBy().isEmpty()).isTrue();
            assertThat(selectQuery.isCount()).isFalse();
            assertThat(selectQuery.limit()).isEqualTo(0);
            assertThat(selectQuery.skip()).isEqualTo(0);
            Optional<Where> where = selectQuery.where();
            assertThat(where.isPresent()).isFalse();
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"find", "exists"})
        @DisplayName("Should Query Using Without By")
        void shouldQueryUsingWithoutBy(String query) {
            String entity = "entity";
            SelectQuery selectQuery = queryProvider.apply(query, entity);
            assertThat(selectQuery).isNotNull();
            assertThat(selectQuery.entity()).isEqualTo(entity);
            assertThat(selectQuery.fields().isEmpty()).isTrue();
            assertThat(selectQuery.orderBy().isEmpty()).isTrue();
            assertThat(selectQuery.isCount()).isFalse();
            assertThat(selectQuery.limit()).isEqualTo(0);
            assertThat(selectQuery.skip()).isEqualTo(0);
            Optional<Where> where = selectQuery.where();
            assertThat(where.isPresent()).isFalse();
        }


        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"countBy", "countAll"})
        @DisplayName("Should Return Parsed Countable Query")
        void shouldReturnParsedCountableQuery(String query) {
            String entity = "entity";
            SelectQuery selectQuery = queryProvider.apply(query, entity);
            assertThat(selectQuery).isNotNull();
            assertThat(selectQuery.entity()).isEqualTo(entity);
            assertThat(selectQuery.fields().isEmpty()).isTrue();
            assertThat(selectQuery.orderBy().isEmpty()).isTrue();
            assertThat(selectQuery.isCount()).isTrue();
            assertThat(selectQuery.limit()).isEqualTo(0);
            assertThat(selectQuery.skip()).isEqualTo(0);
            Optional<Where> where = selectQuery.where();
            assertThat(where.isPresent()).isFalse();
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"count"})
        @DisplayName("Should Count Without By")
        void shouldCountWithoutBy(String query) {
            String entity = "entity";
            SelectQuery selectQuery = queryProvider.apply(query, entity);
            assertThat(selectQuery).isNotNull();
            assertThat(selectQuery.entity()).isEqualTo(entity);
            assertThat(selectQuery.fields().isEmpty()).isTrue();
            assertThat(selectQuery.orderBy().isEmpty()).isTrue();
            assertThat(selectQuery.isCount()).isTrue();
            assertThat(selectQuery.limit()).isEqualTo(0);
            assertThat(selectQuery.skip()).isEqualTo(0);
            Optional<Where> where = selectQuery.where();
            assertThat(where.isPresent()).isFalse();
        }


        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findFirst10By"})
        @DisplayName("Should Find First Ten Limit")
        void shouldFindFirstTenLimit(String query) {
            String entity = "entity";
            SelectQuery selectQuery = queryProvider.apply(query, entity);
            assertThat(selectQuery).isNotNull();
            assertThat(selectQuery.entity()).isEqualTo(entity);
            assertThat(selectQuery.fields().isEmpty()).isTrue();
            assertThat(selectQuery.orderBy().isEmpty()).isTrue();
            assertThat(selectQuery.limit()).isEqualTo(10);
            assertThat(selectQuery.skip()).isEqualTo(0);
            Optional<Where> where = selectQuery.where();
            assertThat(where.isPresent()).isFalse();
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findFirstBy"})
        @DisplayName("Should Find First Limit")
        void shouldFindFirstLimit(String query) {
            String entity = "entity";
            SelectQuery selectQuery = queryProvider.apply(query, entity);
            assertThat(selectQuery).isNotNull();
            assertThat(selectQuery.entity()).isEqualTo(entity);
            assertThat(selectQuery.fields().isEmpty()).isTrue();
            assertThat(selectQuery.orderBy().isEmpty()).isTrue();
            assertThat(selectQuery.limit()).isEqualTo(1);
            assertThat(selectQuery.skip()).isEqualTo(0);
            Optional<Where> where = selectQuery.where();
            assertThat(where.isPresent()).isFalse();
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findFirst10ByAge"})
        @DisplayName("Should Query First First By Age")
        void shouldQueryFirstFirstByAge(String query){
            String entity = "entity";
            SelectQuery selectQuery = queryProvider.apply(query, entity);

            SoftAssertions.assertSoftly( soft ->{
                soft.assertThat(selectQuery).isNotNull();
                soft.assertThat(selectQuery.limit()).isEqualTo(10);
                soft.assertThat(selectQuery.skip()).isEqualTo(0);
                soft.assertThat(selectQuery.orderBy()).isEmpty();
                soft.assertThat(selectQuery.fields()).isEmpty();
                soft.assertThat(selectQuery.where()).isPresent();
                soft.assertThat(selectQuery.where().get().condition().condition()).isEqualTo(Condition.EQUALS);
            });
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByName", "countByName", "existsByName"})
        @DisplayName("Should Parse Name Queries")
        void shouldParseNameQueries(String query) {
            checkCondition(query, Condition.EQUALS, "name");
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByNameEquals", "countByNameEquals", "existsByNameEquals"})
        @DisplayName("Should Parse Name Equals Queries")
        void shouldParseNameEqualsQueries(String query) {
            checkCondition(query, Condition.EQUALS, "name");
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByNameNotEquals", "countByNameNotEquals", "existsByNameNotEquals"})
        @DisplayName("Should Parse Name Not Equals Queries")
        void shouldParseNameNotEqualsQueries(String query) {
            checkNotCondition(query, Condition.EQUALS, "name");
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByAgeGreaterThan", "countByAgeGreaterThan", "existsByAgeGreaterThan"})
        @DisplayName("Should Parse Age Greater Than Queries")
        void shouldParseAgeGreaterThanQueries(String query) {

            Condition operator = Condition.GREATER_THAN;
            String variable = "age";
            checkCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByAgeNotGreaterThan", "countByAgeNotGreaterThan", "existsByAgeNotGreaterThan"})
        @DisplayName("Should Parse Age Not Greater Than Queries")
        void shouldParseAgeNotGreaterThanQueries(String query) {
            Condition operator = Condition.GREATER_THAN;
            String variable = "age";
            checkNotCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByAgeGreaterThanEqual", "countByAgeGreaterThanEqual", "existsByAgeGreaterThanEqual"})
        @DisplayName("Should Parse Age Greater Than Equal Queries")
        void shouldParseAgeGreaterThanEqualQueries(String query) {

            Condition operator = Condition.GREATER_EQUALS_THAN;
            String variable = "age";
            checkCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByAgeNotGreaterThanEqual", "countByAgeNotGreaterThanEqual", "existsByAgeNotGreaterThanEqual"})
        @DisplayName("Should Parse Age Not Greater Than Equal Queries")
        void shouldParseAgeNotGreaterThanEqualQueries(String query) {
            Condition operator = Condition.GREATER_EQUALS_THAN;
            String variable = "age";
            checkNotCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByAgeLessThan", "countByAgeLessThan", "existsByAgeLessThan"})
        @DisplayName("Should Parse Age Less Than Queries")
        void shouldParseAgeLessThanQueries(String query) {

            Condition operator = Condition.LESSER_THAN;
            String variable = "age";
            checkCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByAgeNotLessThan", "countByAgeNotLessThan", "existsByAgeNotLessThan"})
        @DisplayName("Should Parse Age Not Less Than Queries")
        void shouldParseAgeNotLessThanQueries(String query) {
            Condition operator = Condition.LESSER_THAN;
            String variable = "age";
            checkNotCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByAgeLessThanEqual", "countByAgeLessThanEqual", "existsByAgeLessThanEqual"})
        @DisplayName("Should Parse Age Less Than Equal Queries")
        void shouldParseAgeLessThanEqualQueries(String query) {

            Condition operator = Condition.LESSER_EQUALS_THAN;
            String variable = "age";
            checkCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByAgeNotLessThanEqual", "countByAgeNotLessThanEqual", "existsByAgeNotLessThanEqual"})
        @DisplayName("Should Parse Age Not Less Than Equal Queries")
        void shouldParseAgeNotLessThanEqualQueries(String query) {
            Condition operator = Condition.LESSER_EQUALS_THAN;
            String variable = "age";
            checkNotCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByAgeLike", "countByAgeLike", "existsByAgeLike"})
        @DisplayName("Should Parse Age Like Queries")
        void shouldParseAgeLikeQueries(String query) {

            Condition operator = Condition.LIKE;
            String variable = "age";
            checkCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByAgeNotLike", "countByAgeNotLike", "existsByAgeNotLike"})
        @DisplayName("Should Parse Age Not Like Queries")
        void shouldParseAgeNotLikeQueries(String query) {
            Condition operator = Condition.LIKE;
            String variable = "age";
            checkNotCondition(query, operator, variable);
        }


        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByAgeIn", "countByAgeIn", "existsByAgeIn"})
        @DisplayName("Should Parse Age In Queries")
        void shouldParseAgeInQueries(String query) {

            Condition operator = Condition.IN;
            String variable = "age";
            checkCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByAgeNotIn", "countByAgeNotIn", "existsByAgeNotIn"})
        @DisplayName("Should Parse Age Not In Queries")
        void shouldParseAgeNotInQueries(String query) {
            Condition operator = Condition.IN;
            String variable = "age";
            checkNotCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByAgeAndName", "countByAgeAndName", "existsByAgeAndName"})
        @DisplayName("Should Parse Age And Name Queries")
        void shouldParseAgeAndNameQueries(String query) {

            Condition operator = Condition.EQUALS;
            Condition operator2 = Condition.EQUALS;
            String variable = "age";
            String variable2 = "name";
            Condition operatorAppender = Condition.AND;
            checkAppendCondition(query, operator, operator2, variable, variable2, operatorAppender);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByAgeOrName", "countByAgeOrName", "existsByAgeOrName"})
        @DisplayName("Should Parse Age Or Name Queries")
        void shouldParseAgeOrNameQueries(String query) {

            Condition operator = Condition.EQUALS;
            Condition operator2 = Condition.EQUALS;
            String variable = "age";
            String variable2 = "name";
            Condition operatorAppender = Condition.OR;
            checkAppendCondition(query, operator, operator2, variable, variable2, operatorAppender);
        }


        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByAgeOrNameLessThan", "countByAgeOrNameLessThan", "existsByAgeOrNameLessThan"})
        @DisplayName("Should Parse Age Or Name Less Than Queries")
        void shouldParseAgeOrNameLessThanQueries(String query) {

            Condition operator = Condition.EQUALS;
            Condition operator2 = Condition.LESSER_THAN;
            String variable = "age";
            String variable2 = "name";
            Condition operatorAppender = Condition.OR;
            checkAppendCondition(query, operator, operator2, variable, variable2, operatorAppender);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByAgeGreaterThanOrNameIn", "countByAgeGreaterThanOrNameIn", "existsByAgeGreaterThanOrNameIn"})
        @DisplayName("Should Parse Age Greater Than Or Name In Queries")
        void shouldParseAgeGreaterThanOrNameInQueries(String query) {

            Condition operator = Condition.GREATER_THAN;
            Condition operator2 = Condition.IN;
            String variable = "age";
            String variable2 = "name";
            Condition operatorAppender = Condition.OR;
            checkAppendCondition(query, operator, operator2, variable, variable2, operatorAppender);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByOrderByName", "countByOrderByName", "existsByOrderByName"})
        @DisplayName("Should Parse Order By Name Queries")
        void shouldParseOrderByNameQueries(String query) {
            checkOrderBy(query, Direction.ASC);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByOrderByNameAsc", "countByOrderByNameAsc", "existsByOrderByNameAsc"})
        @DisplayName("Should Parse Order By Name Asc Queries")
        void shouldParseOrderByNameAscQueries(String query) {
            Direction type = Direction.ASC;
            checkOrderBy(query, type);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByOrderByNameDesc", "countByOrderByNameDesc", "existsByOrderByNameDesc"})
        @DisplayName("Should Parse Order By Name Desc Queries")
        void shouldParseOrderByNameDescQueries(String query) {
            checkOrderBy(query, Direction.DESC);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByOrderByNameDescAgeAsc", "countByOrderByNameDescAgeAsc", "existsByOrderByNameDescAgeAsc"})
        @DisplayName("Should Parse Order By Name Desc Then Age Asc Queries")
        void shouldParseOrderByNameDescThenAgeAscQueries(String query) {

            Direction type = Direction.DESC;
            Direction type2 = Direction.ASC;
            checkOrderBy(query, type, type2);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByOrderByNameDescAge", "countByOrderByNameDescAge", "existsByOrderByNameDescAge"})
        @DisplayName("Should Parse Order By Name Desc Then Age Queries")
        void shouldParseOrderByNameDescThenAgeQueries(String query) {
            checkOrderBy(query, Direction.DESC, Direction.ASC);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByOrderByNameDescAgeDesc", "countByOrderByNameDescAgeDesc", "existsByOrderByNameDescAgeDesc"})
        @DisplayName("Should Parse Order By Name Desc Then Age Desc Queries")
        void shouldParseOrderByNameDescThenAgeDescQueries(String query) {
            checkOrderBy(query, Direction.DESC, Direction.DESC);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByOrderByNameAscAgeAsc", "countByOrderByNameAscAgeAsc", "existsByOrderByNameAscAgeAsc"})
        @DisplayName("Should Parse Order By Name Asc Then Age Asc Queries")
        void shouldParseOrderByNameAscThenAgeAscQueries(String query) {
            checkOrderBy(query, Direction.ASC, Direction.ASC);
        }


        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByAgeBetween", "countByAgeBetween", "existsByAgeBetween"})
        @DisplayName("Should Parse Age Between Queries")
        void shouldParseAgeBetweenQueries(String query) {

            Condition operator = Condition.BETWEEN;
            String entity = "entity";
            SelectQuery selectQuery = queryProvider.apply(query, entity);
            assertThat(selectQuery).isNotNull();
            assertThat(selectQuery.entity()).isEqualTo(entity);
            assertThat(selectQuery.fields().isEmpty()).isTrue();
            assertThat(selectQuery.orderBy().isEmpty()).isTrue();
            assertThat(selectQuery.limit()).isEqualTo(0);
            assertThat(selectQuery.skip()).isEqualTo(0);
            Optional<Where> where = selectQuery.where();
            assertThat(where.isPresent()).isTrue();
            QueryCondition condition = where.get().condition();
            QueryValue<?> value = condition.value();
            assertThat(condition.condition()).isEqualTo(operator);
            QueryValue<?>[] values = MethodArrayValue.class.cast(value).get();
            ParamQueryValue param1 = (ParamQueryValue) values[0];
            ParamQueryValue param2 = (ParamQueryValue) values[1];
            assertThat(param1.get()).isNotEqualTo(param2.get());
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByAgeNotBetween", "countByAgeNotBetween", "existsByAgeNotBetween"})
        @DisplayName("Should Parse Age Not Between Queries")
        void shouldParseAgeNotBetweenQueries(String query) {

            String entity = "entity";
            SelectQuery selectQuery = queryProvider.apply(query, entity);
            assertThat(selectQuery).isNotNull();
            assertThat(selectQuery.entity()).isEqualTo(entity);
            assertThat(selectQuery.fields().isEmpty()).isTrue();
            assertThat(selectQuery.orderBy().isEmpty()).isTrue();
            assertThat(selectQuery.limit()).isEqualTo(0);
            assertThat(selectQuery.skip()).isEqualTo(0);
            Optional<Where> where = selectQuery.where();
            assertThat(where.isPresent()).isTrue();
            QueryCondition condition = where.get().condition();
            QueryValue<?> value = condition.value();
            assertThat(condition.condition()).isEqualTo(Condition.NOT);
            QueryCondition notCondition = ConditionQueryValue.class.cast(value).get().getFirst();
            assertThat(notCondition.condition()).isEqualTo(Condition.BETWEEN);

            QueryValue<?>[] values = MethodArrayValue.class.cast(notCondition.value()).get();
            ParamQueryValue param1 = (ParamQueryValue) values[0];
            ParamQueryValue param2 = (ParamQueryValue) values[1];
            assertThat(param1.get()).isNotEqualTo(param2.get());
        }


        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findBySalary_Currency", "countBySalary_Currency", "existsBySalary_Currency"})
        @DisplayName("Should Parse Salary Currency Embedded Field Queries")
        void shouldParseSalaryCurrencyEmbeddedFieldQueries(String query) {
            String entity = "entity";
            SelectQuery selectQuery = queryProvider.apply(query, entity);
            assertThat(selectQuery).isNotNull();
            assertThat(selectQuery.entity()).isEqualTo(entity);
            assertThat(selectQuery.fields().isEmpty()).isTrue();
            assertThat(selectQuery.orderBy().isEmpty()).isTrue();
            assertThat(selectQuery.limit()).isEqualTo(0);
            assertThat(selectQuery.skip()).isEqualTo(0);
            Optional<Where> where = selectQuery.where();
            assertThat(where.isPresent()).isTrue();
            assertThat(where.isPresent()).isTrue();
            QueryCondition condition = where.get().condition();
            assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(condition.name()).isEqualTo("salary.currency");
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findBySalary_CurrencyAndCredential_Role", "countBySalary_CurrencyAndCredential_Role",
                "existsBySalary_CurrencyAndCredential_Role"})
        @DisplayName("Should Parse Salary Currency And Credential Role Queries")
        void shouldParseSalaryCurrencyAndCredentialRoleQueries(String query) {
            String entity = "entity";
            SelectQuery selectQuery = queryProvider.apply(query, entity);
            assertThat(selectQuery).isNotNull();
            assertThat(selectQuery.entity()).isEqualTo(entity);
            assertThat(selectQuery.fields().isEmpty()).isTrue();
            assertThat(selectQuery.orderBy().isEmpty()).isTrue();
            assertThat(selectQuery.limit()).isEqualTo(0);
            assertThat(selectQuery.skip()).isEqualTo(0);
            Optional<Where> where = selectQuery.where();
            assertThat(where.isPresent()).isTrue();
            QueryCondition condition = where.get().condition();
            assertThat(condition.condition()).isEqualTo(Condition.AND);
            final QueryValue<?> value = condition.value();
            QueryCondition condition1 = ConditionQueryValue.class.cast(value).get().get(0);
            QueryCondition condition2 = ConditionQueryValue.class.cast(value).get().get(1);
            assertThat(condition1.name()).isEqualTo("salary.currency");
            assertThat(condition2.name()).isEqualTo("credential.role");
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findBySalary_CurrencyAndName", "countBySalary_CurrencyAndName",
                "existsBySalary_CurrencyAndName"})
        @DisplayName("Should Parse Salary Currency And Name Queries")
        void shouldParseSalaryCurrencyAndNameQueries(String query) {
            String entity = "entity";
            SelectQuery selectQuery = queryProvider.apply(query, entity);
            assertThat(selectQuery).isNotNull();
            assertThat(selectQuery.entity()).isEqualTo(entity);
            assertThat(selectQuery.fields().isEmpty()).isTrue();
            assertThat(selectQuery.orderBy().isEmpty()).isTrue();
            assertThat(selectQuery.limit()).isEqualTo(0);
            assertThat(selectQuery.skip()).isEqualTo(0);
            Optional<Where> where = selectQuery.where();
            assertThat(where.isPresent()).isTrue();
            QueryCondition condition = where.get().condition();
            assertThat(condition.condition()).isEqualTo(Condition.AND);
            final QueryValue<?> value = condition.value();
            QueryCondition condition1 = ConditionQueryValue.class.cast(value).get().get(0);
            QueryCondition condition2 = ConditionQueryValue.class.cast(value).get().get(1);
            assertThat(condition1.name()).isEqualTo("salary.currency");
            assertThat(condition2.name()).isEqualTo("name");
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findBySalary_CurrencyOrderBySalary_Value", "countBySalary_CurrencyOrderBySalary_Value"
                ,"existsBySalary_CurrencyOrderBySalary_Value"})
        @DisplayName("Should Parse Salary Currency Order By Salary Value Queries")
        void shouldParseSalaryCurrencyOrderBySalaryValueQueries(String query) {
            String entity = "entity";
            SelectQuery selectQuery = queryProvider.apply(query, entity);
            assertThat(selectQuery).isNotNull();
            assertThat(selectQuery.entity()).isEqualTo(entity);
            assertThat(selectQuery.fields().isEmpty()).isTrue();
            assertThat(selectQuery.orderBy().isEmpty()).isFalse();
            assertThat(selectQuery.limit()).isEqualTo(0);
            assertThat(selectQuery.skip()).isEqualTo(0);
            Optional<Where> where = selectQuery.where();
            assertThat(where.isPresent()).isTrue();
            QueryCondition condition = where.get().condition();
            assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(condition.name()).isEqualTo("salary.currency");

            final Sort<?> sort = selectQuery.orderBy().getFirst();
            assertThat(sort.property()).isEqualTo("salary.value");
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByActiveTrue", "countByActiveTrue", "existsByActiveTrue"})
        @DisplayName("Should Parse Active True Queries")
        void shouldParseActiveTrueQueries(String query) {
            String entity = "entity";
            SelectQuery selectQuery = queryProvider.apply(query, entity);
            assertThat(selectQuery).isNotNull();
            assertThat(selectQuery.entity()).isEqualTo(entity);
            assertThat(selectQuery.fields().isEmpty()).isTrue();
            assertThat(selectQuery.orderBy().isEmpty()).isTrue();
            assertThat(selectQuery.limit()).isEqualTo(0);
            assertThat(selectQuery.skip()).isEqualTo(0);
            Optional<Where> where = selectQuery.where();
            assertThat(where.isPresent()).isTrue();
            QueryCondition condition = where.orElseThrow().condition();
            assertThat(condition.name()).isEqualTo("active");
            assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(condition.value()).isEqualTo(BooleanQueryValue.TRUE);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByActiveFalse", "countByActiveFalse", "existsByActiveFalse"})
        @DisplayName("Should Parse Active False Queries")
        void shouldParseActiveFalseQueries(String query) {
            String entity = "entity";
            SelectQuery selectQuery = queryProvider.apply(query, entity);
            assertThat(selectQuery).isNotNull();
            assertThat(selectQuery.entity()).isEqualTo(entity);
            assertThat(selectQuery.fields().isEmpty()).isTrue();
            assertThat(selectQuery.orderBy().isEmpty()).isTrue();
            assertThat(selectQuery.limit()).isEqualTo(0);
            assertThat(selectQuery.skip()).isEqualTo(0);
            Optional<Where> where = selectQuery.where();
            assertThat(where.isPresent()).isTrue();
            QueryCondition condition = where.orElseThrow().condition();
            assertThat(condition.name()).isEqualTo("active");
            assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(condition.value()).isEqualTo(BooleanQueryValue.FALSE);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByNameNot", "countByNameNot", "existsByNameNot"})
        @DisplayName("Should Parse Name Not Queries")
        void shouldParseNameNotQueries(String query) {
            checkNotCondition(query, Condition.EQUALS, "name");
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByNameNotNull", "countByNameNotNull", "existsByNameNotNull"})
        @DisplayName("Should Parse Name Not Null Queries")
        void shouldParseNameNotNullQueries(String query) {
            String entity = "entity";
            SelectQuery selectQuery = queryProvider.apply(query, entity);
            assertThat(selectQuery).isNotNull();
            assertThat(selectQuery.entity()).isEqualTo(entity);
            assertThat(selectQuery.fields().isEmpty()).isTrue();
            assertThat(selectQuery.orderBy().isEmpty()).isTrue();
            assertThat(selectQuery.limit()).isEqualTo(0);
            assertThat(selectQuery.skip()).isEqualTo(0);
            Optional<Where> where = selectQuery.where();
            assertThat(where.isPresent()).isTrue();
            QueryCondition condition = where.get().condition();
            QueryValue<?> value = condition.value();
            assertThat(condition.condition()).isEqualTo(Condition.NOT);


            assertThat(condition.name()).isEqualTo("_NOT");
            assertThat(value).isInstanceOf(ConditionQueryValue.class);
            QueryCondition condition1 = ConditionQueryValue.class.cast(value).get().getFirst();

            assertThat(condition1.name()).isEqualTo("name");
            assertThat(condition1.condition()).isEqualTo(Condition.EQUALS);
            var param = condition1.value();
            assertThat(StringQueryValue.class.cast(param).get()).isNull();

        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByNameNull", "countByNameNull", "existsByNameNull"})
        @DisplayName("Should Parse Name Null Queries")
        void shouldParseNameNullQueries(String query) {
            String entity = "entity";
            SelectQuery selectQuery = queryProvider.apply(query, entity);
            assertThat(selectQuery).isNotNull();
            assertThat(selectQuery.entity()).isEqualTo(entity);
            assertThat(selectQuery.fields().isEmpty()).isTrue();
            assertThat(selectQuery.orderBy().isEmpty()).isTrue();
            assertThat(selectQuery.limit()).isEqualTo(0);
            assertThat(selectQuery.skip()).isEqualTo(0);
            Optional<Where> where = selectQuery.where();
            assertThat(where.isPresent()).isTrue();
            QueryCondition condition = where.get().condition();
            assertThat(condition.name()).isEqualTo("name");
            assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(condition.value().get()).isNull();
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @CsvSource(useHeadersInDisplayName = true, delimiter = '|',
                textBlock = """
                query                                      | expectedProperty   | expectedConditions
                findByStreetNameIgnoreCase                 | streetName         | IGNORE_CASE, EQUALS
                findByAddress_StreetNameIgnoreCase         | address.streetName | IGNORE_CASE, EQUALS
                findByHexadecimalIgnoreCase                | hexadecimal        | IGNORE_CASE, EQUALS
                findByStreetNameIgnoreCaseNot              | streetName         | NOT, IGNORE_CASE, EQUALS
                findByAddress_StreetNameIgnoreCaseNot      | address.streetName | NOT, IGNORE_CASE, EQUALS
                findByHexadecimalIgnoreCaseNot             | hexadecimal        | NOT, IGNORE_CASE, EQUALS
                findByStreetNameIgnoreCaseLike             | streetName         | IGNORE_CASE, LIKE
                findByStreetNameIgnoreCaseNotLike          | streetName         | NOT, IGNORE_CASE, LIKE
                findByStreetNameIgnoreCaseBetween          | streetName         | IGNORE_CASE, BETWEEN
                findByStreetNameIgnoreCaseIn               | streetName         | IGNORE_CASE, IN
                findByStreetNameIgnoreCaseGreaterThan      | streetName         | IGNORE_CASE, GREATER_THAN
                findByStreetNameIgnoreCaseGreaterThanEqual | streetName         | IGNORE_CASE, GREATER_EQUALS_THAN
                findByStreetNameIgnoreCaseLessThan         | streetName         | IGNORE_CASE, LESSER_THAN
                findByStreetNameIgnoreCaseLessThanEqual    | streetName         | IGNORE_CASE, LESSER_EQUALS_THAN
                findByStreetNameIgnoreCaseContains         | streetName         | IGNORE_CASE, CONTAINS
                findByStreetNameIgnoreCaseEndsWith         | streetName         | IGNORE_CASE, ENDS_WITH
                findByStreetNameIgnoreCaseStartsWith       | streetName         | IGNORE_CASE, STARTS_WITH
                            """)
        @DisplayName("Should Find By Street Name Ignore Case Conditions")
        void shouldFindByStreetNameIgnoreCaseConditions(String query, String expectedProperty,
                                                        @ConvertWith(ConditionConverter.class) Condition[] conditions) {
            checkConditions(query, expectedProperty, conditions);
        }

    }

    @Nested
    @DisplayName("When parsing mixed logical predicates")
    class WhenTheMixedLogicalPredicatesAreParsed {

        @ParameterizedTest(name = "{0}ByNameAndAgeOrCity")
        @ValueSource(strings = {"find", "count", "exists"})
        @DisplayName("Should apply AND before OR when the conjunction comes first")
        void shouldApplyAndBeforeOrWhenTheConjunctionComesFirst(String operation) {
            // Given
            String query = operation + "ByNameAndAgeOrCity";

            // When
            QueryCondition condition = queryProvider.apply(query, "entity")
                    .where().orElseThrow().condition();

            // Then
            assertConditionTree(condition, "OR(AND(name,age),city)", "name", "age", "city");
        }

        @ParameterizedTest(name = "{0}ByNameOrAgeAndCity")
        @ValueSource(strings = {"find", "count", "exists"})
        @DisplayName("Should apply AND before OR when the conjunction comes last")
        void shouldApplyAndBeforeOrWhenTheConjunctionComesLast(String operation) {
            // Given
            String query = operation + "ByNameOrAgeAndCity";

            // When
            QueryCondition condition = queryProvider.apply(query, "entity")
                    .where().orElseThrow().condition();

            // Then
            assertConditionTree(condition, "OR(name,AND(age,city))", "name", "age", "city");
        }

        @ParameterizedTest(name = "{0}ByNameAndAgeOrCityAndActiveAndEnabledOrEmail")
        @ValueSource(strings = {"find", "count", "exists"})
        @DisplayName("Should keep chained OR branches and their AND groups in lexical order")
        void shouldKeepChainedBranchesInLexicalOrder(String operation) {
            // Given
            String query = operation + "ByNameAndAgeOrCityAndActiveAndEnabledOrEmail";

            // When
            QueryCondition condition = queryProvider.apply(query, "entity")
                    .where().orElseThrow().condition();

            // Then
            assertConditionTree(condition, "OR(AND(name,age),AND(city,active,enabled),email)",
                    "name", "age", "city", "active", "enabled", "email");
        }
    }

    /*
     Converts from comma-separated values (space around commas is ignored) to an array of Condition instances,
     using Condition.valueOf
    */
    static class ConditionConverter implements ArgumentConverter {

        @Override
        public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
            if (!(source instanceof String)) {
                throw new ArgumentConversionException("Can only convert from String");
            }
            return Stream.of(String.class.cast(source).split("\\h*,\\h*"))
                    .map(Condition::valueOf)
                    .toArray(Condition[]::new);
        }
    }

    @Nested
    @DisplayName("WhenTheAdditionalMethodQueryIsParsed")
    class WhenTheAdditionalMethodQueryIsParsed {


        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByIdBetweenOrderByNumTypeOrdinalAsc"})
        @DisplayName("Should Find By Id Between Order By Num Type Ordinal Asc")
        void shouldFindByIdBetweenOrderByNumTypeOrdinalAsc(String query){
            String entity = "entity";
            SelectQuery selectQuery = queryProvider.apply(query, entity);

            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(selectQuery).isNotNull();
                soft.assertThat(selectQuery.entity()).isEqualTo(entity);
            });
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByNameContains"})
        @DisplayName("Should Find By Contains")
        void shouldFindByContains(String query) {
            Condition operator = Condition.CONTAINS;
            String variable = "name";
            checkCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByNameStartsWith"})
        @DisplayName("Should Find By Start With")
        void shouldFindByStartWith(String query) {
            Condition operator = Condition.STARTS_WITH;
            String variable = "name";
            checkCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByNameEndsWith"})
        @DisplayName("Should Find By Ends With")
        void shouldFindByEndsWith(String query) {
            Condition operator = Condition.ENDS_WITH;
            String variable = "name";
            checkCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByNameNotContains"})
        @DisplayName("Should Find By Not Contains")
        void shouldFindByNotContains(String query) {
            Condition operator = Condition.CONTAINS;
            String variable = "name";
            checkNotCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByNameNotStartsWith"})
        @DisplayName("Should Find By Not Start With")
        void shouldFindByNotStartWith(String query) {
            Condition operator = Condition.STARTS_WITH;
            String variable = "name";
            checkNotCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"findByNameNotEndsWith"})
        @DisplayName("Should Find By Not Ends With")
        void shouldFindByNotEndsWith(String query) {
            Condition operator = Condition.ENDS_WITH;
            String variable = "name";
            checkNotCondition(query, operator, variable);
        }

    }

    private void checkOrderBy(String query, Direction direction, Direction direction2) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertThat(selectQuery).isNotNull();
        assertThat(selectQuery.entity()).isEqualTo(entity);
        List<Sort<?>> sorts = selectQuery.orderBy();

        assertThat(sorts.size()).isEqualTo(2);
        Sort<?> sort = sorts.getFirst();
        assertThat(sort.property()).isEqualTo("name");
        assertThat(sort.isAscending() ? Direction.ASC : Direction.DESC).isEqualTo(direction);
        Sort<?> sort2 = sorts.get(1);
        assertThat(sort2.property()).isEqualTo("age");
        assertThat(sort2.isAscending() ? Direction.ASC : Direction.DESC).isEqualTo(direction2);
    }

    private void checkOrderBy(String query, Direction type) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertThat(selectQuery).isNotNull();
        assertThat(selectQuery.entity()).isEqualTo(entity);
        List<Sort<?>> sorts = selectQuery.orderBy();

        assertThat(sorts.size()).isEqualTo(1);
        Sort<?> sort = sorts.getFirst();
        assertThat(sort.property()).isEqualTo("name");
        assertThat(sort.isAscending() ? Direction.ASC : Direction.DESC).isEqualTo(type);
    }
    private void checkAppendCondition(String query, Condition operator, Condition operator2, String variable,
                                      String variable2, Condition operatorAppender) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertThat(selectQuery).isNotNull();
        assertThat(selectQuery.entity()).isEqualTo(entity);
        assertThat(selectQuery.fields().isEmpty()).isTrue();
        assertThat(selectQuery.orderBy().isEmpty()).isTrue();
        assertThat(selectQuery.limit()).isEqualTo(0);
        assertThat(selectQuery.skip()).isEqualTo(0);
        Optional<Where> where = selectQuery.where();
        assertThat(where.isPresent()).isTrue();
        QueryCondition condition = where.get().condition();
        QueryValue<?> value = condition.value();
        assertThat(condition.condition()).isEqualTo(operatorAppender);
        assertThat(value).isInstanceOf(ConditionQueryValue.class);
        QueryCondition condition1 = ConditionQueryValue.class.cast(value).get().get(0);
        QueryCondition condition2 = ConditionQueryValue.class.cast(value).get().get(1);

        assertThat(condition1.condition()).isEqualTo(operator);
        QueryValue<?> param = condition1.value();
        assertThat(condition1.condition()).isEqualTo(operator);
        assertThat(ParamQueryValue.class.cast(param).get().contains(variable)).isTrue();

        assertThat(condition2.condition()).isEqualTo(operator2);
        QueryValue<?> param2 = condition2.value();
        assertThat(operator2).isEqualTo(condition2.condition());
        assertThat(ParamQueryValue.class.cast(param2).get().contains(variable2)).isTrue();
    }


    private void checkNotCondition(String query, Condition operator, String variable) {
        checkConditions(query, variable, NOT, operator);
    }

    private void checkCondition(String query, Condition operator, String variable) {
        checkConditions(query, variable, operator);
    }

    private void checkConditions(String query, String variable, Condition... operators) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertThat(selectQuery).isNotNull();
        assertThat(selectQuery.entity()).isEqualTo(entity);
        assertThat(selectQuery.fields().isEmpty()).isTrue();
        assertThat(selectQuery.orderBy().isEmpty()).isTrue();
        assertThat(selectQuery.limit()).isEqualTo(0);
        assertThat(selectQuery.skip()).isEqualTo(0);
        Optional<Where> where = selectQuery.where();
        assertThat(where.isPresent()).isTrue();
        QueryCondition condition = where.get().condition();

        LinkedList<Condition> prependedOperators = new LinkedList<>(Arrays.asList(operators));
        Condition lastOperator = prependedOperators.getLast();
        prependedOperators.removeLast();

        for (Condition operator : prependedOperators) {
            condition = checkPrependedCondition(operator, condition);
        }

        checkTerminalCondition(condition, lastOperator, variable);
    }

    static QueryCondition checkPrependedCondition(Condition operator, QueryCondition condition) throws IllegalStateException {
        assertThat(condition.condition()).isEqualTo(operator);
        String expectedConditionName = switch (operator) {
            case NOT -> "_NOT";
            case IGNORE_CASE -> "_IGNORE_CASE";
            default -> throw new IllegalStateException("Operator " + operator + " not covered by these checks, please fix the tests to cover it.");
        };
        assertThat(condition.name()).isEqualTo(expectedConditionName);
        QueryValue<?> value = condition.value();
        assertThat(value).isInstanceOf(ConditionQueryValue.class);
        condition = ConditionQueryValue.class.cast(value).get().getFirst();
        return condition;
    }

    static void checkTerminalCondition(QueryCondition condition, Condition lastOperator, String variable) {
        QueryValue<?> value = condition.value();
        assertThat(condition.condition()).isEqualTo(lastOperator);

        switch (condition.condition()) {
            case EQUALS -> {
                assertThat(condition.name()).isEqualTo(variable);
                assertThat(ParamQueryValue.class.cast(value).get().contains(variable)).isTrue();
            }
            case BETWEEN -> {
                QueryValue<?>[] values = MethodArrayValue.class.cast(value).get();
                ParamQueryValue param1 = (ParamQueryValue) values[0];
                ParamQueryValue param2 = (ParamQueryValue) values[1];
                assertThat(param1.get()).isNotEqualTo(param2.get());
            }
            default -> {
                assertThat(ParamQueryValue.class.cast(value).get().contains(variable)).isTrue();
            }
        }
    }

    static void assertConditionTree(QueryCondition condition, String expectedTree, String... expectedLeaves) {
        List<QueryCondition> leaves = leafConditions(condition);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(conditionTree(condition))
                    .as("logical condition tree")
                    .isEqualTo(expectedTree);
            softly.assertThat(leaves)
                    .as("logical leaves in lexical order")
                    .extracting(QueryCondition::name)
                    .containsExactly(expectedLeaves);

            for (int index = 0; index < expectedLeaves.length; index++) {
                String expectedLeaf = expectedLeaves[index];
                softly.assertThat(leaves.get(index).value())
                        .as("value for leaf %s", expectedLeaf)
                        .isInstanceOf(ParamQueryValue.class);
                softly.assertThat(ParamQueryValue.class.cast(leaves.get(index).value()).get())
                        .as("parameter for leaf %s at index %s", expectedLeaf, index)
                        .startsWith(expectedLeaf + "_");
            }
        });
    }

    private static String conditionTree(QueryCondition condition) {
        if (Condition.AND.equals(condition.condition()) || Condition.OR.equals(condition.condition())) {
            return condition.condition().name() + ConditionQueryValue.class.cast(condition.value()).get().stream()
                    .map(SelectMethodQueryProviderTest::conditionTree)
                    .collect(java.util.stream.Collectors.joining(",", "(", ")"));
        }
        return condition.name();
    }

    private static List<QueryCondition> leafConditions(QueryCondition condition) {
        if (Condition.AND.equals(condition.condition()) || Condition.OR.equals(condition.condition())) {
            return ConditionQueryValue.class.cast(condition.value()).get().stream()
                    .flatMap(child -> leafConditions(child).stream())
                    .toList();
        }
        return List.of(condition);
    }


}
