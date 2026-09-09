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
package org.eclipse.jnosql.mapping.timeseries.configuration;

import jakarta.data.exceptions.MappingException;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.timeseries.MockProducer;
import org.eclipse.jnosql.mapping.timeseries.spi.TimeSeriesExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.TIME_SERIES_DATABASE;
import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.TIME_SERIES_PROVIDER;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, TimeSeriesExtension.class})
@DisplayName("TimeSeries manager supplier")
class TimeSeriesManagerSupplierTest {

    @Inject
    private TimeSeriesManagerSupplier supplier;

    @BeforeEach
    void beforeEach() {
        System.clearProperty(TIME_SERIES_PROVIDER.get());
        System.clearProperty(TIME_SERIES_DATABASE.get());
    }

    @Nested
    @DisplayName("When resolving a time-series manager")
    class WhenTheManagerResolution {

        @Test
        @DisplayName("Should use the configured provider")
        void shouldUseConfiguredProvider() {

            // Given
            System.setProperty(TIME_SERIES_PROVIDER.get(), TimeSeriesConfigurationMock.class.getName());
            System.setProperty(TIME_SERIES_DATABASE.get(), "database");

            // When
            DatabaseManager manager = supplier.get();

            // Then
            assertSoftly(softly -> {
                softly.assertThat(manager).isNotNull();
                softly.assertThat(manager).isInstanceOf(TimeSeriesConfigurationMock.TimeSeriesManagerMock.class);
            });
        }

        @Test
        @DisplayName("Should use the default configuration when the provider is invalid")
        void shouldUseDefaultConfigurationWhenProviderIsInvalid() {

            // Given
            System.setProperty(TIME_SERIES_PROVIDER.get(), Integer.class.getName());
            System.setProperty(TIME_SERIES_DATABASE.get(), "database");

            // When
            DatabaseManager manager = supplier.get();

            // Then
            assertSoftly(softly -> {
                softly.assertThat(manager).isNotNull();
                softly.assertThat(manager).isInstanceOf(TimeSeriesConfigurationMock2.TimeSeriesManagerMock.class);
            });
        }

        @Test
        @DisplayName("Should use the default configuration when no provider is configured")
        void shouldUseDefaultConfigurationWhenNoProviderIsConfigured() {

            // Given
            System.setProperty(TIME_SERIES_DATABASE.get(), "database");

            // When
            DatabaseManager manager = supplier.get();

            // Then
            assertSoftly(softly -> {
                softly.assertThat(manager).isNotNull();
                softly.assertThat(manager).isInstanceOf(TimeSeriesConfigurationMock2.TimeSeriesManagerMock.class);
            });
        }

        @Test
        @DisplayName("Should throw an exception when the database is missing")
        void shouldThrowExceptionWhenDatabaseIsMissing() {

            // When / Then
            assertThatExceptionOfType(MappingException.class).isThrownBy(supplier::get);
        }
    }

    @Nested
    @DisplayName("When closing a time-series manager")
    class WhenTheManagerClosing {

        @Test
        @DisplayName("Should close the manager")
        void shouldCloseManager() {

            // Given
            DatabaseManager manager = Mockito.mock(DatabaseManager.class);

            // When
            supplier.close(manager);

            // Then
            Mockito.verify(manager).close();
        }
    }
}
