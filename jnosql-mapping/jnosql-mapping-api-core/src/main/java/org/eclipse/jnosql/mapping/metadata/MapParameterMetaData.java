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


import org.eclipse.jnosql.communication.Value;

/**
 * The MapParameterMetaData interface extends the {@link ParameterMetaData} interface and provides
 * additional information about a parameter with a generic type for maps.
 *
 * <p>This interface is used to represent parameters of generic types, where the type may be a map
 * containing keys and values of specific types.</p>
 *
 * @see ParameterMetaData
 */
public interface MapParameterMetaData extends ParameterMetaData {

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

    /**
     * Returns the object from the field type.
     *
     * @param value the value {@link Value}
     * @return the instance from the field type
     */
    Object value(Value value);
}