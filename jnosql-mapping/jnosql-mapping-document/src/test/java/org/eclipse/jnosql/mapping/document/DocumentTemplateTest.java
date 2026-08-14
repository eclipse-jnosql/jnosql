/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.document;

import jakarta.inject.Inject;
import jakarta.nosql.Template;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.document.spi.DocumentExtension;
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
import static org.eclipse.jnosql.mapping.DatabaseType.DOCUMENT;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class, DocumentTemplate.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, DocumentExtension.class})
@DisplayName("Document template")
class DocumentTemplateTest {

    @Inject
    private Template template;

    @Inject
    @Database(DOCUMENT)
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
            DefaultDocumentTemplate template = new DefaultDocumentTemplate();

            // Then
            assertThat(template).isNotNull();
        }
    }

}
