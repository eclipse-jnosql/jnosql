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
 * The common representation of a Jakarta Query function application, such as {@code UPPER(LEFT(:name, 19))}.
 * <p>
 * A function has exactly two execution specializations, and the category is determined by its
 * <strong>arguments</strong>, never by its name:
 * <ul>
 *   <li>{@link EvaluableFunction}: does not depend on stored records. JNoSQL can compute it and, being a
 *   {@link org.eclipse.jnosql.communication.Value}, it is transparent to database drivers.</li>
 *   <li>{@link NativeFunction}: depends on at least one stored attribute. It keeps its structure and the
 *   database driver translates it or rejects it.</li>
 * </ul>
 * This interface exposes no evaluation method because not every function can be evaluated by JNoSQL.
 *
 * <h2>Examples</h2>
 * <table>
 *   <caption>Classification</caption>
 *   <tr><th>Condition</th><th>Representation</th></tr>
 *   <tr><td>{@code WHERE name = UPPER(LEFT('otavio', 19))}</td><td>evaluable; the driver receives {@code 'OTAVIO'}</td></tr>
 *   <tr><td>{@code WHERE name = UPPER(LEFT(:name, 19))}</td><td>evaluable; the driver receives the evaluated value</td></tr>
 *   <tr><td>{@code WHERE UPPER(name) = 'OTAVIO'}</td><td>native; the driver receives the structured {@code UPPER(name)}</td></tr>
 *   <tr><td>{@code WHERE UPPER(name) = UPPER(LEFT(:name, 19))}</td><td>native on the left, evaluable on the right;
 *   with {@code :name} bound to {@code "otavio"} the driver should receive the equivalent of
 *   {@code UPPER(name) = 'OTAVIO'}</td></tr>
 *   <tr><td>{@code LEFT(name, LENGTH(:prefix))}</td><td>native containing an evaluable argument</td></tr>
 * </table>
 * <p>
 * A native function may contain evaluable arguments, which allows a future partial evaluation, for example
 * {@code LEFT(name, LENGTH(:prefix))} with {@code prefix} bound to {@code "OTA"} becoming {@code LEFT(name, 3)}.
 * An evaluable function must never contain a native function or an attribute path.
 *
 * <h2>Scope</h2>
 * This API only represents functions. Function evaluation, memoization, JDQL integration, automatic
 * classification, partial evaluation and driver translation are <strong>not available</strong> yet and are
 * outside the guarantees of this interface.
 *
 * @see EvaluableFunction
 * @see NativeFunction
 * @see FunctionArgument
 * @see FunctionSignature
 */
public sealed interface Function permits EvaluableFunction, NativeFunction {

    /**
     * Returns the function name. Reading it never evaluates the function.
     *
     * @return the function name
     */
    String name();

    /**
     * Returns the structural arguments, in declaration order. The list is immutable and inspecting it never
     * evaluates any argument.
     *
     * @return the arguments
     */
    List<FunctionArgument> arguments();

    /**
     * Returns the shared semantic contract of this function when it is a standard function.
     * Reading it never evaluates the function.
     *
     * @return the signature, or empty when the function is not a standard function
     */
    default Optional<FunctionSignature> signature() {
        return StandardFunction.find(name()).map(FunctionSignature.class::cast);
    }
}
