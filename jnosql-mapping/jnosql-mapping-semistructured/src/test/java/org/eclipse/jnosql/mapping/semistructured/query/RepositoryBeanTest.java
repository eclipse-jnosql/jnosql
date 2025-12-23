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

import jakarta.data.repository.DataRepository;
import jakarta.enterprise.context.spi.CreationalContext;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredRepositoryProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class RepositoryBeanTest {

    private static final String PROVIDER = "testProvider";
    private RepositoryBean<MockRepository> repositoryBean;
    private RepositoryBean<MockRepository> repositoryBeanDefault;

    @BeforeEach
    void setUp() {
        repositoryBean = new RepositoryBean<>(MockRepository.class, PROVIDER, DatabaseType.DOCUMENT) {
            @Override
            protected Class<? extends SemiStructuredTemplate> getTemplateClass() {
                return SemiStructuredTemplate.class;
            }
        };

        repositoryBeanDefault = new RepositoryBean<>(MockRepository.class, "", DatabaseType.DOCUMENT) {
            @Override
            protected Class<? extends SemiStructuredTemplate> getTemplateClass() {
                return SemiStructuredTemplate.class;
            }
        };

    }

    @Test
    void shouldReturnBeanClass() {
        assertThat(repositoryBean.getBeanClass()).isEqualTo(MockRepository.class);
        assertThat(repositoryBeanDefault.getBeanClass()).isEqualTo(MockRepository.class);
    }

    @Test
    void shouldCreateProxyInstance() {
        CreationalContext<MockRepository> context = mock(CreationalContext.class);
        repositoryBean = spy(repositoryBean);
        SemistructuredRepositoryProducer producer = mock(SemistructuredRepositoryProducer.class);
        doReturn(mock(EntitiesMetadata.class)).when(repositoryBean).getInstance(EntitiesMetadata.class);
        doReturn(mock(SemiStructuredTemplate.class)).when(repositoryBean).getInstance(eq(SemiStructuredTemplate.class), any());
        doReturn(producer).when(repositoryBean).getInstance(eq(SemistructuredRepositoryProducer.class), any());
        doReturn(producer).when(repositoryBean).getInstance(eq(SemistructuredRepositoryProducer.class));

        repositoryBean.create(context);

    }

    @Test
    void shouldReturnCorrectQualifiers() {
        Set<?> qualifiers = repositoryBean.getQualifiers();
        assertThat(qualifiers).isNotEmpty();
    }

    @Test
    void shouldGetId() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(repositoryBean.getId()).isNotNull();
            soft.assertThat(repositoryBeanDefault.getId()).isNotNull();
        });
    }

    @Test
    void shouldGetTypes() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(repositoryBean.getTypes()).isNotNull();
            soft.assertThat(repositoryBeanDefault.getTypes()).isNotNull();
        });
    }

    interface MockRepository extends DataRepository<MockRepository, String> {}
}