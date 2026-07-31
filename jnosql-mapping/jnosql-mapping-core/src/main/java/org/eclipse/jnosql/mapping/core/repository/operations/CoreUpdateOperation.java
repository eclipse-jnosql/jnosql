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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.metadata.repository.spi.UpdateOperation;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
class CoreUpdateOperation implements UpdateOperation {

    private final LifecycleEventHandler lifecycleEventHandler;

    @Inject
    CoreUpdateOperation(LifecycleEventHandler lifecycleEventHandler) {
        this.lifecycleEventHandler = lifecycleEventHandler;
    }

    CoreUpdateOperation() {
        this.lifecycleEventHandler = null;
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        Object[] parameters = context.parameters();

        if (parameters.length != 1) {
            throw new IllegalArgumentException(
                    "The update method must have only one parameter instead of: "
                            + parameters.length
                            + " parameters: "
                            + Arrays.toString(parameters));
        }

        Object element = Objects.requireNonNull(
                parameters[0],
                "The entity to update must not be null");

        if (element.getClass().isArray()) {
            return (T) updateArray(context, element);
        }

        if (element instanceof Iterable<?> iterable) {
            return (T) updateIterable(context, iterable);
        }

        return (T) updateEntity(context, element);
    }

    private Object updateEntity(RepositoryInvocationContext context, Object entity) {

        lifecycleEventHandler.preUpdate(entity);
        Object updatedEntity = context.template().update(entity);
        lifecycleEventHandler.postUpdate(updatedEntity);

        return updatedEntity;
    }

    private Iterable<?> updateIterable(RepositoryInvocationContext context, Iterable<?> iterable) {

        List<Object> entities = materialize(iterable);

        entities.forEach(lifecycleEventHandler::preUpdate);

        List<Object> updatedEntities = new ArrayList<>();
        context.template()
                .update(entities)
                .forEach(updatedEntity -> {
                    lifecycleEventHandler.postUpdate(updatedEntity);
                    updatedEntities.add(updatedEntity);
                });

        return updatedEntities;
    }

    private Object updateArray(RepositoryInvocationContext context, Object array) {

        List<Object> entities = toList(array);

        entities.forEach(lifecycleEventHandler::preUpdate);

        List<Object> updatedEntities = new ArrayList<>();
        context.template()
                .update(entities)
                .forEach(updatedEntity -> {
                    lifecycleEventHandler.postUpdate(updatedEntity);
                    updatedEntities.add(updatedEntity);
                });

        Object result = Array.newInstance(
                array.getClass().getComponentType(),
                updatedEntities.size());

        for (int index = 0; index < updatedEntities.size(); index++) {
            Array.set(result, index, updatedEntities.get(index));
        }

        return result;
    }

    private List<Object> materialize(Iterable<?> iterable) {
        List<Object> entities = new ArrayList<>();
        iterable.forEach(entities::add);
        return entities;
    }

    private List<Object> toList(Object array) {
        int length = Array.getLength(array);
        List<Object> entities = new ArrayList<>(length);

        for (int index = 0; index < length; index++) {
            entities.add(Array.get(array, index));
        }

        return entities;
    }
}
