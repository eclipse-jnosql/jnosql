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

import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class RepositoryInvocationContextTest {

    @Mock
    private RepositoryMethod repositoryMethod;

    @Mock
    private RepositoryMetadata repositoryMetadata;

    @Mock
    private EntityMetadata entityMetadata;


    @Test
    void shouldNotAllowNullRepositoryMethod() {
        assertThatThrownBy(() ->
                new RepositoryInvocationContext(null, repositoryMetadata, entityMetadata, new Object[]{})
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("method is required");
    }

    @Test
    void shouldNotAllowNullMetadata() {
        assertThatThrownBy(() ->
                new RepositoryInvocationContext(repositoryMethod, null,entityMetadata,  new Object[]{})
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("metadata is required");
    }

    @Test
    void shouldNotAllowNullParametersArray() {
        assertThatThrownBy(() ->
                new RepositoryInvocationContext(repositoryMethod, repositoryMetadata, entityMetadata, null)
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("parameters is required");
    }

    @Test
    void shouldNotAllowNullEntityMetadata() {
        assertThatThrownBy(() ->
                new RepositoryInvocationContext(repositoryMethod, repositoryMetadata, null, new Object[]{})
        ).isInstanceOf(NullPointerException.class);
    }
}