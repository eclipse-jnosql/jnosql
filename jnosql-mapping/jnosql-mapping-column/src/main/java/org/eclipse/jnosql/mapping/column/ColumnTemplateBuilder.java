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
package org.eclipse.jnosql.mapping.column;

import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.EntityPostPersist;
import org.eclipse.jnosql.mapping.EntityPrePersist;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.EntityConverterFactoryBuilder;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManagerBuilder;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Step builder for creating {@link ColumnTemplate} instances outside a CDI container.
 *
 * <p>
 * Usage example:
 *
 * <pre>{@code
 * ColumnTemplate template = ColumnTemplateBuilder.builder()
 *         .withConverters(converters)
 *         .withEntities(entitiesMetadata)
 *         .withManager(manager)
 *         .build();
 * }</pre>
 *
 * <p>
 * All steps are immutable records. Each {@code withXxx()} call returns a new
 * record with the updated value, leaving the original step unchanged.
 * </p>
 *
 * <p>
 * The {@code build()} method is available from {@link ManagerStep} onward.
 * The two event consumers ({@link EntityPrePersist} and {@link EntityPostPersist})
 * are optional — when not provided they default to no-op.
 * </p>
 *
 * <p>
 * <strong>Note:</strong> Instances created by this builder are not managed by
 * any CDI container. The caller is responsible for lifecycle management.
 * </p>
 */
