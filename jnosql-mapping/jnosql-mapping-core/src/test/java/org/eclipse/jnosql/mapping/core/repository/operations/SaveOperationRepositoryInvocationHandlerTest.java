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
import java.util.Optional;

@EnableAutoWeld
@AddPackages(value = Convert.class)
@AddPackages(value = EntitiesMetadata.class)
@AddPackages(value = VetedConverter.class)
@AddPackages(value = InfrastructureOperatorProvider.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
@AddPackages(value = ReflectionClassConverter.class)
@DisplayName("Test scenario where the handler goes on the save provider")
class SaveOperationRepositoryInvocationHandlerTest {
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
        this.repositoryHandler = CoreRepositoryInvocationHandler.of(executor
                , entitiesMetadata.get(ComicBook.class),
                repositoriesMetadata.get(ComicBookRepository.class).orElseThrow(),
                infrastructureOperatorProvider,
                repositoryOperationProvider,
                template);
        comicBookRepository = (ComicBookRepository) Proxy.newProxyInstance(
                SaveOperationRepositoryInvocationHandlerTest.class.getClassLoader(),
                new Class[]{ComicBookRepository.class}, repositoryHandler);
    }

    @Test
    void shouldInvalidWhenParameterIsInvalid() {
        Assertions.assertThatThrownBy(() -> comicBookRepository.invalidSave())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
   void shouldInsertWhenThereIsNoEntityAtDatabase() {
       Mockito.when(template.find(ComicBook.class, "id")).thenReturn(Optional.empty());
       Mockito.when(template.insert(Mockito.any(ComicBook.class))).thenAnswer(invocation -> invocation.getArgument(0));
       var book = comicBookRepository.save(new ComicBook("id", "Book"));
       Mockito.verify(template).insert(Mockito.any(ComicBook.class));
       Assertions.assertThat(book).isNotNull();
    }


    @Test
    void shouldUpdateWhenThereIsAtDatabase() {
        Mockito.when(template.find(ComicBook.class, "id")).thenReturn(Optional.of(new ComicBook("id", "Book")));
        Mockito.when(template.update(Mockito.any(ComicBook.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var book = comicBookRepository.save(new ComicBook("id", "Book"));
        Mockito.verify(template).update(Mockito.any(ComicBook.class));
        Assertions.assertThat(book).isNotNull();
    }

    @Test
    void shouldSaveIterable() {
        Mockito.when(template.find(ComicBook.class, "id")).thenReturn(Optional.of(new ComicBook("id", "Book")));
        Mockito.when(template.update(Mockito.any(ComicBook.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var books = comicBookRepository.save(List.of(new ComicBook("id", "Book updated")));
        Mockito.verify(template).update(new ComicBook("id", "Book updated"));
        Assertions.assertThat(books).isNotNull().isNotEmpty().contains(new ComicBook("id", "Book updated"));
    }

    @Test
    void shouldSaveArray(){
        Mockito.when(template.find(ComicBook.class, "id")).thenReturn(Optional.of(new ComicBook("id", "Book")));
        Mockito.when(template.update(Mockito.any(ComicBook.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var books = comicBookRepository.save(new ComicBook[]{new ComicBook("id", "Book updated")});
        Mockito.verify(template).update(new ComicBook("id", "Book updated"));
        Assertions.assertThat(books).isNotNull().isNotEmpty().contains(new ComicBook("id", "Book updated"));
    }

}