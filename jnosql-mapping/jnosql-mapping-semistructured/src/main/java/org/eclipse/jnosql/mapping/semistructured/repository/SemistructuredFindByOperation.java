/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
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
import org.eclipse.jnosql.communication.query.method.SelectMethodProvider;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQueryParser;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.FindByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

@ApplicationScoped
public class SemistructuredFindByOperation implements FindByOperation {

    private static final SelectQueryParser SELECT_PARSER = new SelectQueryParser();

    private final SemistructuredQueryBuilder semistructuredQueryBuilder;

    private final SemistructuredReturnType semistructuredReturnType;

    @Inject
    SemistructuredFindByOperation(SemistructuredQueryBuilder semistructuredQueryBuilder,
                                   SemistructuredReturnType semistructuredReturnType) {
        this.semistructuredQueryBuilder = semistructuredQueryBuilder;
        this.semistructuredReturnType = semistructuredReturnType;
    }

    protected SemistructuredFindByOperation() {
        this.semistructuredQueryBuilder = null;
        this.semistructuredReturnType = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var query = getSelectQuery(context);
        return (T) semistructuredReturnType.executeFindByQuery(context, query);
    }

    private SelectQuery getSelectQuery(RepositoryInvocationContext context) {
        RepositoryMethod method = context.method();
        var entityMetadata = context.entityMetadata();
        var parameters = context.parameters();
        var provider = SelectMethodProvider.INSTANCE;
        var selectQuery = provider.apply(method.name(), entityMetadata.name());
        var observer = semistructuredQueryBuilder.observer(entityMetadata);
        var paramsBinder = semistructuredQueryBuilder.paramsBinder(entityMetadata);
        var queryParams = SELECT_PARSER.apply(selectQuery, observer);
        var query = queryParams.query();
        var params = queryParams.params();
        paramsBinder.bind(params, parameters, method.name());
        return semistructuredQueryBuilder.updateDynamicQuery(query, context);
    }
}
