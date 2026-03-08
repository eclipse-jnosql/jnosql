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

import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EntityConverterFactoryBuilderTest {

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
        Converters converters;

        @Mock
        Converters otherConverters;

        @Test
        @DisplayName("builder() should return an EntitiesStep instance")
        void shouldReturnEntitiesStepFromBuilder() {
            EntityConverterFactoryBuilder builder = EntityConverterFactoryBuilder.builder();
            assertThat(builder).isInstanceOf(EntityConverterFactoryBuilder.EntitiesStep.class);
        }

        @Test
        @DisplayName("withEntities() on EntitiesStep should return a new step without modifying the original")
        void shouldBeImmutableOnEntitiesStep() {
            EntityConverterFactoryBuilder.EntitiesStep original = EntityConverterFactoryBuilder.builder()
                    .withEntities(entities);
            EntityConverterFactoryBuilder.EntitiesStep updated = original.withEntities(otherEntities);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(original.entities())
                        .as("original step should still hold the first entities metadata")
                        .isSameAs(entities);
                softly.assertThat(updated.entities())
                        .as("updated step should hold the second entities metadata")
                        .isSameAs(otherEntities);
                softly.assertThat(original).as("original and updated should be different instances")
                        .isNotSameAs(updated);
            });
        }

        @Test
        @DisplayName("withConverters() on EntitiesStep should advance to ConvertersStep")
        void shouldAdvanceToConvertersStep() {
            EntityConverterFactoryBuilder next = EntityConverterFactoryBuilder.builder()
                    .withEntities(entities)
                    .withConverters(converters);
            assertThat(next).isInstanceOf(EntityConverterFactoryBuilder.ConvertersStep.class);
        }

        @Test
        @DisplayName("withEntities() on ConvertersStep should return a new step without modifying the original")
        void shouldBeImmutableOnConvertersStepEntities() {
            EntityConverterFactoryBuilder.ConvertersStep original = EntityConverterFactoryBuilder.builder()
                    .withEntities(entities)
                    .withConverters(converters);
            EntityConverterFactoryBuilder.ConvertersStep updated = original.withEntities(otherEntities);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(original.entities())
                        .as("original step should still hold the first entities metadata")
                        .isSameAs(entities);
                softly.assertThat(updated.entities())
                        .as("updated step should hold the other entities metadata")
                        .isSameAs(otherEntities);
                softly.assertThat(original.converters())
                        .as("converters should be unchanged in both")
                        .isSameAs(updated.converters());
                softly.assertThat(original).as("original and updated should be different instances")
                        .isNotSameAs(updated);
            });
        }

        @Test
        @DisplayName("withConverters() on ConvertersStep should return a new step without modifying the original")
        void shouldBeImmutableOnConvertersStepConverters() {
            EntityConverterFactoryBuilder.ConvertersStep original = EntityConverterFactoryBuilder.builder()
                    .withEntities(entities)
                    .withConverters(converters);
            EntityConverterFactoryBuilder.ConvertersStep updated = original.withConverters(otherConverters);

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
        @DisplayName("build() should return a non-null EntityConverterFactory")
        void shouldBuildEntityConverterFactory() {
            EntityConverterFactory factory = EntityConverterFactoryBuilder.builder()
                    .withEntities(entities)
                    .withConverters(converters)
                    .build();
            assertThat(factory).isNotNull();
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
    @AddExtensions(ReflectionEntityMetadataExtension.class)
    @DisplayName("Functional tests (with real EntitiesMetadata and Converters via CDI)")
    class FunctionalTests {

        private static final String ID = "~id";

        @Inject
        EntitiesMetadata entitiesMetadata;

        @Inject
        Converters converters;

        @Test
        @DisplayName("build() should produce an EntityConverterFactory that creates a functional EntityConverter")
        void shouldProduceFunctionalEntityConverter() {
            EntityConverterFactory factory = EntityConverterFactoryBuilder.builder()
                    .withEntities(entitiesMetadata)
                    .withConverters(converters)
                    .build();

            assertThat(factory).isNotNull();

            EntityConverter converter = factory.create(() -> Optional.of(ID));
            assertThat(converter).isNotNull();

            Person person = Person.builder()
                    .id(42)
                    .name("Ada")
                    .age()
                    .phones(Arrays.asList("111", "222"))
                    .build();

            CommunicationEntity communication = converter.toCommunication(person);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(communication).as("communication entity should not be null").isNotNull();
                softly.assertThat(communication.name()).as("entity name should be 'Person'").isEqualTo("Person");
                softly.assertThat(communication.find(ID).map(Element::get).orElse(null))
                        .as("~id field should be 42L")
                        .isEqualTo(42L);
                softly.assertThat(communication.find("name").map(Element::get).orElse(null))
                        .as("name field should be 'Ada'")
                        .isEqualTo("Ada");
            });
        }

        @Test
        @DisplayName("ConvertersStep should allow re-configuration and produce a new factory each time")
        void shouldAllowReconfiguration() {
            EntityConverterFactoryBuilder.ConvertersStep step = EntityConverterFactoryBuilder.builder()
                    .withEntities(entitiesMetadata)
                    .withConverters(converters);

            EntityConverterFactory factory1 = step.build();
            EntityConverterFactory factory2 = step.withConverters(converters).build();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(factory1).as("first factory should not be null").isNotNull();
                softly.assertThat(factory2).as("second factory should not be null").isNotNull();
                softly.assertThat(factory1).as("each build() call should return a new instance").isNotSameAs(factory2);
            });
        }
    }
}
