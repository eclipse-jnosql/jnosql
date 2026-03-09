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
package org.eclipse.jnosql.mapping.graph;

import org.eclipse.jnosql.communication.graph.GraphDatabaseManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.EntityConverterFactory;
import org.eclipse.jnosql.mapping.semistructured.EntityConverterFactoryBuilder;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManagerBuilder;
import org.eclipse.jnosql.mapping.EntityPostPersist;
import org.eclipse.jnosql.mapping.EntityPrePersist;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Step builder for creating {@link GraphTemplate} instances outside a CDI container.
 *
 * <p>
 * Usage example:
 * 
 * <pre>{@code
 * GraphTemplate template = GraphTemplateBuilder.builder()
 *         .withConverters(converters)
 *         .withEntities(entitiesMetadata)
 *         .withManager(graphDatabaseManager)
 *         .withPrePersist(prePersistConsumer)
 *         .withPostPersist(postPersistConsumer)
 *         .build();
 * }</pre>
 *
 * <p>
 * All steps are immutable records. Each {@code withXxx()} call returns a new
 * record with the updated value, leaving the original step unchanged.
 * </p>
 *
 * <p>
 * The {@code build()} method is available once {@link Converters},
 * {@link EntitiesMetadata}, and {@link GraphDatabaseManager} have been provided
 * (from {@link ManagerStep} onward). Event consumers are optional and default to
 * no-op implementations if not provided.
 * </p>
 *
 * <p>
 * <strong>Note:</strong> Instances created by this builder are not managed by
 * any CDI container. The caller is responsible for lifecycle management.
 * </p>
 */
