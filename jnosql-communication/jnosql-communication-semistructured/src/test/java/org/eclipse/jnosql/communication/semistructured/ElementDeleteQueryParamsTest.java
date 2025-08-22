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

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.Params;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ElementDeleteQueryParamsTest {

    @ParameterizedTest
    @MethodSource("scenarios")
    void shouldInstantiateSuccessfully(DeleteQuery query, Params params) {
        DeleteQueryParams target = newInstance(query, params);
        assertThat(target).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("scenarios")
    void shouldReturnTheSameQueryInstance(DeleteQuery expectedQuery, Params params) {
        var target = newInstance(expectedQuery, params);
        assertThat(expectedQuery).isSameAs(target.query());
    }

    @ParameterizedTest
    @MethodSource("scenarios")
    void shouldReturnTheSameParamsInstance(DeleteQuery query, Params expectedParams) {
        var target = newInstance(query, expectedParams);
        assertThat(expectedParams).isSameAs(target.params());
    }

    @ParameterizedTest
    @MethodSource("scenarios")
    void shouldBeNotEqualsToNull(DeleteQuery query, Params params) {
        var instance = newInstance(query, params);
        assertThat(instance).isNotEqualTo(null);
    }


    @ParameterizedTest
    @MethodSource("scenarios")
    void shouldBeNotEqualsToAnyOtherInstanceOfDifferentType(DeleteQuery query, Params params) {
        var instance = newInstance(query, params);
        assertThat(instance).isNotEqualTo(new Object());
    }

    @ParameterizedTest
    @MethodSource("scenarios")
    void shouldBeEqualsToItself(DeleteQuery query, Params params) {
        var instance = newInstance(query, params);
        assertThat(instance).isEqualTo(instance);
    }

    @Test
    void shouldBeEqualsWhenQueryAndParamsAreUsedByTwoDifferentInstances() {
        var query = newDummyColumnDeleteQuery();
        var params = newDummyParams();

        var leftInstance = newInstance(query, params);
        var rightInstance = newInstance(query, params);

        assertThat(leftInstance).isEqualTo(rightInstance);
    }

    @ParameterizedTest
    @MethodSource("scenarios")
    void shouldBeNotEqualsWhenDifferentQueryAndParamsAreUsedByTwoDifferentInstances(DeleteQuery query, Params params) {

        var leftInstance = newInstance(query, params);
        var rightInstance = newInstance(newDummyColumnDeleteQuery(), newDummyParams());

        assertThat(leftInstance).isNotEqualTo(rightInstance);
    }

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
