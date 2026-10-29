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
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.spi.FindAllOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

@ApplicationScoped
public class SemistructuredFindAllOperation implements FindAllOperation {

    private final SemistructuredQueryBuilder semistructuredQueryBuilder;

    private final SemistructuredReturnType semistructuredReturnType;

    @Inject
    SemistructuredFindAllOperation(SemistructuredQueryBuilder semistructuredQueryBuilder,
                                   SemistructuredReturnType semistructuredReturnType) {
        this.semistructuredQueryBuilder = semistructuredQueryBuilder;
        this.semistructuredReturnType = semistructuredReturnType;
    }

    protected SemistructuredFindAllOperation() {
        this.semistructuredQueryBuilder = null;
        this.semistructuredReturnType = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        EntityMetadata entityMetadata = context.entityMetadata();
        var query = SelectQuery.select().from(entityMetadata.name()).build();

        return (T) semistructuredReturnType.executeFindByQuery(context,
                semistructuredQueryBuilder.applyInheritance(query, context));
    }


}
