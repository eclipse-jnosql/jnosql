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
package org.eclipse.jnosql.mapping.metadata;

import java.util.List;

/**
 * Strategy interface for building projection instances from query results.
 * <p>
 * A {@code ProjectorBuilder} is responsible for creating instances of projection
 * records (typically annotated with {@link org.eclipse.jnosql.mapping.Projection} using runtime data,
 * such as a tuple or column-value map retrieved from a query.
 * </p>
 *
 * @see ProjectorMetadata
 */
public interface ProjectorBuilder {

    /**
     * Returns the constructor parameters.
     *
     * @return the constructor parameters
     */
    List<ProjectorParameterMetadata> parameters();

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
     * Builds and returns the projector using the provided constructor parameters.
     * @param <T> the projector type
     * @return the built projector
     */
    <T> T build();
}
