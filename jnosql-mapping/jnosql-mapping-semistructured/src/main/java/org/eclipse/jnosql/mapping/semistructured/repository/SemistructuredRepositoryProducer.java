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
package org.eclipse.jnosql.mapping.semistructured.repository;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.repository.CoreRepositoryInvocationHandler;
import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.core.repository.RepositoryOperationProvider;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * CDI producer responsible for creating runtime implementations of semistructured Jakarta Data repositories.
 */
@ApplicationScoped
public class SemistructuredRepositoryProducer {

    private final EntitiesMetadata entities;
    private final RepositoriesMetadata repositoriesMetadata;
    private final InfrastructureOperatorProvider infrastructureOperatorProvider;
    private final RepositoryOperationProvider repositoryOperationProvider;

    /**
     * CDI constructor injection. All four dependencies are required.
     *
     * @param entities                    entity reflection metadata
     * @param repositoriesMetadata        repository interface metadata
     * @param infrastructureOperatorProvider proxy infrastructure operators
     * @param repositoryOperationProvider semistructured repository operations
     */
    @Inject
    public SemistructuredRepositoryProducer(EntitiesMetadata entities,
                                            RepositoriesMetadata repositoriesMetadata,
                                            InfrastructureOperatorProvider infrastructureOperatorProvider,
                                            RepositoryOperationProvider repositoryOperationProvider) {
        this.entities = entities;
        this.repositoriesMetadata = repositoriesMetadata;
        this.infrastructureOperatorProvider = infrastructureOperatorProvider;
        this.repositoryOperationProvider = repositoryOperationProvider;
    }

    /**
     * Package-private no-arg constructor for CDI proxy compatibility.
     * Do not use directly.
     */
    SemistructuredRepositoryProducer() {
        this(null, null, null, null);
    }

    /**
     * Returns a fully functional repository implementation for the given
     * repository interface.
     *
     * @param repositoryClass the repository interface to implement
     * @param template the semistructured template used by the repository
     * @param <R> the repository type
     * @return an instance implementing the given repository interface
     * @throws NullPointerException if any argument is {@code null}
     * @throws java.util.NoSuchElementException if required repository or entity
     *         metadata cannot be resolved
     */
    @SuppressWarnings("unchecked")
    public <R> R get(Class<?> repositoryClass, SemiStructuredTemplate template) {
        Objects.requireNonNull(repositoryClass, "repository class is required");
        Objects.requireNonNull(template, "template class is required");
        RepositoryMetadata repositoryMetadata = repositoriesMetadata.get(repositoryClass).orElseThrow();
        var entityMetadata = entities.get(repositoryMetadata.entity().orElseThrow());

        var executor = SemistructuredRepository.of(template, entityMetadata);

        var repositoryHandler = CoreRepositoryInvocationHandler.of(executor,
                entityMetadata,
                repositoryMetadata,
                infrastructureOperatorProvider,
                repositoryOperationProvider,
                template);

        return (R) Proxy.newProxyInstance(repositoryClass.getClassLoader(),
                new Class[]{repositoryClass},
                repositoryHandler);
    }

}
