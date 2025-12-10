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

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import org.eclipse.jnosql.mapping.DynamicQueryException;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.RepositoryReturn;
import org.junit.jupiter.api.Assertions;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class SortedSetRepositoryReturnTest {

    private final RepositoryReturn repositoryReturn = new SortedSetRepositoryReturn();

    @Mock
    private Page<Person> page;

    @Test
    void shouldReturnIsCompatible() {
        Assertions.assertTrue(repositoryReturn.isCompatible(Person.class, NavigableSet.class));
        Assertions.assertTrue(repositoryReturn.isCompatible(Person.class, SortedSet.class));
        assertFalse(repositoryReturn.isCompatible(Object.class, Person.class));
        assertFalse(repositoryReturn.isCompatible(Person.class, Object.class));
    }


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
                .page(p -> page)
                .build();
        TreeSet<Person> person = (TreeSet<Person>) repositoryReturn.convertPageRequest(dynamic);
        Assertions.assertNotNull(person);
        assertFalse(person.isEmpty());
        assertEquals(ada, person.stream().findFirst().get());
    }

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
        Assertions.assertNotNull(person);
        Assertions.assertFalse(person.isEmpty());
        Assertions.assertEquals(ada, person.stream().findFirst().get());
    }

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
                .page(p -> page)
                .build();
        Assertions.assertThrows(DynamicQueryException.class, () -> repositoryReturn.convertPageRequest(dynamic));
    }

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
                .page(p -> page)
                .build();
        Assertions.assertThrows(DynamicQueryException.class, () -> repositoryReturn.convert(dynamic));
    }

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

}