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
package org.eclipse.jnosql.mapping.graph;

import java.util.Map;

/**
 * Represents an Edge (Relationship) in a Graph database.
 * An Edge connects two vertices and may contain additional properties.
 * In Neo4j, edges are called "relationships," whereas in Apache TinkerPop, edges are directional with
 * "out" (source) and "in" (target) vertices.
 * This interface abstracts the common behavior for edges in both Neo4j and TinkerPop.
 * @param <S> the source entity type (outgoing vertex in TinkerPop, start node in Neo4j)
 * @param <T> the target entity type (incoming vertex in TinkerPop, end node in Neo4j)
 */
public interface Edge<S, T> {

    /**
     * Gets the unique identifier of the edge.
     *
     * @return the edge ID, which can be of type Long (Neo4j), String, UUID, or other types in TinkerPop.
     */
    Object id();

    /**
     * Gets the unique identifier of the edge and converts it to the specified type.
     *
     * @param <K>  the expected ID type
     * @param type the class of the expected ID type
     * @return the edge ID converted to the specified type
     * @throws NullPointerException if the provided type is null
     * @throws ClassCastException   if the ID cannot be converted to the specified type
     */
    <K> T id(Class<K> type);

    /**
     * Gets the source vertex of the edge.
     *
     * @return the source entity
     */
    S source();

    /**
     * Gets the target vertex of the edge.
     *
     * @return the target entity
     */
    T target();

    /**
     * Gets the label of the edge, representing the type of relationship.
     *
     * @return the edge label
     */
    String label();

    /**
     * Gets the properties associated with the edge.
     * These properties represent additional information about the relationship.
     *
     * @return a map of key-value pairs representing edge properties
     */
    Map<String, Object> properties();
}
