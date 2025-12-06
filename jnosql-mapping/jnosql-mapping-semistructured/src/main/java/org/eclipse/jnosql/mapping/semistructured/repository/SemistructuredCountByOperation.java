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
import jakarta.nosql.Template;
import org.eclipse.jnosql.communication.query.method.SelectMethodProvider;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.metadata.repository.spi.CountByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

@ApplicationScoped
class SemistructuredCountByOperation implements CountByOperation {

    private final SemistructuredOperation semistructuredOperation;

    @Inject
    SemistructuredCountByOperation(SemistructuredOperation semistructuredOperation) {
        this.semistructuredOperation = semistructuredOperation;
    }

    SemistructuredCountByOperation() {
        this.semistructuredOperation = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        SelectQuery selectQuery = this.semistructuredOperation.selectQuery(context);
        var template = (SemiStructuredTemplate) context.template();
        Long count = template.count(selectQuery);
        return (T) count;
    }
}
