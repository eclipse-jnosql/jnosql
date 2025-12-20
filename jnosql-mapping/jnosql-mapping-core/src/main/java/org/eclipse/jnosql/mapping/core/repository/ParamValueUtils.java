/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.core.repository;

import jakarta.data.constraint.AtLeast;
import jakarta.data.constraint.Between;
import jakarta.data.constraint.Constraint;
import jakarta.data.constraint.EqualTo;
import jakarta.data.constraint.In;
import jakarta.data.constraint.Like;
import jakarta.data.constraint.NotEqualTo;
import jakarta.data.expression.Expression;
import jakarta.data.spi.expression.literal.Literal;
import org.eclipse.jnosql.communication.Condition;

import java.util.List;

enum ParamValueUtils {

    INSTANCE;

    static ParamValue valueFromConstraintInstance(Constraint<?> constraint) {
        return switch (constraint) {
            case AtLeast<?> atLeast -> new ParamValue(Condition.GREATER_EQUALS_THAN, valueFromExpression(atLeast.bound()), false);
            case jakarta.data.constraint.AtMost<?> atMost -> new ParamValue(Condition.LESSER_EQUALS_THAN, valueFromExpression(atMost.bound()), false);
            case jakarta.data.constraint.GreaterThan<?> greaterThan ->
                    new ParamValue(Condition.GREATER_THAN, valueFromExpression(greaterThan.bound()), false);
            case jakarta.data.constraint.LessThan<?> lessThan -> new ParamValue(Condition.LESSER_THAN, valueFromExpression(lessThan.bound()), false);
            case Between<?> between -> new ParamValue(Condition.BETWEEN,
                    List.of(valueFromExpression(between.lowerBound()), valueFromExpression(between.upperBound())), false);
            case EqualTo<?> equalTo -> new ParamValue(Condition.EQUALS, valueFromExpression(equalTo.expression()), false);
            case Like like -> new ParamValue(Condition.LIKE, valueFromExpression(like.pattern()), false);
            case In<?> in -> new ParamValue(Condition.IN, in.expressions().stream().map(ParamValueUtils::valueFromExpression).toList(), false);
            // Negate conditions
            case jakarta.data.constraint.NotBetween<?> notBetween -> new ParamValue(Condition.BETWEEN,
                    List.of(valueFromExpression(notBetween.lowerBound()), valueFromExpression(notBetween.upperBound())), true);
            case NotEqualTo<?> notEqualTo -> new ParamValue(Condition.EQUALS, valueFromExpression(notEqualTo.expression()), true);
            case jakarta.data.constraint.NotIn<?> notIn -> new ParamValue(Condition.IN,  notIn.expressions().stream()
                    .map(ParamValueUtils::valueFromExpression).toList(), true);
            case jakarta.data.constraint.NotLike notLike -> new ParamValue(Condition.LIKE, valueFromExpression(notLike.pattern()), true);
            default ->
                    throw new UnsupportedOperationException("The FindBy annotation does not support this constraint: " + constraint.getClass()
                            + " at the Is annotation, please use one of the following: "
                            + "AtLeast, AtMost, GreaterThan, LesserThan, Between, EqualTo, Like, In, NotBetween, NotEquals, NotIn or NotLike");
        };
    }

    private static Object  valueFromExpression(Expression<?, ?> expression) {
        if (expression instanceof Literal<?> literal) {
            return literal.value();
        }
        throw new UnsupportedOperationException("On NoSQL database this is not supported: " + expression.getClass());
    }

    static ParamValue valueFromConstraintClass(Object value, Class<? extends Constraint> constraint) {
        return switch (constraint.getName()) {
            case "jakarta.data.constraint.AtLeast" -> new ParamValue(Condition.GREATER_EQUALS_THAN, value, false);
            case "jakarta.data.constraint.AtMost" -> new ParamValue(Condition.LESSER_EQUALS_THAN, value, false);
            case "jakarta.data.constraint.GreaterThan" -> new ParamValue(Condition.GREATER_THAN, value, false);
            case "jakarta.data.constraint.LessThan" -> new ParamValue(Condition.LESSER_THAN, value, false);
            case "jakarta.data.constraint.Between" -> new ParamValue(Condition.BETWEEN, value, false);
            case "jakarta.data.constraint.EqualTo" -> new ParamValue(Condition.EQUALS, value, false);
            case "jakarta.data.constraint.Like" -> new ParamValue(Condition.LIKE, value, false);
            case "jakarta.data.constraint.In" -> new ParamValue(Condition.IN, value, false);
            // Negate conditions
            case "jakarta.data.constraint.NotBetween" -> new ParamValue(Condition.BETWEEN, value, true);
            case "jakarta.data.constraint.NotEqualTo" -> new ParamValue(Condition.EQUALS, value, true);
            case "jakarta.data.constraint.NotIn" -> new ParamValue(Condition.IN, value, true);
            case "jakarta.data.constraint.NotLike" -> new ParamValue(Condition.LIKE, value, true);
            default ->
                    throw new UnsupportedOperationException("The FindBy annotation does not support this constraint: " + constraint.getName()
                            + " at the Is annotation, please use one of the following: "
                            + "AtLeast, AtMost, GreaterThan, LesserThan, Between, EqualTo, Like, In, NotBetween, NotEquals, NotIn or NotLike");
        };
    }

    static ParamValue getParamValue(Object value, Class<? extends Constraint> type) {
        if (value instanceof Constraint<?> constraint){
            return valueFromConstraintInstance(constraint);
        }
        return valueFromConstraintClass(value, type);
    }


}
