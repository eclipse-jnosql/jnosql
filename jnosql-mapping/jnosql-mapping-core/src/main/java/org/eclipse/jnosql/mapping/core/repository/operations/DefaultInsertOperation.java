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
import org.eclipse.jnosql.mapping.metadata.repository.spi.InsertOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.util.Arrays;

@ApplicationScoped
class DefaultInsertOperation implements InsertOperation {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        Object[] parameters = context.parameters();
        if(parameters.length != 1) {
            throw new IllegalArgumentException("The insert method must have only one parameter instead of: " + parameters.length + " parameters: "
                    + Arrays.toString(parameters));
        }
        var template = context.template();
        if(parameters[0] instanceof Object[]) {

        }
        else if(parameters[0] instanceof Iterable iterable) {
            return (T) template.insert(iterable);
        }
        return (T) template.insert(parameters[0]);
    }
}
