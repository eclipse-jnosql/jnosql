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
import org.eclipse.jnosql.mapping.core.repository.RepositoryOperationProvider;
import org.eclipse.jnosql.mapping.metadata.repository.spi.CountAllOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.CountByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.CursorPaginationOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.DeleteByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.DeleteOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ExistsByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.FindAllOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.FindByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.InsertOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ParameterBasedOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.QueryOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.SaveOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.UpdateOperation;

/**
 * Default {@link RepositoryOperationProvider} implementation for the core
 * Jakarta Data integration layer. This provider exposes the semantic
 * repository operations supported by the engine and resolves each operation
 * lazily through CDI using {@link jakarta.enterprise.inject.Instance}.
 *
 * Storage implementations or specialized engines may override individual
 * operations using CDI alternatives. If an operation is not available in the
 * current environment, this provider throws a descriptive
 * {@link UnsupportedOperationException} to indicate that the corresponding
 * repository method type is not supported.
 */
@ApplicationScoped
class CoreAPIRepositoryOperationProvider implements RepositoryOperationProvider {

    @Inject
    private Instance<FindByOperation> findByOperation;
    @Inject
    private Instance<FindAllOperation> findAllOperation;
    @Inject
    private Instance<CountByOperation> countByOperation;
    @Inject
    private Instance<CountAllOperation> countAllOperation;
    @Inject
    private Instance<ExistsByOperation> existsByOperation;
    @Inject
    private Instance<InsertOperation> insertOperation;
    @Inject
    private Instance<UpdateOperation> updateOperation;
    @Inject
    private Instance<DeleteOperation> deleteOperation;
    @Inject
    private Instance<DeleteByOperation> deleteByOperation;
    @Inject
    private Instance<ParameterBasedOperation> parameterBasedOperation;
    @Inject
    private Instance<CursorPaginationOperation> cursorPaginationOperation;
    @Inject
    private Instance<QueryOperation> queryOperation;
    @Inject
    private Instance<SaveOperation> saveOperation;

    @Override
    public FindByOperation findByOperation() {
        return resolve(findByOperation, "FindByOperation is not supported");
    }

    @Override
    public FindAllOperation findAllOperation() {
        return resolve(findAllOperation, "FindAllOperation is not supported");
    }

    @Override
    public CountByOperation countByOperation() {
        return resolve(countByOperation, "CountByOperation is not supported");
    }

    @Override
    public CountAllOperation countAllOperation() {
        return resolve(countAllOperation, "CountAllOperation is not supported");
    }

    @Override
    public ExistsByOperation existsByOperation() {
        return resolve(existsByOperation, "ExistsByOperation is not supported");
    }

    @Override
    public InsertOperation insertOperation() {
        return resolve(insertOperation, "InsertOperation is not supported");
    }

    @Override
    public UpdateOperation updateOperation() {
        return resolve(updateOperation, "UpdateOperation is not supported");
    }

    @Override
    public DeleteOperation deleteOperation() {
        return resolve(deleteOperation, "DeleteOperation is not supported");
    }

    @Override
    public DeleteByOperation deleteByOperation() {
        return resolve(deleteByOperation, "DeleteByOperation is not supported");
    }

    @Override
    public ParameterBasedOperation parameterBasedOperation() {
        return resolve(parameterBasedOperation, "ParameterBasedOperation (@Find) is not supported");
    }

    @Override
    public CursorPaginationOperation cursorPaginationOperation() {
        return resolve(cursorPaginationOperation, "CursorPaginationOperation is not supported");
    }

    @Override
    public QueryOperation queryOperation() {
        return resolve(queryOperation, "@Query Operation is not supported");
    }

    @Override
    public SaveOperation saveOperation() {
        return resolve(saveOperation, "@Save Operation is not supported");
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
