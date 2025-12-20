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
import org.eclipse.jnosql.mapping.core.repository.ParamValue;
import org.eclipse.jnosql.mapping.core.repository.RepositoryMetadataUtils;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ParameterBasedOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.query.SemiStructuredParameterBasedQuery;

import java.util.Collections;
import java.util.Map;

@ApplicationScoped
class SemistructuredParameterBasedOperation implements ParameterBasedOperation {

    private final SemistructuredQueryBuilder semistructuredQueryBuilder;

    private final SemistructuredReturnType semistructuredReturnType;

    @Inject
    SemistructuredParameterBasedOperation(SemistructuredQueryBuilder semistructuredQueryBuilder,
                                                 SemistructuredReturnType semistructuredReturnType) {
        this.semistructuredQueryBuilder = semistructuredQueryBuilder;
        this.semistructuredReturnType = semistructuredReturnType;
    }

    SemistructuredParameterBasedOperation() {
        this.semistructuredQueryBuilder = null;
        this.semistructuredReturnType = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        RepositoryMethod method = context.method();
        var entityMetadata = context.entityMetadata();
        var parameters = context.parameters();
        Map<String, ParamValue> paramValueMap = RepositoryMetadataUtils.INSTANCE.getBy(method, parameters);
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(paramValueMap, Collections.emptyList(), entityMetadata);
        var updateDynamicQuery = semistructuredQueryBuilder.updateDynamicQuery(query, context);
        return (T) semistructuredReturnType.executeFindByQuery(context, updateDynamicQuery);
    }
}
