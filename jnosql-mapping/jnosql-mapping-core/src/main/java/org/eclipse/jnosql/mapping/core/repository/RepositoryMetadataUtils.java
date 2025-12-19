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

import jakarta.data.constraint.Constraint;
import jakarta.data.repository.By;
import jakarta.data.repository.Is;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public enum RepositoryMetadataUtils {

    INSTANCE;

    public Map<String, Object> getParams(RepositoryMethod method, Object[] args) {
        Map<String, Object> params = new HashMap<>();

        var parameters = method.params();
        int queryIndex = 1;
        for (int index = 0; index < parameters.size(); index++) {
            var parameter = parameters.get(index);
            boolean isNotSpecialParameter = SpecialParameters.isNotSpecialParameter(parameter.type());
            if (isNotSpecialParameter) {
                var param = parameter.param();
                params.put(param, args[index]);
                params.put("?" + queryIndex++, args[index]);
            }
        }
        return params;
    }

    public Map<String, ParamValue> getBy(RepositoryMethod method, Object[] arguments) {
        Map<String, ParamValue> params = new HashMap<>();

        var parameters = method.params();
        for (int index = 0; index < parameters.size(); index++) {
            var parameter = parameters.get(index);
            boolean isNotSpecialParameter = SpecialParameters.isNotSpecialParameter(parameter.type());
            var by = parameter.by();
            var is = parameter.is();
            if (isNotSpecialParameter) {
                params.put(by, condition(is.orElse(null), arguments[index]));
            }
        }
        return params;
    }

    private ParamValue condition(Class<? extends Constraint<?>> isType, Object value) {
        if (Objects.isNull(isType) && !(value instanceof Constraint<?>)) {
            return new ParamValue(Condition.EQUALS, value, false);
        } else if (value instanceof Constraint<?> constraint) {
            return ParamValueUtils.valueFromConstraintInstance(constraint);
        }
        return ParamValueUtils.getParamValue(value, isType);
    }
}
