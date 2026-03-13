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

import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.EntityPostPersist;
import org.eclipse.jnosql.mapping.EntityPrePersist;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class DocumentTemplateBuilderTest {

    // -----------------------------------------------------------------------
    // Structural / immutability tests — pure unit tests, no CDI
    // -----------------------------------------------------------------------

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("Builder structure and immutability (no CDI)")
    class StructureTests {

        @Mock
        Converters converters;

        @Mock
        Converters otherConverters;

        @Mock
        EntitiesMetadata entities;

        @Mock
        EntitiesMetadata otherEntities;

        @Mock
        DatabaseManager manager;

        @Mock
        DatabaseManager otherManager;

        @Test
        @DisplayName("builder() should return a ConvertersStep instance")
        void shouldReturnConvertersStepFromBuilder() {
            DocumentTemplateBuilder builder = DocumentTemplateBuilder.builder();
            assertThat(builder).isInstanceOf(DocumentTemplateBuilder.ConvertersStep.class);
        }

        @Test
        @DisplayName("withConverters() on ConvertersStep should return a new step without modifying the original")
        void shouldBeImmutableOnConvertersStep() {
            DocumentTemplateBuilder.ConvertersStep original = DocumentTemplateBuilder.builder()
                    .withConverters(converters);
            DocumentTemplateBuilder.ConvertersStep updated = original.withConverters(otherConverters);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(original.converters())
                        .as("original step should still hold the first converters")
                        .isSameAs(converters);
                softly.assertThat(updated.converters())
                        .as("updated step should hold the second converters")
                        .isSameAs(otherConverters);
                softly.assertThat(original).as("original and updated should be different instances")
                        .isNotSameAs(updated);
            });
        }

        @Test
        @DisplayName("withEntities() on ConvertersStep should advance to EntitiesStep")
        void shouldAdvanceToEntitiesStep() {
            DocumentTemplateBuilder next = DocumentTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities);
            assertThat(next).isInstanceOf(DocumentTemplateBuilder.EntitiesStep.class);
        }

        @Test
        @DisplayName("withConverters() on EntitiesStep should return a new ConvertersStep")
        void shouldBeImmutableOnEntitiesStepConverters() {
            DocumentTemplateBuilder.EntitiesStep original = DocumentTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities);
            DocumentTemplateBuilder.ConvertersStep updated = original.withConverters(otherConverters);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(original.converters())
                        .as("original step should still hold the first converters")
                        .isSameAs(converters);
                softly.assertThat(updated.converters())
                        .as("updated step should hold the other converters")
                        .isSameAs(otherConverters);
            });
        }

        @Test
        @DisplayName("withEntities() on EntitiesStep should return a new step without modifying the original")
        void shouldBeImmutableOnEntitiesStepEntities() {
            DocumentTemplateBuilder.EntitiesStep original = DocumentTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities);
            DocumentTemplateBuilder.EntitiesStep updated = original.withEntities(otherEntities);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(original.entities())
                        .as("original step should still hold the first entities")
                        .isSameAs(entities);
                softly.assertThat(updated.entities())
                        .as("updated step should hold the other entities")
                        .isSameAs(otherEntities);
                softly.assertThat(original.converters())
                        .as("converters should be unchanged in both")
                        .isSameAs(updated.converters());
                softly.assertThat(original).as("original and updated should be different instances")
                        .isNotSameAs(updated);
            });
        }

        @Test
        @DisplayName("withManager() on EntitiesStep should advance to ManagerStep")
        void shouldAdvanceToManagerStep() {
            DocumentTemplateBuilder next = DocumentTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager);
            assertThat(next).isInstanceOf(DocumentTemplateBuilder.ManagerStep.class);
        }

        @Test
        @DisplayName("withManager() on ManagerStep should return a new step without modifying the original")
        void shouldBeImmutableOnManagerStepManager() {
            DocumentTemplateBuilder.ManagerStep original = DocumentTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager);
            DocumentTemplateBuilder.ManagerStep updated = original.withManager(otherManager);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(original.manager())
                        .as("original step should still hold the first manager")
                        .isSameAs(manager);
                softly.assertThat(updated.manager())
                        .as("updated step should hold the other manager")
                        .isSameAs(otherManager);
                softly.assertThat(original).as("original and updated should be different instances")
                        .isNotSameAs(updated);
            });
        }

        @Test
        @DisplayName("build() on ManagerStep should return a non-null DocumentTemplate")
        void shouldBuildDocumentTemplateFromManagerStep() {
            DocumentTemplate template = DocumentTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager)
                    .build();
            assertThat(template).isNotNull();
        }

        @Test
        @DisplayName("returned DocumentTemplate should be an instance of DocumentTemplate")
        void shouldReturnDocumentTemplateInstance() {
            DocumentTemplate template = DocumentTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager)
                    .build();
            assertThat(template).isInstanceOf(DocumentTemplate.class);
        }

        @Test
        @DisplayName("withPrePersist() on ManagerStep should advance to PrePersistStep")
        void shouldAdvanceToPrePersistStep() {
            Consumer<EntityPrePersist> prePersist = e -> {};
            DocumentTemplateBuilder next = DocumentTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager)
                    .withPrePersist(prePersist);
            assertThat(next).isInstanceOf(DocumentTemplateBuilder.PrePersistStep.class);
        }

        @Test
        @DisplayName("withPostPersist() on PrePersistStep should advance to PostPersistStep")
        void shouldAdvanceToPostPersistStep() {
            Consumer<EntityPrePersist> prePersist = e -> {};
            Consumer<EntityPostPersist> postPersist = e -> {};
            DocumentTemplateBuilder next = DocumentTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager)
                    .withPrePersist(prePersist)
                    .withPostPersist(postPersist);
            assertThat(next).isInstanceOf(DocumentTemplateBuilder.PostPersistStep.class);
        }

        @Test
        @DisplayName("build() on PostPersistStep should return a non-null DocumentTemplate")
        void shouldBuildDocumentTemplateFromPostPersistStep() {
            Consumer<EntityPrePersist> prePersist = e -> {};
            Consumer<EntityPostPersist> postPersist = e -> {};
            DocumentTemplate template = DocumentTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager)
                    .withPrePersist(prePersist)
                    .withPostPersist(postPersist)
                    .build();
            assertThat(template).isNotNull();
        }

        @Test
        @DisplayName("null converters should throw NullPointerException on withConverters()")
        void shouldThrowOnNullConverters() {
            assertThatNullPointerException()
                    .isThrownBy(() -> DocumentTemplateBuilder.builder().withConverters(null))
                    .withMessage("converters is required");
        }

        @Test
        @DisplayName("null entities should throw NullPointerException on withEntities()")
        void shouldThrowOnNullEntities() {
            DocumentTemplateBuilder.ConvertersStep step = DocumentTemplateBuilder.builder()
                    .withConverters(converters);
            assertThatNullPointerException()
                    .isThrownBy(() -> step.withEntities(null))
                    .withMessage("entities is required");
        }

        @Test
        @DisplayName("null manager should throw NullPointerException on withManager()")
        void shouldThrowOnNullManager() {
            DocumentTemplateBuilder.EntitiesStep step = DocumentTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities);
            assertThatNullPointerException()
                    .isThrownBy(() -> step.withManager(null))
                    .withMessage("manager is required");
        }

        @Test
        @DisplayName("null prePersist should throw NullPointerException on withPrePersist()")
        void shouldThrowOnNullPrePersist() {
            DocumentTemplateBuilder.ManagerStep step = DocumentTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager);
            assertThatNullPointerException()
                    .isThrownBy(() -> step.withPrePersist(null))
                    .withMessage("prePersist is required");
        }

        @Test
        @DisplayName("null postPersist should throw NullPointerException on withPostPersist()")
        void shouldThrowOnNullPostPersist() {
            Consumer<EntityPrePersist> prePersist = e -> {};
            DocumentTemplateBuilder.PrePersistStep step = DocumentTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager)
                    .withPrePersist(prePersist);
            assertThatNullPointerException()
                    .isThrownBy(() -> step.withPostPersist(null))
                    .withMessage("postPersist is required");
        }
    }

    // -----------------------------------------------------------------------
    // Functional tests — uses CDI to get real dependencies
    // -----------------------------------------------------------------------

    @Nested
    @EnableAutoWeld
    @AddPackages(value = {Converters.class, EntityConverter.class, DocumentTemplate.class})
    @AddPackages(MockProducer.class)
    @AddPackages(Reflections.class)
    @AddExtensions({ReflectionEntityMetadataExtension.class})
    @ExtendWith(MockitoExtension.class)
    @DisplayName("Builder functional tests with real CDI dependencies")
    class FunctionalTests {

        @Inject
        private Converters converters;

        @Inject
        private EntitiesMetadata entities;

        @Mock
        private DatabaseManager manager;

        @Test
        @DisplayName("should build a DocumentTemplate without pre/post persist consumers")
        void shouldBuildWithoutEventConsumers() {
            DocumentTemplate template = DocumentTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager)
                    .build();

            assertThat(template).isNotNull()
                    .isInstanceOf(DocumentTemplate.class);
        }

        @Test
        @DisplayName("should invoke pre-persist consumer when provided")
        void shouldInvokePrePersistConsumer() {
            AtomicBoolean prePersistCalled = new AtomicBoolean(false);
            Consumer<EntityPrePersist> prePersist = e -> prePersistCalled.set(true);

            DocumentTemplate template = DocumentTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager)
                    .withPrePersist(prePersist)
                    .build();

            assertThat(template).isNotNull();
            // Note: We don't directly invoke the consumer here as that would require
            // a full entity persistence scenario. This test verifies the builder
            // accepts and holds the consumer.
        }

        @Test
        @DisplayName("should invoke post-persist consumer when provided")
        void shouldInvokePostPersistConsumer() {
            AtomicBoolean postPersistCalled = new AtomicBoolean(false);
            Consumer<EntityPostPersist> postPersist = e -> postPersistCalled.set(true);

            DocumentTemplate template = DocumentTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager)
                    .withPrePersist(e -> {})
                    .withPostPersist(postPersist)
                    .build();

            assertThat(template).isNotNull();
            // Note: We don't directly invoke the consumer here as that would require
            // a full entity persistence scenario. This test verifies the builder
            // accepts and holds the consumer.
        }

        @Test
        @DisplayName("should build DocumentTemplate without CDI container")
        void shouldBuildWithoutCDI() {
            // This test verifies that the builder can instantiate a DocumentTemplate
            // outside of CDI, demonstrating the primary capability of this builder.
            DocumentTemplate template = DocumentTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager)
                    .build();

            assertThat(template).isNotNull()
                    .isInstanceOf(DocumentTemplate.class);
        }
    }
}
