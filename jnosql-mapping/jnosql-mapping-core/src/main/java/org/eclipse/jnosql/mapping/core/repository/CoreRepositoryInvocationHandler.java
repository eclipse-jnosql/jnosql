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

import jakarta.nosql.Template;
import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;

import java.util.Objects;

/**
 * Invocation handler responsible for executing repository methods for the
 * Core Jakarta NoSQL engine. This handler coordinates the interaction between
 * the underlying {@link AbstractRepository} implementation, entity metadata,
 * repository metadata, and the execution operators that perform individual
 * repository operations.
 *
 * <p>The Core handler provides the baseline method dispatching mechanism used
 * when no storage-specific engine (such as key-value or semi-structured) is
 * active. It delegates CRUD operations to the {@link AbstractRepository} and
 * forwards all semantic method types to the appropriate components supplied by
 * {@link InfrastructureOperatorProvider} and {@link RepositoryOperationProvider}.
 *
 * <p>This class serves as the default invocation pipeline for repositories
 * generated or proxied by the Core API, ensuring consistent resolution of
 * repository methods and integration with the underlying {@link Template}.
 */
public class CoreRepositoryInvocationHandler<T, K>  extends AbstractRepositoryInvocationHandler<T, K> {

    private final AbstractRepository<T, K> repository;

    private final EntityMetadata entityMetadata;

    private final RepositoryMetadata repositoryMetadata;

    private final InfrastructureOperatorProvider infrastructureOperatorProvider;

    private final RepositoryOperationProvider repositoryOperationProvider;

    private final Template template;

    private CoreRepositoryInvocationHandler(
            AbstractRepository<T, K> repository,
            EntityMetadata entityMetadata,
            RepositoryMetadata repositoryMetadata,
            InfrastructureOperatorProvider infrastructureOperatorProvider,
            RepositoryOperationProvider repositoryOperationProvider,
            Template template
    ) {
        this.repository = Objects.requireNonNull(repository, "repository is required");
        this.entityMetadata = Objects.requireNonNull(entityMetadata, "entityMetadata is required");
        this.repositoryMetadata = Objects.requireNonNull(repositoryMetadata, "repositoryMetadata is required");
        this.infrastructureOperatorProvider = Objects.requireNonNull(infrastructureOperatorProvider, "infrastructureOperatorProvider is required");
        this.repositoryOperationProvider = Objects.requireNonNull(repositoryOperationProvider, "repositoryOperationProvider is required");
        this.template = Objects.requireNonNull(template, "template is required");
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

    @Override
    protected RepositoryOperationProvider repositoryOperationProvider() {
        return repositoryOperationProvider;
    }

    @Override
    protected Template template() {
        return template;
    }

    /**
     * Creates a new {@code CoreRepositoryInvocationHandler} with the components
     * required to resolve and execute Jakarta Data repository methods through the
     * Core engine.
     *
     * @param repository                       the repository implementation that provides CRUD semantics
     * @param entityMetadata                   metadata describing the entity type managed by the repository
     * @param repositoryMetadata               metadata describing the Jakarta Data repository interface
     * @param infrastructureOperatorProvider   provider for executing built-in or custom infrastructure methods
     * @param repositoryOperationProvider      provider for executing semantic repository operations
     * @param template                         the underlying NoSQL {@link Template} used for persistence
     * @throws NullPointerException if any argument is {@code null}
     */
    public static <T, K> CoreRepositoryInvocationHandler<T, K> of(
            AbstractRepository<T, K> repository,
            EntityMetadata entityMetadata,
            RepositoryMetadata repositoryMetadata,
            InfrastructureOperatorProvider infrastructureOperatorProvider,
            RepositoryOperationProvider repositoryOperationProvider,
            Template template
    ) {
        return new CoreRepositoryInvocationHandler<>(
                repository,
                entityMetadata,
                repositoryMetadata,
                infrastructureOperatorProvider,
                repositoryOperationProvider,
                template
        );
    }
}