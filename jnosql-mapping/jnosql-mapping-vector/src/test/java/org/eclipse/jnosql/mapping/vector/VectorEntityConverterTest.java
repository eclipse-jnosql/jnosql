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
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.vector.entities.Article;
import org.eclipse.jnosql.mapping.vector.entities.VectorRecord;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@EnableAutoWeld
@AddPackages({Converters.class, EntityConverter.class, Reflections.class})
@AddExtensions(ReflectionEntityMetadataExtension.class)
@DisplayName("Vector values in the existing entity mapping")
class VectorEntityConverterTest {

    @Inject
    private EntityConverter converter;

    @Inject
    private EntitiesMetadata entities;

    @Nested
    @DisplayName("When converting a vector entity to communication data")
    class WhenTheCommunicationConversion {

        @Test
        @DisplayName("Should preserve the vector and payload under their mapped column names")
        void shouldPreserveMappedColumns() {
            DenseVector vector = DenseVector.of(0.12F, 0.45F, 0.78F);
            Article article = new Article("article-123", "Jakarta NoSQL", vector, new float[]{10F, 20F});

            CommunicationEntity communication = converter.toCommunication(article);

            assertSoftly(softly -> {
                softly.assertThat(communication.name()).as("entity name").isEqualTo("Article");
                softly.assertThat(communication.size()).as("persisted column count").isEqualTo(4);
                softly.assertThat(communication.find("_id", String.class)).as("identifier").contains("article-123");
                softly.assertThat(communication.find("content", String.class)).as("payload content").contains("Jakarta NoSQL");
                softly.assertThat(communication.find("representation", DenseVector.class))
                        .as("vector value").containsSame(vector);
                softly.assertThat(communication.elements().stream().filter(element -> element.get() instanceof Vector))
                        .as("vector columns exclude raw array payload").extracting(Element::name).containsExactly("representation");
                softly.assertThat(entities.get(Article.class).columnField("features"))
                        .as("vector column alias").isEqualTo("representation");
            });
        }

        @Test
        @DisplayName("Should reject a null entity")
        void shouldRejectNullEntity() {

            assertThatNullPointerException().isThrownBy(() -> converter.toCommunication(null))
                    .withMessage("entity is required");
        }
    }

    @Nested
    @DisplayName("When converting communication data to a vector entity")
    class WhenTheEntityConversion {

        @Test
        @DisplayName("Should restore a provider-supplied dense vector")
        void shouldRestoreDenseVector() {
            CommunicationEntity communication = CommunicationEntity.of("Article");
            communication.add("_id", "article-123");
            communication.add("content", "Jakarta NoSQL");
            communication.add("representation", DenseVector.of(1F, 2F));

            Article result = converter.toEntity(Article.class, communication);

            assertSoftly(softly -> {
                softly.assertThat(result.getId()).as("identifier").isEqualTo("article-123");
                softly.assertThat(result.getContent()).as("payload content").isEqualTo("Jakarta NoSQL");
                softly.assertThat(result.getFeatures()).as("restored vector").isEqualTo(DenseVector.of(1F, 2F));
            });
        }

        @Test
        @DisplayName("Should reject null communication data")
        void shouldRejectNullCommunication() {

            assertThatNullPointerException().isThrownBy(() -> converter.toEntity(Article.class, null))
                    .withMessage("entity is required");
        }

        @Test
        @DisplayName("Should reject a null entity type")
        void shouldRejectNullEntityType() {
            CommunicationEntity communication = CommunicationEntity.of("Article");

            assertThatNullPointerException().isThrownBy(() -> converter.toEntity((Class<Article>) null, communication))
                    .withMessage("type is required");
        }
    }

    @Nested
    @DisplayName("When round-tripping vector entities through communication data")
    class WhenTheRoundTrip {

        @Test
        @DisplayName("Should retain the identifier, dense vector, and payload")
        void shouldRetainEntityValues() {
            Article article = new Article("article-123", "Jakarta NoSQL", DenseVector.of(0.12F, 0.45F, 0.78F),
                    new float[]{10F, 20F});

            CommunicationEntity communication = converter.toCommunication(article);
            Article result = converter.toEntity(Article.class, communication);

            assertSoftly(softly -> {
                softly.assertThat(result.getId()).as("identifier").isEqualTo(article.getId());
                softly.assertThat(result.getContent()).as("payload content").isEqualTo(article.getContent());
                softly.assertThat(result.getFeatures()).as("dense vector").isEqualTo(article.getFeatures());
                softly.assertThat(result.getMeasurements()).as("raw array payload").containsExactly(10F, 20F);
            });
        }

        @Test
        @DisplayName("Should retain a record with a generic Vector column")
        void shouldRetainGenericVectorRecord() {
            VectorRecord record = new VectorRecord("article-123", DenseVector.of(1F, 2F), "Otavio");

            CommunicationEntity communication = converter.toCommunication(record);
            VectorRecord result = converter.toEntity(VectorRecord.class, communication);

            assertSoftly(softly -> {
                softly.assertThat(communication.find("embedding", Vector.class))
                        .as("generic vector value").containsSame(record.embedding());
                softly.assertThat(result).as("restored record").isEqualTo(record);
            });
        }

        @Test
        @DisplayName("Should honor the provider's identifier column without changing the vector")
        void shouldHonorProviderIdentifier() {
            Article article = new Article("article-123", "Jakarta NoSQL", DenseVector.of(1F), new float[]{2F});

            CommunicationEntity communication = converter.toCommunication(article);
            Article result = converter.toEntity(Article.class, communication);

            assertSoftly(softly -> {
                softly.assertThat(communication.find("_id", String.class))
                        .as("provider identifier column").contains("article-123");
                softly.assertThat(result.getId()).as("restored identifier").isEqualTo(article.getId());
                softly.assertThat(result.getFeatures()).as("restored vector").isEqualTo(article.getFeatures());
            });
        }
    }
}
