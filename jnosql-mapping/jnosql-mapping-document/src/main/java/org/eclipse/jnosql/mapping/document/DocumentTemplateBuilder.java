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
package org.eclipse.jnosql.mapping.document;

import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.EntityPostPersist;
import org.eclipse.jnosql.mapping.EntityPrePersist;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.EntityConverterFactory;
import org.eclipse.jnosql.mapping.semistructured.EntityConverterFactoryBuilder;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManagerBuilder;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Step builder for creating {@link DocumentTemplate} instances outside a
 * CDI container.
 *
 * <p>
 * Usage example:
 *
 * <pre>{@code
 * DocumentTemplate template = DocumentTemplateBuilder.builder()
 *         .withConverters(converters)
 *         .withEntities(entitiesMetadata)
 *         .withManager(databaseManager)
 *         .withPrePersist(event -> {})
 *         .withPostPersist(event -> {})
 *         .build();
 * }</pre>
 *
 * <p>
 * All steps are immutable records. Each {@code withXxx()} call returns a new
 * record with the updated value, leaving the original step unchanged.
 * </p>
 *
 * <p>
 * Required dependencies: {@link Converters}, {@link EntitiesMetadata},
 * and {@link DatabaseManager}.
 * Optional dependencies: event consumers for {@link EntityPrePersist}
 * and {@link EntityPostPersist}. If not provided, no-op consumers are used.
 * </p>
 *
 * <p>
 * The {@code build()} method is available from {@link ManagerStep} onward.
 * Once all required dependencies are provided, the builder creates
 * an instance of {@link DocumentTemplate} internally using
 * {@link EventPersistManager} and {@link EntityConverterFactory}.
 * </p>
 *
 * <p>
 * <strong>Note:</strong> Instances created by this builder are not managed by
 * any CDI container. The caller is responsible for lifecycle management.
 * </p>
 */
