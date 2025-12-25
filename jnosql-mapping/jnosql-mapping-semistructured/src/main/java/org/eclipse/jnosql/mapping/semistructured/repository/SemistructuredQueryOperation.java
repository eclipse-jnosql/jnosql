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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.query.data.QueryType;
import org.eclipse.jnosql.mapping.core.repository.DynamicQueryMethodReturn;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.RepositoryMetadataUtils;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.QueryOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

import java.util.logging.Logger;

@ApplicationScoped
class SemistructuredQueryOperation implements QueryOperation {

    private static final Logger LOGGER = Logger.getLogger(SemistructuredQueryOperation.class.getName());


    private final  SemistructuredQueryBuilder queryBuilder;

    private final  SemistructuredReturnType semistructuredReturnType;

    private final EntitiesMetadata entitiesMetadata;

    @Inject
    SemistructuredQueryOperation(SemistructuredQueryBuilder queryBuilder,
                                 SemistructuredReturnType semistructuredReturnType,
                                 EntitiesMetadata entitiesMetadata) {
        this.queryBuilder = queryBuilder;
        this.semistructuredReturnType = semistructuredReturnType;
        this.entitiesMetadata = entitiesMetadata;
    }

    SemistructuredQueryOperation() {
        this.queryBuilder = null;
        this.semistructuredReturnType = null;
        this.entitiesMetadata = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {

        var entityMetadata = context.entityMetadata();
        var method = context.method();
        var params = context.parameters();
        var template = (SemiStructuredTemplate) context.template();
        Class<?> type = entityMetadata.type();
        var entity = getEntity(entityMetadata, method);
        var pageRequest = DynamicReturn.findPageRequest(params);
        var queryValue = method.query().orElseThrow();
        var queryType = QueryType.parse(queryValue);
        var returnType = method.returnType().orElseThrow();
        LOGGER.finest("Query: " + queryValue + " with type: " + queryType + " and return type: " + returnType);
        queryType.checkValidReturn(returnType, queryValue);

        var methodReturn = DynamicQueryMethodReturn.builder()
                .args(params)
                .methodName(method.name())
                .returnType(method.returnType().orElseThrow())
                .querySupplier(() -> queryValue)
                .paramsSupplier(() -> RepositoryMetadataUtils.INSTANCE.getParams(method, params))
                .typeClass(type)
                .pageRequest(pageRequest)
                .mapper(semistructuredReturnType.mapper(method, entityMetadata))
                .prepareConverter(textQuery -> {
                    var prepare = (org.eclipse.jnosql.mapping.semistructured.PreparedStatement) template.prepare(textQuery, entity);
                        prepare.setSelectMapper(query -> queryBuilder.updateDynamicQuery(query, context));
                    return prepare;
                }).build();
        return (T) methodReturn.execute();
    }

    private String getEntity(EntityMetadata entityMetadata, RepositoryMethod method) {
       var elementType = method.elementType();

       return elementType.flatMap(type -> entitiesMetadata.findByClassName(type.getName()))
               .map(EntityMetadata::name).orElse(entityMetadata.name());
    }
}
