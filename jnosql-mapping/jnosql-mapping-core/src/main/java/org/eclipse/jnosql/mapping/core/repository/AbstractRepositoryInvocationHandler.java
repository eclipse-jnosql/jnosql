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
import java.util.Optional;
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
     * Returns the repository instance associated with this proxy.
     * The returned repository is used as the execution target for built-in repository
     * operations and may also be required by infrastructure operators when invoking
     * repository methods.
     *
     * @return the repository instance handled by this invocation handler
     */
    protected abstract AbstractRepository<T, K> repository();

    /**
     * Returns metadata describing the entity managed by this repository.
     * The metadata provides structural information such as entity attributes,
     * identifiers, and mapping configuration used during repository operation
     * execution.
     *
     * @return the entity metadata associated with the repository
     */
    protected abstract EntityMetadata entityMetadata();

    /**
     * Returns metadata describing the repository definition.
     * Repository metadata contains information about repository methods,
     * including their classification and mapping to repository operations.
     *
     * @return the repository metadata
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

        RepositoryMethodDescriptor methodDescriptor = resolveMethodDescriptor(method);

        return dispatchRepositoryMethod(proxy, method, params, methodDescriptor);
    }

    protected Object dispatchRepositoryMethod(Object proxy, Method method, Object[] params,
                                      RepositoryMethodDescriptor methodDescriptor) throws Throwable {
        switch (methodDescriptor.type()) {
            case BUILT_IN_METHOD -> {
                return executeAndUnwrapInvocationException(() -> infrastructureOperatorProvider().buildInMethodOperator().invokeDefault(repository(), method, params));
            }
            case DEFAULT_METHOD -> {
                return executeAndUnwrapInvocationException(() -> infrastructureOperatorProvider().defaultMethodOperator().invokeDefault(proxy, method, params));
            }
            case OBJECT_METHOD -> {
                return executeAndUnwrapInvocationException(() -> executeAndUnwrapInvocationException(() ->
                        infrastructureOperatorProvider().objectMethodOperator().invokeObjectMethod(this, method, params)));
            }
            case CUSTOM_REPOSITORY -> {
                return executeAndUnwrapInvocationException(() ->
                        infrastructureOperatorProvider().customRepositoryMethodOperator().invokeCustomRepository(method, params));
            }
            case INSERT -> {
                var context = createInvocationContext(params, methodDescriptor);
                return executeAndUnwrapInvocationException(() -> executeAndUnwrapInvocationException(() ->
                        repositoryOperationProvider().insertOperation().execute(context)));
            }
            case UPDATE -> {
                var context = createInvocationContext(params, methodDescriptor);
                return executeAndUnwrapInvocationException(() -> executeAndUnwrapInvocationException(() ->
                        repositoryOperationProvider().updateOperation().execute(context)));
            }
            case DELETE -> {
                var context = createInvocationContext(params, methodDescriptor);
                return executeAndUnwrapInvocationException(() -> executeAndUnwrapInvocationException(() ->
                        repositoryOperationProvider().deleteOperation().execute(context)));
            }
            case SAVE -> {
                var context = createInvocationContext(params, methodDescriptor);
                return executeAndUnwrapInvocationException(() -> executeAndUnwrapInvocationException(() ->
                        repositoryOperationProvider().saveOperation().execute(context)));
            }
            case DELETE_BY -> {
                var context = createInvocationContext(params, methodDescriptor);
                return executeAndUnwrapInvocationException(() -> executeAndUnwrapInvocationException(() ->
                        repositoryOperationProvider().deleteByOperation().execute(context)));
            }
            case FIND_BY -> {
                var context = createInvocationContext(params, methodDescriptor);
                return executeAndUnwrapInvocationException(() -> executeAndUnwrapInvocationException(() ->
                        repositoryOperationProvider().findByOperation().execute(context)));
            }
            case COUNT_ALL -> {
                var context = createInvocationContext(params, methodDescriptor);
                return executeAndUnwrapInvocationException(() -> executeAndUnwrapInvocationException(() ->
                        repositoryOperationProvider().countAllOperation().execute(context)));
            }
            case COUNT_BY -> {
                var context = createInvocationContext(params, methodDescriptor);
                return executeAndUnwrapInvocationException(() -> executeAndUnwrapInvocationException(() ->
                        repositoryOperationProvider().countByOperation().execute(context)));
            }
            case CURSOR_PAGINATION -> {
                var context = createInvocationContext(params, methodDescriptor);
                return executeAndUnwrapInvocationException(() -> executeAndUnwrapInvocationException(() ->
                        repositoryOperationProvider().cursorPaginationOperation().execute(context)));
            }
            case PARAMETER_BASED -> {
                var context = createInvocationContext(params, methodDescriptor);
                return executeAndUnwrapInvocationException(() -> executeAndUnwrapInvocationException(() ->
                        repositoryOperationProvider().parameterBasedOperation().execute(context)));
            }
            case EXISTS_BY -> {
                var context = createInvocationContext(params, methodDescriptor);
                return executeAndUnwrapInvocationException(() -> executeAndUnwrapInvocationException(() ->
                        repositoryOperationProvider().existsByOperation().execute(context)));
            }
            case FIND_ALL -> {
                var context = createInvocationContext(params, methodDescriptor);
                return executeAndUnwrapInvocationException(() -> executeAndUnwrapInvocationException(() ->
                        repositoryOperationProvider().findAllOperation().execute(context)));
            }
            case QUERY -> {
                var context = createInvocationContext(params, methodDescriptor);
                return executeAndUnwrapInvocationException(() -> executeAndUnwrapInvocationException(() ->
                        repositoryOperationProvider().queryOperation().execute(context)));
            } case PROVIDER_OPERATION -> {
                var context = createInvocationContext(params, methodDescriptor);
                return executeAndUnwrapInvocationException(() -> executeAndUnwrapInvocationException(() ->
                        repositoryOperationProvider().providerOperation().execute(context)));
            }
            default -> throw new UnsupportedOperationException("Method not supported: " + method);
        }
    }

    private RepositoryInvocationContext createInvocationContext(Object[] params, RepositoryMethodDescriptor methodDescriptor) {
        return new RepositoryInvocationContext(methodDescriptor.method(),
                repositoryMetadata(), entityMetadata(),
                template(), params == null ? EMPTY : params);
    }

    /**
     * Resolves the {@link RepositoryMethodDescriptor} associated with the given method.
     * <p>
     * The descriptor describes how the method should be handled by the repository
     * proxy. Results are cached to avoid repeated metadata resolution.
     *
     * @param method the repository method being invoked
     * @return the resolved method descriptor
     */
    protected RepositoryMethodDescriptor resolveMethodDescriptor(Method method) {
        var repositoryMethodType = this.methodRepositoryTypeMap.get(method);
        if (repositoryMethodType == null) {
            repositoryMethodType = computeMethodDescriptor(method);
        }
        return repositoryMethodType;
    }

    /**
     * Computes the {@link RepositoryMethodDescriptor} for the given method.
     * <p>
     * This method inspects repository metadata to determine the
     * {@link RepositoryMethodType}. If the type cannot be resolved directly
     * from metadata, the resolution is delegated to
     * {@link #resolveUnknownMethodType(Method)}.
     *
     * @param method the repository method
     * @return the computed method descriptor
     */
    protected RepositoryMethodDescriptor computeMethodDescriptor(Method method) {
        var repositoryMethod = repositoryMetadata().find(new ReflectionMethodKey(method));
        var type = repositoryMethod.map(RepositoryMethod::type).orElse(RepositoryMethodType.UNKNOWN);
        if (!RepositoryMethodType.UNKNOWN.equals(type)) {
            RepositoryMethodDescriptor repositoryMethodType = new RepositoryMethodDescriptor(type, repositoryMethod.orElseThrow());
            this.methodRepositoryTypeMap.put(method, repositoryMethodType);
            return repositoryMethodType;
        }

        var repositoryMethodType = resolveUnknownMethodType(method);
        this.methodRepositoryTypeMap.put(method, repositoryMethodType);
        return repositoryMethodType;
    }

    /**
     * Resolves the method descriptor when the method type cannot be determined
     * from repository metadata.
     * <p>
     * This method identifies common infrastructure cases such as object methods,
     * built-in repository methods, or CDI-managed custom repositories.
     * <p>
     * Subclasses may extend the resolution process by overriding
     * {@link #resolveCustomMethodType(Method)}.
     *
     * @param method the repository method
     * @return the resolved descriptor, or {@code null} if the method type
     *         cannot be determined
     */
    protected RepositoryMethodDescriptor resolveUnknownMethodType(Method method) {
        if (Object.class.equals(method.getDeclaringClass())) {
            return new RepositoryMethodDescriptor(RepositoryMethodType.OBJECT_METHOD, null);
        } else if (IS_REPOSITORY_METHOD.test(method.getDeclaringClass())) {
            return new RepositoryMethodDescriptor(RepositoryMethodType.BUILT_IN_METHOD, null);
        } else if (isCdiManagedComponent(method.getDeclaringClass())) {
            return new RepositoryMethodDescriptor(RepositoryMethodType.CUSTOM_REPOSITORY, null);
        }
        return resolveCustomMethodType(method).orElse(null);
    }

    /**
     * Resolves custom repository method types not handled by the default
     * resolution logic.
     * <p>
     * Subclasses may override this method to support additional method
     * classifications. Returning {@link Optional#empty()} indicates that
     * the method could not be resolved.
     *
     * @param method the repository method
     * @return an optional descriptor describing the method type
     */
    protected Optional<RepositoryMethodDescriptor> resolveCustomMethodType(Method method) {
        return Optional.empty();
    }

    /**
     * Executes the given supplier and unwraps any {@link InvocationTargetException}
     * thrown during invocation.
     * <p>
     * If the supplier throws an {@code InvocationTargetException}, the original
     * cause is rethrown to preserve the underlying exception semantics.
     *
     * @param supplier the operation to execute
     * @return the result produced by the supplier
     * @throws Throwable if the supplier throws an exception
     */
    protected Object executeAndUnwrapInvocationException(ThrowingSupplier<Object> supplier) throws Throwable {
        try {
            return supplier.get();
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }

    private boolean isCdiManagedComponent(Class<?> type) {
        return CDI.current().select(type).isResolvable();
    }
}
