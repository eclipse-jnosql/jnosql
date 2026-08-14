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
 *   Elias Nogueira
 *
 */
package org.eclipse.jnosql.communication.reader;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.ValueReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

class ArrayReaderTest {

    private final ValueReader valueReader = new ArrayReader();

    @Nested
    @DisplayName("When the supported type is checked")
    class WhenTheSupportedTypeIsChecked {

        @Test
        @DisplayName("Should accept array types and reject non-array types")
        void shouldIsValid() {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(valueReader.test(Integer.class)).isFalse();
                softly.assertThat(valueReader.test(String.class)).isFalse();
                softly.assertThat(valueReader.test(Object.class)).isFalse();

                softly.assertThat(valueReader.test(Object[].class)).isTrue();
                softly.assertThat(valueReader.test(byte[].class)).isTrue();
                softly.assertThat(valueReader.test(String[].class)).isTrue();
            });
        }
    }

    @Nested
    @DisplayName("When the value is converted")
    class WhenTheValueIsConverted {

        @Test
        @DisplayName("Should convert a list to an array")
        void shouldConvertListToArray() {
            List<Integer> elements = List.of(97, 98, 99, 100);
            byte[] bytes = valueReader.read(byte[].class, elements);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(bytes).as("Should be able to convert List to byte[]").isNotNull();
                softly.assertThat(bytes.length).as("Should be able to convert List to byte[]").isEqualTo(elements.size());
                softly.assertThat(bytes).isNotNull().isEqualTo(new byte[]{97, 98, 99, 100});
            });
        }

        @Test
        @DisplayName("Should return an array value that already matches the requested type")
        void shouldConvertToTheSameInstance() {
            var data = new byte[]{'a', 'b', 'c', 'd'};
            byte[] bytes = valueReader.read(byte[].class, data);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(bytes).as("Should be able to convert byte[] to byte[]").isNotNull();
                softly.assertThat(bytes.length).as("Should be able to convert byte[] to byte[]").isEqualTo(data.length);
                softly.assertThat(bytes).isNotNull().isEqualTo(new byte[]{97, 98, 99, 100});
            });
        }

        @Test
        @DisplayName("Should convert one array type to another array type")
        void shouldConvertArrayToArray() {
            var elements = new int[]{1, 2, 3, 4};
            byte[] bytes = valueReader.read(byte[].class, elements);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(bytes).as("Should be able to convert int[] to byte[]").isNotNull();
                softly.assertThat(bytes.length).as("Should be able to convert int[] to byte[]").isEqualTo(elements.length);
                softly.assertThat(bytes).isNotNull().isEqualTo(new byte[]{1, 2, 3, 4});
            });
        }

        @Test
        @DisplayName("Should convert a single element to a one element array")
        void shouldConvertSingleElement() {
            var elements = 1;
            byte[] bytes = valueReader.read(byte[].class, elements);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(bytes).as("Should be able to convert single value to byte[]").isNotNull();
                softly.assertThat(bytes.length).as("Should be able to convert single value to byte[]").isEqualTo(1);
                softly.assertThat(bytes).isNotNull().isEqualTo(new byte[]{1});
            });
        }
    }
}
