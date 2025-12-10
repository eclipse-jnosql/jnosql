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

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DefaultRepositoryReturnTest {

    private final RepositoryReturn repositoryReturn = new DefaultRepositoryReturn();

    @Mock
    private Page<Person> page;

    @Test
    void shouldReturnIsCompatible() {
        Assertions.assertTrue(repositoryReturn.isCompatible(Person.class, Optional.class));
        Assertions.assertTrue(repositoryReturn.isCompatible(Object.class, Person.class));
        Assertions.assertTrue(repositoryReturn.isCompatible(Person.class, Object.class));
    }



    @Test
    void shouldReturnDefaultPage() {
        Method method = Person.class.getDeclaredMethods()[0];
        Person ada = new Person("Ada");
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
        Object person = repositoryReturn.convertPageRequest(dynamic);
        Assertions.assertNotNull(person);
    }

    @Test
    void shouldReturnDefault() {
        Method method = Person.class.getDeclaredMethods()[0];
        Person ada = new Person("Ada");
        DynamicReturn<Person> dynamic  = DynamicReturn.builder()
                .singleResult(Optional::empty)
                .classSource(Person.class)
                .result(() -> Stream.of(ada))
                .returnType(method.getReturnType())
                .methodName(method.getName())
                .build();
        Object person = repositoryReturn.convert(dynamic);
        Assertions.assertNotNull(person);
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