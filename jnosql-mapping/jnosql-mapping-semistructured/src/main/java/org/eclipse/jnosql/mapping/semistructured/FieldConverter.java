/*
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
 */
package org.eclipse.jnosql.mapping.semistructured;

import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.semistructured.Element;
import jakarta.nosql.AttributeConverter;
import org.eclipse.jnosql.mapping.metadata.ArrayFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.CollectionFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MapFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

enum FieldConverter {
    EMBEDDED {
        @Override
        public <X, Y, T> void convert(T instance, List<Element> elements, Element element,
                                      FieldMetadata field, EntityConverter converter) {
            Object subEntity = converter.toEntity(field.type(), elements);
            EntityMetadata mapping = converter.entities().get(subEntity.getClass());
            boolean areAllFieldsNull = mapping.fields()
                    .stream()
                    .map(f -> f.read(subEntity))
                    .allMatch(Objects::isNull);
            if (!areAllFieldsNull) {
                field.write(instance, subEntity);
            }
        }
    }, ENTITY {
        @Override
        public <X, Y, T> void convert(T instance, List<Element> elements, Element subElement, FieldMetadata field,
                                      EntityConverter converter) {

            if (Objects.nonNull(subElement)) {
                converterSubDocument(instance, subElement, field, converter);
            } else {
                field.write(instance, converter.toEntity(field.type(), elements));
            }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private <T> void converterSubDocument(T instance, Element subElement, FieldMetadata field,
                                              EntityConverter converter) {
            Object value = subElement.get();
            if (value instanceof Map map) {
                List<Element> embeddedColumns = new ArrayList<>();

                for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
                    embeddedColumns.add(Element.of(entry.getKey().toString(), entry.getValue()));
                }
                field.write(instance, converter.toEntity(field.type(), embeddedColumns));

            } else {
                field.write(instance, converter.toEntity(field.type(),
                        subElement.get(new TypeReference<List<Element>>() {
                        })));
            }
        }
    }, COLLECTION {
        @Override
        public <X, Y, T> void convert(T instance, List<Element> columns, Element element, FieldMetadata field,
                                      EntityConverter converter) {

            if (Objects.nonNull(element)) {
                var collectionFieldMetadata = (CollectionFieldMetadata) field;
                Class<?> type = collectionFieldMetadata.elementType();
                Collection<?> elements = collectionFieldMetadata.collectionInstance();
                if (feedEmbeddedList(element, converter, type, elements)) {
                    return;
                }
                field.write(instance, elements);
            }
        }
    }, ARRAY {
        @Override
        public <X, Y, T> void convert(T instance, List<Element> columns, Element element, FieldMetadata field,
                                      EntityConverter converter) {

            if (Objects.nonNull(element)) {
                var arrayFieldMetadata = (ArrayFieldMetadata) field;
                if (arrayFieldMetadata.isEmbeddable()) {
                    Class<?> type = arrayFieldMetadata.elementType();
                    List<Object> elements = new ArrayList<>();
                    if (feedEmbeddedList(element, converter, type, elements)) {
                        return;
                    }
                    var array = arrayFieldMetadata.arrayInstance(elements);
                    field.write(instance, array);
                } else {
                    executeNoEmbeddableField(instance, element, field, arrayFieldMetadata);
                }
            }
        }


        private <X, Y, T> void executeNoEmbeddableField(T instance, Element element, FieldMetadata field,
                                                        ArrayFieldMetadata arrayFieldMetadata) {
            var elements = new ArrayList<>();
            var value = element.get();
            if (value instanceof Iterable<?> iterable) {
                executeIterable(arrayFieldMetadata, iterable, elements);
            } else if(value instanceof Object[] objects) {
                executeIterable(arrayFieldMetadata, Arrays.asList(objects), elements);
            } else if(value.getClass().isArray()) {
                //array as primitive
                field.write(instance, value);
                return;
            } else {
                executeIterable(arrayFieldMetadata, Collections.singletonList(value), elements);
            }
            var array = arrayFieldMetadata.arrayInstance(elements);
            field.write(instance, array);
        }

        private <X, Y> void executeIterable(ArrayFieldMetadata arrayFieldMetadata, Iterable<?> iterable, List<Object> elements) {
            for (Object item : iterable) {
                elements.add(Value.of(item).get(arrayFieldMetadata.elementType()));
            }
        }
    },
    MAP {
        @SuppressWarnings("unchecked")
        @Override
        <X, Y, T> void convert(T instance, List<Element> elements, Element element, FieldMetadata field, EntityConverter converter) {
            if (Objects.nonNull(element)) {
                var mapFieldMetadata = (MapFieldMetadata) field;
                Y value = (Y) mapFieldMetadata.value(element.value());
                var optionalConverter = field.converter();
                if (mapFieldMetadata.isEmbeddable()) {
                    executeEmbeddableMap(instance, field, mapFieldMetadata, (Map<?, ?>) value, converter);
                } else if (optionalConverter.isPresent()) {
                    AttributeConverter<X, Y> attributeConverter = converter.converters().get(field);
                    Object attributeConverted = attributeConverter.convertToEntityAttribute(value);
                    field.write(instance, attributeConverted);
                } else {
                    field.write(instance, value);
                }
            }
        }

        @SuppressWarnings({"rawtypes"})
        private static <Y, T> void executeEmbeddableMap(T instance, FieldMetadata field, MapFieldMetadata mapFieldMetadata, Map<?, ?> value, EntityConverter converter) {
            Map<Object, Object> mapEntity = new HashMap<>();
            Class<?> type = mapFieldMetadata.valueType();
            for (Object key : value.keySet()) {
                var document = value.get(key);
                if (document instanceof Map map) {
                    var entity = getEntity(converter, map, type);
                    mapEntity.put(key.toString(), entity);
                }  else {
                    throw new IllegalStateException("The value of the map is not embeddable, Please use a converter or a Entity or " +
                            "Embeddable type in this map, the value type is: " + mapFieldMetadata.valueType());
                }
            }
            field.write(instance, mapEntity);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static Object getEntity(EntityConverter converter, Map map, Class<?> type) {
            List<Element> embeddedColumns = new ArrayList<>();
            for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
                embeddedColumns.add(Element.of(entry.getKey().toString(), entry.getValue()));
            }
            return converter.toEntity(type, embeddedColumns);
        }

    }, DEFAULT {
        @Override
        public <X, Y, T> void convert(T instance, List<Element> elements, Element element,
                                      FieldMetadata field, EntityConverter converter) {
            if (Objects.nonNull(element)) {
                Value value = element.value();
                Optional<Class<AttributeConverter<Object, Object>>> optionalConverter = field.converter();
                if (optionalConverter.isPresent()) {
                    executeConverter(instance, element, field, converter, value);
                } else {
                    field.write(instance, field.value(value));
                }
            }
        }

        @SuppressWarnings("unchecked")
        <X, Y, T> void executeConverter(T instance, Element element, FieldMetadata field, EntityConverter converter, Value value) {
            AttributeConverter<X, Y> attributeConverter = converter.converters().get(field);
            Y attr = (Y) (value.isInstanceOf(List.class) ? element : value.get());
            if (isElement(attr)) {
                var mapValue = value.get(new TypeReference<Map<String, Object>>() {
                });
                Object attributeConverted = attributeConverter.convertToEntityAttribute((Y) mapValue);
                field.write(instance, field.value(Value.of(attributeConverted)));
            } else {
                Object attributeConverted = attributeConverter.convertToEntityAttribute(attr);
                field.write(instance, field.value(Value.of(attributeConverted)));
            }
        }

    };

