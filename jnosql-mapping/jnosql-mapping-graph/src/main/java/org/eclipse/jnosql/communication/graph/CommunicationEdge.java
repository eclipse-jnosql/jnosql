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

import java.util.Map;

/**
 * Represents an Edge (relationship) in a NoSQL database communication layer.
 * <p>
 * This interface provides a standardized representation of an edge, similar to how
 * {@link CommunicationEntity} represents a vertex in the NoSQL database context.
 * It serves as an abstraction for graph-based databases that support relationships between entities.
 * </p>
 * <p>
 * Implementations of this interface should ensure proper mapping of edges, allowing seamless
 * communication between different graph database backends, such as Neo4j and Apache TinkerPop.
 * </p>
 *
 * <p><strong>Example Usage:</strong></p>
 * <pre>{@code
 * CommunicationEdge edge = ...;
 * Object edgeId = edge.id();
 * CommunicationEntity source = edge.source();
 * CommunicationEntity target = edge.target();
 * String relationship = edge.label();
 * Map<String, Object> properties = edge.properties();
 * }</pre>
 */
public interface CommunicationEdge {

    /**
     * Gets the unique identifier of this edge.
     * <p>
     * The identifier type may vary based on the underlying database.
     * For example, in Neo4j, it might be a {@code Long}, while in Apache TinkerPop,
     * it could be a {@code String} or {@code UUID}.
     * </p>
     *
     * @return the unique identifier of this edge, or {@code null} if the edge is not yet persisted
     */
    Object id();

    /**
     * Gets the source entity (vertex) of this edge.
     * <p>
     * The source entity represents the starting point of the relationship.
     * This corresponds to the <i>outgoing</i> vertex in TinkerPop terminology or
     * the first node in Neo4j relationships.
     * </p>
     *
     * @return the source entity of the edge
     * @throws NullPointerException if the source entity is {@code null}
     */
    CommunicationEntity source();

    /**
     * Gets the target entity (vertex) of this edge.
     * <p>
     * The target entity represents the destination of the relationship.
     * This corresponds to the <i>incoming</i> vertex in TinkerPop terminology or
     * the second node in Neo4j relationships.
     * </p>
     *
     * @return the target entity of the edge
     * @throws NullPointerException if the target entity is {@code null}
     */
    CommunicationEntity target();

    /**
     * Gets the label of this edge, representing the relationship type.
     * <p>
     * The label defines the nature of the relationship between the source and target entities.
     * Common examples include "FRIENDS_WITH", "WORKS_AT", or "READS".
     * </p>
     *
     * @return the label of the edge
     * @throws NullPointerException if the label is {@code null}
     */
    String label();

    /**
     * Gets the properties associated with this edge.
     * <p>
     * Edge properties provide additional metadata about the relationship.
     * Examples of properties include timestamps, weights, or categorical values.
     * </p>
     *
     * <p>Common property examples:</p>
     * <pre>{@code
     * {
     *     "since": 2020,
     *     "weight": 0.85,
     *     "status": "active"
     * }
     * }</pre>
     *
     * @return an immutable map containing key-value pairs of properties
     * @throws NullPointerException if properties are not initialized
     */
    Map<String, Object> properties();
}
