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

/**
 * The MapFieldMetadata interface extends the {@link FieldMetadata} interface and provides
 * additional information about a parameter with a map type.
 *
 * <p>This interface is used to represent parameters of map types, where the type may be a map
 * containing keys and values of specific types.</p>
 *
 * @see ParameterMetaData
 */
public interface MapFieldMetadata extends FieldMetadata {
    /**
     * Returns true if the {@link MapFieldMetadata#valueType()} has Entity or Embeddable annotations
     * @return true if the value has Entity or Embeddable annotations
     */
    boolean isEmbeddable();

    /**
     * Returns the {@link Class} representing the type of keys in the map.
     *
     * @return the key type of the map parameter
     */
    Class<?> keyType();

    /**
     * Returns the {@link Class} representing the type of values in the map.
     *
     * @return the value type of the map parameter
     */
    Class<?> valueType();
}