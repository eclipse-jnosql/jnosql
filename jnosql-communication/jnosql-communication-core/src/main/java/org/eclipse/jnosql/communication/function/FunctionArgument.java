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

import java.util.Objects;

/**
 * The structural representation of a single {@link Function} argument.
 * <p>
 * The representation keeps the semantic distinction between the different kinds of argument, so that
 * {@code UPPER('name')}, {@code UPPER(name)}, {@code UPPER(:name)} and {@code UPPER(?1)} are never
 * indistinguishable strings:
 * <ul>
 *   <li>{@link Literal}: a constant value;</li>
 *   <li>{@link NamedParameter}: {@code :name};</li>
 *   <li>{@link PositionalParameter}: {@code ?1};</li>
 *   <li>{@link AttributePath}: a stored attribute, such as {@code name} or {@code address.city};</li>
 *   <li>{@link EvaluableArgument}: a nested {@link EvaluableFunction};</li>
 *   <li>{@link NativeArgument}: a nested {@link NativeFunction};</li>
 *   <li>{@link ValueArgument}: any other {@link Value}, resolved lazily.</li>
 * </ul>
 * The representation is structurally immutable and composes recursively. <strong>Inspecting an argument never
 * evaluates it</strong>: parameter and {@link Value} arguments are only references, resolution is the
 * responsibility of the evaluation owner.
 * <p>
 * An argument is <em>evaluable</em> when it does not depend on stored records, see {@link #isEvaluable()}.
 * The execution category of a function is derived from its arguments, never from its name.
 */
public sealed interface FunctionArgument {

    /**
     * Indicates whether this argument can be resolved without accessing stored database records.
     * The result is computed from the structure only and never triggers evaluation.
     *
     * @return {@code false} for attribute paths and native functions, {@code true} otherwise
     */
    boolean isEvaluable();

    /**
     * A constant. {@code null} is a legal literal and follows null-propagation semantics.
     *
     * @param value the constant, possibly {@code null}
     */
    record Literal(Object value) implements FunctionArgument {
        @Override
        public boolean isEvaluable() {
            return true;
        }
    }

    /**
     * A named parameter, such as {@code :name}. Only the name is held: the binding is not known at this level
     * and a missing binding is an evaluation-time failure.
     *
     * @param name the parameter name, without the colon
     */
    record NamedParameter(String name) implements FunctionArgument {
        /**
         * @throws NullPointerException     when the name is null
         * @throws IllegalArgumentException when the name is blank
         */
        public NamedParameter {
            Objects.requireNonNull(name, "name is required");
            if (name.isBlank()) {
                throw new IllegalArgumentException("the parameter name must not be blank");
            }
        }

        @Override
        public boolean isEvaluable() {
            return true;
        }
    }

    /**
     * A positional parameter, such as {@code ?1}.
     *
     * @param position the one-based position
     */
    record PositionalParameter(int position) implements FunctionArgument {
        /**
         * @throws IllegalArgumentException when the position is lower than one
         */
        public PositionalParameter {
            if (position < 1) {
                throw new IllegalArgumentException("the parameter position is one-based: " + position);
            }
        }

        @Override
        public boolean isEvaluable() {
            return true;
        }
    }

    /**
     * A stored attribute or attribute path, such as {@code name} or {@code address.city}.
     * Its value depends on stored records, so it makes the enclosing function native.
     *
     * @param path the attribute path
     */
    record AttributePath(String path) implements FunctionArgument {
        /**
         * @throws NullPointerException     when the path is null
         * @throws IllegalArgumentException when the path is blank
         */
        public AttributePath {
            Objects.requireNonNull(path, "path is required");
            if (path.isBlank()) {
                throw new IllegalArgumentException("the attribute path must not be blank");
            }
        }

        @Override
        public boolean isEvaluable() {
            return false;
        }
    }

    /**
     * A nested evaluable function.
     *
     * @param function the nested function; it is not evaluated by this representation
     */
    record EvaluableArgument(EvaluableFunction function) implements FunctionArgument {
        /**
         * @throws NullPointerException when the function is null
         */
        public EvaluableArgument {
            Objects.requireNonNull(function, "function is required");
        }

        @Override
        public boolean isEvaluable() {
            return true;
        }
    }

    /**
     * A nested native function.
     *
     * @param function the nested function
     */
    record NativeArgument(NativeFunction function) implements FunctionArgument {
        /**
         * @throws NullPointerException when the function is null
         */
        public NativeArgument {
            Objects.requireNonNull(function, "function is required");
        }

        @Override
        public boolean isEvaluable() {
            return false;
        }
    }

    /**
     * Any other lazily resolved {@link Value}. The value is never resolved by the representation;
     * calling {@code value()} only returns the reference.
     *
     * @param value the value reference
     */
    record ValueArgument(Value value) implements FunctionArgument {
        /**
         * @throws NullPointerException when the value is null
         */
        public ValueArgument {
            Objects.requireNonNull(value, "value is required");
        }

        @Override
        public boolean isEvaluable() {
            return true;
        }
    }

    /**
     * Creates a literal argument.
     *
     * @param value the constant, possibly {@code null}
     * @return the argument
     */
    static FunctionArgument literal(Object value) {
        return new Literal(value);
    }

    /**
     * Creates a named parameter argument.
     *
     * @param name the parameter name
     * @return the argument
     */
    static FunctionArgument named(String name) {
        return new NamedParameter(name);
    }

    /**
     * Creates a positional parameter argument.
     *
     * @param position the one-based position
     * @return the argument
     */
    static FunctionArgument positional(int position) {
        return new PositionalParameter(position);
    }

    /**
     * Creates an attribute path argument.
     *
     * @param path the attribute path
     * @return the argument
     */
    static FunctionArgument attribute(String path) {
        return new AttributePath(path);
    }

    /**
     * Wraps a function as an argument, choosing {@link EvaluableArgument} or {@link NativeArgument}
     * according to the function specialization.
     *
     * @param function the nested function
     * @return the argument
     */
    static FunctionArgument function(Function function) {
        Objects.requireNonNull(function, "function is required");
        return switch (function) {
            case EvaluableFunction evaluable -> new EvaluableArgument(evaluable);
            case NativeFunction nativeFunction -> new NativeArgument(nativeFunction);
        };
    }
}
