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

import jakarta.data.Sort;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryParam;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryType;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

record ReflectionRepositoryMethod(String name,
                                  RepositoryType type,
                                  String queryValue,
                                  Integer firstValue,
                                  Class<?> returnTypeValue,
                                  Class<?> elementTypeValue,
                                  List<RepositoryParam> params,
                                  List<Sort<?>> sorts) implements RepositoryMethod {
    @Override
    public Optional<String> query() {
        return Optional.ofNullable(queryValue);
    }

    @Override
    public OptionalInt first() {
        if (firstValue != null) {
            return OptionalInt.of(firstValue);
        }
        return OptionalInt.empty();
    }

    @Override
    public Optional<Class<?>> returnType() {
        return Optional.ofNullable(returnTypeValue);
    }

    @Override
    public Optional<Class<?>> elementType() {
        return Optional.ofNullable(elementTypeValue);
    }
}
