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
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InterceptionFactory;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.repository.CoreRepositoryInvocationHandler;
import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * CDI producer responsible for resolving repository metadata and creating runtime
 * implementations of semistructured Jakarta Data repositories. The resulting JNoSQL
 * repository proxy is wrapped by CDI so that repository interceptor bindings are applied.
 */
@ApplicationScoped
public class SemistructuredRepositoryProducer {

    private final EntitiesMetadata entities;

    private final InfrastructureOperatorProvider infrastructureOperatorProvider;

    private final SemistructuredRepositoryOperationProvider semistructuredRepositoryOperationProvider;

    private final RepositoriesMetadata repositoriesMetadata;

    private final LifecycleEventHandler lifecycleEventHandler;

    private final BeanManager beanManager;

    @Inject
    SemistructuredRepositoryProducer(EntitiesMetadata entities,
                                     InfrastructureOperatorProvider infrastructureOperatorProvider,
                                     SemistructuredRepositoryOperationProvider semistructuredRepositoryOperationProvider,
                                     RepositoriesMetadata repositoriesMetadata,
                                     LifecycleEventHandler lifecycleEventHandler,
                                     BeanManager beanManager) {
        this.entities = entities;
        this.infrastructureOperatorProvider = infrastructureOperatorProvider;
        this.semistructuredRepositoryOperationProvider = semistructuredRepositoryOperationProvider;
        this.repositoriesMetadata = repositoriesMetadata;
        this.lifecycleEventHandler = lifecycleEventHandler;
        this.beanManager = beanManager;
    }

    public SemistructuredRepositoryProducer() {
        this(null, null, null, null, null, null);
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
    public <R> R get(Class<R> repositoryClass, SemiStructuredTemplate template) {
        Objects.requireNonNull(repositoryClass, "repository class is required");
        Objects.requireNonNull(template, "template class is required");
        return get(repositoryClass, template, beanManager.createCreationalContext(null));
    }

    /**
     * Returns a fully functional CDI-intercepted repository implementation using
     * the repository bean's creational context.
     *
     * @param repositoryClass the repository interface to implement
     * @param template the semistructured template used by the repository
     * @param creationalContext the repository bean creational context
     * @param <R> the repository type
     * @return an intercepted instance implementing the repository interface
     * @throws NullPointerException if any argument is {@code null}
     * @throws java.util.NoSuchElementException if required repository or entity
     *         metadata cannot be resolved
     */
    public <R> R get(Class<R> repositoryClass, SemiStructuredTemplate template,
                     CreationalContext<R> creationalContext) {
        Objects.requireNonNull(repositoryClass, "repository class is required");
        Objects.requireNonNull(template, "template class is required");
        Objects.requireNonNull(creationalContext, "creational context is required");
        RepositoryMetadata repositoryMetadata = repositoriesMetadata.get(repositoryClass).orElseThrow();
        var entityMetadata = entities.get(repositoryMetadata.entity().orElseThrow());

        var executor = SemistructuredRepository.of(template, entityMetadata, lifecycleEventHandler);

        var repositoryHandler = CoreRepositoryInvocationHandler.of(executor,
                entityMetadata,
                repositoryMetadata,
                infrastructureOperatorProvider,
                semistructuredRepositoryOperationProvider,
                template);

        R repositoryProxy = repositoryClass.cast(Proxy.newProxyInstance(repositoryClass.getClassLoader(),
                new Class<?>[]{repositoryClass},
                repositoryHandler));
        InterceptionFactory<R> interceptionFactory =
                beanManager.createInterceptionFactory(creationalContext, repositoryClass);
        return interceptionFactory.createInterceptedInstance(repositoryProxy);
    }

}
