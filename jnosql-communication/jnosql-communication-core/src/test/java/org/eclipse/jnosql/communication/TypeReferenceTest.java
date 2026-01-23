/*
 *
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
 *
 */
package org.eclipse.jnosql.communication;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class TypeReferenceTest {
    @Test
    @DisplayName("Should capture generic type information")
    void shouldCaptureGenericType() {
        TypeReference<String> reference = new TypeReference<>() {};

        Type type = reference.get();

        assertThat(type)
                .isEqualTo(String.class);
    }

    @Test
    @DisplayName("Should capture parameterized generic type information")
    void shouldCaptureParameterizedGenericType() {
        TypeReference<List<String>> reference = new TypeReference<>() {};

        Type type = reference.get();

        assertThat(type)
                .isInstanceOf(java.lang.reflect.ParameterizedType.class);

        assertThat(type.getTypeName())
                .isEqualTo("java.util.List<java.lang.String>");
    }

    @Test
    @DisplayName("Should capture complex nested generic type information")
    void shouldCaptureNestedGenericType() {
        TypeReference<Map<String, List<Integer>>> reference =
                new TypeReference<>() {};

        Type type = reference.get();

        assertThat(type.getTypeName())
                .isEqualTo("java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>");
    }

    @Test
    @DisplayName("Should throw exception when constructed without generic type information")
    void shouldFailWhenConstructedWithoutTypeInformation() {
        assertThatThrownBy(() -> new RawTypeReference())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Internal error: TypeReference constructed without actual type information"
                );
    }

    @Test
    @DisplayName("Should include type information in toString output")
    void shouldIncludeTypeInToString() {
        TypeReference<Integer> reference = new TypeReference<>() {};

        assertThat(reference.toString())
                .contains("TypeReference")
                .contains("java.lang.Integer");
    }

    /**
     * Helper class to simulate incorrect usage with raw type.
     */
    @SuppressWarnings("rawtypes")
    static class RawTypeReference extends TypeReference {
    }
}