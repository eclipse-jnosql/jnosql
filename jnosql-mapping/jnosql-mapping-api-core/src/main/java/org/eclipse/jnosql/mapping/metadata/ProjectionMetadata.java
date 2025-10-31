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


/**
 * Represents the metamodel of a Projection record used in Jakarta Data.
 * <p>
 * This interface provides metadata about a Java {@code record} annotated with {@link jakarta.nosql.Projection},
 * which is used to define the structure of the result returned by a projection query.
 * It allows Jakarta Data providers and annotation processors to introspect the structure,
 * including the mapping between record components and query results.
 * </p>
 */
public interface ProjectionMetadata {

    /**
     * Returns the {@link Class#getName()}} of the entity
     * @return the {@link Class#getName()} of the entity
     */
    String className();

    /**
     * @return a {@code Class} representing the record type
     */
    Class<?> type();

    /**
     * Returns a {@link jakarta.nosql.Projection#from}
     *
     * @return the {@link jakarta.nosql.Projection#from}
     */
    Class<?> from();

    /**
     * Returns the {@link  ProjectionConstructorMetadata} the representation of a constructor to this record structure.
     * @return The {@link  ProjectionConstructorMetadata}
     */
    ProjectionConstructorMetadata constructor();

}
