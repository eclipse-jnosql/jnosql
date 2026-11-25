/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NullQueryValueTest {

    @Nested
    @DisplayName("When the null query value is used")
    class WhenTheNullQueryValue {
    }


    @Test
    @DisplayName("Should Return Null Query Value")
    void shouldReturnNullQueryValue() {
        QueryValue<NullQueryValue> nullQueryValue = NullQueryValue.INSTANCE.get();

        SoftAssertions.assertSoftly(a -> {
            a.assertThat(nullQueryValue).isNotNull();
            a.assertThat(nullQueryValue.type()).isEqualTo(ValueType.NULL);
        });
    }
}