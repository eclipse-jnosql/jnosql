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
import org.eclipse.jnosql.mapping.DynamicQueryException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;


@SuppressWarnings("unchecked")
class DynamicReturnTest {

    private static final Function<String, String> SORT_MAPPER = Function.identity();




























    private Method method(Class<?> repository, String methodName) {
        return Stream.of(repository.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst().get();

    }


    private record Animal(String name) {


    }

    private record Person(String name) implements Comparable<Person> {

        @Override
        public int compareTo(Person o) {
            return name.compareTo(o.name);
        }
    }

    private interface AnimalRepository extends CrudRepository<Animal, String> {

        SortedSet<Person> getSortedSet();
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



    @Nested
    @DisplayName("When the dynamic return operates")
    class WhenTheDynamicReturnOperates {

        @DisplayName("Should return npewhen there is pagination")
        @Test
        void shouldReturnNPEWhenThereIsPagination() {
            Method method = method(PersonRepository.class, "getOptional");
            Supplier<Stream<?>> stream = Stream::empty;
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            assertThatExceptionOfType(NullPointerException.class).isThrownBy(() ->
                    DynamicReturn.builder()
                            .classSource(Person.class)
                            .methodName(method.getName())
                            .returnType(method.getReturnType())
                            .result(stream)
                            .singleResult(singleResult)
                            .pagination(PageRequest.ofPage(1L).size(2)).build());

        }
        @DisplayName("Should return empty optional")
        @Test
        void shouldReturnEmptyOptional() {

            Method method = method(PersonRepository.class, "getOptional");
            Supplier<Stream<?>> stream = Stream::empty;
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .result(stream)
                    .singleResult(singleResult).build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Optional.class);
            Optional<Person> optional = (Optional) execute;
            assertThat(optional.isPresent()).isFalse();
        }
        @DisplayName("Should return optional")
        @Test
        void shouldReturnOptional() {

            Method method = method(PersonRepository.class, "getOptional");
            Supplier<Stream<?>> stream = () -> Stream.of(new Person("Ada"));
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .result(stream)
                    .singleResult(singleResult).build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Optional.class);
            Optional<Person> optional = (Optional) execute;
            assertThat(optional.isPresent()).isTrue();
            assertThat(optional.get()).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return optional error")
        @Test
        void shouldReturnOptionalError() {

            Method method = method(PersonRepository.class, "getOptional");
            Supplier<Stream<?>> stream = () -> Stream.of(new Person("Poliana"), new Person("Otavio"));
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .result(stream)
                    .singleResult(singleResult).build();

            assertThatExceptionOfType(NonUniqueResultException.class).isThrownBy(dynamicReturn::execute);

        }
        @DisplayName("Should return an instance")
        @Test
        void shouldReturnAnInstance() {
            Method method = method(PersonRepository.class, "getInstance");
            Supplier<Stream<?>> stream = () -> Stream.of(new Person("Ada"));
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .result(stream)
                    .singleResult(singleResult).build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Person.class);
            Person person = (Person) execute;
            assertThat(person).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return empty exception")
        @Test
        void shouldReturnEmptyException() {

            Method method = method(PersonRepository.class, "getInstance");
            Supplier<Stream<?>> stream = Stream::empty;
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .result(stream)
                    .singleResult(singleResult).build();

            assertThatExceptionOfType(EmptyResultException.class).isThrownBy(dynamicReturn::execute);
        }
        @DisplayName("Should return list")
        @Test
        void shouldReturnList() {

            Method method = method(PersonRepository.class, "getList");
            Supplier<Stream<?>> stream = () -> Stream.of(new Person("Ada"));
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .result(stream)
                    .singleResult(singleResult).build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(List.class);
            List<Person> persons = (List) execute;
            assertThat(persons.isEmpty()).isFalse();
            assertThat(persons.getFirst()).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return iterable")
        @Test
        void shouldReturnIterable() {

            Method method = method(PersonRepository.class, "getIterable");
            Supplier<Stream<?>> stream = () -> Stream.of(new Person("Ada"));
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .result(stream)
                    .singleResult(singleResult).build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Iterable.class);
            Iterable<Person> persons = (List) execute;
            assertThat(persons.iterator().next()).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return collection")
        @Test
        void shouldReturnCollection() {

            Method method = method(PersonRepository.class, "getCollection");
            Supplier<Stream<?>> stream = () -> Stream.of(new Person("Ada"));
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .result(stream)
                    .singleResult(singleResult).build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Collection.class);
            Collection<Person> persons = (Collection) execute;
            assertThat(persons.isEmpty()).isFalse();
            assertThat(persons.iterator().next()).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return set")
        @Test
        void shouldReturnSet() {

            Method method = method(PersonRepository.class, "getSet");
            Supplier<Stream<?>> stream = () -> Stream.of(new Person("Ada"));
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .result(stream)
                    .singleResult(singleResult).build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Set.class);
            Set<Person> persons = (Set) execute;
            assertThat(persons.isEmpty()).isFalse();
            assertThat(persons.iterator().next()).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return queue")
        @Test
        void shouldReturnQueue() {

            Method method = method(PersonRepository.class, "getQueue");
            Supplier<Stream<?>> stream = () -> Stream.of(new Person("Ada"));
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .result(stream)
                    .singleResult(singleResult).build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Queue.class);
            Queue<Person> persons = (Queue) execute;
            assertThat(persons.isEmpty()).isFalse();
            assertThat(persons.iterator().next()).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return stream")
        @Test
        void shouldReturnStream() {

            Method method = method(PersonRepository.class, "getStream");
            Supplier<Stream<?>> stream = () -> Stream.of(new Person("Ada"));
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .result(stream)
                    .singleResult(singleResult).build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Stream.class);
            Stream<Person> persons = (Stream) execute;
            assertThat(persons.iterator().next()).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return sorted set")
        @Test
        void shouldReturnSortedSet() {

            Method method = method(PersonRepository.class, "getSortedSet");
            Supplier<Stream<?>> stream = () -> Stream.of(new Person("Ada"));
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .result(stream)
                    .singleResult(singleResult).build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(SortedSet.class);
            SortedSet<Person> persons = (SortedSet) execute;
            assertThat(persons.isEmpty()).isFalse();
            assertThat(persons.getFirst()).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return navigable set")
        @Test
        void shouldReturnNavigableSet() {

            Method method = method(PersonRepository.class, "getNavigableSet");
            Supplier<Stream<?>> stream = () -> Stream.of(new Person("Ada"));
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .result(stream)
                    .singleResult(singleResult).build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(NavigableSet.class);
            NavigableSet<Person> persons = (NavigableSet) execute;
            assertThat(persons.isEmpty()).isFalse();
            assertThat(persons.getFirst()).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return deque")
        @Test
        void shouldReturnDeque() {

            Method method = method(PersonRepository.class, "getDeque");
            Supplier<Stream<?>> stream = () -> Stream.of(new Person("Ada"));
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .result(stream)
                    .singleResult(singleResult).build();
            Object execute = dynamicReturn.execute();
            assertThat(execute).isInstanceOf(Deque.class);
            Deque<Person> persons = (Deque) execute;
            assertThat(persons.isEmpty()).isFalse();
            assertThat(persons.getFirst()).isEqualTo(new Person("Ada"));
        }
        @DisplayName("Should return error when execute page")
        @Test
        void shouldReturnErrorWhenExecutePage() {
            Method method = method(PersonRepository.class, "getPage");
            Supplier<Stream<?>> stream = () -> Stream.of(new Person("Ada"));
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Person.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .result(stream)
                    .singleResult(singleResult).build();
            assertThatExceptionOfType(DynamicQueryException.class).isThrownBy(dynamicReturn::execute);
        }
        @DisplayName("Should return error navigable set entity is not comparable")
        @Test
        void shouldReturnErrorNavigableSetEntityIsNotComparable() {

            Method method = method(AnimalRepository.class, "getSortedSet");
            Supplier<Stream<?>> stream = () -> Stream.of(new Animal("Ada"));
            Supplier<Optional<?>> singleResult = DynamicReturn.toSingleResult(method.getName()).apply(stream);
            DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                    .classSource(Animal.class)
                    .methodName(method.getName())
                    .returnType(method.getReturnType())
                    .result(stream)
                    .singleResult(singleResult).build();

            assertThatExceptionOfType(DynamicQueryException.class).isThrownBy(dynamicReturn::execute);
        }
        @DisplayName("Should return null when param is empty on find special")
        @Test
        void shouldReturnNullWhenParamIsEmptyOnFindSpecial() {
            assertThat(DynamicReturn.findSpecialParameters(null, SORT_MAPPER).isEmpty()).isTrue();
            assertThat(DynamicReturn.findSpecialParameters(new Object[0], SORT_MAPPER).isEmpty()).isTrue();
        }
        @DisplayName("Should find special")
        @Test
        void shouldFindSpecial() {
            PageRequest pageRequest = PageRequest.ofPage(1L).size(2);
            SpecialParameters specialParameters = DynamicReturn.findSpecialParameters(new Object[]{"value", 23, pageRequest}, SORT_MAPPER);
            assertThat(specialParameters.pageRequest().orElseThrow()).isEqualTo(pageRequest);
        }
        @DisplayName("Should return null when there is not special")
        @Test
        void shouldReturnNullWhenThereIsNotSpecial() {
            SpecialParameters pagination = DynamicReturn.findSpecialParameters(new Object[]{"value", 23, BigDecimal.TEN}, SORT_MAPPER);
            assertThat(pagination.isEmpty()).isTrue();
        }
        @DisplayName("Should return null when param is empty on find pagination")
        @Test
        void shouldReturnNullWhenParamIsEmptyOnFindPagination() {
            assertThat(DynamicReturn.findPageRequest(null)).isNull();
            assertThat(DynamicReturn.findPageRequest(new Object[0])).isNull();
        }
        @DisplayName("Should find pagination")
        @Test
        void shouldFindPagination() {
            PageRequest pageRequest = PageRequest.ofPage(1L).size(2);
            PageRequest pageRequest2 = DynamicReturn.findPageRequest(new Object[]{"value", 23, pageRequest});
            assertThat(pageRequest2).isEqualTo(pageRequest);
        }
        @DisplayName("Should return null when there is not pagination")
        @Test
        void shouldReturnNullWhenThereIsNotPagination() {
            PageRequest pageRequest = DynamicReturn.findPageRequest(new Object[]{"value", 23, BigDecimal.TEN});
            assertThat(pageRequest).isNull();
        }
    }
}
