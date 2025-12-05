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
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.mapping.metadata.repository.spi.CountAllOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

import java.util.function.Function;

@ApplicationScoped
class SemistructuredCountAllOperation implements CountAllOperation {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var entityMetadata = context.entityMetadata();
        var template = (SemiStructuredTemplate) context.template();
        var method = context.method();
        Long count = template.count(entityMetadata.type());
        var returnType = method.returnType();
        Function<Class<?>, Object> mapper = r -> Value.of(count).get(r);
        return (T) returnType.map(mapper).orElse(count);
    }
}
