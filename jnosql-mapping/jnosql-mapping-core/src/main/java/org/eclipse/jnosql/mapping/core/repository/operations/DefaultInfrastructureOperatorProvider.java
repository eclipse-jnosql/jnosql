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
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.repository.BuiltInMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.CustomRepositoryMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.core.repository.ObjectMethodOperator;

@ApplicationScoped
class DefaultInfrastructureOperatorProvider implements InfrastructureOperatorProvider {

    private final BuiltInMethodOperator builtInMethodOperator;

    private final ObjectMethodOperator objectMethodOperator;

    private final CustomRepositoryMethodOperator customRepositoryMethodOperator;

    @Inject
    DefaultInfrastructureOperatorProvider(BuiltInMethodOperator builtInMethodOperator,
                                          ObjectMethodOperator objectMethodOperator,
                                          CustomRepositoryMethodOperator customRepositoryMethodOperator) {
        this.builtInMethodOperator = builtInMethodOperator;
        this.objectMethodOperator = objectMethodOperator;
        this.customRepositoryMethodOperator = customRepositoryMethodOperator;
    }

    DefaultInfrastructureOperatorProvider() {
        this(null, null, null);
    }


    @Override
    public BuiltInMethodOperator defaultMethodOperator() {
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
}
