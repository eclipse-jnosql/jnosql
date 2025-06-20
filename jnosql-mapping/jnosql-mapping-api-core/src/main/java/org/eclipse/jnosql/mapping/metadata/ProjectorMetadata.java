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

public interface ProjectorMetadata {

    /**
     * Returns the {@link Class#getName()}} of the entity
     * @return the {@link Class#getName()} of the entity
     */
    String className();

    /**
     * @return a {@code Class} object identifying the declared
     * type of the entity represented by this object
     */
    Class<?> type();

    /**
     * Returns the {@link  ProjectorConstructorMetadata} the representation of a constructor to this record structure.
     * @return The {@link  ProjectorConstructorMetadata}
     */
    ProjectorConstructorMetadata constructor();

}
