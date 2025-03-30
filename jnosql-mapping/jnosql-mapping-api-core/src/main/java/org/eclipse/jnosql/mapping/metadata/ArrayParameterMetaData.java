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
package org.eclipse.jnosql.mapping.metadata;

import java.util.Collection;

/**
 * The ArrayParameterMetaData interface extends the {@link ParameterMetaData} interface and provides
 * additional information about a parameter with a generic type for arrays.
 *
 * <p>This interface is used to represent parameters of generic types, where the type is an array
 * containing elements of a specific type.</p>
 *
 * @see ParameterMetaData
 */
public interface ArrayParameterMetaData extends ParameterMetaData {

    /**
     * Returns true if the array element type has either Entity or Embeddable annotations.
     *
     * @return true if the element type has Entity or Embeddable annotations
     */
    boolean isEmbeddable();
    /**
     * Returns the {@link Class} representing the type of elements in the array.
     *
     * @return the element type of the array parameter
     */
    Class<?> elementType();

    /**
     * Converts the provided {@link Collection} into an array of the appropriate type.
     *
     * @param collection the collection to be converted into an array
     * @return an array containing the elements of the collection, as an Object
     */
    Object arrayInstance(Collection<?> collection);
}