    private static boolean feedEmbeddedList(Element element, EntityConverter converter, Class<?> type, Collection elements) {
        List<List<Element>> embeddable = (List<List<Element>>) element.get();
        if (Objects.isNull(embeddable)) {
            return true;
        }
        for (List<Element> elementList : embeddable) {
            var item = converter.toEntity(type, elementList);
            elements.add(item);
        }
        return false;
    }

    <Y> boolean isElement(Y attr) {
        return attr instanceof Element;
    }

    static FieldConverter get(FieldMetadata field) {
        if (MappingType.EMBEDDED.equals(field.mappingType())) {
            return EMBEDDED;
        } else if (MappingType.ENTITY.equals(field.mappingType()) || MappingType.EMBEDDED_GROUP.equals(field.mappingType())) {
            return ENTITY;
        } else if (isCollectionEmbeddable(field)) {
            return COLLECTION;
        } else if (MappingType.MAP.equals(field.mappingType())) {
            return MAP;
        } else if (MappingType.ARRAY.equals(field.mappingType())) {
            return ARRAY;
        } else {
            return DEFAULT;
        }
    }

    private static boolean isCollectionEmbeddable(FieldMetadata field) {
        return MappingType.COLLECTION.equals(field.mappingType()) && ((CollectionFieldMetadata) field).isEmbeddable();
    }

    abstract <X, Y, T> void convert(T instance, List<Element> elements, Element element, FieldMetadata field,
                                    EntityConverter converter);

    <X, Y, T> void convert(T instance, Element element, FieldMetadata field,
                           EntityConverter converter) {
        convert(instance, null, element, field, converter);
    }
}
