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
package org.eclipse.jnosql.mapping.core.repository.operations;

import jakarta.inject.Inject;
import jakarta.nosql.Convert;
import jakarta.nosql.Template;
import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.mapping.core.VetedConverter;
import org.eclipse.jnosql.mapping.core.entities.ComicBook;
import org.eclipse.jnosql.mapping.core.entities.ComicBookRepository;
import org.eclipse.jnosql.mapping.core.repository.CoreRepositoryInvocationHandler;
import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.reflection.ReflectionClassConverter;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;
import java.util.List;

@EnableAutoWeld
@AddPackages(value = Convert.class)
@AddPackages(value = EntitiesMetadata.class)
@AddPackages(value = VetedConverter.class)
@AddPackages(value = InfrastructureOperatorProvider.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
@AddPackages(value = ReflectionClassConverter.class)
@DisplayName("Test scenario where the handler goes on the update provider")
class UpdateOperationRepositoryInvocationHandlerTest {
    private Template template;
    @Inject
    private EntitiesMetadata entitiesMetadata;
    @Inject
    private RepositoriesMetadata repositoriesMetadata;
    @Inject
    private InfrastructureOperatorProvider infrastructureOperatorProvider;

    @Inject
    private CoreBaseRepositoryOperationProvider repositoryOperationProvider;
    private TestRepositoryExecutor executor;
    private CoreRepositoryInvocationHandler<?, ?> repositoryHandler;
    private ComicBookRepository comicBookRepository;

    @BeforeEach
    void setUp() {
        this.template = Mockito.mock(Template.class);
        this.executor = new TestRepositoryExecutor(this.template, entitiesMetadata);
        this.repositoryHandler =  CoreRepositoryInvocationHandler.of(executor
                , entitiesMetadata.get(ComicBook.class),
                repositoriesMetadata.get(ComicBookRepository.class).orElseThrow(),
                infrastructureOperatorProvider,
                repositoryOperationProvider,
                template);
        comicBookRepository = (ComicBookRepository) Proxy.newProxyInstance(
                UpdateOperationRepositoryInvocationHandlerTest.class.getClassLoader(),
                new Class[]{ComicBookRepository.class}, repositoryHandler);
    }

    @Test
    void shouldInvalidWhenParameterIsInvalid() {
        Assertions.assertThatThrownBy(() -> comicBookRepository.invalidUpdate())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldUpdateVoid() {
        Mockito.when(template.update(Mockito.any(ComicBook.class))).thenAnswer(invocation -> invocation.getArgument(0));
        comicBookRepository.update(new ComicBook("1234", "Book"));
        Mockito.verify(template).update(Mockito.any(ComicBook.class));
    }

    @Test
    void shouldUpdateReturn() {
        Mockito.when(template.update(Mockito.any(ComicBook.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ComicBook book = comicBookRepository.update(new ComicBook("1234", "Book"));
        Assertions.assertThat(book).isNotNull().isInstanceOf(ComicBook.class);
        Mockito.verify(template).update(Mockito.any(ComicBook.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldUpdateIterableReturn() {
        Mockito.when(template.update(Mockito.any(Iterable.class))).thenAnswer(invocation -> invocation.getArgument(0));
        List<ComicBook> books = comicBookRepository.update(List.of(new ComicBook("1234", "Book")));
        Assertions.assertThat(books).isNotNull().isNotEmpty().contains(new ComicBook("1234", "Book"));
        Mockito.verify(template).update(Mockito.any(Iterable.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldUpdateArrayReturn() {
        Mockito.when(template.update(Mockito.any(Iterable.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ComicBook[] books = comicBookRepository.update(new ComicBook[]{new ComicBook("1234", "Book")});
        Assertions.assertThat(books).isNotNull().isNotEmpty().contains(new ComicBook("1234", "Book"));
        Mockito.verify(template).update(Mockito.any(Iterable.class));
    }

    void shouldFindBy()


}