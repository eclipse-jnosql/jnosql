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
package org.eclipse.jnosql.mapping.core.repository.returns;

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.RepositoryReturn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ArrayRepositoryReturnTest {

    private final RepositoryReturn repositoryReturn = new ArrayRepositoryReturn();

    @Mock
    private Page<Person> page;

    @Test
    void shouldReturnIsCompatible() {
        Assertions.assertTrue(repositoryReturn.isCompatible(Person.class, Person[].class));
        Assertions.assertFalse(repositoryReturn.isCompatible(Person.class, Iterable.class));
        Assertions.assertFalse(repositoryReturn.isCompatible(Person.class, Collection.class));
        assertFalse(repositoryReturn.isCompatible(Object.class, Person.class));
        assertFalse(repositoryReturn.isCompatible(Person.class, Object.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnArray() {
        Method method = Person.class.getDeclaredMethods()[0];
        Person ada = new Person("Ada");
        DynamicReturn<Person> dynamic = DynamicReturn.builder()
                .singleResult(Optional::empty)
                .classSource(Person.class)
                .result(() -> Stream.of(ada))
                .methodName(method.getName())
                .returnType(Person[].class)
                .build();
        Person[] person = (Person[]) repositoryReturn.convert(dynamic);
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(person).isNotNull();
            s.assertThat(person).hasSize(1);
            s.assertThat(person[0]).isEqualTo(ada);
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnListPage() {
        Method method = Person.class.getDeclaredMethods()[0];
        Person ada = new Person("Ada");
        DynamicReturn<Person> dynamic = DynamicReturn.builder()
                .classSource(Person.class)
                .singleResult(Optional::empty)
                .result(Collections::emptyList)
                .singleResultPagination(p -> Optional.empty())
                .streamPagination(p -> Stream.of(ada))
                .methodName(method.getName())
                .returnType(Person[].class)
                .pagination(PageRequest.ofPage(2).size(2))
                .page(p -> page)
                .build();
        Person[] person = (Person[]) repositoryReturn.convertPageRequest(dynamic);
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(person).isNotNull();
            s.assertThat(person).hasSize(1);
            s.assertThat(person[0]).isEqualTo(ada);
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnArrayPrimitive() {
        Method method = Person.class.getDeclaredMethods()[0];
        DynamicReturn<long[]> dynamic = DynamicReturn.builder()
                .singleResult(Optional::empty)
                .classSource(Person.class)
                .result(() -> Stream.of(1L, 2L, 3L))
                .methodName(method.getName())
                .returnType(long[].class)
                .build();

        long[] values = (long[]) repositoryReturn.convert(dynamic);
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(values).isNotNull();
            soft.assertThat(values).hasSize(3);
            soft.assertThat(values).containsExactly(1L, 2L, 3L);
        });
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