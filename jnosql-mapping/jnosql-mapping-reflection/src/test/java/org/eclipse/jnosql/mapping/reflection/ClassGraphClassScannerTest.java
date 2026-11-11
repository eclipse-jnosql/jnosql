/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.reflection;

import jakarta.data.repository.CrudRepository;
import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.eclipse.jnosql.mapping.reflection.RepositoryFilterTest.Persons;
import org.eclipse.jnosql.mapping.reflection.entities.AnimalRepository;
import org.eclipse.jnosql.mapping.reflection.entities.Contact;
import org.eclipse.jnosql.mapping.reflection.entities.Job;
import org.eclipse.jnosql.mapping.reflection.entities.MovieRepository;
import org.eclipse.jnosql.mapping.reflection.entities.NoSQLVendor;
import org.eclipse.jnosql.mapping.reflection.entities.Person;
import org.eclipse.jnosql.mapping.reflection.entities.PersonRepository;
import org.eclipse.jnosql.mapping.reflection.repository.InvalidEntityCustomRepository;
import org.eclipse.jnosql.mapping.reflection.repository.MethodEntityCustomRepository;
import org.eclipse.jnosql.mapping.reflection.repository.OtherProviderCustomRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ClassGraphClassScannerTest {

    private ClassGraphClassScanner classScanner = ClassGraphClassScanner.INSTANCE;

    @Test
    void shouldReturnEntities() {
        Set<Class<?>> entities = classScanner.entities();
        Assertions.assertNotNull(entities);
        assertThat(entities).hasSize(34)
                .contains(Person.class);
    }

    @Test
    void shouldReturnEmbeddables() {
        Set<Class<?>> embeddables = classScanner.embeddables();
        Assertions.assertNotNull(embeddables);
        assertThat(embeddables).hasSize(5)
                .contains(Job.class, Contact.class);
    }


    @Test
    void shouldReturnRepositories() {
        Set<Class<?>> reepositores = classScanner.repositories();
        Assertions.assertNotNull(reepositores);

        assertThat(reepositores).hasSize(4)
                .contains(Persons.class,
                        AnimalRepository.class,
                        PersonRepository.class,
                        MovieRepository.class);
    }

    @Test
    void shouldFilterRepositories() {
        Set<Class<?>> repositories = classScanner.repositories(NoSQLVendor.class);
        Assertions.assertNotNull(repositories);

        assertThat(repositories).hasSize(1)
                .contains(AnimalRepository.class);
    }

    @Test
    void shouldFieldByCrudRepository() {
        Set<Class<?>> repositories = classScanner.repositories(CrudRepository.class);
        Assertions.assertNotNull(repositories);

        assertThat(repositories).hasSize(1)
                .contains(MovieRepository.class);
    }

    @Test
    void shouldFieldByNoSQL() {
        Set<Class<?>> repositories = classScanner.repositories(NoSQLRepository.class);
        Assertions.assertNotNull(repositories);

        assertThat(repositories).hasSize(1).contains(PersonRepository.class);
    }

    @Test
    void shouldReturnStandardRepositories() {
        Set<Class<?>> repositories = classScanner.repositoriesStandard();
        assertThat(repositories).hasSize(3)
                .contains(Persons.class, PersonRepository.class, MovieRepository.class);
    }

    @Test
    void shouldReturnCustomRepositories() {
        Set<Class<?>> repositories = classScanner.customRepositories();
        assertThat(repositories).hasSize(1)
                .contains(MethodEntityCustomRepository.class);
    }

    @Test
    void shouldReturnRepositoriesStandard() {
        Set<Class<?>> repositories = classScanner.repositoriesStandard();
        assertThat(repositories).hasSize(3);
    }

    @Test
    void shouldReturnCustomRepository() {
        Set<Class<?>> repositories = classScanner.customRepositories();
        assertThat(repositories).hasSize(1)
                .contains(MethodEntityCustomRepository.class);
    }

    @Test
    void shouldIgnoreInvalidEntityRepositories() {
        Set<Class<?>> repositories = classScanner.customRepositories();
        assertThat(repositories).doesNotContain(InvalidEntityCustomRepository.class);
    }

    @Test
    void shouldIgnoreCustomRepositoriesFromOtherProviders() {
        Set<Class<?>> repositories = classScanner.customRepositories();
        assertThat(repositories).doesNotContain(OtherProviderCustomRepository.class);
    }

    @Test
    void shouldFindRepository() {
        Set<Class<?>> repositories = classScanner.repositories(NoSQLRepository.class);
        assertThat(repositories).hasSize(1);
    }

}