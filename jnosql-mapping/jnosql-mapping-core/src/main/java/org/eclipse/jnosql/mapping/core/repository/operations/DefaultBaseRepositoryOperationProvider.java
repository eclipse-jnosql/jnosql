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
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.repository.BaseRepositoryOperationProvider;
import org.eclipse.jnosql.mapping.metadata.repository.spi.DeleteOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.InsertOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.SaveOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.UpdateOperation;


@ApplicationScoped
class DefaultBaseRepositoryOperationProvider implements BaseRepositoryOperationProvider {

    @Inject
    private InsertOperation insertOperation;
    @Inject
    private UpdateOperation updateOperation;
    @Inject
    private DeleteOperation deleteOperation;
    @Inject
    private SaveOperation saveOperation;


    @Override
    public InsertOperation insertOperation() {
        return insertOperation;
    }

    @Override
    public UpdateOperation updateOperation() {
        return updateOperation;
    }

    @Override
    public DeleteOperation deleteOperation() {
        return deleteOperation;
    }


    @Override
    public SaveOperation saveOperation() {
        return saveOperation;
    }

    /**
     * Resolves an operation from a CDI Instance, or throws an UnsupportedOperationException
     * with the provided message if no implementation is available.
     *
     * @param instance the CDI Instance
     * @param message  the error message for unsupported operations
     * @param <T>      the operation type
     * @return the resolved operation
     */
    private <T> T resolve(Instance<T> instance, String message) {
        if (instance.isUnsatisfied()) {
            throw new UnsupportedOperationException(message);
        }
        return instance.get();
    }
}
