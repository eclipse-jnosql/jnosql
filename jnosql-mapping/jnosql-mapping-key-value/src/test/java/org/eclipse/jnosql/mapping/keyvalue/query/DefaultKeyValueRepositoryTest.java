/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplate;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class DefaultKeyValueRepositoryTest {

    @Mock
    private KeyValueTemplate template;
    @Mock
    private EntityMetadata metadata;
    @Mock
    private LifecycleEventHandler lifecycleEventHandler;

    @Nested
    @DisplayName("When the repository is created")
    class WhenTheRepositoryIsCreated {

        @Test
        @DisplayName("Should return error when template is null")
        void shouldReturnErrorWhenTemplateIsNull() {
            assertThatThrownBy(() -> DefaultKeyValueRepository.of(template, null, lifecycleEventHandler)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> DefaultKeyValueRepository.of(null, metadata, lifecycleEventHandler)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> DefaultKeyValueRepository.of(null, null, lifecycleEventHandler)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> DefaultKeyValueRepository.of(template, metadata, null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should create repository")
        void shouldCreateRepository() {
            DefaultKeyValueRepository<Object, Object> repository = DefaultKeyValueRepository.of(template, metadata, lifecycleEventHandler);
            assertThat(repository).isNotNull();
        }
    }

}