public sealed interface GraphTemplateBuilder permits GraphTemplateBuilder.ConvertersStep,
        GraphTemplateBuilder.EntitiesStep,
        GraphTemplateBuilder.ManagerStep,
        GraphTemplateBuilder.PrePersistStep,
        GraphTemplateBuilder.PostPersistStep {

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
    record ConvertersStep(Converters converters) implements GraphTemplateBuilder {

        /**
         * Returns a new {@code ConvertersStep} with the given converters.
         *
         * @param converters the converters
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
    record EntitiesStep(Converters converters, EntitiesMetadata entities) implements GraphTemplateBuilder {

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
         * @return a new {@code EntitiesStep} with the updated metadata
         * @throws NullPointerException if {@code entities} is null
         */
        public EntitiesStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new EntitiesStep(converters, entities);
        }

        /**
         * Advances to the next step by providing the {@link GraphDatabaseManager}.
         *
         * @param manager the graph database manager
         * @return a {@link ManagerStep} holding all three dependencies
         * @throws NullPointerException if {@code manager} is null
         */
        public ManagerStep withManager(GraphDatabaseManager manager) {
            Objects.requireNonNull(manager, "manager is required");
            return new ManagerStep(converters, entities, manager);
        }
    }

    /**
     * Third step of the builder. Holds {@link Converters}, {@link EntitiesMetadata},
     * and {@link GraphDatabaseManager}. The {@code build()} method is available here
     * with no-op consumers for events.
     *
     * @param converters the converters; must not be null
     * @param entities   the entities metadata; must not be null
     * @param manager    the graph database manager; must not be null
     */
    record ManagerStep(Converters converters, EntitiesMetadata entities, GraphDatabaseManager manager)
            implements GraphTemplateBuilder {

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
         * @return a new {@code ManagerStep} with the updated metadata
         * @throws NullPointerException if {@code entities} is null
         */
        public ManagerStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new ManagerStep(converters, entities, manager);
        }

        /**
         * Returns a new {@code ManagerStep} with the given manager.
         *
         * @param manager the new graph database manager
         * @return a new {@code ManagerStep} with the updated manager
         * @throws NullPointerException if {@code manager} is null
         */
        public ManagerStep withManager(GraphDatabaseManager manager) {
            Objects.requireNonNull(manager, "manager is required");
            return new ManagerStep(converters, entities, manager);
        }

        /**
         * Advances to the next step by providing a pre-persist event consumer.
         *
         * @param prePersist the consumer to invoke before entity persistence
         * @return a {@link PrePersistStep} holding all four dependencies
         * @throws NullPointerException if {@code prePersist} is null
         */
        public PrePersistStep withPrePersist(Consumer<EntityPrePersist> prePersist) {
            Objects.requireNonNull(prePersist, "prePersist is required");
            return new PrePersistStep(converters, entities, manager, prePersist);
        }

        /**
         * Builds a {@link GraphTemplate} using no-op consumers for both
         * pre-persist and post-persist events.
         *
         * @return a new {@link GraphTemplate} instance
         */
        public GraphTemplate build() {
            return new PostPersistStep(converters, entities, manager, e -> {}, e -> {}).build();
        }
    }

    /**
     * Fourth step of the builder. Holds all three required dependencies plus
     * the pre-persist event consumer.
     *
     * @param converters  the converters; must not be null
     * @param entities    the entities metadata; must not be null
     * @param manager     the graph database manager; must not be null
     * @param prePersist  the pre-persist consumer; must not be null
     */
    record PrePersistStep(Converters converters, EntitiesMetadata entities, GraphDatabaseManager manager,
                          Consumer<EntityPrePersist> prePersist) implements GraphTemplateBuilder {

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
         * @return a new {@code PrePersistStep} with the updated metadata
         * @throws NullPointerException if {@code entities} is null
         */
        public PrePersistStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new PrePersistStep(converters, entities, manager, prePersist);
        }

        /**
         * Returns a new {@code PrePersistStep} with the given manager.
         *
         * @param manager the new graph database manager
         * @return a new {@code PrePersistStep} with the updated manager
         * @throws NullPointerException if {@code manager} is null
         */
        public PrePersistStep withManager(GraphDatabaseManager manager) {
            Objects.requireNonNull(manager, "manager is required");
            return new PrePersistStep(converters, entities, manager, prePersist);
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
         * Advances to the final step by providing a post-persist event consumer.
         * If {@code null} is provided, a no-op consumer is used.
         *
         * @param postPersist the consumer to invoke after entity persistence
         * @return a {@link PostPersistStep} holding all dependencies
         * @throws NullPointerException if {@code postPersist} is null
         */
        public PostPersistStep withPostPersist(Consumer<EntityPostPersist> postPersist) {
            Objects.requireNonNull(postPersist, "postPersist is required");
            return new PostPersistStep(converters, entities, manager, prePersist, postPersist);
        }

        /**
         * Builds a {@link GraphTemplate} using a no-op consumer for the post-persist event.
         *
         * @return a new {@link GraphTemplate} instance
         */
        public GraphTemplate build() {
            return new PostPersistStep(converters, entities, manager, prePersist, e -> {}).build();
        }
    }

    /**
     * Final step of the builder. Holds all dependencies including both
     * pre-persist and post-persist event consumers.
     *
     * @param converters    the converters; must not be null
     * @param entities      the entities metadata; must not be null
     * @param manager       the graph database manager; must not be null
     * @param prePersist    the pre-persist consumer; must not be null
     * @param postPersist   the post-persist consumer; must not be null
     */
    record PostPersistStep(Converters converters, EntitiesMetadata entities, GraphDatabaseManager manager,
                           Consumer<EntityPrePersist> prePersist, Consumer<EntityPostPersist> postPersist)
            implements GraphTemplateBuilder {

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
         * @return a new {@code PostPersistStep} with the updated metadata
         * @throws NullPointerException if {@code entities} is null
         */
        public PostPersistStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new PostPersistStep(converters, entities, manager, prePersist, postPersist);
        }

        /**
         * Returns a new {@code PostPersistStep} with the given manager.
         *
         * @param manager the new graph database manager
         * @return a new {@code PostPersistStep} with the updated manager
         * @throws NullPointerException if {@code manager} is null
         */
        public PostPersistStep withManager(GraphDatabaseManager manager) {
            Objects.requireNonNull(manager, "manager is required");
            return new PostPersistStep(converters, entities, manager, prePersist, postPersist);
        }

        /**
         * Returns a new {@code PostPersistStep} with the given pre-persist consumer.
         *
         * @param prePersist the new pre-persist consumer
         * @return a new {@code PostPersistStep} with the updated consumer
         * @throws NullPointerException if {@code prePersist} is null
         */
        public PostPersistStep withPrePersist(Consumer<EntityPrePersist> prePersist) {
            Objects.requireNonNull(prePersist, "prePersist is required");
            return new PostPersistStep(converters, entities, manager, prePersist, postPersist);
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
         * Builds a complete {@link GraphTemplate} using all accumulated dependencies.
         * Internally creates an {@link EventPersistManager} and an {@link EntityConverterFactory}
         * without any CDI container involvement, then returns a {@link GraphTemplate}
         * implementation.
         *
         * @return a new {@link GraphTemplate} instance
         * @throws NullPointerException if any required dependency is null
         */
        public GraphTemplate build() {
            Objects.requireNonNull(converters, "converters is required");
            Objects.requireNonNull(entities, "entities is required");
            Objects.requireNonNull(manager, "manager is required");
            Objects.requireNonNull(prePersist, "prePersist is required");
            Objects.requireNonNull(postPersist, "postPersist is required");

            // Create EventPersistManager using the builder from semistructured
            EventPersistManager eventManager = EventPersistManagerBuilder.builder()
                    .withPrePersist(prePersist)
                    .withPostPersist(postPersist)
                    .build();

            // Create EntityConverterFactory using the builder from semistructured
            EntityConverterFactory converterFactory = EntityConverterFactoryBuilder.builder()
                    .withEntities(entities)
                    .withConverters(converters)
                    .build();

            // Create and return the ProducerGraphTemplate (package-private class)
            return new GraphTemplateProducer.ProducerGraphTemplate(converterFactory, manager, eventManager, entities, converters);
        }
    }
}
