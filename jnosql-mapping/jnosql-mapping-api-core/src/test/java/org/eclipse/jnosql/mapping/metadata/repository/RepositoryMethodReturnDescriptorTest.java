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
package org.eclipse.jnosql.mapping.metadata.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class RepositoryMethodReturnDescriptorTest {

    @Nested
    @DisplayName("When the metadata does not describe nested types")
    class WhenTheDefaultDescriptor {

        @Test
        @DisplayName("Should describe only the raw return type")
        void shouldDescribeRawType() {
            var method = mock(RepositoryMethod.class, CALLS_REAL_METHODS);
            doReturn(Optional.of(List.class)).when(method).returnType();

            assertSoftly(softly -> softly.assertThat(method.returnDescriptor())
                    .contains(ReturnType.of(List.class)));
        }

        @Test
        @DisplayName("Should be empty when the return type is unknown")
        void shouldBeEmptyWithoutReturnType() {
            var method = mock(RepositoryMethod.class, CALLS_REAL_METHODS);
            doReturn(Optional.empty()).when(method).returnType();

            assertSoftly(softly -> softly.assertThat(method.returnDescriptor()).isEmpty());
        }
    }
}
