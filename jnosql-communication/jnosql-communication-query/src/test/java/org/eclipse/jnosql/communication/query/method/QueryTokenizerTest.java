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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class QueryTokenizerTest {

    @Test
    void shouldReturnNullWhenQueryIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> QueryTokenizer.of(null));
    }

    @Test
    void shouldEquals() {
        QueryTokenizer query = QueryTokenizer.of("findByAge");
        Assertions.assertEquals(query, QueryTokenizer.of("findByAge"));
    }

    @Test
    void shouldHashCode() {
        QueryTokenizer query = QueryTokenizer.of("findByAge");
        Assertions.assertEquals(query.hashCode(), QueryTokenizer.of("findByAge").hashCode());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAge"})
    void shouldTokenizeSimpleMethodName(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By Age", queryTokenizer.get());
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByNameAndAge"})
    void shouldTokenizeMethodNameWithAndKeyword(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By Name And Age", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByNameOrAge"})
    void shouldRunQuery2(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By Name Or Age", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByNameOrAgeOrderByName"})
    void shouldTokenizeMethodNameWithOrKeyword(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By Name Or Age OrderBy Name", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByNameOrAgeOrderByNameAsc"})
    void shouldTokenizeMethodNameWithOrderByClause(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By Name Or Age OrderBy Name Asc", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"find ByNameOrAgeOrderByNameDesc"})
    void shouldTokenizeMethodNameWithOrderByDescendingClause(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By Name Or Age OrderBy Name Desc", queryTokenizer.get());
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByLastNameAndFirstName"})
    void shouldTokenizeMethodNameWithBetweenKeyword(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By LastName And FirstName", queryTokenizer.get());
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByLastNameOrFirstName"})
    void shouldTokenizeMethodNameWithGreaterThanKeyword(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By LastName Or FirstName", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByStartDateBetween"})
    void shouldTokenizeMethodNameWithGreaterThanEqualKeyword(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By StartDate Between", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeLessThan"})
    void shouldTokenizeMethodNameWithLessThanKeyword(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By Age LessThan", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeLessThanEqual"})
    void shouldTokenizeMethodNameWithLessThanEqualKeyword(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By Age LessThanEqual", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeGreaterThan"})
    void shouldTokenizeMethodNameWithLikeKeyword(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By Age GreaterThan", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeGreaterThanEqual"})
    void shouldTokenizeMethodNameWithStartingWithKeyword(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By Age GreaterThanEqual", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByFirstNameLike"})
    void shouldTokenizeMethodNameWithEndingWithKeyword(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By FirstName Like", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByFirstNameNotLike"})
    void shouldTokenizeMethodNameWithContainingKeyword(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By FirstName Not Like", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByFirstNameLikeOrderByNameAscAgeDesc"})
    void shouldTokenizeMethodNameWithNotKeyword(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By FirstName Like OrderBy Name Asc Age Desc", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByFirstNameLikeOrderByNameAscAge"})
    void shouldTokenizeMethodNameWithInKeyword(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By FirstName Like OrderBy Name Asc Age", queryTokenizer.get());
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAge"})
    void shouldTokenizeMethodNameWithNestedProperty(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("delete By Age", queryTokenizer.get());
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByNameAndAge"})
    void shouldTokenizeMethodNameWithMultipleNestedProperties(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("delete By Name And Age", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByNameOrAge"})
    void shouldTokenizeMethodNameWithSingleOrderBy(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("delete By Name Or Age", queryTokenizer.get());
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByLastNameAndFirstName"})
    void shouldTokenizeMethodNameWithMultipleOrderByFields(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("delete By LastName And FirstName", queryTokenizer.get());
    }


    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByLastNameOrFirstName"})
    void shouldTokenizeMethodNameWithBooleanProperty(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("delete By LastName Or FirstName", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByStartDateBetween"})
    void shouldTokenizeMethodNameWithEnumProperty(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("delete By StartDate Between", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeLessThan"})
    void shouldTokenizeMethodNameWithNumericProperty(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("delete By Age LessThan", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByAgeLessThanEqual"})
    void shouldTokenizeMethodNameWithStringProperty(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By Age LessThanEqual", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeGreaterThan"})
    void shouldTokenizeMethodNameWithMultipleParameters(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("delete By Age GreaterThan", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByAgeGreaterThanEqual"})
    void shouldTokenizeMethodNameWithRepeatedProperty(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("delete By Age GreaterThanEqual", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByFirstNameLike"})
    void shouldTokenizeCountMethodName(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("delete By FirstName Like", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteByFirstNameNotLike"})
    void shouldTokenizeExistsMethodName(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("delete By FirstName Not Like", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteBySalary_Currency"})
    void shouldTokenizeDeleteMethodName(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("delete By Salary_Currency", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteBySalary_CurrencyAndCredential_Role"})
    void shouldTokenizeRemoveMethodName(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("delete By Salary_Currency And Credential_Role", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"deleteBySalary_CurrencyAndName"})
    void shouldTokenizeDistinctMethodName(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("delete By Salary_Currency And Name", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findBySalary_Currency"})
    void shouldTokenizeTopKeywordInMethodName(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By Salary_Currency", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findBySalary_CurrencyAndCredential_Role"})
    void shouldTokenizeFirstKeywordInMethodName(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By Salary_Currency And Credential_Role", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findBySalary_CurrencyAndName"})
    void shouldTokenizeMethodNameWithLimitValue(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By Salary_Currency And Name", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findFirstByHexadecimalStartsWithAndIsControlOrderByIdAsc"})
    void shouldTokenizeMethodNameWithMultipleLimitValues(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find First By Hexadecimal StartsWith And IsControl OrderBy Id Asc", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findByFirstNameAndLastName"})
    void shouldTokenizeMethodNameWithCompositePropertyName(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find By FirstName And LastName", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"existByFirstNameAndLastName"})
    void shouldTokenizeMethodNameWithMultipleCompositeProperties(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("exist By FirstName And LastName", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"countByFirstNameAndLastName"})
    void shouldTokenizeCountMethodNameWithMultipleProperties(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("count By FirstName And LastName", queryTokenizer.get());
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"findFirst10ByAge"})
    void shouldTokenizeMethodNameWithFirstAndLimitKeyword(String query) {
        QueryTokenizer queryTokenizer = QueryTokenizer.of(query);
        assertNotNull(queryTokenizer);
        assertEquals("find First 10 By Age", queryTokenizer.get());
    }
}