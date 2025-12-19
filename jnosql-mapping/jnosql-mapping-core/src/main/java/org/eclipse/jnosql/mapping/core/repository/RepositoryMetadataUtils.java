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

import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;

import java.util.HashMap;
import java.util.Map;

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
}
