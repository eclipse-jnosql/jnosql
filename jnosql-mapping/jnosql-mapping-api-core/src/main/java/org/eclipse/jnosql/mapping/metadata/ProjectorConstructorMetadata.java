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
 * Represents metadata about a constructor used for instantiating a projection record in Jakarta Data.
 * <p>
 * This metadata is primarily used by Jakarta Data providers and annotation processors
 * to determine how to invoke the correct constructor of a projection class during query result mapping.
 * It exposes information about constructor parameters, including their names and types,
 * which may be used to bind query results to record components.
 * </p>
 */
public interface ProjectorConstructorMetadata {

    /**
     * Returns a list of ProjectorParameterMetadata objects representing the parameters of the constructor.
     *
     * @return the constructor parameters
     */
    List<ProjectorParameterMetadata> parameters();
}
