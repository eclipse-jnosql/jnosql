/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;


import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.junit.jupiter.api.Test;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ElementTest {


    @Nested
    @DisplayName("When the element is used")
    class WhenTheElementIsUsed {

        private static final Value DEFAULT_VALUE = Value.of(12);

        @DisplayName("Should Return Name When Name Is Null")
        @Test
        void shouldReturnNameWhenNameIsNull() {
            assertThatThrownBy(() -> Element.of(null, DEFAULT_VALUE)).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Return Name When Value Is Null")
        @Test
        void shouldReturnNameWhenValueIsNull() {
            Element element = Element.of("Name", null);
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(element.name()).isEqualTo("Name");
                softly.assertThat(element.value().isNull()).isTrue();
            });
        }

        @DisplayName("Should Create An Column Instance")
        @Test
        void shouldCreateAnColumnInstance() {
            String name = "name";
            Element element = Element.of(name, DEFAULT_VALUE);
            assertThat(element).isNotNull();
            assertThat(element.name()).isEqualTo(name);
            assertThat(element.value()).isEqualTo(DEFAULT_VALUE);
        }

        @DisplayName("Should Be Equals")
        @Test
        void shouldBeEquals() {
            assertThat(Element.of("name", DEFAULT_VALUE)).isEqualTo(Element.of("name", DEFAULT_VALUE));
        }

        @DisplayName("Should Return Get Object")
        @Test
        void shouldReturnGetObject() {
            Value value = Value.of("text");
            Element element = Element.of("name", value);
            assertThat(element.get()).isEqualTo(value.get());
        }

        @DisplayName("Should Return Get Class")
        @Test
        void shouldReturnGetClass() {
            Value value = Value.of("text");
            Element element = Element.of("name", value);
            assertThat(element.get(String.class)).isEqualTo(value.get(String.class));
        }


        @DisplayName("Should Return Get Type")
        @Test
        void shouldReturnGetType() {
            Value value = Value.of("text");
            Element element = Element.of("name", value);
            TypeReference<List<String>> typeReference = new TypeReference<>() {
            };
            assertThat(element.get(typeReference)).isEqualTo(value.get(typeReference));
        }
    }

}
