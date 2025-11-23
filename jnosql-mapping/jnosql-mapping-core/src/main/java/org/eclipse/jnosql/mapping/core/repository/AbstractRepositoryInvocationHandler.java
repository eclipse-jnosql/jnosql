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
import org.eclipse.jnosql.mapping.metadata.repository.ReflectionMethodKey;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethodType;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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

    protected Map<Method, RepositoryMethodDescriptor> methodRepositoryTypeMap = new HashMap<>();

    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {

        RepositoryMethodDescriptor methodDescriptor = methodDescriptor(method);

        switch (methodDescriptor.type()) {
            case DEFAULT -> {
                return unwrapInvocationTargetException(() -> method.invoke(repository(), params));
            } case OBJECT_METHOD -> {
                return unwrapInvocationTargetException(() ->  unwrapInvocationTargetException(() -> method.invoke(this, params)));
            }
        }
        return null;
    }

    private RepositoryMethodDescriptor methodDescriptor(Method method) {
        RepositoryMethodDescriptor repositoryMethodType = this.methodRepositoryTypeMap.get(method);
        if(repositoryMethodType == null) {
            var repositoryMethod = repositoryMetadata().find(new ReflectionMethodKey(method));
            var type  = repositoryMethod.map(RepositoryMethod::type).orElse(RepositoryMethodType.UNKNOWN);
            if(RepositoryMethodType.UNKNOWN.equals(type)) {
             //validation here
            } else {
                this.methodRepositoryTypeMap.put(method, repositoryMethodType);
            }
        }
        return repositoryMethodType;
    }

    protected Object unwrapInvocationTargetException(ThrowingSupplier<Object> supplier) throws Throwable {
        try {
            return supplier.get();
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
}
