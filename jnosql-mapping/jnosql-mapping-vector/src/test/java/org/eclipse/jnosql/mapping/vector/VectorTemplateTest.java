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
package org.eclipse.jnosql.mapping.vector;

import jakarta.inject.Inject;
import jakarta.nosql.Template;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.vector.spi.VectorExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.mapping.DatabaseType.VECTOR;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class, VectorTemplate.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, VectorExtension.class})
@DisplayName("Vector template")
class VectorTemplateTest {

    @Inject
    private Template template;

    @Inject
    @Database(VECTOR)
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
            DefaultVectorTemplate template = new DefaultVectorTemplate();

            // Then
            assertThat(template).isNotNull();
        }
    }

}
