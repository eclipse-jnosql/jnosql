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

import jakarta.data.metamodel.TextAttribute;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@EnableAutoWeld
@AddPackages(Convert.class)
@AddPackages(EntitiesMetadata.class)
@AddPackages(VetedConverter.class)
@AddPackages(InfrastructureOperatorProvider.class)
@AddExtensions(ReflectionEntityMetadataExtension.class)
@AddPackages(ReflectionClassConverter.class)
@DisplayName("Delete repository invocation handler")
class DeleteOperationRepositoryInvocationHandlerTest {

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
                        DeleteOperationRepositoryInvocationHandlerTest.class
                                .getClassLoader(),
                        new Class[]{ComicBookRepository.class},
                        repositoryHandler);
    }

    @Nested
    @DisplayName("When deleting one entity")
    class WhenDeleteEntity {

        @Test
        @DisplayName("Should reject a delete method without the required parameter")
        void shouldRejectDeleteWithoutRequiredParameter() {
            assertThatThrownBy(comicBookRepository::invalidDelete)
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(lifecycleEventHandler);
        }

        @Test
        @DisplayName("Should reject a delete method with an unsupported return type")
        void shouldRejectDeleteWithUnsupportedReturnType() {
            // given
            ComicBook book = new ComicBook("1234", "Book");

            // when, then
            assertThatThrownBy(() -> comicBookRepository.invalidDelete(book))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(template, never()).delete(any(ComicBook.class));
            verifyNoInteractions(lifecycleEventHandler);
        }

        @Test
        @DisplayName("Should delete one entity between pre-delete and post-delete events")
        void shouldDeleteEntityWithLifecycleEvents() {
            // given
            ComicBook book = new ComicBook("1234", "Book");

            // when
            comicBookRepository.delete(book);

            // then
            InOrder ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preDelete(book);
            ordered.verify(template).delete(book);
            ordered.verify(lifecycleEventHandler).postDelete(book);
        }
    }

    @Nested
    @DisplayName("When deleting multiple entities")
    class WhenDeleteMultipleEntities {

        @Test
        @DisplayName("Should delete iterable entities with lifecycle events for each entity")
        void shouldDeleteIterableWithLifecycleEvents() {
            // given
            ComicBook book = new ComicBook("1234", "Book");
            List<ComicBook> books = List.of(book);

            // when
            comicBookRepository.delete(books);

            // then
            InOrder ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preDelete(book);
            ordered.verify(template).delete(books);
            ordered.verify(lifecycleEventHandler).postDelete(book);
        }

        @Test
        @DisplayName("Should delete array entities with lifecycle events for each entity")
        void shouldDeleteArrayWithLifecycleEvents() {
            // given
            ComicBook book = new ComicBook("1234", "Book");

            // when
            comicBookRepository.delete(new ComicBook[]{book});

            // then
            InOrder ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preDelete(book);
            ordered.verify(template).delete(List.of(book));
            ordered.verify(lifecycleEventHandler).postDelete(book);
        }
    }

    @Nested
    @DisplayName("When deleting by restriction")
    class WhenDeleteByRestriction {

        @Test
        @DisplayName("Should reject restriction-based deletion when the provider does not support it")
        void shouldRejectUnsupportedRestrictionDelete() {
            // given
            TextAttribute<ComicBook> name =
                    TextAttribute.of(ComicBook.class, "name");

            // when, then
            assertThatThrownBy(
                    () -> comicBookRepository.delete(
                            name.contains("Marvel")))
                    .isInstanceOf(UnsupportedOperationException.class);

            verifyNoInteractions(lifecycleEventHandler);
        }
    }

    @Nested
    @DisplayName("When invoking Object methods")
    class WhenInvokeObjectMethods {

        @Test
        @DisplayName("Should return a non-null textual representation for the repository proxy")
        void shouldReturnRepositoryToString() {
            assertThat(comicBookRepository.toString()).isNotNull();
        }
    }
}