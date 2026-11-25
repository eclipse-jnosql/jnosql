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
import java.util.Objects;

/**
 * A {@link Function} whose result depends on stored database records, for example {@code UPPER(name)},
 * {@code LEFT(name, 3)}, {@code LEFT(name, :length)} or {@code UPPER(LOWER(name))}.
 * <p>
 * A native function represents an <em>operation</em>, not an already resolved value:
 * <ul>
 *   <li>it preserves its structured representation (name and {@link FunctionArgument arguments}); it is never
 *   reduced to a string with a database expression;</li>
 *   <li>it is <strong>not</strong> a {@link org.eclipse.jnosql.communication.Value} and exposes no Java-side
 *   evaluation;</li>
 *   <li>the database driver is responsible for translating it to the native query, and may explicitly reject a
 *   function it does not support, or one whose native semantics differ from the {@link FunctionSignature}
 *   contract, instead of silently producing incorrect results;</li>
 *   <li>it must never cause implicit client-side filtering or the retrieval of records only to evaluate the
 *   function in JNoSQL.</li>
 * </ul>
 * For example {@code WHERE UPPER(name) = 'OTAVIO'} delivers the structured {@code UPPER(name)} to the driver.
 * In {@code WHERE UPPER(name) = UPPER(LEFT(:name, 19))} the left side is native and the right side is an
 * {@link EvaluableFunction}, seen by the driver as the plain value {@code 'OTAVIO'} when {@code :name} is bound
 * to {@code "otavio"}.
 * <p>
 * A native function may contain evaluable arguments, such as {@code LEFT(name, LENGTH(:prefix))}, which allows
 * partial evaluation in a future contribution. Inspecting the arguments never evaluates them. Implementations
 * may be provided externally; they should validate with {@link #requireNative(List)}.
 *
 * @see EvaluableFunction
 */
public non-sealed interface NativeFunction extends Function {

    /**
     * Creates a native function after structural validation. The arguments are defensively copied, and no
     * argument is evaluated.
     * <p>
     * Standard functions are validated against their {@link StandardFunction} signature; other names are
     * accepted with any arguments.
     *
     * @param name      the function name
     * @param arguments the arguments, at least one of them depending on stored records, directly or transitively
     * @return the native function
     * @throws NullPointerException     when the name or an argument is null
     * @throws IllegalArgumentException when the name is blank, the arguments do not match the signature, or no
     *                                  argument depends on stored records
     */
    static NativeFunction of(String name, FunctionArgument... arguments) {
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(arguments, "arguments are required");
        if (name.isBlank()) {
            throw new IllegalArgumentException("the function name must not be blank");
        }
        List<FunctionArgument> copy = List.of(arguments);
        StandardFunction.find(name).ifPresent(signature -> signature.validate(copy));
        requireNative(copy);
        return new DefaultNativeFunction(name, copy);
    }

    /**
     * Structural check for implementations: at least one argument must depend on stored records.
     *
     * @param arguments the arguments
     * @throws NullPointerException     when the arguments are null
     * @throws IllegalArgumentException when every argument is evaluable
     */
    static void requireNative(List<FunctionArgument> arguments) {
        Objects.requireNonNull(arguments, "arguments are required");
        if (arguments.stream().noneMatch(argument -> !argument.isEvaluable())) {
            throw new IllegalArgumentException(
                    "A native function requires at least one argument that depends on stored records");
        }
    }
}
