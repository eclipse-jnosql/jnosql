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
package org.eclipse.jnosql.mapping.metadata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ConstructorBuilderTest {

    @Nested
    @DisplayName("When the constructor builder is requested")
    class WhenTheConstructorBuilderIsRequested {

        @Test
        @DisplayName("Should return a constructor builder for metadata")
        void shouldReturnAConstructorBuilderForMetadata() {
            ConstructorMetadata constructorMetadata = mock(ConstructorMetadata.class);
            ConstructorBuilder builder = ConstructorBuilder.of(constructorMetadata);

            assertThat(builder).isNotNull();
        }
    }

}