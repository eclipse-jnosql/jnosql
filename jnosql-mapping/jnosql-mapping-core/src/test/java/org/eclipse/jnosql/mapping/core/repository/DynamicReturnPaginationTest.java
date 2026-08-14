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
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.CrudRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.concurrent.ThreadLocalRandom.current;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamicReturnPaginationTest {

    @Mock
    private Function<PageRequest, Stream<Person>> streamPagination;

    @Mock
    private Function<PageRequest, Optional<Person>> singlePagination;

    @Mock
    private BiFunction<PageRequest, LongSupplier, Page<Person>> page;



















    private Method method(Class<?> repository, String methodName) throws NoSuchMethodException {
        return Stream.of(repository.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst().get();

    }

    private record Person(String name) implements Comparable<Person> {
        @Override
        public int compareTo(Person o) {
            return name.compareTo(o.name);
        }
    }


    private interface PersonRepository extends CrudRepository<Person, String> {

        Optional<Person> getOptional();

        Person getInstance();

        List<Person> getList();

        Iterable<Person> getIterable();

        Collection<Person> getCollection();

        Set<Person> getSet();

        Queue<Person> getQueue();

        Stream<Person> getStream();

        SortedSet<Person> getSortedSet();

        NavigableSet<Person> getNavigableSet();

        Deque<Person> getDeque();

        Page<Person> getPage();
    }

    private long getRandomLong() {
        return current().nextLong(1, 10);
    }

    private PageRequest getPagination() {
        return PageRequest.ofPage(getRandomLong()).size((int) getRandomLong());
    }


    @Nested
    @DisplayName("When the dynamic return pagination operates")
    class WhenTheDynamicReturnPaginationOperates {

        @SuppressWarnings("unchecked")
        @DisplayName("Should return empty optional")
        @Test
        void shouldReturnEmptyOptional() throws NoSuchMethodException {

            Method method = method(PersonRepository.class, "getOptional");
            Supplier<Stream<?>> stream = Stream::empty;
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);

            PageRequest pageRequest = getPagination();

            when(singlePagination.apply(pageRequest)).thenReturn(Optional.empty());

            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .singleResult(singleResult)
                    .result(stream)
                    .pagination(pageRequest)
                    .streamPagination(streamPagination)
                    .singleResultPagination(singlePagination)
                    .page(page)
                    .totalSupplier(() -> 1L)
                    .build();
            Object execute = dynamicReturn.execute();

            assertThat(execute).isInstanceOf(Optional.class);
            Optional<Person> optional = (Optional) execute;
            assertThat(optional.isPresent()).isFalse();

            Mockito.verify(singlePagination).apply(pageRequest);
            Mockito.verify(streamPagination, Mockito.never()).apply(pageRequest);
        }
        @SuppressWarnings("unchecked")
        @DisplayName("Should return optional")
        @Test
        void shouldReturnOptional() throws NoSuchMethodException {

            Method method = method(PersonRepository.class, "getOptional");
            Supplier<Stream<?>> stream = Stream::empty;
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            PageRequest pageRequest = getPagination();

            when(singlePagination.apply(pageRequest)).thenReturn(Optional.of(new Person("Ada")));

            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .result(stream)
                    .singleResult(singleResult)
                    .pagination(pageRequest)
                    .streamPagination(streamPagination)
                    .singleResultPagination(singlePagination)
                    .page(page)
                    .totalSupplier(() -> 1L)
                    .build();

            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Optional.class);
            Optional<Person> optional = (Optional) execute;
            assertThat(optional.isPresent()).isTrue();
            assertThat(optional.get()).isEqualTo(new Person("Ada"));
            Mockito.verify(singlePagination).apply(pageRequest);
            Mockito.verify(streamPagination, Mockito.never()).apply(pageRequest);
        }
        @SuppressWarnings("unchecked")
        @DisplayName("Should return an instance")
        @Test
        void shouldReturnAnInstance() throws NoSuchMethodException {
            Method method = method(PersonRepository.class, "getInstance");
            Supplier<Stream<?>> stream = Stream::empty;
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);

            PageRequest pageRequest = getPagination();
            when(singlePagination.apply(pageRequest)).thenReturn(Optional.of(new Person("Ada")));

            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .result(stream)
                    .singleResult(singleResult)
                    .pagination(pageRequest)
                    .streamPagination(streamPagination)
                    .singleResultPagination(singlePagination)
                    .page(page)
                    .totalSupplier(() -> 1L)
                    .build();

            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Person.class);
            Person person = (Person) execute;
            assertThat(person).isEqualTo(new Person("Ada"));
            Mockito.verify(singlePagination).apply(pageRequest);
            Mockito.verify(streamPagination, Mockito.never()).apply(pageRequest);
        }
        @SuppressWarnings("unchecked")
        @DisplayName("Should return empty result exception")
        @Test
        void shouldReturnEmptyResultException() throws NoSuchMethodException {

            Method method = method(PersonRepository.class, "getInstance");
            Supplier<Stream<?>> stream = Stream::empty;
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);

            PageRequest pageRequest = getPagination();
            when(singlePagination.apply(pageRequest)).thenReturn(Optional.empty());

            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .result(stream)
                    .singleResult(singleResult)
                    .pagination(pageRequest)
                    .streamPagination(streamPagination)
                    .singleResultPagination(singlePagination)
                    .page(page)
                    .totalSupplier(() -> 1L)
                    .build();

            assertThatExceptionOfType(EmptyResultException.class).isThrownBy(dynamicReturn::execute);

            Mockito.verify(singlePagination).apply(pageRequest);
            Mockito.verify(streamPagination, Mockito.never()).apply(pageRequest);
        }
        @SuppressWarnings("unchecked")
        @DisplayName("Should return list")
        @Test
        void shouldReturnList() throws NoSuchMethodException {

            Method method = method(PersonRepository.class, "getList");
            Supplier<Stream<?>> stream = Stream::empty;
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            PageRequest pageRequest = getPagination();
            when(streamPagination.apply(pageRequest)).thenReturn(Stream.of(new Person("Ada")));

            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .result(stream)
                    .singleResult(singleResult)
                    .pagination(pageRequest)
                    .streamPagination(streamPagination)
                    .singleResultPagination(singlePagination)
                    .page(page)
                    .totalSupplier(() -> 1L)
                    .build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(List.class);
            List<Person> persons = (List) execute;
            assertThat(persons.isEmpty()).isFalse();
            assertThat(persons.getFirst()).isEqualTo(new Person("Ada"));

            Mockito.verify(singlePagination, Mockito.never()).apply(pageRequest);
            Mockito.verify(streamPagination).apply(pageRequest);
        }
        @SuppressWarnings("unchecked")
        @DisplayName("Should return iterable")
        @Test
        void shouldReturnIterable() throws NoSuchMethodException {

            Method method = method(PersonRepository.class, "getIterable");
            Supplier<Stream<?>> stream = Stream::empty;
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            PageRequest pageRequest = getPagination();
            when(streamPagination.apply(pageRequest)).thenReturn(Stream.of(new Person("Ada")));

            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .result(stream)
                    .singleResult(singleResult)
                    .pagination(pageRequest)
                    .streamPagination(streamPagination)
                    .singleResultPagination(singlePagination)
                    .page(page)
                    .totalSupplier(() -> 1L)
                    .build();

            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Iterable.class);
            Iterable<Person> persons = (List) execute;
            assertThat(persons.iterator().next()).isEqualTo(new Person("Ada"));
            Mockito.verify(singlePagination, Mockito.never()).apply(pageRequest);
            Mockito.verify(streamPagination).apply(pageRequest);
        }
        @SuppressWarnings("unchecked")
        @DisplayName("Should return collection")
        @Test
        void shouldReturnCollection() throws NoSuchMethodException {

            Method method = method(PersonRepository.class, "getCollection");
            Supplier<Stream<?>> stream = Stream::empty;
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            PageRequest pageRequest = getPagination();
            when(streamPagination.apply(pageRequest)).thenReturn(Stream.of(new Person("Ada")));

            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .result(stream)
                    .singleResult(singleResult)
                    .pagination(pageRequest)
                    .streamPagination(streamPagination)
                    .singleResultPagination(singlePagination)
                    .page(page)
                    .totalSupplier(() -> 1L)
                    .build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Collection.class);
            Collection<Person> persons = (Collection) execute;
            assertThat(persons.isEmpty()).isFalse();
            assertThat(persons.iterator().next()).isEqualTo(new Person("Ada"));
            Mockito.verify(singlePagination, Mockito.never()).apply(pageRequest);
            Mockito.verify(streamPagination).apply(pageRequest);
        }
        @SuppressWarnings("unchecked")
        @DisplayName("Should return set")
        @Test
        void shouldReturnSet() throws NoSuchMethodException {

            Method method = method(PersonRepository.class, "getSet");
            Supplier<Stream<?>> stream = Stream::empty;
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            PageRequest pageRequest = getPagination();
            when(streamPagination.apply(pageRequest)).thenReturn(Stream.of(new Person("Ada")));

            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .result(stream)
                    .singleResult(singleResult)
                    .pagination(pageRequest)
                    .streamPagination(streamPagination)
                    .singleResultPagination(singlePagination)
                    .page(page)
                    .totalSupplier(() -> 1L)
                    .build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Set.class);
            Set<Person> persons = (Set) execute;
            assertThat(persons.isEmpty()).isFalse();
            assertThat(persons.iterator().next()).isEqualTo(new Person("Ada"));
            Mockito.verify(singlePagination, Mockito.never()).apply(pageRequest);
            Mockito.verify(streamPagination).apply(pageRequest);
        }
        @SuppressWarnings("unchecked")
        @DisplayName("Should return queue")
        @Test
        void shouldReturnQueue() throws NoSuchMethodException {

            Method method = method(PersonRepository.class, "getQueue");
            Supplier<Stream<?>> stream = Stream::empty;
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            PageRequest pageRequest = getPagination();
            when(streamPagination.apply(pageRequest)).thenReturn(Stream.of(new Person("Ada")));

            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .result(stream)
                    .singleResult(singleResult)
                    .pagination(pageRequest)
                    .streamPagination(streamPagination)
                    .singleResultPagination(singlePagination)
                    .page(page)
                    .totalSupplier(() -> 1L)
                    .build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Queue.class);
            Queue<Person> persons = (Queue) execute;
            assertThat(persons.isEmpty()).isFalse();
            assertThat(persons.iterator().next()).isEqualTo(new Person("Ada"));
            Mockito.verify(singlePagination, Mockito.never()).apply(pageRequest);
            Mockito.verify(streamPagination).apply(pageRequest);
        }
        @SuppressWarnings("unchecked")
        @DisplayName("Should return stream")
        @Test
        void shouldReturnStream() throws NoSuchMethodException {

            Method method = method(PersonRepository.class, "getStream");
            Supplier<Stream<?>> stream = Stream::empty;
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            PageRequest pageRequest = getPagination();
            when(streamPagination.apply(pageRequest)).thenReturn(Stream.of(new Person("Ada")));

            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .result(stream)
                    .singleResult(singleResult)
                    .pagination(pageRequest)
                    .streamPagination(streamPagination)
                    .singleResultPagination(singlePagination)
                    .page(page)
                    .totalSupplier(() -> 1L)
                    .build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Stream.class);
            Stream<Person> persons = (Stream) execute;
            assertThat(persons.iterator().next()).isEqualTo(new Person("Ada"));
            Mockito.verify(singlePagination, Mockito.never()).apply(pageRequest);
            Mockito.verify(streamPagination).apply(pageRequest);
        }
        @SuppressWarnings("unchecked")
        @DisplayName("Should return sorted set")
        @Test
        void shouldReturnSortedSet() throws NoSuchMethodException {

            Method method = method(PersonRepository.class, "getSortedSet");
            Supplier<Stream<?>> stream = Stream::empty;
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            PageRequest pageRequest = getPagination();
            when(streamPagination.apply(pageRequest)).thenReturn(Stream.of(new Person("Ada")));

            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .result(stream)
                    .singleResult(singleResult)
                    .pagination(pageRequest)
                    .streamPagination(streamPagination)
                    .singleResultPagination(singlePagination)
                    .page(page)
                    .totalSupplier(() -> 1L)
                    .build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(SortedSet.class);
            SortedSet<Person> persons = (SortedSet) execute;
            assertThat(persons.isEmpty()).isFalse();
            assertThat(persons.getFirst()).isEqualTo(new Person("Ada"));
            Mockito.verify(singlePagination, Mockito.never()).apply(pageRequest);
            Mockito.verify(streamPagination).apply(pageRequest);
        }
        @SuppressWarnings("unchecked")
        @DisplayName("Should return navigable set")
        @Test
        void shouldReturnNavigableSet() throws NoSuchMethodException {

            Method method = method(PersonRepository.class, "getNavigableSet");
            Supplier<Stream<?>> stream = Stream::empty;
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            PageRequest pageRequest = getPagination();
            when(streamPagination.apply(pageRequest)).thenReturn(Stream.of(new Person("Ada")));

            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .result(stream)
                    .singleResult(singleResult)
                    .pagination(pageRequest)
                    .streamPagination(streamPagination)
                    .singleResultPagination(singlePagination)
                    .page(page)
                    .totalSupplier(() -> 1L)
                    .build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(NavigableSet.class);
            NavigableSet<Person> persons = (NavigableSet) execute;
            assertThat(persons.isEmpty()).isFalse();
            assertThat(persons.getFirst()).isEqualTo(new Person("Ada"));
            Mockito.verify(singlePagination, Mockito.never()).apply(pageRequest);
            Mockito.verify(streamPagination).apply(pageRequest);
        }
        @SuppressWarnings("unchecked")
        @DisplayName("Should return deque")
        @Test
        void shouldReturnDeque() throws NoSuchMethodException {

            Method method = method(PersonRepository.class, "getDeque");
            Supplier<Stream<?>> stream = Stream::empty;
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            PageRequest pageRequest = getPagination();
            when(streamPagination.apply(pageRequest)).thenReturn(Stream.of(new Person("Ada")));

            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .result(stream)
                    .singleResult(singleResult)
                    .pagination(pageRequest)
                    .streamPagination(streamPagination)
                    .singleResultPagination(singlePagination)
                    .page(page)
                    .totalSupplier(() -> 1L)
                    .build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Deque.class);
            Deque<Person> persons = (Deque) execute;
            assertThat(persons.isEmpty()).isFalse();
            assertThat(persons.getFirst()).isEqualTo(new Person("Ada"));
            Mockito.verify(singlePagination, Mockito.never()).apply(pageRequest);
            Mockito.verify(streamPagination).apply(pageRequest);
        }
        @SuppressWarnings("unchecked")
        @DisplayName("Should return error when execute page")
        @Test
        void shouldReturnErrorWhenExecutePage() throws NoSuchMethodException {
            Method method = method(PersonRepository.class, "getPage");
            Supplier<Stream<?>> stream = Stream::empty;
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            PageRequest pageRequest = getPagination();
            LongSupplier supplier = () -> 1L;
            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .result(stream)
                    .singleResult(singleResult)
                    .pagination(pageRequest)
                    .streamPagination(streamPagination)
                    .singleResultPagination(singlePagination)
                    .page(page)
                    .totalSupplier(supplier)
                    .build();

            dynamicReturn.execute();
            Mockito.verify(singlePagination, Mockito.never()).apply(pageRequest);
            Mockito.verify(streamPagination, Mockito.never()).apply(pageRequest);
            Mockito.verify(page).apply(pageRequest, supplier);
        }
    }
}
