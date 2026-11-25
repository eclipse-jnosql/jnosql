/*
 *  Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 * and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;


import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.query.ConditionQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryPath;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.ValueType;
import org.eclipse.jnosql.communication.query.Where;

public final class Conditions {

    private Conditions() {
    }

    public static CriteriaCondition getCondition(Where where, Params params, CommunicationObserverParser observer, String entity) {
        QueryCondition condition = where.condition();
        return getCondition(condition, params, observer, entity, TemporalReference.live());
    }

    static CriteriaCondition getCondition(Where where, Params params, CommunicationObserverParser observer,
                                          String entity, TemporalReference reference) {
        return getCondition(where.condition(), params, observer, entity, reference);
    }

    static CriteriaCondition getCondition(QueryCondition condition, Params parameters, CommunicationObserverParser observer, String entity) {
        return getCondition(condition, parameters, observer, entity, TemporalReference.live());
    }

    static CriteriaCondition getCondition(QueryCondition condition, Params parameters, CommunicationObserverParser observer,
                                          String entity, TemporalReference reference) {
        if (hasNativeOperand(condition)) {
            return nativeCondition(condition, parameters, observer, entity, reference);
        }
        return switch (condition.condition()) {
            case EQUALS -> CriteriaCondition.eq(Element.of(getName(condition, observer, entity),
                    Values.get(condition.rightOperand(), parameters, reference)));
            case GREATER_THAN -> CriteriaCondition.gt(Element.of(getName(condition, observer, entity),
                    Values.get(condition.rightOperand(), parameters, reference)));
            case GREATER_EQUALS_THAN -> CriteriaCondition.gte(Element.of(getName(condition, observer, entity),
                    Values.get(condition.rightOperand(), parameters, reference)));
            case LESSER_THAN -> CriteriaCondition.lt(Element.of(getName(condition, observer, entity),
                    Values.get(condition.rightOperand(), parameters, reference)));
            case LESSER_EQUALS_THAN -> CriteriaCondition.lte(Element.of(getName(condition, observer, entity),
                    Values.get(condition.rightOperand(), parameters, reference)));
            case IN -> CriteriaCondition.in(Element.of(getName(condition, observer, entity),
                    Values.get(condition.rightOperand(), parameters, reference)));
            case LIKE -> CriteriaCondition.like(Element.of(getName(condition, observer, entity),
                    Values.get(condition.rightOperand(), parameters, reference)));
            case BETWEEN -> CriteriaCondition.between(Element.of(getName(condition, observer, entity),
                    Values.get(condition.rightOperand(), parameters, reference)));
            case NOT -> getCondition(ConditionQueryValue.class.cast(condition.rightOperand()).get().get(0),
                    parameters, observer,
                    entity, reference).negate();
            case OR -> CriteriaCondition.or(ConditionQueryValue.class.cast(condition.rightOperand())
                    .get()
                    .stream().map(v -> getCondition(v, parameters, observer, entity, reference))
                    .toArray(CriteriaCondition[]::new));
            case AND -> CriteriaCondition.and(ConditionQueryValue.class.cast(condition.rightOperand())
                    .get()
                    .stream().map(v -> getCondition(v, parameters, observer, entity, reference))
                    .toArray(CriteriaCondition[]::new));
            case CONTAINS -> CriteriaCondition.contains(Element.of(getName(condition, observer, entity),
                    Values.get(condition.rightOperand(), parameters, reference)));
            case STARTS_WITH -> CriteriaCondition.startsWith(Element.of(getName(condition, observer, entity),
                    Values.get(condition.rightOperand(), parameters, reference)));
            case ENDS_WITH -> CriteriaCondition.endsWith(Element.of(getName(condition, observer, entity),
                    Values.get(condition.rightOperand(), parameters, reference)));
            case IGNORE_CASE -> CriteriaCondition.ignoreCase(ConditionQueryValue.class.cast(condition.rightOperand())
                    .get()
                    .stream().map(v -> getCondition(v, parameters, observer, entity, reference))
                    .findAny().orElseThrow());
            default -> throw new QueryException("There is not support the type: " + condition.condition());
        };
    }

    private static boolean hasNativeOperand(QueryCondition condition) {
        return switch (condition.condition()) {
            case AND, OR, NOT, IGNORE_CASE -> false;
            default -> NativeFunctions.isNative(condition.leftOperand())
                    || NativeFunctions.isNative(condition.rightOperand());
        };
    }

    private static CriteriaCondition nativeCondition(QueryCondition condition, Params parameters,
                                                     CommunicationObserverParser observer, String entity,
                                                     TemporalReference reference) {
        var left = operand(condition.leftOperand(), parameters, observer, entity, reference);
        var right = operand(condition.rightOperand(), parameters, observer, entity, reference);
        return CriteriaCondition.of(left, condition.condition(), right);
    }

    private static CriteriaOperand operand(QueryValue<?> value, Params parameters, CommunicationObserverParser observer,
                                           String entity, TemporalReference reference) {
        if (value.type() == ValueType.PATH) {
            return new CriteriaOperand.Attribute(observer.fireConditionField(entity, ((QueryPath) value).get()));
        }
        if (NativeFunctions.isNative(value)) {
            return new CriteriaOperand.Function(NativeFunctions.convert(
                    (org.eclipse.jnosql.communication.query.Function) value.get(), parameters, observer, entity));
        }
        var converted = Values.get(value, parameters, reference);
        return new CriteriaOperand.Constant(converted instanceof Value constant ? constant : Value.of(converted));
    }

    private static String getName(QueryCondition condition, CommunicationObserverParser observer, String entity) {
        var left = condition.leftOperand();
        if (left.type() != ValueType.PATH) {
            throw new QueryException("A " + left.type() + " as the left operand of a condition is not supported yet: "
                    + left + ". The communication model cannot represent a function predicate");
        }
        return observer.fireConditionField(entity, ((QueryPath) left).get());
    }

}
