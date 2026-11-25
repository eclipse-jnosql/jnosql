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
import java.util.Optional;

/**
 * The semantic contract of a function, shared by {@link EvaluableFunction} and {@link NativeFunction}.
 * It describes the name, the parameters, the result and the null handling, and it performs
 * <em>structural</em> validation of the arguments.
 * <p>
 * The standard Jakarta Query functions are described by {@link StandardFunction}. The interface is open so that
 * future functions can be described without extending that enumeration.
 *
 * <h2>Structural versus evaluation-time validation</h2>
 * {@link #validate(List)} checks only what is known without resolving anything: the argument count and the Java
 * type of literals and of nested functions with a known result type. It never resolves a parameter or a
 * {@link org.eclipse.jnosql.communication.Value}. Bound parameter types, missing bindings, argument constraints that
 * depend on bound values (for example a negative {@code LEFT} length), numeric overflow and result validation are
 * <strong>evaluation-time</strong> concerns of a future implementation.
 *
 * <h2>Null handling</h2>
 * Per the Jakarta Query specification, when any argument of a function application evaluates to null, the whole
 * application evaluates to null, see {@link #nullPropagating()}. A <em>bound</em> null follows this rule;
 * an <em>unbound</em> parameter is an invalid execution state and must fail instead of propagating null.
 */
public interface FunctionSignature {

    /**
     * @return the function name, upper case
     */
    String name();

    /**
     * @return the expected argument types, in order; its size is the argument count
     */
    List<ArgumentType> parameters();

    /**
     * @return the result type
     */
    ResultType result();

    /**
     * @return {@code true} when a null argument makes the whole application null; always {@code true} for
     * the standard functions
     */
    default boolean nullPropagating() {
        return true;
    }

    /**
     * Validates the structure of the arguments without resolving any value.
     *
     * @param arguments the arguments
     * @throws NullPointerException     when the arguments are null
     * @throws IllegalArgumentException when the count differs from the signature or a statically known argument
     *                                  has an invalid type
     */
    default void validate(List<FunctionArgument> arguments) {
        java.util.Objects.requireNonNull(arguments, "arguments are required");
        List<ArgumentType> expected = parameters();
        if (arguments.size() != expected.size()) {
            throw new IllegalArgumentException("The function " + name() + " requires " + expected.size()
                    + " argument(s) but received " + arguments.size());
        }
        for (int index = 0; index < expected.size(); index++) {
            validate(index, expected.get(index), arguments.get(index));
        }
    }

    private void validate(int index, ArgumentType type, FunctionArgument argument) {
        java.util.Objects.requireNonNull(argument, "argument " + (index + 1) + " is required");
        // parameters, attributes and values are only checked at evaluation time
        if (argument instanceof FunctionArgument.Literal literal) {
            if (!type.accepts(literal.value())) {
                throw invalid(index, type, literal.value().getClass().getName());
            }
        } else if (argument instanceof FunctionArgument.EvaluableArgument nested) {
            checkResult(index, type, nested.function());
        } else if (argument instanceof FunctionArgument.NativeArgument nested) {
            checkResult(index, type, nested.function());
        }
    }

    private void checkResult(int index, ArgumentType type, Function nested) {
        Optional<FunctionSignature> nestedSignature = nested.signature();
        if (nestedSignature.isEmpty()) {
            return;
        }
        ResultType result = nestedSignature.get().result();
        boolean compatible = switch (result) {
            case STRING -> type == ArgumentType.STRING;
            case INTEGER -> type == ArgumentType.INTEGER || type == ArgumentType.NUMERIC;
            case SAME_AS_ARGUMENT -> type == ArgumentType.NUMERIC || type == ArgumentType.INTEGER;
        };
        if (!compatible) {
            throw invalid(index, type, "the result of " + nested.name() + " (" + result + ")");
        }
    }

    private IllegalArgumentException invalid(int index, ArgumentType type, String found) {
        return new IllegalArgumentException("Invalid argument type for " + name() + " at position " + (index + 1)
                + ": expected " + type + " but found " + found);
    }
}
