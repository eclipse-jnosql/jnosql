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
package org.eclipse.jnosql.mapping.reflection.repository;

import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;

import java.util.Objects;
import java.util.function.Function;

class ReflectionRepositorySupplier implements Function<Class<?>, RepositoryMetadata> {

    @Override
    public RepositoryMetadata apply(Class<?> type) {
        Objects.requireNonNull(type, "type is required");
        if(!type.isInterface()) {
            throw new IllegalArgumentException("The type " + type.getName() + " is not an interface");
        }
        return null;
    }
}
