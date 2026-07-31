/*
 *  Copyright (c) 2025-2026 Contributors to the Eclipse Foundation
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
import org.eclipse.jnosql.mapping.core.VetedConverter;
import org.eclipse.jnosql.mapping.core.entities.ComicBook;
import org.eclipse.jnosql.mapping.core.entities.ComicBookRepository;
import org.eclipse.jnosql.mapping.core.repository.CoreRepositoryInvocationHandler;
import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.reflection.ReflectionClassConverter;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(Convert.class)
@AddPackages(EntitiesMetadata.class)
@AddPackages(VetedConverter.class)
@AddPackages(InfrastructureOperatorProvider.class)
@AddExtensions(ReflectionEntityMetadataExtension.class)
@AddPackages(ReflectionClassConverter.class)
@DisplayName("Insert repository invocation handler")
class InsertOperationRepositoryInvocationHandlerTest {

    private Template template;

    private LifecycleEventHandler lifecycleEventHandler;

    @Inject
    private EntitiesMetadata entitiesMetadata;

    @Inject
    private RepositoriesMetadata repositoriesMetadata;

    @Inject
    private InfrastructureOperatorProvider infrastructureOperatorProvider;

    private ComicBookRepository comicBookRepository;

    @BeforeEach
    void setUp() {
        this.template = mock(Template.class);
        this.lifecycleEventHandler = mock(LifecycleEventHandler.class);

        var executor = new TestRepositoryExecutor(
                template,
                entitiesMetadata,
                lifecycleEventHandler);

        var repositoryOperationProvider = new CoreBaseRepositoryOperationProvider(
                new CoreInsertOperation(lifecycleEventHandler),
                null,
                null,
                null,
                null);

        CoreRepositoryInvocationHandler<?, ?> repositoryHandler =
                CoreRepositoryInvocationHandler.of(
                        executor,
                        entitiesMetadata.get(ComicBook.class),
                        repositoriesMetadata.get(ComicBookRepository.class)
                                .orElseThrow(),
                        infrastructureOperatorProvider,
                        repositoryOperationProvider,
                        template);

        this.comicBookRepository = (ComicBookRepository) Proxy.newProxyInstance(
                InsertOperationRepositoryInvocationHandlerTest.class.getClassLoader(),
                new Class[]{ComicBookRepository.class},
                repositoryHandler);
    }

    @Nested
    @DisplayName("When inserting one entity")
    class WhenInsertEntity {

        @Test
        @DisplayName("Should reject an insert method without the required parameter")
        void shouldRejectInsertWithoutRequiredParameter() {
            assertThatThrownBy(comicBookRepository::invalidInsert)
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(lifecycleEventHandler);
        }

        @Test
        @DisplayName("Should insert an entity between pre-insert and post-insert events for a void method")
        void shouldInsertEntityWithVoidReturnAndLifecycleEvents() {
            // given
            ComicBook book = new ComicBook("1234", "Book");

            when(template.insert(any(ComicBook.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            comicBookRepository.insertVoid(book);

            // then
            InOrder ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preInsert(book);
            ordered.verify(template).insert(book);
            ordered.verify(lifecycleEventHandler).postInsert(book);
        }

        @Test
        @DisplayName("Should return the inserted entity after pre-insert and post-insert events")
        void shouldReturnInsertedEntityWithLifecycleEvents() {
            // given
            ComicBook book = new ComicBook("1234", "Book");

            when(template.insert(any(ComicBook.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            ComicBook result = comicBookRepository.insert(book);

            // then
            assertThat(result)
                    .isNotNull()
                    .isSameAs(book);

            InOrder ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preInsert(book);
            ordered.verify(template).insert(book);
            ordered.verify(lifecycleEventHandler).postInsert(book);
        }
    }

    @Nested
    @DisplayName("When inserting multiple entities")
    class WhenInsertMultipleEntities {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should return inserted iterable entities after firing lifecycle events for each entity")
        void shouldInsertIterableWithLifecycleEvents() {
            // given
            ComicBook book = new ComicBook("1234", "Book");
            List<ComicBook> books = List.of(book);

            when(template.insert(any(Iterable.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            List<ComicBook> result = comicBookRepository.insert(books);

            // then
            assertThat(result)
                    .isNotNull()
                    .containsExactly(book);

            ArgumentCaptor<Iterable<ComicBook>> captor =
                    ArgumentCaptor.forClass(Iterable.class);

            InOrder ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preInsert(book);
            ordered.verify(template).insert(captor.capture());
            ordered.verify(lifecycleEventHandler).postInsert(book);

            assertThat(captor.getValue())
                    .containsExactly(book);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should return inserted array entities after firing lifecycle events for each entity")
        void shouldInsertArrayWithLifecycleEvents() {
            // given
            ComicBook book = new ComicBook("1234", "Book");
            ComicBook[] books = {book};

            when(template.insert(any(Iterable.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            ComicBook[] result = comicBookRepository.insert(books);

            // then
            assertThat(result)
                    .isNotNull()
                    .containsExactly(book);

            ArgumentCaptor<Iterable<ComicBook>> captor =
                    ArgumentCaptor.forClass(Iterable.class);

            InOrder ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preInsert(book);
            ordered.verify(template).insert(captor.capture());
            ordered.verify(lifecycleEventHandler).postInsert(book);

            assertThat(captor.getValue())
                    .containsExactly(book);
        }
    }
}