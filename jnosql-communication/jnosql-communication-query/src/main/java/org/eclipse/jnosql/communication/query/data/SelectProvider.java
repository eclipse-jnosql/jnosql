package org.eclipse.jnosql.communication.query.data;

import org.eclipse.jnosql.communication.query.SelectQuery;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

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
