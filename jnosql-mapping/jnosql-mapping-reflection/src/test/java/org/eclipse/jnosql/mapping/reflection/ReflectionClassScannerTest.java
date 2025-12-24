/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.reflection;

import jakarta.data.repository.CrudRepository;
import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.eclipse.jnosql.mapping.reflection.entities.AnimalRepository;
import org.eclipse.jnosql.mapping.reflection.entities.ComputerView;
import org.eclipse.jnosql.mapping.reflection.entities.Contact;
import org.eclipse.jnosql.mapping.reflection.entities.Garage;
import org.eclipse.jnosql.mapping.reflection.entities.Job;
import org.eclipse.jnosql.mapping.reflection.entities.Library;
import org.eclipse.jnosql.mapping.reflection.entities.MovieRepository;
import org.eclipse.jnosql.mapping.reflection.entities.NoSQLVendor;
import org.eclipse.jnosql.mapping.reflection.entities.PCView;
import org.eclipse.jnosql.mapping.reflection.entities.Person;
import org.eclipse.jnosql.mapping.reflection.entities.PersonRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReflectionClassScannerTest {

    private ReflectionClassScanner classScanner = new ReflectionClassScanner();

    @Test
    void shouldReturnEntities() {
        Set<Class<?>> entities = classScanner.entities();
        Assertions.assertNotNull(entities);
        assertThat(entities).hasSize(33)
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

        assertThat(reepositores).hasSize(5)
                .contains(RepositoryFilterTest.Persons.class,
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
        assertThat(repositories).hasSize(4)
                .contains(RepositoryFilterTest.Persons.class, PersonRepository.class, MovieRepository.class);
    }

    @Test
    void shouldReturnCustomRepositories() {
        Set<Class<?>> repositories = classScanner.customRepositories();
        assertThat(repositories).hasSize(3)
                .contains(Library.class, Garage.class);
    }

    @Test
    void shouldReturnRepositoriesStandard() {
        Set<Class<?>> repositories = classScanner.repositoriesStandard();
        assertThat(repositories).hasSize(4);
    }

    @Test
    void shouldReturnCustomRepository() {
        Set<Class<?>> repositories = classScanner.customRepositories();
        assertThat(repositories).hasSize(3)
                .contains(Library.class, Garage.class);
    }

    @Test
    void shouldFindRepository() {
        Set<Class<?>> repositories = classScanner.repositories(NoSQLRepository.class);
        assertThat(repositories).hasSize(1);
    }

    @Test
    void shouldFindProjections() {
        Set<Class<?>> projections = classScanner.projections();
        assertThat(projections).hasSize(3)
                .contains(ComputerView.class, PCView.class);
    }
}