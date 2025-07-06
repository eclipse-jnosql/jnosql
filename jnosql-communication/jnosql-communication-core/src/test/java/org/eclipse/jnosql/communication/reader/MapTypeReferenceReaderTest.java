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

import org.eclipse.jnosql.communication.Entry;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.TypeReferenceReader;
import org.eclipse.jnosql.communication.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class MapTypeReferenceReaderTest {

    private final TypeReferenceReader referenceReader = new MapTypeReferenceReader();

    @DisplayName("Should be compatible")
    @ParameterizedTest(name = "compatible {0}")
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
    @DisplayName("Should be able to convert the value to Map")
    void shouldConvert() {
        assertSoftly(softly -> {
            softly.assertThat(referenceReader.convert(new TypeReference<Map<String, String>>() {
                    }, singletonMap(123, 123L))).as("TypeReference<Map<String, String>> conversion")
                    .isEqualTo(new HashMap<>(singletonMap("123", "123")));

            softly.assertThat(referenceReader.convert(new TypeReference<Map<Long, Integer>>() {
                    }, singletonMap(123, 123L))).as("TypeReference<Map<Long, Integer>> conversion")
                    .isEqualTo(new HashMap<>(singletonMap(123L, 123)));
        });
    }

    @Test
    @DisplayName("Should create mutable Map")
    void shouldCreateMutableMap() {
        Map<String, String> map = referenceReader.convert(new TypeReference<>() {
        }, singletonMap(123, 123L));

        map.put("23", "123");

        assertThat(map).hasSize(2).containsOnly(entry("123", "123"), entry("23", "123"));
    }

    @Test
    @DisplayName("Should create mutable between Maps")
    void shouldMutableMap2() {
        Map<Integer, Long> oldMap = new HashMap<>();
        oldMap.put(1, 234L);
        oldMap.put(2, 2345L);

        Map<String, String> map = referenceReader.convert(new TypeReference<>() {
        }, oldMap);

        map.put("23", "123");

        assertThat(map).hasSize(3).containsOnly(entry("1", "234"), entry("2", "2345"), entry("23", "123"));
    }

    @Test
    @DisplayName("Should create list of Maps")
    void shouldMergeListMap() {
        List<Map<Integer, Long>> maps = new ArrayList<>();
        maps.add(Map.of(1, 234L, 2, 2345L));
        maps.add(Map.of(3, 234564L, 4, 3452L));

        Map<String, String> map = referenceReader.convert(new TypeReference<>() {
        }, maps);


        assertThat(map).hasSize(4).containsOnly(entry("1", "234"),
                entry("2", "2345"), entry("3", "234564"), entry("4", "3452"));
    }


    @Test
    @DisplayName("Should convert Entry to Map")
    void shouldConvertEntryToMap() {
        Entry entry = new EntryTest("key", Value.of("value"));

        Map<String, String> map = referenceReader.convert(new TypeReference<>() {
        }, Collections.singletonList(entry));

        assertThat(map).hasSize(1).contains(entry("key", "value"));
    }

    @Test
    @DisplayName("Should convert list of entry to Map")
    void shouldConvertEntriesToMap() {
        List<Entry> entries = List.of(new EntryTest("key", Value.of("value")),
                new EntryTest("key2", Value.of("value")));
        Map<String, String> map = referenceReader.convert(new TypeReference<>() {
        }, entries);

        assertThat(map).hasSize(2).contains(entry("key", "value"), entry("key2", "value"));
    }

    @Test
    @DisplayName("Should convert SubEntry to Map")
    void shouldConvertSubEntryToMap() {
        Entry subEntry = new EntryTest("key", Value.of("value"));
        Entry entry = new EntryTest("key", Value.of(subEntry));

        Map<String, Map<String, String>> map = referenceReader.convert(new TypeReference<>() {
        }, Collections.singletonList(entry));

        Map<String, String> subMap = map.get("key");

        assertSoftly(softly -> {
            softly.assertThat(map).as("Map is correctly converted").hasSize(1).contains(entry("key", Map.of("key", "value")));
            softly.assertThat(subMap).as("SubMap is correctly converted").hasSize(1).contains(entry("key", "value"));
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldConvertUsingObject() {
        Entry subEntry = new EntryTest("key", Value.of("value"));
        Entry entry = new EntryTest("key", Value.of(subEntry));

        Map<String, Object> map = referenceReader.convert(new TypeReference<>() {
        }, Collections.singletonList(entry));

        Map<String, String> subMap = (Map<String, String>) map.get("key");

        assertSoftly(softly -> {
            softly.assertThat(map).as("Map is correctly converted").hasSize(1).contains(entry("key", Map.of("key", "value")));
            softly.assertThat(subMap).as("SubMap is correctly converted").hasSize(1).contains(entry("key", "value"));
        });
    }

    @Test
    void shouldConvertEntry() {
        Entry entry = new EntryTest("key", Value.of("value"));

        Map<String, String> map = referenceReader.convert(new TypeReference<>() {
        }, entry);

        assertSoftly(softly -> softly.assertThat(map).as("Map is correctly converted").hasSize(1).contains(entry("key", "value")));
    }

    @Test
    void shouldConvertEntryWithSubEntry() {
        Entry subEntry = new EntryTest("key", Value.of("value"));
        Entry entry = new EntryTest("key", Value.of(subEntry));

        Map<String, Object> map = referenceReader.convert(new TypeReference<>() {
        }, Collections.singletonList(entry));

        Map<String, String> subMap = (Map<String, String>) map.get("key");

        assertSoftly(softly -> {
            softly.assertThat(map).as("Map is correctly converted").hasSize(1).contains(entry("key", Map.of("key", "value")));
            softly.assertThat(subMap).as("SubMap is correctly converted").hasSize(1).contains(entry("key", "value"));
        });
    }

    @Test
    void shouldConvertEntryWithSubEntryAsList() {
        Entry subEntry = new EntryTest("key3", Value.of("value"));
        Entry subEntry2 = new EntryTest("key2", Value.of("value2"));
        Entry entry = new EntryTest("key", Value.of(List.of(subEntry, subEntry2)));

        Map<String, Object> map = referenceReader.convert(new TypeReference<>() {
        }, Collections.singletonList(entry));

        Map<String, Object> subMap = (Map<String, Object>) map.get("key");

        assertSoftly(softly -> {
            softly.assertThat(map).as("Map is correctly converted").hasSize(1).containsKey("key");
            softly.assertThat(subMap).as("SubMap is correctly converted").hasSize(2).contains(entry("key2", "value2"),
                    entry("key3", "value"));
        });
    }

    static Stream<Arguments> compatibleTypeReferences() {
        return Stream.of(
                arguments(new TypeReference<Map<String, String>>() {
                }),
                arguments(new TypeReference<Map<Long, Integer>>() {
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
                arguments(new TypeReference<Map<Integer, List<String>>>() {
                })
        );
    }

    public record EntryTest(String name, Value value) implements Entry {
    }
}
