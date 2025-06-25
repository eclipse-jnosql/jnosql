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



import jakarta.data.repository.By;
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
     * Represents a parameter value with its condition.
     * It will get the {@link Param} value and combine it with {@link jakarta.data.repository.Is}
     * by default if does not have {@link jakarta.data.repository.Is} it will use {@link Condition#EQUALS}.
     */
    public record ParamValue(Condition condition, Object value){}

    /**
     * Converts values at arg at a {@link Map}
     *
     * @param method the method that has the {@link By} info
     * @param args   the arguments from the method
     * @return the {@link Map} from method and its arguments
     */
    public Map<String, Object> getBy(Method method, Object[] args) {
        Map<String, Object> params = new HashMap<>();

        Parameter[] parameters = method.getParameters();
        for (int index = 0; index < parameters.length; index++) {
            Parameter parameter = parameters[index];
            boolean isNotSpecialParameter = SpecialParameters.isNotSpecialParameter(parameter.getType());
            By by = parameter.getAnnotation(By.class);
            if (Objects.nonNull(by)) {
                params.put(by.value(), args[index]);
            } else if(parameter.isNamePresent() && isNotSpecialParameter) {
                params.put(parameter.getName(), args[index]);
            }
        }
        return params;
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
