/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.keyvalue.query;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InterceptionFactory;
import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.eclipse.jnosql.mapping.core.repository.CoreRepositoryInvocationHandler;
import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreBaseRepositoryOperationProvider;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplate;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplateProducer;
import org.eclipse.jnosql.mapping.keyvalue.entities.Person;
import org.eclipse.jnosql.mapping.keyvalue.entities.PersonRepository;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("KeyValueRepositoryProducer")
class KeyValueRepositoryProducerTest {

    private final KeyValueTemplateProducer templateProducer = mock(KeyValueTemplateProducer.class);
    private final EntitiesMetadata entities = mock(EntitiesMetadata.class);
    private final InfrastructureOperatorProvider infrastructureOperatorProvider =
            mock(InfrastructureOperatorProvider.class);
    private final CoreBaseRepositoryOperationProvider operationProvider =
            mock(CoreBaseRepositoryOperationProvider.class);
    private final RepositoriesMetadata repositoriesMetadata = mock(RepositoriesMetadata.class);
    private final LifecycleEventHandler lifecycleEventHandler = mock(LifecycleEventHandler.class);
    private final BeanManager beanManager = mock(BeanManager.class);
    private final KeyValueTemplate template = mock(KeyValueTemplate.class);
    private final RepositoryMetadata repositoryMetadata = mock(RepositoryMetadata.class);
    private final EntityMetadata entityMetadata = mock(EntityMetadata.class);
    private final CreationalContext<PersonRepository> creationalContext = mock(CreationalContext.class);
    private final InterceptionFactory<PersonRepository> interceptionFactory = mock(InterceptionFactory.class);

    private KeyValueRepositoryProducer producer;

    @BeforeEach
    void setUp() {
        producer = new KeyValueRepositoryProducer(templateProducer,
                entities,
                infrastructureOperatorProvider,
                operationProvider,
                repositoriesMetadata,
                lifecycleEventHandler,
                beanManager);
    }

    @Nested
    @DisplayName("When validating repository creation")
    class WhenTheValidation {

        @Test
        @DisplayName("Should reject a null repository class")
        void shouldRejectNullRepositoryClass() {
            assertThatNullPointerException()
                    .isThrownBy(() -> producer.get(null, template));
        }

        @Test
        @DisplayName("Should reject a null template")
        void shouldRejectNullTemplate() {
            assertThatNullPointerException()
                    .isThrownBy(() -> producer.get(PersonRepository.class, (KeyValueTemplate) null));
        }

        @Test
        @DisplayName("Should reject a null manager")
        void shouldRejectNullManager() {
            assertThatNullPointerException()
                    .isThrownBy(() -> producer.get(PersonRepository.class, (BucketManager) null));
        }
    }

    @Nested
    @DisplayName("When creating a repository")
    class WhenTheCreation {

        @BeforeEach
        void setUpMetadata() {
            when(repositoriesMetadata.get(PersonRepository.class)).thenReturn(Optional.of(repositoryMetadata));
            when(repositoryMetadata.entity()).thenReturn(Optional.of(Person.class));
            when(entities.get(Person.class)).thenReturn(entityMetadata);
            doReturn(interceptionFactory).when(beanManager)
                    .createInterceptionFactory(creationalContext, PersonRepository.class);
        }

        @Test
        @DisplayName("Should return the CDI-intercepted repository")
        void shouldReturnInterceptedRepository() {
            PersonRepository expected = mock(PersonRepository.class);
            when(interceptionFactory.createInterceptedInstance(any())).thenReturn(expected);

            PersonRepository result = producer.get(PersonRepository.class, template, creationalContext);

            ArgumentCaptor<PersonRepository> repositoryCaptor = ArgumentCaptor.forClass(PersonRepository.class);
            verify(interceptionFactory).createInterceptedInstance(repositoryCaptor.capture());
            assertThat(Proxy.getInvocationHandler(repositoryCaptor.getValue()))
                    .isInstanceOf(CoreRepositoryInvocationHandler.class);
            assertThat(result).isSameAs(expected);
        }

        @Test
        @DisplayName("Should create the template from the manager")
        void shouldCreateTemplateFromManager() {
            BucketManager manager = mock(BucketManager.class);
            PersonRepository expected = mock(PersonRepository.class);
            when(templateProducer.apply(manager)).thenReturn(template);
            doReturn(creationalContext).when(beanManager).createCreationalContext(null);
            when(interceptionFactory.createInterceptedInstance(any())).thenReturn(expected);

            PersonRepository result = producer.get(PersonRepository.class, manager);

            verify(templateProducer).apply(manager);
            assertThat(result).isSameAs(expected);
        }
    }
}
