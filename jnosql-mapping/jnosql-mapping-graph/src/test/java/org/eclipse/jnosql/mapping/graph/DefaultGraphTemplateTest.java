/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.graph;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.graph.CommunicationEdge;
import org.eclipse.jnosql.communication.graph.GraphDatabaseManager;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EntityConverterFactory;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultGraphTemplateTest {

    @Mock
    private EntityConverterFactory entityConverterFactory;

    @Mock
    private EntityConverter entityConverter;

    @Mock
    private GraphDatabaseManager graphDatabaseManager;

    @Mock
    private EventPersistManager eventPersistManager;

    @Mock
    private EntitiesMetadata entitiesMetadata;

    @Mock
    private Converters converters;

    private DefaultGraphTemplate graphTemplate;

    private Person person;
    private Book book;

    @BeforeEach
    void setUp() {
        person = new Person();
        book = new Book();
        when(entityConverterFactory.create(graphDatabaseManager)).thenReturn(entityConverter);
        this.graphTemplate = new DefaultGraphTemplate(entityConverterFactory, graphDatabaseManager, eventPersistManager, entitiesMetadata, converters);
    }

    @Nested
    @DisplayName("When the edge is created")
    class WhenTheEdgeIsCreated {

        @Test
        @DisplayName("Should create an edge successfully")
        void shouldCreateEdgeSuccessfully() {
            String label = "READS";
            Map<String, Object> properties = Map.of("since", 2020);
            CommunicationEntity sourceEntity = mock(CommunicationEntity.class);
            CommunicationEntity targetEntity = mock(CommunicationEntity.class);
            CommunicationEdge communicationEdge = mock(CommunicationEdge.class);

            when(entityConverter.toCommunication(person)).thenReturn(sourceEntity);
            when(entityConverter.toCommunication(book)).thenReturn(targetEntity);
            when(graphDatabaseManager.edge(sourceEntity, label, targetEntity, properties)).thenReturn(communicationEdge);
            when(communicationEdge.id()).thenReturn(123L);
            when(communicationEdge.source()).thenReturn(sourceEntity);
            when(communicationEdge.target()).thenReturn(targetEntity);
            when(entityConverter.toEntity(sourceEntity)).thenReturn(person);
            when(entityConverter.toEntity(targetEntity)).thenReturn(book);

            Edge<Person, Book> edge = graphTemplate.edge(person, label, book, properties);

            assertSoftly(soft -> {
                soft.assertThat(edge.label()).isEqualTo(label);
                soft.assertThat(edge.source()).isEqualTo(person);
                soft.assertThat(edge.target()).isEqualTo(book);
                soft.assertThat(edge.properties()).containsEntry("since", 2020);
                soft.assertThat(edge.id()).contains(123L);
                soft.assertThat(edge.label()).isNotNull();
                soft.assertThat(edge.label()).isEqualTo(label);
            });

            verify(graphDatabaseManager).edge(sourceEntity, label, targetEntity, properties);
        }
    }

    @Nested
    @DisplayName("When the edge is deleted")
    class WhenTheEdgeIsDeleted {

        @Test
        @DisplayName("Should delete edge successfully")
        void shouldDeleteEdgeSuccessfully() {
            Edge<Person, Book> edge = mock(Edge.class);
            when(edge.id()).thenReturn(Optional.of(123L));

            graphTemplate.delete(edge);

            verify(graphDatabaseManager).deleteEdge(123L);
        }

        @Test
        @DisplayName("Should delete edge by id")
        void shouldDeleteEdgeById() {
            long edgeId = 123L;

            graphTemplate.deleteEdge(edgeId);

            verify(graphDatabaseManager).deleteEdge(edgeId);
        }
    }

    @Nested
    @DisplayName("When the edge is searched")
    class WhenTheEdgeIsSearched {

        @Test
        @DisplayName("Should find edge by id")
        void shouldFindEdgeById() {
            long edgeId = 123L;
            CommunicationEntity sourceEntity = mock(CommunicationEntity.class);
            CommunicationEntity targetEntity = mock(CommunicationEntity.class);
            CommunicationEdge communicationEdge = mock(CommunicationEdge.class);

            when(graphDatabaseManager.findEdgeById(edgeId)).thenReturn(Optional.of(communicationEdge));
            when(communicationEdge.id()).thenReturn(edgeId);
            when(communicationEdge.label()).thenReturn("READS");
            when(communicationEdge.source()).thenReturn(sourceEntity);
            when(communicationEdge.target()).thenReturn(targetEntity);
            when(communicationEdge.properties()).thenReturn(Map.of("since", 2020));
            when(entityConverter.toEntity(sourceEntity)).thenReturn(person);
            when(entityConverter.toEntity(targetEntity)).thenReturn(book);

            Optional<Edge<Person, Book>> edge = graphTemplate.findEdgeById(edgeId);

            assertSoftly(soft -> {
                soft.assertThat(edge).isPresent();
                soft.assertThat(edge.get().label()).isEqualTo("READS");
                soft.assertThat(edge.get().source()).isEqualTo(person);
                soft.assertThat(edge.get().target()).isEqualTo(book);
                soft.assertThat(edge.get().properties()).containsEntry("since", 2020);
            });

            verify(graphDatabaseManager).findEdgeById(edgeId);
        }
    }

    @Nested
    @DisplayName("When the template is constructed")
    class WhenTheTemplateIsConstructed {

        @Test
        @DisplayName("Should have a default constructor for CDI")
        void shouldHaveDefaultConstructor() {
            DefaultGraphTemplate template = new DefaultGraphTemplate();

            assertThat(template).isNotNull();
        }
    }


    static class Person {}

    static class Book {}
}
