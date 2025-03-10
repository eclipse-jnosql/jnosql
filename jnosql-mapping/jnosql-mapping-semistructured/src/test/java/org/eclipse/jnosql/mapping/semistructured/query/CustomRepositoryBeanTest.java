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
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class CustomRepositoryBeanTest {

    private CustomRepositoryBean<MockRepository> repositoryBean;
    private CustomRepositoryBean<?> defaultRepositoryBean;
    private final String provider = "testProvider";

    @BeforeEach
    void setUp() {
        repositoryBean = new CustomRepositoryBean<>(MockRepository.class, provider, DatabaseType.GRAPH) {
            @Override
            protected Class<? extends SemiStructuredTemplate> getTemplateClass() {
                return SemiStructuredTemplate.class;
            }
        };

        defaultRepositoryBean = new CustomRepositoryBean<>(MockRepository.class, "", DatabaseType.GRAPH) {
            @Override
            protected Class<? extends SemiStructuredTemplate> getTemplateClass() {
                return SemiStructuredTemplate.class;
            }
        };
    }

    @Test
    void shouldReturnBeanClass() {
        assertThat(repositoryBean.getBeanClass()).isEqualTo(MockRepository.class);
    }

    @Test
    void shouldReturnCorrectQualifiers() {
        Set<?> qualifiers = repositoryBean.getQualifiers();
        assertThat(qualifiers).isNotEmpty();
    }

    @Test
    void shouldGetId() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(repositoryBean.getId()).isNotNull();
            softly.assertThat(defaultRepositoryBean.getId()).isNotEmpty();
        });
    }

    @Test
    void shouldGetTypes() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(repositoryBean.getTypes()).isNotEmpty();
            softly.assertThat(repositoryBean.getTypes()).hasSize(1);

            softly.assertThat(defaultRepositoryBean.getTypes()).isNotEmpty();
            softly.assertThat(defaultRepositoryBean.getTypes()).hasSize(1);
        });
    }

    @Test
    void shouldCreateProxyInstance() {
        @SuppressWarnings("unchecked")
        CreationalContext<MockRepository> context = mock(CreationalContext.class);

        repositoryBean = spy(repositoryBean);

        EntitiesMetadata mockEntitiesMetadata = mock(EntitiesMetadata.class);
        SemiStructuredTemplate mockTemplate = mock(SemiStructuredTemplate.class);
        Converters mockConverters = mock(Converters.class);

        doReturn(mockEntitiesMetadata).when(repositoryBean).getInstance(EntitiesMetadata.class);
        doReturn(mockTemplate).when(repositoryBean).getInstance(eq(SemiStructuredTemplate.class), any());
        doReturn(mockConverters).when(repositoryBean).getInstance(Converters.class);

        MockRepository createdInstance = repositoryBean.create(context);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(createdInstance).isNotNull();
            softly.assertThat(createdInstance).isInstanceOf(MockRepository.class);
            softly.assertThat(java.lang.reflect.Proxy.isProxyClass(createdInstance.getClass())).isTrue();
        });
    }

    interface MockRepository {}
}