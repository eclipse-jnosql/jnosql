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
package org.eclipse.jnosql.mapping.semistructured.repository;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InterceptionFactory;
import org.eclipse.jnosql.mapping.core.repository.CoreRepositoryInvocationHandler;
import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.query.Tasks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SemistructuredRepositoryProducer")
class SemistructuredRepositoryProducerTest {

    private final EntitiesMetadata entities = mock(EntitiesMetadata.class);
    private final InfrastructureOperatorProvider infrastructureOperatorProvider =
            mock(InfrastructureOperatorProvider.class);
    private final SemistructuredRepositoryOperationProvider operationProvider =
            mock(SemistructuredRepositoryOperationProvider.class);
    private final RepositoriesMetadata repositoriesMetadata = mock(RepositoriesMetadata.class);
    private final LifecycleEventHandler lifecycleEventHandler = mock(LifecycleEventHandler.class);
    private final BeanManager beanManager = mock(BeanManager.class);
    private final CreationalContext<Tasks> creationalContext = mock(CreationalContext.class);
    private final InterceptionFactory<Tasks> interceptionFactory = mock(InterceptionFactory.class);
    private final SemiStructuredTemplate template = mock(SemiStructuredTemplate.class);
    private final RepositoryMetadata repositoryMetadata = mock(RepositoryMetadata.class);
    private final EntityMetadata entityMetadata = mock(EntityMetadata.class);

    private SemistructuredRepositoryProducer producer;

    @BeforeEach
    void setUp() {
        producer = new SemistructuredRepositoryProducer(entities,
                infrastructureOperatorProvider,
                operationProvider,
                repositoriesMetadata,
                lifecycleEventHandler,
                beanManager);
    }

    @Nested
    @DisplayName("when validating repository creation arguments")
    class Validation {

        @Test
        @DisplayName("throws NullPointerException when the repository class is null")
        void shouldRejectNullRepositoryClass() {
            assertThatThrownBy(() -> producer.get(null, template))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("throws NullPointerException when the template is null")
        void shouldRejectNullTemplate() {
            assertThatThrownBy(() -> producer.get(Tasks.class, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("when repository metadata is resolved")
    class ProxyCreation {

        @BeforeEach
        void setUpMetadata() {
            when(repositoriesMetadata.get(Tasks.class)).thenReturn(Optional.of(repositoryMetadata));
            when(repositoryMetadata.entity()).thenReturn(Optional.of(Object.class));
            when(entities.get(Object.class)).thenReturn(entityMetadata);
        }

        @Test
        @DisplayName("returns the repository instance created by CDI interception")
        void shouldCreateCdiInterceptedRepository() {
            Tasks expected = mock(Tasks.class);
            doReturn(creationalContext).when(beanManager).createCreationalContext(null);
            doReturn(interceptionFactory).when(beanManager)
                    .createInterceptionFactory(creationalContext, Tasks.class);
            when(interceptionFactory.createInterceptedInstance(any())).thenReturn(expected);

            Tasks result = producer.get(Tasks.class, template);

            ArgumentCaptor<Tasks> repositoryCaptor = ArgumentCaptor.forClass(Tasks.class);
            verify(interceptionFactory).createInterceptedInstance(repositoryCaptor.capture());
            assertThat(Proxy.getInvocationHandler(repositoryCaptor.getValue()))
                    .isInstanceOf(CoreRepositoryInvocationHandler.class);
            assertThat(result).isSameAs(expected);
        }
    }

    @Nested
    @DisplayName("when instantiated for CDI proxying")
    class Construction {

        @Test
        @DisplayName("provides a package-private no-argument constructor for CDI")
        void shouldProvideDefaultConstructor() {
            assertThat(new SemistructuredRepositoryProducer()).isNotNull();
        }
    }
}
