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
package org.eclipse.jnosql.communication.graph;

import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.graph.Edge;

import java.util.Map;
import java.util.Optional;

public interface GraphDatabaseManager extends DatabaseManager {

    /**
     * Creates a relationship (edge) between two {@link CommunicationEntity} nodes.
     *
     * @param source           the source entity.
     * @param target           the target entity.
     * @param label the type of relationship to create.
     * @throws NullPointerException  if {@code source}, {@code target}, or {@code label} is null.
     */
    void edge(CommunicationEntity source, String label, CommunicationEntity target, Map<String, Object> properties);

    /**
     * Removes an existing relationship (edge) between two {@link CommunicationEntity} nodes.
     *
     * @param source           the source entity, which must already exist in the database.
     * @param target           the target entity, which must already exist in the database.
     * @param label the type of relationship to remove.
     * @throws NullPointerException       if {@code source}, {@code target}, or {@code label} is null.
     */
    void remove(CommunicationEntity source, String label, CommunicationEntity target);

    /**
     * Deletes an edge (relationship) from the graph database by its unique identifier.
     *
     * @param <K> the type of the edge identifier
     * @param id  the unique identifier of the edge to delete
     * @throws NullPointerException if the ID is null
     */
    <K> void deleteEdge(K id);

    /**
     * Finds an edge (relationship) in the graph database by its unique identifier.
     *
     * @param <K> the type of the edge identifier
     * @param <T> the source entity type
     * @param <E> the target entity type
     * @param id  the unique identifier of the edge
     * @return an {@link Optional} containing the edge if found, otherwise an empty {@link Optional}
     * @throws NullPointerException if the ID is null
     */
    <K, T, E> Optional<Edge<T, E>> findEdgeById(K id);
}
