/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.reflection.repository;

import org.eclipse.jnosql.mapping.reflection.entities.Person;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface PersonNestedReturnCustomRepository {

    CompletionStage<Optional<Person>> findByName(String name);

    CompletionStage<Person> findByEmail(String email);

    CompletableFuture<List<Person>> findByAge(int age);

    CompletionStage<List<? extends Person>> findByCity(String city);

    <T extends Person> List<T> findByType(String type);

    List<String>[] findByTags(String tag);

    @SuppressWarnings("rawtypes")
    CompletionStage findByRaw(String raw);
}
