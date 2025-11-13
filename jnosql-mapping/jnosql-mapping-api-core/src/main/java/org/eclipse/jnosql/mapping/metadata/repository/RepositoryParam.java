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
 * @since 1.0
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
    Optional<Class<? extends Constraint>> is();

    /**
     * Returns the name of the parameter as it should appear in the query.
     * If the parameter is annotated with {@link jakarta.data.repository.Param}, the value provided by that annotation
     * overrides the default parameter name derived from the method signature.
     *
     * @return the effective name of the repository method parameter, never {@code null}.
     */
    String name();

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
}
