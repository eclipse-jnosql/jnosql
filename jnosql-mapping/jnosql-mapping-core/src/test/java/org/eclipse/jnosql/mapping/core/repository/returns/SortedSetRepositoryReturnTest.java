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
package org.eclipse.jnosql.mapping.core.repository.returns;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import org.eclipse.jnosql.mapping.DynamicQueryException;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.RepositoryReturn;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;


@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class SortedSetRepositoryReturnTest {

    private final RepositoryReturn repositoryReturn = new SortedSetRepositoryReturn();

    @Mock
    private Page<Person> page;







    private static class Animal {

    }

    private static class Person implements Comparable<Person> {

        private String name;

        public Person(String name) {
            this.name = name;
        }

        public Person() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Person person = (Person) o;
            return Objects.equals(name, person.name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    '}';
        }

        @Override
        public int compareTo(Person o) {
            return name.compareTo(o.name);
        }
    }


    @Nested
    @DisplayName("When the sorted set repository return operates")
    class WhenTheSortedSetRepositoryReturnOperates {

        @DisplayName("Should return is compatible")
        @Test
        void shouldReturnIsCompatible() {
            assertThat(repositoryReturn.isCompatible(Person.class, NavigableSet.class)).isTrue();
            assertThat(repositoryReturn.isCompatible(Person.class, SortedSet.class)).isTrue();
            assertThat(repositoryReturn.isCompatible(Object.class, Person.class)).isFalse();
            assertThat(repositoryReturn.isCompatible(Person.class, Object.class)).isFalse();
        }
        @DisplayName("Should return tree set page")
        @Test
        void shouldReturnTreeSetPage() {
            Person ada = new Person("Ada");
            Method method = Person.class.getDeclaredMethods()[0];
            DynamicReturn<Person> dynamic = DynamicReturn.builder()
                    .classSource(Person.class)
                    .singleResult(Optional::empty)
                    .result(Collections::emptyList)
                    .singleResultPagination(p -> Optional.empty())
                    .streamPagination(p -> Stream.of(ada))
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .pagination(PageRequest.ofPage(2).size(2))
                    .page((p, l) -> page)
                    .totalSupplier(() -> 1L)
                    .build();
            TreeSet<Person> person = (TreeSet<Person>) repositoryReturn.convertPageRequest(dynamic);
            assertThat(person).isNotNull();
            assertThat(person.isEmpty()).isFalse();
            assertThat(person.stream().findFirst().get()).isEqualTo(ada);
        }
        @DisplayName("Should return tree set")
        @Test
        void shouldReturnTreeSet() {
            Person ada = new Person("Ada");
            Method method = Person.class.getDeclaredMethods()[0];
            DynamicReturn<Person> dynamic = DynamicReturn.builder()
                    .singleResult(Optional::empty)
                    .classSource(Person.class)
                    .result(() -> Stream.of(ada))
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .build();
            TreeSet<Person> person = (TreeSet<Person>) repositoryReturn.convert(dynamic);
            assertThat(person).isNotNull();
            assertThat(person.isEmpty()).isFalse();
            assertThat(person.stream().findFirst().get()).isEqualTo(ada);
        }
        @DisplayName("Should return error on tree set page")
        @Test
        void shouldReturnErrorOnTreeSetPage() {
            Method method = Person.class.getDeclaredMethods()[0];
            Animal animal = new Animal();
            DynamicReturn<Animal> dynamic = DynamicReturn.builder()
                    .classSource(Animal.class)
                    .singleResult(Optional::empty)
                    .result(Collections::emptyList)
                    .singleResultPagination(p -> Optional.empty())
                    .streamPagination(p -> Stream.of(animal))
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .pagination(PageRequest.ofPage(2).size(2))
                    .page((p, l) -> page)
                    .totalSupplier(() -> 1L)
                    .build();
            assertThatExceptionOfType(DynamicQueryException.class).isThrownBy(() -> repositoryReturn.convertPageRequest(dynamic));
        }
        @DisplayName("Should return error on tree set")
        @Test
        void shouldReturnErrorOnTreeSet() {
            Method method = Person.class.getDeclaredMethods()[0];
            Animal animal = new Animal();
            DynamicReturn<Animal> dynamic = DynamicReturn.builder()
                    .classSource(Animal.class)
                    .singleResult(Optional::empty)
                    .result(Collections::emptyList)
                    .singleResultPagination(p -> Optional.empty())
                    .streamPagination(p -> Stream.of(animal))
                    .returnType(method.getReturnType())
                    .methodName(method.getName())
                    .pagination(PageRequest.ofPage(2).size(2))
                    .page((p, l) -> page)
                    .totalSupplier(() -> 1L)
                    .build();
            assertThatExceptionOfType(DynamicQueryException.class).isThrownBy(() -> repositoryReturn.convert(dynamic));
        }
    }
}
