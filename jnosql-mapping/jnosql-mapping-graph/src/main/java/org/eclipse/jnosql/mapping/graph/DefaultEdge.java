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
package org.eclipse.jnosql.mapping.graph;

import org.eclipse.jnosql.communication.Value;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

record DefaultEdge<S, T>(S source, T target, String label, Map<String, Object> properties, Object key) implements Edge<S, T> {


    @Override
    public Optional<Object> id() {
        return Optional.ofNullable(key);
    }

    @Override
    public <K> Optional<K> id(Class<K> type) {
        return Optional.ofNullable(key).map(Value::of).map(v -> v.get(type));
    }

    @Override
    public Map<String, Object> properties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public <V> Optional<V> property(String key, Class<V> type) {
        Objects.requireNonNull(key, "key is required");
        Objects.requireNonNull(type, "type is required");
        return Optional.ofNullable(properties.get(key)).map(Value::of).map(v -> v.get(type));
    }
}
