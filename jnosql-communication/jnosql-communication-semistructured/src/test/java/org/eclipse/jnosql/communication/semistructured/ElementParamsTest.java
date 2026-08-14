/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;

import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;


class ElementParamsTest {


    @Nested
    @DisplayName("When the element params is used")
    class WhenTheElementParamsIsUsed {

        @DisplayName("Should Set Parameter")
        @Test
        void shouldSetParameter() {
            Params params = Params.newParams();
            Value name = params.add("name");
            Element element = Element.of("name", name);
            params.bind("name", "Ada Lovelace");

            assertThat(element.get()).isEqualTo("Ada Lovelace");

            params.bind("name", "Diana");
            assertThat(element.get()).isEqualTo("Diana");
        }
    }

}
