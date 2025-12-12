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
import jakarta.enterprise.inject.Typed;
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
 * Core implementation of {@link RepositoryOperationProvider} that exposes only
 * the fundamental Jakarta Data operations supported by the Core API:
 * {@code @Insert}, {@code @Update}, {@code @Delete}, and {@code @Save}.
 *
 * <p>This provider forms the baseline execution layer for environments where
 * no storage-specific engine (such as key-value or semi-structured/document)
 * is active. All derived query operations—including {@code findBy}, projections,
 * parameter-based lookups, and cursor pagination—are intentionally unsupported
 * and will result in {@link UnsupportedOperationException}.
 *
 * <p>Storage engines offering richer query capabilities must supply their own
 * specialized {@link RepositoryOperationProvider} implementations to extend or
 * override this behavior.
 */
@ApplicationScoped
@Typed(CoreBaseRepositoryOperationProvider.class)
public class CoreBaseRepositoryOperationProvider implements RepositoryOperationProvider {


    private final InsertOperation insertOperation;
    private final UpdateOperation updateOperation;
    private final DeleteOperation deleteOperation;
    private final SaveOperation saveOperation;

    @Inject
    CoreBaseRepositoryOperationProvider(InsertOperation insertOperation,
                                        UpdateOperation updateOperation,
                                        DeleteOperation deleteOperation,
                                        SaveOperation saveOperation) {
        this.insertOperation = insertOperation;
        this.updateOperation = updateOperation;
        this.deleteOperation = deleteOperation;
        this.saveOperation = saveOperation;
    }

    CoreBaseRepositoryOperationProvider() {
        this(null, null, null, null);
    }

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

    @Override
    public FindByOperation findByOperation() {
        throw new UnsupportedOperationException("The Core API does not support findByOperation");
    }

    @Override
    public FindAllOperation findAllOperation() {
       throw new UnsupportedOperationException("The Core API does not support findAllOperation");
    }

    @Override
    public CountByOperation countByOperation() {
       throw new UnsupportedOperationException("The Core API does not support countByOperation");
    }

    @Override
    public CountAllOperation countAllOperation() {
       throw new UnsupportedOperationException("The Core API does not support countAllOperation");
    }

    @Override
    public ExistsByOperation existsByOperation() {
        throw new UnsupportedOperationException("The Core API does not support existsByOperation");
    }

    @Override
    public DeleteByOperation deleteByOperation() {
        throw new UnsupportedOperationException("The Core API does not support deleteByOperation");
    }

    @Override
    public ParameterBasedOperation parameterBasedOperation() {
        throw new UnsupportedOperationException("The Core API does not support parameterBasedOperation");
    }

    @Override
    public CursorPaginationOperation cursorPaginationOperation() {
        throw new UnsupportedOperationException("The Core API does not support cursorPaginationOperation");
    }

    @Override
    public QueryOperation queryOperation() {
        throw new UnsupportedOperationException("The Core API does not support queryOperation");
    }

}
