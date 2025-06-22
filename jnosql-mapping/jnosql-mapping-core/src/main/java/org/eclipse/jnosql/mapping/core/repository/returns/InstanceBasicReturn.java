/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
import org.eclipse.jnosql.mapping.core.repository.RepositoryReturn;

import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class InstanceBasicReturn implements RepositoryReturn {

    private static final Predicate<Class<?>> IS_BASIC_TYPE = type -> type.isPrimitive()
            || type.isEnum()
            || Number.class.isAssignableFrom(type)
            || type.equals(java.util.Date.class)
            || type.equals(java.sql.Date.class)
            || Temporal.class.isAssignableFrom(type);

    @Override
    public boolean isCompatible(Class<?> entity, Class<?> returnType) {
        return !entity.equals(returnType) && IS_BASIC_TYPE.test(returnType);
    }

    @Override
    public <T> Object convert(DynamicReturn<T> dynamic) {
        Optional<T> optional = dynamic.singleResult();
        return optional.orElse(null);
    }

    @Override
    public <T> Object convertPageRequest(DynamicReturn<T> dynamic) {
        Optional<T> optional = dynamic.singleResultPagination();
        return optional.orElse(null);
    }

}
