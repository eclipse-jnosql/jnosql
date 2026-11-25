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

import org.eclipse.jnosql.communication.Value;

/**
 * A {@link Function} that JNoSQL can compute independently of stored database records, for example
 * {@code UPPER('otavio')}, {@code UPPER(LEFT(:name, 19))} or {@code ABS(:amount)}.
 * <p>
 * An evaluable function <em>is a</em> {@link Value}. A database driver receiving
 * {@code WHERE name = UPPER(LEFT(:name, 19))} with {@code :name} bound to {@code "otavio"} just reads the value
 * {@code "OTAVIO"}: it does not need to recognize the function, inspect its arguments or implement any
 * function logic. Implementations may be provided externally.
 *
 * <h2>Lazy evaluation with memoization (call-by-need)</h2>
 * Implementations must follow this contract. It is a requirement on implementations; this interface itself
 * contains no evaluation code.
 * <ul>
 *   <li>Construction, factory invocation, {@link #name()}, {@link #arguments()}, {@link #signature()}, query
 *   parsing and statement preparation <strong>must not</strong> evaluate the arguments.</li>
 *   <li>The first operation that needs the actual value triggers the evaluation of the arguments and of the
 *   function. That includes {@link #get()}, {@link #get(Class)}, {@link #get(org.eclipse.jnosql.communication.TypeSupplier)},
 *   {@link #isInstanceOf(Class)} and {@link #isNull()}.</li>
 *   <li>After a successful evaluation the result is retained and reused for the lifetime of the instance. All
 *   the {@code Value} operations observe the same memoized result, they must not recompute it independently.</li>
 *   <li>A successful <strong>null</strong> result is memoized too. An implementation must distinguish
 *   <em>not evaluated</em>, <em>evaluated to null</em> and <em>evaluated to a non-null value</em>; {@code null}
 *   alone must not represent the unevaluated state.</li>
 *   <li>Typed conversion and type inspection operate on the evaluated result, not on the function representation,
 *   and follow the normal JNoSQL {@code Value} conversion conventions. This differs from the arguments, which
 *   must satisfy the strict {@link FunctionSignature}.</li>
 *   <li>Nested evaluable functions are lazy and memoized the same way, and constructing the outer function does
 *   not evaluate the inner one.</li>
 * </ul>
 *
 * <h2>Lifecycle and ownership</h2>
 * An instance represents a <strong>single logical evaluation lifecycle</strong>. Its owner creates it with the
 * appropriate argument context. Once successfully evaluated, it keeps its result even if an underlying supplier or
 * mutable argument changes later. A new execution with different arguments, for instance a prepared statement
 * rebinding {@code :name} from {@code "otavio"} to {@code "jakarta"}, must use a <strong>fresh instance</strong>
 * (yielding {@code "JAKARTA"}), and the whole execution-dependent function tree must be rebuilt with the new
 * context: creating a fresh outer function over a previously evaluated inner function is incorrect.
 * <p>
 * For that reason the API deliberately has <strong>no</strong> {@code reset}, {@code invalidate}, {@code refresh}
 * or {@code clearCache} operation, no parameter tracking, no global cache and no expiration. The memoized result
 * belongs only to its instance, and independent instances must not share mutable evaluation state.
 *
 * <h2>Failure and concurrency</h2>
 * A failed evaluation (unbound parameter, invalid argument type or value, numeric overflow) is not a successful
 * null result and must be reported by an exception. This API does not define exception memoization or retry
 * policy: whether a later access after a failure retries is implementation-specific and must be documented by
 * the implementation. Instances are <strong>not required to be thread-safe</strong>; callers must not share one
 * instance between threads without external synchronization unless the implementation states otherwise.
 *
 * <h2>Structure</h2>
 * An evaluable function must not contain an attribute path or a native function, directly or transitively.
 * Implementations should check this with {@link #requireEvaluable(java.util.List)} during construction, which
 * validates the structure without resolving any argument. Argument types known only at evaluation time are
 * validated then.
 *
 * <h2>Standard implementation</h2>
 * {@link #of(String, FunctionArgument...)} creates the lazy, memoized implementation of the six standard functions
 * (the semantics are those of {@link StandardFunction}, with the choices below). Intended usage, expressed in JDQL
 * terms (JDQL parser execution is <strong>not</strong> supported yet): {@code UPPER(LEFT('otavio', 19))} is
 * {@code "OTAVIO"}, {@code LENGTH(UPPER('Jakarta'))} is {@code 7} and {@code LEFT('Jakarta', LENGTH('Java'))} is
 * {@code "Jaka"}.
 * <ul>
 *   <li>Arguments are resolved in order on the first value access, once each: literals, {@link FunctionArgument.ValueArgument}
 *   values and nested evaluable functions. A parameter whose binding is owned elsewhere is represented by a lazy
 *   {@code ValueArgument}; an unresolved {@code NamedParameter} or {@code PositionalParameter} fails the
 *   evaluation with {@link IllegalStateException}, and it is never treated as null.</li>
 *   <li>Arguments are validated strictly (Jakarta Query, no implicit conversion): a wrong type fails with
 *   {@link IllegalArgumentException} naming the function, position, expected and actual type; a negative LEFT/RIGHT
 *   length fails with {@link IllegalArgumentException}; a numeric overflow in ABS ({@code Byte.MIN_VALUE},
 *   {@code Short.MIN_VALUE}, {@code Integer.MIN_VALUE}, {@code Long.MIN_VALUE}) fails with
 *   {@link ArithmeticException}. A null argument yields null only after every argument resolved and validated
 *   successfully, so null never hides a failure.</li>
 *   <li>Result types are exact: LENGTH returns {@code Integer}; ABS returns the same numeric type, keeping
 *   {@code BigDecimal} scale and never widening. Float and double {@code NaN} stay {@code NaN}, infinities become
 *   positive infinity and {@code -0.0} becomes {@code 0.0} (JNoSQL decision, as {@link Math#abs(double)}).
 *   {@code Number} types outside the supported ones are rejected.</li>
 *   <li>LOWER and UPPER use {@link java.util.Locale#ROOT}. LENGTH, LEFT and RIGHT count Unicode code points
 *   (JNoSQL decision; the Jakarta Query specification does not define it) and never split surrogate pairs.
 *   LEFT/RIGHT with a length above the string length return the same string without copying; lengths beyond
 *   {@code Integer.MAX_VALUE} are valid and do not overflow.</li>
 *   <li>A failed evaluation is not memoized; a later access evaluates again, although nested functions that
 *   already succeeded keep their results. The standard implementation is not thread-safe.</li>
 * </ul>
 * Reading the result follows the normal {@code Value} conversions, and a failing conversion does not invalidate the
 * memoized result. A new logical execution context requires a new instance, including a new nested tree.
 *
 * @see NativeFunction
 */
