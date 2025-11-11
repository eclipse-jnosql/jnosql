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

import java.util.List;
import java.util.Optional;

/**
 * Provides metadata about a Jakarta Data repository type.
 * This interface is primarily used by Jakarta Data implementations and
 * tooling (e.g., annotation processors, runtime engines, or schema generators)
 * to describe and inspect repository structures, including their entity type,
 * identifier type, and inherited repository interfaces.
 */
public interface RepositoryMetadata {

    /**
     * Returns the entity type handled by this repository, if any.
     *
     * @return an {@link Optional} containing the entity class, or empty if
     * this repository is not parameterized with an entity type.
     */
    Optional<Class<?>> entity();

    /**
     * Returns the identifier type associated with this repository’s entity, if any.
     *
     * @return an {@link Optional} containing the identifier class, or empty
     * if this repository does not declare an identifier type.
     */
    Optional<Class<?>> id();

    /**
     * Returns metadata about the declared query methods of this repository.
     * <p>
     * Each method is represented by a {@link MethodExecution} instance,
     * describing its signature, parameters, and query derivation strategy.
     *
     * @return a list of {@link MethodExecution} descriptors defined in this repository.
     *         The list may be empty but never {@code null}.
     */
    List<MethodExecution> methods();
}
