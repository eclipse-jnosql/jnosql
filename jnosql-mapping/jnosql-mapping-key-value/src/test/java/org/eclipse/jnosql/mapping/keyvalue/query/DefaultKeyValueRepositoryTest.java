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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
class DefaultKeyValueRepositoryTest {

    @Mock
    private KeyValueTemplate template;
    @Mock
    private EntityMetadata metadata;

    @Test
    void shouldReturnErrorWhenTemplateIsNull() {
        assertThrows(NullPointerException.class, () -> DefaultKeyValueRepository.of(template, null));
        assertThrows(NullPointerException.class, () -> DefaultKeyValueRepository.of(null, metadata));
        assertThrows(NullPointerException.class, () -> DefaultKeyValueRepository.of(null, null));
    }

    @Test
    void shouldCreateRepository() {
        DefaultKeyValueRepository<Object, Object> repository = DefaultKeyValueRepository.of(template, metadata);
        assertNotNull(repository);
    }
}