/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.semistructured;

import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.TypeSupplier;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.function.EvaluableFunction;
import org.eclipse.jnosql.communication.function.FunctionArgument;
import org.eclipse.jnosql.communication.function.NativeFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayName("Criteria operands")
class CriteriaOperandTest {

    private static final NativeFunction UPPER =
            NativeFunction.of("UPPER", new FunctionArgument.AttributePath("name"));

    private static final CriteriaOperand.Function UPPER_OPERAND = new CriteriaOperand.Function(UPPER);

    private static Value counting(AtomicInteger counter, Object result) {
        return new Value() {
            @Override
            public Object get() {
                counter.incrementAndGet();
                return result;
            }

            @Override
            public <T> T get(Class<T> type) {
                return type.cast(get());
            }

            @Override
            public <T> T get(TypeSupplier<T> supplier) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isInstanceOf(Class<?> type) {
                return type.isInstance(result);
            }

            @Override
            public boolean isNull() {
                return result == null;
            }
        };
    }

    @Nested
    @DisplayName("When creating a predicate from operands")
    class WhenTheCreation {

        @Test
        @DisplayName("Should create a native function predicate on the left")
        void shouldCreateLeftFunction() {

            // Given
            var value = new CriteriaOperand.Constant(Value.of("OTAVIO"));

            // When
            var condition = CriteriaCondition.of(UPPER_OPERAND, Condition.EQUALS, value);

            // Then
            assertSoftly(softly -> {
                softly.assertThat(condition.isNative()).as("native").isTrue();
                softly.assertThat(condition.leftOperand()).as("left").isEqualTo(UPPER_OPERAND);
                softly.assertThat(condition.rightOperand()).as("right").isEqualTo(value);
            });
        }

        @Test
        @DisplayName("Should create a native function predicate on the right")
        void shouldCreateRightFunction() {

            // Given
            var attribute = new CriteriaOperand.Attribute("name");

            // When
            var condition = CriteriaCondition.of(attribute, Condition.EQUALS, UPPER_OPERAND);

            // Then
            assertSoftly(softly -> {
                softly.assertThat(condition.leftOperand()).as("left").isEqualTo(attribute);
                softly.assertThat(condition.rightOperand()).as("right").isEqualTo(UPPER_OPERAND);
            });
        }

        @Test
        @DisplayName("Should expose an evaluable function as an ordinary value")
        void shouldExposeEvaluableAsValue() {

            // Given
            var evaluable = EvaluableFunction.of("UPPER", FunctionArgument.literal("otavio"));

            // When
            var condition = CriteriaCondition.of(UPPER_OPERAND, Condition.EQUALS,
                    new CriteriaOperand.Constant(evaluable));

            // Then
            assertThat(((CriteriaOperand.Constant) condition.rightOperand()).value().get()).isEqualTo("OTAVIO");
        }

        @Test
        @DisplayName("Should create the same condition as the element factory for an attribute and a value")
        void shouldMatchElementFactory() {

            // When
            var condition = CriteriaCondition.of(new CriteriaOperand.Attribute("name"), Condition.EQUALS,
                    new CriteriaOperand.Constant(Value.of("OTAVIO")));

            // Then
            assertThat(condition).isEqualTo(CriteriaCondition.eq(Element.of("name", "OTAVIO")));
        }

        @Test
        @DisplayName("Should compose native predicates with AND and OR")
        void shouldCompose() {

            // Given
            var first = CriteriaCondition.of(UPPER_OPERAND, Condition.EQUALS,
                    new CriteriaOperand.Constant(Value.of("A")));
            var second = CriteriaCondition.of(UPPER_OPERAND, Condition.EQUALS,
                    new CriteriaOperand.Constant(Value.of("B")));

            // When
            var composed = first.and(second).or(first);

            // Then
            assertThat(composed.condition()).isEqualTo(Condition.OR);
        }

