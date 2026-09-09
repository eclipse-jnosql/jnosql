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
package org.eclipse.jnosql.mapping.timeseries.query;

import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.timeseries.TimeSeriesTemplate;
import org.eclipse.jnosql.mapping.timeseries.MockProducer;
import org.eclipse.jnosql.mapping.timeseries.entities.Person;
import org.eclipse.jnosql.mapping.timeseries.entities.PersonRepository;
import org.eclipse.jnosql.mapping.timeseries.spi.TimeSeriesExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.query.SemiStructuredRepositoryProxy;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class, SemiStructuredRepositoryProxy.class})
@AddPackages({MockProducer.class, TimeSeriesTemplate.class, Reflections.class})
@AddExtensions({ReflectionEntityMetadataExtension.class, TimeSeriesExtension.class})
@DisplayName("TimeSeries repository extension")
class TimeSeriesRepositoryExtensionTest {

    @Inject
    @Database(value = DatabaseType.TIME_SERIES)
    private PersonRepository repository;

    @Inject
    @Database(value = DatabaseType.TIME_SERIES, provider = "timeseriesRepositoryMock")
    private PersonRepository repositoryMock;

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
}
