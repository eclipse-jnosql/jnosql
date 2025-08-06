/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.core.repository.returns;

import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;

import java.util.List;

public class ArrayRepositoryReturn extends AbstractRepositoryReturn {

    public ArrayRepositoryReturn() {
        super(null);
    }

    @Override
    public boolean isCompatible(Class<?> entity, Class<?> returnType) {
        return returnType.isArray();
    }

    @Override

    public <T> Object convert(DynamicReturn<T> dynamicReturn) {
        List<T> entities = dynamicReturn.result().toList();
       return toArray(entities);
    }

    @Override
    public <T> Object convertPageRequest(DynamicReturn<T> dynamicReturn) {
        List<T> entities = dynamicReturn.streamPagination().toList();
        return toArray(entities);
    }

    @SuppressWarnings("unchecked")
    private <T> T[] toArray(List<T> entities) {
        if (entities.isEmpty()) {
            return (T[]) java.lang.reflect.Array.newInstance(Object.class, 0);
        }
        T[] array = (T[]) java.lang.reflect.Array.newInstance(
                entities.getFirst().getClass(), entities.size());
        return entities.toArray(array);
    }
}
