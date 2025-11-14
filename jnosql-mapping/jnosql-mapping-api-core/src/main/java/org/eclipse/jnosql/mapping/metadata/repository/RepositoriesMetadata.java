/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.metadata.repository;

import java.util.Optional;

/**
 * Represents a source of metadata for Jakarta Data repository types.
 * This interface provides functionality for retrieving metadata about repository
 * structures, using the repository's class type as a key.
 * Implementations of this interface can be utilized by tools and runtime engines
 * to inspect and analyze repositories within a Jakarta Data application.
 */
public interface RepositoriesMetadata {

    /**
     * Retrieves the metadata associated with a specific repository type.
     *
     * @param type the repository class to retrieve metadata for; must not be null
     * @return an {@code Optional} containing the metadata for the specified repository type,
     *         or empty if no metadata is available for the given type
     * @throws NullPointerException if the provided type is null
     */
    Optional<RepositoryMetadata> get(Class<?> type);
}
