/*
 *
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 *
 */
package org.eclipse.jnosql.mapping.graph;



import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * {@link SemiStructuredTemplate} specialization for graph databases.
 * Entities are stored as vertices, and relationships between them as {@link Edge}s.
 *
 * <pre>{@code
 * @Inject
 * GraphTemplate template;
 *
 * Person ada = template.insert(new Person());
 * Person grace = template.insert(new Person());
 * Edge<Person, Person> knows = template.edge(ada, "knows", grace, Map.of("since", 2020));
 * }</pre>
 *
 * @see SemiStructuredTemplate
 * @see Edge
 */
public interface GraphTemplate extends SemiStructuredTemplate {


    /**
     * Creates an edge (relationship) between two entities with additional properties.
     *
     * @param <T>        the source entity type
     * @param <E>        the target entity type
     * @param source     the source entity (starting vertex)
     * @param label      the relationship label
     * @param target     the target entity (ending vertex)
     * @param properties a map of properties to be associated with the edge
     * @return the created edge instance with assigned properties
     * @throws NullPointerException if any of the parameters are null
     */
    <T, E> Edge<T, E> edge(T source, String label, E target, Map<String, Object> properties);

    /**
     * Creates an edge (relationship) between two entities with additional properties and a dynamic label.
     *
     * @param <T>        the source entity type
     * @param <E>        the target entity type
     * @param source     the source entity (starting vertex)
     * @param label      a supplier that dynamically generates the relationship label
     * @param target     the target entity (ending vertex)
     * @param properties a map of properties to be associated with the edge
     * @return the created edge instance with assigned properties
     * @throws NullPointerException if any of the parameters are null
     */
    <T, E> Edge<T, E> edge(T source, Supplier<String> label, E target, Map<String, Object> properties);

    /**
     * Saves an edge (relationship) into the graph database.
     *
     * @param <T>  the source entity type
     * @param <E>  the target entity type
     * @param edge the edge entity representing the relationship
     * @return the saved edge instance with assigned properties or identifiers
     * @throws NullPointerException if the edge is null
     */
    <T, E> Edge<T, E> edge(Edge<T, E> edge);

    /**
     * Deletes a specified edge from the graph.
     *
     * <p>This method removes the given edge, including its properties and relationship, from the graph database.
     * If the edge does not exist, the operation is safely ignored.</p>
     *
     * @param <T>   the source entity type
     * @param <E>   the target entity type
     * @param edge  the edge to be deleted
     * @throws NullPointerException if the edge is null
     */
    <T, E> void delete(Edge<T, E> edge);


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
