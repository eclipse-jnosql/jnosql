/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
import jakarta.data.repository.By;
import jakarta.data.repository.Is;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.spi.expression.literal.Literal;
import org.eclipse.jnosql.communication.Condition;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utilitarian class to {@link Param anotation}
 */
public enum RepositoryReflectionUtils {

    INSTANCE;

    /**
     * Converts values at arg at a {@link Map}
     *
     * @param method the method that has the {@link Param} info
     * @param args   the arguments from the method
     * @return the {@link Map} from method and its arguments
     */
    public Map<String, Object> getParams(Method method, Object[] args) {
        Map<String, Object> params = new HashMap<>();

        Parameter[] parameters = method.getParameters();
        int queryIndex = 1;
        for (int index = 0; index < parameters.length; index++) {
            Parameter parameter = parameters[index];
            boolean isNotSpecialParameter = SpecialParameters.isNotSpecialParameter(parameter.getType());
            Param param = parameter.getAnnotation(Param.class);
            if (Objects.nonNull(param)) {
                params.put(param.value(), args[index]);
            } else if (isNotSpecialParameter) {
                if (parameter.isNamePresent()) {
                    params.put(parameter.getName(), args[index]);
                }
                params.put("?" + queryIndex++, args[index]);
            }
        }
        return params;
    }

    /**
     * Converts values at arg at a {@link Map}
     *
     * @param method the method that has the {@link By} info
     * @param args   the arguments from the method
     * @return the {@link Map} from method and its arguments
     */
    public Map<String, ParamValue> getBy(Method method, Object[] args) {
        Map<String, ParamValue> params = new HashMap<>();

        Parameter[] parameters = method.getParameters();
        for (int index = 0; index < parameters.length; index++) {
            Parameter parameter = parameters[index];
            boolean isNotSpecialParameter = SpecialParameters.isNotSpecialParameter(parameter.getType());
            By by = parameter.getAnnotation(By.class);
            Is is = parameter.getAnnotation(Is.class);
            if (Objects.nonNull(by)) {
                params.put(by.value(), condition(is, args[index]));
            } else if(parameter.isNamePresent() && isNotSpecialParameter) {
                params.put(parameter.getName(),  condition(is,args[index]));
            }
        }
        return params;
    }

    @SuppressWarnings("unchecked")
    ParamValue condition(Is is, Object value) {
        if (Objects.isNull(is) && !(value instanceof Constraint<?>)) {
            return new ParamValue(Condition.EQUALS, value, false);
        } else if (value instanceof Constraint<?> constraint) {
            return valueFromConstraintInstance(constraint);
        }
        Class<? extends Constraint<?>> constraint = (Class<? extends Constraint<?>>) is.value();
        return getParamValue(value, constraint);
    }
    static ParamValue getParamValue(Object value, Class<? extends Constraint> type) {
        if(value instanceof Constraint<?> constraint){
            return valueFromConstraintInstance(constraint);
        }
        return valueFromConstraintClass(value, type);
    }

    private static ParamValue valueFromConstraintInstance(Constraint<?> constraint) {
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
            case In<?> in -> new ParamValue(Condition.IN, in.expressions().stream().map(RepositoryReflectionUtils::valueFromExpression).toList(), false);
            // Negate conditions
            case jakarta.data.constraint.NotBetween<?> notBetween -> new ParamValue(Condition.BETWEEN,
                    List.of(valueFromExpression(notBetween.lowerBound()), valueFromExpression(notBetween.upperBound())), true);
            case NotEqualTo<?> notEqualTo -> new ParamValue(Condition.EQUALS, valueFromExpression(notEqualTo.expression()), true);
            case jakarta.data.constraint.NotIn<?> notIn -> new ParamValue(Condition.IN,  notIn.expressions().stream()
                    .map(RepositoryReflectionUtils::valueFromExpression).toList(), true);
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

    private static ParamValue valueFromConstraintClass(Object value, Class<? extends Constraint> constraint) {
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


    /**
     * Returns the query value from the {@link Query} annotation
     *
     * @param method the method
     * @return the query value
     */
    public String getQuery(Method method) {
        return method.getAnnotation(Query.class).value();
    }




}
