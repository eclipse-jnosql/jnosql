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


import jakarta.data.constraint.Constraint;

import java.util.Optional;

/**
 * Provides metadata about a parameter declared in a repository method.
 * <p>
 * Each repository parameter can be annotated with Jakarta Data annotations such as
 * {@link jakarta.data.repository.Param}, {@link jakarta.data.repository.Is}, or {@link jakarta.data.repository.By} to
 * control how it is interpreted in derived or annotated queries. This interface exposes these details so that Jakarta
 * Data implementations can analyze or bind parameters accordingly.
 *
 */
public interface RepositoryParam {

    /**
     * Returns the constraint type specified by {@link jakarta.data.repository.Is#value()}, if present.
     * The {@link jakarta.data.repository.Is} annotation defines comparison semantics (e.g., {@code Is.Equal},
     * {@code Is.GreaterThan}, {@code Is.Not}) that influence query generation.
     *
     * @return an {@link Optional} containing the constraint type defined by {@link jakarta.data.repository.Is#value()},
     * or empty if no {@link jakarta.data.repository.Is} annotation is present.
     */
    Optional<Class<? extends Constraint<?>>> is();

    /**
     * Returns the name of the parameter as it should appear in the query.
     * @return the effective name of the repository method parameter, never {@code null}.
     */
    String name();

    /**
     * Returns the value specified by {@link jakarta.data.repository.Param#value()} for this parameter.
     * <p>
     * The {@link jakarta.data.repository.Param} annotation allows explicit naming of parameters in repository
     * methods, which can be used to bind method parameters to query parameters.
     * <pre>{@code
     *     @Query("SELECT p FROM Person p WHERE p.address.city = :address.city")
     *     List<Person> findByAddressCity(@Param("address.city") String city);
     *     }
     * </pre>
     * @return the value defined by {@link jakarta.data.repository.Param#value()}, or an empty string if no
     * {@link jakarta.data.repository.Param} annotation is present.
     */
    String param();

    /**
     * Returns the property path specified by {@link jakarta.data.repository.By#value()} for this parameter.
     * <p>
     * The {@link jakarta.data.repository.By} annotation defines the entity attribute path to which the parameter is
     * bound when constructing derived queries, for example:
     * <pre>{@code
     *     List<Person> findByAddressCity(@By("address.city") String city);
     *     }
     * </pre>
     *
     * @return the property path defined by {@link jakarta.data.repository.By#value()}, or an empty string if no
     * {@link jakarta.data.repository.By} annotation is present.
     */
    String by();

    /**
     * Returns the type of the parameter in the repository method.
     * This information typically corresponds to the Java class of the parameter,
     * providing insight into its expected data type and usage.
     *
     * @return the {@link Class} representing the type of the parameter, never null.
     */
    Class<?> type();

    /**
     * Returns the element type contained within the return structure, if applicable.
     * For example:
     * <ul>
     *   <li>If the method returns {@code List<Person>}, this will be {@code Person.class}.</li>
     *   <li>If the method returns {@code Optional<Order>}, this will be {@code Order.class}.</li>
     * </ul>
     *
     * @return an {@link Optional} containing the element type, or empty if not applicable.
     */
    Optional<Class<?>> elementType();
}
