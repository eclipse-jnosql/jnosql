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
import jakarta.data.repository.DataRepository;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RepositoryBeanTest {

    private RepositoryBean<MockRepository> repositoryBean;
    private RepositoryBean<MockRepository> repositoryBeanDefault;
    private final String provider = "testProvider";

    @BeforeEach
    void setUp() {
        repositoryBean = new RepositoryBean<>(MockRepository.class, provider, DatabaseType.DOCUMENT) {
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

        doReturn(mock(EntitiesMetadata.class)).when(repositoryBean).getInstance(EntitiesMetadata.class);
        doReturn(mock(SemiStructuredTemplate.class)).when(repositoryBean).getInstance(eq(SemiStructuredTemplate.class), any());
        doReturn(mock(Converters.class)).when(repositoryBean).getInstance(Converters.class);

        MockRepository createdInstance = repositoryBean.create(context);
        assertThat(createdInstance).isNotNull();
        assertThat(Proxy.isProxyClass(createdInstance.getClass())).isTrue();
    }

    @Test
    void shouldReturnCorrectQualifiers() {
        Set<?> qualifiers = repositoryBean.getQualifiers();
        assertThat(qualifiers).isNotEmpty();
    }

    @Test
    void shouldGetId() {
        SoftAssertions.assertSoftly(soft -> {;
            soft.assertThat(repositoryBean.getId()).isNotNull();
            soft.assertThat(repositoryBeanDefault.getId()).isNotNull();
        });
    }

    @Test
    void shouldGetTypes() {
        SoftAssertions.assertSoftly(soft -> {;
            soft.assertThat(repositoryBean.getTypes()).isNotNull();
            soft.assertThat(repositoryBeanDefault.getTypes()).isNotNull();
        });
    }

    interface MockRepository extends DataRepository<MockRepository, String> {}
}