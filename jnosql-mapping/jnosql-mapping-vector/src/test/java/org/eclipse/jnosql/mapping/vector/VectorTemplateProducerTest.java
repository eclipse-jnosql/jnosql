/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.vector;

import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.vector.entities.Article;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages({Converters.class, EntityConverter.class, Reflections.class, VectorTemplate.class})
@AddExtensions(ReflectionEntityMetadataExtension.class)
@DisplayName("Vector template producer")
class VectorTemplateProducerTest {

    @Inject
    private VectorTemplateProducer producer;

    @Nested
    @DisplayName("When creating an application-managed template")
    class WhenTheCreation {

        @Test
        @DisplayName("Should require a database manager")
        void shouldRejectNullManager() {
            assertThatNullPointerException().isThrownBy(() -> producer.apply(null)).withMessage("manager is required");
        }

        @Test
        @DisplayName("Should use the supplied manager without taking ownership of it")
        void shouldUseSuppliedManager() {
            DatabaseManager manager = mock(DatabaseManager.class);
            when(manager.insert(any(CommunicationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
            Article article = new Article("1", "content", DenseVector.of(1F), new float[]{2F});

            VectorTemplate template = producer.apply(manager);
            Article result = template.insert(article);

            assertThat(result.getFeatures()).as("persisted vector").isEqualTo(article.getFeatures());
            verify(manager).insert(any(CommunicationEntity.class));
            verify(manager, never()).close();
        }
    }
}
