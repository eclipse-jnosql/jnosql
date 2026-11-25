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
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FunctionApiTest {

    /**
     * Test double: it only counts value resolutions, it does not implement evaluation or memoization.
     */
    static final class CountingValue implements Value {
        final AtomicInteger resolutions = new AtomicInteger();

        private Object resolve() {
            resolutions.incrementAndGet();
            return "otavio";
        }

        @Override
        public Object get() {
            return resolve();
        }

        @Override
        public <T> T get(Class<T> type) {
            return type.cast(resolve());
        }

        @Override
        public <T> T get(TypeSupplier<T> supplier) {
            resolve();
            return null;
        }

        @Override
        public boolean isInstanceOf(Class<?> type) {
            return type.isInstance(resolve());
        }

        @Override
        public boolean isNull() {
            return resolve() == null;
        }
    }

    /**
     * Test double for an externally provided evaluable function; it delegates to a counting value.
     */
    record StubEvaluable(String name, List<FunctionArgument> arguments, CountingValue value)
            implements EvaluableFunction {

        StubEvaluable {
            EvaluableFunction.requireEvaluable(arguments);
            arguments = List.copyOf(arguments);
        }

        @Override
        public Object get() {
            return value.get();
        }

        @Override
        public <T> T get(Class<T> type) {
            return value.get(type);
        }

        @Override
        public <T> T get(TypeSupplier<T> supplier) {
            return value.get(supplier);
        }

        @Override
        public boolean isInstanceOf(Class<?> type) {
            return value.isInstanceOf(type);
        }

        @Override
        public boolean isNull() {
            return value.isNull();
        }
    }

    private static StubEvaluable evaluable(String name, CountingValue counter, FunctionArgument... arguments) {
        return new StubEvaluable(name, List.of(arguments), counter);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> standardFunctions() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(StandardFunction.ABS,
                        List.of(ArgumentType.NUMERIC), ResultType.SAME_AS_ARGUMENT),
                org.junit.jupiter.params.provider.Arguments.of(StandardFunction.LENGTH,
                        List.of(ArgumentType.STRING), ResultType.INTEGER),
                org.junit.jupiter.params.provider.Arguments.of(StandardFunction.LOWER,
                        List.of(ArgumentType.STRING), ResultType.STRING),
                org.junit.jupiter.params.provider.Arguments.of(StandardFunction.UPPER,
                        List.of(ArgumentType.STRING), ResultType.STRING),
                org.junit.jupiter.params.provider.Arguments.of(StandardFunction.LEFT,
                        List.of(ArgumentType.STRING, ArgumentType.INTEGER), ResultType.STRING),
                org.junit.jupiter.params.provider.Arguments.of(StandardFunction.RIGHT,
                        List.of(ArgumentType.STRING, ArgumentType.INTEGER), ResultType.STRING));
    }

    @ParameterizedTest
    @MethodSource("standardFunctions")
    void shouldDescribeStandardSignatures(StandardFunction function, List<ArgumentType> parameters,
                                          ResultType result) {
        assertThat(function.parameters()).isEqualTo(parameters);
        assertThat(function.result()).isEqualTo(result);
        assertThat(function.nullPropagating()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(StandardFunction.class)
    void shouldFindStandardFunctionIgnoringCase(StandardFunction function) {
        assertThat(StandardFunction.find(function.name().toLowerCase())).contains(function);
    }

    @Test
    void shouldNotFindUnknownFunction() {
        assertThat(StandardFunction.find("SOUNDEX")).isEmpty();
        assertThat(StandardFunction.find(null)).isEmpty();
    }

    @Test
    void shouldHaveSealedHierarchy() {
        assertThat(Function.class.isSealed()).isTrue();
        assertThat(Function.class.getPermittedSubclasses())
                .containsExactlyInAnyOrder(EvaluableFunction.class, NativeFunction.class);
    }

    @Test
    void shouldAllowExternalImplementations() {
        assertThat(EvaluableFunction.class.isSealed()).isFalse();
        assertThat(NativeFunction.class.isSealed()).isFalse();
    }

    @Test
    void shouldDistinguishValueCompatibility() {
        assertThat(Value.class).isAssignableFrom(EvaluableFunction.class);
        assertThat(Value.class.isAssignableFrom(NativeFunction.class)).isFalse();
        Value value = evaluable("UPPER", new CountingValue(), FunctionArgument.literal("otavio"));
        assertThat(value).isInstanceOf(EvaluableFunction.class);
    }

    @Test
    void shouldNotExposeEvaluationOrCacheManagementOnParent() {
        List<String> names = new ArrayList<>();
        for (var method : Function.class.getDeclaredMethods()) {
            if (!method.isSynthetic()) {
                names.add(method.getName());
            }
        }
        assertThat(names).containsExactlyInAnyOrder("name", "arguments", "signature");
        assertThat(EvaluableFunction.class.getDeclaredMethods())
                .extracting(java.lang.reflect.Method::getName)
                .doesNotContain("reset", "invalidate", "refresh", "clearCache");
    }

    @Test
    void shouldDistinguishArgumentKinds() {
        List<FunctionArgument> arguments = List.of(
                FunctionArgument.literal("name"),
                FunctionArgument.attribute("name"),
                FunctionArgument.named("name"),
                FunctionArgument.positional(1));
        assertThat(arguments).hasSize(4).doesNotHaveDuplicates();
        assertThat(arguments).extracting(Object::getClass).containsExactly(
                FunctionArgument.Literal.class, FunctionArgument.AttributePath.class,
                FunctionArgument.NamedParameter.class, FunctionArgument.PositionalParameter.class);
        assertThat(arguments).extracting(FunctionArgument::isEvaluable)
                .containsExactly(true, false, true, true);
    }

    @Test
    void shouldPreserveArgumentOrder() {
        NativeFunction function = NativeFunction.of("LEFT", FunctionArgument.attribute("name"),
                FunctionArgument.literal(3));
        assertThat(function.arguments()).containsExactly(new FunctionArgument.AttributePath("name"),
                new FunctionArgument.Literal(3));
    }

    @Test
    void shouldCreateNativeFunctions() {
        NativeFunction upper = NativeFunction.of("UPPER", FunctionArgument.attribute("name"));
        assertThat(upper.name()).isEqualTo("UPPER");
        assertThat(upper.signature()).contains(StandardFunction.UPPER);
        assertThat(upper).isNotInstanceOf(Value.class);
    }

    @Test
    void shouldBeImmutableAndDefensive() {
        FunctionArgument[] source = {FunctionArgument.attribute("name"), FunctionArgument.literal(3)};
        NativeFunction function = NativeFunction.of("LEFT", source);
        source[1] = FunctionArgument.literal(99);
        assertThat(function.arguments()).contains(FunctionArgument.literal(3));
        List<FunctionArgument> arguments = function.arguments();
        assertThatThrownBy(() -> arguments.add(FunctionArgument.literal(1)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldSupportNativeWithEvaluableArgument() {
        EvaluableFunction length = evaluable("LENGTH", new CountingValue(), FunctionArgument.named("prefix"));
        NativeFunction left = NativeFunction.of("LEFT", FunctionArgument.attribute("name"),
                FunctionArgument.function(length));
        assertThat(left.arguments().get(1)).isInstanceOf(FunctionArgument.EvaluableArgument.class);
        assertThat(left.arguments().get(1).isEvaluable()).isTrue();
    }

    @Test
    void shouldSupportNestedNative() {
        NativeFunction lower = NativeFunction.of("LOWER", FunctionArgument.attribute("name"));
        NativeFunction upper = NativeFunction.of("UPPER", FunctionArgument.function(lower));
        assertThat(upper.arguments().get(0)).isInstanceOf(FunctionArgument.NativeArgument.class);
        assertThat(upper.arguments().get(0).isEvaluable()).isFalse();
    }

    @Test
    void shouldNotAllowNativeWithoutStoredDependency() {
        assertThatThrownBy(() -> NativeFunction.of("UPPER", FunctionArgument.named("name")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NativeFunction.of("UPPER", FunctionArgument.literal("a")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNativeArgumentsInEvaluable() {
        NativeFunction lower = NativeFunction.of("LOWER", FunctionArgument.attribute("name"));
        assertThatThrownBy(() -> evaluable("UPPER", new CountingValue(), FunctionArgument.attribute("name")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> evaluable("UPPER", new CountingValue(), FunctionArgument.function(lower)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectInvalidArgumentCounts() {
        assertThatThrownBy(() -> NativeFunction.of("UPPER"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NativeFunction.of("UPPER", FunctionArgument.attribute("a"),
                FunctionArgument.attribute("b"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NativeFunction.of("LEFT", FunctionArgument.attribute("a")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NativeFunction.of("RIGHT", FunctionArgument.attribute("a"),
                FunctionArgument.literal(1), FunctionArgument.literal(2)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectInvalidStructure() {
        assertThatThrownBy(() -> NativeFunction.of(null, FunctionArgument.attribute("a")))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> NativeFunction.of(" ", FunctionArgument.attribute("a")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NativeFunction.of("UPPER", (FunctionArgument) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> FunctionArgument.named(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FunctionArgument.positional(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FunctionArgument.attribute(" ")).isInstanceOf(IllegalArgumentException.class);
    }

    static Stream<Object[]> invalidLiteralTypes() {
        return Stream.of(
                new Object[]{"LEFT", List.of(FunctionArgument.literal("abc"), FunctionArgument.literal(1.5))},
                new Object[]{"LEFT", List.of(FunctionArgument.literal("abc"), FunctionArgument.literal("2"))},
                new Object[]{"LEFT", List.of(FunctionArgument.literal(123), FunctionArgument.literal(2))},
                new Object[]{"RIGHT", List.of(FunctionArgument.literal("abc"), FunctionArgument.literal(1.5F))},
                new Object[]{"UPPER", List.of(FunctionArgument.literal(123))},
                new Object[]{"UPPER", List.of(FunctionArgument.literal(true))},
                new Object[]{"LOWER", List.of(FunctionArgument.literal(123))},
                new Object[]{"LENGTH", List.of(FunctionArgument.literal(123))},
                new Object[]{"ABS", List.of(FunctionArgument.literal("10"))});
    }

    @ParameterizedTest
    @MethodSource("invalidLiteralTypes")
    void shouldRejectInvalidLiteralTypes(String name, List<FunctionArgument> arguments) {
        FunctionSignature signature = StandardFunction.find(name).orElseThrow();
        assertThatThrownBy(() -> signature.validate(arguments))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid argument type");
    }

    static Stream<Object> validNumbers() {
        return Stream.of((byte) 1, (short) 1, 1, 1L, 1.0F, 1.0D, BigInteger.ONE, BigDecimal.ONE);
    }

    @ParameterizedTest
    @MethodSource("validNumbers")
    void shouldAcceptEveryNumericTypeForAbs(Object number) {
        assertThatCode(() -> StandardFunction.ABS.validate(List.of(FunctionArgument.literal(number))))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptNullLiteralsAndDeferredArguments() {
        assertThatCode(() -> StandardFunction.LEFT.validate(
                List.of(FunctionArgument.literal(null), FunctionArgument.literal(null))))
                .doesNotThrowAnyException();
        assertThatCode(() -> StandardFunction.LEFT.validate(
                List.of(FunctionArgument.named("name"), FunctionArgument.positional(2))))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldValidateNestedResultTypes() {
        NativeFunction lower = NativeFunction.of("LOWER", FunctionArgument.attribute("name"));
        assertThatCode(() -> NativeFunction.of("UPPER", FunctionArgument.function(lower)))
                .doesNotThrowAnyException();
        NativeFunction length = NativeFunction.of("LENGTH", FunctionArgument.attribute("name"));
        assertThatThrownBy(() -> NativeFunction.of("UPPER", FunctionArgument.function(length)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> NativeFunction.of("LEFT", FunctionArgument.attribute("name"),
                FunctionArgument.function(length))).doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptNonStandardNativeFunction() {
        NativeFunction function = NativeFunction.of("CUSTOM", FunctionArgument.attribute("name"));
        assertThat(function.signature()).isEmpty();
    }

    @Test
    void shouldNotResolveOnConstructionOrInspection() {
        CountingValue counter = new CountingValue();
        CountingValue inner = new CountingValue();
        EvaluableFunction leftFunction = evaluable("LEFT", inner, FunctionArgument.named("name"),
                FunctionArgument.literal(19));
        EvaluableFunction upper = evaluable("UPPER", counter, FunctionArgument.function(leftFunction));

        assertThat(upper.name()).isEqualTo("UPPER");
        assertThat(upper.arguments()).hasSize(1);
        assertThat(upper.signature()).contains(StandardFunction.UPPER);
        assertThat(upper.arguments().get(0).isEvaluable()).isTrue();
        StandardFunction.UPPER.validate(upper.arguments());
        Optional<FunctionSignature> signature = upper.signature();
        assertThat(signature).isPresent();

        assertThat(counter.resolutions).hasValue(0);
        assertThat(inner.resolutions).hasValue(0);
    }

    @Test
    void shouldNotResolveWhenBuildingNativeWithValueArguments() {
        CountingValue counter = new CountingValue();
        NativeFunction function = NativeFunction.of("LEFT", FunctionArgument.attribute("name"),
                new FunctionArgument.ValueArgument(counter));
        function.arguments().forEach(FunctionArgument::isEvaluable);
        assertThat(function.arguments().get(1)).isEqualTo(new FunctionArgument.ValueArgument(counter));
        assertThat(counter.resolutions).hasValue(0);
    }

    @Test
    void shouldSupportAllValueOperationsThroughValueReference() {
        CountingValue counter = new CountingValue();
        Value value = evaluable("UPPER", counter, FunctionArgument.literal("otavio"));
        assertThat(value.isNull()).isFalse();
        assertThat(value.isInstanceOf(String.class)).isTrue();
        assertThat(value.get()).isEqualTo("otavio");
        assertThat(value.get(String.class)).isEqualTo("otavio");
    }
}
