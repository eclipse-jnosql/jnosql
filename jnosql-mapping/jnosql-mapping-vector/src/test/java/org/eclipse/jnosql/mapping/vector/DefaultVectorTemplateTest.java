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
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;
import org.eclipse.jnosql.mapping.vector.entities.Article;
import org.eclipse.jnosql.mapping.vector.entities.ConvertedIdArticle;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages({Converters.class, EntityConverter.class, Reflections.class})
@AddExtensions(ReflectionEntityMetadataExtension.class)
@DisplayName("Default vector template")
class DefaultVectorTemplateTest {

    @Inject
    private EntityConverter converter;
    @Inject
    private EntitiesMetadata entities;
    @Inject
    private Converters converters;

    private VectorManager manager;
    private EventPersistManager events;
    private DefaultVectorTemplate template;
    private Article article;

    @BeforeEach
    void setUp() {
        manager = mock(VectorManager.class);
        events = mock(EventPersistManager.class);
        template = new DefaultVectorTemplate(converter, manager, events, entities, converters);
        article = new Article("article-123", "Jakarta NoSQL", DenseVector.of(1F, 2F), new float[]{3F});
    }

    @Nested
    @DisplayName("When inserting vector entities")
    class WhenTheInsertion {

        @Test
        @DisplayName("Should preserve the vector and payload and publish persistence events")
        void shouldPersistEntity() {
            when(manager.insert(any(CommunicationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Article result = template.insert(article);

            var captured = ArgumentCaptor.forClass(CommunicationEntity.class);
            var order = inOrder(events, manager);
            order.verify(events).firePreEntity(article);
            order.verify(manager).insert(captured.capture());
            order.verify(events).firePostEntity(article);
            assertSoftly(softly -> {
                softly.assertThat(captured.getValue().find("representation", DenseVector.class))
                        .as("persisted vector").contains(article.getFeatures());
                softly.assertThat(captured.getValue().find("content", String.class))
                        .as("payload").contains("Jakarta NoSQL");
                softly.assertThat(result.getId()).as("identifier").isEqualTo("article-123");
                softly.assertThat(result.getFeatures()).as("returned vector").isEqualTo(article.getFeatures());
            });
        }

        @Test
        @DisplayName("Should forward the time to live")
        void shouldPreserveTimeToLive() {
            Duration ttl = Duration.ofMinutes(5);
            when(manager.insert(any(CommunicationEntity.class), eq(ttl))).thenAnswer(invocation -> invocation.getArgument(0));

            Article result = template.insert(article, ttl);

            verify(manager).insert(any(CommunicationEntity.class), eq(ttl));
            assertThat(result.getFeatures()).as("returned vector").isEqualTo(article.getFeatures());
        }

        @Test
        @DisplayName("Should insert each entity in a batch")
        void shouldPersistBatch() {
            when(manager.insert(any(CommunicationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Iterable<Article> results = template.insert(List.of(article));

            assertThat(results).as("inserted entities").containsExactly(article);
            verify(manager).insert(any(CommunicationEntity.class));
        }

        @Test
        @DisplayName("Should reject a null entity before contacting the manager")
        void shouldRejectNullEntity() {
            assertThatNullPointerException().isThrownBy(() -> template.insert((Article) null));
            verifyNoInteractions(manager, events);
        }
    }

    @Nested
    @DisplayName("When updating a vector entity")
    class WhenTheUpdate {

        @Test
        @DisplayName("Should map the updated entity and publish persistence events")
        void shouldUpdateEntity() {
            when(manager.update(any(CommunicationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Article result = template.update(article);

            var order = inOrder(events, manager);
            order.verify(events).firePreEntity(article);
            order.verify(manager).update(any(CommunicationEntity.class));
            order.verify(events).firePostEntity(article);
            assertThat(result.getFeatures()).as("updated vector").isEqualTo(article.getFeatures());
        }
    }

    @Nested
    @DisplayName("When finding a vector entity by identifier")
    class WhenTheLookup {

        @Test
        @DisplayName("Should convert identifiers before querying the manager")
        void shouldConvertIdentifier() {
            when(manager.select(any(SelectQuery.class))).thenAnswer(invocation -> Stream.empty());

            template.find(ConvertedIdArticle.class, 123L);

            var captured = ArgumentCaptor.forClass(SelectQuery.class);
            verify(manager).select(captured.capture());
            assertThat(captured.getValue().condition()).as("converted identifier condition")
                    .hasValueSatisfying(condition ->
                            assertThat(condition.element().get()).as("database identifier").isEqualTo("stored:123"));
        }

        @Test
        @DisplayName("Should restore the entity from the manager result")
        void shouldFindEntity() {
            CommunicationEntity communication = converter.toCommunication(article);
            when(manager.select(any(SelectQuery.class))).thenAnswer(invocation -> Stream.of(communication));

            Optional<Article> result = template.find(Article.class, "article-123");

            assertThat(result).as("found entity").hasValueSatisfying(found -> assertSoftly(softly -> {
                softly.assertThat(found.getId()).as("identifier").isEqualTo(article.getId());
                softly.assertThat(found.getFeatures()).as("vector").isEqualTo(article.getFeatures());
            }));
        }

        @Test
        @DisplayName("Should return empty when the identifier is absent")
        void shouldReturnEmptyWhenAbsent() {
            when(manager.select(any(SelectQuery.class))).thenAnswer(invocation -> Stream.empty());

            Optional<Article> result = template.find(Article.class, "missing");

            assertThat(result).as("missing entity").isEmpty();
        }
    }

    @Nested
    @DisplayName("When deleting a vector entity")
    class WhenTheRemoval {

        @Test
        @DisplayName("Should delegate deletion by identifier")
        void shouldDeleteEntity() {
            template.delete(Article.class, "article-123");

            var captured = ArgumentCaptor.forClass(DeleteQuery.class);
            verify(manager).delete(captured.capture());
            assertSoftly(softly -> {
                softly.assertThat(captured.getValue().name()).as("entity name").isEqualTo("Article");
                softly.assertThat(captured.getValue().condition()).as("identifier condition").isPresent();
            });
        }
    }

    @Nested
    @DisplayName("When searching nearest neighbors")
    class WhenSearchingNearestNeighbors {

        @Test
        @DisplayName("Should delegate the vector search and convert the results")
        void shouldSearchNearestNeighbors() {
            CommunicationEntity communication = converter.toCommunication(article);
            Vector queryVector = DenseVector.of(1F, 2F);
            Limit limit = Limit.of(10);

            when(manager.searchNearestNeighbors("Article", queryVector, limit))
                    .thenReturn(List.of(communication));

            List<Article> result = template.searchNearestNeighbors(
                    Article.class,
                    queryVector,
                    limit);

            verify(manager).searchNearestNeighbors("Article", queryVector, limit);

            assertThat(result)
                    .singleElement()
                    .satisfies(found -> assertSoftly(softly -> {
                        softly.assertThat(found.getId())
                                .as("identifier")
                                .isEqualTo(article.getId());
                        softly.assertThat(found.getContent())
                                .as("payload")
                                .isEqualTo(article.getContent());
                        softly.assertThat(found.getFeatures())
                                .as("vector")
                                .isEqualTo(article.getFeatures());
                    }));
        }

        @Test
        @DisplayName("Should reject null arguments before contacting the manager")
        void shouldRejectNullArguments() {
            Vector queryVector = DenseVector.of(1F, 2F);
            Limit limit = Limit.of(10);

            assertThatNullPointerException()
                    .isThrownBy(() -> template.searchNearestNeighbors(null, queryVector, limit));

            assertThatNullPointerException()
                    .isThrownBy(() -> template.searchNearestNeighbors(
                            Article.class, null, limit));

            assertThatNullPointerException()
                    .isThrownBy(() -> template.searchNearestNeighbors(
                            Article.class, queryVector, null));

            verifyNoInteractions(manager);
        }
    }

    @Nested
    @DisplayName("When searching nearest neighbors with filters")
    class WhenSearchingNearestNeighborsWithFilters {

        @Test
        @DisplayName("Should map field names and delegate the filtered vector search")
        void shouldSearchNearestNeighborsWithFilters() {
            CommunicationEntity communication = converter.toCommunication(article);
            Vector queryVector = DenseVector.of(1F, 2F);
            Limit limit = Limit.of(10);

            Map<String, Object> filters = Map.of(
                    "features", article.getFeatures()
            );

            Map<String, Object> mappedFilters = Map.of(
                    "representation", article.getFeatures()
            );

            when(manager.searchNearestNeighbors(
                    "Article",
                    queryVector,
                    mappedFilters,
                    limit))
                    .thenReturn(List.of(communication));

            List<Article> result = template.searchNearestNeighbors(
                    Article.class,
                    queryVector,
                    filters,
                    limit);

            verify(manager).searchNearestNeighbors(
                    "Article",
                    queryVector,
                    mappedFilters,
                    limit);

            assertThat(result)
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
        @DisplayName("Should reject null arguments before contacting the manager")
        void shouldRejectNullArguments() {
            Vector queryVector = DenseVector.of(1F, 2F);
            Limit limit = Limit.of(10);
            Map<String, Object> filters = Map.of("content", "Jakarta NoSQL");

            assertThatNullPointerException()
                    .isThrownBy(() -> template.searchNearestNeighbors(
                            null, queryVector, filters, limit));

            assertThatNullPointerException()
                    .isThrownBy(() -> template.searchNearestNeighbors(
                            Article.class, null, filters, limit));

            assertThatNullPointerException()
                    .isThrownBy(() -> template.searchNearestNeighbors(
                            Article.class, queryVector, null, limit));

            assertThatNullPointerException()
                    .isThrownBy(() -> template.searchNearestNeighbors(
                            Article.class, queryVector, filters, null));

            verifyNoInteractions(manager);
        }
    }

    @Nested
    @DisplayName("When searching within a vector threshold")
    class WhenSearchingWithinThreshold {

        @Test
        @DisplayName("Should delegate the threshold search and convert the results")
        void shouldSearchWithinThreshold() {
            CommunicationEntity communication = converter.toCommunication(article);
            Vector queryVector = DenseVector.of(1F, 2F);
            float threshold = 0.85F;

            when(manager.searchWithinThreshold(
                    "Article",
                    queryVector,
                    threshold))
                    .thenReturn(List.of(communication));

            List<Article> result = template.searchWithinThreshold(
                    Article.class,
                    queryVector,
                    threshold);

            verify(manager).searchWithinThreshold(
                    "Article",
                    queryVector,
                    threshold);

            assertThat(result)
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
        @DisplayName("Should reject a non-finite threshold")
        void shouldRejectNonFiniteThreshold() {
            Vector queryVector = DenseVector.of(1F, 2F);

            assertThatThrownBy(() -> template.searchWithinThreshold(
                    Article.class,
                    queryVector,
                    Float.NaN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("threshold must be finite");

            assertThatThrownBy(() -> template.searchWithinThreshold(
                    Article.class,
                    queryVector,
                    Float.POSITIVE_INFINITY))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("threshold must be finite");

            verifyNoInteractions(manager);
        }

        @Test
        @DisplayName("Should reject null arguments before contacting the manager")
        void shouldRejectNullArguments() {
            Vector queryVector = DenseVector.of(1F, 2F);

            assertThatNullPointerException()
                    .isThrownBy(() -> template.searchWithinThreshold(
                            null,
                            queryVector,
                            0.85F));

            assertThatNullPointerException()
                    .isThrownBy(() -> template.searchWithinThreshold(
                            Article.class,
                            null,
                            0.85F));

            verifyNoInteractions(manager);
        }
    }

    @Nested
    @DisplayName("When creating the vector template")
    class WhenCreatingTheVectorTemplate {

        @Test
        @DisplayName("Should accept a database manager that implements VectorManager")
        void shouldAcceptVectorManager() {
            VectorManager vectorManager = mock(VectorManager.class);

            DefaultVectorTemplate vectorTemplate = new DefaultVectorTemplate(
                    converter,
                    vectorManager,
                    events,
                    entities,
                    converters);

            assertThat(vectorTemplate.manager())
                    .as("vector manager")
                    .isSameAs(vectorManager);
        }

        @Test
        @DisplayName("Should create a DefaultVectorTemplate with default constructor")
        void shouldCreateDefaultConstructor() {
            DefaultVectorTemplate vectorTemplate = new DefaultVectorTemplate();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(vectorTemplate.manager()).as("manager").isNull();
                softly.assertThat(vectorTemplate.converter()).as("converter").isNull();
                softly.assertThat(vectorTemplate.eventManager()).as("event manager").isNull();
                softly.assertThat(vectorTemplate.entities()).as("entities metadata").isNull();
                softly.assertThat(vectorTemplate.converters()).as("converters").isNull();
            });
        }
    }
}
