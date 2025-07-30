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

import org.eclipse.jnosql.communication.query.DeleteQuery;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public enum DeleteProvider implements Function<String, DeleteQuery> {
    INSTANCE;

    private final Map<String, DeleteQuery> cache = new ConcurrentHashMap<>();

    @Override
    public DeleteQuery apply(String query) {
        Objects.requireNonNull(query, " query is required");
        return cache.computeIfAbsent(query, k -> {
            var deleteParser = new DeleteParser();
            return deleteParser.apply(query);
        });
    }
}
