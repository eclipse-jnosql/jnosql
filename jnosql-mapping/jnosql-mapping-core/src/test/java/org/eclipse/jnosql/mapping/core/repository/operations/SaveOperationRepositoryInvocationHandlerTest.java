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
import org.eclipse.jnosql.mapping.core.entities.InvalidEntity;
import org.eclipse.jnosql.mapping.core.entities.InvalidEntityRepository;
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
import org.mockito.InOrder;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(Convert.class)
@AddPackages(EntitiesMetadata.class)
@AddPackages(VetedConverter.class)
@AddPackages(InfrastructureOperatorProvider.class)
@AddExtensions(ReflectionEntityMetadataExtension.class)
@AddPackages(ReflectionClassConverter.class)
@DisplayName("Save repository invocation handler")
class SaveOperationRepositoryInvocationHandlerTest {

    private Template template;

    private LifecycleEventHandler lifecycleEventHandler;

    @Inject
    private EntitiesMetadata entitiesMetadata;

    @Inject
    private RepositoriesMetadata repositoriesMetadata;

    @Inject
    private InfrastructureOperatorProvider infrastructureOperatorProvider;

    private ComicBookRepository comicBookRepository;

    private InvalidEntityRepository invalidEntityRepository;

    @BeforeEach
    void setUp() {
        this.template = mock(Template.class);
        this.lifecycleEventHandler = mock(LifecycleEventHandler.class);

        var executor = new TestRepositoryExecutor(
                template,
                entitiesMetadata,
                lifecycleEventHandler);

        var repositoryOperationProvider = new CoreBaseRepositoryOperationProvider(
                null,
                null,
                null,
                new CoreSaveOperation(lifecycleEventHandler),
                null);

        var repositoryHandler = CoreRepositoryInvocationHandler.of(
                executor,
                entitiesMetadata.get(ComicBook.class),
                repositoriesMetadata.get(ComicBookRepository.class)
                        .orElseThrow(),
                infrastructureOperatorProvider,
                repositoryOperationProvider,
                template);

        this.comicBookRepository = (ComicBookRepository) Proxy.newProxyInstance(
                SaveOperationRepositoryInvocationHandlerTest.class.getClassLoader(),
                new Class[]{ComicBookRepository.class},
                repositoryHandler);

        var invalidHandler = CoreRepositoryInvocationHandler.of(
                executor,
                entitiesMetadata.get(InvalidEntity.class),
                repositoriesMetadata.get(InvalidEntityRepository.class)
                        .orElseThrow(),
                infrastructureOperatorProvider,
                repositoryOperationProvider,
                template);

        this.invalidEntityRepository =
                (InvalidEntityRepository) Proxy.newProxyInstance(
                        SaveOperationRepositoryInvocationHandlerTest.class
                                .getClassLoader(),
                        new Class[]{InvalidEntityRepository.class},
                        invalidHandler);
    }

    @Nested
    @DisplayName("When saving one entity")
    class WhenSaveEntity {

        @Test
        @DisplayName("Should reject a save method without the required parameter")
        void shouldRejectSaveWithoutRequiredParameter() {
            assertThatThrownBy(comicBookRepository::invalidSave)
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(lifecycleEventHandler);
        }

        @Test
        @DisplayName("Should insert a missing entity between pre-upsert and post-upsert events")
        void shouldInsertWhenEntityDoesNotExist() {
            // given
            ComicBook book = new ComicBook("id", "Book");

            when(template.find(ComicBook.class, "id"))
                    .thenReturn(Optional.empty());
            when(template.insert(book))
                    .thenReturn(book);

            // when
            ComicBook result = comicBookRepository.save(book);

            // then
            assertThat(result).isSameAs(book);

            InOrder ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preUpsert(book);
            ordered.verify(template).find(ComicBook.class, "id");
            ordered.verify(template).insert(book);
            ordered.verify(lifecycleEventHandler).postUpsert(book);
        }

        @Test
        @DisplayName("Should update an existing entity between pre-upsert and post-upsert events")
        void shouldUpdateWhenEntityExists() {
            // given
            ComicBook book = new ComicBook("id", "Book");

            when(template.find(ComicBook.class, "id"))
                    .thenReturn(Optional.of(book));
            when(template.update(book))
                    .thenReturn(book);

            // when
            ComicBook result = comicBookRepository.save(book);

            // then
            assertThat(result).isSameAs(book);

            InOrder ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preUpsert(book);
            ordered.verify(template).find(ComicBook.class, "id");
            ordered.verify(template).update(book);
            ordered.verify(lifecycleEventHandler).postUpsert(book);
        }
    }

    @Nested
    @DisplayName("When saving multiple entities")
    class WhenSaveMultipleEntities {

        @Test
        @DisplayName("Should save iterable entities with upsert lifecycle events for each entity")
        void shouldSaveIterableWithLifecycleEvents() {
            // given
            ComicBook book = new ComicBook("id", "Book updated");

            when(template.find(ComicBook.class, "id"))
                    .thenReturn(Optional.of(book));
            when(template.update(book))
                    .thenReturn(book);

            // when
            List<ComicBook> result =
                    comicBookRepository.save(List.of(book));

            // then
            assertThat(result).containsExactly(book);

            InOrder ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preUpsert(book);
            ordered.verify(template).find(ComicBook.class, "id");
            ordered.verify(template).update(book);
            ordered.verify(lifecycleEventHandler).postUpsert(book);
        }

        @Test
        @DisplayName("Should save array entities with upsert lifecycle events for each entity")
        void shouldSaveArrayWithLifecycleEvents() {
            // given
            ComicBook book = new ComicBook("id", "Book updated");

            when(template.find(ComicBook.class, "id"))
                    .thenReturn(Optional.of(book));
            when(template.update(book))
                    .thenReturn(book);

            // when
            ComicBook[] result =
                    comicBookRepository.save(new ComicBook[]{book});

            // then
            assertThat(result).containsExactly(book);

            InOrder ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preUpsert(book);
            ordered.verify(template).find(ComicBook.class, "id");
            ordered.verify(template).update(book);
            ordered.verify(lifecycleEventHandler).postUpsert(book);
        }
    }

    @Nested
    @DisplayName("When saving an invalid entity")
    class WhenSaveInvalidEntity {

        @Test
        @DisplayName("Should reject an entity without an identifier and not fire a post-upsert event")
        void shouldRejectEntityWithoutIdentifier() {
            // given
            InvalidEntity entity = new InvalidEntity("Invalid");

            // when, then
            assertThatThrownBy(
                    () -> invalidEntityRepository.invalidSave(entity))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(lifecycleEventHandler).preUpsert(entity);
            verify(lifecycleEventHandler, never()).postUpsert(any());
        }
    }
}