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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

/**
 * Lazy, memoized implementation of the six standard functions. The state is a single reference:
 * {@code null} means <em>not evaluated</em>, otherwise it holds the successful result wrapped in a
 * {@link Value} ({@link Value#ofNull()} for a null result), so a null result is memoized too and every
 * {@code Value} operation delegates to the same result. A failed evaluation leaves the state untouched,
 * so the next access evaluates again. Not thread-safe, see {@link EvaluableFunction}.
 * <p>
 * Characters are Unicode code points for LENGTH, LEFT and RIGHT, so surrogate pairs are never split.
 */
final class DefaultEvaluableFunction implements EvaluableFunction {

    private final StandardFunction function;
    private final List<FunctionArgument> arguments;
    private Value result;

    DefaultEvaluableFunction(StandardFunction function, List<FunctionArgument> arguments) {
        this.function = function;
        this.arguments = List.copyOf(arguments);
    }

    @Override
    public String name() {
        return function.name();
    }

    @Override
    public List<FunctionArgument> arguments() {
        return arguments;
    }

    @Override
    public Object get() {
        return evaluated().get();
    }

    @Override
    public <T> T get(Class<T> type) {
        return evaluated().get(type);
    }

    @Override
    public <T> T get(TypeSupplier<T> supplier) {
        return evaluated().get(supplier);
    }

    @Override
    public boolean isInstanceOf(Class<?> type) {
        return evaluated().isInstanceOf(type);
    }

    @Override
    public boolean isNull() {
        return evaluated().isNull();
    }

    @Override
    public String toString() {
        return "EvaluableFunction{" + function + ", arguments=" + arguments + '}';
    }

    private Value evaluated() {
        if (result == null) {
            result = Value.of(evaluate());
        }
        return result;
    }

    private Object evaluate() {
        List<ArgumentType> types = function.parameters();
        Object[] values = new Object[arguments.size()];
        boolean hasNull = false;
        for (int index = 0; index < values.length; index++) {
            Object value = resolve(index);
            if (value != null && !types.get(index).accepts(value)) {
                throw new IllegalArgumentException("Invalid argument type for " + name() + " at position "
                        + (index + 1) + ": expected " + types.get(index) + " but found "
                        + value.getClass().getName());
            }
            hasNull |= value == null;
            values[index] = value;
        }
        Integer length = null;
        if (values.length == 2 && values[1] != null) {
            // a negative length is invalid even when the other argument is null
            length = length((Number) values[1]);
        }
        if (hasNull) {
            return null;
        }
        return switch (function) {
            case ABS -> abs((Number) values[0]);
            case LENGTH -> ((String) values[0]).codePointCount(0, ((String) values[0]).length());
            case LOWER -> ((String) values[0]).toLowerCase(Locale.ROOT);
            case UPPER -> ((String) values[0]).toUpperCase(Locale.ROOT);
            case LEFT -> left((String) values[0], length);
            case RIGHT -> right((String) values[0], length);
        };
    }

    private Object resolve(int index) {
        return switch (arguments.get(index)) {
            case FunctionArgument.Literal literal -> literal.value();
            case FunctionArgument.ValueArgument value -> value.value().get();
            case FunctionArgument.EvaluableArgument nested -> nested.function().get();
            case FunctionArgument.NamedParameter parameter -> throw new IllegalStateException(
                    "Unbound parameter :" + parameter.name() + " in " + name() + " at position " + (index + 1));
            case FunctionArgument.PositionalParameter parameter -> throw new IllegalStateException(
                    "Unbound parameter ?" + parameter.position() + " in " + name() + " at position "
                            + (index + 1));
            case FunctionArgument.AttributePath path -> throw new IllegalStateException(
                    "An evaluable function cannot resolve the stored attribute " + path.path());
            case FunctionArgument.NativeArgument nested -> throw new IllegalStateException(
                    "An evaluable function cannot evaluate the native function " + nested.function().name());
        };
    }

    // a length beyond the int range is clamped: it is larger than any string
    private int length(Number number) {
        if (number instanceof BigInteger big) {
            if (big.signum() < 0) {
                throw negative(number);
            }
            return big.bitLength() > 31 ? Integer.MAX_VALUE : big.intValue();
        }
        long value = number.longValue();
        if (value < 0) {
            throw negative(number);
        }
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    private IllegalArgumentException negative(Number number) {
        return new IllegalArgumentException("Invalid argument value for " + name()
                + " at position 2: the length must not be negative but was " + number);
    }

    private static String left(String text, int length) {
        if (length >= text.length()) {
            return text;
        }
        int count = text.codePointCount(0, text.length());
        if (length >= count) {
            return text;
        }
        return text.substring(0, text.offsetByCodePoints(0, length));
    }

    private static String right(String text, int length) {
        if (length >= text.length()) {
            return text;
        }
        int count = text.codePointCount(0, text.length());
        if (length >= count) {
            return text;
        }
        return text.substring(text.offsetByCodePoints(text.length(), -length));
    }

    private Number abs(Number number) {
        return switch (number) {
            case Byte value -> {
                if (value == Byte.MIN_VALUE) {
                    throw overflow(number);
                }
                yield (byte) Math.abs(value);
            }
            case Short value -> {
                if (value == Short.MIN_VALUE) {
                    throw overflow(number);
                }
                yield (short) Math.abs(value);
            }
            case Integer value -> {
                if (value == Integer.MIN_VALUE) {
                    throw overflow(number);
                }
                yield Math.abs(value);
            }
            case Long value -> {
                if (value == Long.MIN_VALUE) {
                    throw overflow(number);
                }
                yield Math.abs(value);
            }
            case Float value -> Math.abs(value);
            case Double value -> Math.abs(value);
            case BigInteger value -> value.abs();
            case BigDecimal value -> value.abs();
            default -> throw new IllegalArgumentException("Unsupported number type for " + name() + ": "
                    + number.getClass().getName());
        };
    }

    private ArithmeticException overflow(Number number) {
        return new ArithmeticException("Numeric overflow evaluating " + name() + "(" + number + ")");
    }
}
