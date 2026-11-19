/*
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
 */
package org.eclipse.jnosql.mapping;

import org.eclipse.jnosql.communication.QueryException;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a precompiled Query statement.
 * Obtain it from a template, bind the named parameters, then read the result. The query syntax belongs to the
 * provider.
 *
 * <pre>{@code
 * Optional<Person> person = template.prepare("FROM Person WHERE name = :name")
 *         .bind("name", "Ada")
 *         .singleResult();
 * }</pre>
 */
public interface PreparedStatement {

    /**
     * Binds an argument to a named parameter.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return this instance with the parameter bound
     * @throws NullPointerException when there is a null parameter
     */
    PreparedStatement bind(String name, Object value);

    /**
     * Executes a query and returns the result as a {@link Stream}.
     *
     * @param <T> the type of elements in the stream
     * @return The result stream; if no results are found, it returns an empty stream
     */
    <T> Stream<T> result();

    /**
     * Returns the result as a single element, if available.
     *
     * @param <T> the type of the result
     * @return the single result wrapped in an {@link Optional}; empty if no result is found
     * @throws jakarta.data.exceptions.NonUniqueResultException if the query returns more than one entity
     */
    <T> Optional<T> singleResult();

    /**
     * Returns the number of elements in the result.
     *
     * @return the number of elements
     * @throws QueryException if there are parameters left to bind
     * @throws IllegalArgumentException if the operation is not a count operation
     */
    long count();

    /**
     * Checks if the operation is a count operation.
     *
     * @return {@code true} if the operation is a count operation, otherwise {@code false}
     */
    boolean isCount();

}