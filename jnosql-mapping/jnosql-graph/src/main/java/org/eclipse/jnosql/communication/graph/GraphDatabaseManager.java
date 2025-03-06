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

/**
 * A specialization of {@link DatabaseManager} for handling graph database operations.
 * <p>
 * This interface provides a common abstraction for graph-based NoSQL databases, supporting
 * operations related to creating, retrieving, and deleting relationships (edges) between entities.
 * </p>
 * <p>
 * Implementations of this interface should provide database-specific handling for creating edges,
 * retrieving relationships by ID, and removing them, ensuring consistency across different graph database providers.
 * </p>
 *
 * <p><strong>Graph Database Terminology:</strong></p>
 * <ul>
 *     <li><b>Edge:</b> Represents a relationship between two entities (vertices).</li>
 *     <li><b>Source:</b> The starting entity of a relationship (TinkerPop: <i>out</i>, Neo4j: first node).</li>
 *     <li><b>Target:</b> The ending entity of a relationship (TinkerPop: <i>in</i>, Neo4j: second node).</li>
 *     <li><b>Label:</b> Describes the type of relationship (e.g., "FRIENDS_WITH", "WORKS_AT").</li>
 *     <li><b>Properties:</b> Key-value metadata associated with an edge.</li>
 * </ul>
 *
 * <p><strong>Example Usage:</strong></p>
 * <pre>{@code
 * GraphDatabaseManager manager = ...;
 * CommunicationEntity person = new CommunicationEntity("Person", Map.of("name", "John"));
 * CommunicationEntity book = new CommunicationEntity("Book", Map.of("title", "DDD in Action"));
 *
 * // Create an edge
 * manager.edge(person, "READS", book, Map.of("since", 2020));
 *
 * // Find an edge by ID
 * Optional<Edge<Person, Book>> edge = manager.findEdgeById(123L);
 *
 * // Remove an edge
 * manager.remove(person, "READS", book);
 *
 * // Delete an edge by ID
 * manager.deleteEdge(123L);
 * }</pre>
 *
 * @see DatabaseManager
 */
public interface GraphDatabaseManager extends DatabaseManager {

    /**
     * Creates a relationship (edge) between two {@link CommunicationEntity} nodes.
     * <p>
     * This method establishes a connection between two entities with a specified relationship type (label).
     * Additional metadata can be associated with the relationship via properties.
     * </p>
     *
     * @param source     the source entity (outgoing vertex in TinkerPop, first node in Neo4j).
     * @param label      the type of relationship to create.
     * @param target     the target entity (incoming vertex in TinkerPop, second node in Neo4j).
     * @param properties additional attributes for the relationship.
     * @throws NullPointerException if {@code source}, {@code target}, or {@code label} is null.
     * @return the created edge instance with assigned properties.
     */
    CommunicationEdge edge(CommunicationEntity source, String label, CommunicationEntity target, Map<String, Object> properties);

    /**
     * Removes an existing relationship (edge) between two {@link CommunicationEntity} nodes.
     * <p>
     * This method deletes a relationship based on the provided source and target entities along with the relationship label.
     * The relationship must already exist in the database for it to be removed.
     * </p>
     *
     * @param source     the source entity (must already exist in the database).
     * @param label      the type of relationship to remove.
     * @param target     the target entity (must already exist in the database).
     * @throws NullPointerException if {@code source}, {@code target}, or {@code label} is null.
     */
    void remove(CommunicationEntity source, String label, CommunicationEntity target);

    /**
     * Deletes an edge (relationship) from the graph database by its unique identifier.
     * <p>
     * This method removes a relationship based on its ID, regardless of the source and target entities.
     * </p>
     *
     * @param <K> the type of the edge identifier.
     * @param id  the unique identifier of the edge to delete.
     * @throws NullPointerException if the {@code id} is null.
     */
    <K> void deleteEdge(K id);

    /**
     * Finds an edge (relationship) in the graph database by its unique identifier.
     * <p>
     * Retrieves an edge based on its ID, returning an {@link Optional} containing the edge if found.
     * The edge includes source and target entities, label, and associated properties.
     * </p>
     *
     * @param <K> the type of the edge identifier.
     * @param <T> the source entity type.
     * @param <E> the target entity type.
     * @param id  the unique identifier of the edge.
     * @return an {@link Optional} containing the edge if found, otherwise an empty {@link Optional}.
     * @throws NullPointerException if the {@code id} is null.
     */
    <K, T, E> Optional<Edge<T, E>> findEdgeById(K id);
}

