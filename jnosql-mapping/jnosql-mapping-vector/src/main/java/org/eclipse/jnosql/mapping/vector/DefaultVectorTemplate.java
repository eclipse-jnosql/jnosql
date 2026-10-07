/*
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
package org.eclipse.jnosql.mapping.vector;

import jakarta.data.Limit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.AbstractSemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EntityConverterFactory;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Default
@ApplicationScoped
@Database(DatabaseType.VECTOR)
class DefaultVectorTemplate extends AbstractSemiStructuredTemplate implements VectorTemplate {

    private final EntityConverter converter;
    private final VectorManager manager;
    private final EventPersistManager eventManager;
    private final EntitiesMetadata entities;
    private final Converters converters;

    @Inject
    DefaultVectorTemplate(EntityConverterFactory factory, VectorManager manager,
                          EventPersistManager eventManager, EntitiesMetadata entities, Converters converters) {

        this.manager = requireNonNull(manager, "manager is required");
        this.converter = factory.create(manager);
        this.eventManager = eventManager;
        this.entities = entities;
        this.converters = converters;
    }

    DefaultVectorTemplate() {
        this.converter = null;
        this.manager = null;
        this.eventManager = null;
        this.entities = null;
        this.converters = null;
    }

    @Override
    protected EntityConverter converter() {
        return converter;
    }

    @Override
    protected DatabaseManager manager() {
        return manager;
    }

    @Override
    protected EventPersistManager eventManager() {
        return eventManager;
    }

    @Override
    protected EntitiesMetadata entities() {
        return entities;
    }

    @Override
    protected Converters converters() {
        return converters;
    }

    @Override
    public <T> List<T> searchNearestNeighbors(Class<T> entityClass,
                                              Vector queryVector,
                                              Limit limit) {

        requireNonNull(entityClass, "entityClass is required");
        requireNonNull(queryVector, "queryVector is required");
        requireNonNull(limit, "limit is required");

        var entityMetadata = entities.get(entityClass);

        return manager.searchNearestNeighbors(
                        entityMetadata.name(),
                        queryVector,
                        limit)
                .stream()
                .map(entity -> converter.toEntity(entityClass, entity))
                .toList();
    }

    @Override
    public <T> List<T> searchNearestNeighbors(Class<T> entityClass,
                                              Vector queryVector,
                                              Map<String, Object> filters,
                                              Limit limit) {

        requireNonNull(entityClass, "entityClass is required");
        requireNonNull(queryVector, "queryVector is required");
        requireNonNull(filters, "filters is required");
        requireNonNull(limit, "limit is required");

        var entityMetadata = entities.get(entityClass);

        Map<String, Object> mappedFilters = filters.entrySet()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        entry -> entityMetadata.columnField(entry.getKey()),
                        Map.Entry::getValue
                ));

        return manager.searchNearestNeighbors(
                        entityMetadata.name(),
                        queryVector,
                        mappedFilters,
                        limit)
                .stream()
                .map(entity -> converter.toEntity(entityClass, entity))
                .toList();
    }

    @Override
    public <T> List<T> searchWithinThreshold(Class<T> entityClass,
                                             Vector queryVector,
                                             float threshold) {

        requireNonNull(entityClass, "entityClass is required");
        requireNonNull(queryVector, "queryVector is required");

        if (!Float.isFinite(threshold)) {
            throw new IllegalArgumentException("threshold must be finite");
        }

        var entityMetadata = entities.get(entityClass);

        return manager.searchWithinThreshold(
                        entityMetadata.name(),
                        queryVector,
                        threshold)
                .stream()
                .map(entity -> converter.toEntity(entityClass, entity))
                .toList();
    }
}
