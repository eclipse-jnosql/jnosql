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
import jakarta.nosql.Template;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.metadata.repository.spi.SaveOperation;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
class CoreSaveOperation implements SaveOperation {

    private final LifecycleEventHandler lifecycleEventHandler;

    @Inject
     CoreSaveOperation(LifecycleEventHandler lifecycleEventHandler) {
        this.lifecycleEventHandler = lifecycleEventHandler;
    }

    CoreSaveOperation() {
        this.lifecycleEventHandler = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var parameters = context.parameters();

        if (parameters.length != 1) {
            throw new IllegalArgumentException(
                    "The save method must have only one parameter instead of: "
                            + parameters.length
                            + " parameters: "
                            + Arrays.toString(parameters));
        }

        Object entity = Objects.requireNonNull(
                parameters[0],
                "The entity to save must not be null");

        if (entity instanceof Iterable<?> iterable) {
            List<Object> savedEntities = new ArrayList<>();

            iterable.forEach(element ->
                    savedEntities.add(save(element, context)));

            return (T) savedEntities;
        }

        if (entity.getClass().isArray()) {
            return (T) saveArray(entity, context);
        }

        return (T) save(entity, context);
    }

    private Object save(
            Object entity,
            RepositoryInvocationContext context) {

        Object safeEntity = Objects.requireNonNull(
                entity,
                "The entity to save must not be null");

        lifecycleEventHandler.preUpsert(safeEntity);

        Object savedEntity = persist(safeEntity, context);

        lifecycleEventHandler.postUpsert(savedEntity);

        return savedEntity;
    }

    private Object persist(
            Object entity,
            RepositoryInvocationContext context) {

        Template template = context.template();
        EntityMetadata entityMetadata = context.entityMetadata();

        var idField = entityMetadata.id()
                .orElseThrow(() -> new IllegalArgumentException(
                        "The entity "
                                + entity.getClass().getName()
                                + " does not have an id property"));

        Object id = idField.read(entity);
        boolean exists = template.find(entity.getClass(), id).isPresent();

        return exists
                ? template.update(entity)
                : template.insert(entity);
    }

    private Object saveArray(
            Object array,
            RepositoryInvocationContext context) {

        int length = Array.getLength(array);
        List<Object> savedEntities = new ArrayList<>(length);

        for (int index = 0; index < length; index++) {
            Object entity = Array.get(array, index);
            savedEntities.add(save(entity, context));
        }

        Object result = Array.newInstance(
                array.getClass().getComponentType(),
                savedEntities.size());

        for (int index = 0; index < savedEntities.size(); index++) {
            Array.set(result, index, savedEntities.get(index));
        }

        return result;
    }
}
