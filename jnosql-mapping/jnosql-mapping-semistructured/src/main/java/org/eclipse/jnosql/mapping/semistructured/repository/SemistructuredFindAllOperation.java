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
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.FindAllOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

@ApplicationScoped
class SemistructuredFindAllOperation implements FindAllOperation {

    private final SemistructuredQueryBuilder semistructuredQueryBuilder;

    @Inject
    SemistructuredFindAllOperation(SemistructuredQueryBuilder semistructuredQueryBuilder) {
        this.semistructuredQueryBuilder = semistructuredQueryBuilder;
    }

    SemistructuredFindAllOperation() {
        this.semistructuredQueryBuilder = null;
    }

    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        EntityMetadata entityMetadata = context.entityMetadata();
        var template = (SemiStructuredTemplate) context.template();
        Class<?> type = entityMetadata.type();
        RepositoryMethod method = context.method();
        var query = SelectQuery.select().from(entityMetadata.name()).build();
    }


}