public non-sealed interface EvaluableFunction extends Function, Value {

    /**
     * Creates a lazy, memoized evaluable function for one of the six standard functions. Only the structure is
     * validated (name, argument count, statically known literal and nested result types, and the absence of
     * stored-record dependencies); no argument is resolved here. The arguments are copied.
     *
     * @param name      the standard function name, case-insensitive
     * @param arguments the arguments
     * @return a new, unevaluated function whose memoized result belongs to that instance only
     * @throws NullPointerException     when a parameter or an argument is null
     * @throws IllegalArgumentException when the name is not a standard function, the structure is invalid or an
     *                                  argument depends on stored records
     */
    static EvaluableFunction of(String name, FunctionArgument... arguments) {
        java.util.Objects.requireNonNull(name, "name is required");
        java.util.Objects.requireNonNull(arguments, "arguments are required");
        StandardFunction function = StandardFunction.find(name).orElseThrow(() ->
                new IllegalArgumentException("Unsupported evaluable function: " + name));
        java.util.List<FunctionArgument> copy = java.util.List.of(arguments);
        function.validate(copy);
        requireEvaluable(copy);
        return new DefaultEvaluableFunction(function, copy);
    }

    /**
     * Structural check for implementations: every argument must be evaluable, which means that the function
     * cannot depend on stored records. It never resolves any argument.
     *
     * @param arguments the arguments
     * @throws NullPointerException     when the arguments are null
     * @throws IllegalArgumentException when an argument is an attribute path or a native function
     */
    static void requireEvaluable(java.util.List<FunctionArgument> arguments) {
        java.util.Objects.requireNonNull(arguments, "arguments are required");
        for (FunctionArgument argument : arguments) {
            java.util.Objects.requireNonNull(argument, "argument is required");
            if (!argument.isEvaluable()) {
                throw new IllegalArgumentException(
                        "An evaluable function cannot depend on stored records: " + argument);
            }
        }
    }
}
