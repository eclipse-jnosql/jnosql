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
@DisplayName("Update repository invocation handler")
class UpdateOperationRepositoryInvocationHandlerTest {

    private Template template;

    private LifecycleEventHandler lifecycleEventHandler;

    @Inject
    private EntitiesMetadata entitiesMetadata;

    @Inject
    private RepositoriesMetadata repositoriesMetadata;

    @Inject
    private InfrastructureOperatorProvider infrastructureOperatorProvider;

    @Inject
    private CoreBaseRepositoryOperationProvider repositoryOperationProvider;

    private ComicBookRepository comicBookRepository;

    @BeforeEach
    void setUp() {
        this.template = mock(Template.class);
        this.lifecycleEventHandler = mock(LifecycleEventHandler.class);

        var executor = new TestRepositoryExecutor(
                template,
                entitiesMetadata,
                lifecycleEventHandler);

        var repositoryHandler = CoreRepositoryInvocationHandler.of(
                executor,
                entitiesMetadata.get(ComicBook.class),
                repositoriesMetadata.get(ComicBookRepository.class)
                        .orElseThrow(),
                infrastructureOperatorProvider,
                repositoryOperationProvider,
                template);

        this.comicBookRepository =
                (ComicBookRepository) Proxy.newProxyInstance(
                        UpdateOperationRepositoryInvocationHandlerTest.class
                                .getClassLoader(),
                        new Class[]{ComicBookRepository.class},
                        repositoryHandler);
    }

    @Nested
    @DisplayName("When updating one entity")
    class WhenUpdateEntity {

        @Test
        @DisplayName("Should reject an update method without the required parameter")
        void shouldRejectUpdateWithoutRequiredParameter() {
            assertThatThrownBy(comicBookRepository::invalidUpdate)
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(lifecycleEventHandler);
        }

        @Test
        @DisplayName("Should update an entity between pre-update and post-update events for a void method")
        void shouldUpdateEntityWithVoidReturnAndLifecycleEvents() {
            // given
            ComicBook book = new ComicBook("1234", "Book");

            when(template.update(any(ComicBook.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            comicBookRepository.update(book);

            // then
            InOrder ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preUpdate(book);
            ordered.verify(template).update(book);
            ordered.verify(lifecycleEventHandler).postUpdate(book);
        }

        @Test
        @DisplayName("Should return the updated entity after pre-update and post-update events")
        void shouldReturnUpdatedEntityWithLifecycleEvents() {
            // given
            ComicBook book = new ComicBook("1234", "Book");

            when(template.update(book))
                    .thenReturn(book);

            // when
            ComicBook result = comicBookRepository.update(book);

            // then
            assertThat(result)
                    .isNotNull()
                    .isSameAs(book);

            InOrder ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preUpdate(book);
            ordered.verify(template).update(book);
            ordered.verify(lifecycleEventHandler).postUpdate(book);
        }
    }

    @Nested
    @DisplayName("When updating multiple entities")
    class WhenUpdateMultipleEntities {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should return updated iterable entities after firing lifecycle events for each entity")
        void shouldUpdateIterableWithLifecycleEvents() {
            // given
            ComicBook book = new ComicBook("1234", "Book");
            List<ComicBook> books = List.of(book);

            when(template.update(any(Iterable.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            List<ComicBook> result = comicBookRepository.update(books);

            // then
            assertThat(result)
                    .isNotNull()
                    .containsExactly(book);

            InOrder ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preUpdate(book);
            ordered.verify(template).update(books);
            ordered.verify(lifecycleEventHandler).postUpdate(book);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should return updated array entities after firing lifecycle events for each entity")
        void shouldUpdateArrayWithLifecycleEvents() {
            // given
            ComicBook book = new ComicBook("1234", "Book");

            when(template.update(any(Iterable.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            ComicBook[] result =
                    comicBookRepository.update(new ComicBook[]{book});

            // then
            assertThat(result)
                    .isNotNull()
                    .containsExactly(book);

            InOrder ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preUpdate(book);
            ordered.verify(template).update(List.of(book));
            ordered.verify(lifecycleEventHandler).postUpdate(book);
        }
    }
}