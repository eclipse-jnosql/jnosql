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
package org.eclipse.jnosql.mapping.metadata.repository;

import jakarta.data.Sort;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Provides metadata about a single method in a Jakarta Data repository.
 * This interface describes how a repository method is defined, including
 * its query (if present), parameters, sorting rules, return type, and
 * operation type.
 */
public interface RepositoryMethod {

    /**
     * Returns the query expression explicitly associated with this repository method, if any.
     * This value is present when the method is annotated with {@code @Query};
     * otherwise, it may be empty for derived queries.
     *
     * @return an {@link Optional} containing the query text, or empty if this
     * method does not declare one.
     */
    Optional<String> query();

    /**
     * Returns the maximum number of results this method should return, if specified.
     * For example, a method named {@code findTop5ByStatus} would return a value of {@code 5}.
     *
     * @return an {@link OptionalInt} containing the limit value, or empty if none was defined.
     */
    OptionalInt first();

    /**
     * Returns the sorting definitions associated with this method.
     * @return a list of {@link Sort} definitions, which may be empty but never {@code null}.
     */
    List<Sort<?>> sorts();

    /**
     * Returns the name of the repository method as declared in the interface.
     * @return the method name, never {@code null}.
     */
    String name();

    /**
     * Returns the repository operation type, such as {@code SELECT}, {@code UPDATE}, or {@code DELETE}.
     * @return the {@link RepositoryType} representing this method’s behavior.
     */
    RepositoryType type();

    /**
     * Returns metadata about each parameter of this repository method.
     * @return a list of {@link RepositoryParam} descriptors, which may be empty
     * but never {@code null}.
     */
    List<RepositoryParam> params();

    /**
     * Returns the declared return type of this repository method.
     * This represents the raw type of the method’s return value, such as
     * {@code List.class}, {@code Optional.class}, or {@code Entity.class}.
     *
     * @return an {@link Optional} containing the return type, or empty if unknown.
     */
    Optional<Class<?>> returnType();

    /**
     * Returns the element type contained within the return structure, if applicable.
     * For example:
     * <ul>
     *   <li>If the method returns {@code List<Person>}, this will be {@code Person.class}.</li>
     *   <li>If the method returns {@code Optional<Order>}, this will be {@code Order.class}.</li>
     *   <li>If the method returns a single entity, this will match {@link #returnType()}.</li>
     * </ul>
     *
     * @return an {@link Optional} containing the element type, or empty if not applicable.
     */
    Optional<Class<?>> elementType();

    /**
     * The representation of the {@link jakarta.data.repository.Select} annotations at the method.
     * @return the attributes at the select annotation
     */
    List<String> select();
}
