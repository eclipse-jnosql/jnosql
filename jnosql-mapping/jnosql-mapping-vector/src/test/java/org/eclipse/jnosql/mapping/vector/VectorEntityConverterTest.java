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
import org.eclipse.jnosql.mapping.semistructured.EntityConverterFactory;
import org.eclipse.jnosql.mapping.vector.entities.Article;
import org.eclipse.jnosql.mapping.vector.entities.VectorRecord;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@EnableAutoWeld
@AddPackages({Converters.class, EntityConverter.class, Reflections.class})
@AddExtensions(ReflectionEntityMetadataExtension.class)
@DisplayName("Vector values in the existing entity mapping")
class VectorEntityConverterTest {

    @Inject
    private EntityConverterFactory factory;

    @Inject
    private EntitiesMetadata entities;

    @Test
    void shouldPreserveDenseVectorAndPayloadWithMappedColumnName() {
        DenseVector vector = DenseVector.of(0.12F, 0.45F, 0.78F);
        Article article = new Article("article-123", "Jakarta NoSQL", vector, new float[]{10F, 20F});
        EntityConverter converter = factory.create(Optional::empty);

        CommunicationEntity communication = converter.toCommunication(article);

        assertThat(communication.name()).isEqualTo("Article");
        assertThat(communication.size()).isEqualTo(4);
        assertThat(communication.find("_id", String.class)).contains("article-123");
        assertThat(communication.find("content", String.class)).contains("Jakarta NoSQL");
        assertThat(communication.find("representation", DenseVector.class)).containsSame(vector);
        assertThat(communication.elements().stream().filter(element -> element.get() instanceof Vector))
                .extracting(Element::name).containsExactly("representation");

        Article result = converter.toEntity(Article.class, communication);

        assertThat(result.getId()).isEqualTo(article.getId());
        assertThat(result.getContent()).isEqualTo(article.getContent());
        assertThat(result.getFeatures()).isEqualTo(vector);
        assertThat(result.getMeasurements()).containsExactly(10F, 20F);
        assertThat(entities.get(Article.class).columnField("features")).isEqualTo("representation");
    }

    @Test
    void shouldReadProviderSuppliedDenseVector() {
        CommunicationEntity communication = CommunicationEntity.of("Article");
        communication.add("_id", "article-123");
        communication.add("content", "Jakarta NoSQL");
        communication.add("representation", DenseVector.of(1F, 2F));

        Article result = factory.create(Optional::empty).toEntity(Article.class, communication);

        assertThat(result.getId()).isEqualTo("article-123");
        assertThat(result.getFeatures().values()).containsExactly(1F, 2F);
    }

    @Test
    void shouldRoundTripGenericVectorInRecord() {
        VectorRecord record = new VectorRecord("article-123", DenseVector.of(1F, 2F), "Otavio");
        EntityConverter converter = factory.create(Optional::empty);

        CommunicationEntity communication = converter.toCommunication(record);
        VectorRecord result = converter.toEntity(VectorRecord.class, communication);

        assertThat(communication.find("embedding", Vector.class)).containsSame(record.embedding());
        assertThat(result).isEqualTo(record);
    }

    @Test
    void shouldHonorProviderIdentifierMapping() {
        Article article = new Article("article-123", "Jakarta NoSQL", DenseVector.of(1F), new float[]{2F});
        EntityConverter converter = factory.create(() -> Optional.of("record_id"));

        CommunicationEntity communication = converter.toCommunication(article);
        Article result = converter.toEntity(Article.class, communication);

        assertThat(communication.find("record_id", String.class)).contains("article-123");
        assertThat(communication.find("_id", String.class)).isEmpty();
        assertThat(result.getId()).isEqualTo(article.getId());
        assertThat(result.getFeatures()).isEqualTo(article.getFeatures());
    }
}
