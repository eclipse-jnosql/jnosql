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
package org.eclipse.jnosql.mapping.core.repository;

/**
 * Strategy for converting repository operation results into method return values.
 */
public interface RepositoryReturn {

    /**
     * Determines whether this strategy supports the entity and repository return type.
     *
     * @param entity the entity type
     * @param returnType the repository method return type
     * @return {@code true} when this strategy can handle the return type
     */
    boolean isCompatible(Class<?> entity, Class<?> returnType);

    /**
     * Converts a dynamic repository result.
     *
     * @param dynamicReturn the dynamic return metadata and result suppliers
     * @param <T> the result element type
     * @return the converted result
     */
    <T> Object convert(DynamicReturn<T> dynamicReturn);

    /**
     * Converts a paged dynamic repository result.
     *
     * @param dynamicReturn the dynamic return metadata and result suppliers
     * @param <T> the result element type
     * @return the converted paged result
     */
    <T> Object convertPageRequest(DynamicReturn<T> dynamicReturn);

}
