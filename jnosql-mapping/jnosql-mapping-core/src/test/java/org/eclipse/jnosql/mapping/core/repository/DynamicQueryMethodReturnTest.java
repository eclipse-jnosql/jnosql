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
package org.eclipse.jnosql.mapping.core.repository;

import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;

import jakarta.data.exceptions.EmptyResultException;
import jakarta.data.exceptions.NonUniqueResultException;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.PreparedStatement;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

class DynamicQueryMethodReturnTest {
















    private Method getMethod(Class<?> repository, String methodName) throws NoSuchMethodException {
        return Stream.of(repository.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst().get();

    }

    private record Person(String name) {

    }

    private interface PersonRepository extends CrudRepository<Person, String> {

        @Query("query")
        Optional<Person> getOptional();

        @Query("query")
        Person getInstance();

        @Query("query")
        List<Person> getList();

        @Query("query")
        Iterable<Person> getIterable();

        @Query("query")
        Collection<Person> getCollection();

        @Query("query")
        Set<Person> getSet();

        @Query("query")
        Queue<Person> getQueue();

        @Query("query")
        Stream<Person> getStream();

        @Query("query")
        List<Person> query(@Param("name") String name);

        @Query("select count(this) from Person")
        long count();

        @Query("query")
        Page<Person> page(@Param("name") String name, PageRequest pageRequest);
    }


    @Nested
    @DisplayName("When the dynamic query method return operates")
    class WhenTheDynamicQueryMethodReturnOperates {

        @DisplayName("Should return empty optional")
        @Test
        void shouldReturnEmptyOptional() throws NoSuchMethodException {

            PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
            Method method = getMethod(PersonRepository.class, "getOptional");

            Mockito.when(preparedStatement.result()).thenReturn(Stream.empty());
            var dynamicReturn = DynamicQueryMethodReturn.builder()
                    .typeClass(Person.class)
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .querySupplier(() -> RepositoryReflectionUtils.INSTANCE.getQuery(method))
                    .prepareConverter(s -> preparedStatement)
                    .paramsSupplier(() -> RepositoryReflectionUtils.INSTANCE.getParams(method, new Object[]{"Ada"}))
                    .totalSupplier(() -> 0L)
                    .build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Optional.class);
            Optional<Person> optional = (Optional) execute;
            assertThat(optional.isPresent()).isFalse();
        }
        @SuppressWarnings("unchecked")
        @DisplayName("Should return optional")
        @Test
        void shouldReturnOptional() throws NoSuchMethodException {

            PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
            Method method = getMethod(PersonRepository.class, "getOptional");
           Mockito.when(preparedStatement.result()).thenReturn(Stream.of(new Person("Ada")));
            Mockito.when(preparedStatement.singleResult()).thenReturn(Optional.of(new Person("Ada")));

            var dynamicReturn = DynamicQueryMethodReturn.builder()
                    .typeClass(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .querySupplier(() -> RepositoryReflectionUtils.INSTANCE.getQuery(method))
                    .paramsSupplier(() -> RepositoryReflectionUtils.INSTANCE.getParams(method, new Object[]{"Ada"}))
                    .prepareConverter(s -> preparedStatement)
                    .totalSupplier(() -> 0L)
                    .build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Optional.class);
            Optional<Person> optional = (Optional<Person> ) execute;
            assertThat(optional.isPresent()).isTrue();
            assertThat(optional.get()).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return optional error")
        @Test
        void shouldReturnOptionalError() throws NoSuchMethodException {

            PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
            Method method = getMethod(PersonRepository.class, "getOptional");

            Mockito.when(preparedStatement.result()).thenReturn(Stream.of(new Person("Poliana"), new
                    Person("Otavio")));
            Mockito.when(preparedStatement.singleResult()).thenThrow(new NonUniqueResultException(""));
            var dynamicReturn = DynamicQueryMethodReturn.builder()
                    .typeClass(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .querySupplier(() -> RepositoryReflectionUtils.INSTANCE.getQuery(method))
                    .paramsSupplier(() -> RepositoryReflectionUtils.INSTANCE.getParams(method, new Object[]{"Ada"}))
                    .prepareConverter(s -> preparedStatement)
                    .totalSupplier(() -> 0L)
                    .build();

            assertThatExceptionOfType(NonUniqueResultException.class).isThrownBy(dynamicReturn::execute);
        }
        @DisplayName("Should return an instance")
        @Test
        void shouldReturnAnInstance() throws NoSuchMethodException {

            PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
            Method method = getMethod(PersonRepository.class, "getInstance");

            Mockito.when(preparedStatement.result()).thenReturn(Stream.of(new Person("Ada")));
            Mockito.when(preparedStatement.singleResult()).thenReturn(Optional.of(new Person("Ada")));
            var dynamicReturn = DynamicQueryMethodReturn.builder()
                    .typeClass(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .querySupplier(() -> RepositoryReflectionUtils.INSTANCE.getQuery(method))
                    .paramsSupplier(() -> RepositoryReflectionUtils.INSTANCE.getParams(method, new Object[]{"Ada"}))
                    .prepareConverter(s -> preparedStatement)
                    .totalSupplier(() -> 0L)
                    .build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Person.class);
            Person person = (Person) execute;
            assertThat(person).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return empty result exception")
        @Test
        void shouldReturnEmptyResultException() throws NoSuchMethodException {

            PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
            Method method = getMethod(PersonRepository.class, "getInstance");

            Mockito.when(preparedStatement.result()).thenReturn(Stream.empty());
            var dynamicReturn = DynamicQueryMethodReturn.builder()
                    .typeClass(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .querySupplier(() -> RepositoryReflectionUtils.INSTANCE.getQuery(method))
                    .paramsSupplier(() -> RepositoryReflectionUtils.INSTANCE.getParams(method, new Object[]{"Ada"}))
                    .prepareConverter(s -> preparedStatement)
                    .totalSupplier(() -> 0L)
                    .build();

            assertThatExceptionOfType(EmptyResultException.class).isThrownBy(dynamicReturn::execute);

        }
        @DisplayName("Should return list")
        @Test
        void shouldReturnList() throws NoSuchMethodException {

            PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
            Method method = getMethod(PersonRepository.class, "getList");

            Mockito.when(preparedStatement.result()).thenReturn(Stream.of(new Person("Ada")));
            var dynamicReturn = DynamicQueryMethodReturn.builder()
                    .typeClass(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .querySupplier(() -> RepositoryReflectionUtils.INSTANCE.getQuery(method))
                    .paramsSupplier(() -> RepositoryReflectionUtils.INSTANCE.getParams(method, new Object[]{"Ada"}))
                    .prepareConverter(s -> preparedStatement)
                    .totalSupplier(() -> 0L)
                    .build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(List.class);
            List<Person> persons = (List) execute;
            assertThat(persons.isEmpty()).isFalse();
            assertThat(persons.getFirst()).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return iterable")
        @Test
        void shouldReturnIterable() throws NoSuchMethodException {

            PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
            Method method = getMethod(PersonRepository.class, "getIterable");

            Mockito.when(preparedStatement.result()).thenReturn(Stream.of(new Person("Ada")));
            var dynamicReturn = DynamicQueryMethodReturn.builder()
                    .typeClass(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .querySupplier(() -> RepositoryReflectionUtils.INSTANCE.getQuery(method))
                    .paramsSupplier(() -> RepositoryReflectionUtils.INSTANCE.getParams(method, new Object[]{"Ada"}))
                    .prepareConverter(s -> preparedStatement)
                    .totalSupplier(() -> 0L)
                    .build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Iterable.class);
            Iterable<Person> persons = (List) execute;
            assertThat(persons.iterator().next()).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return collection")
        @Test
        void shouldReturnCollection() throws NoSuchMethodException {
            PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
            Method method = getMethod(PersonRepository.class, "getCollection");

            Mockito.when(preparedStatement.result()).thenReturn(Stream.of(new Person("Ada")));
            var dynamicReturn = DynamicQueryMethodReturn.builder()
                    .typeClass(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .querySupplier(() -> RepositoryReflectionUtils.INSTANCE.getQuery(method))
                    .paramsSupplier(() -> RepositoryReflectionUtils.INSTANCE.getParams(method, new Object[]{"Ada"}))
                    .prepareConverter(s -> preparedStatement)
                    .totalSupplier(() -> 0L)
                    .build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Collection.class);
            Collection<Person> persons = (Collection) execute;
            assertThat(persons.isEmpty()).isFalse();
            assertThat(persons.iterator().next()).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return queue")
        @Test
        void shouldReturnQueue() throws NoSuchMethodException {
            PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
            Method method = getMethod(PersonRepository.class, "getQueue");

            Mockito.when(preparedStatement.result()).thenReturn(Stream.of(new Person("Ada")));
            var dynamicReturn = DynamicQueryMethodReturn.builder()
                    .typeClass(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .querySupplier(() -> RepositoryReflectionUtils.INSTANCE.getQuery(method))
                    .paramsSupplier(() -> RepositoryReflectionUtils.INSTANCE.getParams(method, new Object[]{"Ada"}))
                    .prepareConverter(s -> preparedStatement)
                    .totalSupplier(() -> 0L)
                    .build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Queue.class);
            Queue<Person> persons = (Queue) execute;
            assertThat(persons.isEmpty()).isFalse();
            assertThat(persons.iterator().next()).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return stream")
        @Test
        void shouldReturnStream() throws NoSuchMethodException {
            PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
            Method method = getMethod(PersonRepository.class, "getStream");

            Mockito.when(preparedStatement.result()).thenReturn(Stream.of(new Person("Ada")));
            var dynamicReturn = DynamicQueryMethodReturn.builder()
                    .typeClass(Person.class)
                    .methodName(method.getName())
                    .querySupplier(() -> RepositoryReflectionUtils.INSTANCE.getQuery(method))
                    .paramsSupplier(() -> RepositoryReflectionUtils.INSTANCE.getParams(method, new Object[]{"Ada"}))
                    .returnType(method.getReturnType())
                    .prepareConverter(s -> preparedStatement)
                    .totalSupplier(() -> 0L)
                    .build();

            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Stream.class);
            Stream<Person> persons = (Stream) execute;
            assertThat(persons.iterator().next()).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return from prepare statement")
        @Test
        void shouldReturnFromPrepareStatement() throws NoSuchMethodException {
            PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
            Mockito.when(preparedStatement.<Person>result())
                    .thenReturn(Stream.of(new Person("Ada")));

            Method method = getMethod(PersonRepository.class, "query");

            Mockito.when(preparedStatement.result()).thenReturn(Stream.of(new Person("Ada")));
            var dynamicReturn = DynamicQueryMethodReturn.builder()
                    .typeClass(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .querySupplier(() -> RepositoryReflectionUtils.INSTANCE.getQuery(method))
                    .paramsSupplier(() -> RepositoryReflectionUtils.INSTANCE.getParams(method, new Object[]{"Ada"}))
                    .args(new Object[]{"Ada"})
                    .prepareConverter(s -> preparedStatement)
                    .totalSupplier(() -> 0L)
                    .build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Iterable.class);
            Iterable<Person> persons = (List) execute;
            assertThat(persons.iterator().next()).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return long")
        @Test
        void shouldReturnLong() throws NoSuchMethodException {
            var preparedStatement = Mockito.mock(PreparedStatement.class);
            Mockito.when(preparedStatement.count()).thenReturn(1L);
            Mockito.when(preparedStatement.isCount()).thenReturn(true);
            Method method = getMethod(PersonRepository.class, "count");


            var dynamicReturn = DynamicQueryMethodReturn.builder()
                    .typeClass(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .querySupplier(() -> RepositoryReflectionUtils.INSTANCE.getQuery(method))
                    .paramsSupplier(() -> RepositoryReflectionUtils.INSTANCE.getParams(method, new Object[]{"Ada"}))
                    .prepareConverter(s -> preparedStatement)
                    .totalSupplier(() -> 0L)
                    .build();

            Object execute = dynamicReturn.execute();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(execute).isInstanceOf(Long.class);
                soft.assertThat(execute).isEqualTo(1L);
            });
        }
        @SuppressWarnings("unchecked")
        @DisplayName("Should return page")
        @Test
        void shouldReturnPage() throws NoSuchMethodException {
            PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
            Mockito.when(preparedStatement.<Person>result())
                    .thenReturn(Stream.of(new Person("Ada")));

            Method method = getMethod(PersonRepository.class, "page");

            Mockito.when(preparedStatement.result()).thenReturn(Stream.of(new Person("Ada")));
            var dynamicReturn = DynamicQueryMethodReturn.builder()
                    .typeClass(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .querySupplier(() -> RepositoryReflectionUtils.INSTANCE.getQuery(method))
                    .paramsSupplier(() -> RepositoryReflectionUtils.INSTANCE.getParams(method, new Object[]{"Ada"}))
                    .args(new Object[]{"Ada", PageRequest.ofPage(10)})
                    .prepareConverter(s -> preparedStatement)
                    .pageRequest(PageRequest.ofPage(10))
                    .totalSupplier(() -> 0L)
                    .build();
            Object execute = dynamicReturn.execute();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(execute).isInstanceOf(Page.class);
                Page<Person> page = (Page<Person>) execute;
                soft.assertThat(page.content()).containsExactly(new Person("Ada"));
                soft.assertThat(page.pageRequest()).isEqualTo(PageRequest.ofPage(10));
            });
        }
    }
}
