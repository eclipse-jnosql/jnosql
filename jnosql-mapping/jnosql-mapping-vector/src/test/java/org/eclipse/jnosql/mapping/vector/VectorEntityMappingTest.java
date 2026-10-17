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
package org.eclipse.jnosql.mapping.vector;

import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.vector.entities.Article;
import org.eclipse.jnosql.mapping.vector.entities.MediaItem;
import org.eclipse.jnosql.mapping.vector.entities.Essay;
import org.eclipse.jnosql.mapping.vector.entities.ScientificArticle;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@EnableAutoWeld
@AddPackages({Converters.class, EntityConverter.class, Reflections.class})
@AddExtensions(ReflectionEntityMetadataExtension.class)
@DisplayName("Dense, sparse and multi vector declarations in entity mapping")
class VectorEntityMappingTest {

    @Inject
    private EntityConverter entityConverter;

    @Inject
    private EntitiesMetadata entities;

    private EntityConverter converter() {
        return entityConverter;
    }

    private <T> T roundTrip(T entity, Class<T> type) {
        EntityConverter converter = converter();
        CommunicationEntity communication = converter.toCommunication(entity);
        return converter.toEntity(type, communication);
    }

    @Nested
    @DisplayName("When discovering vector attributes in the metadata")
    class WhenMetadataDiscovery {

        private Class<?> typeOf(Class<?> entity, String field) {
            FieldMetadata metadata = entities.get(entity).fieldMapping(field).orElseThrow();
            return metadata.type();
        }

        @Test
        @DisplayName("Should recognize a generic Vector attribute")
        void shouldRecognizeVector() {
            assertThat(Vector.class).isAssignableFrom(typeOf(Essay.class, "embedding"));
            assertThat(typeOf(Essay.class, "embedding")).isEqualTo(Vector.class);
        }

        @Test
        @DisplayName("Should recognize a DenseVector attribute")
        void shouldRecognizeDenseVector() {
            assertThat(typeOf(Article.class, "features")).isEqualTo(DenseVector.class);
            assertThat(Vector.class).isAssignableFrom(DenseVector.class);
        }

        @Test
        @DisplayName("Should recognize a SparseVector attribute")
        void shouldRecognizeSparseVector() {
            assertThat(typeOf(ScientificArticle.class, "embedding")).isEqualTo(SparseVector.class);
            assertThat(Vector.class).isAssignableFrom(SparseVector.class);
        }

        @Test
        @DisplayName("Should recognize a MultiVector attribute")
        void shouldRecognizeMultiVector() {
            assertThat(typeOf(MediaItem.class, "embedding")).isEqualTo(MultiVector.class);
            assertThat(Vector.class).isAssignableFrom(MultiVector.class);
        }
    }

    @Nested
    @DisplayName("When a Vector attribute holds a runtime specialization")
    class WhenGenericVector {

        @Test
        @DisplayName("Should keep a DenseVector as DenseVector")
        void shouldKeepDenseVector() {
            Essay result = roundTrip(new Essay("1", "Embeddings", DenseVector.of(0.2F, 0.8F, 0.1F)), Essay.class);

            assertThat(result.getEmbedding()).isInstanceOf(DenseVector.class);
            DenseVector dense = (DenseVector) result.getEmbedding();
            assertSoftly(softly -> {
                softly.assertThat(dense.dimensions()).as("dimensions").isEqualTo(3);
                softly.assertThat(dense.values()).as("values").containsExactly(0.2F, 0.8F, 0.1F);
            });
        }

        @Test
        @DisplayName("Should keep a SparseVector as SparseVector")
        void shouldKeepSparseVector() {
            Essay result = roundTrip(new Essay("1", "Embeddings",
                    SparseVector.of(10, SparseVector.element(2, 7.0F), SparseVector.element(8, 3.0F))), Essay.class);

            assertThat(result.getEmbedding()).isInstanceOf(SparseVector.class);
            SparseVector sparse = (SparseVector) result.getEmbedding();
            assertSoftly(softly -> {
                softly.assertThat(sparse.dimensions()).as("dimensions").isEqualTo(10);
                softly.assertThat(sparse.indices()).as("indices").containsExactly(2, 8);
                softly.assertThat(sparse.values()).as("values").containsExactly(7.0F, 3.0F);
            });
        }

