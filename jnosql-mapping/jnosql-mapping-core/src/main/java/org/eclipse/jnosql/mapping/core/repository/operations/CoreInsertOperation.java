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
import org.eclipse.jnosql.mapping.metadata.repository.spi.InsertOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
class CoreInsertOperation implements InsertOperation {

    private final LifecycleEventHandler lifecycleEventHandler;

    @Inject
    CoreInsertOperation(LifecycleEventHandler lifecycleEventHandler) {
        this.lifecycleEventHandler = lifecycleEventHandler;
    }

    CoreInsertOperation() {
        this.lifecycleEventHandler = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        Object[] parameters = context.parameters();

        if (parameters.length != 1) {
            throw new IllegalArgumentException(
                    "The insert method must have only one parameter instead of: "
                            + parameters.length
                            + " parameters: "
                            + Arrays.toString(parameters));
        }

        Object element = parameters[0];

        if (element != null && element.getClass().isArray()) {
            return (T) insertArray(context, element);
        }

        if (element instanceof Iterable<?> iterable) {
            return (T) insertIterable(context, iterable);
        }

        return (T) insertEntity(context, element);
    }

    private Object insertEntity(
            RepositoryInvocationContext context,
            Object entity) {

        lifecycleEventHandler.preInsert(entity);

        Object insertedEntity = context.template().insert(entity);

        lifecycleEventHandler.postInsert(insertedEntity);

        return insertedEntity;
    }

    private Iterable<?> insertIterable(
            RepositoryInvocationContext context,
            Iterable<?> iterable) {

        List<Object> entities = materialize(iterable);

        entities.forEach(lifecycleEventHandler::preInsert);

        Iterable<?> insertedEntities = context.template().insert(entities);

        insertedEntities.forEach(lifecycleEventHandler::postInsert);

        return insertedEntities;
    }

    private Object insertArray(
            RepositoryInvocationContext context,
            Object array) {

        List<Object> entities = toList(array);

        entities.forEach(lifecycleEventHandler::preInsert);

        List<Object> insertedEntities = new ArrayList<>();
        context.template()
                .insert(entities)
                .forEach(insertedEntity -> {
                    lifecycleEventHandler.postInsert(insertedEntity);
                    insertedEntities.add(insertedEntity);
                });

        Object result = Array.newInstance(
                array.getClass().getComponentType(),
                insertedEntities.size());

        for (int index = 0; index < insertedEntities.size(); index++) {
            Array.set(result, index, insertedEntities.get(index));
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
