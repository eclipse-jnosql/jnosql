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
import org.eclipse.jnosql.communication.query.ConditionQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.Where;

/**
 * Utility methods for creating semi-structured query conditions.
 */
public final class Conditions {

    private Conditions() {
    }

    /**
     * Converts a query {@link Where} clause into a criteria condition.
     *
     * @param where the query where clause
     * @param params the query parameters
     * @param observer the observer parser
     * @param entity the entity name
     * @return the criteria condition
     */
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
        return switch (condition.condition()) {
            case EQUALS -> CriteriaCondition.eq(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(), parameters, reference)));
            case GREATER_THAN -> CriteriaCondition.gt(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(), parameters, reference)));
            case GREATER_EQUALS_THAN -> CriteriaCondition.gte(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(), parameters, reference)));
            case LESSER_THAN -> CriteriaCondition.lt(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(), parameters, reference)));
            case LESSER_EQUALS_THAN -> CriteriaCondition.lte(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(), parameters, reference)));
            case IN -> CriteriaCondition.in(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(), parameters, reference)));
            case LIKE -> CriteriaCondition.like(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(), parameters, reference)));
            case BETWEEN -> CriteriaCondition.between(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(), parameters, reference)));
            case NOT -> getCondition(ConditionQueryValue.class.cast(condition.value()).get().getFirst(),
                    parameters, observer,
                    entity, reference).negate();
            case OR -> CriteriaCondition.or(ConditionQueryValue.class.cast(condition.value())
                    .get()
                    .stream().map(v -> getCondition(v, parameters, observer, entity, reference))
                    .toArray(CriteriaCondition[]::new));
            case AND -> CriteriaCondition.and(ConditionQueryValue.class.cast(condition.value())
                    .get()
                    .stream().map(v -> getCondition(v, parameters, observer, entity, reference))
                    .toArray(CriteriaCondition[]::new));
            case CONTAINS -> CriteriaCondition.contains(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(), parameters, reference)));
            case STARTS_WITH -> CriteriaCondition.startsWith(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(), parameters, reference)));
            case ENDS_WITH -> CriteriaCondition.endsWith(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(), parameters, reference)));
            case IGNORE_CASE -> CriteriaCondition.ignoreCase(ConditionQueryValue.class.cast(condition.value())
                    .get()
                    .stream().map(v -> getCondition(v, parameters, observer, entity, reference))
                    .findAny().orElseThrow());
        };
    }

    private static String getName(QueryCondition condition, CommunicationObserverParser observer, String entity) {
        return observer.fireConditionField(entity, condition.name());
    }

}
