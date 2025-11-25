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

class TestRepositoryInvocationHandler<T, K> extends AbstractRepositoryInvocationHandler<T, K>  {

    private final AbstractRepository<T, K> repository;

    private final EntityMetadata entityMetadata;

    private final RepositoryMetadata repositoryMetadata;

    private final InfrastructureOperatorProvider infrastructureOperatorProvider;


    public TestRepositoryInvocationHandler(AbstractRepository<T, K> repository,
                                           EntityMetadata entityMetadata,
                                           RepositoryMetadata repositoryMetadata,
                                           InfrastructureOperatorProvider infrastructureOperatorProvider) {
        this.repository = repository;
        this.entityMetadata = entityMetadata;
        this.repositoryMetadata = repositoryMetadata;
        this.infrastructureOperatorProvider = infrastructureOperatorProvider;
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
    protected RepositoryMetadata repositoryMetadata() {
        return repositoryMetadata;
    }

    @Override
    protected InfrastructureOperatorProvider infrastructureOperatorProvider() {
        return infrastructureOperatorProvider;
    }
}