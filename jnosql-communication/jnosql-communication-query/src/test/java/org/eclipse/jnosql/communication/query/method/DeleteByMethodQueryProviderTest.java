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
import org.eclipse.jnosql.communication.query.SelectQuery;
import org.eclipse.jnosql.communication.query.Where;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;

import static org.eclipse.jnosql.communication.query.method.SelectMethodQueryProviderTest.checkPrependedCondition;
import static org.eclipse.jnosql.communication.query.method.SelectMethodQueryProviderTest.checkTerminalCondition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeleteByMethodQueryProviderTest {

    private final DeleteByMethodQueryParser queryProvider = new DeleteByMethodQueryParser();


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteBy"})
    void shouldParseDeletePrefix(String query) {
        String entity = "entity";
        DeleteQuery deleteQuery = queryProvider.apply(query, entity);
        assertNotNull(deleteQuery);
        assertEquals(entity, deleteQuery.entity());
        assertTrue(deleteQuery.fields().isEmpty());
        Optional<Where> where = deleteQuery.where();
        assertFalse(where.isPresent());
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByName"})
    void shouldParseDeleteByName(String query) {
        String entity = "entity";
        checkEqualsQuery(query, entity);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByNameEquals"})
    void shouldParseDeleteByNameEquals(String query) {
        String entity = "entity";
        checkEqualsQuery(query, entity);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByNameNotEquals"})
    void shouldParseDeleteByNameNotEquals(String query) {
        checkNotCondition(query, Condition.EQUALS, "name");
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeGreaterThan"})
    void shouldParseDeleteByAgeGreaterThan(String query) {

        Condition operator = Condition.GREATER_THAN;
        String variable = "age";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeNotGreaterThan"})
    void shouldParseDeleteByAgeNotGreaterThan(String query) {
        Condition operator = Condition.GREATER_THAN;
        String variable = "age";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeGreaterThanEqual"})
    void shouldParseDeleteByAgeGreaterThanEqual(String query) {

        Condition operator = Condition.GREATER_EQUALS_THAN;
        String variable = "age";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeNotGreaterThanEqual"})
    void shouldParseDeleteByAgeNotGreaterThanEqual(String query) {
        Condition operator = Condition.GREATER_EQUALS_THAN;
        String variable = "age";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeLessThan"})
    void shouldParseDeleteByAgeLessThan(String query) {

        Condition operator = Condition.LESSER_THAN;
        String variable = "age";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeNotLessThan"})
    void shouldParseDeleteByAgeNotLessThan(String query) {
        Condition operator = Condition.LESSER_THAN;
        String variable = "age";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeLessThanEqual"})
    void shouldParseDeleteByAgeLessThanEqual(String query) {

        Condition operator = Condition.LESSER_EQUALS_THAN;
        String variable = "age";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeNotLessThanEqual"})
    void shouldParseDeleteByAgeNotLessThanEqual(String query) {
        Condition operator = Condition.LESSER_EQUALS_THAN;
        String variable = "age";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeLike"})
    void shouldParseDeleteByAgeLike(String query) {

        Condition operator = Condition.LIKE;
        String variable = "age";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeNotLike"})
    void shouldParseDeleteByAgeNotLike(String query) {
        Condition operator = Condition.LIKE;
        String variable = "age";
        checkNotCondition(query, operator, variable);
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeIn"})
    void shouldParseDeleteByAgeIn(String query) {

        Condition operator = Condition.IN;
        String variable = "age";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeNotIn"})
    void shouldParseDeleteByAgeNotIn(String query) {
        Condition operator = Condition.IN;
        String variable = "age";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeAndName"})
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
    void shouldParseDeleteByAgeBetween(String query) {

        Condition operator = Condition.BETWEEN;
        String entity = "entity";
        DeleteQuery deleteQuery = queryProvider.apply(query, entity);
        assertNotNull(deleteQuery);
        assertEquals(entity, deleteQuery.entity());
        assertTrue(deleteQuery.fields().isEmpty());
        Optional<Where> where = deleteQuery.where();
        assertTrue(where.isPresent());
        QueryCondition condition = where.get().condition();
        QueryValue<?> value = condition.value();
        assertEquals(operator, condition.condition());
        QueryValue<?>[] values = MethodArrayValue.class.cast(value).get();
        ParamQueryValue param1 = (ParamQueryValue) values[0];
        ParamQueryValue param2 = (ParamQueryValue) values[1];
        assertNotEquals(param2.get(), param1.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeNotBetween"})
    void shouldParseDeleteByAgeNotBetween(String query) {

        String entity = "entity";
        DeleteQuery deleteQuery = queryProvider.apply(query, entity);
        assertNotNull(deleteQuery);
        assertEquals(entity, deleteQuery.entity());
        assertTrue(deleteQuery.fields().isEmpty());
        Optional<Where> where = deleteQuery.where();
        assertTrue(where.isPresent());
        QueryCondition condition = where.get().condition();
        QueryValue<?> value = condition.value();
        assertEquals(Condition.NOT, condition.condition());
        QueryCondition notCondition =  ConditionQueryValue.class.cast(value).get().get(0);
        assertEquals(Condition.BETWEEN, notCondition.condition());

        QueryValue<?>[] values = MethodArrayValue.class.cast(notCondition.value()).get();
        ParamQueryValue param1 = (ParamQueryValue) values[0];
        ParamQueryValue param2 = (ParamQueryValue) values[1];
        assertNotEquals(param2.get(), param1.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteBySalary_Currency"})
    void shouldParseDeleteBySalaryCurrency(String query) {
        String entity = "entity";
        DeleteQuery deleteQuery = queryProvider.apply(query, entity);
        assertNotNull(deleteQuery);
        assertEquals(entity, deleteQuery.entity());
        assertTrue(deleteQuery.fields().isEmpty());
        Optional<Where> where = deleteQuery.where();
        assertTrue(where.isPresent());
        QueryCondition condition = where.get().condition();
        Assertions.assertEquals("salary.currency", condition.name());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteBySalary_CurrencyAndCredential_Role"})
    void shouldParseDeleteBySalaryCurrencyAndCredentialRole(String query) {
        String entity = "entity";
        DeleteQuery deleteQuery = queryProvider.apply(query, entity);
        assertNotNull(deleteQuery);
        assertEquals(entity, deleteQuery.entity());
        assertTrue(deleteQuery.fields().isEmpty());
        Optional<Where> where = deleteQuery.where();
        assertTrue(where.isPresent());
        QueryCondition condition = where.get().condition();
        Assertions.assertEquals(Condition.AND, condition.condition());
        final QueryValue<?> value = condition.value();
        QueryCondition condition1 = ConditionQueryValue.class.cast(value).get().get(0);
        QueryCondition condition2 = ConditionQueryValue.class.cast(value).get().get(1);
        assertEquals("salary.currency", condition1.name());
        assertEquals("credential.role", condition2.name());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteBySalary_CurrencyAndName"})
    void shouldParseDeleteBySalaryCurrencyAndName(String query) {
        String entity = "entity";
        DeleteQuery deleteQuery = queryProvider.apply(query, entity);
        assertNotNull(deleteQuery);
        assertEquals(entity, deleteQuery.entity());
        assertTrue(deleteQuery.fields().isEmpty());
        Optional<Where> where = deleteQuery.where();
        assertTrue(where.isPresent());
        QueryCondition condition = where.get().condition();
        Assertions.assertEquals(Condition.AND, condition.condition());
        final QueryValue<?> value = condition.value();
        QueryCondition condition1 = ConditionQueryValue.class.cast(value).get().get(0);
        QueryCondition condition2 = ConditionQueryValue.class.cast(value).get().get(1);
        assertEquals("salary.currency", condition1.name());
        assertEquals("name", condition2.name());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByActiveTrue"})
    void shouldParseDeleteByActiveTrue(String query) {
        String entity = "entity";
        DeleteQuery deleteQuery = queryProvider.apply(query, entity);
        assertNotNull(deleteQuery);
        assertEquals(entity, deleteQuery.entity());
        assertTrue(deleteQuery.fields().isEmpty());
        Optional<Where> where = deleteQuery.where();
        assertTrue(where.isPresent());
        QueryCondition condition = where.orElseThrow().condition();
        assertEquals("active", condition.name());
        assertEquals(Condition.EQUALS, condition.condition());
        assertEquals(BooleanQueryValue.TRUE, condition.value());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByActiveFalse"})
    void shouldParseDeleteByActiveFalse(String query) {
        String entity = "entity";
        DeleteQuery deleteQuery = queryProvider.apply(query, entity);
        assertNotNull(deleteQuery);
        assertEquals(entity, deleteQuery.entity());
        Optional<Where> where = deleteQuery.where();
        assertTrue(where.isPresent());
        QueryCondition condition = where.orElseThrow().condition();
        assertEquals("active", condition.name());
        assertEquals(Condition.EQUALS, condition.condition());
        assertEquals(BooleanQueryValue.FALSE, condition.value());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByNameContains"})
    void shouldParseDeleteByNameContains(String query) {
        Condition operator = Condition.CONTAINS;
        String variable = "name";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByNameEndsWith"})
    void shouldParseDeleteByNameEndsWith(String query) {
        Condition operator = Condition.ENDS_WITH;
        String variable = "name";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByNameStartsWith"})
    void shouldParseDeleteByNameStartsWith(String query) {
        Condition operator = Condition.STARTS_WITH;
        String variable = "name";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByNameNotContains"})
    void shouldParseDeleteByNameNotContains(String query) {
        Condition operator = Condition.CONTAINS;
        String variable = "name";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByNameNotEndsWith"})
    void shouldParseDeleteByNameNotEndsWith(String query) {
        Condition operator = Condition.ENDS_WITH;
        String variable = "name";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByNameNotStartsWith"})
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
    void shouldDeleteByStreetNameIgnoreCaseConditions(String query, String expectedProperty,
                                                    @ConvertWith(SelectMethodQueryProviderTest.ConditionConverter.class) Condition[] conditions) {
        checkConditions(query, expectedProperty, conditions);
    }

    private void checkConditions(String query, String variable, Condition... operators) {
        String entity = "entity";
        var selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        Optional<Where> where = selectQuery.where();
        assertTrue(where.isPresent());
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
        assertNotNull(deleteQuery);
        assertEquals(entity, deleteQuery.entity());
        assertTrue(deleteQuery.fields().isEmpty());
        Optional<Where> where = deleteQuery.where();
        assertTrue(where.isPresent());
        QueryCondition condition = where.get().condition();
        QueryValue<?> value = condition.value();
        assertEquals(operatorAppender, condition.condition());
        assertTrue(value instanceof ConditionQueryValue);
        QueryCondition condition1 = ConditionQueryValue.class.cast(value).get().get(0);
        QueryCondition condition2 = ConditionQueryValue.class.cast(value).get().get(1);

        assertEquals(operator, condition1.condition());
        QueryValue<?> param = condition1.value();
        assertEquals(operator, condition1.condition());
        assertTrue(ParamQueryValue.class.cast(param).get().contains(variable));

        assertEquals(operator2, condition2.condition());
        QueryValue<?> param2 = condition2.value();
        assertEquals(condition2.condition(), operator2);
        assertTrue(ParamQueryValue.class.cast(param2).get().contains(variable2));
    }


    private void checkNotCondition(String query, Condition operator, String variable) {
        String entity = "entity";
        DeleteQuery deleteQuery = queryProvider.apply(query, entity);
        assertNotNull(deleteQuery);
        assertEquals(entity, deleteQuery.entity());
        assertTrue(deleteQuery.fields().isEmpty());
        Optional<Where> where = deleteQuery.where();
        assertTrue(where.isPresent());
        QueryCondition condition = where.get().condition();
        QueryValue<?> value = condition.value();
        assertEquals(Condition.NOT, condition.condition());


        assertEquals("_NOT", condition.name());
        assertTrue(value instanceof ConditionQueryValue);
        QueryCondition condition1 = ConditionQueryValue.class.cast(value).get().get(0);
        QueryValue<?> param = condition1.value();
        assertEquals(operator, condition1.condition());
        assertTrue(ParamQueryValue.class.cast(param).get().contains(variable));
    }

    private void checkEqualsQuery(String query, String entity) {
        DeleteQuery deleteQuery = queryProvider.apply(query, entity);
        assertNotNull(deleteQuery);
        assertEquals(entity, deleteQuery.entity());
        assertTrue(deleteQuery.fields().isEmpty());
        Optional<Where> where = deleteQuery.where();
        assertTrue(where.isPresent());
        QueryCondition condition = where.get().condition();
        QueryValue<?> value = condition.value();
        assertEquals(Condition.EQUALS, condition.condition());
        assertEquals("name", condition.name());
        assertTrue(value instanceof ParamQueryValue);
        assertTrue(ParamQueryValue.class.cast(value).get().contains("name"));
    }

    private void checkCondition(String query, Condition operator, String variable) {
        String entity = "entity";
        DeleteQuery deleteQuery = queryProvider.apply(query, entity);
        assertNotNull(deleteQuery);
        assertEquals(entity, deleteQuery.entity());
        assertTrue(deleteQuery.fields().isEmpty());
        Optional<Where> where = deleteQuery.where();
        assertTrue(where.isPresent());
        QueryCondition condition = where.get().condition();
        QueryValue<?> value = condition.value();
        assertEquals(operator, condition.condition());
        assertTrue(ParamQueryValue.class.cast(value).get().contains(variable));
    }
}