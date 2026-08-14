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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CommunicationEntityTest {


    @Nested
    @DisplayName("When the communication entity is used")
    class WhenTheCommunicationEntityIsUsed {

        @DisplayName("Should Return Error When Name Is Null")
        @Test
        void shouldReturnErrorWhenNameIsNull() {
            assertThatThrownBy(() -> CommunicationEntity.of(null)).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Return Error When Columns Is Null")
        @Test
        void shouldReturnErrorWhenColumnsIsNull() {
            assertThatThrownBy(() -> CommunicationEntity.of("entity", null)).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Return One Column")
        @Test
        void shouldReturnOneColumn() {
            CommunicationEntity entity = CommunicationEntity.of("entity");
            assertThat(Integer.valueOf(entity.size())).isEqualTo(Integer.valueOf(0));
            assertThat(entity.isEmpty()).isTrue();

            entity.add(Element.of("name", "name"));
            entity.add(Element.of("name2", Value.of("name2")));
            assertThat(entity.isEmpty()).isFalse();
            assertThat(Integer.valueOf(entity.size())).isEqualTo(Integer.valueOf(2));
            assertThat(CommunicationEntity.of("entity", singletonList(Element.of("name", "name"))).isEmpty()).isFalse();
        }

        @DisplayName("Should Do Copy")
        @Test
        void shouldDoCopy() {
            CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(Element.of("name", "name")));
            CommunicationEntity copy = entity.copy();
            assertThat(copy).isNotSameAs(entity);
            assertThat(copy).isEqualTo(entity);

        }

        @DisplayName("Should Find Column")
        @Test
        void shouldFindColumn() {
            Element element = Element.of("name", "name");
            CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(element));
            Optional<Element> name = entity.find("name");
            Optional<Element> notfound = entity.find("not_found");
            assertThat(name.isPresent()).isTrue();
            assertThat(notfound.isPresent()).isFalse();
            assertThat(name.get()).isEqualTo(element);
        }

        @DisplayName("Should Return Error When Find Column Is Null")
        @Test
        void shouldReturnErrorWhenFindColumnIsNull() {
            Element element = Element.of("name", "name");
            assertThatThrownBy(() -> {
                CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(element));
                entity.find(null);
            }).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Find Value")
        @Test
        void shouldFindValue() {
            Element element = Element.of("name", "name");
            CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(element));
            Optional<String> name = entity.find("name", String.class);
            assertThat(name).isNotNull();
            assertThat(name.isPresent()).isTrue();
            assertThat(name.orElse("")).isEqualTo("name");
        }

        @DisplayName("Should Not Find Value")
        @Test
        void shouldNotFindValue() {
            Element element = Element.of("name", "name");
            CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(element));
            Optional<String> name = entity.find("not_found", String.class);
            assertThat(name).isNotNull();
            assertThat(name.isPresent()).isFalse();
        }

        @DisplayName("Should Find Type Supplier")
        @Test
        void shouldFindTypeSupplier() {
            Element element = Element.of("name", "name");
            CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(element));
            List<String> names = entity.find("name", new TypeReference<List<String>>() {
                    })
                    .orElse(Collections.emptyList());
            assertThat(names).isNotNull();
            assertThat(names.isEmpty()).isFalse();
            assertThat(names).contains("name");
        }

        @DisplayName("Should Not Find Type Supplier")
        @Test
        void shouldNotFindTypeSupplier() {
            Element element = Element.of("name", "name");
            CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(element));
            List<String> names = entity.find("not_find", new TypeReference<List<String>>() {
                    })
                    .orElse(Collections.emptyList());
            assertThat(names).isNotNull();
            assertThat(names.isEmpty()).isTrue();
        }

        @DisplayName("Should Remove Column")
        @Test
        void shouldRemoveColumn() {
            Element element = Element.of("name", "name");
            CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(element));
            assertThat(entity.remove("name")).isTrue();
            assertThat(entity.isEmpty()).isTrue();
        }

        @DisplayName("Should Convert To Map")
        @Test
        void shouldConvertToMap() {
            Element element = Element.of("name", "name");
            CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(element));
            Map<String, Object> result = entity.toMap();
            assertThat(result.isEmpty()).isFalse();
            assertThat(Integer.valueOf(result.size())).isEqualTo(Integer.valueOf(1));
            assertThat(result.keySet().stream().findAny().get()).isEqualTo(element.name());
        }

        @DisplayName("Should Convert Sub Column")
        @Test
        void shouldConvertSubColumn() {
            Element element = Element.of("name", "name");
            CommunicationEntity entity = CommunicationEntity.of("entity", singletonList(Element.of("sub", element)));
            Map<String, Object> result = entity.toMap();
            assertThat(result.isEmpty()).isFalse();
            assertThat(Integer.valueOf(result.size())).isEqualTo(Integer.valueOf(1));
            Map<String, Object> map = (Map<String, Object>) result.get("sub");
            assertThat(map.get("name")).isEqualTo("name");
        }


        @DisplayName("Should Convert Sub Column List To Map")
        @Test
        void shouldConvertSubColumnListToMap() {
            CommunicationEntity entity = CommunicationEntity.of("entity");
            entity.add(Element.of("_id", "id"));
            List<Element> elements = asList(Element.of("name", "Ada"), Element.of("type", "type"),
                    Element.of("information", "ada@lovelace.com"));

            entity.add(Element.of("contacts", elements));
            Map<String, Object> result = entity.toMap();
            assertThat(result.get("_id")).isEqualTo("id");
            List<Map<String, Object>> contacts = (List<Map<String, Object>>) result.get("contacts");
            assertThat(contacts.size()).isEqualTo(3);
            assertThat(contacts).contains(singletonMap("name", "Ada"), singletonMap("type", "type"),
                    singletonMap("information", "ada@lovelace.com"));

        }

        @DisplayName("Should Convert Sub Column List To Map2")
        @Test
        void shouldConvertSubColumnListToMap2() {
            CommunicationEntity entity = CommunicationEntity.of("entity");
            entity.add(Element.of("_id", "id"));
            List<List<Element>> columns = new ArrayList<>();
            columns.add(asList(Element.of("name", "Ada"), Element.of("type", "type"),
                    Element.of("information", "ada@lovelace.com")));

            entity.add(Element.of("contacts", columns));
            Map<String, Object> result = entity.toMap();
            assertThat(result.get("_id")).isEqualTo("id");
            List<List<Map<String, Object>>> contacts = (List<List<Map<String, Object>>>) result.get("contacts");
            assertThat(contacts.size()).isEqualTo(1);
            List<Map<String, Object>> maps = contacts.getFirst();
            assertThat(maps.size()).isEqualTo(3);
            assertThat(maps).contains(singletonMap("name", "Ada"), singletonMap("type", "type"),
                    singletonMap("information", "ada@lovelace.com"));

        }

        @DisplayName("Should Create ANew Instance")
        @Test
        void shouldCreateANewInstance() {
            String name = "name";
            CommunicationEntity entity = new CommunicationEntity(name);
            assertThat(entity.name()).isEqualTo(name);
        }

        @DisplayName("Should Create An Empty Entity")
        @Test
        void shouldCreateAnEmptyEntity() {
            CommunicationEntity entity = new CommunicationEntity("name");
            assertThat(entity.isEmpty()).isTrue();
        }

        @DisplayName("Should Return An Error When Add ANull Column")
        @Test
        void shouldReturnAnErrorWhenAddANullColumn() {
            assertThatThrownBy(() -> {
                CommunicationEntity entity = new CommunicationEntity("name");
                entity.add(null);
            }).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Add ANew Column")
        @Test
        void shouldAddANewColumn() {
            CommunicationEntity entity = new CommunicationEntity("name");
            entity.add(Element.of("column", 12));
            assertThat(entity.isEmpty()).isFalse();
            assertThat(entity.size()).isEqualTo(1);
        }

        @DisplayName("Should Return Error When Add An Null Iterable")
        @Test
        void shouldReturnErrorWhenAddAnNullIterable() {
            assertThatThrownBy(() -> {
                CommunicationEntity entity = new CommunicationEntity("name");
                entity.addAll(null);
            }).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Add All Columns")
        @Test
        void shouldAddAllColumns() {
            CommunicationEntity entity = new CommunicationEntity("name");
            entity.addAll(Arrays.asList(Element.of("name", 12), Element.of("value", "value")));
            assertThat(entity.isEmpty()).isFalse();
            assertThat(entity.size()).isEqualTo(2);
        }


        @DisplayName("Should Not Find Column")
        @Test
        void shouldNotFindColumn() {
            CommunicationEntity entity = new CommunicationEntity("name");
            Optional<Element> column = entity.find("name");
            assertThat(column.isPresent()).isFalse();
        }

        @DisplayName("Should Remove By Name")
        @Test
        void shouldRemoveByName() {
            CommunicationEntity entity = new CommunicationEntity("name");
            entity.add(Element.of("value", 32D));
            assertThat(entity.remove("value")).isTrue();
            assertThat(entity.isEmpty()).isTrue();
        }

        @DisplayName("Should Return Error When Removed Name Is Null")
        @Test
        void shouldReturnErrorWhenRemovedNameIsNull() {
            assertThatThrownBy(() -> {
                CommunicationEntity entity = new CommunicationEntity("name");
                entity.remove(null);
            }).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Not Remove By Name")
        @Test
        void shouldNotRemoveByName() {
            CommunicationEntity entity = new CommunicationEntity("name");
            entity.add(Element.of("value", 32D));

            assertThat(entity.remove("value1")).isFalse();
            assertThat(entity.isEmpty()).isFalse();
        }


        @DisplayName("Should Return Error When Remove By Name Is Null")
        @Test
        void shouldReturnErrorWhenRemoveByNameIsNull() {
            assertThatThrownBy(() -> {
                CommunicationEntity entity = new CommunicationEntity("name");
                entity.remove(null);
            }).isInstanceOf(NullPointerException.class);
        }


        @DisplayName("Should Add Column As Name And Object")
        @Test
        void shouldAddColumnAsNameAndObject() {
            CommunicationEntity entity = new CommunicationEntity("columnFamily");
            entity.add("name", 10);
            assertThat(entity.size()).isEqualTo(1);
            Optional<Element> name = entity.find("name");
            assertThat(name.isPresent()).isTrue();
            assertThat(name.get().get()).isEqualTo(10);
        }

        @DisplayName("Should Add Column As Name And Value")
        @Test
        void shouldAddColumnAsNameAndValue() {
            CommunicationEntity entity = new CommunicationEntity("columnFamily");
            entity.add("name", Value.of(10));
            assertThat(entity.size()).isEqualTo(1);
            Optional<Element> name = entity.find("name");
            assertThat(name.isPresent()).isTrue();
            assertThat(name.get().get()).isEqualTo(10);
        }

        @DisplayName("Should Return When Add Columns Object When Has Null Object")
        @Test
        void shouldReturnWhenAddColumnsObjectWhenHasNullObject() {
            CommunicationEntity entity = new CommunicationEntity("columnFamily");
            entity.add("name", null);
            assertThat(entity.size()).isEqualTo(1);
            Element name = entity.find("name").orElseThrow();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(name.name()).isEqualTo("name");
                softly.assertThat(name.get()).isNull();
            });
        }

        @DisplayName("Should Return Error When Add Columns Object When Has Null Column Name")
        @Test
        void shouldReturnErrorWhenAddColumnsObjectWhenHasNullColumnName() {
            assertThatThrownBy(() -> {
                CommunicationEntity entity = new CommunicationEntity("columnFamily");
                entity.add(null, 10);
            }).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Return Error When Add Columns Value When Has Null Column Name")
        @Test
        void shouldReturnErrorWhenAddColumnsValueWhenHasNullColumnName() {
            assertThatThrownBy(() -> {
                CommunicationEntity entity = new CommunicationEntity("columnFamily");
                entity.add(null, Value.of(12));
            }).isInstanceOf(NullPointerException.class);
        }


        @DisplayName("Should Avoid Duplicated Column")
        @Test
        void shouldAvoidDuplicatedColumn() {
            CommunicationEntity entity = new CommunicationEntity("columnFamily");
            entity.add("name", 10);
            entity.add("name", 13);
            assertThat(entity.size()).isEqualTo(1);
            Optional<Element> column = entity.find("name");
            assertThat(column.get()).isEqualTo(Element.of("name", 13));
        }

        @DisplayName("Should Avoid Duplicated Column When Add List")
        @Test
        void shouldAvoidDuplicatedColumnWhenAddList() {
            List<Element> elements = asList(Element.of("name", 10), Element.of("name", 13));
            CommunicationEntity entity = new CommunicationEntity("columnFamily");
            entity.addAll(elements);
            assertThat(entity.size()).isEqualTo(1);
            assertThat(CommunicationEntity.of("columnFamily", elements).size()).isEqualTo(1);
        }

        @DisplayName("Should Returns The Column Names")
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

        @DisplayName("Should Returns The Column Values")
        @Test
        void shouldReturnsTheColumnValues() {
            List<Element> elements = asList(Element.of("name", 10), Element.of("name2", 11),
                    Element.of("name3", 12), Element.of("name4", 13),
                    Element.of("name5", 14), Element.of("name5", 16));

            CommunicationEntity columnFamily = CommunicationEntity.of("columnFamily", elements);
            assertThat(columnFamily.values()).contains(Value.of(10), Value.of(11), Value.of(12),
                    Value.of(13), Value.of(16));
        }

        @DisplayName("Should Return True When Contains Element")
        @Test
        void shouldReturnTrueWhenContainsElement() {
            List<Element> elements = asList(Element.of("name", 10), Element.of("name2", 11),
                    Element.of("name3", 12), Element.of("name4", 13),
                    Element.of("name5", 14), Element.of("name5", 16));

            CommunicationEntity columnFamily = CommunicationEntity.of("columnFamily", elements);

            assertThat(columnFamily.contains("name")).isTrue();
            assertThat(columnFamily.contains("name2")).isTrue();
            assertThat(columnFamily.contains("name3")).isTrue();
            assertThat(columnFamily.contains("name4")).isTrue();
            assertThat(columnFamily.contains("name5")).isTrue();
        }

        @DisplayName("Should Return False When Does Not Contain Element")
        @Test
        void shouldReturnFalseWhenDoesNotContainElement() {
            List<Element> elements = asList(Element.of("name", 10), Element.of("name2", 11),
                    Element.of("name3", 12), Element.of("name4", 13),
                    Element.of("name5", 14), Element.of("name5", 16));

            CommunicationEntity columnFamily = CommunicationEntity.of("columnFamily", elements);

            assertThat(columnFamily.contains("name6")).isFalse();
            assertThat(columnFamily.contains("name7")).isFalse();
            assertThat(columnFamily.contains("name8")).isFalse();
            assertThat(columnFamily.contains("name9")).isFalse();
            assertThat(columnFamily.contains("name10")).isFalse();
        }

        @DisplayName("Should Remove All Elements When Use Clear Method")
        @Test
        void shouldRemoveAllElementsWhenUseClearMethod() {
            List<Element> elements = asList(Element.of("name", 10), Element.of("name2", 11),
                    Element.of("name3", 12), Element.of("name4", 13),
                    Element.of("name5", 14), Element.of("name5", 16));

            CommunicationEntity columnFamily = CommunicationEntity.of("columnFamily", elements);

            assertThat(columnFamily.isEmpty()).isFalse();
            columnFamily.clear();
            assertThat(columnFamily.isEmpty()).isTrue();
        }

        @DisplayName("Should Create Null")
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

        @DisplayName("Should Test To String")
        @Test
        void shouldTestToString() {
            CommunicationEntity entity = CommunicationEntity.of("entity");
            entity.add(Element.of("name", 10));
            assertThat(entity.toString())
                    .isNotNull()
                    .isNotBlank().contains("name=10");
        }


        @DisplayName("Should Test Hash Code")
        @Test
        void shouldTestHashCode(){
            var entity = CommunicationEntity.of("entity");
            entity.add(Element.of("name", 10));
            assertThat(entity.hashCode())
                    .isNotZero();
        }

        @DisplayName("Should Elements")
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

        @DisplayName("Should Equals")
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

}
