/*
 *
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
 *   Maximillian Arruda
 *   Elias Nogueira
 */
package org.eclipse.jnosql.communication.keyvalue;

import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Key-value entity parameters")
class KeyValueEntityParamsTest {

    @Nested
    @DisplayName("When binding a parameter value")
    class WhenTheParameterBinding {

        @Test
        @DisplayName("Should update the entity value when the parameter changes")
        void shouldUpdateEntityValueWhenParameterChanges() {

            // Given
            Params params = Params.newParams();
            Value name = params.add("name");
            KeyValueEntity entity = KeyValueEntity.of("name", name);

            // When / Then
            params.bind("name", "Ada Lovelace");
            assertThat(entity.value()).isEqualTo("Ada Lovelace");

            params.bind("name", "Diana");
            assertThat(entity.value()).isEqualTo("Diana");
        }
    }
}
