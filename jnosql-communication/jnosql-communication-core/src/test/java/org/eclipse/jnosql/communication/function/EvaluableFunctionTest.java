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
package org.eclipse.jnosql.communication.function;

import org.eclipse.jnosql.communication.TypeSupplier;
import org.eclipse.jnosql.communication.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.jnosql.communication.function.FunctionArgument.literal;

class EvaluableFunctionTest {

    private static final String PAIR = "\uD83D\uDE00"; // one code point, two chars

    private static FunctionArgument lazy(AtomicInteger counter, Supplier<Object> supplier) {
        return new FunctionArgument.ValueArgument(new LazyValue(counter, supplier));
    }

    private static Object value(String name, FunctionArgument... arguments) {
        return EvaluableFunction.of(name, arguments).get();
    }

    private static FunctionArgument nested(String name, FunctionArgument... arguments) {
        return FunctionArgument.function(EvaluableFunction.of(name, arguments));
    }

    record LazyValue(AtomicInteger counter, Supplier<Object> supplier) implements Value {
        @Override
        public Object get() {
            counter.incrementAndGet();
            return supplier.get();
        }

        @Override
        public <T> T get(Class<T> type) {
            return type.cast(get());
        }

        @Override
        public <T> T get(TypeSupplier<T> typeSupplier) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isInstanceOf(Class<?> type) {
            return type.isInstance(get());
        }

        @Override
        public boolean isNull() {
            return get() == null;
        }
    }

    @ParameterizedTest
    @CsvSource({"UPPER,otavio,OTAVIO", "UPPER,OTAVIO,OTAVIO", "UPPER,JaKaRtA,JAKARTA", "UPPER,123,123",
            "LOWER,otavio,otavio", "LOWER,OTAVIO,otavio", "LOWER,JaKaRtA,jakarta", "LOWER,123,123"})
    void shouldChangeCase(String function, String input, String expected) {
        assertThat(value(function, literal(input))).isEqualTo(expected);
    }

    @Test
    void shouldHandleEmptyAndUnicodeCase() {
        assertThat(value("UPPER", literal(""))).isEqualTo("");
        assertThat(value("LOWER", literal(""))).isEqualTo("");
        assertThat(value("UPPER", literal("ação é"))).isEqualTo("AÇÃO É");
        assertThat(value("LOWER", literal("ÁÉÍ"))).isEqualTo("áéí");
    }

