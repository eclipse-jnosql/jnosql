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



import java.util.ServiceLoader;
import java.util.function.Function;

/**
 * This interface defines a converter that converts a {@link Class} to an {@link EntityMetadata}.
 * It is used to map Java classes representing entities to their corresponding metadata,
 * which includes information such as their structure, annotations, and relationships.
 *
 * <p>The {@link ClassConverter} extends the {@link Function} interface, allowing you to use it as
 * a function that takes a {@link Class} input and returns an {@link EntityMetadata} output.
 * This conversion is essential for managing and manipulating entities within the context of
 * data storage and persistence frameworks.
 *
 * <p>Implementations of this interface should provide logic to extract relevant information
 * from the provided {@link Class} instance and create a corresponding {@link EntityMetadata}.
 * This metadata can be used by data mapping and storage components to facilitate various
 * operations such as CRUD (Create, Read, Update, Delete) operations and schema management.
 *
 */
public interface ClassConverter extends Function<Class<?>, EntityMetadata> {

    ClassConverter INSTANCE = ServiceLoader.load(ClassConverter.class)
            .findFirst()
            .orElseThrow(() -> new MetadataException("No implementation of ClassConverter found via ServiceLoader"));

    /**
     * Loads and returns an instance of the {@link ClassScanner} implementation using the ServiceLoader mechanism.
     *
     * @return An instance of the loaded {@link ClassScanner} implementation.
     * @throws IllegalStateException If no suitable implementation is found.
     */
    static ClassConverter load() {
        return INSTANCE;
    }
}
