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

import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.function.NativeFunction;

import java.util.Objects;

/**
 * One side of a {@link CriteriaCondition} predicate, as seen by a database driver.
 * <p>
 * An operand is exactly one of:
 * <ul>
 *     <li>{@link Attribute}: a reference to a stored attribute, already translated to its stored field name;</li>
 *     <li>{@link Constant}: an ordinary {@link Value}. Literals, bound parameters and evaluable functions such as
 *     {@code UPPER(:name)} are all constants: the driver reads them through the {@link Value} contract and does not
 *     need to know how the value was produced. Evaluable functions stay lazy and memoized;</li>
 *     <li>{@link Function}: a {@link NativeFunction} that only the database can execute because it depends on stored
 *     attributes, for example {@code UPPER(name)} or {@code LEFT(name, LENGTH(:prefix))}. Its independent arguments
 *     (here {@code LENGTH(:prefix)}) are already exposed as ordinary values and must not be re-evaluated by the
 *     driver.</li>
 * </ul>
 * This type describes the communication contract only. It does not execute anything: a driver that cannot translate
 * a native function with the exact semantics of the shared function contract must reject the query, and must never
 * filter records on the client, drop the condition or treat the function as an attribute name or a literal.
 */
public sealed interface CriteriaOperand {

    /**
     * A stored attribute reference.
     *
     * @param name the stored field name or attribute path
     */
    record Attribute(String name) implements CriteriaOperand {
        public Attribute {
            Objects.requireNonNull(name, "name is required");
        }
    }

    /**
     * An ordinary value, including parameters and evaluable functions.
     *
     * @param value the value
     */
    record Constant(Value value) implements CriteriaOperand {
        public Constant {
            Objects.requireNonNull(value, "value is required");
        }
    }

    /**
     * A function that requires native database execution.
     *
     * @param function the native function, whose attribute arguments are stored field names
     */
    record Function(NativeFunction function) implements CriteriaOperand {
        public Function {
            Objects.requireNonNull(function, "function is required");
        }
    }
}
