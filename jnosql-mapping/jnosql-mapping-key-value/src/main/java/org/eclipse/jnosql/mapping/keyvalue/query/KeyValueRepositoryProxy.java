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


import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplate;
import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

import java.lang.reflect.ParameterizedType;

class KeyValueRepositoryProxy<T, K> extends AbstractKeyValueRepositoryProxy<T, K> {

    private final DefaultKeyValueRepository<T, K>  repository;
    private final KeyValueTemplate template;
    private final Class<T> type;

    private final Class<?> repositoryType;

    private final EntityMetadata metadata;

    KeyValueRepositoryProxy(Class<?> repositoryType, EntitiesMetadata entitiesMetadata, KeyValueTemplate template) {
        Class<T> typeClass = (Class) ((ParameterizedType) repositoryType.getGenericInterfaces()[0])
                .getActualTypeArguments()[0];
        this.metadata = entitiesMetadata.get(typeClass);
        this.repository = new DefaultKeyValueRepository<>(metadata, template);
        this.template = template;
        this.type = typeClass;
        this.repositoryType = repositoryType;
    }

    @Override
    protected AbstractRepository<T, K> repository() {
        return repository;
    }

    @Override
    protected KeyValueTemplate template() {
        return template;
    }

    @Override
    protected Class<T> type() {
        return type;
    }

    @Override
    protected Class<?> repositoryType() {
        return repositoryType;
    }

    @Override
    protected EntityMetadata entityMetadata() {
        return metadata;
    }

}
