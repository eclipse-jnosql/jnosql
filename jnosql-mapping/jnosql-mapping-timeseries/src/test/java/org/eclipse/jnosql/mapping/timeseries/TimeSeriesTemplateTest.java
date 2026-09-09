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
import jakarta.nosql.Template;
import org.eclipse.jnosql.mapping.Database;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.mapping.DatabaseType.TIME_SERIES;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class, TimeSeriesTemplate.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, TimeSeriesExtension.class})
@DisplayName("TimeSeries template")
class TimeSeriesTemplateTest {

    @Inject
    private Template template;

    @Inject
    @Database(TIME_SERIES)
    private Template qualifier;


    @Nested
    @DisplayName("When injecting templates")
    class WhenTheTemplateInjection {

        @Test
        @DisplayName("Should inject the default template")
        void shouldInjectDefaultTemplate() {

            // Then
            assertThat(template).isNotNull();
        }

        @Test
        @DisplayName("Should inject the qualified template")
        void shouldInjectQualifiedTemplate() {

            // Then
            assertThat(qualifier).isNotNull();
        }
    }

    @Nested
    @DisplayName("When creating a template")
    class WhenTheTemplateCreation {

        @Test
        @DisplayName("Should expose a default constructor")
        void shouldExposeDefaultConstructor() {

            // When
            DefaultTimeSeriesTemplate template = new DefaultTimeSeriesTemplate();

            // Then
            assertThat(template).isNotNull();
        }
    }

}
