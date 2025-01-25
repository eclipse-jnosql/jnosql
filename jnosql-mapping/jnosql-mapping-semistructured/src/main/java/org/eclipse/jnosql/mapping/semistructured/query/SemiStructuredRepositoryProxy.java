/*
 *  Copyright (c) 2022,2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.semistructured.query;


import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;


/**
 * Proxy handler to generate {@link org.eclipse.jnosql.mapping.NoSQLRepository} for Semistructure database repositories.
 *
 * @param <T> The entity type managed by the repository.
 * @param <K> The key type used for column-based operations.
 */
public class SemiStructuredRepositoryProxy<T, K> extends AbstractSemiStructuredRepositoryProxy<T, K> {

    private final SemiStructuredTemplate template;

    private final SemiStructuredRepository<T, K> repository;

    private final EntityMetadata entityMetadata;

    private final Converters converters;

    private final Class<?> repositoryType;

    private final EntitiesMetadata entitiesMetadata;

    /**
     * Creates a new instance of ColumnRepositoryProxy.
     *
     * @param template   The SemistructuredTemplate used for column database operations. Must not be {@code null}.
     * @param entities   The metadata of the entities. Must not be {@code null}.
     * @param repositoryType The repository type. Must not be {@code null}.
     * @param converters The converters
     * @throws NullPointerException If either the template, metadata, or repository type is {@code null}.
     */
   @SuppressWarnings({"unchecked", "rawtypes"})
   public SemiStructuredRepositoryProxy(SemiStructuredTemplate template,
                                        EntitiesMetadata entities, Class<?> repositoryType,
                                        Converters converters) {
        this.template = template;
        Class<T> typeClass = (Class) ((ParameterizedType) repositoryType.getGenericInterfaces()[0])
                .getActualTypeArguments()[0];
        this.entityMetadata = entities.get(typeClass);
        this.repository = new SemiStructuredRepository<>(template, entityMetadata);
        this.converters = converters;
        this.repositoryType =  repositoryType;
        this.entitiesMetadata = entities;
    }

    SemiStructuredRepositoryProxy(SemiStructuredTemplate template,
                                  EntityMetadata metadata, Class<?> typeClass,
                                  Converters converters,
                                  EntitiesMetadata entities) {
        this.template = template;
        this.entityMetadata = metadata;
        this.repository = new SemiStructuredRepository<>(template, entityMetadata);
        this.converters = converters;
        this.repositoryType =  typeClass;
        this.entitiesMetadata = entities;
    }

    @Override
    protected AbstractRepository<T, K> repository() {
        return repository;
    }

    @Override
    protected EntityMetadata entityMetadata() {
        return entityMetadata;
    }

    @Override
    protected EntitiesMetadata entitiesMetadata() {
        return entitiesMetadata;
    }

    @Override
    protected SemiStructuredTemplate template() {
        return template;
    }

    @Override
    protected Converters converters() {
        return converters;
    }

    @Override
    protected Class<?> repositoryType() {
        return repositoryType;
    }


    /**
     * Repository implementation for column-based repositories.
     *
     * @param <T> The entity type managed by the repository.
     * @param <K> The key type used for column-based operations.
     */
    public static class SemiStructuredRepository<T, K> extends AbstractSemiStructuredRepository<T, K> {

        private final SemiStructuredTemplate template;

        private final EntityMetadata entityMetadata;

        SemiStructuredRepository(SemiStructuredTemplate template, EntityMetadata entityMetadata) {
            this.template = template;
            this.entityMetadata = entityMetadata;
        }

        @Override
        protected SemiStructuredTemplate template() {
            return template;
        }

        @Override
        protected EntityMetadata entityMetadata() {
            return entityMetadata;
        }

        /**
         * Creates a new instance of ColumnRepository.
         *
         * @param <T>      The entity type managed by the repository.
         * @param <K>      The key type used for column-based operations.
         * @param template The SemistructuredTemplate used for column database operations. Must not be {@code null}.
         * @param metadata The metadata of the entity. Must not be {@code null}.
         * @return A new instance of ColumnRepository.
         * @throws NullPointerException If either the template or metadata is {@code null}.
         */
        public static <T, K> SemiStructuredRepository<T, K> of(SemiStructuredTemplate template, EntityMetadata metadata) {
            Objects.requireNonNull(template,"template is required");
            Objects.requireNonNull(metadata,"metadata is required");
            return new SemiStructuredRepository<>(template, metadata);
        }
    }
}
