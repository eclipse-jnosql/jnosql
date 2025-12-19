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

import jakarta.data.repository.Query;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.communication.query.data.QueryType;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.spi.QueryOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

@ApplicationScoped
class SemistructuredQueryOperation implements QueryOperation {

    @Override
    public <T> T execute(RepositoryInvocationContext context) {

        var entityMetadata = context.entityMetadata();
        var method = context.method();
        Class<?> type = entityMetadata.type();
        var entity = entityMetadata.name();
        var pageRequest = DynamicReturn.findPageRequest(params);
        var queryValue = method.query().orElseThrow();
        var queryType = QueryType.parse(queryValue);
        var returnType = method.getReturnType();
        return null;
    }
}
