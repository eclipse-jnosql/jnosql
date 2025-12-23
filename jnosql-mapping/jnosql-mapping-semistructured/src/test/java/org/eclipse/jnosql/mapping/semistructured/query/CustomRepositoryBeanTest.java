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
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredRepositoryProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class CustomRepositoryBeanTest {
    private static final String PROVIDER = "testProvider";

    private CustomRepositoryBean<MockRepository> repositoryBean;
    private CustomRepositoryBean<?> defaultRepositoryBean;

    @BeforeEach
    void setUp() {
        repositoryBean = new CustomRepositoryBean<>(MockRepository.class, PROVIDER, DatabaseType.GRAPH) {
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


        SemiStructuredTemplate mockTemplate = mock(SemiStructuredTemplate.class);
        SemistructuredRepositoryProducer producer = mock(SemistructuredRepositoryProducer.class);
        Mockito.when(producer.get(eq(BaseRepositoryBeanTest.MockRepository.class), Mockito.any()))
                .thenReturn(Mockito.mock(BaseRepositoryBeanTest.MockRepository.class));

        doReturn(producer).when(repositoryBean).getInstance(eq(SemistructuredRepositoryProducer.class), any());
        doReturn(producer).when(repositoryBean).getInstance(eq(SemistructuredRepositoryProducer.class));
        doReturn(mockTemplate).when(repositoryBean).getInstance(eq(SemiStructuredTemplate.class), any());

        repositoryBean.create(context);
    }

    interface MockRepository {}
}