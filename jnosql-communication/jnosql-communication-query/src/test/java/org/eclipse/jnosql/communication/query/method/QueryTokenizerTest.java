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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@DisplayName("QueryTokenizer")
class QueryTokenizerTest {

    @Nested
    @DisplayName("WhenTheQueryIsMissing")
    class WhenTheQueryIsMissing {

        @Test
        @DisplayName("Should reject a null query")
        void shouldRejectNullQuery() {
            assertThatNullPointerException().isThrownBy(() -> QueryTokenizer.of(null));
        }
    }

    @Nested
    @DisplayName("WhenTheTokenizerIsCompared")
    class WhenTheTokenizerIsCompared {

        @Test
        @DisplayName("Should be equal for the same query")
        void shouldBeEqualForSameQuery() {
            QueryTokenizer query = QueryTokenizer.of("findByAge");

            assertThat(query).isEqualTo(QueryTokenizer.of("findByAge"));
        }

        @Test
        @DisplayName("Should have the same hash code for the same query")
        void shouldHaveSameHashCodeForSameQuery() {
            QueryTokenizer query = QueryTokenizer.of("findByAge");

            assertThat(query).hasSameHashCodeAs(QueryTokenizer.of("findByAge"));
        }
    }

    @Nested
    @DisplayName("WhenTheMethodNameIsTokenized")
    class WhenTheMethodNameIsTokenized {

        @ParameterizedTest(name = "Should tokenize {0}")
        @DisplayName("Should tokenize method query keywords and property names")
        @CsvSource(delimiter = '|', textBlock = """
                findByAge                                      | find By Age
                findByNameAndAge                               | find By Name And Age
                findByNameOrAge                                | find By Name Or Age
                findByNameOrAgeOrderByName                     | find By Name Or Age OrderBy Name
                findByNameOrAgeOrderByNameAsc                  | find By Name Or Age OrderBy Name Asc
                find ByNameOrAgeOrderByNameDesc                | find By Name Or Age OrderBy Name Desc
                findByLastNameAndFirstName                     | find By LastName And FirstName
                findByLastNameOrFirstName                      | find By LastName Or FirstName
                findByStartDateBetween                         | find By StartDate Between
                findByAgeLessThan                              | find By Age LessThan
                findByAgeLessThanEqual                         | find By Age LessThanEqual
                findByAgeGreaterThan                           | find By Age GreaterThan
                findByAgeGreaterThanEqual                      | find By Age GreaterThanEqual
                findByFirstNameLike                            | find By FirstName Like
                findByFirstNameNotLike                         | find By FirstName Not Like
                findByFirstNameLikeOrderByNameAscAgeDesc       | find By FirstName Like OrderBy Name Asc Age Desc
                findByFirstNameLikeOrderByNameAscAge           | find By FirstName Like OrderBy Name Asc Age
                deleteByAge                                    | delete By Age
                deleteByNameAndAge                             | delete By Name And Age
                deleteByNameOrAge                              | delete By Name Or Age
                deleteByLastNameAndFirstName                   | delete By LastName And FirstName
                deleteByLastNameOrFirstName                    | delete By LastName Or FirstName
                deleteByStartDateBetween                       | delete By StartDate Between
                deleteByAgeLessThan                            | delete By Age LessThan
                deleteByAgeGreaterThan                         | delete By Age GreaterThan
                deleteByAgeGreaterThanEqual                    | delete By Age GreaterThanEqual
                deleteByFirstNameLike                          | delete By FirstName Like
                deleteByFirstNameNotLike                       | delete By FirstName Not Like
                deleteBySalary_Currency                        | delete By Salary_Currency
                deleteBySalary_CurrencyAndCredential_Role      | delete By Salary_Currency And Credential_Role
                deleteBySalary_CurrencyAndName                 | delete By Salary_Currency And Name
                findBySalary_Currency                          | find By Salary_Currency
                findBySalary_CurrencyAndCredential_Role        | find By Salary_Currency And Credential_Role
                findBySalary_CurrencyAndName                   | find By Salary_Currency And Name
                findFirstByHexadecimalStartsWithAndIsControlOrderByIdAsc | find First By Hexadecimal StartsWith And IsControl OrderBy Id Asc
                findByFirstNameAndLastName                     | find By FirstName And LastName
                existByFirstNameAndLastName                    | exist By FirstName And LastName
                countByFirstNameAndLastName                    | count By FirstName And LastName
                findFirst10ByAge                               | find First 10 By Age
                """)
        void shouldTokenizeMethodQuery(String query, String expected) {
            QueryTokenizer queryTokenizer = QueryTokenizer.of(query);

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(queryTokenizer).isNotNull();
                soft.assertThat(queryTokenizer.get()).isEqualTo(expected);
            });
        }
    }
}
