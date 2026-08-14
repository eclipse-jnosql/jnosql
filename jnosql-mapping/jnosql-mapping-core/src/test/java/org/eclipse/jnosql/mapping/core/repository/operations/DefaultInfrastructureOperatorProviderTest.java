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
package org.eclipse.jnosql.mapping.core.repository.operations;

import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultInfrastructureOperatorProviderTest {



    @Nested
    @DisplayName("When the default infrastructure operator provider operates")
    class WhenTheDefaultInfrastructureOperatorProviderOperates {

        @DisplayName("Should have default constructor")
        @Test
        void shouldHaveDefaultConstructor() {
            var provider = new DefaultInfrastructureOperatorProvider();
            assertThat(provider).isNotNull();
        }
    }
}
