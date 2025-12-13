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
package org.eclipse.jnosql.mapping.core.repository.operations;


import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.mapping.ProviderQuery;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderQueryHandler;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.util.Map;

@ProviderQuery("sample-query-provider")
@ApplicationScoped
public class SampleQueryProviderHandler implements ProviderQueryHandler {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {

        RepositoryMethod method = context.method();
        Object[] parameters = context.parameters();
        var sampleQueryProvider = method.annotations().stream()
                .filter(annotation -> SampleQueryProvider.class.equals(annotation.annotation()))
                .findFirst().orElseThrow();
        Map<String, Object> attributes = sampleQueryProvider.attributes();
        String value = (String) attributes.get("value");

        return (T) (value + " " + parameters[0]);
    }
}
