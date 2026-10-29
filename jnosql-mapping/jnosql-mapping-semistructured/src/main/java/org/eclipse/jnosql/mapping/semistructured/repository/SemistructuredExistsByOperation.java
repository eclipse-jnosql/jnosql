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
import org.eclipse.jnosql.mapping.metadata.repository.spi.ExistsByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

@ApplicationScoped
public class SemistructuredExistsByOperation implements ExistsByOperation {

    private final SemistructuredQueryBuilder semistructuredQueryBuilder;

    @Inject
    SemistructuredExistsByOperation(SemistructuredQueryBuilder semistructuredQueryBuilder) {
        this.semistructuredQueryBuilder = semistructuredQueryBuilder;
    }

    protected SemistructuredExistsByOperation() {
        this.semistructuredQueryBuilder = null;
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var selectQuery = this.semistructuredQueryBuilder.selectQuery(context);
        var template = (SemiStructuredTemplate) context.template();
        return (T) (Boolean) template.exists(selectQuery);
    }
}
