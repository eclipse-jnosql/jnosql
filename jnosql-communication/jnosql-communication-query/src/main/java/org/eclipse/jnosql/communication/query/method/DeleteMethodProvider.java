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


import org.eclipse.jnosql.communication.query.DeleteQuery;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * A {@link DeleteQuery} factory from {@link Method}, this class create an instance of  DeleteQuery from the {@link Method#getName()}
 * nomenclature convention. It extends a {@link BiFunction} where:
 * - The Method
 * - The entity name
 * - The DeleteQuery from both Method and entity name
 */
public enum DeleteMethodProvider implements BiFunction<Method, String, DeleteQuery> {
    INSTANCE;

    private final Map<String, DeleteQuery> cache = new ConcurrentHashMap<>();

    @Override
    public DeleteQuery apply(Method method, String entity) {
        Objects.requireNonNull(method, "method is required");
        Objects.requireNonNull(entity, "entity is required");
        return apply(method.getName(), entity);
    }

    public DeleteQuery apply(String methodName, String entity) {
        Objects.requireNonNull(methodName, "method is required");
        Objects.requireNonNull(entity, "entity is required");
        String key = methodName + "::" + entity;
        return cache.computeIfAbsent(key, k -> {
            DeleteByMethodQueryParser provider = new DeleteByMethodQueryParser();
            return provider.apply(methodName, entity);
        });
    }
}
