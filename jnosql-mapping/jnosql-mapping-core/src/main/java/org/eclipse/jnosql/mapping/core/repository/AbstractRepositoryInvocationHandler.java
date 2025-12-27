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

import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.CrudRepository;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.nosql.Template;
import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.ReflectionMethodKey;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethodType;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * An abstract implementation of the {@link InvocationHandler} specifically designed for repository-related proxy
 * invocation handling. This class provides a framework for intercepting and dispatching method calls to dynamic proxies
 * that wrap repository instances. It determines the type of method being invoked and dispatches it appropriately using
 * specialized operators.
 *
 * @param <T> The type of the entity managed by the repository.
 * @param <K> The type of the key or identifier of the entity.
 */
public abstract class AbstractRepositoryInvocationHandler<T, K> implements InvocationHandler {
    private static final Predicate<Class<?>> IS_REPOSITORY_METHOD = Predicate.<Class<?>>isEqual(CrudRepository.class)
            .or(Predicate.isEqual(BasicRepository.class))
            .or(Predicate.isEqual(NoSQLRepository.class));
    private static final Object[] EMPTY = new Object[0];

    protected final Map<Method, RepositoryMethodDescriptor> methodRepositoryTypeMap = new HashMap<>();

    /**
     * Retrieves the underlying repository associated with this proxy.
     *
     * @return The underlying repository.
     */
    protected abstract AbstractRepository<T, K> repository();

    /**
     * Retrieves the metadata information about the entity managed by this repository.
     *
     * @return The entity metadata information.
     */
    protected abstract EntityMetadata entityMetadata();

    /**
     * Retrieves the metadata information about the repository.
     *
     * @return the metadata information
     */
    protected abstract RepositoryMetadata repositoryMetadata();

    /**
     * Provides an instance of InfrastructureOperatorProvider, which supplies operational components required by the
     * dynamic repository proxy. These components manage how various categories of infrastructure-level method
     * invocations are performed. It is intended for internal use by the proxy implementation and not by the
     * repository's semantic execution layer.
     *
     * @return an InfrastructureOperatorProvider instance that facilitates operation handling
     */
    protected abstract InfrastructureOperatorProvider infrastructureOperatorProvider();

    /**
     * Provides the {@link RepositoryOperationProvider} to manage and execute repository operations. Each operation
     * corresponds to a specific type of repository interaction, offering consistent and extensible behavior for
     * repository method execution. This method is utilized internally by the dynamic repository proxy and
     * code-generated repository classes.
     *
     * @return an instance of {@link RepositoryOperationProvider} responsible for delegating repository method
     * execution.
     */
    protected abstract RepositoryOperationProvider repositoryOperationProvider();

