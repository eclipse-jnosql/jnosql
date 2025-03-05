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
 * <p>
 * In our system, vertices are Java classes annotated with {@code @Entity}, representing nodes in the graph.
 * This ensures structured data modeling while maintaining flexibility in relationships.
 * </p>
 * <p>
 * An Edge connects two {@code @Entity} classes and may contain additional properties.
 * </p>
 * <p>
 * <b>Directionality:</b>
 * <ul>
 *   <li><b>In Neo4j:</b> Relationships are defined from a <b>start node</b> to an <b>end node</b>.</li>
 *   <li><b>In TinkerPop:</b> Edges connect an <b>outgoing vertex</b> ("out") to an <b>incoming vertex</b> ("in").</li>
 * </ul>
 * </p>
 * <p>
 * <b>Terminology Mapping:</b>
 * </p>
 * <table border="1">
 *   <tr>
 *     <th>Concept</th> <th>Neo4j Term</th> <th>TinkerPop Term</th>
 *   </tr>
 *   <tr>
 *     <td>Relationship</td> <td><b>Relationship</b></td> <td><b>Edge</b></td>
 *   </tr>
 *   <tr>
 *     <td>Start Node</td> <td><b>Start Node</b></td> <td><b>Outgoing Vertex ("out")</b></td>
 *   </tr>
 *   <tr>
 *     <td>End Node</td> <td><b>End Node</b></td> <td><b>Incoming Vertex ("in")</b></td>
 *   </tr>
 *   <tr>
 *     <td>Edge Properties</td> <td><b>Properties</b></td> <td><b>Properties</b></td>
 *   </tr>
 * </table>
 *
 * @param <S> the source entity type (outgoing vertex in TinkerPop, start node in Neo4j)
 * @param <T> the target entity type (incoming vertex in TinkerPop, end node in Neo4j)
 */
public interface Edge<S, T> {

    /**
     * Gets the unique identifier of the edge.
     * <p>
     * The ID format depends on the underlying database:
     * </p>
     * <ul>
     *   <li><b>Neo4j:</b> Typically a {@code Long} ID.</li>
     *   <li><b>TinkerPop (Gremlin):</b> May use {@code String}, {@code UUID}, or another format.</li>
     * </ul>
     *
     * @return the edge ID
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
