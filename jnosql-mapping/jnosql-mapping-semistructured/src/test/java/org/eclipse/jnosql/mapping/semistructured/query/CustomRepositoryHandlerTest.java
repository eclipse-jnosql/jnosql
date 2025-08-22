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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

    private UpdatePersonRepository updatePersonRepository;

    private UpdateArrayPersonRepository updateArrayPersonRepository;

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

        var updateHandler = CustomRepositoryHandler.builder()
                .entitiesMetadata(entitiesMetadata)
                .template(template)
                .customRepositoryType(UpdatePersonRepository.class)
                .converters(converters).build();

        var updateArrayHandler = CustomRepositoryHandler.builder()
                .entitiesMetadata(entitiesMetadata)
                .template(template)
                .customRepositoryType(UpdateArrayPersonRepository.class)
                .converters(converters).build();

        tasks = (Tasks) Proxy.newProxyInstance(Tasks.class.getClassLoader(), new Class[]{Tasks.class},
                customRepositoryHandlerForTasks);

        updatePersonRepository = (UpdatePersonRepository) Proxy.newProxyInstance(UpdatePersonRepository.class.getClassLoader(),
                new Class[]{UpdatePersonRepository.class},
                updateHandler);

        updateArrayPersonRepository =
                (UpdateArrayPersonRepository) Proxy.newProxyInstance(UpdateArrayPersonRepository.class.getClassLoader(),
                new Class[]{UpdateArrayPersonRepository.class}, updateArrayHandler);

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
    void shouldReturnNumberOfDeletedEntitiesFromDeleteQuery() {
        var preparedStatement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        Mockito.when(template.prepare(Mockito.anyString(), Mockito.any())).thenReturn(preparedStatement);
        Mockito.when(preparedStatement.isCount())
                .thenReturn(false);
        Mockito.when(preparedStatement.singleResult())
                .thenReturn(Optional.of(1L));
        Assertions.assertThat(people.deleteByNameReturnLong("Ada")).isEqualTo(1L);
    }

    @Test
    void shouldReturnNumberOfUpdatedEntitiesFromUpdateQuery() {
        var preparedStatement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        Mockito.when(template.prepare(Mockito.anyString(), Mockito.any())).thenReturn(preparedStatement);
        Mockito.when(preparedStatement.isCount())
                .thenReturn(false);
        Mockito.when(preparedStatement.singleResult())
                .thenReturn(Optional.of(1L));
        Assertions.assertThat(people.updateReturnLong("Ada")).isEqualTo(1L);
    }

    @Test
    void shouldFindAll() {

        tasks.findAll();
        Mockito.verify(template).select(Mockito.any(SelectQuery.class));
    }

    @Test
    void shouldDeleteByName() {
        tasks.deleteByName("name");
        Mockito.verify(template).delete(Mockito.any(DeleteQuery.class));
    }

    @Test
    void shouldInsert(){
        updatePersonRepository.insert(Person.builder().age(26).name("Ada").build());
        updateArrayPersonRepository.insert(new Person[]{Person.builder().age(26).name("Ada").build()});
    }


    @ParameterizedTest
    @ValueSource(strings = {"returnLong", "returnLongWrapper"})
    void shouldReturnLong(String methodName) {
        Method method = Arrays.stream(CustomRepositoryHandlerTest.class.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst().orElseThrow();
        Assertions.assertThat(CustomRepositoryHandler.returnsLong(method)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"returnInt", "returnIntWrapper"})
    void shouldReturnInt(String methodName) {
        Method method = Arrays.stream(CustomRepositoryHandlerTest.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("returnInt"))
                .findFirst().orElseThrow();
        Assertions.assertThat(CustomRepositoryHandler.returnsInt(method)).isTrue();
    }

    @Test
    void shouldReturnTrueForSimpleNamedParameter() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from Person where age = :age");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenOnlyOrdinalParametersPresent() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from Person where id = ?1 and age > ?2");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueWhenNamedParameterAppearsBeforeOrdinal() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from Person where name = :name and id = ?1");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenOrdinalAppearsBeforeNamedEvenIfNamedExistsLater() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from Person where id = ?1 and name = :name");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isFalse();
    }

    @Test
    void shouldSupportUnderscoreAndDollarInNamedParameter() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from T where a = :_x and b = :$y and c = :a1_$");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseForInvalidNamedStartingWithDigit() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from T where a = :1abc");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenNoParametersPresent() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from Person");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseForBareQuestionMarkWithoutDigits() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from T where a = ?");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueForDottedIdentifierTreatingPrefixAsNamedParam() {
        var query = Mockito.mock(Query.class);
        Mockito.when(query.value()).thenReturn("select * from T where owner = :user.name");
        boolean result = CustomRepositoryHandler.queryContainsNamedParameters(query);
        assertThat(result).isTrue();
    }

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

}
