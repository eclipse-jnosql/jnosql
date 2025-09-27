/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.method;


import org.eclipse.jnosql.communication.query.SelectQuery;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public enum SelectMethodProvider implements BiFunction<Method, String, SelectQuery> {

    INSTANCE;

    private final Map<String, SelectQuery> cache = new ConcurrentHashMap<>();


    @Override
    public SelectQuery apply(Method method, String entity) {
        Objects.requireNonNull(method, "method is required");
        Objects.requireNonNull(entity, "entity is required");
        return apply(method.getName(), entity);
    }

    public SelectQuery apply(String methodName, String entity) {
        Objects.requireNonNull(methodName, "method is required");
        Objects.requireNonNull(entity, "entity is required");
        var key = methodName + "::" + entity;

        return cache.computeIfAbsent(key, k -> {
            SelectMethodQueryParser provider = new SelectMethodQueryParser();
            return provider.apply(methodName, entity);
        });
    }
}
