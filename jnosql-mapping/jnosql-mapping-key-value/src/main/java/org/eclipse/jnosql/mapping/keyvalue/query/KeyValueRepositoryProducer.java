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


import jakarta.data.repository.BasicRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InterceptionFactory;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.eclipse.jnosql.mapping.core.repository.CoreRepositoryInvocationHandler;
import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreBaseRepositoryOperationProvider;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplate;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplateProducer;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;

import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * CDI producer for key-value repository proxies. The resulting JNoSQL proxy is
 * wrapped by CDI so repository interceptor bindings can be applied.
 */
@ApplicationScoped
public class KeyValueRepositoryProducer {

    private final KeyValueTemplateProducer producer;
    private final EntitiesMetadata entities;
    private final InfrastructureOperatorProvider infrastructureOperatorProvider;

    private final CoreBaseRepositoryOperationProvider repositoryOperationProvider;

    private final RepositoriesMetadata repositoriesMetadata;

    private final LifecycleEventHandler lifecycleEventHandler;

    private final BeanManager beanManager;

    @Inject
    KeyValueRepositoryProducer(KeyValueTemplateProducer producer,
                               EntitiesMetadata entities,
                               InfrastructureOperatorProvider infrastructureOperatorProvider,
                               CoreBaseRepositoryOperationProvider repositoryOperationProvider,
                               RepositoriesMetadata repositoriesMetadata,
                               LifecycleEventHandler lifecycleEventHandler,
                               BeanManager beanManager) {
        this.producer = producer;
        this.entities = entities;
        this.infrastructureOperatorProvider = infrastructureOperatorProvider;
        this.repositoryOperationProvider = repositoryOperationProvider;
        this.repositoriesMetadata = repositoriesMetadata;
        this.lifecycleEventHandler = lifecycleEventHandler;
        this.beanManager = beanManager;
    }

    KeyValueRepositoryProducer() {
        this(null, null, null, null, null, null, null);
    }

    /**
     * Creates a key-value repository backed by a bucket manager.
     *
     * @param repositoryClass the repository class
     * @param manager the bucket manager
     * @param <T> the entity type
     * @param <K> the entity identifier type
     * @param <R> the repository type
     * @return the repository proxy
     */
    public <T, K, R extends BasicRepository<T, K>> R get(Class<R> repositoryClass, BucketManager manager) {
        Objects.requireNonNull(repositoryClass, "repository class is required");
        Objects.requireNonNull(manager, "manager class is required");
        KeyValueTemplate template = producer.apply(manager);
        return get(repositoryClass, template);
    }

    /**
     * Creates a key-value repository backed by a template.
     *
     * @param repositoryClass the repository class
     * @param template the key-value template
     * @param <R> the repository type
     * @return the repository proxy
     */
    public <R extends BasicRepository<?, ?>> R get(Class<R> repositoryClass, KeyValueTemplate template) {
        Objects.requireNonNull(repositoryClass, "repository class is required");
        Objects.requireNonNull(template, "template class is required");
        return get(repositoryClass, template, beanManager.createCreationalContext(null));
    }

    /**
     * Creates a CDI-intercepted key-value repository backed by a template.
     *
     * @param repositoryClass the repository class
     * @param template the key-value template
     * @param creationalContext the repository bean creational context
     * @param <R> the repository type
     * @return the intercepted repository proxy
     */
    public <R extends BasicRepository<?, ?>> R get(Class<R> repositoryClass, KeyValueTemplate template,
                                                   CreationalContext<R> creationalContext) {
        Objects.requireNonNull(repositoryClass, "repository class is required");
        Objects.requireNonNull(template, "template class is required");
        Objects.requireNonNull(creationalContext, "creational context is required");
        RepositoryMetadata repositoryMetadata = repositoriesMetadata.get(repositoryClass).orElseThrow();
        var entityMetadata = entities.get(repositoryMetadata.entity().orElseThrow());
        DefaultKeyValueRepository<?, ?> executor = DefaultKeyValueRepository.of(template, entityMetadata, lifecycleEventHandler);
        var repositoryHandler =  CoreRepositoryInvocationHandler.of(executor
                , entityMetadata,
                repositoryMetadata,
                infrastructureOperatorProvider,
                repositoryOperationProvider,
                template);
        R repositoryProxy = repositoryClass.cast(Proxy.newProxyInstance(repositoryClass.getClassLoader(),
                new Class<?>[]{repositoryClass},
                repositoryHandler));
        InterceptionFactory<R> interceptionFactory =
                beanManager.createInterceptionFactory(creationalContext, repositoryClass);
        return interceptionFactory.createInterceptedInstance(repositoryProxy);
    }
}
