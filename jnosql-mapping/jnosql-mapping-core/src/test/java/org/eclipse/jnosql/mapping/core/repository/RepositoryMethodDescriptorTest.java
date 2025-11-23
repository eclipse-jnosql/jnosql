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
package org.eclipse.jnosql.mapping.core.repository;

import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethodType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class RepositoryMethodDescriptorTest {

    @Mock
    private RepositoryMethod mockMethod;

    @Mock
    private RepositoryMethod anotherMockMethod;

    @Test
    void shouldCreateDescriptorWithMandatoryTypeAndNonNullMethod() {
        RepositoryMethodDescriptor descriptor =
                new RepositoryMethodDescriptor(RepositoryMethodType.FIND_BY, mockMethod);

        assertThat(descriptor.type())
                .isEqualTo(RepositoryMethodType.FIND_BY);

        assertThat(descriptor.method())
                .isSameAs(mockMethod);
    }

    @Test
    void shouldAllowNullMethod() {
        RepositoryMethodDescriptor descriptor =
                new RepositoryMethodDescriptor(RepositoryMethodType.DELETE_BY, null);

        assertThat(descriptor.type())
                .isEqualTo(RepositoryMethodType.DELETE_BY);

        assertThat(descriptor.method())
                .isNull();
    }

    @Test
    void shouldThrowExceptionWhenTypeIsNull() {
        assertThatThrownBy(() -> new RepositoryMethodDescriptor(null, mockMethod))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("type is required");
    }

    @Test
    void shouldFollowRecordEqualitySemantics() {
        RepositoryMethodDescriptor d1 =
                new RepositoryMethodDescriptor(RepositoryMethodType.QUERY, mockMethod);

        RepositoryMethodDescriptor d2 =
                new RepositoryMethodDescriptor(RepositoryMethodType.QUERY, mockMethod);

        RepositoryMethodDescriptor d3 =
                new RepositoryMethodDescriptor(RepositoryMethodType.QUERY, anotherMockMethod);

        assertThat(d1).isEqualTo(d2);
        assertThat(d1).isNotEqualTo(d3);
    }

    @Test
    void shouldHaveValidToString() {
        RepositoryMethodDescriptor descriptor =
                new RepositoryMethodDescriptor(RepositoryMethodType.SAVE, mockMethod);

        assertThat(descriptor.toString())
                .contains("type=SAVE")
                .contains("method=");
    }
}