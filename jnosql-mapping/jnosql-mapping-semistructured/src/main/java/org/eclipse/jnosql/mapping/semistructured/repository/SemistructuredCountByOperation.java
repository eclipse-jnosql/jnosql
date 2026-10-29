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
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.metadata.repository.spi.CountByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

import java.util.function.Function;

@ApplicationScoped
public class SemistructuredCountByOperation implements CountByOperation {

    private final SemistructuredQueryBuilder semistructuredQueryBuilder;

    @Inject
    SemistructuredCountByOperation(SemistructuredQueryBuilder semistructuredQueryBuilder) {
        this.semistructuredQueryBuilder = semistructuredQueryBuilder;
    }

    protected SemistructuredCountByOperation() {
        this.semistructuredQueryBuilder = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var method = context.method();
        SelectQuery selectQuery = this.semistructuredQueryBuilder.selectQuery(context);
        var template = (SemiStructuredTemplate) context.template();
        Long count = template.count(selectQuery);
        var returnType = method.returnType();
        Function<Class<?>, Object> mapper = r -> Value.of(count).get(r);
        return (T) returnType.map(mapper).orElse(count);
    }
}
