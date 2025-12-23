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
import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.QueryOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.SaveOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.UpdateOperation;

@ApplicationScoped
class SemistructuredRepositoryOperationProvider implements RepositoryOperationProvider {

    @Inject
    private InsertOperation insertOperation;
    @Inject
    private UpdateOperation updateOperation;
    @Inject
    private DeleteOperation deleteOperation;
    @Inject
    private SaveOperation saveOperation;
    @Inject
    private FindByOperation findByOperation;
    @Inject
    private FindAllOperation findAllOperation;
    @Inject
    private CountByOperation countByOperation;
    @Inject
    private CountAllOperation countAllOperation;
    @Inject
    private ExistsByOperation existsByOperation;
    @Inject
    private DeleteByOperation deleteByOperation;
    @Inject
    private ParameterBasedOperation parameterBasedOperation;
    @Inject
    private CursorPaginationOperation cursorPaginationOperation;
    @Inject
    private QueryOperation queryOperation;
    @Inject
    private ProviderOperation providerOperation;

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
        return findByOperation;
    }

    @Override
    public FindAllOperation findAllOperation() {
        return findAllOperation;
    }

    @Override
    public CountByOperation countByOperation() {
        return countByOperation;
    }

    @Override
    public CountAllOperation countAllOperation() {
        return countAllOperation;
    }

    @Override
    public ExistsByOperation existsByOperation() {
        return existsByOperation;
    }

    @Override
    public DeleteByOperation deleteByOperation() {
        return deleteByOperation;
    }

    @Override
    public ParameterBasedOperation parameterBasedOperation() {
        return parameterBasedOperation;
    }

    @Override
    public CursorPaginationOperation cursorPaginationOperation() {
        return cursorPaginationOperation;
    }

    @Override
    public QueryOperation queryOperation() {
        return queryOperation;
    }

    @Override
    public ProviderOperation providerOperation() {
        return providerOperation;
    }
}
