/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.data;

import org.eclipse.jnosql.communication.query.SelectQuery;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * A provider for creating and caching {@link SelectQuery} instances based on a query string and an entity name. This
 * implementation uses a concurrent map to cache the queries for performance optimization. The queries are parsed using
 * the {@link SelectParser}.
 *
 * @see SelectParser
 */
public enum SelectProvider implements BiFunction<String, String, SelectQuery> {

    INSTANCE;

    private final Map<String, SelectQuery> cache = new ConcurrentHashMap<>();


    @Override
    public SelectQuery apply(String query, String entity) {
        Objects.requireNonNull(query, " query is required");

        String key = query + "::" + (entity == null ? "<null>" : entity);
        return cache.computeIfAbsent(key, k -> {
            var selectParser = new SelectParser();
            return selectParser.apply(query, entity);
        });
    }
}
