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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


class CommunicationEntityTest {

    @Test
    void shouldReturnErrorWhenNameIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> CommunicationEntity.of(null));
    }

    @Test
    void shouldReturnErrorWhenColumnsIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> CommunicationEntity.of("entity", null));
    }

    @Test
    void shouldReturnOneColumn() {
        CommunicationEntity entity = CommunicationEntity.of("entity");
        assertEquals(Integer.valueOf(0), Integer.valueOf(entity.size()));
        assertTrue(entity.isEmpty());

        entity.add(Element.of("name", "name"));
        entity.add(Element.of("name2", Value.of("name2")));
        assertFalse(entity.isEmpty());
        assertEquals(Integer.valueOf(2), Integer.valueOf(entity.size()));
        assertFalse(CommunicationEntity.of("entity", singletonList(Element.of("name", "name"))).isEmpty());
    }

    @Test
    void shouldDoCopy() {
        CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(Element.of("name", "name")));
        CommunicationEntity copy = entity.copy();
        assertNotSame(entity, copy);
        assertEquals(entity, copy);

    }

    @Test
    void shouldFindColumn() {
        Element element = Element.of("name", "name");
        CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(element));
        Optional<Element> name = entity.find("name");
        Optional<Element> notfound = entity.find("not_found");
        assertTrue(name.isPresent());
        assertFalse(notfound.isPresent());
        assertEquals(element, name.get());
    }

    @Test
    void shouldReturnErrorWhenFindColumnIsNull() {
        Element element = Element.of("name", "name");
        Assertions.assertThrows(NullPointerException.class, () -> {
            CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(element));
            entity.find(null);
        });
    }

    @Test
    void shouldFindValue() {
        Element element = Element.of("name", "name");
        CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(element));
        Optional<String> name = entity.find("name", String.class);
        Assertions.assertNotNull(name);
        Assertions.assertTrue(name.isPresent());
        Assertions.assertEquals("name", name.orElse(""));
    }

    @Test
    void shouldNotFindValue() {
        Element element = Element.of("name", "name");
        CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(element));
        Optional<String> name = entity.find("not_found", String.class);
        Assertions.assertNotNull(name);
        Assertions.assertFalse(name.isPresent());
    }

    @Test
    void shouldFindTypeSupplier() {
        Element element = Element.of("name", "name");
        CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(element));
        List<String> names = entity.find("name", new TypeReference<List<String>>() {
                })
                .orElse(Collections.emptyList());
        Assertions.assertNotNull(names);
        Assertions.assertFalse(names.isEmpty());
        assertThat(names).contains("name");
    }

    @Test
    void shouldNotFindTypeSupplier() {
        Element element = Element.of("name", "name");
        CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(element));
        List<String> names = entity.find("not_find", new TypeReference<List<String>>() {
                })
                .orElse(Collections.emptyList());
        Assertions.assertNotNull(names);
        Assertions.assertTrue(names.isEmpty());
    }

    @Test
    void shouldRemoveColumn() {
        Element element = Element.of("name", "name");
        CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(element));
        assertTrue(entity.remove("name"));
        assertTrue(entity.isEmpty());
    }

    @Test
    void shouldConvertToMap() {
        Element element = Element.of("name", "name");
        CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(element));
        Map<String, Object> result = entity.toMap();
        assertFalse(result.isEmpty());
        assertEquals(Integer.valueOf(1), Integer.valueOf(result.size()));
        assertEquals(element.name(), result.keySet().stream().findAny().get());
    }

    @Test
    void shouldConvertSubColumn() {
        Element element = Element.of("name", "name");
        CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(Element.of("sub", element)));
        Map<String, Object> result = entity.toMap();
        assertFalse(result.isEmpty());
        assertEquals(Integer.valueOf(1), Integer.valueOf(result.size()));
        Map<String, Object> map = (Map<String, Object>) result.get("sub");
        assertEquals("name", map.get("name"));
    }


    @Test
    void shouldConvertSubColumnListToMap() {
        CommunicationEntity entity = CommunicationEntity.of("entity");
        entity.add(Element.of("_id", "id"));
        List<Element> elements = asList(Element.of("name", "Ada"), Element.of("type", "type"),
                Element.of("information", "ada@lovelace.com"));

        entity.add(Element.of("contacts", elements));
        Map<String, Object> result = entity.toMap();
        assertEquals("id", result.get("_id"));
        List<Map<String, Object>> contacts = (List<Map<String, Object>>) result.get("contacts");
        assertEquals(3, contacts.size());
        assertThat(contacts).contains(singletonMap("name", "Ada"), singletonMap("type", "type"),
                singletonMap("information", "ada@lovelace.com"));

    }

    @Test
    void shouldConvertSubColumnListToMap2() {
        CommunicationEntity entity = CommunicationEntity.of("entity");
        entity.add(Element.of("_id", "id"));
        List<List<Element>> columns = new ArrayList<>();
        columns.add(asList(Element.of("name", "Ada"), Element.of("type", "type"),
                Element.of("information", "ada@lovelace.com")));

        entity.add(Element.of("contacts", columns));
        Map<String, Object> result = entity.toMap();
        assertEquals("id", result.get("_id"));
        List<List<Map<String, Object>>> contacts = (List<List<Map<String, Object>>>) result.get("contacts");
        assertEquals(1, contacts.size());
        List<Map<String, Object>> maps = contacts.getFirst();
        assertEquals(3, maps.size());
        assertThat(maps).contains(singletonMap("name", "Ada"), singletonMap("type", "type"),
                singletonMap("information", "ada@lovelace.com"));

    }

    @Test
    void shouldCreateANewInstance() {
        String name = "name";
        CommunicationEntity entity = new CommunicationEntity(name);
        assertEquals(name, entity.name());
    }

    @Test
    void shouldCreateAnEmptyEntity() {
        CommunicationEntity entity = new CommunicationEntity("name");
        assertTrue(entity.isEmpty());
    }

    @Test
    void shouldReturnAnErrorWhenAddANullColumn() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            CommunicationEntity entity = new CommunicationEntity("name");
            entity.add(null);
        });
    }

    @Test
    void shouldAddANewColumn() {
        CommunicationEntity entity = new CommunicationEntity("name");
        entity.add(Element.of("column", 12));
        assertFalse(entity.isEmpty());
        assertEquals(1, entity.size());
    }

    @Test
    void shouldReturnErrorWhenAddAnNullIterable() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            CommunicationEntity entity = new CommunicationEntity("name");
            entity.addAll(null);
        });
    }

    @Test
    void shouldAddAllColumns() {
        CommunicationEntity entity = new CommunicationEntity("name");
        entity.addAll(Arrays.asList(Element.of("name", 12), Element.of("value", "value")));
        assertFalse(entity.isEmpty());
        assertEquals(2, entity.size());
    }


    @Test
    void shouldNotFindColumn() {
        CommunicationEntity entity = new CommunicationEntity("name");
        Optional<Element> column = entity.find("name");
        assertFalse(column.isPresent());
    }

    @Test
    void shouldRemoveByName() {
        CommunicationEntity entity = new CommunicationEntity("name");
        entity.add(Element.of("value", 32D));
        assertTrue(entity.remove("value"));
        assertTrue(entity.isEmpty());
    }

    @Test
    void shouldReturnErrorWhenRemovedNameIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            CommunicationEntity entity = new CommunicationEntity("name");
            entity.remove(null);
        });
    }

    @Test
    void shouldNotRemoveByName() {
        CommunicationEntity entity = new CommunicationEntity("name");
        entity.add(Element.of("value", 32D));

        assertFalse(entity.remove("value1"));
        assertFalse(entity.isEmpty());
    }


    @Test
    void shouldReturnErrorWhenRemoveByNameIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            CommunicationEntity entity = new CommunicationEntity("name");
            entity.remove(null);
        });
    }


    @Test
    void shouldAddColumnAsNameAndObject() {
        CommunicationEntity entity = new CommunicationEntity("columnFamily");
        entity.add("name", 10);
        assertEquals(1, entity.size());
        Optional<Element> name = entity.find("name");
        assertTrue(name.isPresent());
        assertEquals(10, name.get().get());
    }

    @Test
    void shouldAddColumnAsNameAndValue() {
        CommunicationEntity entity = new CommunicationEntity("columnFamily");
        entity.add("name", Value.of(10));
        assertEquals(1, entity.size());
        Optional<Element> name = entity.find("name");
        assertTrue(name.isPresent());
        assertEquals(10, name.get().get());
    }

    @Test
    void shouldReturnWhenAddColumnsObjectWhenHasNullObject() {
        CommunicationEntity entity = new CommunicationEntity("columnFamily");
        entity.add("name", null);
        assertEquals(1, entity.size());
        Element name = entity.find("name").orElseThrow();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(name.name()).isEqualTo("name");
            softly.assertThat(name.get()).isNull();
        });
    }

    @Test
    void shouldReturnErrorWhenAddColumnsObjectWhenHasNullColumnName() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            CommunicationEntity entity = new CommunicationEntity("columnFamily");
            entity.add(null, 10);
        });
    }

    @Test
    void shouldReturnErrorWhenAddColumnsValueWhenHasNullColumnName() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            CommunicationEntity entity = new CommunicationEntity("columnFamily");
            entity.add(null, Value.of(12));
        });
    }


    @Test
    void shouldAvoidDuplicatedColumn() {
        CommunicationEntity entity = new CommunicationEntity("columnFamily");
        entity.add("name", 10);
        entity.add("name", 13);
        assertEquals(1, entity.size());
        Optional<Element> column = entity.find("name");
        assertEquals(Element.of("name", 13), column.get());
    }

    @Test
    void shouldAvoidDuplicatedColumnWhenAddList() {
        List<Element> elements = asList(Element.of("name", 10), Element.of("name", 13));
        CommunicationEntity entity = new CommunicationEntity("columnFamily");
        entity.addAll(elements);
        assertEquals(1, entity.size());
        assertEquals(1, CommunicationEntity.of("columnFamily", elements).size());
    }

    @Test
    void shouldReturnsTheColumnNames() {
        List<Element> elements = asList(Element.of("name", 10), Element.of("name2", 11),
                Element.of("name3", 12), Element.of("name4", 13),
                Element.of("name5", 14), Element.of("name5", 16));

        CommunicationEntity columnFamily = CommunicationEntity.of("columnFamily", elements);
        assertThat(columnFamily.elementNames())
                .hasSize(5)
                .contains("name", "name2", "name3", "name4", "name5");

    }

    @Test
    void shouldReturnsTheColumnValues() {
        List<Element> elements = asList(Element.of("name", 10), Element.of("name2", 11),
                Element.of("name3", 12), Element.of("name4", 13),
                Element.of("name5", 14), Element.of("name5", 16));

        CommunicationEntity columnFamily = CommunicationEntity.of("columnFamily", elements);
        assertThat(columnFamily.values()).contains(Value.of(10), Value.of(11), Value.of(12),
                Value.of(13), Value.of(16));
    }

    @Test
    void shouldReturnTrueWhenContainsElement() {
        List<Element> elements = asList(Element.of("name", 10), Element.of("name2", 11),
                Element.of("name3", 12), Element.of("name4", 13),
                Element.of("name5", 14), Element.of("name5", 16));

        CommunicationEntity columnFamily = CommunicationEntity.of("columnFamily", elements);

        assertTrue(columnFamily.contains("name"));
        assertTrue(columnFamily.contains("name2"));
        assertTrue(columnFamily.contains("name3"));
        assertTrue(columnFamily.contains("name4"));
        assertTrue(columnFamily.contains("name5"));
    }

    @Test
    void shouldReturnFalseWhenDoesNotContainElement() {
        List<Element> elements = asList(Element.of("name", 10), Element.of("name2", 11),
                Element.of("name3", 12), Element.of("name4", 13),
                Element.of("name5", 14), Element.of("name5", 16));

        CommunicationEntity columnFamily = CommunicationEntity.of("columnFamily", elements);

        assertFalse(columnFamily.contains("name6"));
        assertFalse(columnFamily.contains("name7"));
        assertFalse(columnFamily.contains("name8"));
        assertFalse(columnFamily.contains("name9"));
        assertFalse(columnFamily.contains("name10"));
    }

    @Test
    void shouldRemoveAllElementsWhenUseClearMethod() {
        List<Element> elements = asList(Element.of("name", 10), Element.of("name2", 11),
                Element.of("name3", 12), Element.of("name4", 13),
                Element.of("name5", 14), Element.of("name5", 16));

        CommunicationEntity columnFamily = CommunicationEntity.of("columnFamily", elements);

        assertFalse(columnFamily.isEmpty());
        columnFamily.clear();
        assertTrue(columnFamily.isEmpty());
    }

    @Test
    void shouldCreateNull() {
        CommunicationEntity entity = CommunicationEntity.of("entity");
        entity.addNull("name");
        Element name = entity.find("name").orElseThrow();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(name.name()).isEqualTo("name");
            softly.assertThat(name.get()).isNull();
        });
    }

    @Test
    void shouldTestToString() {
        CommunicationEntity entity = CommunicationEntity.of("entity");
        entity.add(Element.of("name", 10));
        org.assertj.core.api.Assertions.assertThat(entity.toString())
                .isNotNull()
                .isNotBlank().contains("name=10");
    }


    @Test
    void shouldTestHashCode(){
        var entity = CommunicationEntity.of("entity");
        entity.add(Element.of("name", 10));
        org.assertj.core.api.Assertions.assertThat(entity.hashCode())
                .isNotZero();
    }

    @Test
    void shouldElements() {
        CommunicationEntity entity = CommunicationEntity.of("entity");
        entity.add(Element.of("name", 10));

        List<Element> elements = entity.elements();
        SoftAssertions.assertSoftly(softly -> {
           softly.assertThat(elements).hasSize(1);
           softly.assertThat(elements.getFirst().name()).isEqualTo("name");
        });
    }

    @Test
    void shouldEquals() {
        var entity = CommunicationEntity.of("entity");
        entity.add(Element.of("name", 10));
        var entity2 = CommunicationEntity.of("entity");
        entity2.add(Element.of("name", 10));
        var entity3 = CommunicationEntity.of("entity");
        entity3.add(Element.of("name", 11));

        SoftAssertions.assertSoftly(softly -> {
           softly.assertThat(entity).isEqualTo(entity2);
           softly.assertThat(entity).isNotEqualTo(entity3);
           softly.assertThat(entity2).isEqualTo(entity);
           softly.assertThat(entity3).isNotEqualTo(entity);
           softly.assertThat(entity).isNotEqualTo(null);
           softly.assertThat(entity).isNotEqualTo(new Object());
           softly.assertThat(entity).isEqualTo(entity);
        });
    }

}