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
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.RepositoryReturn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class InstanceRepositoryReturnTest {

    private final RepositoryReturn repositoryReturn = new InstanceRepositoryReturn();

    @Mock
    private Page<Person> page;

    @ParameterizedTest
    @ValueSource(classes = {Person.class, Object.class, String.class, Integer.class, Date.class})
    void shouldReturnIsCompatible(Class<?> returnType) {
        assertThat(repositoryReturn.isCompatible(Person.class, returnType)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(classes = {List.class, Set.class, Map.class, Iterable.class, Queue.class})
    void shouldReturnIsNotCompatible(Class<?> returnType) {
        assertThat(repositoryReturn.isCompatible(Person.class, returnType)).isFalse();
    }

    @Test
    void shouldReturnInstancePage() {

        Person ada = new Person("Ada");
        DynamicReturn<Person> dynamic = DynamicReturn.builder()
                .classSource(Person.class)
                .singleResult(Optional::empty)
                .result(Collections::emptyList)
                .singleResultPagination(p -> Optional.of(ada))
                .streamPagination(p -> Stream.empty())
                .methodSource(Person.class.getDeclaredMethods()[0])
                .pagination(PageRequest.ofPage(2).size(2))
                .page(p -> page)
                .build();
        Person person = (Person) repositoryReturn.convertPageRequest(dynamic);
        Assertions.assertNotNull(person);
        assertEquals(ada, person);
    }

    @Test
    void shouldReturnNullAsInstancePage() {
        DynamicReturn<Person> dynamic = DynamicReturn.builder()
                .classSource(Person.class)
                .singleResult(Optional::empty)
                .result(Collections::emptyList)
                .singleResultPagination(p -> Optional.empty())
                .streamPagination(p -> Stream.empty())
                .methodSource(Person.class.getDeclaredMethods()[0])
                .pagination(PageRequest.ofPage(2).size(2))
                .page(p -> page)
                .build();
        Person person = (Person) repositoryReturn.convertPageRequest(dynamic);
        Assertions.assertNull(person);
    }

    @Test
    void shouldReturnInstance() {

        Person ada = new Person("Ada");
        DynamicReturn<Person> dynamic = DynamicReturn.builder()
                .singleResult(() -> Optional.of(ada))
                .classSource(Person.class)
                .result(Collections::emptyList)
                .methodSource(Person.class.getDeclaredMethods()[0])
                .build();
        Person person = (Person) repositoryReturn.convert(dynamic);
        Assertions.assertNotNull(person);
        Assertions.assertEquals(ada, person);
    }

    @Test
    void shouldReturnNullAsInstance() {
        DynamicReturn<Person> dynamic = DynamicReturn.builder()
                .singleResult(Optional::empty)
                .classSource(Person.class)
                .result(Collections::emptyList)
                .methodSource(Person.class.getDeclaredMethods()[0])
                .build();
        Person person = (Person) repositoryReturn.convert(dynamic);
        Assertions.assertNull(person);
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