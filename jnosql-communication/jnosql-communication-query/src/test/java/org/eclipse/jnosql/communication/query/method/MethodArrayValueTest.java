/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.method;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.query.ArrayQueryValue;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.ValueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MethodArrayValueTest {

    @Nested
    @DisplayName("When the method array value is used")
    class WhenTheMethodArrayValue {
    }

    @Test
    @DisplayName("Should Return Array Type")
    void shouldReturnArrayType() {
        ArrayQueryValue array = MethodArrayValue.of("method");
        assertThat(array).isNotNull();
        ValueType type = array.type();
        assertThat(type).isEqualTo(ValueType.ARRAY);
    }

    @Test
    @DisplayName("Should Return Array Value")
    void shouldReturnArrayValue() {
        ArrayQueryValue array = MethodArrayValue.of("name");
        assertThat(array.get()).isInstanceOf(QueryValue[].class);
    }

    @Test
    @DisplayName("Should Equals")
    void shouldEquals(){
        var array = MethodArrayValue.of("name");
        var array2 = MethodArrayValue.of("name");
        var array3 = MethodArrayValue.of("name2");
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(array).isEqualTo(array);
            soft.assertThat(array).isNotEqualTo(array2);
            soft.assertThat(array).isNotEqualTo(array3);
            soft.assertThat(array).isNotEqualTo(null);
            soft.assertThat(array).isNotEqualTo("array2");
        });
    }

    @Test
    @DisplayName("Should To String")
    void shouldToString(){
        ArrayQueryValue array = MethodArrayValue.of("name");
        org.assertj.core.api.Assertions.assertThat(array.toString())
                .isNotNull()
                .isNotBlank()
                .contains("name");
    }

    @Test
    @DisplayName("Should Hash Code")
    void shouldHashCode(){
        ArrayQueryValue array = MethodArrayValue.of("name");
        assertThat(array).hasSameHashCodeAs(array);
    }

    @Test
    @DisplayName("Should Get")
    void shouldGet() {
        ArrayQueryValue array = MethodArrayValue.of("name");
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(array.get()).isNotNull();
            soft.assertThat(array.get()).hasSize(2);
        });
    }

}