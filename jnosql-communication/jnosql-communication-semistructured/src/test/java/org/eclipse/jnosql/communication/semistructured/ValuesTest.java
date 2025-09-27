/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
import org.eclipse.jnosql.communication.query.ArrayQueryValue;
import org.eclipse.jnosql.communication.query.EnumQueryValue;
import org.eclipse.jnosql.communication.query.ParamQueryValue;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.ValueType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


class ValuesTest {

    private enum SampleEnum { FIRST, SECOND }


    @Test
    void shouldReturnNumberAsIs() {
        @SuppressWarnings("unchecked")
        QueryValue<Integer> value = Mockito.mock(QueryValue.class);
        Mockito.when(value.type()).thenReturn(ValueType.NUMBER);
        Mockito. when(value.get()).thenReturn(42);

        Object result = Values.get(value, Mockito.mock(Params.class));

        assertThat(result).isEqualTo(42);
    }


    @Test
    void shouldReturnStringAsIs() {
        @SuppressWarnings("unchecked")
        QueryValue<String> value = mock(QueryValue.class);
        when(value.type()).thenReturn(ValueType.STRING);
        when(value.get()).thenReturn("jnosql");

        Object result = Values.get(value, mock(Params.class));

        assertThat(result).isEqualTo("jnosql");
    }

    @Test
    void shouldReturnBooleanAsIs() {
        @SuppressWarnings("unchecked")
        QueryValue<Boolean> value = mock(QueryValue.class);
        when(value.type()).thenReturn(ValueType.BOOLEAN);
        when(value.get()).thenReturn(Boolean.TRUE);

        Object result = Values.get(value, mock(Params.class));

        assertThat(result).isEqualTo(true);
    }

    @Test
    void shouldResolveParameterViaParamsAdd() {
        Params params = mock(Params.class);

        ParamQueryValue paramValue = mock(ParamQueryValue.class);
        when(paramValue.type()).thenReturn(ValueType.PARAMETER);
        when(paramValue.get()).thenReturn("age");
        when(params.add("age")).thenReturn(Value.of(99));

        Object result = Values.get(paramValue, params);

        assertThat(result).isEqualTo(Value.of(99));
        verify(params).add("age");
    }

    @Test
    void shouldResolveArrayRecursivelyIncludingParamsAndNulls() {
        Params params = mock(Params.class);

        @SuppressWarnings("unchecked")
        QueryValue<Integer> num = mock(QueryValue.class);
        when(num.type()).thenReturn(ValueType.NUMBER);
        when(num.get()).thenReturn(7);

        ParamQueryValue param = mock(ParamQueryValue.class);
        when(param.type()).thenReturn(ValueType.PARAMETER);
        when(param.get()).thenReturn("p1");
        when(params.add("p1")).thenReturn(Value.of("added-p1"));

        @SuppressWarnings("unchecked")
        QueryValue<Object> nullVal = mock(QueryValue.class);
        when(nullVal.type()).thenReturn(ValueType.NULL);

        ArrayQueryValue array = () -> new QueryValue[]{ num, param, nullVal};

        Object result = Values.get(array, params);

        assertThat(result)
                .isInstanceOf(List.class)
                .asList()
                .contains(7, Value.of("added-p1"), null);

        verify(params).add("p1");
    }

    @Test
    void shouldMapEnumToItsName() {

        Object result = Values.get(new EnumQueryValue(SampleEnum.SECOND), mock(Params.class));

        assertThat(result).isEqualTo(SampleEnum.SECOND);
    }

    @Test
    void shouldReturnNullForNullType() {
        @SuppressWarnings("unchecked")
        QueryValue<Object> nullValue = mock(QueryValue.class);
        when(nullValue.type()).thenReturn(ValueType.NULL);

        Object result = Values.get(nullValue, mock(Params.class));

        assertThat(result).isNull();
    }

}