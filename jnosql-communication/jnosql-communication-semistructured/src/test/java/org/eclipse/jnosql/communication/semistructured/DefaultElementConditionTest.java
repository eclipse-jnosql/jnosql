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

import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;



class DefaultElementConditionTest {


    @Nested
    @DisplayName("When the default element condition is used")
    class WhenTheDefaultElementConditionIsUsed {

        private final CriteriaCondition lte = CriteriaCondition.lte(Element.of("salary", 10.32));

        @DisplayName("Should Return Error When Column Is Null")
        @Test
        void shouldReturnErrorWhenColumnIsNull() {
            assertThatThrownBy(() -> CriteriaCondition.of(null, Condition.AND)).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Create An Instance")
        @Test
        void shouldCreateAnInstance() {
            Element name = Element.of("name", "Otavio");
            CriteriaCondition condition = CriteriaCondition.of(name, Condition.EQUALS);
            assertThat(condition).isNotNull();
            assertThat(condition.element()).isEqualTo(name);
            assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
        }

        @DisplayName("Should Create Negation Condition")
        @Test
        void shouldCreateNegationCondition() {
            Element age = Element.of("age", 26);
            CriteriaCondition condition = CriteriaCondition.of(age, Condition.GREATER_THAN);
            CriteriaCondition negate = condition.negate();
            Element negateElement = negate.element();
            assertThat(negate.condition()).isEqualTo(Condition.NOT);
            assertThat(negateElement.name()).isEqualTo(Condition.NOT.getNameField());
            assertThat(negateElement.value().get()).isEqualTo(CriteriaCondition.of(age, Condition.GREATER_THAN));
        }

        @DisplayName("Should Return Valid Double Negation")
        @Test
        void shouldReturnValidDoubleNegation() {
            Element age = Element.of("age", 26);
            CriteriaCondition condition = CriteriaCondition.of(age, Condition.GREATER_THAN);
            CriteriaCondition affirmative = condition.negate().negate();
            assertThat(affirmative).isEqualTo(condition);
        }

        @DisplayName("Should Create And Condition")
        @Test
        void shouldCreateAndCondition() {
            Element age = Element.of("age", 26);
            Element name = Element.of("name", "Otavio");
            CriteriaCondition condition1 = CriteriaCondition.of(name, Condition.EQUALS);
            CriteriaCondition condition2 = CriteriaCondition.of(age, Condition.GREATER_THAN);

            CriteriaCondition and = condition1.and(condition2);
            Element andElement = and.element();
            assertThat(and.condition()).isEqualTo(Condition.AND);
            assertThat(andElement.name()).isEqualTo(Condition.AND.getNameField());
            assertThat(andElement.value().get(new TypeReference<List<CriteriaCondition>>() {
                    })).contains(condition1, condition2);

        }

        @DisplayName("Should Create Or Condition")
        @Test
        void shouldCreateOrCondition() {
            Element age = Element.of("age", 26);
            Element name = Element.of("name", "Otavio");
            CriteriaCondition condition1 = CriteriaCondition.of(name, Condition.EQUALS);
            CriteriaCondition condition2 = CriteriaCondition.of(age, Condition.GREATER_THAN);

            CriteriaCondition and = condition1.or(condition2);
            Element andElement = and.element();
            assertThat(and.condition()).isEqualTo(Condition.OR);
            assertThat(andElement.name()).isEqualTo(Condition.OR.getNameField());
            assertThat(andElement.value().get(new TypeReference<List<CriteriaCondition>>() {
                    })).contains(condition1, condition2);

        }

        @DisplayName("Should Return Error When Create And With Null Values")
        @Test
        void shouldReturnErrorWhenCreateAndWithNullValues() {
            assertThatThrownBy(() -> CriteriaCondition.and((CriteriaCondition[]) null)).isInstanceOf(NullPointerException.class);
        }


        @DisplayName("Should Return Error When Create Or With Null Values")
        @Test
        void shouldReturnErrorWhenCreateOrWithNullValues() {
            assertThatThrownBy(() -> CriteriaCondition.or((CriteriaCondition[]) null)).isInstanceOf(NullPointerException.class);
        }


        @DisplayName("Should Append And")
        @Test
        void shouldAppendAnd() {
            CriteriaCondition eq = CriteriaCondition.eq(Element.of("name", "otavio"));
            CriteriaCondition gt = CriteriaCondition.gt(Element.of("age", 10));
            CriteriaCondition and = CriteriaCondition.and(eq, gt);
            assertThat(and.condition()).isEqualTo(Condition.AND);
            List<CriteriaCondition> conditions = and.element().get(new TypeReference<>() {
            });
            assertThat(conditions).contains(eq, gt);
        }

        @DisplayName("Should Append Or")
        @Test
        void shouldAppendOr() {
            CriteriaCondition eq = CriteriaCondition.eq(Element.of("name", "otavio"));
            CriteriaCondition gt = CriteriaCondition.gt(Element.of("age", 10));
            CriteriaCondition and = CriteriaCondition.or(eq, gt);
            assertThat(and.condition()).isEqualTo(Condition.OR);
            List<CriteriaCondition> conditions = and.element().get(new TypeReference<>() {
            });
            assertThat(conditions).contains(eq, gt);
        }

        @DisplayName("Should And")
        @Test
        void shouldAnd() {
            CriteriaCondition eq = CriteriaCondition.eq(Element.of("name", "otavio"));
            CriteriaCondition gt = CriteriaCondition.gt(Element.of("age", 10));
            CriteriaCondition lte = CriteriaCondition.lte(Element.of("salary", 10_000.00));

            CriteriaCondition and = eq.and(gt);
            List<CriteriaCondition> conditions = and.element().get(new TypeReference<>() {
            });
            assertThat(and.condition()).isEqualTo(Condition.AND);
            assertThat(conditions).contains(eq, gt);
            CriteriaCondition result = and.and(lte);

            assertThat(result.condition()).isEqualTo(Condition.AND);
            assertThat(result.element().get(new TypeReference<List<CriteriaCondition>>() {
            })).contains(eq, gt, lte);

        }

        @DisplayName("Should Or")
        @Test
        void shouldOr() {
            CriteriaCondition eq = CriteriaCondition.eq(Element.of("name", "otavio"));
            CriteriaCondition gt = CriteriaCondition.gt(Element.of("age", 10));
            CriteriaCondition lte = CriteriaCondition.lte(Element.of("salary", 10_000.00));

            CriteriaCondition or = eq.or(gt);
            List<CriteriaCondition> conditions = or.element().get(new TypeReference<>() {
            });
            assertThat(or.condition()).isEqualTo(Condition.OR);
            assertThat(conditions).contains(eq, gt);
            CriteriaCondition result = or.or(lte);

            assertThat(result.condition()).isEqualTo(Condition.OR);
            assertThat(result.element().get(new TypeReference<List<CriteriaCondition>>() {
            })).contains(eq, gt, lte);

        }

        @DisplayName("Should Negate")
        @Test
        void shouldNegate() {
            CriteriaCondition eq = CriteriaCondition.eq(Element.of("name", "otavio"));
            CriteriaCondition negate = eq.negate();
            assertThat(negate.condition()).isEqualTo(Condition.NOT);
            CriteriaCondition condition = negate.element().get(CriteriaCondition.class);
            assertThat(condition).isEqualTo(eq);
        }

        @DisplayName("Should Affirm Double Negate")
        @Test
        void shouldAffirmDoubleNegate() {
            CriteriaCondition eq = CriteriaCondition.eq(Element.of("name", "otavio"));
            CriteriaCondition affirm = eq.negate().negate();
            assertThat(affirm.condition()).isEqualTo(eq.condition());

        }

        @DisplayName("Should Return Error When Between Is Null")
        @Test
        void shouldReturnErrorWhenBetweenIsNull() {
            assertThatThrownBy(() -> CriteriaCondition.between(null)).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Return Error When Between Is Not Iterable")
        @Test
        void shouldReturnErrorWhenBetweenIsNotIterable() {
            assertThatThrownBy(() -> {
                Element element = Element.of("age", 12);
                CriteriaCondition.between(element);
            }).isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("Should Return Error When Iterable Has One Element")
        @Test
        void shouldReturnErrorWhenIterableHasOneElement() {
            assertThatThrownBy(() -> {
                Element element = Element.of("age", Collections.singleton(12));
                CriteriaCondition.between(element);
            }).isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("Should Return Error When Iterable Has More Than Two Element2")
        @Test
        void shouldReturnErrorWhenIterableHasMoreThanTwoElement2() {
            assertThatThrownBy(() -> {
                Element element = Element.of("age", Arrays.asList(12, 12, 12));
                CriteriaCondition.between(element);
            }).isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("Should Return Between")
        @Test
        void shouldReturnBetween() {
            Element element = Element.of("age", Arrays.asList(12, 13));
            CriteriaCondition between = CriteriaCondition.between(element);
            assertThat(between.condition()).isEqualTo(Condition.BETWEEN);
            Iterable<Integer> integers = between.element().get(new TypeReference<>() {
            });
            assertThat(integers).contains(12, 13);
        }

        @DisplayName("Should Return Error When In Condition Is Invalid")
        @Test
        void shouldReturnErrorWhenInConditionIsInvalid() {
            assertThatThrownBy(() -> CriteriaCondition.in(null)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> CriteriaCondition.in(Element.of("value", 10))).isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("Should Return In Clause")
        @Test
        void shouldReturnInClause() {
            Element element = Element.of("age", Arrays.asList(12, 13));
            CriteriaCondition in = CriteriaCondition.in(element);
            assertThat(in.condition()).isEqualTo(Condition.IN);
            Iterable<Integer> integers = in.element().get(new TypeReference<>() {
            });
            assertThat(integers).contains(12, 13);
        }
    }

}
