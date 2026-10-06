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
package org.eclipse.jnosql.mapping.vector.spi;

import jakarta.data.Limit;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;
import jakarta.nosql.Template;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.vector.DenseVector;
import org.eclipse.jnosql.mapping.vector.MockProducer;
import org.eclipse.jnosql.mapping.vector.VectorTemplate;
import org.eclipse.jnosql.mapping.vector.entities.Article;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@EnableAutoWeld
@AddPackages({Converters.class, EntityConverter.class, MockProducer.class, VectorTemplate.class, Reflections.class})
@AddExtensions({ReflectionEntityMetadataExtension.class, VectorExtension.class})
@DisplayName("Vector CDI extension")
class VectorExtensionTest {

    @Inject
    private VectorTemplate template;
    @Inject
    @Database(DatabaseType.VECTOR)
    private Template qualified;
    @Inject
    @Database(value = DatabaseType.VECTOR, provider = "named")
    private VectorTemplate named;
    @Inject
    @Database(value = DatabaseType.VECTOR, provider = "named")
    private SemiStructuredTemplate namedSemiStructured;
    @Inject
    @Database(value = DatabaseType.VECTOR, provider = "named")
    private Template namedStandard;
    @Inject
    @Any
    private Instance<VectorTemplate> templates;

    private Article article() {
        return new Article("1", "input", DenseVector.of(1F), new float[]{2F});
    }

    @Nested
    @DisplayName("When resolving vector templates")
    class WhenTheResolution {

        @Test
        @DisplayName("Should expose default and named templates through their inherited contracts")
        void shouldExposeTemplates() {
            Article defaultResult = template.insert(article());
            Article qualifiedResult = qualified.insert(article());
            Article namedResult = named.insert(article());
            Article semiStructuredResult = namedSemiStructured.insert(article());
            Article standardResult = namedStandard.insert(article());

            assertSoftly(softly -> {
                softly.assertThat(defaultResult.getContent()).as("default template").isEqualTo("default");
                softly.assertThat(qualifiedResult.getContent()).as("qualified template").isEqualTo("default");
                softly.assertThat(namedResult.getContent()).as("named template").isEqualTo("named");
                softly.assertThat(semiStructuredResult.getContent()).as("semi-structured contract").isEqualTo("named");
                softly.assertThat(standardResult.getContent()).as("standard contract").isEqualTo("named");
                softly.assertThat(templates.stream().count()).as("one template per vector manager").isEqualTo(2);
            });
        }

        @Test
        @DisplayName("Should ignore manager producers for other database types")
        void shouldIgnoreOtherDatabaseTypes() {
            Instance<VectorTemplate> other = templates.select(DatabaseQualifier.ofVector("other"));

            assertThat(other.isUnsatisfied()).as("unregistered non-vector manager").isTrue();
        }

        @Test
        @DisplayName("Should advertise the extension through the standard CDI service registration")
        void shouldRegisterService() {
            var extensions = ServiceLoader.load(Extension.class).stream().map(ServiceLoader.Provider::type).toList();

            assertThat(extensions).as("CDI service providers").contains(VectorExtension.class);
        }
    }
}
