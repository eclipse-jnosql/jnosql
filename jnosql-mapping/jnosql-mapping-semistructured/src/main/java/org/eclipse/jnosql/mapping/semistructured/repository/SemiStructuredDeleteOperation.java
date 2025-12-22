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

import jakarta.data.restrict.Restriction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.repository.RepositoryMetadataUtils;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreDeleteOperation;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.spi.DeleteOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.query.SemiStructuredParameterBasedQuery;


@ApplicationScoped
@Typed({SemiStructuredDeleteOperation.class, DeleteOperation.class})
public class SemiStructuredDeleteOperation extends CoreDeleteOperation {

    private final SemistructuredQueryBuilder queryBuilder;

    @Inject
    SemiStructuredDeleteOperation(SemistructuredQueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    SemiStructuredDeleteOperation() {
        this.queryBuilder = null;
    }

    @Override
    protected void deleteByRestriction(RepositoryInvocationContext context, Restriction<?> restriction) {

        var template = (SemiStructuredTemplate) context.template();
        var method = context.method();
        EntityMetadata entityMetadata = context.entityMetadata();
        var parameters = context.parameters();
        var paramValueMap = RepositoryMetadataUtils.INSTANCE.getBy(method, parameters);
        var deleteQuery = SemiStructuredParameterBasedQuery.INSTANCE.toDeleteQuery(paramValueMap, entityMetadata);
        var updateDeleteQuery = queryBuilder.includeInheritance(deleteQuery, entityMetadata);
        template.delete(updateDeleteQuery);
    }
}
