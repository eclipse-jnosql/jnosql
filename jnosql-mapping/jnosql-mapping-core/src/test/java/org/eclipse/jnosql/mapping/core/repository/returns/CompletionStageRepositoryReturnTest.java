/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *
 *   The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *
 *   Contributors:
 *
 *   Mohan Lal
 *
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

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class CompletionStageRepositoryReturnTest {

    private final RepositoryReturn repositoryReturn =
            new CompletionStageRepositoryReturn();

    @Mock
    private Page<Person> page;

    @Test
    void shouldReturnIsCompatible() {
        Assertions.assertTrue(
                repositoryReturn.isCompatible(Person.class, CompletionStage.class));
        assertFalse(repositoryReturn.isCompatible(Object.class, Person.class));
        assertFalse(repositoryReturn.isCompatible(Person.class, Object.class));
    }

    @Test
    void shouldReturnCompletionStage() throws NoSuchMethodException {
        Method method = PersonRepository.class.getMethod("getPerson");
        Person ada = new Person("Ada");

        DynamicReturn<Person> dynamic = DynamicReturn.builder()
                .classSource(Person.class)
                .methodSource(method)
                .singleResult(() -> Optional.of(ada))
                .result(() -> Stream.of(ada))
                .singleResultPagination(p -> Optional.of(ada))
                .streamPagination(p -> Stream.of(ada))
                .pagination(PageRequest.ofPage(2).size(2))
                .page(p -> page)
                .build();

        Object result = repositoryReturn.convert(dynamic);

        Assertions.assertInstanceOf(CompletionStage.class, result);

        CompletionStage<?> stage = (CompletionStage<?>) result;

        Assertions.assertEquals(
                ada,
                stage.toCompletableFuture().join());
    }

    @Test
    void shouldReturnCompletionStagePage() throws NoSuchMethodException {
        Method method = PersonRepository.class.getMethod("getPerson");
        Person ada = new Person("Ada");

        DynamicReturn<Person> dynamic = DynamicReturn.builder()
                .classSource(Person.class)
                .methodSource(method)
                .singleResult(() -> Optional.of(ada))
                .result(Stream::empty)
                .singleResultPagination(p -> Optional.of(ada))
                .streamPagination(p -> Stream.of(ada))
                .pagination(PageRequest.ofPage(2).size(2))
                .page(p -> page)
                .build();

        Object result = repositoryReturn.convertPageRequest(dynamic);

        Assertions.assertInstanceOf(CompletionStage.class, result);

        CompletionStage<?> stage = (CompletionStage<?>) result;

        Assertions.assertEquals(
                ada,
                stage.toCompletableFuture().join());
    }

    private interface PersonRepository {

        CompletionStage<Person> getPerson();
    }

    private static class Person {

        private final String name;

        Person(String name) {
            this.name = name;
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
            return java.util.Objects.equals(name, person.name);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(name);
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }
}
