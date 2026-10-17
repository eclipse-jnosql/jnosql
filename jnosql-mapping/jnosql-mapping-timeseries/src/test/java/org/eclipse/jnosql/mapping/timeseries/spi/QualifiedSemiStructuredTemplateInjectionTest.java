/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
import org.eclipse.jnosql.mapping.timeseries.MockProducer;
import org.eclipse.jnosql.mapping.timeseries.TimeSeriesTemplate;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class, TimeSeriesTemplate.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, TimeSeriesExtension.class})
@DisplayName("Qualified SemiStructuredTemplate injection for TIME_SERIES")
class QualifiedSemiStructuredTemplateInjectionTest {

    @Inject
    @Database(DatabaseType.TIME_SERIES)
    private SemiStructuredTemplate template;

    @Test
    @DisplayName("Should resolve SemiStructuredTemplate qualified with @Database(DatabaseType.TIME_SERIES)")
    void shouldInjectQualifiedSemiStructuredTemplate() {
        assertThat(template).as("qualified SemiStructuredTemplate").isNotNull();
    }
}
