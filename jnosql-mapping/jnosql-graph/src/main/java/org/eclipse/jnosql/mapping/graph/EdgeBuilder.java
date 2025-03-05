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

import java.util.function.Supplier;

/**
 * A fluent builder for constructing {@link Edge} instances in a graph database.
 * <p>
 * This builder provides a step-by-step process for defining an edge, ensuring that all
 * necessary components are provided before the edge is created.
 * The process involves:
 * </p>
 * <ol>
 *   <li>Defining the source vertex.</li>
 *   <li>Assigning a label to the relationship.</li>
 *   <li>Defining the target vertex.</li>
 *   <li>Optionally adding properties to the edge.</li>
 *   <li>Building the final {@link Edge} instance.</li>
 * </ol>
 *
 * <p>Example usage:</p>
 * <pre>
 *     EdgeBuilder builder = ...;
 *     Person person = new Person();
 *     Book book = new Book();
 *     Edge<Person, Book> edge = builder
 *          .source(person)
 *          .label("READS")
 *          .target(book)
 *          .property("since", 2019)
 *          .property("where", "kindle")
 *          .build();
 * </pre>
 */
public interface EdgeBuilder {

    /**
     * Defines the source vertex of the edge.
     *
     * @param <S>    the type of the source entity
     * @param source the entity that will serve as the source vertex
     * @return the next step in the builder process, allowing the specification of a relationship label
     * @throws NullPointerException if the source is null
     */
    <S> SourceStep<S> source(S source);

    /**
     * Step in the builder where the label of the edge is defined.
     *
     * @param <S> the type of the source entity
     */
    interface SourceStep<S> {

        /**
         * Assigns a label to the edge, representing the type of relationship.
         *
         * @param label the label describing the relationship type
         * @return the next step in the builder process, allowing the specification of the target entity
         * @throws NullPointerException if the label is null
         */
        LabelStep<S> label(String label);

        /**
         * Assigns a label to the edge using a {@link Supplier}, allowing dynamic label assignment.
         *
         * @param label a supplier that provides the relationship label dynamically
         * @return the next step in the builder process, allowing the specification of the target entity
         * @throws NullPointerException if the label supplier is null
         */
        LabelStep<S> label(Supplier<String> label);
    }

    /**
     * Step in the builder where the target vertex of the edge is defined.
     *
     * @param <S> the type of the source entity
     */
    interface LabelStep<S> {

        /**
         * Defines the target vertex of the edge.
         *
         * @param <T>    the type of the target entity
         * @param target the entity that will serve as the target vertex
         * @return the next step in the builder process, allowing the addition of properties or finalizing the edge
         * @throws NullPointerException if the target is null
         */
        <T> TargetStep<S, T> target(T target);
    }

    /**
     * Step in the builder where properties can be added to the edge or the edge can be finalized.
     *
     * @param <S> the type of the source entity
     * @param <T> the type of the target entity
     */
    interface TargetStep<S, T> {

        /**
         * Builds and returns the finalized {@link Edge} instance.
         *
         * @return the constructed edge
         */
        Edge<S, T> build();

        /**
         * Adds a property to the edge.
         *
         * @param key   the key identifying the property
         * @param value the value associated with the key
         * @return the current step in the builder, allowing additional properties to be added or the edge to be finalized
         * @throws NullPointerException if the key is null
         */
        TargetStep<S, T> property(String key, Object value);
    }
}
