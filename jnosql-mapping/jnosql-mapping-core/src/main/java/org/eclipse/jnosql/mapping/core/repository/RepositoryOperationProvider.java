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
package org.eclipse.jnosql.mapping.core.repository;


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
 * Provides access to all semantic Jakarta Data repository operations supported
 * by the execution engine. Each method returns a component responsible for
 * executing a specific {@link org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethodType}, whether derived from a
 * method name, declared through an annotation, or resolved by parameter
 * inspection.
 *
 * <p>
 * This provider is used by both the dynamic proxy implementation and any
 * code-generated repository implementations to delegate execution of
 * repository methods in a consistent and extensible way.
 *
 * @since 1.1
 */
public interface RepositoryOperationProvider {

    /**
     * Returns the operator for executing derived {@code findBy} queries.
     */
    FindByOperation findByOperation();

    /**
     * Returns the operator for executing {@code findAll} queries.
     */
    FindAllOperation findAllOperation();

    /**
     * Returns the operator for executing derived {@code countBy} projections.
     */
    CountByOperation countByOperation();

    /**
     * Returns the operator for executing {@code countAll} projections.
     */
    CountAllOperation countAllOperation();

    /**
     * Returns the operator for executing derived {@code existsBy} queries.
     */
    ExistsByOperation existsByOperation();

    /**
     * Returns the operator for executing explicit {@code @Insert} operations.
     */
    InsertOperation insertOperation();

    /**
     * Returns the operator for executing explicit {@code @Update} operations.
     */
    UpdateOperation updateOperation();

    /**
     * Returns the operator for executing explicit {@code @Delete} operations.
     */
    DeleteOperation deleteOperation();

    /**
     * Returns the operator for executing derived {@code deleteBy} queries.
     */
    DeleteByOperation deleteByOperation();

    /**
     * Returns the operator for executing parameter-based queries defined
     * through the {@code @Find} annotation.
     */
    ParameterBasedOperation parameterBasedOperation();

    /**
     * Returns the operator for executing cursor-based paginated queries.
     */
    CursorPaginationOperation cursorPaginationOperation();

    /**
     * Returns the operator for executing explicit {@code @Query} operations.
     */
    QueryOperation queryOperation();

    /**
     * Returns the operator for executing explicit {@code @Save} operations.
     */
    SaveOperation saveOperation();
}