    @Test
    void shouldIgnoreDefaultLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertThat(value("UPPER", literal("title"))).isEqualTo("TITLE");
            assertThat(value("LOWER", literal("TITLE"))).isEqualTo("title");
        } finally {
            Locale.setDefault(previous);
        }
    }

    @ParameterizedTest
    @CsvSource({"Jakarta,7", "abc,3", "'',0", "' ',1"})
    void shouldMeasureLength(String input, int expected) {
        Object length = value("LENGTH", literal(input));
        assertThat(length).isInstanceOf(Integer.class).isEqualTo(expected);
    }

    @Test
    void shouldCountCodePoints() {
        assertThat(value("LENGTH", literal("a" + PAIR + "b"))).isEqualTo(3);
        assertThat(value("LENGTH", literal("é"))).isEqualTo(1);
    }

    @ParameterizedTest
    @CsvSource({"abc,0,'',''", "abc,1,a,c", "abc,2,ab,bc", "abc,3,abc,abc", "abc,4,abc,abc",
            "otavio,19,otavio,otavio", "'',10,'',''", "' a ',1,' ',' '"})
    void shouldSlice(String text, int length, String left, String right) {
        assertThat(value("LEFT", literal(text), literal(length))).isEqualTo(left);
        assertThat(value("RIGHT", literal(text), literal(length))).isEqualTo(right);
    }

    @Test
    void shouldSliceExtremeLengths() {
        assertThat(value("LEFT", literal("abc"), literal(Integer.MAX_VALUE))).isEqualTo("abc");
        assertThat(value("RIGHT", literal("abc"), literal(Long.MAX_VALUE))).isEqualTo("abc");
        assertThat(value("LEFT", literal("abc"), literal(BigInteger.TWO.pow(100)))).isEqualTo("abc");
        assertThat(value("RIGHT", literal("abc"), literal((byte) 2))).isEqualTo("bc");
        assertThat(value("LEFT", literal("abc"), literal(BigInteger.ONE))).isEqualTo("a");
        String text = "abc";
        assertThat(EvaluableFunction.of("LEFT", literal(text), literal(5)).get()).isSameAs(text);
    }

    @Test
    void shouldNotSplitSurrogatePairs() {
        String text = "a" + PAIR + "b" + PAIR;
        assertThat(value("LEFT", literal(text), literal(2))).isEqualTo("a" + PAIR);
        assertThat(value("RIGHT", literal(text), literal(2))).isEqualTo("b" + PAIR);
        assertThat(value("LEFT", literal(PAIR), literal(1))).isEqualTo(PAIR);
        assertThat(value("RIGHT", literal(PAIR + PAIR), literal(1))).isEqualTo(PAIR);
        assertThat(value("LEFT", literal(PAIR), literal(0))).isEqualTo("");
    }

    @ParameterizedTest
    @MethodSource("negativeLengths")
    void shouldRejectNegativeLengths(Object length) {
        assertThatThrownBy(() -> value("LEFT", literal("abc"), literal(length)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("negative");
        assertThatThrownBy(() -> value("RIGHT", literal("abc"), literal(length)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("position 2");
    }

    static Stream<Object> negativeLengths() {
        return Stream.of(-1, Integer.MIN_VALUE, Long.MIN_VALUE, BigInteger.valueOf(-5), (byte) -1);
    }

    static Stream<Arguments> invalidTypes() {
        return Stream.of(
                Arguments.of("UPPER", List.of(literal(123))),
                Arguments.of("UPPER", List.of(literal(true))),
                Arguments.of("LOWER", List.of(literal(java.time.LocalDate.now()))),
                Arguments.of("LOWER", List.of(literal(new Object()))),
                Arguments.of("LENGTH", List.of(literal(123))),
                Arguments.of("LENGTH", List.of(literal(true))),
                Arguments.of("LEFT", List.of(literal("abc"), literal(1.5))),
                Arguments.of("LEFT", List.of(literal("abc"), literal("2"))),
                Arguments.of("RIGHT", List.of(literal(123), literal(2))),
                Arguments.of("ABS", List.of(literal("10"))),
                Arguments.of("ABS", List.of(literal(new java.util.concurrent.atomic.AtomicInteger(1)))),
                Arguments.of("ABS", List.of(literal(true))));
    }

    @ParameterizedTest
    @MethodSource("invalidTypes")
    void shouldRejectInvalidTypesAtEvaluation(String name, List<FunctionArgument> arguments) {
        // literal types are known structurally, so the factory itself rejects most; deferred ones fail on evaluation
        assertThatThrownBy(() -> EvaluableFunction.of(name, arguments.toArray(FunctionArgument[]::new)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectDeferredInvalidTypesWithDetails() {
        EvaluableFunction function = EvaluableFunction.of("LEFT", literal("abc"),
                lazy(new AtomicInteger(), () -> 1.5));
        assertThatThrownBy(function::get).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("LEFT").hasMessageContaining("position 2")
                .hasMessageContaining("INTEGER").hasMessageContaining("java.lang.Double");
        assertThatThrownBy(() -> EvaluableFunction.of("UPPER", lazy(new AtomicInteger(), () -> 123)).get())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EvaluableFunction.of("ABS", lazy(new AtomicInteger(),
                () -> new java.util.concurrent.atomic.AtomicInteger(1))).get())
                .isInstanceOf(IllegalArgumentException.class);
    }

    static Stream<Arguments> absolute() {
        return Stream.of(
                Arguments.of((byte) -10, (byte) 10), Arguments.of((short) -10, (short) 10),
                Arguments.of(-10, 10), Arguments.of(-10L, 10L), Arguments.of(-10.5f, 10.5f),
                Arguments.of(-10.5d, 10.5d), Arguments.of(new BigInteger("-100"), new BigInteger("100")),
                Arguments.of(new BigDecimal("-10.50"), new BigDecimal("10.50")),
                Arguments.of(10, 10), Arguments.of(0, 0), Arguments.of(Integer.MAX_VALUE, Integer.MAX_VALUE),
                Arguments.of(Long.MAX_VALUE, Long.MAX_VALUE),
                Arguments.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
                Arguments.of(new BigInteger("-123456789012345678901234567890"),
                        new BigInteger("123456789012345678901234567890")));
    }

    @ParameterizedTest
    @MethodSource("absolute")
    void shouldPreserveNumericTypeAndValue(Number input, Number expected) {
        Object result = value("ABS", literal(input));
        assertThat(result).isEqualTo(expected).isExactlyInstanceOf(expected.getClass());
    }

    @Test
    void shouldPreserveBigDecimalScale() {
        BigDecimal result = (BigDecimal) value("ABS", literal(new BigDecimal("-10.50")));
        assertThat(result.scale()).isEqualTo(2);
        assertThat(result.toString()).isEqualTo("10.50");
    }

    @Test
    void shouldHandleSpecialFloatingPoint() {
        assertThat((Double) value("ABS", literal(-0.0d))).isEqualTo(0.0d);
        assertThat(1 / (Double) value("ABS", literal(-0.0d))).isEqualTo(Double.POSITIVE_INFINITY);
        assertThat((Double) value("ABS", literal(Double.NaN))).isNaN();
        assertThat((Float) value("ABS", literal(Float.NaN))).isNaN();
        assertThat(value("ABS", literal(Float.NEGATIVE_INFINITY))).isEqualTo(Float.POSITIVE_INFINITY);
    }

    @ParameterizedTest
    @MethodSource("minimums")
    void shouldFailOnOverflow(Number minimum) {
        assertThatThrownBy(() -> value("ABS", literal(minimum))).isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("ABS");
    }

    static Stream<Number> minimums() {
        return Stream.of(Byte.MIN_VALUE, Short.MIN_VALUE, Integer.MIN_VALUE, Long.MIN_VALUE);
    }

    @ParameterizedTest
    @MethodSource("nullCases")
    void shouldPropagateNull(String name, FunctionArgument[] arguments) {
        EvaluableFunction function = EvaluableFunction.of(name, arguments);
        assertThat(function.isNull()).isTrue();
        assertThat(function.get()).isNull();
    }

    static Stream<Arguments> nullCases() {
        return Stream.of(
                Arguments.of("UPPER", new FunctionArgument[]{literal(null)}),
                Arguments.of("LOWER", new FunctionArgument[]{literal(null)}),
                Arguments.of("LENGTH", new FunctionArgument[]{literal(null)}),
                Arguments.of("ABS", new FunctionArgument[]{literal(null)}),
                Arguments.of("LEFT", new FunctionArgument[]{literal(null), literal(3)}),
                Arguments.of("LEFT", new FunctionArgument[]{literal("abc"), literal(null)}),
                Arguments.of("RIGHT", new FunctionArgument[]{literal(null), literal(3)}),
                Arguments.of("RIGHT", new FunctionArgument[]{literal("abc"), literal(null)}),
                Arguments.of("UPPER", new FunctionArgument[]{nested("LEFT", literal(null), literal(3))}),
                Arguments.of("LENGTH", new FunctionArgument[]{nested("LOWER", literal(null))}));
    }

    @Test
    void shouldNotHideFailuresBehindNull() {
        assertThatThrownBy(() -> EvaluableFunction.of("LEFT", literal(null),
                lazy(new AtomicInteger(), () -> -1)).get()).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EvaluableFunction.of("LEFT", literal(null),
                FunctionArgument.named("length")).get()).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(":length");
        assertThatThrownBy(() -> EvaluableFunction.of("LEFT", FunctionArgument.positional(1),
                literal(1)).get()).isInstanceOf(IllegalStateException.class).hasMessageContaining("?1");
        assertThatThrownBy(() -> EvaluableFunction.of("ABS", lazy(new AtomicInteger(), () -> Integer.MIN_VALUE))
                .get()).isInstanceOf(ArithmeticException.class);
    }

    @Test
    void shouldEvaluateNestedFunctions() {
        assertThat(value("UPPER", nested("LEFT", literal("otavio"), literal(19)))).isEqualTo("OTAVIO");
        assertThat(value("UPPER", nested("LEFT", literal("otavio"), literal(3)))).isEqualTo("OTA");
        assertThat(value("LOWER", nested("UPPER", literal("Jakarta")))).isEqualTo("jakarta");
        assertThat(value("LENGTH", nested("UPPER", literal("Java")))).isEqualTo(4);
        assertThat(value("LENGTH", nested("LEFT", literal("Jakarta"), literal(3)))).isEqualTo(3);
        assertThat(value("LEFT", literal("Jakarta"), nested("LENGTH", literal("Java")))).isEqualTo("Jaka");
        assertThat(value("RIGHT", literal("Jakarta"), nested("ABS", literal(-3)))).isEqualTo("rta");
        assertThat(value("ABS", nested("ABS", literal(-10)))).isEqualTo(10);
    }

    @Test
    void shouldRejectStructurallyInvalidFunctions() {
        assertThatThrownBy(() -> EvaluableFunction.of("SOUNDEX", literal("a")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EvaluableFunction.of("UPPER"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EvaluableFunction.of("UPPER", FunctionArgument.attribute("name")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EvaluableFunction.of("UPPER", FunctionArgument.function(
                NativeFunction.of("LOWER", FunctionArgument.attribute("name")))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EvaluableFunction.of("LEFT", literal("abc"), nested("UPPER", literal("a"))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EvaluableFunction.of(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldBeLazyOnConstructionAndInspection() {
        AtomicInteger counter = new AtomicInteger();
        EvaluableFunction inner = EvaluableFunction.of("LEFT", lazy(counter, () -> "otavio"), literal(19));
        EvaluableFunction outer = EvaluableFunction.of("UPPER", FunctionArgument.function(inner));

        assertThat(outer.name()).isEqualTo("UPPER");
        assertThat(outer.signature()).contains(StandardFunction.UPPER);
        assertThat(outer.arguments()).hasSize(1);
        assertThat(inner.arguments()).hasSize(2);
        StandardFunction.UPPER.validate(outer.arguments());
        assertThat(outer.arguments().get(0).isEvaluable()).isTrue();
        assertThat(outer.toString()).contains("UPPER");
        assertThat(counter).hasValue(0);

        assertThat(outer.get()).isEqualTo("OTAVIO");
        assertThat(counter).hasValue(1);
    }

    @Test
    void shouldMemoizeAcrossAllValueOperations() {
        AtomicInteger counter = new AtomicInteger();
        Value value = EvaluableFunction.of("UPPER", lazy(counter, () -> "otavio"));

        assertThat(value.isNull()).isFalse();
        assertThat(value.isInstanceOf(String.class)).isTrue();
        assertThat(value.get()).isEqualTo("OTAVIO");
        assertThat(value.get(String.class)).isEqualTo("OTAVIO");
        assertThat(value.get()).isEqualTo("OTAVIO");
        assertThat(counter).hasValue(1);
    }

    @Test
    void shouldMemoizeNull() {
        AtomicInteger counter = new AtomicInteger();
        EvaluableFunction function = EvaluableFunction.of("UPPER", lazy(counter, () -> null));
        assertThat(counter).hasValue(0);
        assertThat(function.isNull()).isTrue();
        assertThat(function.get()).isNull();
        assertThat(function.isNull()).isTrue();
        assertThat(function.get()).isNull();
        assertThat(counter).hasValue(1);
    }

    @Test
    void shouldMemoizeNestedResults() {
        AtomicInteger counter = new AtomicInteger();
        EvaluableFunction inner = EvaluableFunction.of("UPPER", lazy(counter, () -> "a"));
        EvaluableFunction first = EvaluableFunction.of("LOWER", FunctionArgument.function(inner));
        EvaluableFunction second = EvaluableFunction.of("LENGTH", FunctionArgument.function(inner));

        assertThat(first.get()).isEqualTo("a");
        assertThat(second.get()).isEqualTo(1);
        assertThat(first.get()).isEqualTo("a");
        assertThat(inner.get()).isEqualTo("A");
        assertThat(counter).hasValue(1);
    }

    @Test
    void shouldResolveEachArgumentOnceAndNotShareStateBetweenInstances() {
        AtomicInteger counter = new AtomicInteger();
        FunctionArgument argument = lazy(counter, () -> "abc");
        EvaluableFunction one = EvaluableFunction.of("LEFT", argument, literal(1));
        EvaluableFunction two = EvaluableFunction.of("LEFT", argument, literal(2));
        assertThat(one.get()).isEqualTo("a");
        assertThat(counter).hasValue(1);
        assertThat(two.get()).isEqualTo("ab");
        assertThat(counter).hasValue(2);
    }

    @Test
    void shouldFollowLifecycle() {
        AtomicReference<Object> source = new AtomicReference<>("otavio");
        AtomicInteger counter = new AtomicInteger();
        EvaluableFunction function = EvaluableFunction.of("UPPER", lazy(counter, source::get));

        source.set("java");
        assertThat(counter).hasValue(0);
        assertThat(function.get()).isEqualTo("JAVA");
        source.set("jakarta");
        assertThat(function.get()).isEqualTo("JAVA");
        assertThat(counter).hasValue(1);

        EvaluableFunction fresh = EvaluableFunction.of("UPPER", lazy(counter, source::get));
        assertThat(counter).hasValue(1);
        assertThat(fresh.get()).isEqualTo("JAKARTA");
    }

    @Test
    void shouldFollowLifecycleForNestedTrees() {
        AtomicReference<Object> source = new AtomicReference<>("otavio");
        AtomicInteger counter = new AtomicInteger();
        java.util.function.Supplier<EvaluableFunction> tree = () -> EvaluableFunction.of("UPPER",
                nested("LEFT", lazy(counter, source::get), literal(19)));
        EvaluableFunction old = tree.get();
        assertThat(old.get()).isEqualTo("OTAVIO");
        source.set("jakarta");
        assertThat(old.get()).isEqualTo("OTAVIO");
        EvaluableFunction fresh = tree.get();
        assertThat(fresh.get()).isEqualTo("JAKARTA");
        assertThat(counter).hasValue(2);
    }

    @Test
    void shouldNotMemoizeFailures() {
        AtomicInteger counter = new AtomicInteger();
        AtomicReference<Object> source = new AtomicReference<>(123);
        EvaluableFunction function = EvaluableFunction.of("UPPER", lazy(counter, source::get));

        assertThatThrownBy(function::get).isInstanceOf(IllegalArgumentException.class);
        assertThat(counter).hasValue(1);
        assertThatThrownBy(function::isNull).isInstanceOf(IllegalArgumentException.class);
        assertThat(counter).hasValue(2);

        source.set("ok");
        assertThat(function.get()).isEqualTo("OK");
        assertThat(function.isNull()).isFalse();
        assertThat(counter).hasValue(3);
    }

    @Test
    void shouldKeepResultWhenConversionFails() {
        AtomicInteger counter = new AtomicInteger();
        EvaluableFunction function = EvaluableFunction.of("UPPER", lazy(counter, () -> "otavio"));
        assertThatThrownBy(() -> function.get(Integer.class)).isInstanceOf(RuntimeException.class);
        assertThat(function.get()).isEqualTo("OTAVIO");
        assertThat(function.get(String.class)).isEqualTo("OTAVIO");
        assertThat(counter).hasValue(1);
    }

    @Test
    void shouldFollowValueConversionConventionsForResults() {
        assertThat(EvaluableFunction.of("LENGTH", literal("abc")).get(Long.class)).isEqualTo(3L);
        assertThat(EvaluableFunction.of("LENGTH", literal("abc")).isInstanceOf(Integer.class)).isTrue();
        assertThat(EvaluableFunction.of("LENGTH", literal("abc")).isInstanceOf(String.class)).isFalse();
        assertThat(EvaluableFunction.of("ABS", literal(-3)).get(String.class)).isEqualTo("3");
    }
}
