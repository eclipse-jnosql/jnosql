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
package org.eclipse.jnosql.mapping.vector.query;

import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.query.SemiStructuredRepositoryProxy;
import org.eclipse.jnosql.mapping.vector.DenseVector;
import org.eclipse.jnosql.mapping.vector.MockProducer;
import org.eclipse.jnosql.mapping.vector.VectorTemplate;
import org.eclipse.jnosql.mapping.vector.entities.Article;
import org.eclipse.jnosql.mapping.vector.entities.ArticleRepository;
import org.eclipse.jnosql.mapping.vector.entities.Articles;
import org.eclipse.jnosql.mapping.vector.spi.VectorExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@EnableAutoWeld
@AddPackages({Converters.class, EntityConverter.class, SemiStructuredRepositoryProxy.class,
        MockProducer.class, VectorTemplate.class, Reflections.class})
@AddExtensions({ReflectionEntityMetadataExtension.class, VectorExtension.class})
@DisplayName("Vector repository extension")
class VectorRepositoryExtensionTest {

    @Inject
    private ArticleRepository unqualified;
    @Inject
    @Database(DatabaseType.VECTOR)
    private ArticleRepository repository;
    @Inject
    @Database(value = DatabaseType.VECTOR, provider = "named")
    private ArticleRepository namedRepository;
    @Inject
    private Articles unqualifiedCustom;
    @Inject
    @Database(DatabaseType.VECTOR)
    private Articles custom;
    @Inject
    @Database(value = DatabaseType.VECTOR, provider = "named")
    private Articles namedCustom;

    private Article article() {
        return new Article("1", "input", DenseVector.of(1F, 2F), new float[]{3F});
    }

    @Nested
    @DisplayName("When saving through standard vector repositories")
    class WhenTheStandardPersistence {

        @Test
        @DisplayName("Should route unqualified and Vector-qualified repositories to the default manager")
        void shouldUseDefaultManager() {
            Article unqualifiedResult = unqualified.save(article());
            Article qualifiedResult = repository.save(article());

            assertSoftly(softly -> {
                softly.assertThat(unqualifiedResult.getContent()).as("unqualified provider").isEqualTo("default");
                softly.assertThat(qualifiedResult.getContent()).as("qualified provider").isEqualTo("default");
                softly.assertThat(qualifiedResult.getFeatures()).as("vector").isEqualTo(DenseVector.of(1F, 2F));
            });
        }

        @Test
        @DisplayName("Should route a named repository to its selected manager")
        void shouldUseNamedManager() {
            Article result = namedRepository.save(article());

            assertSoftly(softly -> {
                softly.assertThat(result.getContent()).as("named provider").isEqualTo("named");
                softly.assertThat(result.getFeatures()).as("vector").isEqualTo(DenseVector.of(1F, 2F));
            });
        }
    }

    @Nested
    @DisplayName("When inserting through custom vector repositories")
    class WhenTheCustomPersistence {

        @Test
        @DisplayName("Should route unqualified and Vector-qualified custom repositories to the default manager")
        void shouldUseDefaultManager() {
            Article unqualifiedResult = unqualifiedCustom.insert(article());
            Article qualifiedResult = custom.insert(article());

            assertSoftly(softly -> {
                softly.assertThat(unqualifiedResult.getContent()).as("unqualified provider").isEqualTo("default");
                softly.assertThat(qualifiedResult.getContent()).as("qualified provider").isEqualTo("default");
                softly.assertThat(qualifiedResult.getFeatures()).as("vector").isEqualTo(DenseVector.of(1F, 2F));
            });
        }

        @Test
        @DisplayName("Should route a named custom repository to its selected manager")
        void shouldUseNamedManager() {
            Article result = namedCustom.insert(article());

            assertSoftly(softly -> {
                softly.assertThat(result.getContent()).as("named provider").isEqualTo("named");
                softly.assertThat(result.getFeatures()).as("vector").isEqualTo(DenseVector.of(1F, 2F));
            });
        }
    }
}
