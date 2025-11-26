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

import org.eclipse.jnosql.mapping.metadata.repository.spi.DeleteOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.InsertOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.SaveOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.UpdateOperation;

/**
 * Defines the minimal set of Jakarta Data repository operations that every
 * storage engine must provide. These operations correspond to explicit
 * data-modification methods such as {@code @Insert}, {@code @Update},
 * {@code @Delete}, and {@code @Save}, and represent the baseline capabilities
 * required for repository execution.
 * This interface is intended for engines that support only the fundamental
 * persistence operations. Engines offering derived query support or more
 * advanced semantics should extend {@link RepositoryOperationProvider}.
 */
public interface BaseRepositoryOperationProvider {

    InsertOperation insertOperation();

    UpdateOperation updateOperation();

    DeleteOperation deleteOperation();

    SaveOperation saveOperation();
}
