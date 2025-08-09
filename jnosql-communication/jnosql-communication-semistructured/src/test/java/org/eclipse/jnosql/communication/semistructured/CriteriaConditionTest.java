/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
import org.eclipse.jnosql.communication.TypeReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;


class CriteriaConditionTest {

    private static Element el(String name, Object value) {
        return Element.of(name, value);
    }

    @Test
    @DisplayName("Should create conditions using Element factories for each Condition type")
    void shouldCreateConditionsUsingFactories() {
        var name = el("name", "Otavio");
        var age = el("age", 40);
        var bio = el("bio", "java champion");

        assertSoftly(soft -> {
            soft.assertThat(CriteriaCondition.eq(name).condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(CriteriaCondition.gt(age).condition()).isEqualTo(Condition.GREATER_THAN);
            soft.assertThat(CriteriaCondition.gte(age).condition()).isEqualTo(Condition.GREATER_EQUALS_THAN);
            soft.assertThat(CriteriaCondition.lt(age).condition()).isEqualTo(Condition.LESSER_THAN);
            soft.assertThat(CriteriaCondition.lte(age).condition()).isEqualTo(Condition.LESSER_EQUALS_THAN);
            soft.assertThat(CriteriaCondition.like(name).condition()).isEqualTo(Condition.LIKE);
            soft.assertThat(CriteriaCondition.contains(bio).condition()).isEqualTo(Condition.CONTAINS);
            soft.assertThat(CriteriaCondition.startsWith(bio).condition()).isEqualTo(Condition.STARTS_WITH);
            soft.assertThat(CriteriaCondition.endsWith(bio).condition()).isEqualTo(Condition.ENDS_WITH);
            var ic = CriteriaCondition.ignoreCase(CriteriaCondition.like(el("nick", "otavio")));
            soft.assertThat(ic.condition()).isEqualTo(Condition.IGNORE_CASE);
            soft.assertThat(ic.element().get(CriteriaCondition.class).condition()).isEqualTo(Condition.LIKE);
        });
    }

    @Test
    @DisplayName("Should create conditions using name/value aliases")
    void shouldCreateConditionsUsingNameValueAliases() {
        assertSoftly(soft -> {
            soft.assertThat(CriteriaCondition.eq("k", "v").condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(CriteriaCondition.gt("n", 1).condition()).isEqualTo(Condition.GREATER_THAN);
            soft.assertThat(CriteriaCondition.gte("n", 1).condition()).isEqualTo(Condition.GREATER_EQUALS_THAN);
            soft.assertThat(CriteriaCondition.lt("n", 1).condition()).isEqualTo(Condition.LESSER_THAN);
            soft.assertThat(CriteriaCondition.lte("n", 1).condition()).isEqualTo(Condition.LESSER_EQUALS_THAN);
            soft.assertThat(CriteriaCondition.like("s", "x").condition()).isEqualTo(Condition.LIKE);
        });
    }

    @Test
    @DisplayName("Should accept Iterable in 'in' condition and reject non-Iterable values")
    void shouldAcceptIterableInConditionAndRejectNonIterable() {
        var ok = CriteriaCondition.in(el("ids", List.of(1, 2, 3)));
        assertThat(ok.condition()).isEqualTo(Condition.IN);

        assertThatThrownBy(() -> CriteriaCondition.in(el("bad", 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must use an iterable");
    }

    @Test
    @DisplayName("Should validate 'between' requires exactly two elements")
    void shouldValidateBetweenRequiresTwoElements() {
        var ok = CriteriaCondition.between(el("range", List.of(1, 10)));
        assertThat(ok.condition()).isEqualTo(Condition.BETWEEN);

        assertThatThrownBy(() -> CriteriaCondition.between(el("r1", List.of(1))))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> CriteriaCondition.between(el("r3", List.of(1, 2, 3))))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> CriteriaCondition.between(el("notIterable", "oops")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should combine multiple conditions using static AND and OR methods")
    void shouldCombineMultipleConditionsWithStaticAndOr() {
        var c1 = CriteriaCondition.eq(el("name", "Otavio"));
        var c2 = CriteriaCondition.gte(el("age", 30));
        var c3 = CriteriaCondition.like(el("bio", "Java"));

        var and = CriteriaCondition.and(c1, c2, c3);
        var or = CriteriaCondition.or(c1, c2);

        assertSoftly(soft -> {
            soft.assertThat(and.condition()).isEqualTo(Condition.AND);
            soft.assertThat(and.element().get(new TypeReference<List<CriteriaCondition>>() {}))
                    .containsExactly(c1, c2, c3);

            soft.assertThat(or.condition()).isEqualTo(Condition.OR);
            soft.assertThat(or.element().get(new TypeReference<List<CriteriaCondition>>() {}))
                    .containsExactly(c1, c2);
        });
    }

    @Test
    @DisplayName("Should flatten conditions when using instance AND/OR with same operator")
    void shouldFlattenConditionsWithSameOperator() {
        var c1 = CriteriaCondition.and(
                CriteriaCondition.eq(el("a", 1)),
                CriteriaCondition.eq(el("b", 2))
        );
        var cX = CriteriaCondition.eq(el("c", 3));

        var combined = c1.and(cX);
        var list = combined.element().get(new TypeReference<List<CriteriaCondition>>() {});

        assertSoftly(soft -> {
            soft.assertThat(combined.condition()).isEqualTo(Condition.AND);
            soft.assertThat(list).hasSize(3).contains(cX);
        });

        var d1 = CriteriaCondition.eq(el("x", 1));
        var d2 = CriteriaCondition.eq(el("y", 2));
        var orCombined = d1.or(d2);

        assertSoftly(soft -> {
            soft.assertThat(orCombined.condition()).isEqualTo(Condition.OR);
            soft.assertThat(orCombined.element().get(new TypeReference<List<CriteriaCondition>>() {}))
                    .containsExactly(d1, d2);
        });
    }

    @Test
    @DisplayName("Should negate condition and unwrap double negations")
    void shouldNegateAndUnwrapDoubleNegations() {
        var base = CriteriaCondition.eq(el("flag", true));
        var neg = base.negate();
        assertThat(neg.condition()).isEqualTo(Condition.NOT);
        assertThat(neg.element().get(CriteriaCondition.class)).isEqualTo(base);

        var doubleNeg = neg.negate();
        assertThat(doubleNeg).isEqualTo(base);

        var viaStatic = CriteriaCondition.not(base);
        assertThat(viaStatic.condition()).isEqualTo(Condition.NOT);
    }

    @Test
    @DisplayName("Should throw when modifying readOnly conditions")
    void shouldThrowWhenModifyingReadOnlyConditions() {
        var base = CriteriaCondition.eq(el("name", "Otavio"));
        var ro = CriteriaCondition.readOnly(base);

        assertThatThrownBy(() -> ro.and(base)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(ro::negate).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> ro.or(base)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Should throw NullPointerException for null parameters in name/value aliases")
    void shouldThrowNpeForNullParametersInAliases() {
        assertThatThrownBy(() -> CriteriaCondition.eq(null, "v"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CriteriaCondition.eq("k", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should demonstrate equals/hashCode contract issue due to readOnly in hashCode")
    void shouldShowEqualsHashCodeContractIssue() {
        var base = CriteriaCondition.eq(el("x", 1));
        var ro = CriteriaCondition.readOnly(base);

        assertThat(ro).isEqualTo(base);
        assertThat(ro.hashCode()).isNotEqualTo(base.hashCode()); // contract violation
    }

    @Test
    @DisplayName("Should return non-empty toString containing condition name")
    void shouldReturnNonEmptyToStringContainingCondition() {
        var c = CriteriaCondition.gt(el("age", 18));
        assertThat(c.toString()).contains("GREATER_THAN");
    }
}