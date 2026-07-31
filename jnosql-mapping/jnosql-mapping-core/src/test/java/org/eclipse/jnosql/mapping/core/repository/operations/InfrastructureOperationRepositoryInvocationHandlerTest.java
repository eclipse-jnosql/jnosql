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
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
@DisplayName("Infrastructure repository invocation handler")
class InfrastructureOperationRepositoryInvocationHandlerTest {

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
                        InfrastructureOperationRepositoryInvocationHandlerTest.class
                                .getClassLoader(),
                        new Class[]{ComicBookRepository.class},
                        repositoryHandler);
    }

    @Nested
    @DisplayName("When creating the repository proxy")
    class WhenCreateRepositoryProxy {

        @Test
        @DisplayName("Should create a non-null repository proxy")
        void shouldCreateRepositoryProxy() {
            assertThat(comicBookRepository).isNotNull();
        }
    }

    @Nested
    @DisplayName("When invoking Object methods")
    class WhenInvokeObjectMethods {

        @Test
        @DisplayName("Should execute toString, hashCode, and equals without errors")
        void shouldExecuteObjectMethods() {
            assertThatCode(comicBookRepository::toString)
                    .doesNotThrowAnyException();

            assertThatCode(comicBookRepository::hashCode)
                    .doesNotThrowAnyException();

            assertThatCode(() ->
                    comicBookRepository.equals(comicBookRepository))
                    .doesNotThrowAnyException();

            verifyNoInteractions(lifecycleEventHandler);
        }

        @Test
        @DisplayName("Should repeatedly execute a cached Object method without errors")
        void shouldExecuteCachedObjectMethod() {
            for (int index = 0; index < 10; index++) {
                assertThatCode(comicBookRepository::toString)
                        .doesNotThrowAnyException();
            }

            verifyNoInteractions(lifecycleEventHandler);
        }
    }

    @Nested
    @DisplayName("When invoking supported repository methods")
    class WhenInvokeSupportedRepositoryMethods {

        @Test
        @DisplayName("Should save a missing entity between pre-upsert and post-upsert events")
        void shouldSaveMissingEntityWithLifecycleEvents() {
            // given
            ComicBook book =
                    new ComicBook("123421", "Book Comic");

            when(template.find(ComicBook.class, "123421"))
                    .thenReturn(Optional.empty());
            when(template.insert(book))
                    .thenReturn(book);

            // when
            ComicBook result = comicBookRepository.save(book);

            // then
            assertThat(result).isSameAs(book);

            InOrder ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preUpsert(book);
            ordered.verify(template)
                    .find(ComicBook.class, "123421");
            ordered.verify(template).insert(book);
            ordered.verify(lifecycleEventHandler).postUpsert(book);
        }

        @Test
        @DisplayName("Should save all missing entities with upsert lifecycle events")
        void shouldSaveAllMissingEntitiesWithLifecycleEvents() {
            // given
            ComicBook book =
                    new ComicBook("123421", "Book Comic");

            when(template.find(ComicBook.class, "123421"))
                    .thenReturn(Optional.empty());
            when(template.insert(book))
                    .thenReturn(book);

            // when
            var result = comicBookRepository.saveAll(
                    Collections.singletonList(book));

            // then
            assertThat(result).containsExactly(book);

            InOrder ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preUpsert(book);
            ordered.verify(template)
                    .find(ComicBook.class, "123421");
            ordered.verify(template).insert(book);
            ordered.verify(lifecycleEventHandler).postUpsert(book);
        }
    }

    @Nested
    @DisplayName("When invoking repository component methods")
    class WhenInvokeComponentMethods {

        @Test
        @DisplayName("Should return the repository component value")
        void shouldReturnComponentValue() {
            String result = comicBookRepository.component();

            assertThat(result)
                    .isEqualTo("Game based on the Comic Book");

            verifyNoInteractions(lifecycleEventHandler);
        }

        @Test
        @DisplayName("Should execute the repository default method")
        void shouldExecuteDefaultMethod() {
            String result = comicBookRepository.defaultMethod();

            assertThat(result).isEqualTo("defaultMethod");

            verifyNoInteractions(lifecycleEventHandler);
        }
    }

    @Nested
    @DisplayName("When invoking unsupported repository methods")
    class WhenInvokeUnsupportedRepositoryMethods {

        @Test
        @DisplayName("Should reject an unsupported find-by method")
        void shouldRejectFindByMethod() {
            assertThatThrownBy(() ->
                    comicBookRepository.findByName("name"))
                    .isInstanceOf(UnsupportedOperationException.class);

            verifyNoInteractions(lifecycleEventHandler);
        }

        @Test
        @DisplayName("Should reject an unsupported count-by method")
        void shouldRejectCountByMethod() {
            assertThatThrownBy(() ->
                    comicBookRepository.countByName("name"))
                    .isInstanceOf(UnsupportedOperationException.class);

            verifyNoInteractions(lifecycleEventHandler);
        }

        @Test
        @DisplayName("Should reject an unsupported count-all method")
        void shouldRejectCountAllMethod() {
            assertThatThrownBy(comicBookRepository::countAll)
                    .isInstanceOf(UnsupportedOperationException.class);

            verifyNoInteractions(lifecycleEventHandler);
        }

        @Test
        @DisplayName("Should reject an unsupported exists-by method")
        void shouldRejectExistsByMethod() {
            assertThatThrownBy(() ->
                    comicBookRepository.existsByName("name"))
                    .isInstanceOf(UnsupportedOperationException.class);

            verifyNoInteractions(lifecycleEventHandler);
        }

        @Test
        @DisplayName("Should reject an unsupported delete-by method")
        void shouldRejectDeleteByMethod() {
            assertThatThrownBy(() ->
                    comicBookRepository.deleteByName("name"))
                    .isInstanceOf(UnsupportedOperationException.class);

            verifyNoInteractions(lifecycleEventHandler);
        }

        @Test
        @DisplayName("Should reject an unsupported custom find method")
        void shouldRejectCustomFindMethod() {
            assertThatThrownBy(() ->
                    comicBookRepository.find("name"))
                    .isInstanceOf(UnsupportedOperationException.class);

            verifyNoInteractions(lifecycleEventHandler);
        }

        @Test
        @DisplayName("Should reject an unsupported cursor method")
        void shouldRejectCursorMethod() {
            assertThatThrownBy(comicBookRepository::cursor)
                    .isInstanceOf(UnsupportedOperationException.class);

            verifyNoInteractions(lifecycleEventHandler);
        }
    }
}