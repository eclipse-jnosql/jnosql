/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import jakarta.inject.Inject;
import jakarta.nosql.Convert;
import jakarta.nosql.Template;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.core.VetedConverter;
import org.eclipse.jnosql.mapping.core.entities.Person;
import org.eclipse.jnosql.mapping.core.entities.PersonRepository;
import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.reflection.ReflectionClassConverter;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;

@EnableAutoWeld
@AddPackages(value = Convert.class)
@AddPackages(value = EntitiesMetadata.class)
@AddPackages(value = VetedConverter.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
@AddPackages(value = ReflectionClassConverter.class)
class AbstractRepositoryInvocationHandlerTest {
    private Template template;

    @Inject
    private EntitiesMetadata entitiesMetadata;

    @Inject
    private RepositoriesMetadata repositoriesMetadata;

    private RepositoryExecutor executor;

    private TestRepositoryInvocationHandler repositoryHandler;

    private PersonRepository personRepository;

    @BeforeEach
    void setUp(){
        this.template = Mockito.mock(Template.class);
        this.executor = new RepositoryExecutor();
        this.repositoryHandler = new TestRepositoryInvocationHandler<>(executor);
        personRepository = (PersonRepository) Proxy.newProxyInstance(
                AbstractRepositoryInvocationHandlerTest.class.getClassLoader(),
                new Class[] { PersonRepository.class }, repositoryHandler);
    }

    @Test
    void shouldInstantiateHandler() {
        Assertions.assertThat(personRepository).isNotNull();
    }

    @Test
    void shouldExecuteMethodsFromObject() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThatCode(() -> personRepository.toString()).doesNotThrowAnyException();
            softly.assertThatCode(() -> personRepository.hashCode()).doesNotThrowAnyException();
            softly.assertThatCode(() -> personRepository.equals(personRepository)).doesNotThrowAnyException();
        });
    }

    @Test
    void shouldCacheMapResult() {
        for (int index = 0; index < 10; index++) {
            Assertions.assertThatCode(() -> personRepository.toString()).doesNotThrowAnyException();
        }
    }


   private class RepositoryExecutor extends AbstractRepository<Person, Long> {

        @Override
        protected Template template() {
            return template;
        }

        @Override
        protected EntityMetadata entityMetadata() {
            return entitiesMetadata.get(Person.class);
        }
    }

    public class TestRepositoryInvocationHandler<T, K> extends AbstractRepositoryInvocationHandler {

        private final AbstractRepository repository;

        TestRepositoryInvocationHandler(AbstractRepository repository) {
            this.repository = repository;
        }


        @Override
        protected AbstractRepository repository() {
            return repository;
        }

        @Override
        protected Class<?> repositoryType() {
            return Person.class;
        }

        @Override
        protected EntityMetadata entityMetadata() {
            return entitiesMetadata.get(Person.class);
        }

        @Override
        protected RepositoryMetadata repositoryMetadata() {
            return repositoriesMetadata.get(PersonRepository.class).orElseThrow();
        }
    }
}