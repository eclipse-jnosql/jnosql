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
import jakarta.nosql.Template;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.metadata.repository.spi.SaveOperation;

import java.util.Arrays;
import java.util.Optional;

@ApplicationScoped
class DefaultSaveOperation implements SaveOperation {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var parameters = context.parameters();

        if(parameters.length != 1) {
            throw new IllegalArgumentException("The save method must have only one parameter instead of: " + parameters.length + " parameters: "
                    + Arrays.toString(parameters));
        }
        var entity = parameters[0];
        if (entity instanceof Iterable<?> iterable) {

        }
        if (entity.getClass().isArray()) {

        }

        return (T) save(entity, context);
    }
    private Object save(Object entity, RepositoryInvocationContext context) {
        Template template = context.template();
        EntityMetadata entityMetadata = context.entityMetadata();
        var id = entityMetadata.id()
                .orElseThrow(() ->
                        new IllegalArgumentException(" The entity "
                                + entity.getClass().getName()
                                + " does not have an id property"));
        var isAtDatabase = template.find(entity.getClass(), id).isPresent();
        if(isAtDatabase){
            return template.update(entity);
        }else {
            return template.insert(entity);
        }
    }
}
