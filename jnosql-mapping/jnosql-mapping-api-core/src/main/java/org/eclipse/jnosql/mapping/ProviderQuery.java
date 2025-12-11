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
 * Qualifier that links a custom query annotation and its executing
 * {@link org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryOperation} implementation.
 *
 * <p>Vendors use this annotation in two places:</p>
 * <ol>
 *   <li>
 *     On a <strong>repository-level query annotation</strong> that defines
 *     the query language, for example:
 *     <pre>{@code
 *     @ProviderQuery("cql")
 *     @Retention(RUNTIME)
 *     @Target(METHOD)
 *     public @interface Cql {
 *         String value();
 *     }
 *     }</pre>
 *   </li>
 *   <li>
 *     On the <strong>operation implementation</strong> that executes methods
 *     using that query language, for example:
 *     <pre>{@code
 *     @ProviderQuery("cql")
 *     @ApplicationScoped
 *     public class CqlRepositoryOperation implements RepositoryOperation {
 *
 *         @Override
 *         public <T> T execute(RepositoryInvocationContext context) {
 *             // execute CQL query here...
 *         }
 *     }
 *     }</pre>
 *   </li>
 * </ol>
 *
 * <p>The {@link #value()} must be the same on both the query annotation and
 * the operation implementation. This allows the execution engine to:
 * </p>
 * <ul>
 *   <li>detect the provider from the repository method’s annotation, and</li>
 *   <li>resolve the matching {@code RepositoryOperation} bean qualified
 *       with the same {@code @ProviderQuery} value.</li>
 * </ul>
 *
 * <p>This contract works for both reflection-based discovery and
 * annotation-processor–generated metadata, since the link is expressed
 * via the annotation type and qualifier value, not via {@code Method}
 * or {@code Annotation} instances.</p>
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
public @interface ProviderQuery {

    /**
     * Identifies the provider or query language family. Examples: "cql", "sql", "gremlin", "cypher", "arangoql".
     *
     * @return the provider identifier; never null.
     */
    String value();
}
