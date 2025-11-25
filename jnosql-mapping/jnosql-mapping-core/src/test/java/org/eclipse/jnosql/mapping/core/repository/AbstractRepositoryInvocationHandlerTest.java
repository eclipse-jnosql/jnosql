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
import org.eclipse.jnosql.mapping.core.entities.ComicBook;
import org.eclipse.jnosql.mapping.core.entities.ComicBookRepository;
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
@AddPackages(value = InfrastructureOperatorProvider.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
@AddPackages(value = ReflectionClassConverter.class)
class AbstractRepositoryInvocationHandlerTest {
    private Template template;
    @Inject
    private EntitiesMetadata entitiesMetadata;
    @Inject
    private RepositoriesMetadata repositoriesMetadata;
    @Inject
    private InfrastructureOperatorProvider infrastructureOperatorProvider;
    private RepositoryExecutor executor;
    private TestRepositoryInvocationHandler<?,?> repositoryHandler;
    private ComicBookRepository comicBookRepository;

    @BeforeEach
    void setUp(){
        this.template = Mockito.mock(Template.class);
        this.executor = new RepositoryExecutor();
        this.repositoryHandler = new TestRepositoryInvocationHandler<>(executor);
        comicBookRepository = (ComicBookRepository) Proxy.newProxyInstance(
                AbstractRepositoryInvocationHandlerTest.class.getClassLoader(),
                new Class[] { ComicBookRepository.class }, repositoryHandler);
    }

    @Test
    void shouldInstantiateHandler() {
        Assertions.assertThat(comicBookRepository).isNotNull();
    }

    @Test
    void shouldExecuteMethodsFromObject() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThatCode(() -> comicBookRepository.toString()).doesNotThrowAnyException();
            softly.assertThatCode(() -> comicBookRepository.hashCode()).doesNotThrowAnyException();
            softly.assertThatCode(() -> comicBookRepository.equals(comicBookRepository)).doesNotThrowAnyException();
        });
    }

    @Test
    void shouldCacheMapResult() {
        for (int index = 0; index < 10; index++) {
            Assertions.assertThatCode(() -> comicBookRepository.toString()).doesNotThrowAnyException();
        }
    }

    @Test
    void shouldExecuteCustomMethods() {
        Mockito.when(template.insert(Mockito.any(ComicBook.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ComicBook bookComic = comicBookRepository.save(new ComicBook("123421", "Book Comic"));
        Assertions.assertThat(bookComic).isNotNull();
        Mockito.verify(template).insert(bookComic);
    }

   private class RepositoryExecutor extends AbstractRepository<ComicBook, Long> {

        @Override
        protected Template template() {
            return template;
        }

        @Override
        protected EntityMetadata entityMetadata() {
            return entitiesMetadata.get(ComicBook.class);
        }
    }

    public class TestRepositoryInvocationHandler<T, K> extends AbstractRepositoryInvocationHandler {

        private final AbstractRepository<?,?> repository;

        TestRepositoryInvocationHandler(AbstractRepository<?,?> repository) {
            this.repository = repository;
        }


        @Override
        protected AbstractRepository<?,?> repository() {
            return repository;
        }

        @Override
        protected EntityMetadata entityMetadata() {
            return entitiesMetadata.get(ComicBook.class);
        }

        @Override
        protected RepositoryMetadata repositoryMetadata() {
            return repositoriesMetadata.get(ComicBookRepository.class).orElseThrow();
        }

        @Override
        protected InfrastructureOperatorProvider infrastructureOperatorProvider() {
            return infrastructureOperatorProvider;
        }
    }
}