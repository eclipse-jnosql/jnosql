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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class ValueUtilTest {

    @Test
    void shouldConvert() {
        Value value = Value.of(10);
        assertEquals(10, ValueUtil.convert(value));
    }

    @Test
    void shouldConvert2() {
        Value value = Value.of(Arrays.asList(10, 20));
        assertEquals(Arrays.asList(10, 20), ValueUtil.convert(value));
    }

    @Test
    void shouldConvert3() {
        Value value = Value.of(Arrays.asList(Value.of(10), Value.of(20)));
        assertEquals(Arrays.asList(10, 20), ValueUtil.convert(value));
    }

    @Test
    void shouldConvertList() {
        Value value = Value.of(10);
        assertEquals(Collections.singletonList(10), ValueUtil.convertToList(value));
    }

    @Test
    void shouldConvertList2() {
        Value value = Value.of(Arrays.asList(10, 20));
        assertEquals(Arrays.asList(10, 20), ValueUtil.convertToList(value));
    }


    @Test
    void shouldConvertList3() {
        Value value = Value.of(Arrays.asList(Value.of(10), Value.of(20)));
        assertEquals(Arrays.asList(10, 20), ValueUtil.convertToList(value));
    }

    @Test
    void shouldConvertNull() {
        Value value = Value.of(null);
        assertNull(ValueUtil.convert(value));
    }

    @Test
    void shouldReturnNullWhenValueUtilHasNullValue() {
        Object result = ValueUtil.convert(Value.ofNull());
        Assertions.assertNull(result);
    }

    @Test
    void shouldConvertWithCustomWriter() {
        ValueWriter<Integer, String> customWriter = new ValueWriter<>() {
            @Override
            public String write(Integer object) {
                return "Custom-" + object;
            }

            @Override
            public boolean test(Class<?> aClass) {
                return Integer.class.equals(aClass);
            }
        };

        Value value = Value.of(42);
        assertEquals("Custom-42", ValueUtil.convert(value, customWriter));
    }

    @Test
    void shouldConvertListWithCustomWriter() {
        ValueWriter<Integer, String> customWriter = new ValueWriter<>() {
            @Override
            public String write(Integer object) {
                return "Custom-" + object;
            }

            @Override
            public boolean test(Class<?> aClass) {
                return Integer.class.equals(aClass);
            }
        };

        Value value = Value.of(Arrays.asList(10, 20));
        assertEquals(Arrays.asList("Custom-10", "Custom-20"), ValueUtil.convertToList(value, customWriter));
    }

    @Test
    void shouldConvertNestedValueWithCustomWriter() {
        ValueWriter<Integer, String> customWriter = new ValueWriter<>() {
            @Override
            public String write(Integer object) {
                return "Custom-" + object;
            }

            @Override
            public boolean test(Class<?> aClass) {
                return Integer.class.equals(aClass);
            }
        };

        Value value = Value.of(Arrays.asList(Value.of(10), Value.of(20)));
        assertEquals(Arrays.asList("Custom-10", "Custom-20"), ValueUtil.convert(value, customWriter));
    }

    @Test
    void shouldConvertNestedListWithCustomWriter() {
        ValueWriter<Integer, String> customWriter = new ValueWriter<>() {
            @Override
            public String write(Integer object) {
                return "Custom-" + object;
            }

            @Override
            public boolean test(Class<?> aClass) {
                return Integer.class.equals(aClass);
            }
        };

        Value value = Value.of(Arrays.asList(Value.of(10), Value.of(20)));
        assertEquals(Arrays.asList("Custom-10", "Custom-20"), ValueUtil.convertToList(value, customWriter));
    }

    @Test
    void shouldThrowNPEWhenValueIsNullOnConvert() {
        NullPointerException ex =
                Assertions.assertThrows(NullPointerException.class, () -> ValueUtil.convert(null));
        assertEquals("value is required", ex.getMessage());
    }

    @Test
    void shouldThrowNPEWhenWriterIsNullOnConvert() {
        Value value = Value.of(1);
        NullPointerException ex =
                Assertions.assertThrows(NullPointerException.class, () -> ValueUtil.convert(value, null));
        assertEquals("valueWriter is required", ex.getMessage());
    }

    @Test
    void shouldThrowNPEWhenValueIsNullOnConvertToList() {
        NullPointerException ex =
                Assertions.assertThrows(NullPointerException.class, () -> ValueUtil.convertToList(null));
        assertEquals("value is required", ex.getMessage());
    }

    @Test
    void shouldThrowNPEWhenWriterIsNullOnConvertToList() {
        Value value = Value.of(1);
        NullPointerException ex =
                Assertions.assertThrows(NullPointerException.class, () -> ValueUtil.convertToList(value, null));
        assertEquals("valueWriter is required", ex.getMessage());
    }

    @Test
    void shouldConvertNestedScalarValue() {
        Value nested = Value.of(Value.of(10));
        assertEquals(10, ValueUtil.convert(nested));
    }

    @Test
    void shouldHandleIterableContainingNullElements() {
        Value value = Value.of(Arrays.asList(10, null, 20));
        assertEquals(Arrays.asList(10, null, 20), ValueUtil.convert(value));
        assertEquals(Arrays.asList(10, null, 20), ValueUtil.convertToList(value));
    }

    @Test
    void shouldKeepOriginalWhenWriterDoesNotSupportType() {
        ValueWriter<Integer, String> writer = new ValueWriter<>() {
            @Override public String write(Integer object) { return "Custom-" + object; }
            @Override public boolean test(Class<?> aClass) { return Integer.class.equals(aClass); }
        };
        Value stringValue = Value.of("hello");
        assertEquals("hello", ValueUtil.convert(stringValue, writer));
        assertEquals(Collections.singletonList("hello"), ValueUtil.convertToList(stringValue, writer));
    }

    @Test
    void shouldConvertScalarToSingletonListWithCustomWriter() {
        ValueWriter<Integer, String> writer = new ValueWriter<>() {
            @Override public String write(Integer object) { return "Custom-" + object; }
            @Override public boolean test(Class<?> aClass) { return Integer.class.equals(aClass); }
        };
        Value value = Value.of(7);
        assertEquals(Collections.singletonList("Custom-7"), ValueUtil.convertToList(value, writer));
    }

    @Test
    void shouldConvertNestedScalarWithCustomWriter() {
        ValueWriter<Integer, String> writer = new ValueWriter<>() {
            @Override public String write(Integer object) { return "Custom-" + object; }
            @Override public boolean test(Class<?> aClass) { return Integer.class.equals(aClass); }
        };
        Value nested = Value.of(Value.of(9));
        assertEquals("Custom-9", ValueUtil.convert(nested, writer));
    }

    @Test
    void shouldConvertMixedIterableWithCustomWriterPreservingOthersAndNulls() {
        ValueWriter<Integer, String> writer = new ValueWriter<>() {
            @Override public String write(Integer object) { return "Custom-" + object; }
            @Override public boolean test(Class<?> aClass) { return Integer.class.equals(aClass); }
        };
        Value value = Value.of(Arrays.asList(1, "x", null, Value.of(2)));
        assertEquals(Arrays.asList("Custom-1", "x", null, "Custom-2"),
                ValueUtil.convert(value, writer));
        assertEquals(Arrays.asList("Custom-1", "x", null, "Custom-2"),
                ValueUtil.convertToList(value, writer));
    }

    @Test
    void shouldGetParamValue() {
        ValueWriter<Integer, String> writer = new ValueWriter<>() {
            @Override public String write(Integer object) { return "Custom-" + object; }
            @Override public boolean test(Class<?> aClass) { return Integer.class.equals(aClass); }
        };
        ParamValue paramValue = new ParamValue("test");
        paramValue.setValue(234);
        Value value = Value.of(List.of(paramValue));
        assertEquals(Arrays.asList("Custom-234"),
                ValueUtil.convertToList(value, writer));
    }

    @Test
    void shouldGetParamListValue() {
        ValueWriter<Integer, String> writer = new ValueWriter<>() {
            @Override public String write(Integer object) { return "Custom-" + object; }
            @Override public boolean test(Class<?> aClass) { return Integer.class.equals(aClass); }
        };
        ParamValue paramValue = new ParamValue("test");
        paramValue.setValue(List.of(234));
        Value value = Value.of(List.of(paramValue));
        assertEquals(Arrays.asList("Custom-234"),
                ValueUtil.convertToList(value, writer));
    }
}
