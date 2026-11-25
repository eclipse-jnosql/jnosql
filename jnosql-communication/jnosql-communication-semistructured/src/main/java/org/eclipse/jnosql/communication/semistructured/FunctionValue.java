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

import org.eclipse.jnosql.communication.ParamValue;
import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.TypeSupplier;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.function.EvaluableFunction;
import org.eclipse.jnosql.communication.function.FunctionArgument;
import org.eclipse.jnosql.communication.query.ParamQueryValue;
import org.eclipse.jnosql.communication.query.QueryPath;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.ValueType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A JDQL evaluable function exposed as an ordinary lazily evaluated {@link Value}.
 * <p>
 * The instance owns an {@link EvaluableFunction} tree whose result is memoized by that tree, so repeated access
 * within one instance computes the function once. A prepared statement keeps the instance only as a reusable
 * definition: {@link #materialize()} builds a completely new tree for each execution, capturing the parameter values
 * bound at that moment, so no result is ever shared between executions and later rebinds are not observed.
 * Structural errors are reported when the value is created; evaluation stays lazy.
 */
final class FunctionValue implements Value {

    private final org.eclipse.jnosql.communication.query.Function definition;

    private final Map<String, Value> arguments;

    private final EvaluableFunction function;

    private FunctionValue(org.eclipse.jnosql.communication.query.Function definition, Map<String, Value> arguments) {
        this.definition = definition;
        this.arguments = arguments;
        try {
            this.function = build(definition, arguments);
        } catch (IllegalArgumentException | UnsupportedOperationException exp) {
            throw new QueryException("Invalid function " + definition.name() + ": " + exp.getMessage(), exp);
        }
    }

    /**
     * Creates the value registering, once per distinct name, every parameter used anywhere in the function tree.
     */
    static FunctionValue of(org.eclipse.jnosql.communication.query.Function definition, Params params) {
        if (definition.isNative()) {
            throw new QueryException("Native function execution is not supported yet: " + definition.name()
                    + " depends on a stored attribute and cannot be evaluated independently of the database");
        }
        Map<String, Value> arguments = new LinkedHashMap<>();
        register(definition, params, arguments);
        return new FunctionValue(definition, arguments);
    }

    /**
     * Creates an independent copy for one execution. Bound parameters are captured by value; unbound ones are kept
     * so that evaluating them fails explicitly.
     */
    FunctionValue materialize() {
        Map<String, Value> captured = new LinkedHashMap<>();
        arguments.forEach((name, value) -> captured.put(name, capture(value)));
        return new FunctionValue(definition, captured);
    }

    private static Value capture(Value value) {
        if (value instanceof ParamValue param && !param.isEmpty()) {
            return Value.of(param.get());
        }
        return value;
    }

    private static void register(org.eclipse.jnosql.communication.query.Function definition, Params params,
                                 Map<String, Value> arguments) {
        for (Object param : definition.params()) {
            if (param instanceof ParamQueryValue value) {
                arguments.computeIfAbsent(value.get(), params::add);
            } else if (param instanceof QueryValue<?> value && value.type() == ValueType.FUNCTION) {
                register((org.eclipse.jnosql.communication.query.Function) value.get(), params, arguments);
            }
        }
    }

    private static EvaluableFunction build(org.eclipse.jnosql.communication.query.Function definition,
                                           Map<String, Value> arguments) {
        var converted = new FunctionArgument[definition.params().length];
        for (int index = 0; index < converted.length; index++) {
            converted[index] = argument(definition.params()[index], arguments);
        }
        return EvaluableFunction.of(definition.name(), converted);
    }

    private static FunctionArgument argument(Object param, Map<String, Value> arguments) {
        if (!(param instanceof QueryValue<?> value)) {
            throw new IllegalArgumentException("Unsupported function argument: " + param);
        }
        return switch (value.type()) {
            case PARAMETER -> new FunctionArgument.ValueArgument(arguments.get(((ParamQueryValue) value).get()));
            case FUNCTION -> new FunctionArgument.EvaluableArgument(
                    build((org.eclipse.jnosql.communication.query.Function) value.get(), arguments));
            case NUMBER, STRING, BOOLEAN, ENUM -> FunctionArgument.literal(value.get());
            case PATH -> new FunctionArgument.AttributePath(((QueryPath) value).get());
            default -> throw new IllegalArgumentException(
                    "A " + value.type() + " expression is not supported as a function argument");
        };
    }

    @Override
    public Object get() {
        return evaluate(function::get);
    }

    @Override
    public <T> T get(Class<T> type) {
        return evaluate(() -> function.get(type));
    }

    @Override
    public <T> T get(TypeSupplier<T> supplier) {
        return evaluate(() -> function.get(supplier));
    }

    @Override
    public boolean isInstanceOf(Class<?> type) {
        return evaluate(() -> function.isInstanceOf(type));
    }

    @Override
    public boolean isNull() {
        return evaluate(function::isNull);
    }

    private <T> T evaluate(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (QueryException exp) {
            throw exp;
        } catch (IllegalArgumentException | IllegalStateException | ArithmeticException exp) {
            throw new QueryException("Cannot evaluate function " + definition.name() + ": " + exp.getMessage(), exp);
        }
    }

    @Override
    public String toString() {
        return definition.toString();
    }
}
