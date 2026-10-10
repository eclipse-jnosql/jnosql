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

import jakarta.data.Limit;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EntityConverterFactory;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;
import org.eclipse.jnosql.mapping.vector.entities.Article;
import org.eclipse.jnosql.mapping.vector.entities.SpecialArticle;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages({Converters.class, EntityConverter.class, Reflections.class})
@AddExtensions(ReflectionEntityMetadataExtension.class)
@DisplayName("Default vector template search")
class DefaultVectorTemplateSearchTest {

    @Inject
    private EntityConverterFactory factory;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    private VectorManager manager;

    private EventPersistManager events;

    private DefaultVectorTemplate template;

    private Article article;

    private CommunicationEntity communication;

    private Vector queryVector;

    @BeforeEach
    void setUp() {
        manager = mock(VectorManager.class);
        events = mock(EventPersistManager.class);

        when(manager.defaultIdFieldName()).thenReturn(Optional.empty());

        template = new DefaultVectorTemplate(
                factory,
                manager,
                events,
                entities,
                converters
        );

        article = new Article(
                "article-123",
                "Jakarta NoSQL",
                DenseVector.of(1F, 2F),
                new float[]{3F}
        );

        communication = factory.create(manager).toCommunication(article);
        queryVector = DenseVector.of(0.12F, 0.45F, 0.78F);
    }

    @Nested
    @DisplayName("When searching nearest neighbors")
    class WhenSearchingNearestNeighbors {

        @Test
        @DisplayName("Should execute the vector search")
        void shouldSearchNearestNeighbors() {
            when(manager.search(any(VectorSelectQuery.class)))
                    .thenReturn(List.of(communication));

            List<Article> result = template.search(Article.class)
                    .vector(queryVector)
                    .limit(Limit.of(10))
                    .result();

            assertThat(result)
                    .as("vector search result")
                    .singleElement()
                    .satisfies(found -> assertSoftly(softly -> {
                        softly.assertThat(found.getId())
                                .as("identifier")
                                .isEqualTo(article.getId());
                        softly.assertThat(found.getFeatures())
                                .as("vector")
                                .isEqualTo(article.getFeatures());
                    }));
        }

        @Test
        @DisplayName("Should create the vector select query")
        void shouldCreateVectorSelectQuery() {
            when(manager.search(any(VectorSelectQuery.class)))
                    .thenReturn(List.of());

            template.search(Article.class)
                    .vector(queryVector)
                    .limit(Limit.of(10))
                    .result();

            var captor = ArgumentCaptor.forClass(VectorSelectQuery.class);
            verify(manager).search(captor.capture());

            VectorSelectQuery query = captor.getValue();

            assertSoftly(softly -> {
                softly.assertThat(query.name())
                        .as("entity name")
                        .isEqualTo("Article");
                softly.assertThat(query.vector())
                        .as("query vector")
                        .isSameAs(queryVector);
                softly.assertThat(query.limit())
                        .as("limit")
                        .isEqualTo(10L);
                softly.assertThat(query.skip())
                        .as("skip")
                        .isZero();
                softly.assertThat(query.condition())
                        .as("condition")
                        .isEmpty();
                softly.assertThat(query.threshold())
                        .as("threshold")
                        .isEmpty();
            });
        }
    }

    @Nested
    @DisplayName("When searching with payload conditions")
    class WhenSearchingWithPayloadConditions {

        @Test
        @DisplayName("Should append an equals condition")
        void shouldSearchWithEqualsCondition() {
            when(manager.search(any(VectorSelectQuery.class)))
                    .thenReturn(List.of());

            template.search(Article.class)
                    .vector(queryVector)
                    .where("content").eq("Jakarta NoSQL")
                    .limit(Limit.of(10))
                    .result();

            var captor = ArgumentCaptor.forClass(VectorSelectQuery.class);
            verify(manager).search(captor.capture());

            assertThat(captor.getValue().condition())
                    .as("payload condition")
                    .hasValueSatisfying(condition ->
                            assertThat(condition.element().get())
                                    .as("condition value")
                                    .isEqualTo("Jakarta NoSQL"));
        }

        @Test
        @DisplayName("Should append conditions using and")
        void shouldSearchWithAndCondition() {
            when(manager.search(any(VectorSelectQuery.class)))
                    .thenReturn(List.of());

            template.search(Article.class)
                    .vector(queryVector)
                    .where("content").eq("Jakarta NoSQL")
                    .and("id").eq("article-123")
                    .limit(Limit.of(10))
                    .result();

            var captor = ArgumentCaptor.forClass(VectorSelectQuery.class);
            verify(manager).search(captor.capture());

            assertThat(captor.getValue().condition())
                    .as("combined condition")
                    .isPresent();
        }

        @Test
        @DisplayName("Should append conditions using or")
        void shouldSearchWithOrCondition() {
            when(manager.search(any(VectorSelectQuery.class)))
                    .thenReturn(List.of());

            template.search(Article.class)
                    .vector(queryVector)
                    .where("content").eq("Jakarta NoSQL")
                    .or("id").eq("article-456")
                    .limit(Limit.of(10))
                    .result();

            var captor = ArgumentCaptor.forClass(VectorSelectQuery.class);
            verify(manager).search(captor.capture());

            assertThat(captor.getValue().condition())
                    .as("combined condition")
                    .isPresent();
        }
    }

    @Nested
    @DisplayName("When searching with a threshold")
    class WhenSearchingWithThreshold {

