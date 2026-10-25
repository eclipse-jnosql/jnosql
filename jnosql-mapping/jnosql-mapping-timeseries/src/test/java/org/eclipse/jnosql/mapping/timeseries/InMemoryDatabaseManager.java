/*
 *
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.timeseries;

import jakarta.data.Sort;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A minimal in-memory {@link DatabaseManager} that evaluates equality, range, AND and OR conditions (any other operator matches everything),
 * sorting and limit, so tests can assert on returned observations and on the executed {@link SelectQuery}.
 */
final class InMemoryDatabaseManager {

    private final List<CommunicationEntity> stored = new ArrayList<>();
    private final List<SelectQuery> queries = new ArrayList<>();
    private final DatabaseManager manager = Mockito.mock(DatabaseManager.class);

    InMemoryDatabaseManager() {
        Mockito.when(manager.count(Mockito.any(SelectQuery.class))).thenAnswer(invocation -> {
            SelectQuery query = invocation.getArgument(0);
            queries.add(query);
            return (long) execute(query).size();
        });
        Mockito.when(manager.select(Mockito.any(SelectQuery.class))).thenAnswer(invocation -> {
            SelectQuery query = invocation.getArgument(0);
            queries.add(query);
            return execute(query).stream();
        });
    }

    DatabaseManager manager() {
        return manager;
    }

    void store(CommunicationEntity entity) {
        stored.add(entity);
    }

    SelectQuery lastQuery() {
        return queries.get(queries.size() - 1);
    }

    int executions() {
        return queries.size();
    }

    private List<CommunicationEntity> execute(SelectQuery query) {
        List<CommunicationEntity> result = new ArrayList<>(stored.stream()
                .filter(e -> e.name().equals(query.name()))
                .filter(e -> query.condition().map(c -> matches(c, e)).orElse(true))
                .toList());
        Comparator<CommunicationEntity> comparator = null;
        for (Sort<?> sort : query.sorts()) {
            Comparator<CommunicationEntity> next = Comparator.comparing(e -> value(e, sort.property()));
            if (sort.isDescending()) {
                next = next.reversed();
            }
            comparator = comparator == null ? next : comparator.thenComparing(next);
        }
        if (comparator != null) {
            result.sort(comparator);
        }
        if (query.limit() > 0 && result.size() > query.limit()) {
            return result.subList(0, (int) query.limit());
        }
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Comparable value(CommunicationEntity entity, String name) {
        return (Comparable) entity.find(name).orElseThrow().get();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean matches(CriteriaCondition criteria, CommunicationEntity entity) {
        Condition condition = criteria.condition();
        return switch (condition) {
            case AND -> criteria.element().get(new TypeReference<List<CriteriaCondition>>() {
            }).stream().allMatch(c -> matches(c, entity));
            case OR -> criteria.element().get(new TypeReference<List<CriteriaCondition>>() {
            }).stream().anyMatch(c -> matches(c, entity));
            case EQUALS -> value(entity, criteria.element().name()).equals(criteria.element().get());
            case GREATER_EQUALS_THAN -> value(entity, criteria.element().name())
                    .compareTo(criteria.element().get()) >= 0;
            case LESSER_EQUALS_THAN -> value(entity, criteria.element().name())
                    .compareTo(criteria.element().get()) <= 0;
            case GREATER_THAN -> value(entity, criteria.element().name())
                    .compareTo(criteria.element().get()) > 0;
            case LESSER_THAN -> value(entity, criteria.element().name())
                    .compareTo(criteria.element().get()) < 0;
            default -> true;
        };
    }
}
