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

import java.util.Map;
import java.util.Optional;

/**
 * Represents a single annotation declared on a repository method.
 * <p>
 * This abstraction allows repository method metadata to be consumed by the
 * execution engine in a uniform way, regardless of whether the metadata was
 * obtained through reflection or through annotation processing.
 * <p>
 * Each annotation is represented by:
 * <ul>
 *   <li>its annotation type ({@link Class}), and</li>
 *   <li>a map of attribute names and resolved values</li>
 * </ul>
 * This decouples the metadata model from {@code java.lang.annotation.Annotation}
 * so the same structure can be used at both compile time and runtime.
 */
public interface RepositoryAnnotation {

    /**
     * Returns the annotation type as a {@link Class} object.
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code jakarta.data.repository.Query.class}</li>
     *   <li>{@code org.example.CQL.class}</li>
     * </ul>
     *
     * @return the annotation type; never {@code null}.
     */
    Class<?> annotation();

    /**
     * Returns a map of attribute names to their resolved values.
     * <p>
     * These values correspond to the annotation attributes as declared on the
     * repository method and may be populated either through reflection or an
     * annotation processor.
     *
     * <p>Example:
     * <pre>
     * {@code
     *     "value" : "SELECT * FROM person WHERE id = :id",
     *     "timeout" : 2000
     * }
     * </pre>
     *
     * @return a non-null map containing the annotation’s attribute values.
     */
    Map<String, Object> attributes();

    /**
     * Indicates whether this annotation originates from a custom provider.
     * <p>
     * An annotation is considered a <em>provider annotation</em> when its
     * declaration is itself annotated with {@link org.eclipse.jnosql.mapping.ProviderQuery}. Such
     * annotations activate a provider-specific {@link org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryOperation}
     * capable of executing the corresponding query language.
     *
     * <p>Examples of provider annotations:</p>
     * <ul>
     *   <li>{@code @Cql}, when {@code @Cql} is annotated with {@code @ProviderQuery("cql")}</li>
     *   <li>{@code @GremlinQuery}, when annotated with {@code @ProviderQuery("gremlin")}</li>
     * </ul>
     *
     * @return {@code true} if the annotation participates in provider-based
     *         query execution; {@code false} otherwise
     */
    boolean isProviderAnnotation();

    /**
     * Returns the provider identifier when this annotation is a provider
     * annotation. This value corresponds to the {@code @ProviderQuery("...")}
     * qualifier on the annotation type.
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>For {@code @Cql}, returns {@code "cql"}</li>
     *   <li>For {@code @GremlinQuery}, returns {@code "gremlin"}</li>
     * </ul>
     *
     * <p>When {@link #isProviderAnnotation()} is {@code false}, this returns
     * {@link java.util.Optional#empty()}.</p>
     *
     * @return the provider identifier or empty if not applicable
     */
    Optional<String> provider();
}
