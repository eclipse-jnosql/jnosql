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
package org.eclipse.jnosql.mapping.semistructured;


import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.mapping.metadata.ProjectionMetadata;

import java.util.Objects;

/**
 * A converter that transforms an entity into a projection based on the provided metadata. This class is designed to be
 * used in a Jakarta EE environment, specifically within an application scope.
 **/
@ApplicationScoped
public class ProjectorConverter {


    /**
     * Converts the given entity to a projection based on the provided metadata.
     *
     * @param entity the entity to be converted, must not be null
     * @param metadata the metadata defining the projection, must not be null
     * @param <T> the type of the entity
     * @param <P> the type of the projection
     * @return a projection of type P based on the entity and metadata
     * @throws NullPointerException if either entity or metadata is null
     */
    public <T, P> P map(T entity, ProjectionMetadata metadata) {
        Objects.requireNonNull(entity, "entity is required");
        Objects.requireNonNull(metadata, "metadata is required");
    }

}
