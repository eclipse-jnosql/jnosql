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
import org.eclipse.jnosql.communication.Value;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Copies a query replacing every temporal expression by an ordinary value resolved at one instant and every
 * function by a fresh instance of its evaluation tree, so no memoized result is shared between executions.
 * The copy is immutable with respect to time, so a later execution of the same prepared statement
 * cannot change a query already handed to a driver. Parameters and all other values are kept as they are,
 * and a query without temporal expressions is returned unchanged.
 */
final class TemporalSnapshot {

    private final LocalDateTime now;

    private boolean temporal;

    TemporalSnapshot(LocalDateTime now) {
        this.now = now;
    }

    SelectQuery select(SelectQuery query) {
        var condition = query.condition().map(this::condition).orElse(null);
        return temporal ? new DefaultSelectQuery(query.limit(), query.skip(), query.name(), query.columns(),
                query.sorts(), condition, query.isCount()) : query;
    }

    DeleteQuery delete(DeleteQuery query) {
        var condition = query.condition().map(this::condition).orElse(null);
        return temporal ? new DefaultDeleteQuery(query.name(), condition, query.columns()) : query;
    }

    UpdateQuery update(UpdateQuery query) {
        var sets = query.set().stream().map(e -> Element.of(e.name(), value(e.value()))).toList();
        var condition = query.condition().map(this::condition).orElse(null);
        return temporal ? new DefaultUpdateQuery(query.name(), sets, condition) : query;
    }

    private CriteriaCondition condition(CriteriaCondition criteria) {
        if (criteria.isNative()) {
            this.temporal = true;
            return CriteriaCondition.of(operand(criteria.leftOperand()), criteria.condition(),
                    operand(criteria.rightOperand()));
        }
        var element = criteria.element();
        Object converted = switch (criteria.condition()) {
            case AND, OR -> ((List<?>) element.value().get()).stream()
                    .map(c -> condition((CriteriaCondition) c)).toList();
            case NOT -> condition((CriteriaCondition) element.value().get());
            default -> value(element.value());
        };
        return CriteriaCondition.of(Element.of(element.name(), converted), criteria.condition());
    }

    private CriteriaOperand operand(CriteriaOperand operand) {
        if (operand instanceof CriteriaOperand.Function function) {
            return new CriteriaOperand.Function(NativeFunctions.materialize(function.function()));
        }
        if (operand instanceof CriteriaOperand.Constant constant) {
            var resolved = value(constant.value());
            return new CriteriaOperand.Constant(capture(resolved instanceof Value v ? v : Value.of(resolved)));
        }
        return operand;
    }

    private static Value capture(Value value) {
        if (value instanceof ParamValue param) {
            return Value.of(param.get());
        }
        if (value.get() instanceof List<?> items) {
            return Value.of(items.stream().map(item -> item instanceof Value v ? capture(v).get() : item).toList());
        }
        return value;
    }

    private Object value(Value value) {
        if (value instanceof TemporalValue expression) {
            this.temporal = true;
            return Value.of(expression.resolve(now));
        }
        if (value instanceof FunctionValue function) {
            this.temporal = true;
            return function.materialize();
        }
        if (value instanceof ParamValue) {
            return value;
        }
        if (value.get() instanceof List<?> items) {
            return items.stream().map(item -> item instanceof Value v ? value(v) : item).toList();
        }
        return value;
    }
}
