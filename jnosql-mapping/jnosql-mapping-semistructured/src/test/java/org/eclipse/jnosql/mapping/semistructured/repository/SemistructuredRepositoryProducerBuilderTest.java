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
package org.eclipse.jnosql.mapping.semistructured.repository;

import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.core.repository.RepositoryOperationProvider;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class SemistructuredRepositoryProducerBuilderTest {

    // -----------------------------------------------------------------------
    // Structural / immutability tests — pure unit tests, no CDI
    // -----------------------------------------------------------------------

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("Builder structure and immutability (no CDI)")
    class StructureTests {

        @Mock
        EntitiesMetadata entities;

        @Mock
        EntitiesMetadata otherEntities;

        @Mock
        RepositoriesMetadata repositories;

        @Mock
        RepositoriesMetadata otherRepositories;

        @Mock
        InfrastructureOperatorProvider infrastructure;

        @Mock
        InfrastructureOperatorProvider otherInfrastructure;

        @Mock
        RepositoryOperationProvider operations;

        @Mock
        RepositoryOperationProvider otherOperations;

        @Test
        @DisplayName("builder() should return an EntitiesStep instance")
        void shouldReturnEntitiesStepFromBuilder() {
            SemistructuredRepositoryProducerBuilder builder = SemistructuredRepositoryProducerBuilder.builder();
            assertThat(builder).isInstanceOf(SemistructuredRepositoryProducerBuilder.EntitiesStep.class);
        }

        @Test
        @DisplayName("withEntities() on EntitiesStep should return a new step without modifying the original")
        void shouldBeImmutableOnEntitiesStep() {
            SemistructuredRepositoryProducerBuilder.EntitiesStep original =
                    SemistructuredRepositoryProducerBuilder.builder().withEntities(entities);
            SemistructuredRepositoryProducerBuilder.EntitiesStep updated = original.withEntities(otherEntities);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(original.entities())
                        .as("original step should still hold the first entities")
                        .isSameAs(entities);
                softly.assertThat(updated.entities())
                        .as("updated step should hold the other entities")
                        .isSameAs(otherEntities);
                softly.assertThat(original).as("original and updated should be different instances")
                        .isNotSameAs(updated);
            });
        }

        @Test
        @DisplayName("withRepositories() on EntitiesStep should advance to RepositoriesStep")
        void shouldAdvanceToRepositoriesStep() {
            SemistructuredRepositoryProducerBuilder next =
                    SemistructuredRepositoryProducerBuilder.builder()
                            .withEntities(entities)
                            .withRepositories(repositories);
            assertThat(next).isInstanceOf(SemistructuredRepositoryProducerBuilder.RepositoriesStep.class);
        }

        @Test
        @DisplayName("withEntities() on RepositoriesStep should return a new EntitiesStep")
        void shouldResetToEntitiesStepFromRepositoriesStep() {
            SemistructuredRepositoryProducerBuilder.RepositoriesStep step =
                    SemistructuredRepositoryProducerBuilder.builder()
                            .withEntities(entities)
                            .withRepositories(repositories);
            SemistructuredRepositoryProducerBuilder.EntitiesStep reset = step.withEntities(otherEntities);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(reset).as("should be an EntitiesStep")
                        .isInstanceOf(SemistructuredRepositoryProducerBuilder.EntitiesStep.class);
                softly.assertThat(reset.entities())
                        .as("reset step should hold the new entities")
                        .isSameAs(otherEntities);
            });
        }

        @Test
        @DisplayName("withRepositories() on RepositoriesStep should return a new step without modifying the original")
        void shouldBeImmutableOnRepositoriesStep() {
            SemistructuredRepositoryProducerBuilder.RepositoriesStep original =
                    SemistructuredRepositoryProducerBuilder.builder()
                            .withEntities(entities)
                            .withRepositories(repositories);
            SemistructuredRepositoryProducerBuilder.RepositoriesStep updated = original.withRepositories(otherRepositories);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(original.repositories())
                        .as("original step should still hold the first repositories")
                        .isSameAs(repositories);
                softly.assertThat(updated.repositories())
                        .as("updated step should hold the other repositories")
                        .isSameAs(otherRepositories);
                softly.assertThat(original.entities())
                        .as("entities should be preserved in both")
                        .isSameAs(updated.entities());
                softly.assertThat(original).as("original and updated should be different instances")
                        .isNotSameAs(updated);
            });
        }

        @Test
        @DisplayName("withInfrastructure() on RepositoriesStep should advance to InfraStep")
        void shouldAdvanceToInfraStep() {
            SemistructuredRepositoryProducerBuilder next =
                    SemistructuredRepositoryProducerBuilder.builder()
                            .withEntities(entities)
                            .withRepositories(repositories)
                            .withInfrastructure(infrastructure);
            assertThat(next).isInstanceOf(SemistructuredRepositoryProducerBuilder.InfraStep.class);
        }

        @Test
        @DisplayName("withEntities() on InfraStep should reset to EntitiesStep")
        void shouldResetToEntitiesStepFromInfraStep() {
            SemistructuredRepositoryProducerBuilder.InfraStep step =
                    SemistructuredRepositoryProducerBuilder.builder()
                            .withEntities(entities)
                            .withRepositories(repositories)
                            .withInfrastructure(infrastructure);
            SemistructuredRepositoryProducerBuilder.EntitiesStep reset = step.withEntities(otherEntities);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(reset).as("should be an EntitiesStep")
                        .isInstanceOf(SemistructuredRepositoryProducerBuilder.EntitiesStep.class);
                softly.assertThat(reset.entities())
                        .as("reset step should hold the new entities")
                        .isSameAs(otherEntities);
            });
        }

        @Test
        @DisplayName("withRepositories() on InfraStep should reset to RepositoriesStep")
        void shouldResetToRepositoriesStepFromInfraStep() {
            SemistructuredRepositoryProducerBuilder.InfraStep step =
                    SemistructuredRepositoryProducerBuilder.builder()
                            .withEntities(entities)
                            .withRepositories(repositories)
                            .withInfrastructure(infrastructure);
            SemistructuredRepositoryProducerBuilder.RepositoriesStep reset = step.withRepositories(otherRepositories);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(reset).as("should be a RepositoriesStep")
                        .isInstanceOf(SemistructuredRepositoryProducerBuilder.RepositoriesStep.class);
                softly.assertThat(reset.repositories())
                        .as("reset step should hold the new repositories")
                        .isSameAs(otherRepositories);
                softly.assertThat(reset.entities())
                        .as("entities should be preserved")
                        .isSameAs(entities);
            });
        }

        @Test
        @DisplayName("withInfrastructure() on InfraStep should return a new step without modifying the original")
        void shouldBeImmutableOnInfraStep() {
            SemistructuredRepositoryProducerBuilder.InfraStep original =
                    SemistructuredRepositoryProducerBuilder.builder()
                            .withEntities(entities)
                            .withRepositories(repositories)
                            .withInfrastructure(infrastructure);
            SemistructuredRepositoryProducerBuilder.InfraStep updated = original.withInfrastructure(otherInfrastructure);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(original.infrastructure())
                        .as("original step should still hold the first infrastructure")
                        .isSameAs(infrastructure);
                softly.assertThat(updated.infrastructure())
                        .as("updated step should hold the other infrastructure")
                        .isSameAs(otherInfrastructure);
                softly.assertThat(original).as("original and updated should be different instances")
                        .isNotSameAs(updated);
            });
        }

        @Test
        @DisplayName("withOperations() on InfraStep should advance to OperationsStep")
        void shouldAdvanceToOperationsStep() {
            SemistructuredRepositoryProducerBuilder next =
                    SemistructuredRepositoryProducerBuilder.builder()
                            .withEntities(entities)
                            .withRepositories(repositories)
                            .withInfrastructure(infrastructure)
                            .withOperations(operations);
            assertThat(next).isInstanceOf(SemistructuredRepositoryProducerBuilder.OperationsStep.class);
        }

        @Test
        @DisplayName("withEntities() on OperationsStep should reset to EntitiesStep")
        void shouldResetToEntitiesStepFromOperationsStep() {
            SemistructuredRepositoryProducerBuilder.OperationsStep step =
                    SemistructuredRepositoryProducerBuilder.builder()
                            .withEntities(entities)
                            .withRepositories(repositories)
                            .withInfrastructure(infrastructure)
                            .withOperations(operations);
            SemistructuredRepositoryProducerBuilder.EntitiesStep reset = step.withEntities(otherEntities);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(reset).as("should be an EntitiesStep")
                        .isInstanceOf(SemistructuredRepositoryProducerBuilder.EntitiesStep.class);
                softly.assertThat(reset.entities())
                        .as("reset step should hold the new entities")
                        .isSameAs(otherEntities);
            });
        }

        @Test
        @DisplayName("withRepositories() on OperationsStep should reset to RepositoriesStep")
        void shouldResetToRepositoriesStepFromOperationsStep() {
            SemistructuredRepositoryProducerBuilder.OperationsStep step =
                    SemistructuredRepositoryProducerBuilder.builder()
                            .withEntities(entities)
                            .withRepositories(repositories)
                            .withInfrastructure(infrastructure)
                            .withOperations(operations);
            SemistructuredRepositoryProducerBuilder.RepositoriesStep reset = step.withRepositories(otherRepositories);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(reset).as("should be a RepositoriesStep")
                        .isInstanceOf(SemistructuredRepositoryProducerBuilder.RepositoriesStep.class);
                softly.assertThat(reset.repositories())
                        .as("reset step should hold the new repositories")
                        .isSameAs(otherRepositories);
                softly.assertThat(reset.entities())
                        .as("entities should be preserved")
                        .isSameAs(entities);
            });
        }

        @Test
        @DisplayName("withInfrastructure() on OperationsStep should reset to InfraStep")
        void shouldResetToInfraStepFromOperationsStep() {
            SemistructuredRepositoryProducerBuilder.OperationsStep step =
                    SemistructuredRepositoryProducerBuilder.builder()
                            .withEntities(entities)
                            .withRepositories(repositories)
                            .withInfrastructure(infrastructure)
                            .withOperations(operations);
            SemistructuredRepositoryProducerBuilder.InfraStep reset = step.withInfrastructure(otherInfrastructure);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(reset).as("should be an InfraStep")
                        .isInstanceOf(SemistructuredRepositoryProducerBuilder.InfraStep.class);
                softly.assertThat(reset.infrastructure())
                        .as("reset step should hold the new infrastructure")
                        .isSameAs(otherInfrastructure);
                softly.assertThat(reset.entities())
                        .as("entities should be preserved")
                        .isSameAs(entities);
                softly.assertThat(reset.repositories())
                        .as("repositories should be preserved")
                        .isSameAs(repositories);
            });
        }

        @Test
        @DisplayName("withOperations() on OperationsStep should return a new step without modifying the original")
        void shouldBeImmutableOnOperationsStep() {
            SemistructuredRepositoryProducerBuilder.OperationsStep original =
                    SemistructuredRepositoryProducerBuilder.builder()
                            .withEntities(entities)
                            .withRepositories(repositories)
                            .withInfrastructure(infrastructure)
                            .withOperations(operations);
            SemistructuredRepositoryProducerBuilder.OperationsStep updated = original.withOperations(otherOperations);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(original.operations())
                        .as("original step should still hold the first operations")
                        .isSameAs(operations);
                softly.assertThat(updated.operations())
                        .as("updated step should hold the other operations")
                        .isSameAs(otherOperations);
                softly.assertThat(original).as("original and updated should be different instances")
                        .isNotSameAs(updated);
            });
        }

        // -------------------------------------------------------------------
        // Null checks
        // -------------------------------------------------------------------

        @Test
        @DisplayName("null entities should throw NullPointerException on withEntities() at EntitiesStep")
        void shouldThrowOnNullEntitiesAtEntitiesStep() {
            assertThatNullPointerException()
                    .isThrownBy(() -> SemistructuredRepositoryProducerBuilder.builder().withEntities(null))
                    .withMessage("entities is required");
        }

        @Test
        @DisplayName("null repositories should throw NullPointerException on withRepositories()")
        void shouldThrowOnNullRepositories() {
            SemistructuredRepositoryProducerBuilder.EntitiesStep step =
                    SemistructuredRepositoryProducerBuilder.builder().withEntities(entities);
            assertThatNullPointerException()
                    .isThrownBy(() -> step.withRepositories(null))
                    .withMessage("repositories is required");
        }

        @Test
        @DisplayName("null infrastructure should throw NullPointerException on withInfrastructure()")
        void shouldThrowOnNullInfrastructure() {
            SemistructuredRepositoryProducerBuilder.RepositoriesStep step =
                    SemistructuredRepositoryProducerBuilder.builder()
                            .withEntities(entities)
                            .withRepositories(repositories);
            assertThatNullPointerException()
                    .isThrownBy(() -> step.withInfrastructure(null))
                    .withMessage("infrastructure is required");
        }

        @Test
        @DisplayName("null operations should throw NullPointerException on withOperations()")
        void shouldThrowOnNullOperations() {
            SemistructuredRepositoryProducerBuilder.InfraStep step =
                    SemistructuredRepositoryProducerBuilder.builder()
                            .withEntities(entities)
                            .withRepositories(repositories)
                            .withInfrastructure(infrastructure);
            assertThatNullPointerException()
                    .isThrownBy(() -> step.withOperations(null))
                    .withMessage("operations is required");
        }

        @Test
        @DisplayName("null entities on RepositoriesStep.withEntities() should throw NullPointerException")
        void shouldThrowOnNullEntitiesAtRepositoriesStep() {
            SemistructuredRepositoryProducerBuilder.RepositoriesStep step =
                    SemistructuredRepositoryProducerBuilder.builder()
                            .withEntities(entities)
                            .withRepositories(repositories);
            assertThatNullPointerException()
                    .isThrownBy(() -> step.withEntities(null))
                    .withMessage("entities is required");
        }

        @Test
        @DisplayName("null entities on InfraStep.withEntities() should throw NullPointerException")
        void shouldThrowOnNullEntitiesAtInfraStep() {
            SemistructuredRepositoryProducerBuilder.InfraStep step =
                    SemistructuredRepositoryProducerBuilder.builder()
                            .withEntities(entities)
                            .withRepositories(repositories)
                            .withInfrastructure(infrastructure);
            assertThatNullPointerException()
                    .isThrownBy(() -> step.withEntities(null))
                    .withMessage("entities is required");
        }

        @Test
        @DisplayName("null entities on OperationsStep.withEntities() should throw NullPointerException")
        void shouldThrowOnNullEntitiesAtOperationsStep() {
            SemistructuredRepositoryProducerBuilder.OperationsStep step =
                    SemistructuredRepositoryProducerBuilder.builder()
                            .withEntities(entities)
                            .withRepositories(repositories)
                            .withInfrastructure(infrastructure)
                            .withOperations(operations);
            assertThatNullPointerException()
                    .isThrownBy(() -> step.withEntities(null))
                    .withMessage("entities is required");
        }
    }

    // -----------------------------------------------------------------------
    // Functional tests — uses CDI to get real dependencies
    // -----------------------------------------------------------------------

    @Nested
    @EnableAutoWeld
    @AddPackages(value = {Converters.class, EntityConverter.class})
    @AddPackages(MockProducer.class)
    @AddPackages(Reflections.class)
    @AddExtensions({ReflectionEntityMetadataExtension.class})
    @ExtendWith(MockitoExtension.class)
    @DisplayName("Builder functional tests with real CDI dependencies")
    class FunctionalTests {

        @Inject
        private EntitiesMetadata entities;

        @Inject
        private RepositoriesMetadata repositoriesMetadata;

        @Inject
        private InfrastructureOperatorProvider infrastructureOperatorProvider;

        @Inject
        private RepositoryOperationProvider repositoryOperationProvider;

        @Test
        @DisplayName("should build a SemistructuredRepositoryProducer with all dependencies")
        void shouldBuildProducerWithAllDependencies() {
            SemistructuredRepositoryProducer producer = SemistructuredRepositoryProducerBuilder.builder()
                    .withEntities(entities)
                    .withRepositories(repositoriesMetadata)
                    .withInfrastructure(infrastructureOperatorProvider)
                    .withOperations(repositoryOperationProvider)
                    .build();

            assertThat(producer).isNotNull()
                    .isInstanceOf(SemistructuredRepositoryProducer.class);
        }

        @Test
        @DisplayName("should build a SemistructuredRepositoryProducer usable outside CDI")
        void shouldBuildProducerOutsideCDI() {
            SemistructuredRepositoryProducer producer = SemistructuredRepositoryProducerBuilder.builder()
                    .withEntities(entities)
                    .withRepositories(repositoriesMetadata)
                    .withInfrastructure(infrastructureOperatorProvider)
                    .withOperations(repositoryOperationProvider)
                    .build();

            assertThat(producer).isNotNull();
        }
    }
}