public sealed interface DocumentTemplateBuilder
        permits DocumentTemplateBuilder.ConvertersStep,
        DocumentTemplateBuilder.EntitiesStep,
        DocumentTemplateBuilder.ManagerStep,
        DocumentTemplateBuilder.PrePersistStep,
        DocumentTemplateBuilder.PostPersistStep {

    /**
     * Returns a new builder starting from {@link ConvertersStep}.
     * Call {@link ConvertersStep#withConverters(Converters)} to provide the
     * required {@link Converters}.
     *
     * @return the first step of the builder
     */
    static ConvertersStep builder() {
        return new ConvertersStep(null);
    }

    /**
     * First step of the builder. Holds the {@link Converters}.
     *
     * @param converters the converters; may be {@code null} only in the
     *                   initial step returned by {@link #builder()}
     */
    record ConvertersStep(Converters converters) implements DocumentTemplateBuilder {

        /**
         * Returns a new {@code ConvertersStep} with the given converters.
         *
         * @param converters the new converters
         * @return a new {@code ConvertersStep} with the updated converters
         * @throws NullPointerException if {@code converters} is null
         */
        public ConvertersStep withConverters(Converters converters) {
            Objects.requireNonNull(converters, "converters is required");
            return new ConvertersStep(converters);
        }

        /**
         * Advances to the next step by providing the {@link EntitiesMetadata}.
         *
         * @param entities the entities metadata to use
         * @return an {@link EntitiesStep} holding both dependencies
         * @throws NullPointerException if {@code entities} is null
         */
        public EntitiesStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new EntitiesStep(converters, entities);
        }
    }

    /**
     * Second step of the builder. Holds {@link Converters} and {@link EntitiesMetadata}.
     *
     * @param converters the converters; must not be null
     * @param entities   the entities metadata; must not be null
     */
    record EntitiesStep(Converters converters, EntitiesMetadata entities)
            implements DocumentTemplateBuilder {

        /**
         * Returns a new {@code ConvertersStep} with the given converters.
         *
         * @param converters the new converters
         * @return a new {@code ConvertersStep} with the updated converters
         * @throws NullPointerException if {@code converters} is null
         */
        public ConvertersStep withConverters(Converters converters) {
            Objects.requireNonNull(converters, "converters is required");
            return new ConvertersStep(converters);
        }

        /**
         * Returns a new {@code EntitiesStep} with the given entities metadata.
         *
         * @param entities the new entities metadata
         * @return a new {@code EntitiesStep} with the updated metadata
         * @throws NullPointerException if {@code entities} is null
         */
        public EntitiesStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new EntitiesStep(converters, entities);
        }

        /**
         * Advances to the next step by providing the {@link DatabaseManager}.
         *
         * @param manager the database manager to use
         * @return a {@link ManagerStep} holding all three dependencies
         * @throws NullPointerException if {@code manager} is null
         */
        public ManagerStep withManager(DatabaseManager manager) {
            Objects.requireNonNull(manager, "manager is required");
            return new ManagerStep(converters, entities, manager);
        }
    }

    /**
     * Third step of the builder. Holds {@link Converters}, {@link EntitiesMetadata},
     * and {@link DatabaseManager}.
     *
     * @param converters the converters; must not be null
     * @param entities   the entities metadata; must not be null
     * @param manager    the database manager; must not be null
     */
    record ManagerStep(Converters converters, EntitiesMetadata entities, DatabaseManager manager)
            implements DocumentTemplateBuilder {

        /**
         * Returns a new {@code ConvertersStep} with the given converters.
         *
         * @param converters the new converters
         * @return a new {@code ConvertersStep} with the updated converters
         * @throws NullPointerException if {@code converters} is null
         */
        public ConvertersStep withConverters(Converters converters) {
            Objects.requireNonNull(converters, "converters is required");
            return new ConvertersStep(converters);
        }

        /**
         * Returns a new {@code EntitiesStep} with the given entities metadata.
         *
         * @param entities the new entities metadata
         * @return a new {@code EntitiesStep} with the updated metadata
         * @throws NullPointerException if {@code entities} is null
         */
        public EntitiesStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new EntitiesStep(converters, entities);
        }

        /**
         * Returns a new {@code ManagerStep} with the given database manager.
         *
         * @param manager the new database manager
         * @return a new {@code ManagerStep} with the updated manager
         * @throws NullPointerException if {@code manager} is null
         */
        public ManagerStep withManager(DatabaseManager manager) {
            Objects.requireNonNull(manager, "manager is required");
            return new ManagerStep(converters, entities, manager);
        }

        /**
         * Advances to the next step by providing an optional pre-persist consumer.
         *
         * @param prePersist the consumer invoked before entity persistence
         * @return a {@link PrePersistStep} holding the pre-persist consumer
         * @throws NullPointerException if {@code prePersist} is null
         */
        public PrePersistStep withPrePersist(Consumer<EntityPrePersist> prePersist) {
            Objects.requireNonNull(prePersist, "prePersist is required");
            return new PrePersistStep(converters, entities, manager, prePersist);
        }

        /**
         * Builds a {@link DocumentTemplate} using the accumulated dependencies
         * with no-op event consumers.
         *
         * @return a new {@link DocumentTemplate} instance
         */
        public DocumentTemplate build() {
            Objects.requireNonNull(converters, "converters is required");
            Objects.requireNonNull(entities, "entities is required");
            Objects.requireNonNull(manager, "manager is required");

            EventPersistManager eventManager = EventPersistManagerBuilder.builder().build();
            EntityConverterFactory factory = EntityConverterFactoryBuilder.builder()
                    .withEntities(entities)
                    .withConverters(converters)
                    .build();

            return new DocumentTemplateProducer.ProducerDocumentTemplate(factory, manager, eventManager, entities,
                    converters);
        }
    }

    /**
     * Fourth step of the builder. Holds all three required dependencies plus
     * the pre-persist consumer.
     *
     * @param converters  the converters; must not be null
     * @param entities    the entities metadata; must not be null
     * @param manager     the database manager; must not be null
     * @param prePersist  the pre-persist consumer; must not be null
     */
    record PrePersistStep(Converters converters, EntitiesMetadata entities, DatabaseManager manager,
                          Consumer<EntityPrePersist> prePersist) implements DocumentTemplateBuilder {

        /**
         * Returns a new {@code ConvertersStep} with the given converters.
         *
         * @param converters the new converters
         * @return a new {@code ConvertersStep} with the updated converters
         * @throws NullPointerException if {@code converters} is null
         */
        public ConvertersStep withConverters(Converters converters) {
            Objects.requireNonNull(converters, "converters is required");
            return new ConvertersStep(converters);
        }

        /**
         * Returns a new {@code EntitiesStep} with the given entities metadata.
         *
         * @param entities the new entities metadata
         * @return a new {@code EntitiesStep} with the updated metadata
         * @throws NullPointerException if {@code entities} is null
         */
        public EntitiesStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new EntitiesStep(converters, entities);
        }

        /**
         * Returns a new {@code ManagerStep} with the given database manager.
         *
         * @param manager the new database manager
         * @return a new {@code ManagerStep} with the updated manager
         * @throws NullPointerException if {@code manager} is null
         */
        public ManagerStep withManager(DatabaseManager manager) {
            Objects.requireNonNull(manager, "manager is required");
            return new ManagerStep(converters, entities, manager);
        }

        /**
         * Returns a new {@code PrePersistStep} with the given pre-persist consumer.
         *
         * @param prePersist the new pre-persist consumer
         * @return a new {@code PrePersistStep} with the updated consumer
         * @throws NullPointerException if {@code prePersist} is null
         */
        public PrePersistStep withPrePersist(Consumer<EntityPrePersist> prePersist) {
            Objects.requireNonNull(prePersist, "prePersist is required");
            return new PrePersistStep(converters, entities, manager, prePersist);
        }

        /**
         * Advances to the final step by providing the post-persist consumer.
         *
         * @param postPersist the consumer invoked after entity persistence
         * @return a {@link PostPersistStep} ready to build
         * @throws NullPointerException if {@code postPersist} is null
         */
        public PostPersistStep withPostPersist(Consumer<EntityPostPersist> postPersist) {
            Objects.requireNonNull(postPersist, "postPersist is required");
            return new PostPersistStep(converters, entities, manager, prePersist, postPersist);
        }

        /**
         * Builds a {@link DocumentTemplate} with the pre-persist consumer
         * and a no-op post-persist consumer.
         *
         * @return a new {@link DocumentTemplate} instance
         */
        public DocumentTemplate build() {
            Objects.requireNonNull(converters, "converters is required");
            Objects.requireNonNull(entities, "entities is required");
            Objects.requireNonNull(manager, "manager is required");
            Objects.requireNonNull(prePersist, "prePersist is required");

            EventPersistManager eventManager = EventPersistManagerBuilder.builder()
                    .withPrePersist(prePersist)
                    .build();
            EntityConverterFactory factory = EntityConverterFactoryBuilder.builder()
                    .withEntities(entities)
                    .withConverters(converters)
                    .build();

            return new DocumentTemplateProducer.ProducerDocumentTemplate(factory, manager, eventManager, entities,
                    converters);
        }
    }

    /**
     * Final step of the builder. Holds all dependencies including both
     * pre-persist and post-persist consumers.
     *
     * @param converters   the converters; must not be null
     * @param entities     the entities metadata; must not be null
     * @param manager      the database manager; must not be null
     * @param prePersist   the pre-persist consumer; must not be null
     * @param postPersist  the post-persist consumer; must not be null
     */
    record PostPersistStep(Converters converters, EntitiesMetadata entities, DatabaseManager manager,
                           Consumer<EntityPrePersist> prePersist, Consumer<EntityPostPersist> postPersist)
            implements DocumentTemplateBuilder {

        /**
         * Returns a new {@code ConvertersStep} with the given converters.
         *
         * @param converters the new converters
         * @return a new {@code ConvertersStep} with the updated converters
         * @throws NullPointerException if {@code converters} is null
         */
        public ConvertersStep withConverters(Converters converters) {
            Objects.requireNonNull(converters, "converters is required");
            return new ConvertersStep(converters);
        }

        /**
         * Returns a new {@code EntitiesStep} with the given entities metadata.
         *
         * @param entities the new entities metadata
         * @return a new {@code EntitiesStep} with the updated metadata
         * @throws NullPointerException if {@code entities} is null
         */
        public EntitiesStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new EntitiesStep(converters, entities);
        }

        /**
         * Returns a new {@code ManagerStep} with the given database manager.
         *
         * @param manager the new database manager
         * @return a new {@code ManagerStep} with the updated manager
         * @throws NullPointerException if {@code manager} is null
         */
        public ManagerStep withManager(DatabaseManager manager) {
            Objects.requireNonNull(manager, "manager is required");
            return new ManagerStep(converters, entities, manager);
        }

        /**
         * Returns a new {@code PrePersistStep} with the given pre-persist consumer.
         *
         * @param prePersist the new pre-persist consumer
         * @return a new {@code PrePersistStep} with the updated consumer
         * @throws NullPointerException if {@code prePersist} is null
         */
        public PrePersistStep withPrePersist(Consumer<EntityPrePersist> prePersist) {
            Objects.requireNonNull(prePersist, "prePersist is required");
            return new PrePersistStep(converters, entities, manager, prePersist);
        }

        /**
         * Returns a new {@code PostPersistStep} with the given post-persist consumer.
         *
         * @param postPersist the new post-persist consumer
         * @return a new {@code PostPersistStep} with the updated consumer
         * @throws NullPointerException if {@code postPersist} is null
         */
        public PostPersistStep withPostPersist(Consumer<EntityPostPersist> postPersist) {
            Objects.requireNonNull(postPersist, "postPersist is required");
            return new PostPersistStep(converters, entities, manager, prePersist, postPersist);
        }

        /**
         * Builds a {@link DocumentTemplate} using all accumulated dependencies
         * and both event consumers.
         *
         * @return a new {@link DocumentTemplate} instance
         */
        public DocumentTemplate build() {
            Objects.requireNonNull(converters, "converters is required");
            Objects.requireNonNull(entities, "entities is required");
            Objects.requireNonNull(manager, "manager is required");
            Objects.requireNonNull(prePersist, "prePersist is required");
            Objects.requireNonNull(postPersist, "postPersist is required");

            EventPersistManager eventManager = EventPersistManagerBuilder.builder()
                    .withPrePersist(prePersist)
                    .withPostPersist(postPersist)
                    .build();
            EntityConverterFactory factory = EntityConverterFactoryBuilder.builder()
                    .withEntities(entities)
                    .withConverters(converters)
                    .build();

            return new DocumentTemplateProducer.ProducerDocumentTemplate(factory, manager, eventManager, entities,
                    converters);
        }
    }
}
