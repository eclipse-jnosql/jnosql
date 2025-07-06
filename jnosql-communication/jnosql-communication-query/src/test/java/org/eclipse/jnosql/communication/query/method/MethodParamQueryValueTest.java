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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MethodParamQueryValueTest {

    @Test
    void shouldReturnType() {
        MethodParamQueryValue param = new MethodParamQueryValue("name");
        Assertions.assertThat(param).isNotNull()
                .extracting(MethodParamQueryValue::type)
                .isNotNull().isEqualTo(ValueType.PARAMETER);
    }
    @Test
    void shouldCreateInstance() {
        MethodParamQueryValue param = new MethodParamQueryValue("name");
        Assertions.assertThat(param).isNotNull()
                .extracting(MethodParamQueryValue::get)
                .isNotNull();

    }

    @Test
    void shouldEquals() {
        MethodParamQueryValue param = new MethodParamQueryValue("name");
        assertEquals(param, param);
    }

    @Test
    void shouldHashCode() {
        MethodParamQueryValue param = new MethodParamQueryValue("name");
        assertEquals(param.hashCode(), param.hashCode());
    }

    @Test
    void shouldEquality() {
        String value = "testValue";
        MethodParamQueryValue queryValue1 = new MethodParamQueryValue(value);
        assertEquals(queryValue1, queryValue1);
    }

    @Test
    void shouldInequality() {
        MethodParamQueryValue queryValue1 = new MethodParamQueryValue("value1");
        MethodParamQueryValue queryValue2 = new MethodParamQueryValue("value2");

        // Should have inequality
        assertNotEquals(queryValue1, queryValue2);
    }

    @Test
    void shouldConsistentHashcode() {
        String value = "testValue";
        MethodParamQueryValue queryValue1 = new MethodParamQueryValue(value);
        Assertions.assertThat(queryValue1.hashCode()).isEqualTo(queryValue1.hashCode());
    }

    @Test
    void shouldToStringRepresentation() {
        String value = "testValue";
        MethodParamQueryValue queryValue = new MethodParamQueryValue(value);

        Assertions.assertThat(queryValue.toString()).startsWith("@" + value);
    }

    @Test
    void shouldValueWithPrefix() {
        MethodParamQueryValue queryValue = new MethodParamQueryValue("test");

        // Should generate a value with the original prefix and nano time
        assertTrue(queryValue.get().startsWith("test_"));
    }
}