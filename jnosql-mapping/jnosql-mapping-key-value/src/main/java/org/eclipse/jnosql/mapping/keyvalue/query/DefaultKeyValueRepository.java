/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.keyvalue.query;


import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplate;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;

import java.util.Objects;

/**
 * Default implementation of a key-value repository for Java NoSQL databases.
 * This class extends the AbstractKeyValueRepository and provides the necessary
 * functionality for interacting with a key-value store using a KeyValueTemplate.
 *
 * @param <T> The type of entities managed by the repository.
 * @param <K> The type of the key used for key-value operations.
 */
class DefaultKeyValueRepository<T, K>  extends AbstractRepository<T, K> {


    private final KeyValueTemplate repository;

    private final EntityMetadata metadata;

    private final LifecycleEventHandler lifecycleEventHandler;

    DefaultKeyValueRepository(EntityMetadata metadata, KeyValueTemplate repository, LifecycleEventHandler lifecycleEventHandler) {
        this.repository = repository;
        this.metadata = metadata;
        this.lifecycleEventHandler = lifecycleEventHandler;
    }

    @Override
    protected KeyValueTemplate template() {
        return repository;
    }

    @Override
    protected EntityMetadata entityMetadata() {
        return metadata;
    }

    @Override
    protected LifecycleEventHandler lifeCycle() {
        return lifecycleEventHandler;
    }

    @Override
    protected String getErrorMessage() {
        return "The key-value type does not support %s method";
    }


    /**
     * Creates a new instance of DefaultKeyValueRepository with the provided KeyValueTemplate,
     * EntityMetadata, and LifecycleEventHandler.
     *
     * @param <T> The type of entities managed by the repository.
     * @param <K> The type of the key used for key-value operations.
     * @param template The KeyValueTemplate used for database operations. Must not be {@code null}.
     * @param metadata The metadata information about the entity. Must not be {@code null}.
     * @param lifecycleEventHandler the lifecycle event handler for pre/post operation events
     * @return A new instance of DefaultKeyValueRepository.
     * @throws NullPointerException If any argument is {@code null}.
     */
    static <T, K> DefaultKeyValueRepository<T, K> of(KeyValueTemplate template, EntityMetadata metadata,
                                                           LifecycleEventHandler lifecycleEventHandler) {
        Objects.requireNonNull(template,"template is required");
        Objects.requireNonNull(metadata,"metadata is required");
        Objects.requireNonNull(lifecycleEventHandler,"lifecycleEventHandler is required");
        return new DefaultKeyValueRepository<>(metadata, template, lifecycleEventHandler);
    }
}