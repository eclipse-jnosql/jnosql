/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.semistructured.query;

import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.PreparedStatement;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.eclipse.jnosql.mapping.semistructured.entities.Task;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class CustomRepositoryHandlerTest {

    @Inject
    private EntitiesMetadata entitiesMetadata;

    private SemiStructuredTemplate template;

    @Inject
    private Converters converters;

    private People people;

    private Tasks tasks;

    @BeforeEach
    void setUp() {
        template = Mockito.mock(SemiStructuredTemplate.class);
        CustomRepositoryHandler customRepositoryHandlerForPeople = CustomRepositoryHandler.builder()
                .entitiesMetadata(entitiesMetadata)
                .template(template)
                .customRepositoryType(People.class)
                .converters(converters).build();

        people = (People) Proxy.newProxyInstance(People.class.getClassLoader(), new Class[]{People.class},
                customRepositoryHandlerForPeople);

        CustomRepositoryHandler customRepositoryHandlerForTasks = CustomRepositoryHandler.builder()
                .entitiesMetadata(entitiesMetadata)
                .template(template)
                .customRepositoryType(Tasks.class)
                .converters(converters).build();

        tasks = (Tasks) Proxy.newProxyInstance(Tasks.class.getClassLoader(), new Class[]{Tasks.class},
                customRepositoryHandlerForTasks);

    }

    @Test
    void shouldInsertEntity() {
        Person person = Person.builder().age(26).name("Ada").build();
        Mockito.when(template.insert(person)).thenReturn(person);
        Person result = people.insert(person);

        Mockito.verify(template).insert(person);
        Mockito.verifyNoMoreInteractions(template);
        Assertions.assertThat(result).isEqualTo(person);
    }

    @Test
    void shouldInsertListEntity() {
        var persons = List.of(Person.builder().age(26).name("Ada").build());
        Mockito.when(template.insert(persons)).thenReturn(persons);
        List<Person> result = people.insert(persons);

        Mockito.verify(template).insert(persons);
        Mockito.verifyNoMoreInteractions(template);
        Assertions.assertThat(result).isEqualTo(persons);
    }

    @Test
    void shouldInsertArrayEntity() {
        Person ada = Person.builder().age(26).name("Ada").build();
        var persons = new Person[]{ada};
        Mockito.when(template.insert(Mockito.any())).thenReturn(List.of(ada));
        Person[] result = people.insert(persons);

        Mockito.verify(template).insert(List.of(ada));
        Mockito.verifyNoMoreInteractions(template);
        Assertions.assertThat(result).isEqualTo(persons);
    }


    @Test
    void shouldUpdateEntity() {
        Person person = Person.builder().age(26).name("Ada").build();
        Mockito.when(template.update(person)).thenReturn(person);
        Person result = people.update(person);

        Mockito.verify(template).update(person);
        Mockito.verifyNoMoreInteractions(template);
        Assertions.assertThat(result).isEqualTo(person);
    }

    @Test
    void shouldUpdateListEntity() {
        var persons = List.of(Person.builder().age(26).name("Ada").build());
        Mockito.when(template.update(persons)).thenReturn(persons);
        List<Person> result = people.update(persons);

        Mockito.verify(template).update(persons);
        Mockito.verifyNoMoreInteractions(template);
        Assertions.assertThat(result).isEqualTo(persons);
    }

    @Test
    void shouldUpdateArrayEntity() {
        Person ada = Person.builder().age(26).name("Ada").build();
        var persons = new Person[]{ada};
        Mockito.when(template.update(Mockito.any())).thenReturn(List.of(ada));
        Person[] result = people.update(persons);

        Mockito.verify(template).update(List.of(ada));
        Mockito.verifyNoMoreInteractions(template);
        Assertions.assertThat(result).isEqualTo(persons);
    }

    @Test
    void shouldDeleteEntity() {
        Person person = Person.builder().id(1).age(26).name("Ada").build();
        people.delete(person);

        Mockito.verify(template).delete(Person.class, 1L);
        Mockito.verifyNoMoreInteractions(template);
    }

    @Test
    void shouldDeleteListEntity() {
        var persons = List.of(Person.builder().id(12L).age(26).name("Ada").build());
        people.delete(persons);

        Mockito.verify(template).delete(Person.class, 12L);
        Mockito.verifyNoMoreInteractions(template);
    }

    @Test
    void shouldDeleteAll() {
        people.deleteAll();

        Mockito.verify(template).deleteAll(Mockito.any());
        Mockito.verifyNoMoreInteractions(template);
    }

    @Test
    void shouldDeleteArrayEntity() {
        Person ada = Person.builder().id(2L).age(26).name("Ada").build();
        var persons = new Person[]{ada};
        people.delete(persons);

        Mockito.verify(template).delete(Person.class, 2L);
        Mockito.verifyNoMoreInteractions(template);
    }

    @Test
    void shouldSaveEntity() {
        Person person = Person.builder().age(26).name("Ada").build();
        Mockito.when(template.insert(person)).thenReturn(person);
        Person result = people.save(person);

        Mockito.verify(template).insert(person);
        Mockito.verify(template).find(Person.class, 0L);
        Assertions.assertThat(result).isEqualTo(person);
    }

    @Test
    void shouldSaveListEntity() {
        Person ada = Person.builder().age(26).name("Ada").build();
        var persons = List.of(ada);
        Mockito.when(template.insert(persons)).thenReturn(persons);
        Mockito.when(template.insert(ada)).thenReturn(ada);
        List<Person> result = people.save(persons);

        Mockito.verify(template).insert(ada);
        Mockito.verify(template).find(Person.class, 0L);
        Assertions.assertThat(result).isEqualTo(persons);
    }

    @Test
    void shouldSaveArrayEntity() {
        Person ada = Person.builder().age(26).name("Ada").build();
        var persons = new Person[]{ada};
        Mockito.when(template.insert(Mockito.any())).thenReturn(List.of(ada));
        Mockito.when(template.insert(ada)).thenReturn(ada);
        Person[] result = people.save(persons);

        Mockito.verify(template).insert(ada);
        Mockito.verify(template).find(Person.class, 0L);
        Assertions.assertThat(result).isEqualTo(persons);
    }


    @Test
    void shouldExecuteObjectMethods() {
        Assertions.assertThat(people.toString()).isNotNull();
        Assertions.assertThat(people.hashCode()).isNotEqualTo(0);
    }

    @Test
    void shouldExecuteDefaultMethod() {
        Assertions.assertThat(people.defaultMethod()).isEqualTo("default");
    }

    @Test
    void shouldExecuteFindByAge() {
        Mockito.when(template.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.of(Person.builder().age(26).name("Ada").build()));
        var result = people.findByAge(26);

        Assertions.assertThat(result).hasSize(1).isNotNull().isInstanceOf(List.class);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(template).select(captor.capture());
        Mockito.verifyNoMoreInteractions(template);
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.condition()).isPresent();
        });
    }

    @Test
    void shouldExecuteFindById() {

        Mockito.when(template.singleResult(Mockito.any(SelectQuery.class)))
                .thenReturn(Optional.of(Person.builder().age(26).name("Ada").build()));

        var result = people.findById(26L);

        Assertions.assertThat(result).isNotNull().isInstanceOf(Person.class);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(template).singleResult(captor.capture());
        Mockito.verifyNoMoreInteractions(template);
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.condition()).isPresent();
        });
    }

    @Test
    void shouldExecuteFindByIdAndName() {

        Mockito.when(template.singleResult(Mockito.any(SelectQuery.class)))
                .thenReturn(Optional.of(Person.builder().age(26).name("Ada").build()));

        var result = people.findByIdAndName(26L, "Ada");

        Assertions.assertThat(result).isNotNull().isPresent().isInstanceOf(Optional.class);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(template).singleResult(captor.capture());
        Mockito.verifyNoMoreInteractions(template);
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.condition()).isPresent();
        });
    }

    @Test
    void shouldExecuteFindPagination() {

        Mockito.when(template.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.of(Person.builder().age(26).name("Ada").build()));

        var result = people.findByAge(26, PageRequest.ofSize(2));

        Assertions.assertThat(result).isNotNull().isInstanceOf(Page.class);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(template).select(captor.capture());
        Mockito.verifyNoMoreInteractions(template);
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.condition()).isPresent();
        });
    }

    @Test
    void shouldExecuteFindCursorPagination() {

        var mock = Mockito.mock(CursoredPage.class);
        Mockito.when(template.selectCursor(Mockito.any(SelectQuery.class), Mockito.any(PageRequest.class)))
                .thenReturn(mock);

        var result = people.findByName("Ada", PageRequest.ofSize(2));

        Assertions.assertThat(result).isNotNull().isInstanceOf(CursoredPage.class);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(template).selectCursor(captor.capture(), Mockito.any(PageRequest.class));
        Mockito.verifyNoMoreInteractions(template);
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.condition()).isPresent();
        });
    }

    @Test
    void shouldExecutePathParameter() {

        Mockito.when(template.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.of(Person.builder().age(26).name("Ada").build()));

        var result = people.name("Ada");

        Assertions.assertThat(result).isNotNull().isInstanceOf(List.class);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(template).select(captor.capture());
        Mockito.verifyNoMoreInteractions(template);
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.condition()).isNotEmpty();
        });
    }


    @Test
    void shouldExecuteQuery() {

        var preparedStatement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        Mockito.when(template.prepare(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(template.query(Mockito.anyString()))
                .thenReturn(Stream.of(Person.builder().age(26).name("Ada").build()));

        var result = people.queryName("Ada");

        Assertions.assertThat(result).isNotNull().isInstanceOf(List.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(template).prepare(captor.capture(), Mockito.eq("Person"));
        Mockito.verifyNoMoreInteractions(template);
        var query = captor.getValue();

        Assertions.assertThat(query).isEqualTo("from Person where name = :name");
    }

    @Test
    void shouldExecuteQueryWithVoid() {

        var preparedStatement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        Mockito.when(template.prepare(Mockito.anyString(), Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(template.query(Mockito.anyString()))
                .thenReturn(Stream.of(Person.builder().age(26).name("Ada").build()));

        people.deleteByName("Ada");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(template).prepare(captor.capture(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(template);
        var query = captor.getValue();

        Assertions.assertThat(query).isEqualTo("delete from Person where name = :name");
    }

    @Test
    void shouldExecuteFixedQuery() {

        var preparedStatement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        Mockito.when(template.prepare(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(template.query(Mockito.anyString()))
                .thenReturn(Stream.of(Task.builder().description("refactor project A").build()));

        var result = tasks.listActiveTasks();

        Assertions.assertThat(result).isNotNull().isInstanceOf(List.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(template).prepare(captor.capture(), Mockito.eq("Task"));
        Mockito.verifyNoMoreInteractions(template);
        var query = captor.getValue();

        Assertions.assertThat(query).isEqualTo("from Task where active = true");

    }

    @Test
    void shouldExecuteCountBy() {

        var preparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(template.prepare(Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(template.query(Mockito.anyString()))
                .thenReturn(Stream.of(Person.builder().age(26).name("Ada").build()));
        people.countByIdIn(Set.of(1L, 2L));

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(template).count(captor.capture());
        Mockito.verifyNoMoreInteractions(template);
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.condition()).isNotEmpty();
            var condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(Condition.IN);
            soft.assertThat(condition.element().name()).isEqualTo("_id");
            soft.assertThat(condition.element().value().get()).isEqualTo(Set.of(1L, 2L));
        });

    }

    @Test
    void shouldExecuteExistBy() {

        var preparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(template.prepare(Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(template.query(Mockito.anyString()))
                .thenReturn(Stream.of(Person.builder().age(26).name("Ada").build()));

        people.existsByIdIn(Set.of(1L, 2L));

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(template).exists(captor.capture());
        Mockito.verifyNoMoreInteractions(template);
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.condition()).isNotEmpty();
            var condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(Condition.IN);
            soft.assertThat(condition.element().name()).isEqualTo("_id");
            soft.assertThat(condition.element().value().get()).isEqualTo(Set.of(1L, 2L));
        });

    }

    @Test
    void shouldReturnNotSupportedWhenQueryIsNotSelectAsDelete() {
        var preparedStatement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        Mockito.when(template.prepare(Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(template.query(Mockito.anyString()))
                .thenReturn(Stream.of(Person.builder().age(26).name("Ada").build()));
        Assertions.assertThatThrownBy(() -> people.deleteByNameReturnInt())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldReturnNotSupportedWhenQueryIsNotSelectAsUpdate() {
        var preparedStatement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        Mockito.when(template.prepare(Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(template.query(Mockito.anyString()))
                .thenReturn(Stream.of(Person.builder().age(26).name("Ada").build()));
        Assertions.assertThatThrownBy(() -> people.updateReturnInt())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}