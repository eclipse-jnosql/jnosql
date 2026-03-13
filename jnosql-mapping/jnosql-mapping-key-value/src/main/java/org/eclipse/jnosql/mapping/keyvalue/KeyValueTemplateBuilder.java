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
package org.eclipse.jnosql.mapping.keyvalue;

import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.eclipse.jnosql.mapping.EntityPostPersist;
import org.eclipse.jnosql.mapping.EntityPrePersist;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A step builder for creating {@link KeyValueTemplate} instances without requiring a CDI container.
 *
 * <p>Usage:
 * <pre>{@code
 * KeyValueTemplate template = KeyValueTemplateBuilder.builder()
 *     .withConverter(converter)
 *     .withManager(bucketManager)
 *     .build();
 * }</pre>
 *
 * <p>Event consumers are optional. When not provided, no-op consumers are used.
 * Steps are immutable — each {@code with*} method returns a new record instance.
 *
 * <p>The required dependencies are, in order:
 * <ol>
 *   <li>{@link KeyValueEntityConverter} — via {@link ConverterStep#withConverter(KeyValueEntityConverter)}</li>
 *   <li>{@link BucketManager} — via {@link ConverterStep#withManager(BucketManager)}</li>
 * </ol>
 *
 * <p>Optional event consumers:
 * <ul>
 *   <li>{@link Consumer}&lt;{@link EntityPrePersist}&gt; — via {@link ManagerStep#withPrePersist(Consumer)}</li>
 *   <li>{@link Consumer}&lt;{@link EntityPostPersist}&gt; — via {@link PrePersistStep#withPostPersist(Consumer)}</li>
 * </ul>
 *
 * <p>The {@link BucketManager} obtained outside a CDI container is the caller's responsibility
 * to manage; e.g. from a Spring {@code ApplicationContext} or a direct driver instance.
 */
public sealed interface KeyValueTemplateBuilder
        permits KeyValueTemplateBuilder.ConverterStep,
                KeyValueTemplateBuilder.ManagerStep,
                KeyValueTemplateBuilder.PrePersistStep,
                KeyValueTemplateBuilder.PostPersistStep {

    /**
     * Returns the first step of the builder, requiring a {@link KeyValueEntityConverter}.
     *
     * @return the first {@link ConverterStep}
     */
    static ConverterStep builder() {
        return new ConverterStep(null);
    }

    // -------------------------------------------------------------------------
    // Step 1 — ConverterStep
    // -------------------------------------------------------------------------

    /**
     * First step of the builder. Holds the {@link KeyValueEntityConverter} and advances to
     * {@link ManagerStep} once a {@link BucketManager} is provided.
     *
     * @param converter the converter; may be {@code null} at construction time but must be
     *                  non-null before calling {@link #withManager(BucketManager)}
     */
    record ConverterStep(KeyValueEntityConverter converter) implements KeyValueTemplateBuilder {

        /**
         * Returns a new {@link ConverterStep} with the given converter.
         *
         * @param converter the converter; must not be {@code null}
         * @return a new {@link ConverterStep}
         * @throws NullPointerException if {@code converter} is {@code null}
         */
        public ConverterStep withConverter(KeyValueEntityConverter converter) {
            Objects.requireNonNull(converter, "converter is required");
            return new ConverterStep(converter);
        }

        /**
         * Advances to {@link ManagerStep} with the given manager.
         *
         * @param manager the bucket manager; must not be {@code null}
         * @return a new {@link ManagerStep}
         * @throws NullPointerException if {@code converter} (held in this step) or {@code manager} is {@code null}
         */
        public ManagerStep withManager(BucketManager manager) {
            Objects.requireNonNull(converter, "converter is required");
            Objects.requireNonNull(manager, "manager is required");
            return new ManagerStep(converter, manager);
        }
    }

    // -------------------------------------------------------------------------
    // Step 2 — ManagerStep
    // -------------------------------------------------------------------------

    /**
     * Second step of the builder. Holds the converter and the {@link BucketManager}.
     * Allows adding an optional pre-persist consumer or directly building the template.
     *
     * @param converter the entity converter
     * @param manager   the bucket manager
     */
    record ManagerStep(KeyValueEntityConverter converter,
                       BucketManager manager) implements KeyValueTemplateBuilder {

        /**
         * Returns a new {@link ManagerStep} with a replaced converter.
         *
         * @param converter the converter; must not be {@code null}
         * @return a new {@link ManagerStep}
         */
        public ManagerStep withConverter(KeyValueEntityConverter converter) {
            Objects.requireNonNull(converter, "converter is required");
            return new ManagerStep(converter, manager);
        }

        /**
         * Returns a new {@link ManagerStep} with a replaced manager.
         *
         * @param manager the bucket manager; must not be {@code null}
         * @return a new {@link ManagerStep}
         */
        public ManagerStep withManager(BucketManager manager) {
            Objects.requireNonNull(manager, "manager is required");
            return new ManagerStep(converter, manager);
        }

        /**
         * Advances to {@link PrePersistStep} with the given pre-persist consumer.
         *
         * @param prePersist the consumer invoked before persistence; {@code null} is silently replaced by a no-op
         * @return a new {@link PrePersistStep}
         */
        public PrePersistStep withPrePersist(Consumer<EntityPrePersist> prePersist) {
            Consumer<EntityPrePersist> consumer = prePersist != null ? prePersist : e -> {};
            return new PrePersistStep(converter, manager, consumer);
        }

        /**
         * Builds a {@link KeyValueTemplate} using no-op consumers for both pre- and post-persist events.
         *
         * @return a new {@link KeyValueTemplate}
         */
        public KeyValueTemplate build() {
            Consumer<EntityPrePersist> noOpPre = e -> {};
            Consumer<EntityPostPersist> noOpPost = e -> {};
            KeyValueEventPersistManager eventManager =
                    new KeyValueEventPersistManager(noOpPre, noOpPost);
            return new KeyValueTemplateProducer.ProducerKeyValueTemplate(converter, manager, eventManager);
        }
    }

    // -------------------------------------------------------------------------
    // Step 3 — PrePersistStep
    // -------------------------------------------------------------------------

    /**
     * Third step of the builder. Holds converter, manager, and a pre-persist consumer.
     * Allows adding an optional post-persist consumer or directly building the template.
     *
     * @param converter   the entity converter
     * @param manager     the bucket manager
     * @param prePersist  the consumer invoked before persistence
     */
    record PrePersistStep(KeyValueEntityConverter converter,
                          BucketManager manager,
                          Consumer<EntityPrePersist> prePersist) implements KeyValueTemplateBuilder {

        /**
         * Returns a new {@link PrePersistStep} with a replaced converter.
         *
         * @param converter the converter; must not be {@code null}
         * @return a new {@link PrePersistStep}
         */
        public PrePersistStep withConverter(KeyValueEntityConverter converter) {
            Objects.requireNonNull(converter, "converter is required");
            return new PrePersistStep(converter, manager, prePersist);
        }

        /**
         * Returns a new {@link PrePersistStep} with a replaced manager.
         *
         * @param manager the bucket manager; must not be {@code null}
         * @return a new {@link PrePersistStep}
         */
        public PrePersistStep withManager(BucketManager manager) {
            Objects.requireNonNull(manager, "manager is required");
            return new PrePersistStep(converter, manager, prePersist);
        }

        /**
         * Returns a new {@link PrePersistStep} with a replaced pre-persist consumer.
         *
         * @param prePersist the consumer; {@code null} is silently replaced by a no-op
         * @return a new {@link PrePersistStep}
         */
        public PrePersistStep withPrePersist(Consumer<EntityPrePersist> prePersist) {
            Consumer<EntityPrePersist> consumer = prePersist != null ? prePersist : e -> {};
            return new PrePersistStep(converter, manager, consumer);
        }

        /**
         * Advances to {@link PostPersistStep} with the given post-persist consumer.
         *
         * @param postPersist the consumer invoked after persistence; {@code null} is silently replaced by a no-op
         * @return a new {@link PostPersistStep}
         */
        public PostPersistStep withPostPersist(Consumer<EntityPostPersist> postPersist) {
            Consumer<EntityPostPersist> consumer = postPersist != null ? postPersist : e -> {};
            return new PostPersistStep(converter, manager, prePersist, consumer);
        }

        /**
         * Builds a {@link KeyValueTemplate} using the provided pre-persist consumer and a no-op post-persist consumer.
         *
         * @return a new {@link KeyValueTemplate}
         */
        public KeyValueTemplate build() {
            Consumer<EntityPostPersist> noOpPost = e -> {};
            KeyValueEventPersistManager eventManager =
                    new KeyValueEventPersistManager(prePersist, noOpPost);
            return new KeyValueTemplateProducer.ProducerKeyValueTemplate(converter, manager, eventManager);
        }
    }

    // -------------------------------------------------------------------------
    // Step 4 — PostPersistStep
    // -------------------------------------------------------------------------

    /**
     * Fourth (final) step of the builder. Holds all required and optional fields.
     *
     * @param converter   the entity converter
     * @param manager     the bucket manager
     * @param prePersist  the consumer invoked before persistence
     * @param postPersist the consumer invoked after persistence
     */
    record PostPersistStep(KeyValueEntityConverter converter,
                           BucketManager manager,
                           Consumer<EntityPrePersist> prePersist,
                           Consumer<EntityPostPersist> postPersist) implements KeyValueTemplateBuilder {

        /**
         * Returns a new {@link PostPersistStep} with a replaced converter.
         *
         * @param converter the converter; must not be {@code null}
         * @return a new {@link PostPersistStep}
         */
        public PostPersistStep withConverter(KeyValueEntityConverter converter) {
            Objects.requireNonNull(converter, "converter is required");
            return new PostPersistStep(converter, manager, prePersist, postPersist);
        }

        /**
         * Returns a new {@link PostPersistStep} with a replaced manager.
         *
         * @param manager the bucket manager; must not be {@code null}
         * @return a new {@link PostPersistStep}
         */
        public PostPersistStep withManager(BucketManager manager) {
            Objects.requireNonNull(manager, "manager is required");
            return new PostPersistStep(converter, manager, prePersist, postPersist);
        }

        /**
         * Returns a new {@link PostPersistStep} with a replaced pre-persist consumer.
         *
         * @param prePersist the consumer; {@code null} is silently replaced by a no-op
         * @return a new {@link PostPersistStep}
         */
        public PostPersistStep withPrePersist(Consumer<EntityPrePersist> prePersist) {
            Consumer<EntityPrePersist> consumer = prePersist != null ? prePersist : e -> {};
            return new PostPersistStep(converter, manager, consumer, postPersist);
        }

        /**
         * Returns a new {@link PostPersistStep} with a replaced post-persist consumer.
         *
         * @param postPersist the consumer; {@code null} is silently replaced by a no-op
         * @return a new {@link PostPersistStep}
         */
        public PostPersistStep withPostPersist(Consumer<EntityPostPersist> postPersist) {
            Consumer<EntityPostPersist> consumer = postPersist != null ? postPersist : e -> {};
            return new PostPersistStep(converter, manager, prePersist, consumer);
        }

        /**
         * Builds a {@link KeyValueTemplate} using all provided consumers.
         *
         * @return a new {@link KeyValueTemplate}
         */
        public KeyValueTemplate build() {
            KeyValueEventPersistManager eventManager =
                    new KeyValueEventPersistManager(prePersist, postPersist);
            return new KeyValueTemplateProducer.ProducerKeyValueTemplate(converter, manager, eventManager);
        }
    }
}
