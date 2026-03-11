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
package org.eclipse.jnosql.mapping.reflection;

import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.GroupEntityMetadata;

import java.util.Objects;

/**
 * Step builder for creating {@link EntitiesMetadata} instances using reflection-based discovery.
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * GroupEntityMetadata group = ReflectionGroupEntityMetadataBuilder.builder()
 *         .withScanner(scanner)
 *         .withConverter(converter)
 *         .build();
 *
 * EntitiesMetadata entities = ReflectionEntitiesMetadataBuilder.builder()
 *         .withGroup(group)
 *         .build();
 * }</pre>
 */
public sealed interface ReflectionEntitiesMetadataBuilder permits ReflectionEntitiesMetadataBuilder.GroupStep {

    /**
     * Returns a new builder starting from {@link GroupStep}.
     *
     * @return the first step of the builder
     */
    static GroupStep builder() {
        return new GroupStep(null);
    }

    /**
     * Step of the builder that holds the {@link GroupEntityMetadata}.
     */
    record GroupStep(GroupEntityMetadata group) implements ReflectionEntitiesMetadataBuilder {

        /**
         * Returns a new {@code GroupStep} with the given group metadata.
         *
         * @param group the group entity metadata
         * @return a new {@code GroupStep}
         * @throws NullPointerException if {@code group} is null
         */
        public GroupStep withGroup(GroupEntityMetadata group) {
            Objects.requireNonNull(group, "group is required");
            return new GroupStep(group);
        }

        /**
         * Builds an {@link EntitiesMetadata} instance using the provided group metadata.
         *
         * @return a new {@link EntitiesMetadata} instance
         * @throws NullPointerException if {@code group} is null
         */
        public EntitiesMetadata build() {
            Objects.requireNonNull(group, "group is required");
            return new DefaultEntitiesMetadata(group);
        }
    }
}
