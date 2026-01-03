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
package org.eclipse.jnosql.communication.reader;

import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.TypeReferenceReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ListTypeReferenceReaderTest {

    private final TypeReferenceReader referenceReader = new ListTypeReferenceReader();
    private final String firstValue = "123";
    private final String secondValue = "123";

    @DisplayName("Should be compatible")
    @ParameterizedTest(name = "is compatible: {0}")
    @MethodSource("compatibleTypeReferences")
    void shouldBeCompatible(TypeReference<?> type) {
        assertThat(referenceReader.test(type)).isTrue();
    }

    @DisplayName("Should be incompatible")
    @ParameterizedTest(name = "is incompatible: {0}")
    @MethodSource("incompatibleTypeReferences")
    void shouldNotBeCompatible(TypeReference<?> type) {
        assertThat(referenceReader.test(type)).isFalse();
    }

    @Test
    @DisplayName("Should be able to convert the value to Integer")
    void shouldConvert() {
        assertSoftly(softly -> {
            softly.assertThat(referenceReader.convert(new TypeReference<List<String>>() {
            }, firstValue)).as("TypeReference<List<String>> conversion").isEqualTo(singletonList(firstValue));

            softly.assertThat(referenceReader.convert(new TypeReference<List<Long>>() {
            }, Long.valueOf(firstValue))).as("TypeReference<List<Long>> conversion").isEqualTo(singletonList(Long.valueOf(firstValue)));

            softly.assertThat(referenceReader.convert(new TypeReference<Collection<String>>() {
            }, firstValue)).as("TypeReference<Collection<String>> conversion").isEqualTo(singletonList(firstValue));

            softly.assertThat(referenceReader.convert(new TypeReference<Collection<Long>>() {
            }, firstValue)).as("TypeReference<Collection<Long>> conversion").isEqualTo(singletonList(Long.valueOf(firstValue)));

            softly.assertThat(referenceReader.convert(new TypeReference<Iterable<String>>() {
            }, firstValue)).as("TypeReference<Iterable<String>> conversion").isEqualTo(singletonList(firstValue));

            softly.assertThat(referenceReader.convert(new TypeReference<Iterable<Long>>() {
            }, firstValue)).as("TypeReference<Iterable<Long>> conversion").isEqualTo(singletonList(Long.valueOf(firstValue)));
        });
    }

    @Test
    @DisplayName("Should be able to convert and be mutable using String as value")
    void shouldConvertAndBeMutable() {
        List<String> strings = referenceReader.convert(new TypeReference<>() {
        }, firstValue);

        strings.add(secondValue);

        assertThat(strings).hasSize(2).contains(firstValue, secondValue);
    }

    @Test
    @DisplayName("Should be able to convert and be mutable using Arrays as value")
    void shouldConvertAndBeMutable2() {
        List<String> strings = referenceReader.convert(new TypeReference<>() {
        }, Arrays.asList(firstValue, secondValue));

        strings.add("32");

        assertThat(strings).hasSize(3).contains(firstValue, secondValue, "32");
    }

    static Stream<Arguments> compatibleTypeReferences() {
        return Stream.of(
                arguments(new TypeReference<List<String>>() {
                }),
                arguments(new TypeReference<List<Long>>() {
                }),
                arguments(new TypeReference<Collection<String>>() {
                }),
                arguments(new TypeReference<Iterable<String>>() {
                }),
                arguments(new TypeReference<SequencedCollection<String>>() {
                }),
                arguments(new TypeReference<Iterable<Long>>() {
                })
        );
    }

    static Stream<Arguments> incompatibleTypeReferences() {
        return Stream.of(
                arguments(new TypeReference<ArrayList<BigDecimal>>() {
                }),
                arguments(new TypeReference<String>() {
                }),
                arguments(new TypeReference<Set<String>>() {
                }),
                arguments(new TypeReference<List<List<String>>>() {
                }),
                arguments(new TypeReference<Queue<String>>() {
                }),
                arguments(new TypeReference<Queue<String>>() {
                }),
                arguments(new TypeReference<Map<Integer, String>>() {
                })
        );
    }
}
