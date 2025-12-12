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
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.DynamicQueryException;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryAnnotation;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.util.List;

@ApplicationScoped
@Typed(CoreProviderOperation.class)
public class CoreProviderOperation implements ProviderOperation {

    @Inject
    @Any
    private Instance<ProviderOperation> providers;

    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        RepositoryMethod method = context.method();
        List<RepositoryAnnotation> annotations = method.annotations();
        var providerAnnotation = annotations.stream()
                .filter(RepositoryAnnotation::isProviderAnnotation)
                .findFirst()
                .orElseThrow(() -> new DynamicQueryException("No provider found at the method: " + method.name()));

        String provider = providerAnnotation.provider().orElseThrow(() -> new DynamicQueryException("No provider found at the method: " + method.name()));

        Instance<ProviderOperation> repositoryOperation = providers.select(ProviderOperation.class,
                ProviderQueryLiteral.of(provider));

        if (repositoryOperation.isUnsatisfied() || repositoryOperation.isAmbiguous()) {
            throw new DynamicQueryException(
                    "Cannot resolve ProviderOperation for provider '" + provider +
                            "' required by repository method: " + method.name());
        }
        return repositoryOperation.get().execute(context);
    }
}
