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
package org.eclipse.jnosql.mapping.metadata;

import java.util.Collection;

/**
 * The CollectionParameterMetaData interface extends the {@link ParameterMetaData} interface and provides
 * additional information about a parameter with a generic type for collections.
 *
 * <p>This interface is used to represent parameters of generic types, where the type may be a collection
 * or array containing elements of a specific type.</p>
 *
 *  @see ParameterMetaData
 */
public interface CollectionParameterMetaData extends ParameterMetaData {

    /**
     * Returns true if the array element type has either Entity or Embeddable annotations.
     *
     * @return true if the element type has Entity or Embeddable annotations
     */
    boolean isEmbeddable();
    /**
     * Returns the {@link Class} representing the type of elements in the collection or array.
     *
     * @return the element type of the generic parameter
     */
    Class<?> elementType();

    /**
     * Returns an instance of the {@link Collection} interface representing the collection type
     * for the parameter.
     *
     * @return an instance of the collection type for the parameter
     */
    Collection<?> collectionInstance();

}
