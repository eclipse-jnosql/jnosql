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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectMethodQueryProviderTest {

    private final SelectMethodQueryParser queryProvider = new SelectMethodQueryParser();


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findBy", "existsBy"})
    void shouldReturnParserQuery(String query) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        assertTrue(selectQuery.orderBy().isEmpty());
        assertFalse(selectQuery.isCount());
        assertEquals(0, selectQuery.limit());
        assertEquals(0, selectQuery.skip());
        Optional<Where> where = selectQuery.where();
        assertFalse(where.isPresent());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"countBy", "countAll"})
    void shouldReturnParsedCountableQuery(String query) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        assertTrue(selectQuery.orderBy().isEmpty());
        assertTrue(selectQuery.isCount());
        assertEquals(0, selectQuery.limit());
        assertEquals(0, selectQuery.skip());
        Optional<Where> where = selectQuery.where();
        assertFalse(where.isPresent());
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findFirst10By"})
    void shouldFindFirstTenLimit(String query) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        assertTrue(selectQuery.orderBy().isEmpty());
        assertEquals(10, selectQuery.limit());
        assertEquals(0, selectQuery.skip());
        Optional<Where> where = selectQuery.where();
        assertFalse(where.isPresent());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findFirstBy"})
    void shouldFindFirstLimit(String query) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        assertTrue(selectQuery.orderBy().isEmpty());
        assertEquals(1, selectQuery.limit());
        assertEquals(0, selectQuery.skip());
        Optional<Where> where = selectQuery.where();
        assertFalse(where.isPresent());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByName", "countByName", "existsByName"})
    void shouldReturnParserQuery1(String query) {
        String entity = "entity";
        checkEqualsQuery(query, entity);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByNameEquals", "countByNameEquals", "existsByNameEquals"})
    void shouldReturnParserQuery2(String query) {
        String entity = "entity";
        checkEqualsQuery(query, entity);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByNameNotEquals", "countByNameNotEquals", "existsByNameNotEquals"})
    void shouldReturnParserQuery3(String query) {
        checkNotCondition(query, Condition.EQUALS, "name");
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeGreaterThan", "countByAgeGreaterThan", "existsByAgeGreaterThan"})
    void shouldReturnParserQuery4(String query) {

        Condition operator = Condition.GREATER_THAN;
        String variable = "age";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeNotGreaterThan", "countByAgeNotGreaterThan", "existsByAgeNotGreaterThan"})
    void shouldReturnParserQuery5(String query) {
        Condition operator = Condition.GREATER_THAN;
        String variable = "age";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeGreaterThanEqual", "countByAgeGreaterThanEqual", "existsByAgeGreaterThanEqual"})
    void shouldReturnParserQuery6(String query) {

        Condition operator = Condition.GREATER_EQUALS_THAN;
        String variable = "age";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeNotGreaterThanEqual", "countByAgeNotGreaterThanEqual", "existsByAgeNotGreaterThanEqual"})
    void shouldReturnParserQuery7(String query) {
        Condition operator = Condition.GREATER_EQUALS_THAN;
        String variable = "age";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeLessThan", "countByAgeLessThan", "existsByAgeLessThan"})
    void shouldReturnParserQuery8(String query) {

        Condition operator = Condition.LESSER_THAN;
        String variable = "age";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeNotLessThan", "countByAgeNotLessThan", "existsByAgeNotLessThan"})
    void shouldReturnParserQuery9(String query) {
        Condition operator = Condition.LESSER_THAN;
        String variable = "age";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeLessThanEqual", "countByAgeLessThanEqual", "existsByAgeLessThanEqual"})
    void shouldReturnParserQuery10(String query) {

        Condition operator = Condition.LESSER_EQUALS_THAN;
        String variable = "age";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeNotLessThanEqual", "countByAgeNotLessThanEqual", "existsByAgeNotLessThanEqual"})
    void shouldReturnParserQuery11(String query) {
        Condition operator = Condition.LESSER_EQUALS_THAN;
        String variable = "age";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeLike", "countByAgeLike", "existsByAgeLike"})
    void shouldReturnParserQuery12(String query) {

        Condition operator = Condition.LIKE;
        String variable = "age";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeNotLike", "countByAgeNotLike", "existsByAgeNotLike"})
    void shouldReturnParserQuery13(String query) {
        Condition operator = Condition.LIKE;
        String variable = "age";
        checkNotCondition(query, operator, variable);
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeIn", "countByAgeIn", "existsByAgeIn"})
    void shouldReturnParserQuery14(String query) {

        Condition operator = Condition.IN;
        String variable = "age";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeNotIn", "countByAgeNotIn", "existsByAgeNotIn"})
    void shouldReturnParserQuery15(String query) {
        Condition operator = Condition.IN;
        String variable = "age";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeAndName", "countByAgeAndName", "existsByAgeAndName"})
    void shouldReturnParserQuery16(String query) {

        Condition operator = Condition.EQUALS;
        Condition operator2 = Condition.EQUALS;
        String variable = "age";
        String variable2 = "name";
        Condition operatorAppender = Condition.AND;
        checkAppendCondition(query, operator, operator2, variable, variable2, operatorAppender);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeOrName", "countByAgeOrName", "existsByAgeOrName"})
    void shouldReturnParserQuery17(String query) {

        Condition operator = Condition.EQUALS;
        Condition operator2 = Condition.EQUALS;
        String variable = "age";
        String variable2 = "name";
        Condition operatorAppender = Condition.OR;
        checkAppendCondition(query, operator, operator2, variable, variable2, operatorAppender);
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeOrNameLessThan", "countByAgeOrNameLessThan", "existsByAgeOrNameLessThan"})
    void shouldReturnParserQuery18(String query) {

        Condition operator = Condition.EQUALS;
        Condition operator2 = Condition.LESSER_THAN;
        String variable = "age";
        String variable2 = "name";
        Condition operatorAppender = Condition.OR;
        checkAppendCondition(query, operator, operator2, variable, variable2, operatorAppender);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeGreaterThanOrNameIn", "countByAgeGreaterThanOrNameIn", "existsByAgeGreaterThanOrNameIn"})
    void shouldReturnParserQuery19(String query) {

        Condition operator = Condition.GREATER_THAN;
        Condition operator2 = Condition.IN;
        String variable = "age";
        String variable2 = "name";
        Condition operatorAppender = Condition.OR;
        checkAppendCondition(query, operator, operator2, variable, variable2, operatorAppender);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByOrderByName", "countByOrderByName", "existsByOrderByName"})
    void shouldReturnParserQuery20(String query) {
        checkOrderBy(query, Direction.ASC);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByOrderByNameAsc", "countByOrderByNameAsc", "existsByOrderByNameAsc"})
    void shouldReturnParserQuery21(String query) {
        Direction type = Direction.ASC;
        checkOrderBy(query, type);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByOrderByNameDesc", "countByOrderByNameDesc", "existsByOrderByNameDesc"})
    void shouldReturnParserQuery22(String query) {
        checkOrderBy(query, Direction.DESC);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByOrderByNameDescAgeAsc", "countByOrderByNameDescAgeAsc", "existsByOrderByNameDescAgeAsc"})
    void shouldReturnParserQuery23(String query) {

        Direction type = Direction.DESC;
        Direction type2 = Direction.ASC;
        checkOrderBy(query, type, type2);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByOrderByNameDescAge", "countByOrderByNameDescAge", "existsByOrderByNameDescAge"})
    void shouldReturnParserQuery24(String query) {
        checkOrderBy(query, Direction.DESC, Direction.ASC);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByOrderByNameDescAgeDesc", "countByOrderByNameDescAgeDesc", "existsByOrderByNameDescAgeDesc"})
    void shouldReturnParserQuery25(String query) {
        checkOrderBy(query, Direction.DESC, Direction.DESC);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByOrderByNameAscAgeAsc", "countByOrderByNameAscAgeAsc", "existsByOrderByNameAscAgeAsc"})
    void shouldReturnParserQuery26(String query) {
        checkOrderBy(query, Direction.ASC, Direction.ASC);
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeBetween", "countByAgeBetween", "existsByAgeBetween"})
    void shouldReturnParserQuery27(String query) {

        Condition operator = Condition.BETWEEN;
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        assertTrue(selectQuery.orderBy().isEmpty());
        assertEquals(0, selectQuery.limit());
        assertEquals(0, selectQuery.skip());
        Optional<Where> where = selectQuery.where();
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
    @ValueSource(strings = {"findByAgeNotBetween", "countByAgeNotBetween", "existsByAgeNotBetween"})
    void shouldReturnParserQuery28(String query) {

        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        assertTrue(selectQuery.orderBy().isEmpty());
        assertEquals(0, selectQuery.limit());
        assertEquals(0, selectQuery.skip());
        Optional<Where> where = selectQuery.where();
        assertTrue(where.isPresent());
        QueryCondition condition = where.get().condition();
        QueryValue<?> value = condition.value();
        assertEquals(Condition.NOT, condition.condition());
        QueryCondition notCondition = ConditionQueryValue.class.cast(value).get().get(0);
        assertEquals(Condition.BETWEEN, notCondition.condition());

        QueryValue<?>[] values = MethodArrayValue.class.cast(notCondition.value()).get();
        ParamQueryValue param1 = (ParamQueryValue) values[0];
        ParamQueryValue param2 = (ParamQueryValue) values[1];
        assertNotEquals(param2.get(), param1.get());
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findBySalary_Currency", "countBySalary_Currency", "existsBySalary_Currency"})
    void shouldRunQuery29(String query) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        assertTrue(selectQuery.orderBy().isEmpty());
        assertEquals(0, selectQuery.limit());
        assertEquals(0, selectQuery.skip());
        Optional<Where> where = selectQuery.where();
        assertTrue(where.isPresent());
        assertTrue(where.isPresent());
        QueryCondition condition = where.get().condition();
        Assertions.assertEquals(Condition.EQUALS, condition.condition());
        assertEquals("salary.currency", condition.name());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findBySalary_CurrencyAndCredential_Role", "countBySalary_CurrencyAndCredential_Role",
            "existsBySalary_CurrencyAndCredential_Role"})
    void shouldRunQuery30(String query) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        assertTrue(selectQuery.orderBy().isEmpty());
        assertEquals(0, selectQuery.limit());
        assertEquals(0, selectQuery.skip());
        Optional<Where> where = selectQuery.where();
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
    @ValueSource(strings = {"findBySalary_CurrencyAndName", "countBySalary_CurrencyAndName",
            "existsBySalary_CurrencyAndName"})
    void shouldRunQuery31(String query) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        assertTrue(selectQuery.orderBy().isEmpty());
        assertEquals(0, selectQuery.limit());
        assertEquals(0, selectQuery.skip());
        Optional<Where> where = selectQuery.where();
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
    @ValueSource(strings = {"findBySalary_CurrencyOrderBySalary_Value", "countBySalary_CurrencyOrderBySalary_Value"
    ,"existsBySalary_CurrencyOrderBySalary_Value"})
    void shouldRunQuery32(String query) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        assertFalse(selectQuery.orderBy().isEmpty());
        assertEquals(0, selectQuery.limit());
        assertEquals(0, selectQuery.skip());
        Optional<Where> where = selectQuery.where();
        assertTrue(where.isPresent());
        QueryCondition condition = where.get().condition();
        Assertions.assertEquals(Condition.EQUALS, condition.condition());
        assertEquals("salary.currency", condition.name());

        final Sort sort = selectQuery.orderBy().get(0);
        Assertions.assertEquals("salary.value", sort.property());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByActiveTrue", "countByActiveTrue", "existsByActiveTrue"})
    void shouldRunQuery33(String query) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        assertTrue(selectQuery.orderBy().isEmpty());
        assertEquals(0, selectQuery.limit());
        assertEquals(0, selectQuery.skip());
        Optional<Where> where = selectQuery.where();
        assertTrue(where.isPresent());
        QueryCondition condition = where.orElseThrow().condition();
        assertEquals("active", condition.name());
        assertEquals(Condition.EQUALS, condition.condition());
        assertEquals(BooleanQueryValue.TRUE, condition.value());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByActiveFalse", "countByActiveFalse", "existsByActiveFalse"})
    void shouldRunQuery34(String query) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        assertTrue(selectQuery.orderBy().isEmpty());
        assertEquals(0, selectQuery.limit());
        assertEquals(0, selectQuery.skip());
        Optional<Where> where = selectQuery.where();
        assertTrue(where.isPresent());
        QueryCondition condition = where.orElseThrow().condition();
        assertEquals("active", condition.name());
        assertEquals(Condition.EQUALS, condition.condition());
        assertEquals(BooleanQueryValue.FALSE, condition.value());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByNameNot", "countByNameNot", "existsByNameNot"})
    void shouldReturnParserQuery35(String query) {
        checkNotCondition(query, Condition.EQUALS, "name");
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByNameNotNull", "countByNameNotNull", "existsByNameNotNull"})
    void shouldReturnParserQuery36(String query) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        assertTrue(selectQuery.orderBy().isEmpty());
        assertEquals(0, selectQuery.limit());
        assertEquals(0, selectQuery.skip());
        Optional<Where> where = selectQuery.where();
        assertTrue(where.isPresent());
        QueryCondition condition = where.get().condition();
        QueryValue<?> value = condition.value();
        assertEquals(Condition.NOT, condition.condition());


        assertEquals("_NOT", condition.name());
        assertTrue(value instanceof ConditionQueryValue);
        QueryCondition condition1 = ConditionQueryValue.class.cast(value).get().get(0);

        assertEquals("name", condition1.name());
        assertEquals(Condition.EQUALS, condition1.condition());
        var param = condition1.value();
        assertNull(StringQueryValue.class.cast(param).get());

    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByNameNull", "countByNameNull", "existsByNameNull"})
    void shouldReturnParserQuery37(String query) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        assertTrue(selectQuery.orderBy().isEmpty());
        assertEquals(0, selectQuery.limit());
        assertEquals(0, selectQuery.skip());
        Optional<Where> where = selectQuery.where();
        assertTrue(where.isPresent());
        QueryCondition condition = where.get().condition();
        assertEquals("name", condition.name());
        assertEquals(Condition.EQUALS, condition.condition());
        assertNull(condition.value().get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByStreetNameIgnoreCaseLike", "findByHexadecimalIgnoreCase"})
    void shouldReturnUnsupportedOperationExceptionQuery(String query) {
        String entity = "entity";
        queryProvider.apply(query, entity);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> queryProvider.apply(query, entity));
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByStreetNameIgnoreCaseNotLike", "findByHexadecimalIgnoreCaseNot"})
    void shouldReturnUnsupportedOperationExceptionQueryWithNegation(String query) {
        String entity = "entity";
        Assertions.assertThrows(UnsupportedOperationException.class, () -> queryProvider.apply(query, entity));
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByIdBetweenOrderByNumTypeOrdinalAsc"})
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
    void shouldFindByContains(String query) {
        Condition operator = Condition.CONTAINS;
        String variable = "name";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByNameStartsWith"})
    void shouldFindByStartWith(String query) {
        Condition operator = Condition.STARTS_WITH;
        String variable = "name";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByNameEndsWith"})
    void shouldFindByEndsWith(String query) {
        Condition operator = Condition.ENDS_WITH;
        String variable = "name";
        checkCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByNameNotContains"})
    void shouldFindByNotContains(String query) {
        Condition operator = Condition.CONTAINS;
        String variable = "name";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByNameNotStartsWith"})
    void shouldFindByNotStartWith(String query) {
        Condition operator = Condition.STARTS_WITH;
        String variable = "name";
        checkNotCondition(query, operator, variable);
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByNameNotEndsWith"})
    void shouldFindByNotEndsWith(String query) {
        Condition operator = Condition.ENDS_WITH;
        String variable = "name";
        checkNotCondition(query, operator, variable);
    }

    private void checkOrderBy(String query, Direction direction, Direction direction2) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        List<Sort<?>> sorts = selectQuery.orderBy();

        assertEquals(2, sorts.size());
        Sort<?> sort = sorts.get(0);
        assertEquals("name", sort.property());
        assertEquals(direction, sort.isAscending() ? Direction.ASC : Direction.DESC);

        Sort<?> sort2 = sorts.get(1);
        assertEquals("age", sort2.property());

        assertEquals(direction2, sort2.isAscending() ? Direction.ASC : Direction.DESC);
    }

    private void checkOrderBy(String query, Direction type) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        List<Sort<?>> sorts = selectQuery.orderBy();

        assertEquals(1, sorts.size());
        Sort<?> sort = sorts.get(0);
        assertEquals("name", sort.property());
        assertEquals(type, sort.isAscending() ? Direction.ASC : Direction.DESC);
    }


    private void checkAppendCondition(String query, Condition operator, Condition operator2, String variable,
                                      String variable2, Condition operatorAppender) {
        String entity = "entity";
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        assertTrue(selectQuery.orderBy().isEmpty());
        assertEquals(0, selectQuery.limit());
        assertEquals(0, selectQuery.skip());
        Optional<Where> where = selectQuery.where();
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
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        assertTrue(selectQuery.orderBy().isEmpty());
        assertEquals(0, selectQuery.limit());
        assertEquals(0, selectQuery.skip());
        Optional<Where> where = selectQuery.where();
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
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        assertTrue(selectQuery.orderBy().isEmpty());
        assertEquals(0, selectQuery.limit());
        assertEquals(0, selectQuery.skip());
        Optional<Where> where = selectQuery.where();
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
        SelectQuery selectQuery = queryProvider.apply(query, entity);
        assertNotNull(selectQuery);
        assertEquals(entity, selectQuery.entity());
        assertTrue(selectQuery.fields().isEmpty());
        assertTrue(selectQuery.orderBy().isEmpty());
        assertEquals(0, selectQuery.limit());
        assertEquals(0, selectQuery.skip());
        Optional<Where> where = selectQuery.where();
        assertTrue(where.isPresent());
        QueryCondition condition = where.get().condition();
        QueryValue<?> value = condition.value();
        assertEquals(operator, condition.condition());
        assertTrue(ParamQueryValue.class.cast(value).get().contains(variable));
    }
}
