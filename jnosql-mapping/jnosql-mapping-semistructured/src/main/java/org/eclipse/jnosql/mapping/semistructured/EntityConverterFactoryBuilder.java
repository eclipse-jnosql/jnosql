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
package org.eclipse.jnosql.mapping.semistructured;

import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;

import java.util.Objects;

/**
 * Step builder for creating {@link EntityConverterFactory} instances outside a
 * CDI container.
 *
 * <p>
 * Usage example:
 * 
 * <pre>{@code
 * EntityConverterFactory factory = EntityConverterFactoryBuilder.builder()
 *         .withEntities(entitiesMetadata)
 *         .withConverters(converters)
 *         .build();
 * }</pre>
 *
 * <p>
 * All steps are immutable records. Each {@code withXxx()} call returns a new
 * record
 * with the updated value, leaving the original step unchanged.
 * </p>
 *
 * <p>
 * The {@code build()} method is available once both {@link EntitiesMetadata}
 * and
 * {@link Converters} have been provided (from {@link ConvertersStep} onward).
 * The builder
 * internally creates the required {@link ProjectorConverter} and
 * {@link DefaultEntityConverterFactory}
 * without any container.
 * </p>
 *
 * <p>
 * <strong>Note:</strong> Instances created by this builder are not managed by
 * any CDI
 * container. The caller is responsible for lifecycle management.
 * </p>
 */
public sealed interface EntityConverterFactoryBuilder permits EntityConverterFactoryBuilder.EntitiesStep,
        EntityConverterFactoryBuilder.ConvertersStep {

    /**
     * Returns a new builder starting from {@link EntitiesStep}.
     * Call {@link EntitiesStep#withEntities(EntitiesMetadata)} to provide the
     * required
     * {@link EntitiesMetadata}.
     *
     * @return the first step of the builder
     */
    static EntitiesStep builder() {
        return new EntitiesStep(null);
    }

    /**
     * First step of the builder. Holds the {@link EntitiesMetadata}.
     *
     * @param entities the entities metadata; may be {@code null} only in the
     *                 initial step returned by {@link #builder()}
     */
    record EntitiesStep(EntitiesMetadata entities) implements EntityConverterFactoryBuilder {

        /**
         * Returns a new {@code EntitiesStep} with the given entities metadata.
         *
         * @param entities the entities metadata
         * @return a new {@code EntitiesStep} with the updated metadata
         * @throws NullPointerException if {@code entities} is null
         */
        public EntitiesStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new EntitiesStep(entities);
        }

        /**
         * Advances to the next step by providing the {@link Converters}.
         *
         * @param converters the converters to use
         * @return a {@link ConvertersStep} holding both dependencies
         * @throws NullPointerException if {@code converters} is null
         */
        public ConvertersStep withConverters(Converters converters) {
            Objects.requireNonNull(converters, "converters is required");
            return new ConvertersStep(entities, converters);
        }
    }

    /**
     * Second step of the builder. Holds both {@link EntitiesMetadata} and
     * {@link Converters}
     * and provides {@code build()}.
     *
     * @param entities   the entities metadata; must not be null
     * @param converters the converters; must not be null
     */
    record ConvertersStep(EntitiesMetadata entities, Converters converters) implements EntityConverterFactoryBuilder {

        /**
         * Returns a new {@code ConvertersStep} with the given entities metadata.
         *
         * @param entities the new entities metadata
         * @return a new {@code ConvertersStep} with the updated metadata
         */
        public ConvertersStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new ConvertersStep(entities, converters);
        }

        /**
         * Returns a new {@code ConvertersStep} with the given converters.
         *
         * @param converters the new converters
         * @return a new {@code ConvertersStep} with the updated converters
         * @throws NullPointerException if {@code converters} is null
         */
        public ConvertersStep withConverters(Converters converters) {
            Objects.requireNonNull(converters, "converters is required");
            return new ConvertersStep(entities, converters);
        }

        /**
         * Builds an {@link EntityConverterFactory} using the accumulated dependencies.
         * Internally creates a {@link ProjectorConverter} from {@code entities} and
         * then
         * a {@link DefaultEntityConverterFactory} from all three dependencies.
         *
         * @return a new {@link EntityConverterFactory} instance
         * @throws NullPointerException if {@code entities} or {@code converters} is
         *                              null
         */
        public EntityConverterFactory build() {
            Objects.requireNonNull(entities, "entities is required");
            Objects.requireNonNull(converters, "converters is required");
            ProjectorConverter projectorConverter = new ProjectorConverter(entities);
            return new DefaultEntityConverterFactory(entities, converters, projectorConverter);
        }
    }
}