        @Test
        @DisplayName("Should keep the native operand in a read only copy")
        void shouldKeepNativeInReadOnlyCopy() {

            // Given
            var condition = CriteriaCondition.of(UPPER_OPERAND, Condition.EQUALS,
                    new CriteriaOperand.Constant(Value.of("A")));

            // When
            var readOnly = CriteriaCondition.readOnly(condition);

            // Then
            assertSoftly(softly -> {
                softly.assertThat(readOnly).as("equal").isEqualTo(condition);
                softly.assertThat(readOnly.isNative()).as("native").isTrue();
            });
        }

        @Test
        @DisplayName("Should describe the native operand in the text form")
        void shouldDescribe() {

            // When
            var condition = CriteriaCondition.of(UPPER_OPERAND, Condition.EQUALS,
                    new CriteriaOperand.Constant(Value.of("A")));

            // Then
            assertThat(condition.toString()).contains("UPPER");
        }

        @Test
        @DisplayName("Should reject a null operand")
        void shouldRejectNull() {
            assertThatNullPointerException().isThrownBy(
                    () -> CriteriaCondition.of(null, Condition.EQUALS, UPPER_OPERAND));
        }
    }

    @Nested
    @DisplayName("When validating the operand combination")
    class WhenTheValidation {

        @Test
        @DisplayName("Should reject a value as the left operand")
        void shouldRejectValueOnTheLeft() {
            assertThatThrownBy(() -> CriteriaCondition.of(new CriteriaOperand.Constant(Value.of("x")),
                    Condition.EQUALS, UPPER_OPERAND)).isInstanceOf(QueryException.class);
        }

        @Test
        @DisplayName("Should reject a comparison of attributes without a native function")
        void shouldRejectFieldToField() {
            var attribute = new CriteriaOperand.Attribute("a");
            assertThatThrownBy(() -> CriteriaCondition.of(attribute, Condition.EQUALS, attribute))
                    .isInstanceOf(QueryException.class);
        }

        @Test
        @DisplayName("Should reject LIKE against a function")
        void shouldRejectLikeAgainstFunction() {
            assertThatThrownBy(() -> CriteriaCondition.of(UPPER_OPERAND, Condition.LIKE, UPPER_OPERAND))
                    .isInstanceOf(QueryException.class);
        }

