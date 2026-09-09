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
package org.eclipse.jnosql.mapping.timeseries;

import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.timeseries.spi.TimeSeriesExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;


@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class, TimeSeriesTemplate.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, TimeSeriesExtension.class})
@DisplayName("TimeSeries template producer")
class DefaultTimeSeriesTemplateProducerTest {

    @Inject
    private TimeSeriesTemplateProducer producer;


    @Nested
    @DisplayName("When creating a template")
    class WhenTheTemplateCreation {

        @Test
        @DisplayName("Should require a database manager")
        void shouldRequireDatabaseManager() {

            // When / Then
            assertThatNullPointerException().isThrownBy(() -> producer.apply(null));
        }

        @Test
        @DisplayName("Should create a time-series template")
        void shouldCreateTimeSeriesTemplate() {

            // Given
            var manager = Mockito.mock(DatabaseManager.class);

            // When
            TimeSeriesTemplate timeSeriesTemplate = producer.apply(manager);

            // Then
            assertThat(timeSeriesTemplate).isNotNull();
        }
    }

    @Nested
    @DisplayName("When creating the CDI producer")
    class WhenTheProducerCreation {

        @Test
        @DisplayName("Should expose a default constructor")
        void shouldExposeDefaultConstructor() {

            // When
            TimeSeriesTemplateProducer.ProducerTimeSeriesTemplate template = new TimeSeriesTemplateProducer.ProducerTimeSeriesTemplate();

            // Then
            assertThat(template).isNotNull();
        }
    }

}
