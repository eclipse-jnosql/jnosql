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

import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.communication.query.ValueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MethodParamQueryValueTest {

    @Nested
    @DisplayName("When the method param query value is used")
    class WhenTheMethodParamQueryValue {
    }

    @Test
    @DisplayName("Should Return Type")
    void shouldReturnType() {
        MethodParamQueryValue param = new MethodParamQueryValue("name");
        Assertions.assertThat(param).isNotNull()
                .extracting(MethodParamQueryValue::type)
                .isNotNull().isEqualTo(ValueType.PARAMETER);
    }
    @Test
    @DisplayName("Should Create Instance")
    void shouldCreateInstance() {
        MethodParamQueryValue param = new MethodParamQueryValue("name");
        Assertions.assertThat(param).isNotNull()
                .extracting(MethodParamQueryValue::get)
                .isNotNull();

    }

    @Test
    @DisplayName("Should Equals")
    void shouldEquals() {
        MethodParamQueryValue param = new MethodParamQueryValue("name");
        assertThat(param).isEqualTo(param);
    }

    @Test
    @DisplayName("Should Hash Code")
    void shouldHashCode() {
        MethodParamQueryValue param = new MethodParamQueryValue("name");
        assertThat(param).hasSameHashCodeAs(param);
    }

    @Test
    @DisplayName("Should Equality")
    void shouldEquality() {
        String value = "testValue";
        MethodParamQueryValue queryValue1 = new MethodParamQueryValue(value);
        assertThat(queryValue1).isEqualTo(queryValue1);
    }

    @Test
    @DisplayName("Should Inequality")
    void shouldInequality() {
        MethodParamQueryValue queryValue1 = new MethodParamQueryValue("value1");
        MethodParamQueryValue queryValue2 = new MethodParamQueryValue("value2");

        // Should have inequality
        assertThat(queryValue2).isNotEqualTo(queryValue1);
    }

    @Test
    @DisplayName("Should Consistent Hashcode")
    void shouldConsistentHashcode() {
        String value = "testValue";
        MethodParamQueryValue queryValue1 = new MethodParamQueryValue(value);
        Assertions.assertThat(queryValue1.hashCode()).isEqualTo(queryValue1.hashCode());
    }

    @Test
    @DisplayName("Should To String Representation")
    void shouldToStringRepresentation() {
        String value = "testValue";
        MethodParamQueryValue queryValue = new MethodParamQueryValue(value);

        Assertions.assertThat(queryValue.toString()).startsWith("@" + value);
    }

    @Test
    @DisplayName("Should Value With Prefix")
    void shouldValueWithPrefix() {
        MethodParamQueryValue queryValue = new MethodParamQueryValue("test");

        // Should generate a value with the original prefix and nano time
        assertThat(queryValue.get()).startsWith("test_");
    }
}