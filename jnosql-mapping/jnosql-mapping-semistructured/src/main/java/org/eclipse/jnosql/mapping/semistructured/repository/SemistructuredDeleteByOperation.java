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
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.mapping.metadata.repository.spi.DeleteByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

@ApplicationScoped
class SemistructuredDeleteByOperation implements DeleteByOperation {

    private final SemistructuredQueryBuilder semistructuredQueryBuilder;

    @Inject
    SemistructuredDeleteByOperation(SemistructuredQueryBuilder semistructuredQueryBuilder) {
        this.semistructuredQueryBuilder = semistructuredQueryBuilder;
    }

    SemistructuredDeleteByOperation() {
        this.semistructuredQueryBuilder = null;
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var method = context.method();
        var returnType = method.returnType().orElse(void.class);
        if(returnType.equals(void.class) || returnType.equals(Void.class)) {
            DeleteQuery deleteQuery = semistructuredQueryBuilder.deleteQuery(context);
            var template = (SemiStructuredTemplate)context.template();
            template.delete(deleteQuery);
            return (T) Void.class;
        }

        throw new UnsupportedOperationException("The Eclipse JNoSQL Semistructured does not support the method " + method
        + " with return type " + returnType + " delete only works with void");
    }
}
