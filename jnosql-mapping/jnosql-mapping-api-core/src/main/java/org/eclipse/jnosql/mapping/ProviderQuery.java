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
package org.eclipse.jnosql.mapping;


import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Qualifier that marks a repository-level query annotation as being handled
 * by a specific provider. This annotation allows the execution engine to
 * associate a custom query language with its corresponding
 * {@link org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryOperation}
 * implementation.
 *
 * <p>It is applied to user-defined query annotations such as:
 * <pre>{@code
 * @ProviderQuery("cql")
 * public @interface Cql { String value(); }
 * }</pre>
 *
 * <p>The value identifies the provider family (e.g., "sql", "cql", "gremlin"),
 * enabling both CDI and the execution engine to resolve the correct operation.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
public @interface ProviderQuery {

    /**
     * Identifies the provider or query language family.
     * Examples: "cql", "sql", "gremlin", "cypher", "arangoql".
     *
     * @return the provider identifier; never null.
     */
    String value();
}
