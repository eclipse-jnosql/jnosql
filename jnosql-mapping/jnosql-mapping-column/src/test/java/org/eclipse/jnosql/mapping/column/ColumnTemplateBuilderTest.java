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

import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.EntityPrePersist;
import org.eclipse.jnosql.mapping.column.entities.Person;
import org.eclipse.jnosql.mapping.column.spi.ColumnExtension;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ColumnTemplateBuilderTest {

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
            ColumnTemplateBuilder builder = ColumnTemplateBuilder.builder();
            assertThat(builder).isInstanceOf(ColumnTemplateBuilder.ConvertersStep.class);
        }

        @Test
        @DisplayName("withConverters() on ConvertersStep should return a new step without modifying the original")
        void shouldBeImmutableOnConvertersStep() {
            ColumnTemplateBuilder.ConvertersStep original = ColumnTemplateBuilder.builder()
                    .withConverters(converters);
            ColumnTemplateBuilder.ConvertersStep updated = original.withConverters(otherConverters);

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
            ColumnTemplateBuilder next = ColumnTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities);
            assertThat(next).isInstanceOf(ColumnTemplateBuilder.EntitiesStep.class);
        }

        @Test
        @DisplayName("withConverters() on EntitiesStep should return a new step without modifying the original")
        void shouldBeImmutableOnEntitiesStepConverters() {
            ColumnTemplateBuilder.EntitiesStep original = ColumnTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities);
            ColumnTemplateBuilder.EntitiesStep updated = original.withConverters(otherConverters);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(original.converters())
                        .as("original step should still hold the first converters")
                        .isSameAs(converters);
                softly.assertThat(updated.converters())
                        .as("updated step should hold the other converters")
                        .isSameAs(otherConverters);
                softly.assertThat(original.entities())
                        .as("entities should be unchanged in both")
                        .isSameAs(updated.entities());
                softly.assertThat(original).as("original and updated should be different instances")
                        .isNotSameAs(updated);
            });
        }

        @Test
        @DisplayName("withEntities() on EntitiesStep should return a new step without modifying the original")
        void shouldBeImmutableOnEntitiesStepEntities() {
            ColumnTemplateBuilder.EntitiesStep original = ColumnTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities);
            ColumnTemplateBuilder.EntitiesStep updated = original.withEntities(otherEntities);

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
            ColumnTemplateBuilder next = ColumnTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager);
            assertThat(next).isInstanceOf(ColumnTemplateBuilder.ManagerStep.class);
        }

        @Test
        @DisplayName("withManager() on ManagerStep should return a new step without modifying the original")
        void shouldBeImmutableOnManagerStep() {
            ColumnTemplateBuilder.ManagerStep original = ColumnTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager);
            ColumnTemplateBuilder.ManagerStep updated = original.withManager(otherManager);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(original.manager())
                        .as("original step should still hold the first manager")
                        .isSameAs(manager);
                softly.assertThat(updated.manager())
                        .as("updated step should hold the other manager")
                        .isSameAs(otherManager);
                softly.assertThat(original.converters())
                        .as("converters should be unchanged in both")
                        .isSameAs(updated.converters());
                softly.assertThat(original.entities())
                        .as("entities should be unchanged in both")
                        .isSameAs(updated.entities());
                softly.assertThat(original).as("original and updated should be different instances")
                        .isNotSameAs(updated);
            });
        }

        @Test
        @DisplayName("withPrePersist() on ManagerStep should advance to PrePersistStep")
        void shouldAdvanceToPrePersistStep() {
            ColumnTemplateBuilder next = ColumnTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager)
                    .withPrePersist(e -> {});
            assertThat(next).isInstanceOf(ColumnTemplateBuilder.PrePersistStep.class);
        }

        @Test
        @DisplayName("withPostPersist() on PrePersistStep should advance to PostPersistStep")
        void shouldAdvanceToPostPersistStep() {
            ColumnTemplateBuilder next = ColumnTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager)
                    .withPrePersist(e -> {})
                    .withPostPersist(e -> {});
            assertThat(next).isInstanceOf(ColumnTemplateBuilder.PostPersistStep.class);
        }

        @Test
        @DisplayName("null pre-persist consumer on ManagerStep.withPrePersist() should silently use no-op")
        void shouldAcceptNullPrePersistConsumerOnManagerStep() {
            ColumnTemplateBuilder.PrePersistStep step = ColumnTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager)
                    .withPrePersist(null);
            assertThat(step).isNotNull();
            assertThat(step.prePersist()).isNotNull();
        }

        @Test
        @DisplayName("null post-persist consumer on PrePersistStep.withPostPersist() should silently use no-op")
        void shouldAcceptNullPostPersistConsumerOnPrePersistStep() {
            ColumnTemplateBuilder.PostPersistStep step = ColumnTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entities)
                    .withManager(manager)
                    .withPrePersist(e -> {})
                    .withPostPersist(null);
            assertThat(step).isNotNull();
            assertThat(step.postPersist()).isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // Functional tests — uses CDI to get real EntitiesMetadata and Converters
    // -----------------------------------------------------------------------

    @Nested
    @EnableAutoWeld
    @AddPackages(value = {Converters.class, EntityConverter.class})
    @AddPackages(MockProducer.class)
    @AddPackages(Reflections.class)
    @AddExtensions({ReflectionEntityMetadataExtension.class, ColumnExtension.class})
    @DisplayName("Functional tests (with real EntitiesMetadata and Converters via CDI)")
    class FunctionalTests {

        @Inject
        EntitiesMetadata entitiesMetadata;

        @Inject
        Converters converters;

        @Test
        @DisplayName("build() after withManager() should return a non-null ColumnTemplate")
        void shouldBuildColumnTemplate() {
            DatabaseManager manager = mock(DatabaseManager.class);

            ColumnTemplate template = ColumnTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entitiesMetadata)
                    .withManager(manager)
                    .build();

            assertThat(template).isNotNull();
        }

        @Test
        @DisplayName("build() result should implement ColumnTemplate")
        void shouldReturnInstanceOfColumnTemplate() {
            DatabaseManager manager = mock(DatabaseManager.class);

            Object result = ColumnTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entitiesMetadata)
                    .withManager(manager)
                    .build();

            assertThat(result).isInstanceOf(ColumnTemplate.class);
        }

        @Test
        @DisplayName("build() without event consumers should not throw when insert is called")
        void shouldBuildWithNoOpConsumers() {
            DatabaseManager manager = mock(DatabaseManager.class);
            org.eclipse.jnosql.communication.semistructured.CommunicationEntity entity =
                    org.eclipse.jnosql.communication.semistructured.CommunicationEntity.of("Person");
            org.mockito.Mockito.when(manager.insert(org.mockito.Mockito.any(
                    org.eclipse.jnosql.communication.semistructured.CommunicationEntity.class))).thenReturn(entity);

            ColumnTemplate template = ColumnTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entitiesMetadata)
                    .withManager(manager)
                    .build();

            Person person = Person.builder().withId(1L).withName("Ada").withAge(30).build();

            org.assertj.core.api.Assertions.assertThatCode(() -> template.insert(person))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("pre-persist consumer should be invoked when an entity is inserted")
        void shouldInvokePrePersistConsumerOnInsert() {
            DatabaseManager manager = mock(DatabaseManager.class);
            org.eclipse.jnosql.communication.semistructured.CommunicationEntity entity =
                    org.eclipse.jnosql.communication.semistructured.CommunicationEntity.of("Person");
            org.mockito.Mockito.when(manager.insert(org.mockito.Mockito.any(
                    org.eclipse.jnosql.communication.semistructured.CommunicationEntity.class))).thenReturn(entity);

            List<EntityPrePersist> captured = new ArrayList<>();

            ColumnTemplate template = ColumnTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entitiesMetadata)
                    .withManager(manager)
                    .withPrePersist(captured::add)
                    .build();

            Person person = Person.builder().withId(2L).withName("Grace").withAge(40).build();
            template.insert(person);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(captured)
                        .as("pre-persist consumer should have been invoked once")
                        .hasSize(1);
                softly.assertThat(captured.get(0).get())
                        .as("captured entity should be the inserted person")
                        .isSameAs(person);
            });
        }

        @Test
        @DisplayName("ManagerStep.build() and PostPersistStep.build() should each return a distinct ColumnTemplate")
        void shouldReturnDistinctInstancesOnEachBuild() {
            DatabaseManager manager = mock(DatabaseManager.class);

            ColumnTemplateBuilder.ManagerStep step = ColumnTemplateBuilder.builder()
                    .withConverters(converters)
                    .withEntities(entitiesMetadata)
                    .withManager(manager);

            ColumnTemplate first = step.build();
            ColumnTemplate second = step.build();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(first).as("first build result should not be null").isNotNull();
                softly.assertThat(second).as("second build result should not be null").isNotNull();
                softly.assertThat(first).as("each build() call should return a new instance").isNotSameAs(second);
            });
        }
    }
}