        @Test
        @DisplayName("Should include the threshold in the vector query")
        void shouldSearchWithinThreshold() {
            when(manager.search(any(VectorSelectQuery.class)))
                    .thenReturn(List.of());

            template.search(Article.class)
                    .vector(queryVector)
                    .threshold(0.85F)
                    .limit(Limit.of(10))
                    .result();

            var captor = ArgumentCaptor.forClass(VectorSelectQuery.class);
            verify(manager).search(captor.capture());

            assertThat(captor.getValue().threshold())
                    .as("threshold")
                    .contains(0.85F);
        }

        @Test
        @DisplayName("Should reject NaN threshold")
        void shouldRejectNaNThreshold() {
            var search = template.search(Article.class)
                    .vector(queryVector);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> search.threshold(Float.NaN))
                    .withMessage("threshold must be finite");
        }

        @Test
        @DisplayName("Should reject positive infinity threshold")
        void shouldRejectPositiveInfinityThreshold() {
            var search = template.search(Article.class)
                    .vector(queryVector);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> search.threshold(Float.POSITIVE_INFINITY))
                    .withMessage("threshold must be finite");
        }
    }

    @Nested
    @DisplayName("When searching with conditions and threshold")
    class WhenSearchingWithConditionsAndThreshold {

        @Test
        @DisplayName("Should include condition threshold and limit")
        void shouldSearchWithConditionAndThreshold() {
            when(manager.search(any(VectorSelectQuery.class)))
                    .thenReturn(List.of());

            template.search(Article.class)
                    .vector(queryVector)
                    .where("content").eq("Jakarta NoSQL")
                    .threshold(0.85F)
                    .limit(Limit.of(5))
                    .result();

            var captor = ArgumentCaptor.forClass(VectorSelectQuery.class);
            verify(manager).search(captor.capture());

            VectorSelectQuery query = captor.getValue();

            assertSoftly(softly -> {
                softly.assertThat(query.condition())
                        .as("payload condition")
                        .isPresent();
                softly.assertThat(query.threshold())
                        .as("threshold")
                        .contains(0.85F);
                softly.assertThat(query.limit())
                        .as("limit")
                        .isEqualTo(5L);
            });
        }
    }

    @Nested
    @DisplayName("When validating vector search arguments")
    class WhenValidatingVectorSearchArguments {

        @Test
        @DisplayName("Should reject a null entity class")
        void shouldRejectNullEntityClass() {
            assertThatNullPointerException()
                    .isThrownBy(() -> template.search(null));

            verify(manager, never()).search(any(VectorSelectQuery.class));
        }

        @Test
        @DisplayName("Should reject a null vector")
        void shouldRejectNullVector() {
            var search = template.search(Article.class);

            assertThatNullPointerException()
                    .isThrownBy(() -> search.vector(null));

            verify(manager, never()).search(any(VectorSelectQuery.class));
        }

        @Test
        @DisplayName("Should reject a null field")
        void shouldRejectNullField() {
            var search = template.search(Article.class)
                    .vector(queryVector);

            assertThatNullPointerException()
                    .isThrownBy(() -> search.where(null));

            verify(manager, never()).search(any(VectorSelectQuery.class));
        }

        @Test
        @DisplayName("Should reject a null condition value")
        void shouldRejectNullConditionValue() {
            var condition = template.search(Article.class)
                    .vector(queryVector)
                    .where("content");

            assertThatNullPointerException()
                    .isThrownBy(() -> condition.eq(null));

            verify(manager, never()).search(any(VectorSelectQuery.class));
        }

        @Test
        @DisplayName("Should reject a null limit")
        void shouldRejectNullLimit() {
            var search = template.search(Article.class)
                    .vector(queryVector);

            assertThatNullPointerException()
                    .isThrownBy(() -> search.limit(null));

            verify(manager, never()).search(any(VectorSelectQuery.class));
        }
    }

    @Nested
    @DisplayName("When searching inherited entities")
    class WhenSearchingInheritedEntities {

        @Test
        @DisplayName("Should include the inheritance discriminator condition")
        void shouldIncludeInheritanceDiscriminatorCondition() {
            when(manager.search(any(VectorSelectQuery.class)))
                    .thenReturn(List.of());

            template.search(SpecialArticle.class)
                    .vector(queryVector)
                    .limit(Limit.of(10))
                    .result();

            var captor = ArgumentCaptor.forClass(VectorSelectQuery.class);
            verify(manager).search(captor.capture());

            assertThat(captor.getValue().condition())
                    .as("inheritance discriminator condition")
                    .isPresent();
        }

        @Test
        @DisplayName("Should combine the inheritance discriminator with the search condition")
        void shouldCombineInheritanceAndSearchCondition() {
            when(manager.search(any(VectorSelectQuery.class)))
                    .thenReturn(List.of());

            template.search(SpecialArticle.class)
                    .vector(queryVector)
                    .where("content").eq("Jakarta NoSQL")
                    .limit(Limit.of(10))
                    .result();

            var captor = ArgumentCaptor.forClass(VectorSelectQuery.class);
            verify(manager).search(captor.capture());

            CriteriaCondition condition = captor.getValue()
                    .condition()
                    .orElseThrow();

            assertSoftly(softly -> {
                softly.assertThat(condition)
                        .as("combined inheritance condition")
                        .isNotNull();

                softly.assertThat(condition.condition())
                        .as("logical operator")
                        .isEqualTo(Condition.AND);
            });
        }
    }



}