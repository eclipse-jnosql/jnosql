/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.timeseries.spi;

import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.timeseries.TimeSeriesTemplate;
import org.eclipse.jnosql.mapping.timeseries.MockProducer;
import org.eclipse.jnosql.mapping.timeseries.entities.Person;
import org.eclipse.jnosql.mapping.timeseries.entities.PersonRepository;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;


@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class, TimeSeriesTemplate.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, TimeSeriesExtension.class})
@DisplayName("TimeSeries extension")
class TimeSeriesExtensionTest {


    @Inject
    @Database(value = DatabaseType.TIME_SERIES)
    private PersonRepository repository;

    @Inject
    @Database(value = DatabaseType.TIME_SERIES, provider = "timeseriesRepositoryMock")
    private PersonRepository repositoryMock;

    @Inject
    @Database(value = DatabaseType.TIME_SERIES, provider = "timeseriesRepositoryMock")
    private TimeSeriesTemplate templateMock;

    @Inject
    private TimeSeriesTemplate template;

    @Nested
    @DisplayName("When injecting time-series repositories")
    class WhenTheRepositoryInjection {

        @Test
        @DisplayName("Should inject the default repository")
        void shouldInjectDefaultRepository() {

            // When
            Person person = repository.save(Person.builder().build());

            // Then
            assertSoftly(softly -> {
                softly.assertThat(repository).isNotNull();
                softly.assertThat(person.getName()).isEqualTo("Default");
            });
        }

        @Test
        @DisplayName("Should inject the provider-specific repository")
        void shouldInjectProviderRepository() {

            // When
            Person person = repositoryMock.save(Person.builder().build());

            // Then
            assertSoftly(softly -> {
                softly.assertThat(repositoryMock).isNotNull();
                softly.assertThat(person.getName()).isEqualTo("timeseriesRepositoryMock");
            });
        }
    }

    @Nested
    @DisplayName("When injecting time-series templates")
    class WhenTheTemplateInjection {

        @Test
        @DisplayName("Should inject default and provider-specific templates")
        void shouldInjectTemplates() {

            // Then
            assertSoftly(softly -> {
                softly.assertThat(templateMock).isNotNull();
                softly.assertThat(template).isNotNull();
            });
        }
    }

    @Nested
    @DisplayName("When resolving time-series repositories")
    class WhenTheRepositoryResolution {

        @Test
        @DisplayName("Should expose default and provider-specific repositories")
        void shouldExposeRepositories() {

            // Then
            assertThat(repository).isNotNull();
            assertThat(repositoryMock).isNotNull();
        }
    }
}
