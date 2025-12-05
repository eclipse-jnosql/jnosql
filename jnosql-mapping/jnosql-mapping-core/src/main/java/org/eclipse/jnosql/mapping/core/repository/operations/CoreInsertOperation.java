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
import org.eclipse.jnosql.mapping.metadata.repository.spi.InsertOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

@ApplicationScoped
class CoreInsertOperation implements InsertOperation {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        Object[] parameters = context.parameters();
        if (parameters.length != 1) {
            throw new IllegalArgumentException("The insert method must have only one parameter instead of: " + parameters.length + " parameters: "
                    + Arrays.toString(parameters));
        }
        var template = context.template();
        Object element = parameters[0];
        if (element != null && element.getClass().isArray()) {

            var entities = new ArrayList<>();
            template.insert(Arrays.asList((Object[]) element)).forEach(entities::add);
            Object entityArray = Array.newInstance(element.getClass().getComponentType(), entities.size());
            System.arraycopy(entities.toArray(), 0, entityArray, 0, entities.size());
            return (T) entityArray;
        } else if (element instanceof Iterable<?> iterable) {
            return (T) template.insert(iterable);
        }
        return (T) template.insert(element);
    }
}
