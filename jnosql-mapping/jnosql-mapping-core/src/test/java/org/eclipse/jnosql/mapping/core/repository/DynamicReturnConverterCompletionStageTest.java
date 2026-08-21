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
package org.eclipse.jnosql.mapping.core.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

class DynamicReturnConverterCompletionStageTest {

    interface Sample {

        CompletionStage<Person> findByName(String name);

        CompletionStage<List<Person>> findAllByName(String name);
    }

    @Test
    @DisplayName("Should wrap single entity in CompletionStage")
    void shouldWrapSingleEntityInCompletionStage()
            throws NoSuchMethodException {

        // Arrange
        Method method = Sample.class.getMethod(
                "findByName", String.class);
        Person person = new Person("Ada");

        DynamicReturn<Person> dynamic = DynamicReturn.builder()
                .classSource(Person.class)
                .methodSource(method)
                .singleResult(() -> Optional.of(person))
                .result(() -> Stream.of(person))
                .build();

        // Act
        Object result =
                DynamicReturnConverter.INSTANCE.convert(dynamic);

        // Assert
        assertSoftly(softly -> {
            softly.assertThat(result)
                    .as("converted result")
                    .isInstanceOf(CompletionStage.class);

            CompletionStage<?> stage = (CompletionStage<?>) result;

            softly.assertThat(stage.toCompletableFuture().join())
                    .as("completion stage value")
                    .isEqualTo(person);
        });
    }

    @Test
    @DisplayName("Should wrap list in CompletionStage")
    void shouldWrapListInCompletionStage()
            throws NoSuchMethodException {

        // Arrange
        Method method = Sample.class.getMethod(
                "findAllByName", String.class);
        Person ada = new Person("Ada");
        Person alan = new Person("Alan");

        DynamicReturn<Person> dynamic = DynamicReturn.builder()
                .classSource(Person.class)
                .methodSource(method)
                .singleResult(() -> Optional.of(ada))
                .result(() -> Stream.of(ada, alan))
                .build();

        // Act
        Object result =
                DynamicReturnConverter.INSTANCE.convert(dynamic);

        // Assert
        assertSoftly(softly -> {
            softly.assertThat(result)
                    .as("converted result")
                    .isInstanceOf(CompletionStage.class);

            CompletionStage<?> stage = (CompletionStage<?>) result;

            @SuppressWarnings("unchecked")
            List<Person> listResult =
                    (List<Person>) stage.toCompletableFuture().join();

            softly.assertThat(listResult)
                    .as("completion stage list value")
                    .containsExactly(ada, alan);
        });
    }

    record Person(String name) {
    }
}
