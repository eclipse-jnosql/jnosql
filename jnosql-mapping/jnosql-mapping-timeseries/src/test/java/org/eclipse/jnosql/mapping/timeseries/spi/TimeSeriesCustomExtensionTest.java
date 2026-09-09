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
import org.eclipse.jnosql.mapping.timeseries.entities.People;
import org.eclipse.jnosql.mapping.timeseries.entities.Person;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;


@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class, TimeSeriesTemplate.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, TimeSeriesExtension.class})
@DisplayName("TimeSeries custom extension")
class TimeSeriesCustomExtensionTest {

    @Inject
    @Database(value = DatabaseType.TIME_SERIES)
    private People people;

    @Inject
    @Database(value = DatabaseType.TIME_SERIES, provider = "timeseriesRepositoryMock")
    private People pepoleMock;

    @Inject
    private People repository;

    @Nested
    @DisplayName("When injecting custom repositories")
    class WhenTheCustomRepositoryInjection {

        @Test
        @DisplayName("Should inject the default custom repository")
        void shouldInjectDefaultCustomRepository() {

            // When
            Person person = people.insert(Person.builder().build());

            // Then
            assertSoftly(softly -> {
                softly.assertThat(people).isNotNull();
                softly.assertThat(person).isNotNull();
                softly.assertThat(person.getName()).isEqualTo("Default");
            });
        }

        @Test
        @DisplayName("Should inject the provider-specific custom repository")
        void shouldInjectProviderCustomRepository() {

            // When
            Person person = pepoleMock.insert(Person.builder().build());

            // Then
            assertSoftly(softly -> {
                softly.assertThat(pepoleMock).isNotNull();
                softly.assertThat(person).isNotNull();
                softly.assertThat(person.getName()).isEqualTo("timeseriesRepositoryMock");
            });
        }

        @Test
        @DisplayName("Should inject the unqualified custom repository")
        void shouldInjectUnqualifiedCustomRepository() {

            // When
            Person person = repository.insert(Person.builder().build());

            // Then
            assertSoftly(softly -> {
                softly.assertThat(repository).isNotNull();
                softly.assertThat(person).isNotNull();
                softly.assertThat(person.getName()).isEqualTo("Default");
            });
        }
    }
}
