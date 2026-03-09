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

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.graph.GraphDatabaseManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.EntityPostPersist;
import org.eclipse.jnosql.mapping.EntityPrePersist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class GraphTemplateBuilderTest {

    static class Person {
        final String name;

        Person(String name) {
            this.name = name;
        }
    }

    private Converters converters;
    private EntitiesMetadata entities;
    private GraphDatabaseManager manager;

    @BeforeEach
    void setUp() {
        converters = Mockito.mock(Converters.class);
        entities = Mockito.mock(EntitiesMetadata.class);
        manager = Mockito.mock(GraphDatabaseManager.class);
    }

    // --- builder() ---

    @Test
    @DisplayName("builder() should return a ConvertersStep instance")
    void shouldReturnConvertersStepFromBuilder() {
        GraphTemplateBuilder builder = GraphTemplateBuilder.builder();
        assertThat(builder).isInstanceOf(GraphTemplateBuilder.ConvertersStep.class);
    }

    // --- Step sequencing ---

    @Test
    @DisplayName("Steps should advance in order: ConvertersStep → EntitiesStep → ManagerStep")
    void shouldAdvanceThroughStepsInOrder() {
        var convertersStep = GraphTemplateBuilder.builder().withConverters(converters);
        var entitiesStep = convertersStep.withEntities(entities);
        var managerStep = entitiesStep.withManager(manager);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(convertersStep)
                    .as("withConverters() should return ConvertersStep")
                    .isInstanceOf(GraphTemplateBuilder.ConvertersStep.class);
            softly.assertThat(entitiesStep)
                    .as("withEntities() should return EntitiesStep")
                    .isInstanceOf(GraphTemplateBuilder.EntitiesStep.class);
            softly.assertThat(managerStep)
                    .as("withManager() should return ManagerStep")
                    .isInstanceOf(GraphTemplateBuilder.ManagerStep.class);
        });
    }

    @Test
    @DisplayName("ManagerStep.withPrePersist() should return a PrePersistStep")
    void shouldAdvanceToPrePersistStepFromManagerStep() {
        var managerStep = GraphTemplateBuilder.builder()
                .withConverters(converters)
                .withEntities(entities)
                .withManager(manager);
        var nextStep = managerStep.withPrePersist(e -> {});
        assertThat(nextStep).isInstanceOf(GraphTemplateBuilder.PrePersistStep.class);
    }

    @Test
    @DisplayName("PrePersistStep.withPostPersist() should return a PostPersistStep")
    void shouldAdvanceToPostPersistStepFromPrePersistStep() {
        var prePersistStep = GraphTemplateBuilder.builder()
                .withConverters(converters)
                .withEntities(entities)
                .withManager(manager)
                .withPrePersist(e -> {});
        var nextStep = prePersistStep.withPostPersist(e -> {});
        assertThat(nextStep).isInstanceOf(GraphTemplateBuilder.PostPersistStep.class);
    }

    // --- build() returns GraphTemplate ---

    @Test
    @DisplayName("build() from ManagerStep should return a GraphTemplate instance (no-op consumers)")
    void shouldBuildFromManagerStep() {
        GraphTemplate template = GraphTemplateBuilder.builder()
                .withConverters(converters)
                .withEntities(entities)
                .withManager(manager)
                .build();
        assertThat(template).isNotNull().isInstanceOf(GraphTemplate.class);
    }

    @Test
    @DisplayName("build() from PrePersistStep should return a GraphTemplate instance (no-op postPersist)")
    void shouldBuildFromPrePersistStep() {
        GraphTemplate template = GraphTemplateBuilder.builder()
                .withConverters(converters)
                .withEntities(entities)
                .withManager(manager)
                .withPrePersist(e -> {})
                .build();
        assertThat(template).isNotNull().isInstanceOf(GraphTemplate.class);
    }

    @Test
    @DisplayName("build() from PostPersistStep should return a GraphTemplate instance (all consumers)")
    void shouldBuildFromPostPersistStep() {
        GraphTemplate template = GraphTemplateBuilder.builder()
                .withConverters(converters)
                .withEntities(entities)
                .withManager(manager)
                .withPrePersist(e -> {})
                .withPostPersist(e -> {})
                .build();
        assertThat(template).isNotNull().isInstanceOf(GraphTemplate.class);
    }

    // --- null guards ---

    @Test
    @DisplayName("withConverters() should throw NullPointerException when null")
    void shouldThrowWhenConvertersIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> GraphTemplateBuilder.builder().withConverters(null))
                .withMessage("converters is required");
    }

    @Test
    @DisplayName("withEntities() should throw NullPointerException when null")
    void shouldThrowWhenEntitiesIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> GraphTemplateBuilder.builder()
                        .withConverters(converters)
                        .withEntities(null))
                .withMessage("entities is required");
    }

    @Test
    @DisplayName("withManager() should throw NullPointerException when null")
    void shouldThrowWhenManagerIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> GraphTemplateBuilder.builder()
                        .withConverters(converters)
                        .withEntities(entities)
                        .withManager(null))
                .withMessage("manager is required");
    }

    @Test
    @DisplayName("withPrePersist() should throw NullPointerException when null")
    void shouldThrowWhenPrePersistIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> GraphTemplateBuilder.builder()
                        .withConverters(converters)
                        .withEntities(entities)
                        .withManager(manager)
                        .withPrePersist(null))
                .withMessage("prePersist is required");
    }

    @Test
    @DisplayName("withPostPersist() should throw NullPointerException when null")
    void shouldThrowWhenPostPersistIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> GraphTemplateBuilder.builder()
                        .withConverters(converters)
                        .withEntities(entities)
                        .withManager(manager)
                        .withPrePersist(e -> {})
                        .withPostPersist(null))
                .withMessage("postPersist is required");
    }

    @Test
    @DisplayName("build() should throw NullPointerException when converters is null in PostPersistStep")
    void shouldThrowWhenConvertersIsNullInBuild() {
        var step = new GraphTemplateBuilder.PostPersistStep(null, entities, manager, e -> {}, e -> {});
        assertThatNullPointerException()
                .isThrownBy(step::build)
                .withMessage("converters is required");
    }

    @Test
    @DisplayName("build() should throw NullPointerException when entities is null in PostPersistStep")
    void shouldThrowWhenEntitiesIsNullInBuild() {
        var step = new GraphTemplateBuilder.PostPersistStep(converters, null, manager, e -> {}, e -> {});
        assertThatNullPointerException()
                .isThrownBy(step::build)
                .withMessage("entities is required");
    }

    @Test
    @DisplayName("build() should throw NullPointerException when manager is null in PostPersistStep")
    void shouldThrowWhenManagerIsNullInBuild() {
        var step = new GraphTemplateBuilder.PostPersistStep(converters, entities, null, e -> {}, e -> {});
        assertThatNullPointerException()
                .isThrownBy(step::build)
                .withMessage("manager is required");
    }

    // --- immutability ---

    @Test
    @DisplayName("ConvertersStep should be immutable: withConverters() returns new instance")
    void shouldBeImmutableOnConvertersStep() {
        Converters first = Mockito.mock(Converters.class);
        Converters second = Mockito.mock(Converters.class);

        GraphTemplateBuilder.ConvertersStep original = GraphTemplateBuilder.builder().withConverters(first);
        GraphTemplateBuilder.ConvertersStep updated = original.withConverters(second);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(original.converters())
                    .as("original should still hold first converters")
                    .isSameAs(first);
            softly.assertThat(updated.converters())
                    .as("updated should hold second converters")
                    .isSameAs(second);
            softly.assertThat(original)
                    .as("original and updated must be different instances")
                    .isNotSameAs(updated);
        });
    }

    @Test
    @DisplayName("EntitiesStep should be immutable: withConverters() and withEntities() return new instances")
    void shouldBeImmutableOnEntitiesStep() {
        Converters first = Mockito.mock(Converters.class);
        Converters second = Mockito.mock(Converters.class);
        EntitiesMetadata firstMeta = Mockito.mock(EntitiesMetadata.class);
        EntitiesMetadata secondMeta = Mockito.mock(EntitiesMetadata.class);

        GraphTemplateBuilder.EntitiesStep original = GraphTemplateBuilder.builder()
                .withConverters(first)
                .withEntities(firstMeta);
        GraphTemplateBuilder.EntitiesStep updatedConv = original.withConverters(second);
        GraphTemplateBuilder.EntitiesStep updatedEnt = original.withEntities(secondMeta);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(original.converters())
                    .as("original converters unchanged").isSameAs(first);
            softly.assertThat(updatedConv.converters())
                    .as("updated converters applied").isSameAs(second);
            softly.assertThat(original.entities())
                    .as("original entities unchanged").isSameAs(firstMeta);
            softly.assertThat(updatedEnt.entities())
                    .as("updated entities applied").isSameAs(secondMeta);
            softly.assertThat(original).isNotSameAs(updatedConv);
            softly.assertThat(original).isNotSameAs(updatedEnt);
        });
    }

    @Test
    @DisplayName("ManagerStep should be immutable: withManager() returns new instance")
    void shouldBeImmutableOnManagerStep() {
        GraphDatabaseManager first = Mockito.mock(GraphDatabaseManager.class);
        GraphDatabaseManager second = Mockito.mock(GraphDatabaseManager.class);

        GraphTemplateBuilder.ManagerStep original = GraphTemplateBuilder.builder()
                .withConverters(converters)
                .withEntities(entities)
                .withManager(first);
        GraphTemplateBuilder.ManagerStep updated = original.withManager(second);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(original.manager())
                    .as("original should still hold first manager").isSameAs(first);
            softly.assertThat(updated.manager())
                    .as("updated should hold second manager").isSameAs(second);
            softly.assertThat(original).isNotSameAs(updated);
        });
    }

    @Test
    @DisplayName("PrePersistStep should be immutable: withPrePersist() returns new instance")
    void shouldBeImmutableOnPrePersistStep() {
        Consumer<EntityPrePersist> first = e -> {};
        Consumer<EntityPrePersist> second = e -> {};

        GraphTemplateBuilder.PrePersistStep original = GraphTemplateBuilder.builder()
                .withConverters(converters)
                .withEntities(entities)
                .withManager(manager)
                .withPrePersist(first);
        GraphTemplateBuilder.PrePersistStep updated = original.withPrePersist(second);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(original.prePersist())
                    .as("original should still hold first consumer").isSameAs(first);
            softly.assertThat(updated.prePersist())
                    .as("updated should hold second consumer").isSameAs(second);
            softly.assertThat(original).isNotSameAs(updated);
        });
    }

    @Test
    @DisplayName("PostPersistStep should be immutable: withPostPersist() returns new instance")
    void shouldBeImmutableOnPostPersistStep() {
        Consumer<EntityPostPersist> first = e -> {};
        Consumer<EntityPostPersist> second = e -> {};

        GraphTemplateBuilder.PostPersistStep original = GraphTemplateBuilder.builder()
                .withConverters(converters)
                .withEntities(entities)
                .withManager(manager)
                .withPrePersist(e -> {})
                .withPostPersist(first);
        GraphTemplateBuilder.PostPersistStep updated = original.withPostPersist(second);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(original.postPersist())
                    .as("original should still hold first consumer").isSameAs(first);
            softly.assertThat(updated.postPersist())
                    .as("updated should hold second consumer").isSameAs(second);
            softly.assertThat(original).isNotSameAs(updated);
        });
    }

    // --- re-configuration within a step chain ---

    @Test
    @DisplayName("Steps should allow seamless re-configuration via withXxx() in a chain")
    void shouldAllowSeamlessReconfiguration() {
        Converters converters2 = Mockito.mock(Converters.class);
        EntitiesMetadata entities2 = Mockito.mock(EntitiesMetadata.class);
        GraphDatabaseManager manager2 = Mockito.mock(GraphDatabaseManager.class);

        GraphTemplate template = GraphTemplateBuilder.builder()
                .withConverters(converters)
                .withConverters(converters2)
                .withEntities(entities)
                .withEntities(entities2)
                .withManager(manager)
                .withManager(manager2)
                .build();

        assertThat(template).isNotNull().isInstanceOf(GraphTemplate.class);
    }

    // --- no-op default consumers ---

    @Test
    @DisplayName("build() without event consumers should produce a GraphTemplate that does not throw")
    void shouldNotThrowWhenNoEventConsumers() {
        GraphTemplate template = GraphTemplateBuilder.builder()
                .withConverters(converters)
                .withEntities(entities)
                .withManager(manager)
                .build();
        assertThat(template).isNotNull().isInstanceOf(GraphTemplate.class);
    }
}
