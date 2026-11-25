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

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The standard Jakarta Query functions supported by this API, verified against the function table of the
 * Jakarta Query specification (language expressions chapter).
 * <table>
 *   <caption>Standard functions</caption>
 *   <tr><th>Function</th><th>Arguments</th><th>Result</th></tr>
 *   <tr><td>ABS</td><td>numeric</td><td>same numeric type</td></tr>
 *   <tr><td>LENGTH</td><td>string</td><td>integer</td></tr>
 *   <tr><td>LOWER</td><td>string</td><td>string</td></tr>
 *   <tr><td>UPPER</td><td>string</td><td>string</td></tr>
 *   <tr><td>LEFT</td><td>string, integer</td><td>string</td></tr>
 *   <tr><td>RIGHT</td><td>string, integer</td><td>string</td></tr>
 * </table>
 * <p>
 * The following contracts apply to every execution of these functions, Java-side or native. The rows marked
 * <em>specification</em> are normative in Jakarta Query; the rest are <em>JNoSQL decisions</em> that a future
 * implementation must follow. None of this behavior is available yet: this contribution only defines the contract.
 *
 * <h2>Null handling (specification)</h2>
 * A null argument makes the whole application null: {@code UPPER(null)}, {@code LOWER(null)}, {@code LENGTH(null)},
 * {@code ABS(null)}, {@code LEFT(null, 3)}, {@code LEFT('abc', null)}, {@code RIGHT(null, 3)} and
 * {@code UPPER(LEFT(null, 3))} are all null. Null propagation must not hide an unbound parameter, an invalid
 * structure or any other failure.
 *
 * <h2>LEFT and RIGHT (JNoSQL decisions)</h2>
 * {@code LEFT('abc', 2)} is {@code "ab"}, {@code LEFT('abc', 3)} and {@code LEFT('abc', 4)} are {@code "abc"},
 * {@code LEFT('abc', 0)} and {@code LEFT('', 10)} are {@code ""}, {@code RIGHT('abc', 2)} is {@code "bc"},
 * {@code RIGHT('abc', 4)} is {@code "abc"} and {@code RIGHT('abc', 0)} is {@code ""}.
 * A length greater than the string returns the whole string, without allocating a buffer of that length and
 * without arithmetic overflow for large lengths such as {@link Long#MAX_VALUE}. A negative length is an invalid
 * argument. A fractional length ({@code 1.5}), a string length ({@code '2'}) or a non-string first argument
 * ({@code LEFT(123, 2)}) is an invalid argument type. Numbers are never truncated or narrowed silently.
 *
 * <h2>LOWER, UPPER and LENGTH (JNoSQL decisions)</h2>
 * {@code UPPER('otavio')} is {@code "OTAVIO"}, {@code LOWER('OTAVIO')} is {@code "otavio"},
 * {@code UPPER('')} is {@code ""}, {@code LENGTH('')} is {@code 0} and {@code LENGTH('abc')} is {@code 3}.
 * {@code UPPER(123)}, {@code UPPER(TRUE)}, {@code LOWER(123)} and {@code LENGTH(123)} are invalid argument types:
 * numbers, booleans and arbitrary objects are never converted to strings. Java-side case conversion must be
 * locale-independent, using {@link Locale#ROOT}.
 *
 * <h2>ABS (JNoSQL decisions)</h2>
 * The numeric type is preserved for Byte, Short, Integer, Long, Float, Double, BigInteger and BigDecimal;
 * values are never converted to {@code Double} and strings are never parsed. An overflow, such as the absolute
 * value of {@link Integer#MIN_VALUE}, must fail explicitly rather than return a negative value or change the type.
 *
 * <h2>Outstanding design decisions</h2>
 * <ul>
 *   <li><strong>Character counting.</strong> The Jakarta Query function table speaks of the "length" of a string
 *   and of a "prefix"/"suffix", and of character positions for other functions, but does not define whether a
 *   character is a UTF-16 code unit, a Unicode code point or a grapheme cluster. This is not decided here.
 *   {@code LENGTH}, {@code LEFT} and {@code RIGHT} must eventually share one consistent policy, and the policy
 *   must not be silently inherited from Java UTF-16 semantics.</li>
 *   <li>Support for {@link Number} implementations other than the ones listed above.</li>
 *   <li>{@code ABS} for NaN, infinity and negative zero ({@code -0.0}).</li>
 *   <li>Locale-independent case conversion beyond {@link Locale#ROOT} for databases that apply their own
 *   collation rules; such a native function must be rejected if it cannot provide equivalent semantics.</li>
 * </ul>
 */
public enum StandardFunction implements FunctionSignature {

    /**
     * {@code ABS(numeric)}, result of the same numeric type.
     */
    ABS(ResultType.SAME_AS_ARGUMENT, ArgumentType.NUMERIC),

    /**
     * {@code LENGTH(string)}, result is an integer.
     */
    LENGTH(ResultType.INTEGER, ArgumentType.STRING),

    /**
     * {@code LOWER(string)}, result is a string.
     */
    LOWER(ResultType.STRING, ArgumentType.STRING),

    /**
     * {@code UPPER(string)}, result is a string.
     */
    UPPER(ResultType.STRING, ArgumentType.STRING),

    /**
     * {@code LEFT(string, integer)}, result is a string.
     */
    LEFT(ResultType.STRING, ArgumentType.STRING, ArgumentType.INTEGER),

    /**
     * {@code RIGHT(string, integer)}, result is a string.
     */
    RIGHT(ResultType.STRING, ArgumentType.STRING, ArgumentType.INTEGER);

    private final ResultType result;
    private final List<ArgumentType> parameters;

    StandardFunction(ResultType result, ArgumentType... parameters) {
        this.result = result;
        this.parameters = List.of(parameters);
    }

    @Override
    public List<ArgumentType> parameters() {
        return parameters;
    }

    @Override
    public ResultType result() {
        return result;
    }

    /**
     * Finds a standard function by name, ignoring case and independently of the default locale.
     *
     * @param name the function name
     * @return the standard function, or empty when the name is null or not a standard function
     */
    public static Optional<StandardFunction> find(String name) {
        if (name == null) {
            return Optional.empty();
        }
        String normalized = name.toUpperCase(Locale.ROOT);
        for (StandardFunction function : values()) {
            if (function.name().equals(normalized)) {
                return Optional.of(function);
            }
        }
        return Optional.empty();
    }
}
