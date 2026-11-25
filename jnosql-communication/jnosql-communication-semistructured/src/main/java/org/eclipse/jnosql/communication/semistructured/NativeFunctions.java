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
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.function.FunctionArgument;
import org.eclipse.jnosql.communication.function.NativeFunction;
import org.eclipse.jnosql.communication.query.ParamQueryValue;
import org.eclipse.jnosql.communication.query.QueryPath;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.ValueType;

import java.util.ArrayList;

/**
 * Builds the structured {@link NativeFunction} handed to drivers from a parsed query function, and copies it
 * for each execution. Nothing here parses text: argument order, nesting and kinds come from the parsed tree.
 * Only attribute references are translated to stored field names; literals and parameters are never renamed.
 * Independent arguments stay lazy {@link Value}s ({@link FunctionValue} for evaluable functions), so a driver only
 * receives the ordinary Java values it needs, and the same lifecycle as other functions applies.
 */
final class NativeFunctions {

    private NativeFunctions() {
    }

    static NativeFunction convert(org.eclipse.jnosql.communication.query.Function definition, Params params,
                                  CommunicationObserverParser observer, String entity) {
        var arguments = new ArrayList<FunctionArgument>(definition.params().length);
        for (Object param : definition.params()) {
            arguments.add(argument(param, params, observer, entity));
        }
        return NativeFunction.of(definition.name(), arguments.toArray(FunctionArgument[]::new));
    }

    private static FunctionArgument argument(Object param, Params params, CommunicationObserverParser observer,
                                             String entity) {
        var value = (QueryValue<?>) param;
        return switch (value.type()) {
            case PATH -> new FunctionArgument.AttributePath(
                    observer.fireConditionField(entity, ((QueryPath) value).get()));
            case PARAMETER -> new FunctionArgument.ValueArgument(params.add(((ParamQueryValue) value).get()));
            case FUNCTION -> {
                var nested = (org.eclipse.jnosql.communication.query.Function) value.get();
                yield nested.isNative()
                        ? new FunctionArgument.NativeArgument(convert(nested, params, observer, entity))
                        : new FunctionArgument.ValueArgument(FunctionValue.of(nested, params));
            }
            case NUMBER, STRING, BOOLEAN, ENUM -> FunctionArgument.literal(value.get());
            default -> throw new QueryException("A " + value.type()
                    + " expression is not supported as a function argument");
        };
    }

    /**
     * Copies the function for one execution: evaluable arguments get a fresh tree and bound parameters are captured
     * by value, so no result is shared between executions. Attributes and literals are kept.
     */
    static NativeFunction materialize(NativeFunction function) {
        var arguments = new ArrayList<FunctionArgument>();
        for (FunctionArgument argument : function.arguments()) {
            arguments.add(switch (argument) {
                case FunctionArgument.NativeArgument nested ->
                        new FunctionArgument.NativeArgument(materialize(nested.function()));
                case FunctionArgument.ValueArgument(Value value) when value instanceof FunctionValue evaluable ->
                        new FunctionArgument.ValueArgument(evaluable.materialize());
                case FunctionArgument.ValueArgument(Value value) -> new FunctionArgument.ValueArgument(
                        Value.of(((ParamValue) value).get()));
                default -> argument;
            });
        }
        return NativeFunction.of(function.name(), arguments.toArray(FunctionArgument[]::new));
    }

    static boolean isNative(QueryValue<?> value) {
        return value.type() == ValueType.FUNCTION
                && ((org.eclipse.jnosql.communication.query.Function) value.get()).isNative();
    }
}
