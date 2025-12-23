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
package org.eclipse.jnosql.mapping.semistructured.query;

import jakarta.enterprise.context.spi.CreationalContext;
import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredRepositoryProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Unit tests for {@link BaseRepositoryBean}.
 */
class BaseRepositoryBeanTest {
    private static final String PROVIDER = "testProvider";
    private BaseRepositoryBean<MockRepository> repositoryBean;

    @BeforeEach
    void setUp() {
        repositoryBean = new MockBaseRepositoryBean(MockRepository.class, PROVIDER, DatabaseType.GRAPH);
    }

    @Test
    void shouldReturnBeanClass() {
        assertThat(repositoryBean.getBeanClass()).isEqualTo(MockRepository.class);
    }

    @Test
    void shouldCreateProxyInstance() {
        CreationalContext<MockRepository> context = mock(CreationalContext.class);
        BaseRepositoryBean<MockRepository> spyBean = spy(repositoryBean);
        SemistructuredRepositoryProducer producer = mock(SemistructuredRepositoryProducer.class);
        Mockito.when(producer.get(eq(MockRepository.class), Mockito.any())).thenReturn(Mockito.mock(MockRepository.class));

        doReturn(mock(SemiStructuredTemplate.class)).when(spyBean).getInstance(eq(SemiStructuredTemplate.class), any());
        doReturn(mock(EntitiesMetadata.class)).when(spyBean).getInstance(EntitiesMetadata.class);
        doReturn(mock(Converters.class)).when(spyBean).getInstance(Converters.class);
        doReturn(producer).when(spyBean).getInstance(SemistructuredRepositoryProducer.class);

        MockRepository proxyInstance = spyBean.create(context);

        assertNotNull(proxyInstance);
    }

    @Test
    void shouldReturnCorrectQualifiers() {
        Set<Annotation> qualifiers = repositoryBean.getQualifiers();
        assertThat(qualifiers).isNotEmpty();
    }

    @Test
    void shouldReturnCorrectId() {
        String id = repositoryBean.getId();
        Assertions.assertThat(id).isNotNull();
    }

    @Test
    void shouldReturnCorrectTypes() {
        Set<Type> types = repositoryBean.getTypes();
        assertThat(types).isNotEmpty();
        assertThat(types).contains(MockRepository.class);
    }

    private static class MockBaseRepositoryBean extends BaseRepositoryBean<MockRepository> {

        protected MockBaseRepositoryBean(Class<?> type, String provider, DatabaseType databaseType) {
            super(type, provider, databaseType);
        }

        @Override
        protected Class<? extends SemiStructuredTemplate> getTemplateClass() {
            return SemiStructuredTemplate.class;
        }

    }

    interface MockRepository {}
}
