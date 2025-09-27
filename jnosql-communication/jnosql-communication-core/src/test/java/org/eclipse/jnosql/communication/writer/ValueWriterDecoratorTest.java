/*
 *
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
 *   Elias Nogueira
 *
 */
package org.eclipse.jnosql.communication.writer;

import org.eclipse.jnosql.communication.ValueWriter;
import org.eclipse.jnosql.communication.ValueWriterDecorator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueWriterDecoratorTest {

    @SuppressWarnings("rawtypes")
    private final ValueWriter valueWriter = ValueWriterDecorator.getInstance();

    @ParameterizedTest(name = "must be compatible to {0}")
    @DisplayName("Should be able to verify the test compatibility")
    @ValueSource(classes = {Enum.class, Optional.class, Temporal.class})
    @SuppressWarnings("unchecked")
    void shouldVerifyCompatibility(Class<?> supportedClass) {
        assertThat(valueWriter.test(supportedClass)).isTrue();
    }

    @Test
    @DisplayName("Should be able to check the test object incompatibility")
    @SuppressWarnings("unchecked")
    void shouldVerifyIncompatibility() {
        assertThat(valueWriter.test(Boolean.class)).isFalse();
    }


    @Test
    @DisplayName("Should be able to convert the result")
    @SuppressWarnings("unchecked")
    void shouldConvert() {
        String diana = "diana";
        Optional<String> optional = Optional.of("diana");

        Object result = valueWriter.write(optional);
        assertThat(diana).isEqualTo(result);
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException when type is not supported")
    @SuppressWarnings("unchecked")
    void shouldThrowUnsupportedOperationExceptionWhenTypeIsNotSupported() {
        assertThatThrownBy(() -> valueWriter.write(Collections.EMPTY_LIST)).isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("The type class java.util.Collections$EmptyList is not supported yet");
    }

    @Test
    void shouldTestToString() {
        assertThat(valueWriter.toString()).isNotNull().isNotEmpty();
    }
}
