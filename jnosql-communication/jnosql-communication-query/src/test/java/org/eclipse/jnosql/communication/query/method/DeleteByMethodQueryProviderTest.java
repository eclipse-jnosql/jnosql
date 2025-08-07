/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeleteByMethodQueryProviderTest {

    private final DeleteByMethodQueryParser queryProvider = new DeleteByMethodQueryParser();


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteBy"})
    void shouldReturnParserQuery(String query) {
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
    void shouldReturnParserQuery1(String query) {
        String entity = "entity";
        checkEqualsQuery(query, entity);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByNameEquals"})
    void shouldReturnParserQuery2(String query) {
        String entity = "entity";
        checkEqualsQuery(query, entity);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByNameNotEquals"})
    void shouldReturnParserQuery3(String query) {
        checkNotCondition(query, Condition.EQUALS, "name");
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeGreaterThan"})
    void shouldReturnParserQuery4(String query) {

        Condition operator = Condition.GREATER_THAN;
        String variable = "age";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeNotGreaterThan"})
    void shouldReturnParserQuery5(String query) {
        Condition operator = Condition.GREATER_THAN;
        String variable = "age";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeGreaterThanEqual"})
    void shouldReturnParserQuery6(String query) {

        Condition operator = Condition.GREATER_EQUALS_THAN;
        String variable = "age";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeNotGreaterThanEqual"})
    void shouldReturnParserQuery7(String query) {
        Condition operator = Condition.GREATER_EQUALS_THAN;
        String variable = "age";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeLessThan"})
    void shouldReturnParserQuery8(String query) {

        Condition operator = Condition.LESSER_THAN;
        String variable = "age";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeNotLessThan"})
    void shouldReturnParserQuery9(String query) {
        Condition operator = Condition.LESSER_THAN;
        String variable = "age";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeLessThanEqual"})
    void shouldReturnParserQuery10(String query) {

        Condition operator = Condition.LESSER_EQUALS_THAN;
        String variable = "age";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeNotLessThanEqual"})
    void shouldReturnParserQuery11(String query) {
        Condition operator = Condition.LESSER_EQUALS_THAN;
        String variable = "age";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeLike"})
    void shouldReturnParserQuery12(String query) {

        Condition operator = Condition.LIKE;
        String variable = "age";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeNotLike"})
    void shouldReturnParserQuery13(String query) {
        Condition operator = Condition.LIKE;
        String variable = "age";
        checkNotCondition(query, operator, variable);
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeIn"})
    void shouldReturnParserQuery14(String query) {

        Condition operator = Condition.IN;
        String variable = "age";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeNotIn"})
    void shouldReturnParserQuery15(String query) {
        Condition operator = Condition.IN;
        String variable = "age";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeAndName"})
    void shouldReturnParserQuery16(String query) {

        Condition operator = Condition.EQUALS;
        Condition operator2 = Condition.EQUALS;
        String variable = "age";
        String variable2 = "name";
        Condition operatorAppender = Condition.AND;
        checkAppendCondition(query, operator, operator2, variable, variable2, operatorAppender);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeOrName"})
    void shouldReturnParserQuery17(String query) {

        Condition operator = Condition.EQUALS;
        Condition operator2 = Condition.EQUALS;
        String variable = "age";
        String variable2 = "name";
        Condition operatorAppender = Condition.OR;
        checkAppendCondition(query, operator, operator2, variable, variable2, operatorAppender);
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeOrNameLessThan"})
    void shouldReturnParserQuery18(String query) {

        Condition operator = Condition.EQUALS;
        Condition operator2 = Condition.LESSER_THAN;
        String variable = "age";
        String variable2 = "name";
        Condition operatorAppender = Condition.OR;
        checkAppendCondition(query, operator, operator2, variable, variable2, operatorAppender);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeGreaterThanOrNameIn"})
    void shouldReturnParserQuery19(String query) {

        Condition operator = Condition.GREATER_THAN;
        Condition operator2 = Condition.IN;
        String variable = "age";
        String variable2 = "name";
        Condition operatorAppender = Condition.OR;
        checkAppendCondition(query, operator, operator2, variable, variable2, operatorAppender);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeBetween"})
    void shouldReturnParserQuery27(String query) {

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
    void shouldReturnParserQuery28(String query) {

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
        QueryCondition notCondition =  ConditionQueryValue.class.cast(value).get().getFirst();
        assertEquals(Condition.BETWEEN, notCondition.condition());

        QueryValue<?>[] values = MethodArrayValue.class.cast(notCondition.value()).get();
        ParamQueryValue param1 = (ParamQueryValue) values[0];
        ParamQueryValue param2 = (ParamQueryValue) values[1];
        assertNotEquals(param2.get(), param1.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteBySalary_Currency"})
    void shouldRunQuery29(String query) {
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
    void shouldRunQuery30(String query) {
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
    void shouldRunQuery31(String query) {
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
    void shouldRunQuery32(String query) {
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
    void shouldRunQuery33(String query) {
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
    void shouldRunQuery34(String query) {
        Condition operator = Condition.CONTAINS;
        String variable = "name";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByNameEndsWith"})
    void shouldRunQuery35(String query) {
        Condition operator = Condition.ENDS_WITH;
        String variable = "name";
        checkCondition(query, operator, variable);
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
        QueryCondition condition1 = ConditionQueryValue.class.cast(value).get().getFirst();
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