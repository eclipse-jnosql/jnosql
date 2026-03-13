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

import org.eclipse.jnosql.mapping.EntityPostPersist;
import org.eclipse.jnosql.mapping.EntityPrePersist;

import java.util.function.Consumer;

/**
 * Step builder for creating {@link EventPersistManager} instances outside a CDI
 * container.
 *
 * <p>
 * Usage example:
 * 
 * <pre>{@code
 * EventPersistManager manager = EventPersistManagerBuilder.builder()
 *         .withPostPersist(event -> System.out.println("Post: " + event.get()))
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
 * The {@code build()} method is available from the first step
 * ({@link PrePersistStep})
 * onward. Any consumer not explicitly supplied defaults to a no-op
 * ({@code t -> {}}).
 * </p>
 *
 * <p>
 * <strong>Note:</strong> Instances created by this builder are not managed by
 * any CDI
 * container. The caller is responsible for lifecycle management.
 * </p>
 */
public sealed interface EventPersistManagerBuilder permits EventPersistManagerBuilder.PrePersistStep,
        EventPersistManagerBuilder.PostPersistStep {

    /**
     * Returns a new builder starting from {@link PrePersistStep} with a no-op
     * pre-persist consumer.
     *
     * @return the first step of the builder
     */
    static PrePersistStep builder() {
        return new PrePersistStep(t -> {
        });
    }

    /**
     * First step of the builder. Holds the {@link EntityPrePersist} consumer.
     * {@code build()} is available here with a no-op post-persist consumer.
     *
     * @param prePersist the consumer to be invoked before entity persistence; must
     *                   not be null
     */
    record PrePersistStep(Consumer<EntityPrePersist> prePersist) implements EventPersistManagerBuilder {

        /**
         * Returns a new {@code PrePersistStep} with the given pre-persist consumer.
         * If {@code prePersist} is {@code null}, a no-op consumer is used.
         *
         * @param prePersist the new pre-persist consumer; {@code null} is treated as
         *                   no-op
         * @return a new {@code PrePersistStep} with the updated consumer
         */
        public PrePersistStep withPrePersist(Consumer<EntityPrePersist> prePersist) {
            return new PrePersistStep(prePersist != null ? prePersist : t -> {
            });
        }

        /**
         * Advances to the next step by providing a post-persist consumer.
         * If {@code postPersist} is {@code null}, a no-op consumer is used.
         *
         * @param postPersist the consumer to be invoked after entity persistence;
         *                    {@code null} is treated as no-op
         * @return a {@link PostPersistStep} holding both consumers
         */
        public PostPersistStep withPostPersist(Consumer<EntityPostPersist> postPersist) {
            return new PostPersistStep(prePersist, postPersist != null ? postPersist : t -> {
            });
        }

        /**
         * Builds an {@link EventPersistManager} using the current pre-persist consumer
         * and a no-op post-persist consumer.
         *
         * @return a new {@link EventPersistManager} instance
         */
        public EventPersistManager build() {
            return new EventPersistManager(prePersist, t -> {
            });
        }
    }

    /**
     * Second step of the builder. Holds both consumers and provides
     * {@code build()}.
     *
     * @param prePersist  the consumer to be invoked before entity persistence; must
     *                    not be null
     * @param postPersist the consumer to be invoked after entity persistence; must
     *                    not be null
     */
    record PostPersistStep(Consumer<EntityPrePersist> prePersist,
            Consumer<EntityPostPersist> postPersist) implements EventPersistManagerBuilder {

        /**
         * Returns a new {@code PostPersistStep} with the given pre-persist consumer.
         * If {@code prePersist} is {@code null}, a no-op consumer is used.
         *
         * @param prePersist the new pre-persist consumer; {@code null} is treated as
         *                   no-op
         * @return a new {@code PostPersistStep} with the updated pre-persist consumer
         */
        public PostPersistStep withPrePersist(Consumer<EntityPrePersist> prePersist) {
            return new PostPersistStep(prePersist != null ? prePersist : t -> {
            }, postPersist);
        }

        /**
         * Returns a new {@code PostPersistStep} with the given post-persist consumer.
         * If {@code postPersist} is {@code null}, a no-op consumer is used.
         *
         * @param postPersist the new post-persist consumer; {@code null} is treated as
         *                    no-op
         * @return a new {@code PostPersistStep} with the updated post-persist consumer
         */
        public PostPersistStep withPostPersist(Consumer<EntityPostPersist> postPersist) {
            return new PostPersistStep(prePersist, postPersist != null ? postPersist : t -> {
            });
        }

        /**
         * Builds an {@link EventPersistManager} using both consumers.
         *
         * @return a new {@link EventPersistManager} instance
         */
        public EventPersistManager build() {
            return new EventPersistManager(prePersist, postPersist);
        }
    }
}
