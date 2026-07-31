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
import org.eclipse.jnosql.mapping.DynamicQueryException;
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

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

@EnableAutoWeld
@AddPackages(Convert.class)
@AddPackages(EntitiesMetadata.class)
@AddPackages(VetedConverter.class)
@AddPackages(InfrastructureOperatorProvider.class)
@AddExtensions(ReflectionEntityMetadataExtension.class)
@AddPackages(ReflectionClassConverter.class)
@DisplayName("Provider-defined repository operations")
class ProviderOperationRepositoryInvocationHandlerTest {

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
                        ProviderOperationRepositoryInvocationHandlerTest.class
                                .getClassLoader(),
                        new Class[]{ComicBookRepository.class},
                        repositoryHandler);
    }

    @Nested
    @DisplayName("When executing a provider-defined operation")
    class WhenExecuteProviderOperation {

        @Test
        @DisplayName("Should execute the provider-defined query without lifecycle events")
        void shouldExecuteProviderOperationWithoutLifecycleEvents() {
            String result = comicBookRepository.sampleProvider("test");

            assertThat(result)
                    .contains("Sample of query using provider test");

            verifyNoInteractions(lifecycleEventHandler);
        }
    }

    @Nested
    @DisplayName("When executing an invalid provider-defined operation")
    class WhenExecuteInvalidProviderOperation {

        @Test
        @DisplayName("Should reject the invalid provider query without lifecycle events")
        void shouldRejectInvalidProviderOperationWithoutLifecycleEvents() {
            assertThatThrownBy(
                    () -> comicBookRepository.invalidProvider("test"))
                    .isInstanceOf(DynamicQueryException.class);

            verifyNoInteractions(lifecycleEventHandler);
        }
    }
}