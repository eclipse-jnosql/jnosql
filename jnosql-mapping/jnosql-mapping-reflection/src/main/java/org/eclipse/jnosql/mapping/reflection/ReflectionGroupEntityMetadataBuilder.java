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

import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.GroupEntityMetadata;
import org.eclipse.jnosql.mapping.metadata.ProjectionMetadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Step builder for creating {@link GroupEntityMetadata} instances outside a CDI container
 * using reflection-based scanning.
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * GroupEntityMetadata metadata = ReflectionGroupEntityMetadataBuilder.builder()
 *         .withScanner(ClassScanner.load())
 *         .withConverter(ClassConverter.load())
 *         .build();
 * }</pre>
 */
public sealed interface ReflectionGroupEntityMetadataBuilder permits ReflectionGroupEntityMetadataBuilder.ScannerStep,
        ReflectionGroupEntityMetadataBuilder.ConverterStep {

    /**
     * Returns a new builder starting from {@link ScannerStep}.
     *
     * @return the first step of the builder
     */
    static ScannerStep builder() {
        return new ScannerStep(null);
    }

    /**
     * First step of the builder. Holds the {@link ClassScanner}.
     */
    record ScannerStep(ClassScanner scanner) implements ReflectionGroupEntityMetadataBuilder {

        /**
         * Returns a new {@code ScannerStep} with the given scanner.
         *
         * @param scanner the class scanner
         * @return a new {@code ScannerStep}
         * @throws NullPointerException if {@code scanner} is null
         */
        public ScannerStep withScanner(ClassScanner scanner) {
            Objects.requireNonNull(scanner, "scanner is required");
            return new ScannerStep(scanner);
        }

        /**
         * Advances to the next step by providing the {@link ClassConverter}.
         *
         * @param converter the class converter
         * @return a {@link ConverterStep}
         * @throws NullPointerException if {@code converter} is null
         */
        public ConverterStep withConverter(ClassConverter converter) {
            Objects.requireNonNull(converter, "converter is required");
            return new ConverterStep(scanner, converter);
        }
    }

    /**
     * Second step of the builder. Holds both {@link ClassScanner} and {@link ClassConverter}
     * and provides {@code build()}.
     */
    record ConverterStep(ClassScanner scanner, ClassConverter converter) implements ReflectionGroupEntityMetadataBuilder {

        private static final Logger LOGGER = Logger.getLogger(ReflectionGroupEntityMetadataBuilder.class.getName());

        /**
         * Builds a {@link GroupEntityMetadata} by performing a reflection-based scan.
         *
         * @return a new {@link GroupEntityMetadata} instance
         */
        public GroupEntityMetadata build() {
            Objects.requireNonNull(scanner, "scanner is required");
            Objects.requireNonNull(converter, "converter is required");

            LOGGER.fine("Starting the scanning process for Entity and Embeddable annotations (manual): ");

            Map<Class<?>, EntityMetadata> entityMetadataByClass = new HashMap<>();
            Map<String, EntityMetadata> entityMetadataByEntityName = new HashMap<>();
            Map<Class<?>, ProjectionMetadata> projectorMetadataByClass = new HashMap<>();
            Function<Class<?>, ProjectionMetadata> projectionConverter = new ProjectionConverter();

            scanner.entities().forEach(entity -> {
                EntityMetadata entityMetadata = converter.apply(entity);
                if (entityMetadata.hasEntityName()) {
                    entityMetadataByEntityName.put(entityMetadata.name(), entityMetadata);
                }
                entityMetadataByClass.put(entity, entityMetadata);
            });

            scanner.embeddables().forEach(embeddable -> {
                EntityMetadata entityMetadata = converter.apply(embeddable);
                entityMetadataByClass.put(embeddable, entityMetadata);
            });

            scanner.projections().forEach(projection -> {
                ProjectionMetadata projectionMetadata = projectionConverter.apply(projection);
                projectorMetadataByClass.put(projection, projectionMetadata);
            });

            LOGGER.fine(() -> "Finishing the scanning with: %d Entity and Embeddable scanned classes and %d Named entities"
                    .formatted(entityMetadataByClass.size(), entityMetadataByEntityName.size()));

            return new ReflectionGroupEntityMetadata(entityMetadataByEntityName, entityMetadataByClass, projectorMetadataByClass);
        }
    }
}
