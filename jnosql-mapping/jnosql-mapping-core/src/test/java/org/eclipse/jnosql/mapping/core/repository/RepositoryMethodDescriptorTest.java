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

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethodType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(descriptor.type())
                    .isEqualTo(RepositoryMethodType.FIND_BY);
            softly.assertThat(descriptor.method())
                    .isSameAs(mockMethod);
        });
    }

    @Test
    void shouldAllowNullMethod() {
        RepositoryMethodDescriptor descriptor =
                new RepositoryMethodDescriptor(RepositoryMethodType.DELETE_BY, null);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(descriptor.type()).isEqualTo(RepositoryMethodType.DELETE_BY);
            softly.assertThat(descriptor.method()).isNull();
        });

    }

    @Test
    void shouldThrowExceptionWhenTypeIsNull() {
        assertThatThrownBy(() -> new RepositoryMethodDescriptor(null, mockMethod))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("type is required");
    }
}