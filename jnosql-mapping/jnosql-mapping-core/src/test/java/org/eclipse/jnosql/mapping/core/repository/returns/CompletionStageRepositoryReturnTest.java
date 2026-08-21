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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CompletionStageRepositoryReturnTest {

    private final RepositoryReturn repositoryReturn =
            new CompletionStageRepositoryReturn();

    @Mock
    private Page<Person> page;

    @Test
    void shouldReturnIsCompatible() {
        assertTrue(repositoryReturn.isCompatible(
                Person.class, CompletionStage.class));
        assertFalse(repositoryReturn.isCompatible(
                Object.class, Person.class));
        assertFalse(repositoryReturn.isCompatible(
                Person.class, Object.class));
    }

    @Test
    void shouldReturnCompletionStage() throws NoSuchMethodException {
        Person ada = new Person("Ada");

        DynamicReturn<Person> dynamic = DynamicReturn.builder()
                .classSource(Person.class)
                .methodSource(PersonRepository.class.getMethod("getPerson"))
                .singleResult(() -> Optional.of(ada))
                .result(() -> Stream.of(ada))
                .build();

        CompletionStage<Person> result =
                (CompletionStage<Person>) repositoryReturn.convert(dynamic);

        assertEquals(ada, result.toCompletableFuture().join());
    }

    @Test
    void shouldReturnCompletionStagePage() throws NoSuchMethodException {
        Person ada = new Person("Ada");

        DynamicReturn<Person> dynamic = DynamicReturn.builder()
                .classSource(Person.class)
                .methodSource(PersonRepository.class.getMethod("getPerson"))
                .singleResult(() -> Optional.of(ada))
                .result(() -> Stream.of(ada))
                .singleResultPagination(p -> Optional.of(ada))
                .streamPagination(p -> Stream.of(ada))
                .pagination(PageRequest.ofPage(2).size(2))
                .page(p -> page)
                .build();

        CompletionStage<?> result =
                (CompletionStage<?>) repositoryReturn.convertPageRequest(dynamic);

        assertEquals(ada, result.toCompletableFuture().join());
    }

    private interface PersonRepository {

        CompletionStage<Person> getPerson();
    }

    private record Person(String name) {
    }
}
