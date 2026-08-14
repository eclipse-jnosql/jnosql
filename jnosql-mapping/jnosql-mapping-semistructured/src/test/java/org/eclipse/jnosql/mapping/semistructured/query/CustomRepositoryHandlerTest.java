/*
 *  Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
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
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.inject.Inject;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
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
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.eclipse.jnosql.mapping.semistructured.entities.Task;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Inject
    private LifecycleEventHandler lifecycleEventHandler;

    private People people;

    private Tasks tasks;

    private UpdatePersonRepository updatePersonRepository;

    private UpdateArrayPersonRepository updateArrayPersonRepository;

    private DeleteCountRepository deleteCountRepository;

    @BeforeEach
    void setUp() {
        template = Mockito.mock(SemiStructuredTemplate.class);
        CustomRepositoryHandler customRepositoryHandlerForPeople = CustomRepositoryHandler.builder()
                .entitiesMetadata(entitiesMetadata)
                .template(template)
                .lifecycleEventHandler(lifecycleEventHandler)
                .customRepositoryType(People.class)
                .converters(converters).build();

        people = (People) Proxy.newProxyInstance(People.class.getClassLoader(), new Class[]{People.class},
                customRepositoryHandlerForPeople);

        CustomRepositoryHandler customRepositoryHandlerForTasks = CustomRepositoryHandler.builder()
                .entitiesMetadata(entitiesMetadata)
                .template(template)
                .lifecycleEventHandler(lifecycleEventHandler)
                .customRepositoryType(Tasks.class)
                .converters(converters).build();

        var updateHandler = CustomRepositoryHandler.builder()
                .entitiesMetadata(entitiesMetadata)
                .template(template)
                .lifecycleEventHandler(lifecycleEventHandler)
                .customRepositoryType(UpdatePersonRepository.class)
                .converters(converters).build();

        var updateArrayHandler = CustomRepositoryHandler.builder()
                .entitiesMetadata(entitiesMetadata)
                .template(template)
                .lifecycleEventHandler(lifecycleEventHandler)
                .customRepositoryType(UpdateArrayPersonRepository.class)
                .converters(converters).build();

        var deleteCountHandler = CustomRepositoryHandler.builder()
                .entitiesMetadata(entitiesMetadata)
                .template(template)
                .lifecycleEventHandler(lifecycleEventHandler)
                .customRepositoryType(DeleteCountRepository.class)
                .converters(converters).build();

        tasks = (Tasks) Proxy.newProxyInstance(Tasks.class.getClassLoader(), new Class[]{Tasks.class},
                customRepositoryHandlerForTasks);

        updatePersonRepository = (UpdatePersonRepository) Proxy.newProxyInstance(UpdatePersonRepository.class.getClassLoader(),
                new Class[]{UpdatePersonRepository.class},
                updateHandler);

        updateArrayPersonRepository =
                (UpdateArrayPersonRepository) Proxy.newProxyInstance(UpdateArrayPersonRepository.class.getClassLoader(),
                new Class[]{UpdateArrayPersonRepository.class}, updateArrayHandler);

        deleteCountRepository =
                (DeleteCountRepository) Proxy.newProxyInstance(DeleteCountRepository.class.getClassLoader(),
                        new Class[]{DeleteCountRepository.class}, deleteCountHandler);

    }

    @DisplayName("Should insert entity")
    @Test
    void shouldInsertEntity() {
        Person person = Person.builder().age(26).name("Ada").build();
        Mockito.when(template.insert(person)).thenReturn(person);
        Person result = people.insert(person);

        Mockito.verify(template).insert(person);
        Mockito.verifyNoMoreInteractions(template);
        Assertions.assertThat(result).isEqualTo(person);
    }

    @DisplayName("Should insert list entity")
    @Test
    void shouldInsertListEntity() {
        var persons = List.of(Person.builder().age(26).name("Ada").build());
        Mockito.when(template.insert(persons)).thenReturn(persons);
        List<Person> result = people.insert(persons);

        Mockito.verify(template).insert(persons);
        Mockito.verifyNoMoreInteractions(template);
        Assertions.assertThat(result).isEqualTo(persons);
    }

    @DisplayName("Should insert array entity")
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


    @DisplayName("Should update entity")
    @Test
    void shouldUpdateEntity() {
        Person person = Person.builder().age(26).name("Ada").build();
        Mockito.when(template.update(person)).thenReturn(person);
        Person result = people.update(person);

        Mockito.verify(template).update(person);
        Mockito.verifyNoMoreInteractions(template);
        Assertions.assertThat(result).isEqualTo(person);
    }

    @DisplayName("Should update list entity")
    @Test
    void shouldUpdateListEntity() {
        var persons = List.of(Person.builder().age(26).name("Ada").build());
        Mockito.when(template.update(persons)).thenReturn(persons);
        List<Person> result = people.update(persons);

        Mockito.verify(template).update(persons);
        Mockito.verifyNoMoreInteractions(template);
        Assertions.assertThat(result).isEqualTo(persons);
    }

    @DisplayName("Should update array entity")
    @Test
    void shouldUpdateArrayEntity() {
        Person ada = Person.builder().age(26).name("Ada").build();
        var persons = new Person[]{ada};
        Mockito.when(template.update(Mockito.anyList())).thenReturn(List.of(ada));
        Person[] result = people.update(persons);

        Mockito.verify(template).update(List.of(ada));
        Mockito.verifyNoMoreInteractions(template);
        Assertions.assertThat(result).isEqualTo(persons);
    }

    @DisplayName("Should delete entity")
    @Test
    void shouldDeleteEntity() {
        Person person = Person.builder().id(1).age(26).name("Ada").build();
        people.delete(person);

        Mockito.verify(template).delete(Person.class, 1L);
        Mockito.verifyNoMoreInteractions(template);
    }

    @DisplayName("Should delete list entity")
    @Test
    void shouldDeleteListEntity() {
        var persons = List.of(Person.builder().id(12L).age(26).name("Ada").build());
        people.delete(persons);

        Mockito.verify(template).delete(Person.class, 12L);
        Mockito.verifyNoMoreInteractions(template);
    }

    @DisplayName("Should delete all")
    @Test
    void shouldDeleteAll() {
        people.deleteAll();

        Mockito.verify(template).deleteAll(Mockito.any());
        Mockito.verifyNoMoreInteractions(template);
    }

    @DisplayName("Should delete array entity")
    @Test
    void shouldDeleteArrayEntity() {
        Person ada = Person.builder().id(2L).age(26).name("Ada").build();
        var persons = new Person[]{ada};
        people.delete(persons);

        Mockito.verify(template).delete(Person.class, 2L);
        Mockito.verifyNoMoreInteractions(template);
    }

    @DisplayName("Should save entity")
    @Test
    void shouldSaveEntity() {
        Person person = Person.builder().age(26).name("Ada").build();
        Mockito.when(template.insert(person)).thenReturn(person);
        Person result = people.save(person);

        Mockito.verify(template).insert(person);
        Mockito.verify(template).find(Person.class, 0L);
        Assertions.assertThat(result).isEqualTo(person);
    }

    @DisplayName("Should save list entity")
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

    @DisplayName("Should save array entity")
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


    @DisplayName("Should execute object methods")
    @Test
    void shouldExecuteObjectMethods() {
        Assertions.assertThat(people.toString()).isNotNull();
        Assertions.assertThat(people.hashCode()).isNotEqualTo(0);
    }

    @DisplayName("Should execute default method")
    @Test
    void shouldExecuteDefaultMethod() {
        Assertions.assertThat(people.defaultMethod()).isEqualTo("default");
    }

    @DisplayName("Should execute find by age")
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

    @DisplayName("Should execute find by id")
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

    @DisplayName("Should execute find by id and name")
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

    @DisplayName("Should execute find pagination")
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

    @DisplayName("Should execute find cursor pagination")
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

    @DisplayName("Should execute path parameter")
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


    @DisplayName("Should execute query")
    @Test
    void shouldExecuteQuery() {

        var preparedStatement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        Mockito.when(template.prepare(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(template.prepare(Mockito.anyString()))
                .thenReturn(preparedStatement);

        Mockito.when(preparedStatement.result()).thenReturn(Stream.of(Person.builder().age(26).name("Ada").build()));
        var result = people.queryName("Ada");

        Assertions.assertThat(result).isNotNull().isInstanceOf(List.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(template).prepare(captor.capture(), Mockito.eq("Person"));
        Mockito.verifyNoMoreInteractions(template);
        var query = captor.getValue();

        Assertions.assertThat(query).isEqualTo("from Person where name = :name");
    }

    @DisplayName("Should execute query with void")
    @Test
    void shouldExecuteQueryWithVoid() {

        var preparedStatement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        Mockito.when(template.prepare(Mockito.anyString(), Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(template.prepare(Mockito.anyString())).thenReturn(preparedStatement);

        Mockito.when(preparedStatement.result()).thenReturn(Stream.of(Person.builder().age(26).name("Ada").build()));
        people.deleteByName("Ada");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(template).prepare(captor.capture(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(template);
        var query = captor.getValue();

        Assertions.assertThat(query).isEqualTo("delete from Person where name = :name");
    }

    @DisplayName("Should execute fixed query")
    @Test
    void shouldExecuteFixedQuery() {

        var preparedStatement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        Mockito.when(template.prepare(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(template.prepare(Mockito.anyString()))
                .thenReturn(preparedStatement);

        Mockito.when(preparedStatement.result()).thenReturn(Stream.of(Task.builder().description("refactor project A").build()));
        var result = tasks.listActiveTasks();

        Assertions.assertThat(result).isNotNull().isInstanceOf(List.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(template).prepare(captor.capture(), Mockito.eq("Task"));
        Mockito.verifyNoMoreInteractions(template);
        var query = captor.getValue();

        Assertions.assertThat(query).isEqualTo("from Task where active = true");

    }

    @DisplayName("Should execute count by")
    @Test
    void shouldExecuteCountBy() {

        var preparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(template.prepare(Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(template.prepare(Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.result()).thenReturn(Stream.of(Person.builder().age(26).name("Ada").build()));
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

    @DisplayName("Should execute exist by")
    @Test
    void shouldExecuteExistBy() {

        var preparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(template.prepare(Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(template.prepare(Mockito.anyString()))
                .thenReturn(preparedStatement);

        Mockito.when(preparedStatement.result()).thenReturn(Stream.of(Person.builder().age(26).name("Ada").build()));
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

    @DisplayName("Should use result for void delete query")
    @Test
    void shouldUseResultForVoidDeleteQuery() {
        var preparedStatement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        Mockito.when(template.prepare(Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(preparedStatement.isCount())
                .thenReturn(false);
        deleteCountRepository.deleteByName("Ada");
        Mockito.verify(preparedStatement).result();
        Mockito.verify(preparedStatement, Mockito.never()).count();
    }

    @DisplayName("Should return int number of deleted entities from delete query")
    @Test
    void shouldReturnIntNumberOfDeletedEntitiesFromDeleteQuery() {
        var preparedStatement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        Mockito.when(template.prepare(Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(preparedStatement.isCount())
                .thenReturn(false);
        Mockito.when(preparedStatement.count()).thenReturn(1L);
        Assertions.assertThat(deleteCountRepository.deleteByNameReturnInt("Ada")).isEqualTo(1);
        Mockito.verify(preparedStatement).count();
        Mockito.verify(preparedStatement, Mockito.never()).result();
    }

    @DisplayName("Should return long number of deleted entities from delete query")
    @Test
    void shouldReturnLongNumberOfDeletedEntitiesFromDeleteQuery() {
        var preparedStatement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        Mockito.when(template.prepare(Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(preparedStatement.isCount())
                .thenReturn(false);
        Mockito.when(preparedStatement.count()).thenReturn(3L);
        Assertions.assertThat(deleteCountRepository.deleteByNameReturnLong("Ada")).isEqualTo(3L);
        Mockito.verify(preparedStatement).count();
        Mockito.verify(preparedStatement, Mockito.never()).result();
    }

    @DisplayName("Should return number of updated entities from update query")
    @Test
    void shouldReturnNumberOfUpdatedEntitiesFromUpdateQuery() {
        var preparedStatement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        Mockito.when(template.prepare(Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(preparedStatement.isCount())
                .thenReturn(false);
        Mockito.when(preparedStatement.result())
                .thenReturn(Stream.of(Person.builder().age(26).name("Ada").build()));
        Assertions.assertThat(deleteCountRepository.updateReturnLong("Ada")).isEqualTo(1L);
        Mockito.verify(preparedStatement).result();
        Mockito.verify(preparedStatement, Mockito.never()).count();
    }

    @DisplayName("Should find all")
    @Test
    void shouldFindAll() {

        tasks.findAll();
        Mockito.verify(template).select(Mockito.any(SelectQuery.class));
    }

    @DisplayName("Should delete by name")
    @Test
    void shouldDeleteByName() {
        tasks.deleteByName("name");
        Mockito.verify(template).delete(Mockito.any(DeleteQuery.class));
    }

    @DisplayName("Should insert")
    @Test
    void shouldInsert(){
        Mockito.when(template.insert(Mockito.any(Person.class))).thenReturn(Person.builder().age(26).name("Ada").build());
        updatePersonRepository.insert(Person.builder().age(26).name("Ada").build());
        updateArrayPersonRepository.insert(new Person[]{Person.builder().age(26).name("Ada").build()});
    }


    @DisplayName("Should return long")
    @ParameterizedTest
    @ValueSource(strings = {"returnLong", "returnLongWrapper"})
    void shouldReturnLong(String methodName) {
        Method method = Arrays.stream(CustomRepositoryHandlerTest.class.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst().orElseThrow();
        Assertions.assertThat(CustomRepositoryHandler.returnsLong(method)).isTrue();
    }

    @DisplayName("Should return int")
    @ParameterizedTest
    @ValueSource(strings = {"returnInt", "returnIntWrapper"})
    void shouldReturnInt(String methodName) {
        Method method = Arrays.stream(CustomRepositoryHandlerTest.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("returnInt"))
                .findFirst().orElseThrow();
        Assertions.assertThat(CustomRepositoryHandler.returnsInt(method)).isTrue();
    }

    @DisplayName("Should return true for simple named parameter")
    @Test
    void shouldReturnTrueForSimpleNamedParameter() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from Person where age = :age");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isTrue();
    }

    @DisplayName("Should return false when only ordinal parameters present")
    @Test
    void shouldReturnFalseWhenOnlyOrdinalParametersPresent() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from Person where id = ?1 and age > ?2");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isFalse();
    }

    @DisplayName("Should return true when named parameter appears before ordinal")
    @Test
    void shouldReturnTrueWhenNamedParameterAppearsBeforeOrdinal() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from Person where name = :name and id = ?1");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isTrue();
    }

    @DisplayName("Should return false when ordinal appears before named even if named exists later")
    @Test
    void shouldReturnFalseWhenOrdinalAppearsBeforeNamedEvenIfNamedExistsLater() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from Person where id = ?1 and name = :name");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isFalse();
    }

    @DisplayName("Should support underscore and dollar in named parameter")
    @Test
    void shouldSupportUnderscoreAndDollarInNamedParameter() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from T where a = :_x and b = :$y and c = :a1_$");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isTrue();
    }

    @DisplayName("Should return false for invalid named starting with digit")
    @Test
    void shouldReturnFalseForInvalidNamedStartingWithDigit() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from T where a = :1abc");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isFalse();
    }

    @DisplayName("Should return false when no parameters present")
    @Test
    void shouldReturnFalseWhenNoParametersPresent() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from Person");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isFalse();
    }

    @DisplayName("Should return false for bare question mark without digits")
    @Test
    void shouldReturnFalseForBareQuestionMarkWithoutDigits() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from T where a = ?");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isFalse();
    }

    @DisplayName("Should return true for dotted identifier treating prefix as named param")
    @Test
    void shouldReturnTrueForDottedIdentifierTreatingPrefixAsNamedParam() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from T where owner = :user.name");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isTrue();
    }

    @DisplayName("Should return true when multiple named parameters exist")
    @Test
    void shouldReturnTrueWhenMultipleNamedParametersExist() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from T where a = :first and b = :second");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isTrue();
    }
    long returnLong() {
        return 1L;
    }

    Long returnLongWrapper() {
        return 1L;
    }

    int returnInt() {
        return 1;
    }

    Integer returnIntWrapper() {
        return 1;
    }


    @Repository
    public interface UpdatePersonRepository {

        @Insert
        void insert(Person person);
    }

    @Repository
    public interface UpdateArrayPersonRepository {

        @Insert
        void insert(Person[] person);
    }

    @Repository
    public interface DeleteCountRepository {

        @Query("delete from Person where name = :name")
        void deleteByName(@jakarta.data.repository.Param("name") String name);

        @Query("delete from Person where name = :name")
        int deleteByNameReturnInt(@jakarta.data.repository.Param("name") String name);

        @Query("delete from Person where name = :name")
        long deleteByNameReturnLong(@jakarta.data.repository.Param("name") String name);

        @Query("update Person where name = :name")
        long updateReturnLong(@jakarta.data.repository.Param("name") String name);
    }


    @Nested
    @DisplayName("When the custom repository handler is tested")
    class WhenTheCustomRepositoryHandlerIsTested {
    }
}
