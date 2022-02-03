/*
 *  Copyright (c) 2017 Otávio Santana and others
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
package org.eclipse.jnosql.mapping.document.query;

import jakarta.nosql.mapping.Converters;
import jakarta.nosql.mapping.document.DocumentQueryMapper;
import org.eclipse.jnosql.mapping.reflection.ClassMapping;
import org.eclipse.jnosql.mapping.reflection.ClassMappings;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import static java.util.Objects.requireNonNull;

@ApplicationScoped
class DefaultDocumentQueryMapper implements DocumentQueryMapper {

    @Inject
    private Instance<ClassMappings> mappings;

    @Inject
    private Instance<Converters> converters;

    @Override
    public <T> DocumentMapperFrom selectFrom(Class<T> entityClass) {
        requireNonNull(entityClass, "entity is required");
        ClassMapping mapping = mappings.get().get(entityClass);
        return new DefaultDocumentMapperSelectBuilder(mapping, converters.get());
    }

    @Override
    public <T> DocumentMapperDeleteFrom deleteFrom(Class<T> entityClass) {
        requireNonNull(entityClass, "entity is required");
        ClassMapping mapping = mappings.get().get(entityClass);
        return new DefaultDocumentMapperDeleteBuilder(mapping, converters.get());
    }
}
