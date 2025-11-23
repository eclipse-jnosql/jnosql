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
package org.eclipse.jnosql.mapping.core.repository;

import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public abstract class AbstractRepositoryInvocationHandler<T, K> implements InvocationHandler {

    /**
     * Retrieves the underlying repository associated with this proxy.
     *
     * @return The underlying repository.
     */
    protected abstract AbstractRepository<T, K> repository();

    /**
     * Retrieves the type of the repository interface.
     *
     * @return The repository interface type.
     */
    protected abstract Class<?> repositoryType();

    /**
     * Retrieves the metadata information about the entity managed by this repository.
     *
     * @return The entity metadata information.
     */
    protected abstract EntityMetadata entityMetadata();

    /**
     * Retrieves the metadata information about the repository.
     * @return the metadata information
     */
    protected abstract RepositoryMetadata repositoryMetadata();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
