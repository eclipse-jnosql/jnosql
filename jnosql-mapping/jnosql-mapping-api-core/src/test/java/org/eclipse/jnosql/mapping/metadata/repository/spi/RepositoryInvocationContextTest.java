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

import jakarta.nosql.Template;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Mock
    private Template template;


    @Nested
    @DisplayName("When the invocation context is created")
    class WhenTheInvocationContextIsCreated {

        @Test
        @DisplayName("Should reject a null repository method")
        void shouldRejectANullRepositoryMethod() {
            assertThatThrownBy(() ->
                    new RepositoryInvocationContext(null, repositoryMetadata, entityMetadata, template, new Object[]{})
            ).isInstanceOf(NullPointerException.class)
                    .hasMessage("method is required");
        }

        @Test
        @DisplayName("Should reject null repository metadata")
        void shouldRejectNullRepositoryMetadata() {
            assertThatThrownBy(() ->
                    new RepositoryInvocationContext(repositoryMethod, null, entityMetadata, template, new Object[]{})
            ).isInstanceOf(NullPointerException.class)
                    .hasMessage("metadata is required");
        }

        @Test
        @DisplayName("Should reject a null parameters array")
        void shouldRejectANullParametersArray() {
            assertThatThrownBy(() ->
                    new RepositoryInvocationContext(repositoryMethod, repositoryMetadata, entityMetadata, template, null)
            ).isInstanceOf(NullPointerException.class)
                    .hasMessage("parameters is required");
        }

        @Test
        @DisplayName("Should reject null entity metadata")
        void shouldRejectNullEntityMetadata() {
            assertThatThrownBy(() ->
                    new RepositoryInvocationContext(repositoryMethod, repositoryMetadata, null, template, new Object[]{})
            ).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should reject a null template")
        void shouldRejectANullTemplate() {
            assertThatThrownBy(() ->
                    new RepositoryInvocationContext(repositoryMethod, repositoryMetadata, entityMetadata, null,
                            new Object[]{})
            ).isInstanceOf(NullPointerException.class);
        }
    }
}