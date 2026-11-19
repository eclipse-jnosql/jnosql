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
     * Creates and stores an edge (relationship) between two entities.
     *
     * <pre>{@code
     * Edge<Person, Person> knows = template.edge(ada, "knows", grace, Map.of("since", 2020));
     * Optional<Object> id = knows.id();
     * }</pre>
     *
     * @param <T>        the source entity type
     * @param <E>        the target entity type
     * @param source     the source entity (starting vertex)
     * @param label      the relationship label
     * @param target     the target entity (ending vertex)
     * @param properties the properties of the edge; use an empty map for none
     * @return the stored edge, carrying the identifier assigned by the database
     * @throws NullPointerException if any of the parameters are null
     */
    <T, E> Edge<T, E> edge(T source, String label, E target, Map<String, Object> properties);

    /**
     * Creates and stores an edge whose label is computed by the supplier, for example from an enum.
     *
     * <pre>{@code
     * Edge<Person, Person> edge = template.edge(ada, () -> "knows", grace, Map.of());
     * }</pre>
     *
     * @param <T>        the source entity type
     * @param <E>        the target entity type
     * @param source     the source entity (starting vertex)
     * @param label      a supplier of the relationship label
     * @param target     the target entity (ending vertex)
     * @param properties the properties of the edge; use an empty map for none
     * @return the stored edge, carrying the identifier assigned by the database
     * @throws NullPointerException if any of the parameters are null
     */
    <T, E> Edge<T, E> edge(T source, Supplier<String> label, E target, Map<String, Object> properties);

    /**
     * Stores the given edge, using its source, label, target and properties.
     *
     * @param <T>  the source entity type
     * @param <E>  the target entity type
     * @param edge the edge representing the relationship
     * @return the stored edge, carrying the identifier assigned by the database
     * @throws NullPointerException if the edge is null
     */
    <T, E> Edge<T, E> edge(Edge<T, E> edge);

    /**
     * Deletes the edge, identified by {@link Edge#id()}. The vertices are not removed.
     *
     * <pre>{@code
     * template.delete(knows);
     * }</pre>
     *
     * @param <T>   the source entity type
     * @param <E>   the target entity type
     * @param edge  the edge to be deleted, as returned by a store or find operation
     * @throws NullPointerException     if the edge is null
     * @throws IllegalArgumentException if the edge has no identifier
     */
    <T, E> void delete(Edge<T, E> edge);


    /**
     * Deletes an edge (relationship) by its unique identifier.
     *
     * <pre>{@code
     * template.deleteEdge(edgeId);
     * }</pre>
     *
     * @param <K> the type of the edge identifier
     * @param id  the unique identifier of the edge to delete
     * @throws NullPointerException if the ID is null
     */
    <K> void deleteEdge(K id);

    /**
     * Finds an edge (relationship) by its unique identifier.
     *
     * <pre>{@code
     * Optional<Edge<Person, Person>> edge = template.findEdgeById(edgeId);
     * }</pre>
     *
     * @param <K> the type of the edge identifier
     * @param <T> the source entity type
     * @param <E> the target entity type
     * @param id  the unique identifier of the edge
     * @return the edge, or {@link Optional#empty()} when it is not found
     * @throws NullPointerException if the ID is null
     */
    <K, T, E> Optional<Edge<T, E>> findEdgeById(K id);

}
