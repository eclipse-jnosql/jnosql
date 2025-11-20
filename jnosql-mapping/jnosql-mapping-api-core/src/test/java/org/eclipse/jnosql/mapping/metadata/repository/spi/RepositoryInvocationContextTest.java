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
package org.eclipse.jnosql.mapping.metadata.repository.spi;

import org.eclipse.jnosql.mapping.metadata.repository.MethodKey;
import org.eclipse.jnosql.mapping.metadata.repository.NameKey;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class RepositoryInvocationContextTest {

    private MethodKey methodKey = new NameKey("methodName");

    @Mock
    private RepositoryMethod repositoryMethod;

    @Mock
    private RepositoryMetadata repositoryMetadata;

    @Test
    void shouldNotAllowNullMethodKey() {
        assertThatThrownBy(() ->
                new RepositoryInvocationContext(null, repositoryMethod, repositoryMetadata, new Object[]{})
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("methodKey is required");
    }

    @Test
    void shouldNotAllowNullRepositoryMethod() {
        assertThatThrownBy(() ->
                new RepositoryInvocationContext(methodKey, null, repositoryMetadata, new Object[]{})
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("method is required");
    }

    @Test
    void shouldNotAllowNullMetadata() {
        assertThatThrownBy(() ->
                new RepositoryInvocationContext(methodKey, repositoryMethod, null, new Object[]{})
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("metadata is required");
    }

    @Test
    void shouldNotAllowNullParametersArray() {
        assertThatThrownBy(() ->
                new RepositoryInvocationContext(methodKey, repositoryMethod, repositoryMetadata, null)
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("parameters is required");
    }
}