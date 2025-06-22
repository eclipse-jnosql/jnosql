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

import java.util.Map;

/**
 * This interface represents metadata for grouped entities in the context of a data mapping framework.
 * It provides methods to access mappings and classes related to entity metadata.
 *
 * <p>The {@code GroupEntityMetadata} interface defines methods for retrieving information about
 * loaded entity mappings and classes. It is often used to manage and access metadata information
 * for multiple entities in a data mapping framework.
 */
public interface GroupEntityMetadata {

    /**
     * Returns a mapping of entity names to their corresponding {@link EntityMetadata}.
     *
     * @return A map containing entity names and their corresponding metadata.
     */
    Map<String, EntityMetadata> mappings();

    /**
     * Returns a mapping of Java classes to their corresponding {@link EntityMetadata}.
     *
     * @return A map containing Java classes and their corresponding metadata.
     */
    Map<Class<?>, EntityMetadata> classes();


    /**
     * Returns a mapping of Java classes to their corresponding {@link ProjectionMetadata}.
     *
     * @return A map containing Java classes and their corresponding projector metadata.
     */
    Map<Class<?>, ProjectionMetadata> projections();
}