        @Test
        @DisplayName("Should reject IN without an iterable")
        void shouldRejectInWithoutIterable() {
            assertThatThrownBy(() -> CriteriaCondition.of(UPPER_OPERAND, Condition.IN,
                    new CriteriaOperand.Constant(Value.of("x")))).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should accept IN with an iterable")
        void shouldAcceptIn() {

            // When
            var condition = CriteriaCondition.of(UPPER_OPERAND, Condition.IN,
                    new CriteriaOperand.Constant(Value.of(List.of("JAVA", "JAKARTA"))));

            // Then
            assertThat(condition.condition()).isEqualTo(Condition.IN);
        }

        @Test
        @DisplayName("Should reject BETWEEN without exactly two values")
        void shouldRejectBetweenWithOneValue() {
            assertThatThrownBy(() -> CriteriaCondition.of(UPPER_OPERAND, Condition.BETWEEN,
                    new CriteriaOperand.Constant(Value.of(List.of(1))))).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @EnumSource(value = Condition.class,
                names = {"AND", "OR", "NOT", "CONTAINS", "STARTS_WITH", "ENDS_WITH", "IGNORE_CASE"})
        @DisplayName("Should reject conditions that cannot hold a native function")
        void shouldRejectUnsupportedConditions(Condition condition) {
            assertThatThrownBy(() -> CriteriaCondition.of(UPPER_OPERAND, condition,
                    new CriteriaOperand.Constant(Value.of("x")))).isInstanceOf(QueryException.class);
        }

        @ParameterizedTest
        @EnumSource(value = Condition.class, names = {"AND", "OR"})
        @DisplayName("Should reject building a logical condition from attribute and value operands")
        void shouldRejectLogicalFromOperands(Condition condition) {
            assertThatThrownBy(() -> CriteriaCondition.of(new CriteriaOperand.Attribute("a"), condition,
                    new CriteriaOperand.Constant(Value.of("x")))).isInstanceOf(QueryException.class);
        }
    }

    @Nested
    @DisplayName("When accessing the legacy element of a condition")
    class WhenTheLegacyAccess {

        @Test
        @DisplayName("Should keep an ordinary condition compatible")
        void shouldKeepOrdinaryCompatible() {

            // When
            var condition = CriteriaCondition.eq(Element.of("name", "OTAVIO"));

            // Then
            assertSoftly(softly -> {
                softly.assertThat(condition.isNative()).as("native").isFalse();
                softly.assertThat(condition.element().name()).as("name").isEqualTo("name");
                softly.assertThat(condition.element().get()).as("value").isEqualTo("OTAVIO");
                softly.assertThat(condition.leftOperand()).as("left")
                        .isEqualTo(new CriteriaOperand.Attribute("name"));
                softly.assertThat(((CriteriaOperand.Constant) condition.rightOperand()).value().get())
                        .as("right").isEqualTo("OTAVIO");
            });
        }

        @Test
        @DisplayName("Should fail explicitly for a native function predicate")
        void shouldFailForNative() {

            // Given
            var condition = CriteriaCondition.of(UPPER_OPERAND, Condition.EQUALS,
                    new CriteriaOperand.Constant(Value.of("A")));

            // When / Then
            assertThatThrownBy(condition::element).isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("native function");
        }

        @Test
        @DisplayName("Should fail explicitly when asking a logical condition for operands")
        void shouldFailOperandsOfLogical() {

            // Given
            var condition = CriteriaCondition.and(CriteriaCondition.eq("a", 1), CriteriaCondition.eq("b", 2));

            // When / Then
            assertThatThrownBy(condition::leftOperand).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(condition::rightOperand).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("When evaluating the arguments of a native function")
    class WhenTheLazyEvaluation {

        @Test
        @DisplayName("Should not evaluate the argument when building or inspecting the function")
        void shouldNotEvaluateEagerly() {

            // Given
            var counter = new AtomicInteger();
            var length = EvaluableFunction.of("LENGTH", new FunctionArgument.ValueArgument(counting(counter, "ota")));

            // When
            var left = NativeFunction.of("LEFT", new FunctionArgument.AttributePath("name"),
                    new FunctionArgument.ValueArgument(length));
            var condition = CriteriaCondition.of(new CriteriaOperand.Function(left), Condition.EQUALS,
                    new CriteriaOperand.Constant(Value.of("ota")));

            // Then
            assertSoftly(softly -> {
                softly.assertThat(((CriteriaOperand.Function) condition.leftOperand()).function().arguments())
                        .as("arguments").hasSize(2);
                softly.assertThat(counter).as("evaluations").hasValue(0);
            });
        }

        @Test
        @DisplayName("Should evaluate once and reuse the memoized result")
        void shouldMemoize() {

            // Given
            var counter = new AtomicInteger();
            var length = EvaluableFunction.of("LENGTH", new FunctionArgument.ValueArgument(counting(counter, "ota")));
            var argument = new FunctionArgument.ValueArgument(length);

            // When
            var first = argument.value().get();
            var second = argument.value().get();

            // Then
            assertSoftly(softly -> {
                softly.assertThat(first).as("first").isEqualTo(3);
                softly.assertThat(second).as("second").isEqualTo(3);
                softly.assertThat(counter).as("evaluations").hasValue(1);
            });
        }

        @Test
        @DisplayName("Should memoize a null result")
        void shouldMemoizeNull() {

            // Given
            var counter = new AtomicInteger();
            var upper = EvaluableFunction.of("UPPER", new FunctionArgument.ValueArgument(counting(counter, null)));

            // When
            var first = upper.get();
            var second = upper.get();

            // Then
            assertSoftly(softly -> {
                softly.assertThat(first).as("first").isNull();
                softly.assertThat(second).as("second").isNull();
                softly.assertThat(counter).as("evaluations").hasValue(1);
            });
        }

        @Test
        @DisplayName("Should expose immutable arguments")
        void shouldBeImmutable() {

            // Given
            var arguments = UPPER.arguments();

            // When / Then
            assertThatThrownBy(() -> arguments.add(new FunctionArgument.Literal(1)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
