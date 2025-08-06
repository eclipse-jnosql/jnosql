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
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.RepositoryReturn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class QueueRepositoryReturnTest {

    private final RepositoryReturn repositoryReturn = new QueueRepositoryReturn();

    @Mock
    private Page<Person> page;

    @Test
    void shouldReturnIsCompatible() {
        Assertions.assertTrue(repositoryReturn.isCompatible(Person.class, Queue.class));
        Assertions.assertTrue(repositoryReturn.isCompatible(Person.class, Deque.class));
        assertFalse(repositoryReturn.isCompatible(Object.class, Person.class));
        assertFalse(repositoryReturn.isCompatible(Person.class, Object.class));
    }

    @Test
    void shouldReturnLinkedListPage() {
        Person ada = new Person("Ada");
        DynamicReturn<Person> dynamic = DynamicReturn.builder()
                .classSource(Person.class)
                .singleResult(Optional::empty)
                .result(Collections::emptyList)
                .singleResultPagination(p -> Optional.empty())
                .streamPagination(p -> Stream.of(ada))
                .methodSource(Person.class.getDeclaredMethods()[0])
                .pagination(PageRequest.ofPage(2).size(2))
                .page(p -> page)
                .build();
        LinkedList<Person> person = (LinkedList<Person>) repositoryReturn.convertPageRequest(dynamic);
        Assertions.assertNotNull(person);
        assertFalse(person.isEmpty());
        assertEquals(ada, person.stream().findFirst().get());
    }

    @Test
    void shouldReturnLinkedList() {
        Person ada = new Person("Ada");
        DynamicReturn<Person> dynamic = DynamicReturn.builder()
                .singleResult(Optional::empty)
                .classSource(Person.class)
                .result(() -> Stream.of(ada))
                .methodSource(Person.class.getDeclaredMethods()[0])
                .build();
        LinkedList<Person> person = (LinkedList<Person>) repositoryReturn.convert(dynamic);
        Assertions.assertNotNull(person);
        Assertions.assertFalse(person.isEmpty());
        Assertions.assertEquals(ada, person.getFirst());
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