    /**
     * Provides an implementation of the {@code Template} that serves as a central component for managing and
     * orchestrating operations related to repositories.
     *
     * @return an instance of {@code Template} responsible for executing the repository-related operations and acting as
     * a mediator for infrastructure functions in the repository context.
     */
    protected abstract Template template();

    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {

        RepositoryMethodDescriptor methodDescriptor = methodDescriptor(method);

        switch (methodDescriptor.type()) {
            case BUILT_IN_METHOD -> {
                return unwrapInvocationTargetException(() -> infrastructureOperatorProvider().buildInMethodOperator().invokeDefault(repository(), method, params));
            }
            case DEFAULT_METHOD -> {
                return unwrapInvocationTargetException(() -> infrastructureOperatorProvider().defaultMethodOperator().invokeDefault(proxy, method, params));
            }
            case OBJECT_METHOD -> {
                return unwrapInvocationTargetException(() -> unwrapInvocationTargetException(() ->
                        infrastructureOperatorProvider().objectMethodOperator().invokeObjectMethod(this, method, params)));
            }
            case CUSTOM_REPOSITORY -> {
                return unwrapInvocationTargetException(() ->
                        infrastructureOperatorProvider().customRepositoryMethodOperator().invokeCustomRepository(method, params));
            }
            case INSERT -> {
                var context = repositoryInvocationContext(params, methodDescriptor);
                return unwrapInvocationTargetException(() -> unwrapInvocationTargetException(() ->
                        repositoryOperationProvider().insertOperation().execute(context)));
            }
            case UPDATE -> {
                var context = repositoryInvocationContext(params, methodDescriptor);
                return unwrapInvocationTargetException(() -> unwrapInvocationTargetException(() ->
                        repositoryOperationProvider().updateOperation().execute(context)));
            }
            case DELETE -> {
                var context = repositoryInvocationContext(params, methodDescriptor);
                return unwrapInvocationTargetException(() -> unwrapInvocationTargetException(() ->
                        repositoryOperationProvider().deleteOperation().execute(context)));
            }
            case SAVE -> {
                RepositoryInvocationContext context = repositoryInvocationContext(params, methodDescriptor);
                return unwrapInvocationTargetException(() -> unwrapInvocationTargetException(() ->
                        repositoryOperationProvider().saveOperation().execute(context)));
            }
            case DELETE_BY -> {
                var context = repositoryInvocationContext(params, methodDescriptor);
                return unwrapInvocationTargetException(() -> unwrapInvocationTargetException(() ->
                        repositoryOperationProvider().deleteByOperation().execute(context)));
            }
            case FIND_BY -> {
                var context = repositoryInvocationContext(params, methodDescriptor);
                return unwrapInvocationTargetException(() -> unwrapInvocationTargetException(() ->
                        repositoryOperationProvider().findByOperation().execute(context)));
            }
            case COUNT_ALL -> {
                var context = repositoryInvocationContext(params, methodDescriptor);
                return unwrapInvocationTargetException(() -> unwrapInvocationTargetException(() ->
                        repositoryOperationProvider().countAllOperation().execute(context)));
            }
            case COUNT_BY -> {
                var context = repositoryInvocationContext(params, methodDescriptor);
                return unwrapInvocationTargetException(() -> unwrapInvocationTargetException(() ->
                        repositoryOperationProvider().countByOperation().execute(context)));
            }
            case CURSOR_PAGINATION -> {
                var context = repositoryInvocationContext(params, methodDescriptor);
                return unwrapInvocationTargetException(() -> unwrapInvocationTargetException(() ->
                        repositoryOperationProvider().cursorPaginationOperation().execute(context)));
            }
            case PARAMETER_BASED -> {
                var context = repositoryInvocationContext(params, methodDescriptor);
                return unwrapInvocationTargetException(() -> unwrapInvocationTargetException(() ->
                        repositoryOperationProvider().parameterBasedOperation().execute(context)));
            }
            case EXISTS_BY -> {
                var context = repositoryInvocationContext(params, methodDescriptor);
                return unwrapInvocationTargetException(() -> unwrapInvocationTargetException(() ->
                        repositoryOperationProvider().existsByOperation().execute(context)));
            }
            case FIND_ALL -> {
                var context = repositoryInvocationContext(params, methodDescriptor);
                return unwrapInvocationTargetException(() -> unwrapInvocationTargetException(() ->
                        repositoryOperationProvider().findAllOperation().execute(context)));
            }
            case QUERY -> {
                var context = repositoryInvocationContext(params, methodDescriptor);
                return unwrapInvocationTargetException(() -> unwrapInvocationTargetException(() ->
                        repositoryOperationProvider().queryOperation().execute(context)));
            } case PROVIDER_OPERATION -> {
                var context = repositoryInvocationContext(params, methodDescriptor);
                return unwrapInvocationTargetException(() -> unwrapInvocationTargetException(() ->
                        repositoryOperationProvider().providerOperation().execute(context)));
            }
            default -> throw new UnsupportedOperationException("Method not supported: " + method);
        }
    }

    private RepositoryInvocationContext repositoryInvocationContext(Object[] params, RepositoryMethodDescriptor methodDescriptor) {
        return new RepositoryInvocationContext(methodDescriptor.method(),
                repositoryMetadata(), entityMetadata(),
                template(), params == null ? EMPTY : params);
    }

    protected RepositoryMethodDescriptor methodDescriptor(Method method) {
        var repositoryMethodType = this.methodRepositoryTypeMap.get(method);
        if (repositoryMethodType == null) {
            repositoryMethodType = processingMethodDescriptor(method);
        }
        return repositoryMethodType;
    }

    private RepositoryMethodDescriptor processingMethodDescriptor(Method method) {
        var repositoryMethod = repositoryMetadata().find(new ReflectionMethodKey(method));
        var type = repositoryMethod.map(RepositoryMethod::type).orElse(RepositoryMethodType.UNKNOWN);
        if (!RepositoryMethodType.UNKNOWN.equals(type)) {
            RepositoryMethodDescriptor repositoryMethodType = new RepositoryMethodDescriptor(type, repositoryMethod.orElseThrow());
            this.methodRepositoryTypeMap.put(method, repositoryMethodType);
            return repositoryMethodType;
        }

        RepositoryMethodDescriptor repositoryMethodType = null;
        if (Object.class.equals(method.getDeclaringClass())) {
            repositoryMethodType = new RepositoryMethodDescriptor(RepositoryMethodType.OBJECT_METHOD, null);
        } else if (IS_REPOSITORY_METHOD.test(method.getDeclaringClass())) {
            repositoryMethodType = new RepositoryMethodDescriptor(RepositoryMethodType.BUILT_IN_METHOD, null);
        } else if (isCDIComponent(method.getDeclaringClass())) {
            repositoryMethodType = new RepositoryMethodDescriptor(RepositoryMethodType.CUSTOM_REPOSITORY, null);
        }
        this.methodRepositoryTypeMap.put(method, repositoryMethodType);
        return repositoryMethodType;
    }

    private boolean isCDIComponent(Class<?> type) {
        return CDI.current().select(type).isResolvable();
    }

    protected Object unwrapInvocationTargetException(ThrowingSupplier<Object> supplier) throws Throwable {
        try {
            return supplier.get();
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
}
