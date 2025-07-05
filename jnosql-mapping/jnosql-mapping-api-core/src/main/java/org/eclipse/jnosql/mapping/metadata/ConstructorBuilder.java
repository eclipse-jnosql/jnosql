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


import jakarta.nosql.NoSQLException;
import org.eclipse.jnosql.communication.ServiceProviderLoader;

import java.util.List;
import java.util.Objects;

/**
 * The ConstructorBuilder interface provides a way to create an entity from a constructor.
 * It allows you to define the constructor parameters, add values for those parameters, and
 * build the resulting entity.
 *
 * <p>Implementations of this interface should be used to dynamically create instances of
 * entities using constructor-based instantiation.</p>
 */
public interface ConstructorBuilder {

    ConstructorBuilderSupplier CONSTRUCTOR_BUILDER_SUPPLIER = ServiceProviderLoader.loadSingleton(ConstructorBuilderSupplier.class,
            new NoSQLException("There is not implementation for the ConstructorBuilderSupplier"));
    /**
     * Returns the constructor parameters.
     *
     * @return the constructor parameters
     */
    List<ParameterMetaData> parameters();

    /**
     * Adds a value for the next constructor parameter.
     *
     * @param value the value to be added
     */
    void add(Object value);

    /**
     * Adds an empty parameter value.
     */
    void addEmptyParameter();

    /**
     * Builds and returns the entity using the provided constructor parameters.
     * @param <T> the entity type
     * @return the built entity
     */
    <T> T build();

    /**
     *  Creates a new instance of the {@link ConstructorBuilder} interface using the provided
     *  * {@link ConstructorMetadata}.
     * @param constructor the constructor
     * @return the ConstructorBuilder instance
     */
    static ConstructorBuilder of(ConstructorMetadata constructor){
        Objects.requireNonNull(constructor, "constructor is required");
        return CONSTRUCTOR_BUILDER_SUPPLIER.apply(constructor);
    }
}
