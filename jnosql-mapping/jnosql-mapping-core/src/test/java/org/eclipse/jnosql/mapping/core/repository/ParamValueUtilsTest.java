/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.core.repository;


import jakarta.data.constraint.AtLeast;
import jakarta.data.constraint.AtMost;
import jakarta.data.constraint.Between;
import jakarta.data.constraint.Constraint;
import jakarta.data.constraint.EqualTo;
import jakarta.data.constraint.GreaterThan;
import jakarta.data.constraint.In;
import jakarta.data.constraint.LessThan;
import jakarta.data.constraint.Like;
import jakarta.data.constraint.NotBetween;
import jakarta.data.constraint.NotEqualTo;
import jakarta.data.constraint.NotIn;
import jakarta.data.constraint.NotLike;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class ParamValueUtilsTest {

    @ParameterizedTest(name = "should match condition for {0}")
    @MethodSource("conditions")
    void shouldReturnParam(Class<? extends Constraint<?>> constraint, boolean isNegate, Condition condition) {
        ParamValue paramValue = ParamValueUtils.getParamValue("name", constraint);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(paramValue.condition()).isEqualTo(condition);
            softly.assertThat(paramValue.negate()).isEqualTo(isNegate);
            softly.assertThat(paramValue.value()).isEqualTo("name");
        });
    }

    private static Stream<Arguments> conditions() {
        return Stream.of(
                Arguments.of(AtLeast.class, false, Condition.GREATER_EQUALS_THAN),
                Arguments.of(AtMost.class, false, Condition.LESSER_EQUALS_THAN),
                Arguments.of(GreaterThan.class, false, Condition.GREATER_THAN),
                Arguments.of(LessThan.class, false, Condition.LESSER_THAN),
                Arguments.of(Between.class, false, Condition.BETWEEN),
                Arguments.of(EqualTo.class, false, Condition.EQUALS),
                Arguments.of(Like.class, false, Condition.LIKE),
                Arguments.of(In.class, false, Condition.IN),
                Arguments.of(NotBetween.class, true, Condition.BETWEEN),
                Arguments.of(NotEqualTo.class, true, Condition.EQUALS),
                Arguments.of(NotIn.class, true, Condition.IN),
                Arguments.of(NotLike.class, true, Condition.LIKE)
        );
    }
}