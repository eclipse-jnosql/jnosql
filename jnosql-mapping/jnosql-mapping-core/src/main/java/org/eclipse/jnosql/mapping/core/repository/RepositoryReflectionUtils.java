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
import jakarta.data.constraint.Constraint;
import jakarta.data.repository.By;
import jakarta.data.repository.Is;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import org.eclipse.jnosql.communication.Condition;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
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

    public ParamValue condition(Is is, Object value) {
        if (Objects.isNull(is)) {
            return new ParamValue(Condition.EQUALS, value, false);
        }
        Class<? extends Constraint> constraint = is.value();
        return switch (constraint.getName()) {
            case "jakarta.data.constraint.AtLeast" -> new ParamValue(Condition.GREATER_EQUALS_THAN, value, false);
            case "jakarta.data.constraint.AtMost" -> new ParamValue(Condition.LESSER_EQUALS_THAN, value, false);
            case "jakarta.data.constraint.GreaterThan" -> new ParamValue(Condition.GREATER_THAN, value, false);
            case "jakarta.data.constraint.LesserThan" -> new ParamValue(Condition.LESSER_THAN, value, false);
            case "jakarta.data.constraint.Between" -> new ParamValue(Condition.BETWEEN, value, false);
            case "jakarta.data.constraint.EqualTo" -> new ParamValue(Condition.EQUALS, value, false);
            case "jakarta.data.constraint.Like" -> new ParamValue(Condition.LIKE, value, false);
            case "jakarta.data.constraint.In" -> new ParamValue(Condition.IN, value, false);
            // Negate conditions
            case "jakarta.data.constraint.NotBetween" -> new ParamValue(Condition.BETWEEN, value, true);
            case "jakarta.data.constraint.NotEquals" -> new ParamValue(Condition.EQUALS, value, true);
            case "jakarta.data.constraint.NotIn" -> new ParamValue(Condition.IN, value, true);
            case "jakarta.data.constraint.NotLike" -> new ParamValue(Condition.LIKE, value, true);
            default -> throw new UnsupportedOperationException("The FindBy annotation does not support this constraint: " + constraint.getName()
            +" at the Is annotation, please use one of the following: "
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
