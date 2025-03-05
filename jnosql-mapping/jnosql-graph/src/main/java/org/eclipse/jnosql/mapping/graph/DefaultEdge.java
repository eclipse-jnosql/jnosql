package org.eclipse.jnosql.mapping.graph;

import java.util.Map;
import java.util.Optional;

record DefaultEdge<S, T>( Object key, S source, T target, String label, Map<String, Object> properties) implements Edge<S, T> {


    @Override
    public <V> Optional<V> property(String key, Class<V> type) {
        return Optional.empty();
    }
}
