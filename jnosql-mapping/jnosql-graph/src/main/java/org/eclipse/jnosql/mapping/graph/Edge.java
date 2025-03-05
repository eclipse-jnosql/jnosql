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
import java.util.Optional;

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
     * Gets the unique identifier of the edge, if available.
     * <p>
     * The ID may not be present if the edge has not been persisted in the database yet.
     * </p>
     * <p>
     * <b>Graph-Specific Behavior:</b>
     * <ul>
     *   <li><b>Neo4j:</b> Relationship IDs are usually <b>Long</b> values.</li>
     *   <li><b>TinkerPop:</b> The ID type is flexible and may be a <b>String, UUID, or other types</b>.</li>
     * </ul>
     * </p>
     *
     * @return an {@link Optional} containing the edge ID if it exists, otherwise {@link Optional#empty()}
     */
    Optional<Object> id();

    /**
     * Gets the unique identifier of the edge and converts it to the specified type, if available.
     * <p>
     * The ID may not be present if the edge has not been persisted in the database yet.
     * </p>
     *
     * @param <K>  the expected ID type
     * @param type the class of the expected ID type
     * @return an {@link Optional} containing the edge ID converted to the specified type if present, otherwise {@link Optional#empty()}
     * @throws NullPointerException if the provided type is null
     * @throws ClassCastException   if the ID cannot be converted to the specified type
     */
    <K> Optional<K> id(Class<K> type);

    /**
     * Gets the source vertex (start node) of the edge.
     * <p>
     * This represents the entity where the relationship originates.
     * </p>
     * <p>
     * <b>Directionality:</b>
     * <ul>
     *   <li><b>Neo4j:</b> This is the <b>start node</b> of the relationship.</li>
     *   <li><b>TinkerPop:</b> This is the <b>outgoing vertex</b> (often referred to as "out").</li>
     * </ul>
     * </p>
     *
     * @return the source entity (start node or outgoing vertex)
     */
    S source();

    /**
     * Gets the target vertex (end node) of the edge.
     * <p>
     * This represents the entity where the relationship points to.
     * </p>
     * <p>
     * <b>Directionality:</b>
     * <ul>
     *   <li><b>Neo4j:</b> This is the <b>end node</b> of the relationship.</li>
     *   <li><b>TinkerPop:</b> This is the <b>incoming vertex</b> (often referred to as "in").</li>
     * </ul>
     * </p>
     *
     * @return the target entity (end node or incoming vertex)
     */
    T target();

    /**
     * Gets the label of the edge, representing the type of relationship.
     * <p>
     * The label is a key identifier that describes the nature of the relationship
     * between the source and target vertices.
     * </p>
     * <p>
     * <b>Graph-Specific Behavior:</b>
     * <ul>
     *   <li><b>Neo4j:</b> Relationship types are always labeled and must be defined explicitly.</li>
     *   <li><b>TinkerPop:</b> Labels are optional but often used for classification.</li>
     * </ul>
     * </p>
     *
     * @return the edge label (relationship type)
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
