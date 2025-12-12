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
 * {@link org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderQueryHandler}
 * responsible for executing it.
 *
 * <p>A custom query annotation must declare {@code @ProviderQuery} to indicate
 * that its execution is handled by an external provider. This allows repository
 * methods to express queries or operations that rely on a provider-specific
 * execution model rather than Jakarta Data’s built-in derived or annotated
 * operations.</p>
 *
 * <p>Providers implement their own {@code ProviderQueryHandler} and annotate
 * it with the same {@code @ProviderQuery} value used on the custom query
 * annotation. At runtime, the invocation engine resolves the appropriate
 * handler based solely on this qualifier.</p>
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
 * public class ExampleQueryHandler implements ProviderQueryHandler {
 *     @Override
 *     public <T> T execute(RepositoryInvocationContext context) {
 *         // provider-specific execution mechanism
 *     }
 * }
 * }</pre>
 *
 * <p>The {@link #value()} must match between the custom query annotation and its
 * provider handler. This design allows the mechanism to work consistently for
 * reflection-based metadata and annotation-processor–generated metadata, since
 * the provider link is expressed through the qualifier rather than concrete
 * annotation instances.</p>
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
public @interface ProviderQuery {

    /**
     * Identifies the provider or query mechanism name.
     * This string is used to associate a custom query annotation with the
     * corresponding {@code ProviderQueryHandler}.
     *
     * @return the provider identifier; never null.
     */
    String value();
}
