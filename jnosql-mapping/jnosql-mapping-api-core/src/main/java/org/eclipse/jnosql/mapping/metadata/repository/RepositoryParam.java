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


import jakarta.data.constraint.Constraint;

import java.util.Optional;

public interface RepositoryParam {

    /**
     * The representation of {@link jakarta.data.repository.Is#value()}
     * @return the representation of {@link jakarta.data.repository.Is#value()}
     */
    Optional<Class<? extends Constraint>> is();

    /**
     * The name of the parameter, where it can be overwritable by {@link jakarta.data.repository.Param}
     * @return the name of the parameter
     */
    String name();

    /**
     * The representation of {@link jakarta.data.repository.By#value()}
     * @return The representation of {@link jakarta.data.repository.By#value()}
     */
    String by();
}
