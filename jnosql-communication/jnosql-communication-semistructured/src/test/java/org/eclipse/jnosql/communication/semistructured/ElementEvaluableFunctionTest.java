/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.semistructured;

import org.eclipse.jnosql.communication.TypeSupplier;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.function.EvaluableFunction;
import org.eclipse.jnosql.communication.function.FunctionArgument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Isolated communication-level check that a driver-style consumer reads an evaluable function through
 * {@link Value}. It does not prove JDQL parsing or database execution.
 */
@DisplayName("An element carrying an evaluable function value")
class ElementEvaluableFunctionTest {

    private static final class CountingValue implements Value {
        private final AtomicInteger resolutions = new AtomicInteger();

        @Override
        public Object get() {
            resolutions.incrementAndGet();
            return "otavio";
        }

        @Override
        public <T> T get(Class<T> type) {
            return type.cast(get());
        }

        @Override
        public <T> T get(TypeSupplier<T> supplier) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isInstanceOf(Class<?> type) {
            return type.isInstance(get());
        }

        @Override
        public boolean isNull() {
            return get() == null;
        }
    }

    @Test
    @DisplayName("exposes the evaluated value without the consumer knowing the function")
    void shouldExposeEvaluatedValue() {
        CountingValue source = new CountingValue();
        EvaluableFunction upper = EvaluableFunction.of("UPPER",
                FunctionArgument.function(EvaluableFunction.of("LEFT",
                        new FunctionArgument.ValueArgument(source), FunctionArgument.literal(19))));

        Element element = Element.of("name", upper);
        Value value = element.value();

        assertThat(source.resolutions).hasValue(0);
        assertThat(value.isNull()).isFalse();
        assertThat(value.isInstanceOf(String.class)).isTrue();
        assertThat(value.get()).isEqualTo("OTAVIO");
        assertThat(value.get(String.class)).isEqualTo("OTAVIO");
        assertThat(element.get()).isEqualTo("OTAVIO");
        assertThat(source.resolutions).hasValue(1);
    }

    @Test
    @DisplayName("exposes a memoized null result as a null value")
    void shouldExposeNull() {
        Element element = Element.of("name", EvaluableFunction.of("UPPER", FunctionArgument.literal(null)));
        assertThat(element.value().isNull()).isTrue();
        assertThat(element.get()).isNull();
    }
}
