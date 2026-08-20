/*
 *  Copyright (c) 2022,2025 Contributors to the Eclipse Foundation
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
 *   Mohan Lal
 */
package org.eclipse.jnosql.mapping.core.repository.returns;

import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.RepositoryReturn;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompletionStageRepositoryReturnTest {

    private final RepositoryReturn repositoryReturn =
            new CompletionStageRepositoryReturn();

    @Test
    void shouldReturnIsCompatible() {
        assertTrue(repositoryReturn.isCompatible(
                Person.class, CompletionStage.class));
    }

    @Test
    void shouldReturnCompletionStage() {
        Person ada = new Person("Ada");

        DynamicReturn<Person> dynamic = DynamicReturn.builder()
                .singleResult(() -> Optional.of(ada))
                .classSource(Person.class)
                .result(() -> Stream.of(ada))
                .methodSource(PersonRepository.class.getDeclaredMethods()[0])
                .build();

        CompletionStage<Person> result =
                (CompletionStage<Person>) repositoryReturn.convert(dynamic);

        assertEquals(ada, result.toCompletableFuture().join());
    }

    private interface PersonRepository {

        CompletionStage<Person> getPerson();
    }

    private record Person(String name) {
    }
}