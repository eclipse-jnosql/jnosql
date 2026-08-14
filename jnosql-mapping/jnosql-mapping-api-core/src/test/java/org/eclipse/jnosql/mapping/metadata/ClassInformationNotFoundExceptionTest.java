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

class ClassInformationNotFoundExceptionTest {

    @Nested
    @DisplayName("When the exception is created")
    class WhenTheExceptionIsCreated {

        @Test
        @DisplayName("Should preserve the provided message")
        void shouldPreserveTheProvidedMessage() {
            String errorMessage = "Class information not found.";
            ClassInformationNotFoundException exception = new ClassInformationNotFoundException(errorMessage);

            assertThat(exception.getMessage()).isEqualTo(errorMessage);
        }
    }
}