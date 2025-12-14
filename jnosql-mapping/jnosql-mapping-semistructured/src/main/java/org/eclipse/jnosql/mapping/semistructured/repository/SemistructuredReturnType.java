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

import jakarta.data.repository.Select;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.ProjectionMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@ApplicationScoped
class SemistructuredReturnType {

    private EntitiesMetadata entitiesMetadata;

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
                .pagination(DynamicReturn.findPageRequest(args))
                .streamPagination(streamPagination(query, method))
                .singleResultPagination(getSingleResult(query, method))
                .page(getPage(query, method))
                .build();
        return dynamicReturn.execute();
    }

    protected <E> Function<Object, E> mapper(RepositoryInvocationContext context) {
        return value -> {
            RepositoryMethod method = context.method();
            var entityMetadata = context.entityMetadata();
            var returnType = method.elementType().orElseThrow();
            Optional<ProjectionMetadata> projection = this.entitiesMetadata.projection(returnType);
            if (projection.isPresent()) {
                ProjectionMetadata projectionMetadata = projection.orElseThrow();
                return projectorConverter().map(value, projectionMetadata);
            }
            Select[] annotations = method.getAnnotationsByType(Select.class);
            if (annotations.length == 1) {
                String fieldReturn = annotations[0].value();
                Optional<EntityMetadata> valueEntityMetadata = entitiesMetadata().findByClassName(value.getClass().getName());
                return (E) valueEntityMetadata
                        .map(entityMetadata -> value(entityMetadata, fieldReturn, value))
                        .orElse(value);
            }
            return (E) value;
        };
    }
}
