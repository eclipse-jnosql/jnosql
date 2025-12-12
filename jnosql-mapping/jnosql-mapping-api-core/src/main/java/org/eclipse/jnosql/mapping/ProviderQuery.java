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
 * Qualifier that links a custom repository query annotation with the
 * {@link org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderOperation}
 * responsible for executing it.
 *
 * <p>A custom query annotation must declare {@code @ProviderQuery} to indicate
 * that its execution is delegated to an external provider. The provider then
 * implements a {@code ProviderOperation} and applies the same qualifier value,
 * allowing the execution engine to match repository methods with the correct
 * provider at runtime.</p>
 *
 * <pre>{@code
 * @ProviderQuery("example")
 * @Retention(RUNTIME)
 * @Target(METHOD)
 * public @interface ExampleQuery {
 *     String value();
 * }
 *
 * @ProviderQuery("example")
 * @ApplicationScoped
 * public class ExampleProviderOperation implements ProviderOperation {
 *
 *     @Override
 *     public <T> T execute(RepositoryInvocationContext context) {
 *         // provider-specific execution
 *     }
 * }
 * }</pre>
 *
 * <p>The {@link #value()} must match on both the query annotation and the
 * provider implementation. This mechanism supports both reflection-based
 * discovery and annotation-processor models, since the linkage is expressed
 * through the qualifier rather than annotation instances.</p>
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
