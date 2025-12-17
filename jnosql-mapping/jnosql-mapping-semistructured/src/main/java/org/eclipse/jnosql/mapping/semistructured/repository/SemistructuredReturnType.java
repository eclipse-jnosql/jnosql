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
package org.eclipse.jnosql.mapping.semistructured.repository;

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.NoSQLPage;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.ProjectionMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.ProjectorConverter;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
class SemistructuredReturnType {

    private final EntitiesMetadata entitiesMetadata;

    private final  ProjectorConverter projectorConverter;

    @Inject
    SemistructuredReturnType(EntitiesMetadata entitiesMetadata, ProjectorConverter projectorConverter) {
        this.entitiesMetadata = entitiesMetadata;
        this.projectorConverter = projectorConverter;
    }

    SemistructuredReturnType() {
        this.entitiesMetadata = null;
        this.projectorConverter = null;
    }

    @SuppressWarnings("unchecked")
    protected Object executeFindByQuery(RepositoryInvocationContext context, SelectQuery query) {

        var method = context.method();
        var template = (SemiStructuredTemplate) context.template();
        var entityMetadata = context.entityMetadata();
        var typeClass = entityMetadata.type();
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .classSource(typeClass)
                .methodName(method.name())
                .returnType(method.returnType().orElseThrow())

                .result(() -> {
                    Stream<Object> select = template.select(query);
                    return select.map(mapper(method));
                })
                .singleResult(() -> {
                    Optional<Object> object = template.singleResult(query);
                    return object.map(mapper(method));
                })
                .pagination(DynamicReturn.findPageRequest(context.parameters()))
                .streamPagination(streamPagination(query, method, template))
                .singleResultPagination(getSingleResult(query, method, template))
                .page(getPage(query, method, template))
                .build();
        return dynamicReturn.execute();
    }

    @SuppressWarnings("unchecked")
    protected <E> Function<Object, E> mapper(RepositoryInvocationContext context) {
        return value -> {
            RepositoryMethod method = context.method();
            var returnType = method.elementType().orElseThrow();
            Optional<ProjectionMetadata> projection = this.entitiesMetadata.projection(returnType);
            if (projection.isPresent()) {
                ProjectionMetadata projectionMetadata = projection.orElseThrow();
                return projectorConverter.map(value, projectionMetadata);
            }
            List<String> select = method.select();
            if (select.size() == 1) {
                String fieldReturn = select.getFirst();
                Optional<EntityMetadata> valueEntityMetadata = entitiesMetadata.findByClassName(value.getClass().getName());
                return (E) valueEntityMetadata
                        .map(entityMetadata -> value(entityMetadata, fieldReturn, value))
                        .orElse(value);
            }
            return (E) value;
        };
    }

    protected <T> Function<PageRequest, Stream<T>> streamPagination(SelectQuery query,
                                                                    RepositoryMethod method,
                                                                    SemiStructuredTemplate template) {
        return p -> template.select(query).map(mapper(method));
    }

    protected <T> Function<PageRequest, Optional<T>> getSingleResult(SelectQuery query,
                                                                     RepositoryMethod method,
                                                                 SemiStructuredTemplate template) {
        return p -> template.singleResult(query).map(mapper(method));
    }

    protected <T>  Function<PageRequest, Page<T>> getPage(SelectQuery query,
                                                          RepositoryMethod method,
                                                          SemiStructuredTemplate template) {
        return p -> {
            Stream<T> entities = template.select(query).map(mapper(method));
            return NoSQLPage.of(entities.toList(), p);
        };
    }


    @SuppressWarnings("unchecked")
    protected <E> Function<Object, E> mapper(RepositoryMethod method) {
        return value -> {
            var returnType = method.elementType().orElse(method.returnType().orElseThrow());
            var projection = this.entitiesMetadata.projection(returnType);
            if (projection.isPresent()) {
                ProjectionMetadata projectionMetadata = projection.orElseThrow();
                return projectorConverter.map(value, projectionMetadata);
            }
            var attributes = method.select();
            if (attributes.size() == 1) {
                String fieldReturn = attributes.getFirst();
                Optional<EntityMetadata> valueEntityMetadata = entitiesMetadata.findByClassName(value.getClass().getName());
                return (E) valueEntityMetadata
                        .map(entityMetadata -> value(entityMetadata, fieldReturn, value))
                        .orElse(value);
            }
            return (E) value;
        };
    }

    private Object value(EntityMetadata entityMetadata, String returnName, Object value) {
        var names = returnName.split("\\.");
        Optional<FieldMetadata> fieldMetadata = entityMetadata.fieldMapping(names[0]);
        if (fieldMetadata.isPresent()) {
            var field = fieldMetadata.orElseThrow();
            var convertedField = field.read(value);
            if (convertedField != null && names.length > 1) {
                var subField = Stream.of(names).skip(1).collect(Collectors.joining("."));
                var subEntityMetadata = entitiesMetadata.findByClassName(convertedField.getClass().getName())
                        .orElseThrow(() -> new IllegalArgumentException("Entity metadata not found for " + convertedField.getClass()));
                return value(subEntityMetadata, subField, convertedField);

            }
            return convertedField == null ? value : convertedField;
        }
        return value;
    }
}