public sealed interface ColumnTemplateBuilder permits ColumnTemplateBuilder.ConvertersStep,
        ColumnTemplateBuilder.EntitiesStep,
        ColumnTemplateBuilder.ManagerStep,
        ColumnTemplateBuilder.PrePersistStep,
        ColumnTemplateBuilder.PostPersistStep {

    /**
     * Returns a new builder starting from {@link ConvertersStep}.
     *
     * @return the first step of the builder
     */
    static ConvertersStep builder() {
        return new ConvertersStep(null);
    }

    /**
     * First step of the builder. Holds the {@link Converters}.
     *
     * @param converters the converters; may be {@code null} only in the initial step
     *                   returned by {@link #builder()}
     */
    record ConvertersStep(Converters converters) implements ColumnTemplateBuilder {

        /**
         * Returns a new {@code ConvertersStep} with the given converters.
         *
         * @param converters the converters to use
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
         * @param entities the entities metadata
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
    record EntitiesStep(Converters converters, EntitiesMetadata entities) implements ColumnTemplateBuilder {

        /**
         * Returns a new {@code EntitiesStep} with the given converters.
         *
         * @param converters the new converters
         * @return a new {@code EntitiesStep} with the updated converters
         * @throws NullPointerException if {@code converters} is null
         */
        public EntitiesStep withConverters(Converters converters) {
            Objects.requireNonNull(converters, "converters is required");
            return new EntitiesStep(converters, entities);
        }

        /**
         * Returns a new {@code EntitiesStep} with the given entities metadata.
         *
         * @param entities the new entities metadata
         * @return a new {@code EntitiesStep} with the updated entities
         * @throws NullPointerException if {@code entities} is null
         */
        public EntitiesStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new EntitiesStep(converters, entities);
        }

        /**
         * Advances to the next step by providing the {@link DatabaseManager}.
         *
         * @param manager the column database manager
         * @return a {@link ManagerStep} holding all three required dependencies
         * @throws NullPointerException if {@code manager} is null
         */
        public ManagerStep withManager(DatabaseManager manager) {
            Objects.requireNonNull(manager, "manager is required");
            return new ManagerStep(converters, entities, manager);
        }
    }

    /**
     * Third step of the builder. Holds {@link Converters}, {@link EntitiesMetadata},
     * and {@link DatabaseManager}. {@code build()} is available here using no-op
     * consumers for both persist events.
     *
     * @param converters the converters; must not be null
     * @param entities   the entities metadata; must not be null
     * @param manager    the column database manager; must not be null
     */
    record ManagerStep(Converters converters, EntitiesMetadata entities,
                       DatabaseManager manager) implements ColumnTemplateBuilder {

        /**
         * Returns a new {@code ManagerStep} with the given converters.
         *
         * @param converters the new converters
         * @return a new {@code ManagerStep} with the updated converters
         * @throws NullPointerException if {@code converters} is null
         */
        public ManagerStep withConverters(Converters converters) {
            Objects.requireNonNull(converters, "converters is required");
            return new ManagerStep(converters, entities, manager);
        }

        /**
         * Returns a new {@code ManagerStep} with the given entities metadata.
         *
         * @param entities the new entities metadata
         * @return a new {@code ManagerStep} with the updated entities
         * @throws NullPointerException if {@code entities} is null
         */
        public ManagerStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new ManagerStep(converters, entities, manager);
        }

        /**
         * Returns a new {@code ManagerStep} with the given manager.
         *
         * @param manager the new column database manager
         * @return a new {@code ManagerStep} with the updated manager
         * @throws NullPointerException if {@code manager} is null
         */
        public ManagerStep withManager(DatabaseManager manager) {
            Objects.requireNonNull(manager, "manager is required");
            return new ManagerStep(converters, entities, manager);
        }

        /**
         * Advances to the next step by providing a pre-persist consumer.
         * If {@code prePersist} is {@code null}, a no-op consumer is used.
         *
         * @param prePersist the consumer to be invoked before entity persistence;
         *                   {@code null} is treated as no-op
         * @return a {@link PrePersistStep} with the given consumer
         */
        public PrePersistStep withPrePersist(Consumer<EntityPrePersist> prePersist) {
            return new PrePersistStep(converters, entities, manager,
                    prePersist != null ? prePersist : t -> {});
        }

        /**
         * Builds a {@link ColumnTemplate} using no-op consumers for both persist events.
         *
         * @return a new {@link ColumnTemplate} instance
         */
        public ColumnTemplate build() {
            Objects.requireNonNull(converters, "converters is required");
            Objects.requireNonNull(entities, "entities is required");
            Objects.requireNonNull(manager, "manager is required");
            EventPersistManager eventManager = EventPersistManagerBuilder.builder().build();
            var converterFactory = EntityConverterFactoryBuilder.builder()
                    .withEntities(entities)
                    .withConverters(converters)
                    .build();
            return new ColumnTemplateProducer.ProducerColumnTemplate(converterFactory, manager,
                    eventManager, entities, converters);
        }
    }

    /**
     * Fourth step of the builder. Holds all required dependencies plus the pre-persist consumer.
     * {@code build()} is available here using a no-op post-persist consumer.
     *
     * @param converters the converters; must not be null
     * @param entities   the entities metadata; must not be null
     * @param manager    the column database manager; must not be null
     * @param prePersist the consumer invoked before entity persistence; must not be null
     */
    record PrePersistStep(Converters converters, EntitiesMetadata entities,
                          DatabaseManager manager,
                          Consumer<EntityPrePersist> prePersist) implements ColumnTemplateBuilder {

        /**
         * Returns a new {@code PrePersistStep} with the given converters.
         *
         * @param converters the new converters
         * @return a new {@code PrePersistStep} with the updated converters
         * @throws NullPointerException if {@code converters} is null
         */
        public PrePersistStep withConverters(Converters converters) {
            Objects.requireNonNull(converters, "converters is required");
            return new PrePersistStep(converters, entities, manager, prePersist);
        }

        /**
         * Returns a new {@code PrePersistStep} with the given entities metadata.
         *
         * @param entities the new entities metadata
         * @return a new {@code PrePersistStep} with the updated entities
         * @throws NullPointerException if {@code entities} is null
         */
        public PrePersistStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new PrePersistStep(converters, entities, manager, prePersist);
        }

        /**
         * Returns a new {@code PrePersistStep} with the given manager.
         *
         * @param manager the new column database manager
         * @return a new {@code PrePersistStep} with the updated manager
         * @throws NullPointerException if {@code manager} is null
         */
        public PrePersistStep withManager(DatabaseManager manager) {
            Objects.requireNonNull(manager, "manager is required");
            return new PrePersistStep(converters, entities, manager, prePersist);
        }

        /**
         * Returns a new {@code PrePersistStep} with the given pre-persist consumer.
         * If {@code prePersist} is {@code null}, a no-op consumer is used.
         *
         * @param prePersist the new pre-persist consumer; {@code null} is treated as no-op
         * @return a new {@code PrePersistStep} with the updated consumer
         */
        public PrePersistStep withPrePersist(Consumer<EntityPrePersist> prePersist) {
            return new PrePersistStep(converters, entities, manager,
                    prePersist != null ? prePersist : t -> {});
        }

        /**
         * Advances to the next step by providing a post-persist consumer.
         * If {@code postPersist} is {@code null}, a no-op consumer is used.
         *
         * @param postPersist the consumer to be invoked after entity persistence;
         *                    {@code null} is treated as no-op
         * @return a {@link PostPersistStep} with both consumers
         */
        public PostPersistStep withPostPersist(Consumer<EntityPostPersist> postPersist) {
            return new PostPersistStep(converters, entities, manager, prePersist,
                    postPersist != null ? postPersist : t -> {});
        }

        /**
         * Builds a {@link ColumnTemplate} using the current pre-persist consumer and a no-op
         * post-persist consumer.
         *
         * @return a new {@link ColumnTemplate} instance
         */
        public ColumnTemplate build() {
            Objects.requireNonNull(converters, "converters is required");
            Objects.requireNonNull(entities, "entities is required");
            Objects.requireNonNull(manager, "manager is required");
            EventPersistManager eventManager = EventPersistManagerBuilder.builder()
                    .withPrePersist(prePersist).build();
            var converterFactory = EntityConverterFactoryBuilder.builder()
                    .withEntities(entities)
                    .withConverters(converters)
                    .build();
            return new ColumnTemplateProducer.ProducerColumnTemplate(converterFactory, manager,
                    eventManager, entities, converters);
        }
    }

    /**
     * Fifth and final step of the builder. Holds all dependencies including both
     * persist consumers. Provides the complete {@code build()}.
     *
     * @param converters  the converters; must not be null
     * @param entities    the entities metadata; must not be null
     * @param manager     the column database manager; must not be null
     * @param prePersist  the consumer invoked before entity persistence; must not be null
     * @param postPersist the consumer invoked after entity persistence; must not be null
     */
    record PostPersistStep(Converters converters, EntitiesMetadata entities,
                           DatabaseManager manager,
                           Consumer<EntityPrePersist> prePersist,
                           Consumer<EntityPostPersist> postPersist) implements ColumnTemplateBuilder {

        /**
         * Returns a new {@code PostPersistStep} with the given converters.
         *
         * @param converters the new converters
         * @return a new {@code PostPersistStep} with the updated converters
         * @throws NullPointerException if {@code converters} is null
         */
        public PostPersistStep withConverters(Converters converters) {
            Objects.requireNonNull(converters, "converters is required");
            return new PostPersistStep(converters, entities, manager, prePersist, postPersist);
        }

        /**
         * Returns a new {@code PostPersistStep} with the given entities metadata.
         *
         * @param entities the new entities metadata
         * @return a new {@code PostPersistStep} with the updated entities
         * @throws NullPointerException if {@code entities} is null
         */
        public PostPersistStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new PostPersistStep(converters, entities, manager, prePersist, postPersist);
        }

        /**
         * Returns a new {@code PostPersistStep} with the given manager.
         *
         * @param manager the new column database manager
         * @return a new {@code PostPersistStep} with the updated manager
         * @throws NullPointerException if {@code manager} is null
         */
        public PostPersistStep withManager(DatabaseManager manager) {
            Objects.requireNonNull(manager, "manager is required");
            return new PostPersistStep(converters, entities, manager, prePersist, postPersist);
        }

        /**
         * Returns a new {@code PostPersistStep} with the given pre-persist consumer.
         * If {@code prePersist} is {@code null}, a no-op consumer is used.
         *
         * @param prePersist the new pre-persist consumer; {@code null} is treated as no-op
         * @return a new {@code PostPersistStep} with the updated consumer
         */
        public PostPersistStep withPrePersist(Consumer<EntityPrePersist> prePersist) {
            return new PostPersistStep(converters, entities, manager,
                    prePersist != null ? prePersist : t -> {}, postPersist);
        }

        /**
         * Returns a new {@code PostPersistStep} with the given post-persist consumer.
         * If {@code postPersist} is {@code null}, a no-op consumer is used.
         *
         * @param postPersist the new post-persist consumer; {@code null} is treated as no-op
         * @return a new {@code PostPersistStep} with the updated consumer
         */
        public PostPersistStep withPostPersist(Consumer<EntityPostPersist> postPersist) {
            return new PostPersistStep(converters, entities, manager, prePersist,
                    postPersist != null ? postPersist : t -> {});
        }

        /**
         * Builds a {@link ColumnTemplate} using all accumulated dependencies.
         * Internally creates an {@link EventPersistManager} from the two consumers,
         * an {@link org.eclipse.jnosql.mapping.semistructured.EntityConverterFactory} via
         * {@link EntityConverterFactoryBuilder}, and instantiates
         * {@link ColumnTemplateProducer.ProducerColumnTemplate}.
         *
         * @return a new {@link ColumnTemplate} instance
         */
        public ColumnTemplate build() {
            Objects.requireNonNull(converters, "converters is required");
            Objects.requireNonNull(entities, "entities is required");
            Objects.requireNonNull(manager, "manager is required");
            EventPersistManager eventManager = EventPersistManagerBuilder.builder()
                    .withPrePersist(prePersist)
                    .withPostPersist(postPersist)
                    .build();
            var converterFactory = EntityConverterFactoryBuilder.builder()
                    .withEntities(entities)
                    .withConverters(converters)
                    .build();
            return new ColumnTemplateProducer.ProducerColumnTemplate(converterFactory, manager,
                    eventManager, entities, converters);
        }
    }
}
