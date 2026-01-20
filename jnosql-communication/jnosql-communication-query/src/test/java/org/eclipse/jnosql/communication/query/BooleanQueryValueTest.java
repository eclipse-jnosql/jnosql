/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BooleanQueryValue")
class BooleanQueryValueTest {

    @Test
    @DisplayName("should expose true as a Boolean query value")
    void shouldExposeTrueValue() {
        BooleanQueryValue queryValue = BooleanQueryValue.TRUE;

        assertThat(queryValue.get())
                .isTrue();
    }

    @Test
    @DisplayName("should expose false as a Boolean query value")
    void shouldExposeFalseValue() {
        BooleanQueryValue queryValue = BooleanQueryValue.FALSE;

        assertThat(queryValue.get())
                .isFalse();
    }

    @Test
    @DisplayName("should report BOOLEAN as its ValueType")
    void shouldExposeBooleanValueType() {
        BooleanQueryValue queryValue = BooleanQueryValue.TRUE;

        assertThat(queryValue.type())
                .isEqualTo(ValueType.BOOLEAN);
    }

    @Test
    @DisplayName("should implement the QueryValue contract")
    void shouldImplementQueryValueContract() {
        BooleanQueryValue queryValue = BooleanQueryValue.FALSE;

        assertThat(queryValue)
                .isInstanceOf(QueryValue.class);
    }

    @Test
    @DisplayName("static TRUE instance should be equivalent to a true value")
    void staticTrueInstanceShouldBeEquivalent() {
        BooleanQueryValue fromConstant = BooleanQueryValue.TRUE;
        BooleanQueryValue fromConstructor = new BooleanQueryValue(true);

        assertThat(fromConstant)
                .isEqualTo(fromConstructor);
    }

    @Test
    @DisplayName("static FALSE instance should be equivalent to a false value")
    void staticFalseInstanceShouldBeEquivalent() {
        BooleanQueryValue fromConstant = BooleanQueryValue.FALSE;
        BooleanQueryValue fromConstructor = new BooleanQueryValue(false);

        assertThat(fromConstant)
                .isEqualTo(fromConstructor);
    }
}