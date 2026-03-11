/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.mapping.reflection.repository;

import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.reflection.ProjectionFound;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Step builder for creating {@link RepositoriesMetadata} instances using reflection-based discovery.
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * RepositoriesMetadata repositories = ReflectionRepositoriesMetadataBuilder.builder()
 *         .withObserver(projectionFound -> {
 *             // handle projection found event
 *         })
 *         .build();
 * }</pre>
 */
public sealed interface ReflectionRepositoriesMetadataBuilder permits ReflectionRepositoriesMetadataBuilder.ConsumerStep {

    /**
     * Returns a new builder starting from {@link ConsumerStep}.
     *
     * @return the first step of the builder
     */
    static ConsumerStep builder() {
        return new ConsumerStep(null);
    }

    /**
     * Step of the builder that holds the projection found observer.
     */
    record ConsumerStep(Consumer<ProjectionFound> observer) implements ReflectionRepositoriesMetadataBuilder {

        /**
         * Returns a new {@code ConsumerStep} with the given observer.
         *
         * @param observer the projection found observer
         * @return a new {@code ConsumerStep}
         * @throws NullPointerException if {@code observer} is null
         */
        public ConsumerStep withObserver(Consumer<ProjectionFound> observer) {
            Objects.requireNonNull(observer, "observer is required");
            return new ConsumerStep(observer);
        }

        /**
         * Builds a {@link RepositoriesMetadata} instance using the provided observer.
         * If no observer is provided, a no-op one will be used.
         *
         * @return a new {@link RepositoriesMetadata} instance
         */
        public RepositoriesMetadata build() {
            Consumer<ProjectionFound> actualObserver = observer != null ? observer : p -> {};
            return new ReflectionRepositoriesMetadata(actualObserver);
        }
    }
}
