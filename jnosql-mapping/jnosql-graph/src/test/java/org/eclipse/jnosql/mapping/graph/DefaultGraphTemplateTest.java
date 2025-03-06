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

import org.eclipse.jnosql.communication.graph.GraphDatabaseManager;
import org.eclipse.jnosql.communication.graph.CommunicationEdge;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.assertj.core.api.SoftAssertions;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DefaultGraphTemplateTest {

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

    @InjectMocks
    private DefaultGraphTemplate graphTemplate;

    private Person person;
    private Book book;

    @BeforeEach
    void setUp() {
        person = new Person();
        book = new Book();
    }

    @Test
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

        SoftAssertions.assertSoftly(soft -> {
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

    @Test
    void shouldDeleteEdgeSuccessfully() {
        String label = "READS";
        Edge<Person, Book> edge = mock(Edge.class);
        when(edge.id()).thenReturn(Optional.of(123L));

        graphTemplate.delete(edge);

        verify(graphDatabaseManager).deleteEdge(123L);
    }

    @Test
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

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(edge).isPresent();
            soft.assertThat(edge.get().label()).isEqualTo("READS");
            soft.assertThat(edge.get().source()).isEqualTo(person);
            soft.assertThat(edge.get().target()).isEqualTo(book);
            soft.assertThat(edge.get().properties()).containsEntry("since", 2020);
        });

        verify(graphDatabaseManager).findEdgeById(edgeId);
    }

    @Test
    void shouldDeleteEdgeById() {
        long edgeId = 123L;

        graphTemplate.deleteEdge(edgeId);

        verify(graphDatabaseManager).deleteEdge(edgeId);
    }

    static class Person {}

    static class Book {}
}
