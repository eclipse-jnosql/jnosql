/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 * and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.mapping.vector;

import jakarta.data.Limit;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;

import java.util.List;
import java.util.Map;

/**
 * Defines vector-native operations over {@link CommunicationEntity} instances.
 *
 * <p>
 * Standard persistence operations are inherited from {@link DatabaseManager}.
 * This interface adds operations that are specific to vector databases, such as
 * nearest-neighbor and threshold-based searches.
 * </p>
 *
 * <p>
 * The entity name identifies the vector collection or equivalent database structure.
 * A {@link CommunicationEntity} is expected to contain a single persisted
 * {@link Vector}; all remaining fields represent payload or metadata.
 * </p>
 *
 * <p>
 * The provider is responsible for converting the {@link Vector} to its native
 * representation and for applying the configured similarity or distance metric.
 * </p>
 */
public interface VectorManager extends DatabaseManager {

    /**
     * Executes the supplied vector search query.
     *
     * <p>
     * Results are ordered according to the similarity or distance metric
     * configured by the underlying vector database.
     * </p>
     *
     * <pre>{@code
     * VectorSelectQuery query = ...;
     *
     * List<CommunicationEntity> entities = manager.search(query);
     * }</pre>
     *
     * @param query the vector search query
     * @return the matching communication entities
     * @throws NullPointerException if {@code query} is {@code null}
     * @throws IllegalArgumentException if the vector dimensions or query
     *         parameters are incompatible with the configured vector space
     * @throws UnsupportedOperationException if the vector representation
     *         or requested search capability is not supported by the provider
     */
    List<CommunicationEntity> search(VectorSelectQuery query);
}