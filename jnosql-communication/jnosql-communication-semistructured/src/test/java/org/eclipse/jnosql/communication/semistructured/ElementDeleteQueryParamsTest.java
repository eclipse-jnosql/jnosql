/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.Params;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElementDeleteQueryParamsTest {


    @Nested
    @DisplayName("When the element delete query params is used")
    class WhenTheElementDeleteQueryParamsIsUsed {

        @DisplayName("Should Instantiate Successfully")
        @ParameterizedTest
        @MethodSource("scenarios")
        void shouldInstantiateSuccessfully(DeleteQuery query, Params params) {
            DeleteQueryParams target = newInstance(query, params);
            assertThat(target).isNotNull();
        }

        @DisplayName("Should Return The Same Query Instance")
        @ParameterizedTest
        @MethodSource("scenarios")
        void shouldReturnTheSameQueryInstance(DeleteQuery expectedQuery, Params params) {
            var target = newInstance(expectedQuery, params);
            assertThat(expectedQuery).isSameAs(target.query());
        }

        @DisplayName("Should Return The Same Params Instance")
        @ParameterizedTest
        @MethodSource("scenarios")
        void shouldReturnTheSameParamsInstance(DeleteQuery query, Params expectedParams) {
            var target = newInstance(query, expectedParams);
            assertThat(expectedParams).isSameAs(target.params());
        }

        @DisplayName("Should Be Not Equals To Null")
        @ParameterizedTest
        @MethodSource("scenarios")
        void shouldBeNotEqualsToNull(DeleteQuery query, Params params) {
            var instance = newInstance(query, params);
            assertThat(instance).isNotEqualTo(null);
        }


        @DisplayName("Should Be Not Equals To Any Other Instance Of Different Type")
        @ParameterizedTest
        @MethodSource("scenarios")
        void shouldBeNotEqualsToAnyOtherInstanceOfDifferentType(DeleteQuery query, Params params) {
            var instance = newInstance(query, params);
            assertThat(instance).isNotEqualTo(new Object());
        }

        @DisplayName("Should Be Equals To Itself")
        @ParameterizedTest
        @MethodSource("scenarios")
        void shouldBeEqualsToItself(DeleteQuery query, Params params) {
            var instance = newInstance(query, params);
            assertThat(instance).isEqualTo(instance);
        }

        @DisplayName("Should Be Equals When Query And Params Are Used By Two Different Instances")
        @Test
        void shouldBeEqualsWhenQueryAndParamsAreUsedByTwoDifferentInstances() {
            var query = newDummyColumnDeleteQuery();
            var params = newDummyParams();

            var leftInstance = newInstance(query, params);
            var rightInstance = newInstance(query, params);

            assertThat(leftInstance).isEqualTo(rightInstance);
        }

        @DisplayName("Should Be Not Equals When Different Query And Params Are Used By Two Different Instances")
        @ParameterizedTest
        @MethodSource("scenarios")
        void shouldBeNotEqualsWhenDifferentQueryAndParamsAreUsedByTwoDifferentInstances(DeleteQuery query, Params params) {

            var leftInstance = newInstance(query, params);
            var rightInstance = newInstance(newDummyColumnDeleteQuery(), newDummyParams());

            assertThat(leftInstance).isNotEqualTo(rightInstance);
        }

        @DisplayName("Should Hash Code Be Conditioned To Query And Params Attributes")
        @Test
        void shouldHashCodeBeConditionedToQueryAndParamsAttributes() {

            DeleteQuery firstQuery = newDummyColumnDeleteQuery();
            Params firstParams = newDummyParams();

            var fistInstance = newInstance(firstQuery, firstParams);
            var secondInstance = newInstance(firstQuery, firstParams);

            assertThat(fistInstance).hasSameHashCodeAs(secondInstance);

            DeleteQuery secondQuery = newDummyColumnDeleteQuery();
            Params secondParams = newDummyParams();

            var thirdInstance = newInstance(secondQuery, secondParams);

            assertThat(fistInstance.hashCode()).isNotEqualTo(thirdInstance.hashCode());

        }

        @DisplayName("Should Delete Using Condition")
        @Test
        void shouldDeleteUsingCondition() {
            DeleteQuery.DeleteQueryBuilder builder = new DefaultDeleteQueryBuilder();
            DeleteQuery deleteQuery = builder.from("entity")
                    .where(CriteriaCondition.of(Element.of("field", "value"), Condition.EQUALS)).build();

            SoftAssertions.assertSoftly(soft-> {
                soft.assertThat(deleteQuery.columns()).isEmpty();
                soft.assertThat(deleteQuery.name()).isEqualTo("entity");
                soft.assertThat(deleteQuery.condition()).isNotEmpty();
                var condition = deleteQuery.condition().orElseThrow();
                soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
                soft.assertThat(condition.element()).isEqualTo(Element.of("field", "value"));
            });
        }

        @DisplayName("Should Delete Columns Using Condition")
        @Test
        void shouldDeleteColumnsUsingCondition() {
            DeleteQuery.DeleteQueryBuilder builder = new DefaultDeleteQueryBuilder();
            DeleteQuery deleteQuery = builder.from("entity").delete("field", "field2")
                    .where(CriteriaCondition.of(Element.of("field", "value"), Condition.EQUALS)).build();

            SoftAssertions.assertSoftly(soft-> {
                soft.assertThat(deleteQuery.columns()).isNotEmpty().hasSize(2).contains("field", "field2");
                soft.assertThat(deleteQuery.name()).isEqualTo("entity");
                soft.assertThat(deleteQuery.condition()).isNotEmpty();
                var condition = deleteQuery.condition().orElseThrow();
                soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
                soft.assertThat(condition.element()).isEqualTo(Element.of("field", "value"));
            });

        }

        @DisplayName("Should To String")
        @Test
        void shouldToString() {
            DeleteQuery.DeleteQueryBuilder builder = new DefaultDeleteQueryBuilder();
            assertThat(builder.toString()).isNotBlank().isNotNull();
        }

        @DisplayName("Should To Hash Code")
        @Test
        void shouldToHashCode() {
            DeleteQuery.DeleteQueryBuilder builder = new DefaultDeleteQueryBuilder();
            DeleteQuery.DeleteQueryBuilder builder2 = new DefaultDeleteQueryBuilder();
            assertThat(builder.hashCode()).isEqualTo(builder2.hashCode());
        }

        @DisplayName("Should Equals")
        @Test
        void shouldEquals() {
            DeleteQuery.DeleteQueryBuilder builder = new DefaultDeleteQueryBuilder();
            DeleteQuery.DeleteQueryBuilder builder2 = new DefaultDeleteQueryBuilder();
            DeleteQuery.DeleteQueryBuilder builder3 = new DefaultDeleteQueryBuilder().delete("field", "field2");

            SoftAssertions.assertSoftly(soft-> {
               soft.assertThat(builder).isEqualTo(builder2);
               soft.assertThat(builder).isNotEqualTo(builder3);
               soft.assertThat(builder2).isEqualTo(builder);
                soft.assertThat(builder).isEqualTo(builder2);
                soft.assertThat(builder).isEqualTo(builder);
                soft.assertThat(builder).isNotEqualTo(null);
                soft.assertThat(builder).isNotEqualTo("234");
            });
        }

        @DisplayName("Should Get Issue When Not Entity")
        @Test
        void shouldGetIssueWhenNotEntity() {
            DeleteQuery.DeleteQueryBuilder builder = new DefaultDeleteQueryBuilder();
            assertThatThrownBy(builder::build).isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("Should Execute")
        @Test
        void shouldExecute() {
            DatabaseManager manager = Mockito.mock(DatabaseManager.class);
            new DefaultDeleteQueryBuilder().delete("field", "field2")
                    .from("entity").delete(manager);

            Mockito.verify(manager).delete(Mockito.any(DeleteQuery.class));
        }

        static Stream<Arguments> scenarios() {
            return Stream.of(
                    givenNullArguments(),
                    givenColumnDeleteQueryOnly(),
                    givenParamsOnly(),
                    givenValidArguments()
            );
        }

        private DeleteQueryParams newInstance(DeleteQuery query, Params params) {
            return new DeleteQueryParams(query,params);
        }

        private static Params newDummyParams() {
            Params params = Params.newParams();
            params.add(UUID.randomUUID().toString());
            return params;
        }

        private static DeleteQuery newDummyColumnDeleteQuery() {
            return DeleteQuery.builder().from(UUID.randomUUID().toString()).build();
        }

        private static Arguments givenValidArguments() {
            return arguments(newDummyColumnDeleteQuery(), newDummyParams());
        }

        private static Arguments givenParamsOnly() {
            return arguments(null, newDummyParams());
        }

        private static Arguments givenColumnDeleteQueryOnly() {
            return arguments(newDummyColumnDeleteQuery(), null);
        }

        private static Arguments givenNullArguments() {
            return arguments(null, null);
        }
    }

}
