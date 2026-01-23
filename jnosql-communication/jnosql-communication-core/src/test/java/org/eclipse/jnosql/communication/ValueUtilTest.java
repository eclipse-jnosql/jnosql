/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.communication;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValueUtilTest {

    @Test
    @DisplayName("Should convert a scalar Value to its raw object")
    void shouldConvertScalarValue() {
        Value value = Value.of(10);
        assertEquals(10, ValueUtil.convert(value));
    }

    @Test
    @DisplayName("Should convert an iterable Value preserving its elements")
    void shouldConvertIterableValue() {
        Value value = Value.of(Arrays.asList(10, 20));
        assertEquals(Arrays.asList(10, 20), ValueUtil.convert(value));
    }

    @Test
    @DisplayName("Should unwrap nested Value elements inside an iterable")
    void shouldConvertIterableContainingNestedValues() {
        Value value = Value.of(Arrays.asList(Value.of(10), Value.of(20)));
        assertEquals(Arrays.asList(10, 20), ValueUtil.convert(value));
    }

    @Test
    @DisplayName("Should convert a scalar Value into a singleton list")
    void shouldConvertScalarValueToSingletonList() {
        Value value = Value.of(10);
        assertEquals(Collections.singletonList(10), ValueUtil.convertToList(value));
    }

    @Test
    @DisplayName("Should convert an iterable Value directly into a list")
    void shouldConvertIterableValueToList() {
        Value value = Value.of(Arrays.asList(10, 20));
        assertEquals(Arrays.asList(10, 20), ValueUtil.convertToList(value));
    }

    @Test
    @DisplayName("Should unwrap nested Value elements when converting to list")
    void shouldConvertIterableWithNestedValuesToList() {
        Value value = Value.of(Arrays.asList(Value.of(10), Value.of(20)));
        assertEquals(Arrays.asList(10, 20), ValueUtil.convertToList(value));
    }

    @Test
    @DisplayName("Should return null when Value wraps a null reference")
    void shouldReturnNullWhenWrappedValueIsNull() {
        Value value = Value.of(null);
        assertNull(ValueUtil.convert(value));
    }

    @Test
    @DisplayName("Should return null when converting Value.ofNull()")
    void shouldReturnNullForExplicitNullValue() {
        Object result = ValueUtil.convert(Value.ofNull());
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert using a custom ValueWriter")
    void shouldConvertUsingCustomWriter() {
        ValueWriter<Integer, String> writer = integerToCustomStringWriter();
        Value value = Value.of(42);

        assertEquals("Custom-42", ValueUtil.convert(value, writer));
    }

    @Test
    @DisplayName("Should convert iterable using a custom ValueWriter")
    void shouldConvertIterableUsingCustomWriter() {
        ValueWriter<Integer, String> writer = integerToCustomStringWriter();
        Value value = Value.of(Arrays.asList(10, 20));

        assertEquals(Arrays.asList("Custom-10", "Custom-20"),
                ValueUtil.convertToList(value, writer));
    }

    @Test
    @DisplayName("Should convert nested Values using a custom ValueWriter")
    void shouldConvertNestedValuesUsingCustomWriter() {
        ValueWriter<Integer, String> writer = integerToCustomStringWriter();
        Value value = Value.of(Arrays.asList(Value.of(10), Value.of(20)));

        assertEquals(Arrays.asList("Custom-10", "Custom-20"),
                ValueUtil.convert(value, writer));
    }

    @Test
    @DisplayName("Should convert nested Values to list using a custom ValueWriter")
    void shouldConvertNestedValuesToListUsingCustomWriter() {
        ValueWriter<Integer, String> writer = integerToCustomStringWriter();
        Value value = Value.of(Arrays.asList(Value.of(10), Value.of(20)));

        assertEquals(Arrays.asList("Custom-10", "Custom-20"),
                ValueUtil.convertToList(value, writer));
    }

    @Test
    @DisplayName("Should throw NullPointerException when converting a null Value")
    void shouldFailWhenValueIsNullOnConvert() {
        NullPointerException ex =
                assertThrows(NullPointerException.class, () -> ValueUtil.convert(null));

        assertEquals("value is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw NullPointerException when ValueWriter is null on convert")
    void shouldFailWhenWriterIsNullOnConvert() {
        Value value = Value.of(1);
        NullPointerException ex =
                assertThrows(NullPointerException.class, () -> ValueUtil.convert(value, null));

        assertEquals("valueWriter is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw NullPointerException when converting a null Value to list")
    void shouldFailWhenValueIsNullOnConvertToList() {
        NullPointerException ex =
                assertThrows(NullPointerException.class, () -> ValueUtil.convertToList(null));

        assertEquals("value is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw NullPointerException when ValueWriter is null on convertToList")
    void shouldFailWhenWriterIsNullOnConvertToList() {
        Value value = Value.of(1);
        NullPointerException ex =
                assertThrows(NullPointerException.class, () -> ValueUtil.convertToList(value, null));

        assertEquals("valueWriter is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should unwrap nested scalar Value")
    void shouldConvertNestedScalarValue() {
        Value nested = Value.of(Value.of(10));
        assertEquals(10, ValueUtil.convert(nested));
    }

    @Test
    @DisplayName("Should preserve null elements inside iterables")
    void shouldPreserveNullElementsInIterable() {
        Value value = Value.of(Arrays.asList(10, null, 20));

        assertEquals(Arrays.asList(10, null, 20), ValueUtil.convert(value));
        assertEquals(Arrays.asList(10, null, 20), ValueUtil.convertToList(value));
    }

    @Test
    @DisplayName("Should keep original value when ValueWriter does not support its type")
    void shouldKeepOriginalWhenWriterDoesNotSupportType() {
        ValueWriter<Integer, String> writer = integerToCustomStringWriter();
        Value value = Value.of("hello");

        assertEquals("hello", ValueUtil.convert(value, writer));
        assertEquals(Collections.singletonList("hello"),
                ValueUtil.convertToList(value, writer));
    }

    @Test
    @DisplayName("Should convert scalar value to singleton list using custom writer")
    void shouldConvertScalarToSingletonListUsingCustomWriter() {
        ValueWriter<Integer, String> writer = integerToCustomStringWriter();
        Value value = Value.of(7);

        assertEquals(Collections.singletonList("Custom-7"),
                ValueUtil.convertToList(value, writer));
    }

    @Test
    @DisplayName("Should convert nested scalar using custom writer")
    void shouldConvertNestedScalarUsingCustomWriter() {
        ValueWriter<Integer, String> writer = integerToCustomStringWriter();
        Value nested = Value.of(Value.of(9));

        assertEquals("Custom-9", ValueUtil.convert(nested, writer));
    }

    @Test
    @DisplayName("Should convert mixed iterable preserving non-supported types and nulls")
    void shouldConvertMixedIterableUsingCustomWriter() {
        ValueWriter<Integer, String> writer = integerToCustomStringWriter();
        Value value = Value.of(Arrays.asList(1, "x", null, Value.of(2)));

        List<Object> expected = Arrays.asList("Custom-1", "x", null, "Custom-2");

        assertEquals(expected, ValueUtil.convert(value, writer));
        assertEquals(expected, ValueUtil.convertToList(value, writer));
    }

    @Test
    @DisplayName("Should extract value from ParamValue wrapper")
    void shouldResolveParamValue() {
        ValueWriter<Integer, String> writer = integerToCustomStringWriter();

        ParamValue param = new ParamValue("test");
        param.setValue(234);

        Value value = Value.of(List.of(param));

        assertEquals(List.of("Custom-234"),
                ValueUtil.convertToList(value, writer));
    }

    @Test
    @DisplayName("Should extract iterable value from ParamValue wrapper")
    void shouldResolveParamValueContainingIterable() {
        ValueWriter<Integer, String> writer = integerToCustomStringWriter();

        ParamValue param = new ParamValue("test");
        param.setValue(List.of(234));

        Value value = Value.of(List.of(param));

        assertEquals(List.of("Custom-234"),
                ValueUtil.convertToList(value, writer));
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException when converting a ReferenceToken")
    void shouldFailWhenConvertingReferenceToken() {
        Value value = Value.of(new ReferenceToken("field"));

        assertThatThrownBy(() -> ValueUtil.convert(value))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("By default, ReferenceToken is not supported");
    }

    private static ValueWriter<Integer, String> integerToCustomStringWriter() {
        return new ValueWriter<>() {
            @Override
            public String write(Integer object) {
                return "Custom-" + object;
            }

            @Override
            public boolean test(Class<?> aClass) {
                return Integer.class.equals(aClass);
            }
        };
    }
}