        @Test
        @DisplayName("Should keep a MultiVector as MultiVector")
        void shouldKeepMultiVector() {
            Essay result = roundTrip(new Essay("1", "Embeddings", MultiVector.of(
                    DenseVector.of(0.2F, 0.8F, 0.1F), DenseVector.of(0.7F, 0.1F, 0.4F))), Essay.class);

            assertThat(result.getEmbedding()).isInstanceOf(MultiVector.class);
            MultiVector multi = (MultiVector) result.getEmbedding();
            assertSoftly(softly -> {
                softly.assertThat(multi.size()).as("size").isEqualTo(2);
                softly.assertThat(multi.dimensions()).as("dimensions").isEqualTo(3);
                softly.assertThat(multi.vectors().get(0).values()).as("first").containsExactly(0.2F, 0.8F, 0.1F);
                softly.assertThat(multi.vectors().get(1).values()).as("second").containsExactly(0.7F, 0.1F, 0.4F);
            });
        }
    }

    @Nested
    @DisplayName("When an attribute is declared with a concrete vector type")
    class WhenExplicitVector {

        @Test
        @DisplayName("Should round-trip a DenseVector attribute")
        void shouldRoundTripDenseVector() {
            Article result = roundTrip(new Article("1", "Jakarta NoSQL", DenseVector.of(0.2F, 0.8F, 0.1F),
                    new float[]{1F}), Article.class);

            DenseVector dense = result.getFeatures();
            assertSoftly(softly -> {
                softly.assertThat(dense).as("type").isInstanceOf(DenseVector.class);
                softly.assertThat(dense.dimensions()).as("dimensions").isEqualTo(3);
                softly.assertThat(dense.values()).as("values").containsExactly(0.2F, 0.8F, 0.1F);
            });
        }

        @Test
        @DisplayName("Should round-trip a SparseVector attribute without densifying it")
        void shouldRoundTripSparseVector() {
            ScientificArticle result = roundTrip(new ScientificArticle("1", "Jakarta NoSQL",
                    SparseVector.of(10, SparseVector.element(2, 7.0F), SparseVector.element(8, 3.0F))),
                    ScientificArticle.class);

            SparseVector sparse = result.getEmbedding();
            assertSoftly(softly -> {
                softly.assertThat(sparse).as("type").isInstanceOf(SparseVector.class);
                softly.assertThat(sparse.dimensions()).as("dimensions").isEqualTo(10);
                softly.assertThat(sparse.indices()).as("indices").containsExactly(2, 8);
                softly.assertThat(sparse.values()).as("values").containsExactly(7.0F, 3.0F);
            });
        }

        @Test
        @DisplayName("Should round-trip a MultiVector attribute without flattening it")
        void shouldRoundTripMultiVector() {
            MediaItem result = roundTrip(new MediaItem("1", "Documentary", MultiVector.of(
                    DenseVector.of(0.2F, 0.8F, 0.1F), DenseVector.of(0.7F, 0.1F, 0.4F))), MediaItem.class);

            MultiVector multi = result.getEmbedding();
            assertSoftly(softly -> {
                softly.assertThat(multi).as("type").isInstanceOf(MultiVector.class);
                softly.assertThat(multi.size()).as("size").isEqualTo(2);
                softly.assertThat(multi.dimensions()).as("dimensions").isEqualTo(3);
                softly.assertThat(multi.vectors().get(0).values()).as("first segment").containsExactly(0.2F, 0.8F, 0.1F);
                softly.assertThat(multi.vectors().get(1).values()).as("second segment").containsExactly(0.7F, 0.1F, 0.4F);
            });
        }

        @Test
        @DisplayName("Should restore provider-supplied sparse and multi vectors")
        void shouldRestoreProviderVectors() {
            CommunicationEntity sparse = CommunicationEntity.of("ScientificArticle");
            sparse.add("_id", "1");
            sparse.add("embedding", SparseVector.of(4, SparseVector.element(1, 5.0F)));
            CommunicationEntity multi = CommunicationEntity.of("MediaItem");
            multi.add("_id", "2");
            multi.add("embedding", MultiVector.of(DenseVector.of(1F, 2F), DenseVector.of(3F, 4F)));

            ScientificArticle article = converter().toEntity(ScientificArticle.class, sparse);
            MediaItem mediaItem = converter().toEntity(MediaItem.class, multi);

            assertSoftly(softly -> {
                softly.assertThat(article.getEmbedding().indices()).as("indices").containsExactly(1);
                softly.assertThat(mediaItem.getEmbedding().vectors().get(1).values()).as("second").containsExactly(3F, 4F);
            });
        }
    }
}
