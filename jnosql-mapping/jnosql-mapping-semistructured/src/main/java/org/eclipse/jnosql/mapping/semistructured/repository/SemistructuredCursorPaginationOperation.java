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

import jakarta.data.page.CursoredPage;
import jakarta.data.page.PageRequest;
import jakarta.data.page.impl.CursoredPageRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.RepositoryMetadataUtils;
import org.eclipse.jnosql.mapping.core.repository.SpecialParameters;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.CursorPaginationOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.PreparedStatement;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.query.SemiStructuredParameterBasedQuery;

import java.util.Collections;
import java.util.function.Function;

@ApplicationScoped
class SemistructuredCursorPaginationOperation implements CursorPaginationOperation {

    private final SemistructuredQueryBuilder queryBuilder;

    private final SemistructuredReturnType returnType;

    @Inject
    SemistructuredCursorPaginationOperation(SemistructuredQueryBuilder queryBuilder, SemistructuredReturnType returnType) {
        this.queryBuilder = queryBuilder;
        this.returnType = returnType;
    }

    SemistructuredCursorPaginationOperation() {
        this.queryBuilder = null;
        this.returnType = null;
    }


    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var method = context.method();
        var entityMetadata = context.entityMetadata();
        var template = (SemiStructuredTemplate) context.template();
        if (method.query().isPresent()) {
            return mapper(context, executePatinationToQueryAnnotation(context, entityMetadata, method, template));
        } else if (method.find().isPresent()) {
            return mapper(context, executeFindAnnotation(context, method, entityMetadata, template));
        } else {
            return mapper(context, executeMethodByQuery(context, method, template));
        }
    }

    private CursoredPage<?> executeFindAnnotation(RepositoryInvocationContext context, RepositoryMethod method, EntityMetadata entityMetadata, SemiStructuredTemplate template) {
        var paramValueMap = RepositoryMetadataUtils.INSTANCE.getBy(method, context.parameters());
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(paramValueMap, Collections.emptyList(), entityMetadata);
        var updateDynamicQuery = queryBuilder.updateDynamicQuery(query, context);
        var special = DynamicReturn.findSpecialParameters(context.parameters(), Function.identity());
        var pageRequest = pageRequest(method, special);
        return template.selectCursor(updateDynamicQuery, pageRequest);
    }

    private CursoredPage<?> executeMethodByQuery(RepositoryInvocationContext context, RepositoryMethod method, SemiStructuredTemplate template) {
        SelectQuery query = queryBuilder.updateDynamicQuery(queryBuilder.selectQuery(context), context);
        var special = DynamicReturn.findSpecialParameters(context.parameters(), Function.identity());
        var pageRequest = pageRequest(method, special);
        return template.selectCursor(query, pageRequest);
    }

    private CursoredPage<?> executePatinationToQueryAnnotation(RepositoryInvocationContext context,
                                                     EntityMetadata entityMetadata,
                                                     RepositoryMethod method,
                                                     SemiStructuredTemplate template) {

        var entity = entityMetadata.name();
        var textQuery = method.query().orElseThrow();
        var prepare = (PreparedStatement) template.prepare(textQuery, entity);
        var argsParams = RepositoryMetadataUtils.INSTANCE.getParams(method, context.parameters());
        argsParams.forEach(prepare::bind);
        var query = queryBuilder.updateDynamicQuery(prepare.selectQuery().orElseThrow(), context);
        var special = DynamicReturn.findSpecialParameters(context.parameters(), Function.identity());
        var pageRequest = pageRequest(method, special);
        return template.selectCursor(query, pageRequest);
    }


    @SuppressWarnings("unchecked")
    private <T> T mapper(RepositoryInvocationContext context, CursoredPage<?> cursoredPage) {
        RepositoryMethod method = context.method();
        var mappedResult = cursoredPage.content().stream().map(returnType.mapper(method)).toList();
        var cursorPage = (CursoredPageRecord<?>) cursoredPage;
        return (T) new CursoredPageRecord<>(mappedResult, cursorPage.cursors(),
                -1,
                cursorPage.pageRequest(),
                cursorPage.nextPageRequest(),
                cursorPage.previousPageRequest());
    }

    private static PageRequest pageRequest(RepositoryMethod method, SpecialParameters special) {
        return special.pageRequest()
                .orElseThrow(() -> new IllegalArgumentException("Pageable is required in the method signature" +
                        " as parameter at " + method.name()));
    }

}
