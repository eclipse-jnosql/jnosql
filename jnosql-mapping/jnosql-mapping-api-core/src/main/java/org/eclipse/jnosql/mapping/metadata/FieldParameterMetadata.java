/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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


import jakarta.nosql.AttributeConverter;
import jakarta.nosql.Column;
import jakarta.nosql.Id;

import java.util.Optional;

/**
 * * The {@code FieldParameterMetadata} class represents a container for holding metadata
 *  associated with both fields and constructor parameters.
 */
public interface FieldParameterMetadata {

    /**
     * Return the type of the field
     *
     * @return the {@link MappingType}
     */
    MappingType mappingType();

    /**
     * Returns the name of the field that can be either the field name
     * or {@link Column#value()}
     *
     * @return the name
     */
    String name();

    /**
     * @return a {@code Class} object identifying the declared
     * type of the entity represented by this object
     */
    Class<?> type();

    /**
     * Returns true is the field is annotated with {@link Id}
     *
     * @return true is annotated with {@link Id}
     */
    boolean isId();

    /**
     * Returns the converter class
     * @param <X> the type of the entity attribute
     * @param <Y> the type of the database column
     * @param <T> the Converter type
     * @return the converter if present
     */
    <X, Y, T extends AttributeConverter<X, Y>> Optional<Class<T>> converter();

    /**
     * Returns the converter instance using the default constructor
     * @param <X> the type of the entity attribute
     * @param <Y> the type of the database column
     * @param <T> the Converter type
     * @return the converter if present
     */
    <X, Y, T extends AttributeConverter<X, Y>> Optional<T> newConverter();
}
