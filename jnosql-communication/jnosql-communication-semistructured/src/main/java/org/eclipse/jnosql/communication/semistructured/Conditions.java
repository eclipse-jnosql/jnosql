/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;


import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.query.ConditionQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.Where;

public final class Conditions {

    private Conditions() {
    }

    public static CriteriaCondition getCondition(Where where, Params params, CommunicationObserverParser observer, String entity) {
        QueryCondition condition = where.condition();
        return getCondition(condition, params, observer, entity);
    }

    static CriteriaCondition getCondition(QueryCondition condition, Params parameters, CommunicationObserverParser observer, String entity) {
        return switch (condition.condition()) {
            case EQUALS -> CriteriaCondition.eq(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(), parameters)));
            case GREATER_THAN -> CriteriaCondition.gt(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(), parameters)));
            case GREATER_EQUALS_THAN -> CriteriaCondition.gte(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(), parameters)));
            case LESSER_THAN -> CriteriaCondition.lt(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(), parameters)));
            case LESSER_EQUALS_THAN -> CriteriaCondition.lte(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(), parameters)));
            case IN -> CriteriaCondition.in(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(), parameters)));
            case LIKE -> CriteriaCondition.like(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(),
                            parameters)));
            case BETWEEN -> CriteriaCondition.between(Element.of(getName(condition, observer, entity),
                    Values.get(condition.value(),
                            parameters)));
            case NOT -> getCondition(ConditionQueryValue.class.cast(condition.value()).get().get(0),
                    parameters, observer,
                    entity).negate();
            case OR -> CriteriaCondition.or(ConditionQueryValue.class.cast(condition.value())
                    .get()
                    .stream().map(v -> getCondition(v, parameters, observer, entity))
                    .toArray(CriteriaCondition[]::new));
            case AND -> CriteriaCondition.and(ConditionQueryValue.class.cast(condition.value())
                    .get()
                    .stream().map(v -> getCondition(v, parameters, observer, entity))
                    .toArray(CriteriaCondition[]::new));
            default -> throw new QueryException("There is not support the type: " + condition.condition());
        };
    }

    private static String getName(QueryCondition condition, CommunicationObserverParser observer, String entity) {
        return observer.fireConditionField(entity, condition.name());
    }

}
