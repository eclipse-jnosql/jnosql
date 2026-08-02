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
package org.eclipse.jnosql.mapping.core.repository.operations;

import jakarta.data.restrict.Restriction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.metadata.repository.spi.DeleteOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Default implementation of the {@link DeleteOperation} used by the core
 * execution engine.
 *
 * <p>This operation executes a repository {@code delete} method whose
 * signature accepts exactly one argument and returns {@code void}.
 *
 * <p>Delete-by-restriction is not supported by default. Providers that support
 * restriction-based deletes must override {@link #deleteByRestriction}.</p>
 *
 * <p>If the repository method declares an unsupported return type or an
 * invalid number of parameters, an {@link IllegalArgumentException} is thrown.</p>
 */
@ApplicationScoped
@Typed(CoreDeleteOperation.class)
public class CoreDeleteOperation implements DeleteOperation {

    protected final LifecycleEventHandler lifecycleEventHandler;

    @Inject
    protected CoreDeleteOperation(LifecycleEventHandler lifecycleEventHandler) {
        this.lifecycleEventHandler = lifecycleEventHandler;
    }

    CoreDeleteOperation() {
        this.lifecycleEventHandler = null;
    }

    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var parameters = context.parameters();
        var returnType = context.method().returnType().orElse(void.class);

        if (parameters.length != 1) {
            throw new IllegalArgumentException(
                    "Delete operation requires one parameter instead of: "
                            + Arrays.asList(parameters));
        }

        if (isNotVoidReturn(returnType)) {
            throw new IllegalArgumentException(
                    "Delete operation doesn't support return type: " + returnType
                            + " it supports void as return");
        }

        var entity = parameters[0];

        if (entity instanceof Restriction<?> restriction) {
            deleteByRestriction(context, restriction);
        } else if (entity instanceof Iterable<?> entities) {
            deleteEntities(context, entities);
        } else if (entity.getClass().isArray()) {
            deleteEntities(context, toList(entity));
        } else {
            deleteEntity(context, entity);
        }

        return null;
    }

    private void deleteEntity(RepositoryInvocationContext context, Object entity) {
        lifecycleEventHandler.preDelete(entity);
        context.template().delete(entity);
        lifecycleEventHandler.postDelete(entity);
    }

    private void deleteEntities(RepositoryInvocationContext context, Iterable<?> entities) {

        List<Object> materializedEntities = new ArrayList<>();
        entities.forEach(materializedEntities::add);
        materializedEntities.forEach(lifecycleEventHandler::preDelete);
        context.template().delete(materializedEntities);
        materializedEntities.forEach(lifecycleEventHandler::postDelete);
    }

    private List<Object> toList(Object array) {
        int length = Array.getLength(array);
        List<Object> entities = new ArrayList<>(length);

        for (int index = 0; index < length; index++) {
            entities.add(Array.get(array, index));
        }

        return entities;
    }

    /**
     * Executes a delete operation based on a {@link Restriction}.
     *
     * <p>This method is not supported by the core engine and must be overridden
     * by provider-specific implementations that support restriction-based
     * deletion.</p>
     *
     * <p>A provider implementation must fire one pre-delete and one post-delete
     * lifecycle event for each record deleted by the restriction. A post-delete
     * event must only be fired after the corresponding deletion succeeds.</p>
     *
     * @param context the repository invocation context
     * @param restriction the deletion restriction
     * @throws UnsupportedOperationException if not overridden by a provider
     */
    protected void deleteByRestriction(RepositoryInvocationContext context, Restriction<?> restriction) {
        throw new UnsupportedOperationException("Delete by restriction is not supported by default");
    }

    private boolean isNotVoidReturn(Class<?> returnType) {
        return !returnType.equals(void.class);
    }
}
