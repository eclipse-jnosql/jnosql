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
package org.eclipse.jnosql.mapping.core.repository.operations;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.repository.BuiltInMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.CustomRepositoryMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.DefaultMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.core.repository.ObjectMethodOperator;

@ApplicationScoped
public class DefaultInfrastructureOperatorProvider implements InfrastructureOperatorProvider {

    private final BuiltInMethodOperator builtInMethodOperator;

    private final ObjectMethodOperator objectMethodOperator;

    private final CustomRepositoryMethodOperator customRepositoryMethodOperator;

    private final DefaultMethodOperator defaultMethodOperator;

    @Inject
    DefaultInfrastructureOperatorProvider(BuiltInMethodOperator builtInMethodOperator,
                                          ObjectMethodOperator objectMethodOperator,
                                          CustomRepositoryMethodOperator customRepositoryMethodOperator,
                                          DefaultMethodOperator defaultMethodOperator) {
        this.builtInMethodOperator = builtInMethodOperator;
        this.objectMethodOperator = objectMethodOperator;
        this.customRepositoryMethodOperator = customRepositoryMethodOperator;
        this.defaultMethodOperator = defaultMethodOperator;
    }

    protected DefaultInfrastructureOperatorProvider() {
        this.builtInMethodOperator = null;
        this.objectMethodOperator = null;
        this.customRepositoryMethodOperator = null;
        this.defaultMethodOperator = null;
    }


    @Override
    public BuiltInMethodOperator buildInMethodOperator() {
        return builtInMethodOperator;
    }

    @Override
    public ObjectMethodOperator objectMethodOperator() {
        return objectMethodOperator;
    }

    @Override
    public CustomRepositoryMethodOperator customRepositoryMethodOperator() {
        return customRepositoryMethodOperator;
    }

    @Override
    public DefaultMethodOperator defaultMethodOperator() {
        return defaultMethodOperator;
    }
}
