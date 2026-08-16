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

import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.query.BooleanQueryValue;
import org.eclipse.jnosql.communication.query.ConditionQueryValue;
import org.eclipse.jnosql.communication.query.DeleteQuery;
import org.eclipse.jnosql.communication.query.ParamQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.Where;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;

import static org.eclipse.jnosql.communication.query.method.SelectMethodQueryProviderTest.checkPrependedCondition;
import static org.eclipse.jnosql.communication.query.method.SelectMethodQueryProviderTest.checkTerminalCondition;
import static org.eclipse.jnosql.communication.query.method.SelectMethodQueryProviderTest.assertConditionTree;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DeleteByMethodQueryProvider")
class DeleteByMethodQueryProviderTest {

    private final DeleteByMethodQueryParser queryProvider = new DeleteByMethodQueryParser();


    @Nested
    @DisplayName("WhenTheDeleteMethodQueryIsParsed")
    class WhenTheDeleteMethodQueryIsParsed {

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteBy"})
        @DisplayName("Should Parse Delete Prefix")
        void shouldParseDeletePrefix(String query) {
            String entity = "entity";
            DeleteQuery deleteQuery = queryProvider.apply(query, entity);
            assertThat(deleteQuery).isNotNull();
            assertThat(deleteQuery.entity()).isEqualTo(entity);
            assertThat(deleteQuery.fields().isEmpty()).isTrue();
            Optional<Where> where = deleteQuery.where();
            assertThat(where.isPresent()).isFalse();
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"delete"})
        @DisplayName("Should Parse Default Delete")
        void shouldParseDefaultDelete(String query) {
            String entity = "entity";
            DeleteQuery deleteQuery = queryProvider.apply(query, entity);
            assertThat(deleteQuery).isNotNull();
            assertThat(deleteQuery.entity()).isEqualTo(entity);
            assertThat(deleteQuery.fields().isEmpty()).isTrue();
            Optional<Where> where = deleteQuery.where();
            assertThat(where.isPresent()).isFalse();
        }


        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByName"})
        @DisplayName("Should Parse Delete By Name")
        void shouldParseDeleteByName(String query) {
            String entity = "entity";
            checkEqualsQuery(query, entity);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByNameEquals"})
        @DisplayName("Should Parse Delete By Name Equals")
        void shouldParseDeleteByNameEquals(String query) {
            String entity = "entity";
            checkEqualsQuery(query, entity);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByNameNotEquals"})
        @DisplayName("Should Parse Delete By Name Not Equals")
        void shouldParseDeleteByNameNotEquals(String query) {
            checkNotCondition(query, Condition.EQUALS, "name");
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByAgeGreaterThan"})
        @DisplayName("Should Parse Delete By Age Greater Than")
        void shouldParseDeleteByAgeGreaterThan(String query) {

            Condition operator = Condition.GREATER_THAN;
            String variable = "age";
            checkCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByAgeNotGreaterThan"})
        @DisplayName("Should Parse Delete By Age Not Greater Than")
        void shouldParseDeleteByAgeNotGreaterThan(String query) {
            Condition operator = Condition.GREATER_THAN;
            String variable = "age";
            checkNotCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByAgeGreaterThanEqual"})
        @DisplayName("Should Parse Delete By Age Greater Than Equal")
        void shouldParseDeleteByAgeGreaterThanEqual(String query) {

            Condition operator = Condition.GREATER_EQUALS_THAN;
            String variable = "age";
            checkCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByAgeNotGreaterThanEqual"})
        @DisplayName("Should Parse Delete By Age Not Greater Than Equal")
        void shouldParseDeleteByAgeNotGreaterThanEqual(String query) {
            Condition operator = Condition.GREATER_EQUALS_THAN;
            String variable = "age";
            checkNotCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByAgeLessThan"})
        @DisplayName("Should Parse Delete By Age Less Than")
        void shouldParseDeleteByAgeLessThan(String query) {

            Condition operator = Condition.LESSER_THAN;
            String variable = "age";
            checkCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByAgeNotLessThan"})
        @DisplayName("Should Parse Delete By Age Not Less Than")
        void shouldParseDeleteByAgeNotLessThan(String query) {
            Condition operator = Condition.LESSER_THAN;
            String variable = "age";
            checkNotCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByAgeLessThanEqual"})
        @DisplayName("Should Parse Delete By Age Less Than Equal")
        void shouldParseDeleteByAgeLessThanEqual(String query) {

            Condition operator = Condition.LESSER_EQUALS_THAN;
            String variable = "age";
            checkCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByAgeNotLessThanEqual"})
        @DisplayName("Should Parse Delete By Age Not Less Than Equal")
        void shouldParseDeleteByAgeNotLessThanEqual(String query) {
            Condition operator = Condition.LESSER_EQUALS_THAN;
            String variable = "age";
            checkNotCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByAgeLike"})
        @DisplayName("Should Parse Delete By Age Like")
        void shouldParseDeleteByAgeLike(String query) {

            Condition operator = Condition.LIKE;
            String variable = "age";
            checkCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByAgeNotLike"})
        @DisplayName("Should Parse Delete By Age Not Like")
        void shouldParseDeleteByAgeNotLike(String query) {
            Condition operator = Condition.LIKE;
            String variable = "age";
            checkNotCondition(query, operator, variable);
        }


        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByAgeIn"})
        @DisplayName("Should Parse Delete By Age In")
        void shouldParseDeleteByAgeIn(String query) {

            Condition operator = Condition.IN;
            String variable = "age";
            checkCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByAgeNotIn"})
        @DisplayName("Should Parse Delete By Age Not In")
        void shouldParseDeleteByAgeNotIn(String query) {
            Condition operator = Condition.IN;
            String variable = "age";
            checkNotCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByAgeAndName"})
        @DisplayName("Should Parse Delete By Age And Name")
        void shouldParseDeleteByAgeAndName(String query) {

            Condition operator = Condition.EQUALS;
            Condition operator2 = Condition.EQUALS;
            String variable = "age";
            String variable2 = "name";
            Condition operatorAppender = Condition.AND;
            checkAppendCondition(query, operator, operator2, variable, variable2, operatorAppender);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByAgeOrName"})
        @DisplayName("Should Parse Delete By Age Or Name")
        void shouldParseDeleteByAgeOrName(String query) {

            Condition operator = Condition.EQUALS;
            Condition operator2 = Condition.EQUALS;
            String variable = "age";
            String variable2 = "name";
            Condition operatorAppender = Condition.OR;
            checkAppendCondition(query, operator, operator2, variable, variable2, operatorAppender);
        }


        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByAgeOrNameLessThan"})
        @DisplayName("Should Parse Delete By Age Or Name Less Than")
        void shouldParseDeleteByAgeOrNameLessThan(String query) {

            Condition operator = Condition.EQUALS;
            Condition operator2 = Condition.LESSER_THAN;
            String variable = "age";
            String variable2 = "name";
            Condition operatorAppender = Condition.OR;
            checkAppendCondition(query, operator, operator2, variable, variable2, operatorAppender);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByAgeGreaterThanOrNameIn"})
        @DisplayName("Should Parse Delete By Age Greater Than Or Name In")
        void shouldParseDeleteByAgeGreaterThanOrNameIn(String query) {

            Condition operator = Condition.GREATER_THAN;
            Condition operator2 = Condition.IN;
            String variable = "age";
            String variable2 = "name";
            Condition operatorAppender = Condition.OR;
            checkAppendCondition(query, operator, operator2, variable, variable2, operatorAppender);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByAgeBetween"})
        @DisplayName("Should Parse Delete By Age Between")
        void shouldParseDeleteByAgeBetween(String query) {

            Condition operator = Condition.BETWEEN;
            String entity = "entity";
            DeleteQuery deleteQuery = queryProvider.apply(query, entity);
            assertThat(deleteQuery).isNotNull();
            assertThat(deleteQuery.entity()).isEqualTo(entity);
            assertThat(deleteQuery.fields().isEmpty()).isTrue();
            Optional<Where> where = deleteQuery.where();
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
        @ValueSource(strings = {"deleteByAgeNotBetween"})
        @DisplayName("Should Parse Delete By Age Not Between")
        void shouldParseDeleteByAgeNotBetween(String query) {

            String entity = "entity";
            DeleteQuery deleteQuery = queryProvider.apply(query, entity);
            assertThat(deleteQuery).isNotNull();
            assertThat(deleteQuery.entity()).isEqualTo(entity);
            assertThat(deleteQuery.fields().isEmpty()).isTrue();
            Optional<Where> where = deleteQuery.where();
            assertThat(where.isPresent()).isTrue();
            QueryCondition condition = where.get().condition();
            QueryValue<?> value = condition.value();
            assertThat(condition.condition()).isEqualTo(Condition.NOT);
            QueryCondition notCondition =  ConditionQueryValue.class.cast(value).get().getFirst();
            assertThat(notCondition.condition()).isEqualTo(Condition.BETWEEN);

            QueryValue<?>[] values = MethodArrayValue.class.cast(notCondition.value()).get();
            ParamQueryValue param1 = (ParamQueryValue) values[0];
            ParamQueryValue param2 = (ParamQueryValue) values[1];
            assertThat(param1.get()).isNotEqualTo(param2.get());
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteBySalary_Currency"})
        @DisplayName("Should Parse Delete By Salary Currency")
        void shouldParseDeleteBySalaryCurrency(String query) {
            String entity = "entity";
            DeleteQuery deleteQuery = queryProvider.apply(query, entity);
            assertThat(deleteQuery).isNotNull();
            assertThat(deleteQuery.entity()).isEqualTo(entity);
            assertThat(deleteQuery.fields().isEmpty()).isTrue();
            Optional<Where> where = deleteQuery.where();
            assertThat(where.isPresent()).isTrue();
            QueryCondition condition = where.get().condition();
            assertThat(condition.name()).isEqualTo("salary.currency");
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteBySalary_CurrencyAndCredential_Role"})
        @DisplayName("Should Parse Delete By Salary Currency And Credential Role")
        void shouldParseDeleteBySalaryCurrencyAndCredentialRole(String query) {
            String entity = "entity";
            DeleteQuery deleteQuery = queryProvider.apply(query, entity);
            assertThat(deleteQuery).isNotNull();
            assertThat(deleteQuery.entity()).isEqualTo(entity);
            assertThat(deleteQuery.fields().isEmpty()).isTrue();
            Optional<Where> where = deleteQuery.where();
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
        @ValueSource(strings = {"deleteBySalary_CurrencyAndName"})
        @DisplayName("Should Parse Delete By Salary Currency And Name")
        void shouldParseDeleteBySalaryCurrencyAndName(String query) {
            String entity = "entity";
            DeleteQuery deleteQuery = queryProvider.apply(query, entity);
            assertThat(deleteQuery).isNotNull();
            assertThat(deleteQuery.entity()).isEqualTo(entity);
            assertThat(deleteQuery.fields().isEmpty()).isTrue();
            Optional<Where> where = deleteQuery.where();
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
        @ValueSource(strings = {"deleteByActiveTrue"})
        @DisplayName("Should Parse Delete By Active True")
        void shouldParseDeleteByActiveTrue(String query) {
            String entity = "entity";
            DeleteQuery deleteQuery = queryProvider.apply(query, entity);
            assertThat(deleteQuery).isNotNull();
            assertThat(deleteQuery.entity()).isEqualTo(entity);
            assertThat(deleteQuery.fields().isEmpty()).isTrue();
            Optional<Where> where = deleteQuery.where();
            assertThat(where.isPresent()).isTrue();
            QueryCondition condition = where.orElseThrow().condition();
            assertThat(condition.name()).isEqualTo("active");
            assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(condition.value()).isEqualTo(BooleanQueryValue.TRUE);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByActiveFalse"})
        @DisplayName("Should Parse Delete By Active False")
        void shouldParseDeleteByActiveFalse(String query) {
            String entity = "entity";
            DeleteQuery deleteQuery = queryProvider.apply(query, entity);
            assertThat(deleteQuery).isNotNull();
            assertThat(deleteQuery.entity()).isEqualTo(entity);
            Optional<Where> where = deleteQuery.where();
            assertThat(where.isPresent()).isTrue();
            QueryCondition condition = where.orElseThrow().condition();
            assertThat(condition.name()).isEqualTo("active");
            assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            assertThat(condition.value()).isEqualTo(BooleanQueryValue.FALSE);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByNameContains"})
        @DisplayName("Should Parse Delete By Name Contains")
        void shouldParseDeleteByNameContains(String query) {
            Condition operator = Condition.CONTAINS;
            String variable = "name";
            checkCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByNameEndsWith"})
        @DisplayName("Should Parse Delete By Name Ends With")
        void shouldParseDeleteByNameEndsWith(String query) {
            Condition operator = Condition.ENDS_WITH;
            String variable = "name";
            checkCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByNameStartsWith"})
        @DisplayName("Should Parse Delete By Name Starts With")
        void shouldParseDeleteByNameStartsWith(String query) {
            Condition operator = Condition.STARTS_WITH;
            String variable = "name";
            checkCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByNameNotContains"})
        @DisplayName("Should Parse Delete By Name Not Contains")
        void shouldParseDeleteByNameNotContains(String query) {
            Condition operator = Condition.CONTAINS;
            String variable = "name";
            checkNotCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByNameNotEndsWith"})
        @DisplayName("Should Parse Delete By Name Not Ends With")
        void shouldParseDeleteByNameNotEndsWith(String query) {
            Condition operator = Condition.ENDS_WITH;
            String variable = "name";
            checkNotCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @ValueSource(strings = {"deleteByNameNotStartsWith"})
        @DisplayName("Should Parse Delete By Name Not Starts With")
        void shouldParseDeleteByNameNotStartsWith(String query) {
            Condition operator = Condition.STARTS_WITH;
            String variable = "name";
            checkNotCondition(query, operator, variable);
        }

        @ParameterizedTest(name = "Should parser the query {0}")
        @CsvSource(useHeadersInDisplayName = true, delimiter = '|',
                textBlock = """
                query                                      | expectedProperty   | expectedConditions
                deleteByStreetNameIgnoreCase                 | streetName         | IGNORE_CASE, EQUALS
                deleteByAddress_StreetNameIgnoreCase         | address.streetName | IGNORE_CASE, EQUALS
                deleteByHexadecimalIgnoreCase                | hexadecimal        | IGNORE_CASE, EQUALS
                deleteByStreetNameIgnoreCaseNot              | streetName         | NOT, IGNORE_CASE, EQUALS
                deleteByAddress_StreetNameIgnoreCaseNot      | address.streetName | NOT, IGNORE_CASE, EQUALS
                deleteByHexadecimalIgnoreCaseNot             | hexadecimal        | NOT, IGNORE_CASE, EQUALS
                deleteByStreetNameIgnoreCaseLike             | streetName         | IGNORE_CASE, LIKE
                deleteByStreetNameIgnoreCaseNotLike          | streetName         | NOT, IGNORE_CASE, LIKE
                deleteByStreetNameIgnoreCaseBetween          | streetName         | IGNORE_CASE, BETWEEN
                deleteByStreetNameIgnoreCaseIn               | streetName         | IGNORE_CASE, IN
                deleteByStreetNameIgnoreCaseGreaterThan      | streetName         | IGNORE_CASE, GREATER_THAN
                deleteByStreetNameIgnoreCaseGreaterThanEqual | streetName         | IGNORE_CASE, GREATER_EQUALS_THAN
                deleteByStreetNameIgnoreCaseLessThan         | streetName         | IGNORE_CASE, LESSER_THAN
                deleteByStreetNameIgnoreCaseLessThanEqual    | streetName         | IGNORE_CASE, LESSER_EQUALS_THAN
                deleteByStreetNameIgnoreCaseContains         | streetName         | IGNORE_CASE, CONTAINS
                deleteByStreetNameIgnoreCaseEndsWith         | streetName         | IGNORE_CASE, ENDS_WITH
                deleteByStreetNameIgnoreCaseStartsWith       | streetName         | IGNORE_CASE, STARTS_WITH
                            """)
        @DisplayName("Should Delete By Street Name Ignore Case Conditions")
        void shouldDeleteByStreetNameIgnoreCaseConditions(String query, String expectedProperty,
                                                        @ConvertWith(SelectMethodQueryProviderTest.ConditionConverter.class) Condition[] conditions) {
            checkConditions(query, expectedProperty, conditions);
        }

    }

    @Nested
    @DisplayName("When deleting with mixed logical predicates")
    class WhenTheMixedLogicalPredicatesAreParsed {

        @Test
        @DisplayName("Should apply AND before OR when the conjunction comes first")
        void shouldApplyAndBeforeOrWhenTheConjunctionComesFirst() {
            // When
            QueryCondition condition = queryProvider.apply("deleteByNameAndAgeOrCity", "entity")
                    .where().orElseThrow().condition();

            // Then
            assertConditionTree(condition, "OR(AND(name,age),city)", "name", "age", "city");
        }

        @Test
        @DisplayName("Should apply AND before OR when the conjunction comes last")
        void shouldApplyAndBeforeOrWhenTheConjunctionComesLast() {
            // When
            QueryCondition condition = queryProvider.apply("deleteByNameOrAgeAndCity", "entity")
                    .where().orElseThrow().condition();

            // Then
            assertConditionTree(condition, "OR(name,AND(age,city))", "name", "age", "city");
        }

        @Test
        @DisplayName("Should keep chained OR branches and their AND groups in lexical order")
        void shouldKeepChainedBranchesInLexicalOrder() {
            // When
            QueryCondition condition = queryProvider.apply(
                            "deleteByNameAndAgeOrCityAndActiveAndEnabledOrEmail", "entity")
                    .where().orElseThrow().condition();

            // Then
            assertConditionTree(condition, "OR(AND(name,age),AND(city,active,enabled),email)",
                    "name", "age", "city", "active", "enabled", "email");
        }
    }

    private void checkConditions(String query, String variable, Condition... operators) {
        String entity = "entity";
        var selectQuery = queryProvider.apply(query, entity);
        assertThat(selectQuery).isNotNull();
        assertThat(selectQuery.entity()).isEqualTo(entity);
        assertThat(selectQuery.fields().isEmpty()).isTrue();
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


    private void checkAppendCondition(String query, Condition operator, Condition operator2, String variable,
                                      String variable2, Condition operatorAppender) {
        String entity = "entity";
        DeleteQuery deleteQuery = queryProvider.apply(query, entity);
        assertThat(deleteQuery).isNotNull();
        assertThat(deleteQuery.entity()).isEqualTo(entity);
        assertThat(deleteQuery.fields().isEmpty()).isTrue();
        Optional<Where> where = deleteQuery.where();
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
        String entity = "entity";
        DeleteQuery deleteQuery = queryProvider.apply(query, entity);
        assertThat(deleteQuery).isNotNull();
        assertThat(deleteQuery.entity()).isEqualTo(entity);
        assertThat(deleteQuery.fields().isEmpty()).isTrue();
        Optional<Where> where = deleteQuery.where();
        assertThat(where.isPresent()).isTrue();
        QueryCondition condition = where.get().condition();
        QueryValue<?> value = condition.value();
        assertThat(condition.condition()).isEqualTo(Condition.NOT);


        assertThat(condition.name()).isEqualTo("_NOT");
        assertThat(value).isInstanceOf(ConditionQueryValue.class);
        QueryCondition condition1 = ConditionQueryValue.class.cast(value).get().getFirst();
        QueryValue<?> param = condition1.value();
        assertThat(condition1.condition()).isEqualTo(operator);
        assertThat(ParamQueryValue.class.cast(param).get().contains(variable)).isTrue();
    }

    private void checkEqualsQuery(String query, String entity) {
        DeleteQuery deleteQuery = queryProvider.apply(query, entity);
        assertThat(deleteQuery).isNotNull();
        assertThat(deleteQuery.entity()).isEqualTo(entity);
        assertThat(deleteQuery.fields().isEmpty()).isTrue();
        Optional<Where> where = deleteQuery.where();
        assertThat(where.isPresent()).isTrue();
        QueryCondition condition = where.get().condition();
        QueryValue<?> value = condition.value();
        assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
        assertThat(condition.name()).isEqualTo("name");
        assertThat(value).isInstanceOf(ParamQueryValue.class);
        assertThat(ParamQueryValue.class.cast(value).get().contains("name")).isTrue();
    }

    private void checkCondition(String query, Condition operator, String variable) {
        String entity = "entity";
        DeleteQuery deleteQuery = queryProvider.apply(query, entity);
        assertThat(deleteQuery).isNotNull();
        assertThat(deleteQuery.entity()).isEqualTo(entity);
        assertThat(deleteQuery.fields().isEmpty()).isTrue();
        Optional<Where> where = deleteQuery.where();
        assertThat(where.isPresent()).isTrue();
        QueryCondition condition = where.get().condition();
        QueryValue<?> value = condition.value();
        assertThat(condition.condition()).isEqualTo(operator);
        assertThat(ParamQueryValue.class.cast(value).get().contains(variable)).isTrue();
    }
}
