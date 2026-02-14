/*
 *  Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
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

import jakarta.data.repository.BasicRepository;
import jakarta.nosql.Column;
import jakarta.nosql.Id;
import org.eclipse.jnosql.mapping.reflection.entities.NoSQLVendor;
import org.eclipse.jnosql.mapping.reflection.entities.Person;
import org.eclipse.jnosql.mapping.reflection.entities.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryFilterTest {

    private Predicate<Class<?>> valid;

    private Predicate<Class<?>> supported;
    
    private Predicate<Class<?>> validAndSupported;

    @BeforeEach
    void setUp() {
        this.valid = RepositoryFilter.INSTANCE::isValid;
        this.supported = RepositoryFilter.INSTANCE::isSupported;
        this.validAndSupported = RepositoryFilter.INSTANCE;
    }

    @Test
    void shouldReturnTrueWhenHasSupportRepository() {
        assertThat(valid.test(PersonRepository.class)).isTrue();
        assertThat(valid.test(People.class)).isTrue();
        assertThat(valid.test(Persons.class)).isTrue();

    }

    @Test
    void shouldReturnFalseWhenHasNotSupportRepository() {
        assertThat(valid.test(NoSQLVendor.class)).isFalse();
        assertThat(valid.test(Server.class)).isFalse();
        assertThat(valid.test(StringSupplier.class)).isFalse();
        assertThat(valid.test(Repository.class)).isFalse();
    }
    
    @Test
    void shouldReturnSupportedWhenAnnotatedWithRepositoryANY_PROVIDER() {
        assertThat(supported.test(PersonRepository.class)).isTrue();
        assertThat(supported.test(Persons.class)).isTrue();
    }
    
    @Test
    void shouldReturnSupportedWhenAnnotatedWithRepositoryEclipse_JNoSQL() {
        assertThat(supported.test(Server.class)).isTrue();
    }

    @Test
    void shouldReturnUnsupportedWhenNotAnnotatedWithRepository() {
        assertThat(supported.test(NoSQLVendor.class)).isFalse();
        assertThat(supported.test(People.class)).isFalse();
        assertThat(supported.test(StringSupplier.class)).isFalse();
        assertThat(supported.test(Repository.class)).isFalse();
    }

    @Test
    void shouldReturnUnsupportedWhenAnnotatedWithRepositoryInvalidProvider() {
        assertThat(supported.test(OtherServer.class)).isFalse();
    }
    
    @Test
    void shouldReturnValidAndSupported() {
        assertThat(validAndSupported.test(PersonRepository.class)).isTrue();
        assertThat(validAndSupported.test(Persons.class)).isTrue();
    }
    
    @Test
    void shouldReturnReturnNotValidAndSupported() {
        assertThat(validAndSupported.test(People.class)).isFalse();
        assertThat(validAndSupported.test(Server.class)).isFalse();
        assertThat(validAndSupported.test(StringSupplier.class)).isFalse();
    }
    
    private interface People extends BasicRepository<Person, Long> {

    }

    @jakarta.data.repository.Repository
    public interface Persons extends BasicRepository<Person, Long> {
    }
	
    @jakarta.data.repository.Repository(provider = "Eclipse_JNoSQL")
    private interface Server extends BasicRepository<Computer, String> {
    }

    @jakarta.data.repository.Repository(provider = "Other")
    private interface OtherServer extends BasicRepository<Computer, String> {
    }

    private interface StringSupplier extends Supplier<String> {

    }

    private interface Repository {

    }

    private static class Computer {

        @Id
        private String id;

        @Column
        private String name;
    }
}