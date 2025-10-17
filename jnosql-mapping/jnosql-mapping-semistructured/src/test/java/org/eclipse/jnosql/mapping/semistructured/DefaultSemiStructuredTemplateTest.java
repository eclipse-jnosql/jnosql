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
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.data.exceptions.NonUniqueResultException;
import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.page.impl.CursoredPageRecord;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;

import org.eclipse.jnosql.communication.Configurations;

import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.Configurations;
import org.eclipse.jnosql.communication.TypeReference;

import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.IdNotFoundException;
import org.eclipse.jnosql.mapping.PreparedStatement;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.entities.Job;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.LargeProject;

import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.jnosql.communication.semistructured.DeleteQuery.delete;
import static org.eclipse.jnosql.communication.semistructured.SelectQuery.select;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class DefaultSemiStructuredTemplateTest {

    private final Person person = Person.builder().
            age().
            phones(Arrays.asList("234", "432")).
            name("Name")
            .id(19)
            .ignore().build();

    private final Element[] columns = new Element[]{
            Element.of("age", 10),
            Element.of("phones", Arrays.asList("234", "432")),
            Element.of("name", "Name"),
            Element.of("id", 19L),
    };

    @Inject
    private EntityConverter converter;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    private DatabaseManager managerMock;

    private DefaultSemiStructuredTemplate template;

    private ArgumentCaptor<CommunicationEntity> captor;

    private EventPersistManager eventPersistManager;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        managerMock = Mockito.mock(DatabaseManager.class);
        eventPersistManager = Mockito.mock(EventPersistManager.class);
        captor = ArgumentCaptor.forClass(CommunicationEntity.class);
        Instance<DatabaseManager> instance = Mockito.mock(Instance.class);
        Mockito.when(instance.get()).thenReturn(managerMock);
        this.template = new DefaultSemiStructuredTemplate(converter, instance,
                eventPersistManager, entities, converters);
    }

    @Test
    void shouldInsert() {
        var communicationEntity = CommunicationEntity.of("Person");
        communicationEntity.addAll(Stream.of(columns).collect(Collectors.toList()));

        Mockito.when(managerMock
                        .insert(any(CommunicationEntity.class)))
                .thenReturn(communicationEntity);

        template.insert(this.person);
        verify(managerMock).insert(captor.capture());
        verify(eventPersistManager).firePostEntity(any(Person.class));
        verify(eventPersistManager).firePreEntity(any(Person.class));
        CommunicationEntity value = captor.getValue();
        assertEquals("Person", value.name());
        assertEquals(5, value.elements().size());
    }


    @Test
    void shouldMergeOnInsert() {
        var communicationEntity = CommunicationEntity.of("Person");
        communicationEntity.addAll(Stream.of(columns).collect(Collectors.toList()));

        Mockito.when(managerMock
                        .insert(any(CommunicationEntity.class)))
                .thenReturn(communicationEntity);

        Person person = Person.builder().build();
        Person result = template.insert(person);
        verify(managerMock).insert(captor.capture());
        verify(eventPersistManager).firePostEntity(any(Person.class));
        verify(eventPersistManager).firePreEntity(any(Person.class));
        assertSame(person, result);
        assertEquals(10, person.getAge());

    }


    @Test
    void shouldInsertTTL() {
        var communicationEntity = CommunicationEntity.of("Person");
        communicationEntity.addAll(Stream.of(columns).collect(Collectors.toList()));

        Mockito.when(managerMock
                        .insert(any(CommunicationEntity.class),
                                any(Duration.class)))
                .thenReturn(communicationEntity);

        template.insert(this.person, Duration.ofHours(2));
        verify(managerMock).insert(captor.capture(), Mockito.eq(Duration.ofHours(2)));
        verify(eventPersistManager).firePostEntity(any(Person.class));
        verify(eventPersistManager).firePreEntity(any(Person.class));
        CommunicationEntity value = captor.getValue();
        assertEquals("Person", value.name());
        assertEquals(5, value.elements().size());
    }

    @Test
    void shouldUpdate() {
        var communicationEntity = CommunicationEntity.of("Person");
        communicationEntity.addAll(Stream.of(columns).collect(Collectors.toList()));

        Mockito.when(managerMock
                        .update(any(CommunicationEntity.class)))
                .thenReturn(communicationEntity);

        template.update(this.person);
        verify(managerMock).update(captor.capture());
        verify(eventPersistManager).firePostEntity(any(Person.class));
        verify(eventPersistManager).firePreEntity(any(Person.class));
        CommunicationEntity value = captor.getValue();
        assertEquals("Person", value.name());
        assertEquals(5, value.elements().size());
    }

    @Test
    void shouldMergeOnUpdate() {
        var communicationEntity = CommunicationEntity.of("Person");
        communicationEntity.addAll(Stream.of(columns).collect(Collectors.toList()));

        Mockito.when(managerMock
                        .update(any(CommunicationEntity.class)))
                .thenReturn(communicationEntity);

        Person person = Person.builder().build();
        Person result = template.update(person);
        verify(managerMock).update(captor.capture());
        verify(eventPersistManager).firePostEntity(any(Person.class));
        verify(eventPersistManager).firePreEntity(any(Person.class));
        assertSame(person, result);
        assertEquals(10, person.getAge());

    }

    @Test
    void shouldInsertEntitiesTTL() {
        var communicationEntity = CommunicationEntity.of("Person");
        communicationEntity.addAll(Stream.of(columns).collect(Collectors.toList()));
        Duration duration = Duration.ofHours(2);

        Mockito.when(managerMock
                        .insert(any(CommunicationEntity.class), Mockito.eq(duration)))
                .thenReturn(communicationEntity);

        template.insert(Arrays.asList(person, person), duration);
        verify(managerMock, times(2)).insert(any(CommunicationEntity.class), any(Duration.class));
    }

    @Test
    void shouldInsertEntities() {
        var communicationEntity = CommunicationEntity.of("Person");
        communicationEntity.addAll(Stream.of(columns).collect(Collectors.toList()));

        Mockito.when(managerMock
                        .insert(any(CommunicationEntity.class)))
                .thenReturn(communicationEntity);

        template.insert(Arrays.asList(person, person));
        verify(managerMock, times(2)).insert(any(CommunicationEntity.class));
    }

    @Test
    void shouldUpdateEntities() {
        var communicationEntity = CommunicationEntity.of("Person");
        communicationEntity.addAll(Stream.of(columns).collect(Collectors.toList()));

        Mockito.when(managerMock
                        .update(any(CommunicationEntity.class)))
                .thenReturn(communicationEntity);

        template.update(Arrays.asList(person, person));
        verify(managerMock, times(2)).update(any(CommunicationEntity.class));
    }

    @Test
    void shouldDelete() {

        DeleteQuery query = delete().from("delete").build();
        template.delete(query);
        verify(managerMock).delete(query);
    }

    @Test
    void shouldSelect() {
        SelectQuery query = select().from("person").build();
        template.select(query);
        verify(managerMock).select(query);
        verify(eventPersistManager, never()).firePostEntity(any(Person.class));
        verify(eventPersistManager, never()).firePreEntity(any(Person.class));
    }

    @Test
    void shouldCountBy() {
        SelectQuery query = select().from("person").build();
        template.count(query);
        verify(managerMock).count(query);
        verify(eventPersistManager, never()).firePostEntity(any(Person.class));
        verify(eventPersistManager, never()).firePreEntity(any(Person.class));
    }

    @Test
    void shouldExist() {
        SelectQuery query = select().from("person").build();
        template.exists(query);
        verify(managerMock).exists(query);
        verify(eventPersistManager, never()).firePostEntity(any(Person.class));
        verify(eventPersistManager, never()).firePreEntity(any(Person.class));
    }

    @Test
    void shouldReturnSingleResult() {
        CommunicationEntity columnEntity = CommunicationEntity.of("Person");
        columnEntity.addAll(Stream.of(columns).collect(Collectors.toList()));

        Mockito.when(managerMock
                        .select(any(SelectQuery.class)))
                .thenReturn(Stream.of(columnEntity));

        SelectQuery query = select().from("person").build();

        Optional<Person> result = template.singleResult(query);
        assertTrue(result.isPresent());
        verify(eventPersistManager, never()).firePostEntity(any(Person.class));
        verify(eventPersistManager, never()).firePreEntity(any(Person.class));
    }

    @Test
    void shouldReturnSingleResultIsEmpty() {
        Mockito.when(managerMock
                        .select(any(SelectQuery.class)))
                .thenReturn(Stream.empty());

        SelectQuery query = select().from("person").build();

        Optional<Person> result = template.singleResult(query);
        assertFalse(result.isPresent());
        verify(eventPersistManager, never()).firePostEntity(any(Person.class));
        verify(eventPersistManager, never()).firePreEntity(any(Person.class));
    }

    @Test
    void shouldReturnErrorWhenThereMoreThanASingleResult() {
        Assertions.assertThrows(NonUniqueResultException.class, () -> {
            CommunicationEntity columnEntity = CommunicationEntity.of("Person");
            columnEntity.addAll(Stream.of(columns).collect(Collectors.toList()));

            Mockito.when(managerMock
                            .select(any(SelectQuery.class)))
                    .thenReturn(Stream.of(columnEntity, columnEntity));

            SelectQuery query = select().from("person").build();

            template.singleResult(query);
        });
    }


    @Test
    void shouldReturnErrorWhenFindIdHasIdNull() {
        Assertions.assertThrows(NullPointerException.class, () -> template.find(Person.class, null));
    }

    @Test
    void shouldReturnErrorWhenFindIdHasClassNull() {
        Assertions.assertThrows(NullPointerException.class, () -> template.find(null, "10"));
    }

    @Test
    void shouldReturnErrorWhenThereIsNotIdInFind() {
        Assertions.assertThrows(IdNotFoundException.class, () -> template.find(Job.class, "10"));
    }

    @Test
    void shouldReturnFind() {
        template.find(Person.class, "10");
        ArgumentCaptor<SelectQuery> queryCaptor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(managerMock).select(queryCaptor.capture());
        SelectQuery query = queryCaptor.getValue();
        CriteriaCondition condition = query.condition().get();

        assertEquals("Person", query.name());
        assertEquals(CriteriaCondition.eq(Element.of("_id", 10L)), condition);
    }

    @Test
    void shouldDeleteTypeClass() {
        template.delete(Person.class, "10");
        ArgumentCaptor<DeleteQuery> queryCaptor = ArgumentCaptor.forClass(DeleteQuery.class);
        verify(managerMock).delete(queryCaptor.capture());

        DeleteQuery query = queryCaptor.getValue();

        CriteriaCondition condition = query.condition().get();

        assertEquals("Person", query.name());
        assertEquals(CriteriaCondition.eq(Element.of("_id", 10L)), condition);
    }

    @Test
    void shouldExecuteQuery() {
        template.prepare("FROM Person").result();
        ArgumentCaptor<SelectQuery> queryCaptor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(managerMock).select(queryCaptor.capture());
        SelectQuery query = queryCaptor.getValue();
        assertEquals("Person", query.name());
    }

    @Test
    void shouldExecuteQueryEntity() {
        template.prepare("FROM Person", "Person").result();
        ArgumentCaptor<SelectQuery> queryCaptor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(managerMock).select(queryCaptor.capture());
        SelectQuery query = queryCaptor.getValue();
        assertEquals("Person", query.name());
    }


    @Test
    void shouldConvertEntity() {
        template.prepare("FROM Movie").result();
        ArgumentCaptor<SelectQuery> queryCaptor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(managerMock).select(queryCaptor.capture());
        SelectQuery query = queryCaptor.getValue();
        assertEquals("movie", query.name());
    }

    @Test
    void shouldConvertEntityName() {
        template.prepare("SELECT name FROM download").result();
        ArgumentCaptor<SelectQuery> queryCaptor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(managerMock).select(queryCaptor.capture());
        SelectQuery query = queryCaptor.getValue();
        assertEquals("download", query.name());
    }

    @Test
    void shouldConvertEntityNameClassName() {
        template.prepare("FROM " + Person.class.getSimpleName()).result();
        ArgumentCaptor<SelectQuery> queryCaptor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(managerMock).select(queryCaptor.capture());
        SelectQuery query = queryCaptor.getValue();
        assertEquals("Person", query.name());
    }

    @Test
    void shouldConvertConvertFromAnnotationEntity() {
        template.prepare("FROM Vendor").result();
        ArgumentCaptor<SelectQuery> queryCaptor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(managerMock).select(queryCaptor.capture());
        SelectQuery query = queryCaptor.getValue();
        assertEquals("vendors", query.name());
    }

    @Test
    void shouldPreparedStatement() {
        PreparedStatement preparedStatement = template.prepare("FROM Person WHERE name = :name");
        preparedStatement.bind("name", "Ada");
        preparedStatement.result();
        ArgumentCaptor<SelectQuery> queryCaptor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(managerMock).select(queryCaptor.capture());
        SelectQuery query = queryCaptor.getValue();
        assertEquals("Person", query.name());
    }

    @Test
    void shouldPreparedStatementEntity() {
        PreparedStatement preparedStatement = template.prepare("FROM Person WHERE name = :name", "Person");
        preparedStatement.bind("name", "Ada");
        preparedStatement.result();
        ArgumentCaptor<SelectQuery> queryCaptor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(managerMock).select(queryCaptor.capture());
        SelectQuery query = queryCaptor.getValue();
        assertEquals("Person", query.name());
    }

    @Test
    void shouldCount() {
        template.count("Person");
        verify(managerMock).count("Person");
    }

    @Test
    void shouldCountFromEntityClass() {
        template.count(Person.class);
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(managerMock).count(captor.capture());
        var query = captor.getValue();
        SoftAssertions.assertSoftly(soft -> soft.assertThat(query.condition()).isEmpty());
    }


    @Test
    void shouldFindAll() {
        template.findAll(Person.class);
        verify(managerMock).select(select().from("Person").build());
    }

    @Test
    void shouldDeleteAll() {
        template.deleteAll(Person.class);
        verify(managerMock).delete(delete().from("Person").build());
    }

    @Test
    void shouldSelectCursor() {
        PageRequest request = PageRequest.ofSize(2);

        PageRequest afterKey = PageRequest.afterCursor(PageRequest.Cursor.forKey("Ada"), 1, 2, false);
        SelectQuery query = select().from("Person").orderBy("name").asc().build();

        Mockito.when(managerMock.selectCursor(query, request))
                .thenReturn(new CursoredPageRecord<>(content(),
                        Collections.emptyList(), -1, request, afterKey, null));

        PageRequest personRequest = PageRequest.ofSize(2);
        CursoredPage<Person> result = template.selectCursor(query, personRequest);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(result).isNotNull();
            soft.assertThat(result.content()).hasSize(1);
            soft.assertThat(result.hasNext()).isTrue();
            Person person = result.stream().findFirst().orElseThrow();

            soft.assertThat(person.getAge()).isEqualTo(10);
            soft.assertThat(person.getName()).isEqualTo("Name");
            soft.assertThat(person.getPhones()).containsExactly("234", "432");
        });

    }

    @Test
    void shouldThrowExceptionWhenCursorHasMultipleSorts() {
        System.setProperty(Configurations.CURSOR_PAGINATION_MULTIPLE_SORTING.get(), "true");
        PageRequest request = PageRequest.ofSize(2);

        PageRequest afterKey = PageRequest.afterCursor(PageRequest.Cursor.forKey("Ada"), 1, 2, false);
        SelectQuery query = select().from("Person").orderBy("name").asc().orderBy("age").desc().build();

        Mockito.when(managerMock.selectCursor(query, request))
                .thenReturn(new CursoredPageRecord<>(content(),
                        Collections.emptyList(), -1, request, afterKey, null));

        PageRequest personRequest = PageRequest.ofSize(2);

        CursoredPage<Person> result = template.selectCursor(query, personRequest);
        org.assertj.core.api.Assertions.assertThat(result).isNotNull();
        System.clearProperty(Configurations.CURSOR_PAGINATION_MULTIPLE_SORTING.get());
    }

    @Test
    void shouldExecuteMultipleSortsWhenEnableIt() {
        PageRequest request = PageRequest.ofSize(2);

        PageRequest afterKey = PageRequest.afterCursor(PageRequest.Cursor.forKey("Ada"), 1, 2, false);
        SelectQuery query = select().from("Person").orderBy("name").asc().orderBy("age").desc().build();

        Mockito.when(managerMock.selectCursor(query, request))
                .thenReturn(new CursoredPageRecord<>(content(),
                        Collections.emptyList(), -1, request, afterKey, null));

        PageRequest personRequest = PageRequest.ofSize(2);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> template.selectCursor(query, personRequest))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldSelectOffSet() {
        PageRequest request = PageRequest.ofPage(2).size(10);

        SelectQuery query = select().from("Person").orderBy("name").asc().build();

        Mockito.when(managerMock.select(Mockito.any())).thenReturn(Stream.empty());

        Page<Person> result = template.selectOffSet(query, request);
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery value = captor.getValue();
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(result).isNotNull();
            soft.assertThat(result.content()).isEmpty();
            soft.assertThat(result.hasNext()).isFalse();
            soft.assertThat(value.columns()).isEmpty();
            soft.assertThat(value.name()).isEqualTo("Person");
            soft.assertThat(value.sorts()).isNotEmpty();
            soft.assertThat(value.skip()).isEqualTo(10);
            soft.assertThat(value.limit()).isEqualTo(10);

        });
    }

    @Test
    void shouldFindByIdUsingInheritance() {

        this.template.find(LargeProject.class, 1L);
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(managerMock).select(captor.capture());
        var query = captor.getValue();
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.name()).isEqualTo("Project");
            soft.assertThat(query.condition()).isPresent();
            CriteriaCondition criteriaCondition = query.condition().orElseThrow();
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.AND);
            List<CriteriaCondition> conditions = criteriaCondition.element().get(new TypeReference<>() {
            });
            soft.assertThat(conditions).hasSize(2);
            soft.assertThat(conditions.get(0).element()).isEqualTo(Element.of("size", "Large"));
            soft.assertThat(conditions.get(1).element()).isEqualTo(Element.of("_id", "1"));
        });
    }

    @Test
    void shouldDeleteByIdUsingInheritance() {
        this.template.delete(LargeProject.class, 1L);
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        Mockito.verify(managerMock).delete(captor.capture());
        var query = captor.getValue();
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.name()).isEqualTo("Project");
            soft.assertThat(query.condition()).isPresent();
            CriteriaCondition criteriaCondition = query.condition().orElseThrow();
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.AND);
            List<CriteriaCondition> conditions = criteriaCondition.element().get(new TypeReference<>() {
            });
            soft.assertThat(conditions).hasSize(2);
            soft.assertThat(conditions.get(0).element()).isEqualTo(Element.of("size", "Large"));
            soft.assertThat(conditions.get(1).element()).isEqualTo(Element.of("_id", "1"));
        });
    }

    @Test
    void shouldDeleteEntity() {
        Person person = Person.builder().id(10).build();
        template.delete(person);
        ArgumentCaptor<DeleteQuery> queryCaptor = ArgumentCaptor.forClass(DeleteQuery.class);
        verify(managerMock).delete(queryCaptor.capture());

        DeleteQuery query = queryCaptor.getValue();

        CriteriaCondition condition = query.condition().get();

        assertEquals("Person", query.name());
        assertEquals(CriteriaCondition.eq(Element.of("_id", 10L)), condition);
    }

    @Test
    void shouldDeleteEntityIterable() {
        Person person = Person.builder().id(10).build();
        template.delete(List.of(person));
        ArgumentCaptor<DeleteQuery> queryCaptor = ArgumentCaptor.forClass(DeleteQuery.class);
        verify(managerMock).delete(queryCaptor.capture());

        DeleteQuery query = queryCaptor.getValue();

        CriteriaCondition condition = query.condition().get();

        assertEquals("Person", query.name());
        assertEquals(CriteriaCondition.eq(Element.of("_id", 10L)), condition);
    }



    private List<CommunicationEntity> content() {
        CommunicationEntity columnEntity = CommunicationEntity.of("Person");
        columnEntity.addAll(Stream.of(columns).collect(Collectors.toList()));
        return List.of(columnEntity);
    }
}
