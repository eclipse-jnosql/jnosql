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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@ExtendWith(MockitoExtension.class)
class CompletionStageRepositoryReturnTest {

    private final RepositoryReturn repositoryReturn =
            new CompletionStageRepositoryReturn();

    @Mock
    private Page<Person> page;

    @Test
    @DisplayName("Should return true when return type is CompletionStage")
    void shouldReturnIsCompatible() {
        assertSoftly(softly -> {
            softly.assertThat(repositoryReturn.isCompatible(
                            Person.class, CompletionStage.class))
                    .as("CompletionStage return type is compatible")
                    .isTrue();

            softly.assertThat(repositoryReturn.isCompatible(
                            Object.class, Person.class))
                    .as("different entity and return types are incompatible")
                    .isFalse();

            softly.assertThat(repositoryReturn.isCompatible(
                            Person.class, Object.class))
                    .as("different return type is incompatible")
                    .isFalse();
        });
    }

    @Test
    @DisplayName("Should return entity wrapped in CompletionStage")
    void shouldReturnCompletionStage() throws NoSuchMethodException {
        Person ada = new Person("Ada");

        DynamicReturn<Person> dynamic = DynamicReturn.builder()
                .singleResult(() -> Optional.of(ada))
                .classSource(Person.class)
                .result(() -> Stream.of(ada))
                .methodSource(PersonRepository.class.getMethod("getPerson"))
                .build();

        CompletionStage<Person> result =
                (CompletionStage<Person>) repositoryReturn.convert(dynamic);

        assertSoftly(softly -> {
            softly.assertThat(result)
                    .as("repository result")
                    .isNotNull();

            softly.assertThat(result.toCompletableFuture().join())
                    .as("completion stage value")
                    .isEqualTo(ada);
        });
    }

    @Test
    @DisplayName("Should return paginated entity wrapped in CompletionStage")
    void shouldReturnCompletionStagePage() throws NoSuchMethodException {
        Person ada = new Person("Ada");

        DynamicReturn<Person> dynamic = DynamicReturn.builder()
                .classSource(Person.class)
                .singleResult(Optional::empty)
                .result(Collections::emptyList)
                .singleResultPagination(p -> Optional.of(ada))
                .streamPagination(p -> Stream.of(ada))
                .methodSource(PersonRepository.class.getMethod("getPerson"))
                .pagination(PageRequest.ofPage(2).size(2))
                .page(p -> page)
                .build();

        CompletionStage<Person> result =
                (CompletionStage<Person>)
                        repositoryReturn.convertPageRequest(dynamic);

        assertSoftly(softly -> {
            softly.assertThat(result)
                    .as("repository page result")
                    .isNotNull();

            softly.assertThat(result.toCompletableFuture().join())
                    .as("completion stage page value")
                    .isEqualTo(ada);
        });
    }

    private interface PersonRepository {

        CompletionStage<Person> getPerson();
    }

    private record Person(String name) {
    }
}
