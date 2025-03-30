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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.mapping.semistructured;

import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.mapping.metadata.ArrayParameterMetaData;
import org.eclipse.jnosql.mapping.metadata.CollectionParameterMetaData;
import org.eclipse.jnosql.mapping.metadata.ConstructorBuilder;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.MapParameterMetaData;
import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

enum ParameterConverter {
    DEFAULT {
        @Override
        void convert(EntityConverter converter,
                     Element element,
                     ParameterMetaData metaData,
                     ConstructorBuilder builder) {

            metaData.converter().ifPresentOrElse(c -> {
                Object value = converter.converters().get(metaData).convertToEntityAttribute(element.get());
                builder.add(value);
            }, () -> builder.add(element.get(metaData.type())));

        }
    }, ENTITY {
        @Override
        void convert(EntityConverter converter, Element element, ParameterMetaData metaData,
                     ConstructorBuilder builder) {

            Object value = element.get();
            if (value instanceof Map<?, ?> map) {
                List<Element> elements = new ArrayList<>();

                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    elements.add(Element.of(entry.getKey().toString(), entry.getValue()));
                }

                Object entity = converter.toEntity(metaData.type(), elements);
                builder.add(entity);

            } else {
                List<Element> columns = element.get(new TypeReference<>() {
                });
                Object entity = converter.toEntity(metaData.type(), columns);
                builder.add(entity);
            }
        }
    }, COLLECTION {
        @SuppressWarnings("unchecked")
        @Override
        void convert(EntityConverter converter, Element element, ParameterMetaData metaData,
                     ConstructorBuilder builder) {

            var collectionParameterMetaData = (CollectionParameterMetaData) metaData;
            Collection elements = collectionParameterMetaData.collectionInstance();
            List<List<Element>> embeddable = (List<List<Element>>) element.get();
            for (List<Element> elementsList : embeddable) {
                Object item = converter.toEntity(collectionParameterMetaData.elementType(), elementsList);
                elements.add(item);
            }
            builder.add(elements);
        }

    }, MAP {
        @Override
        void convert(EntityConverter converter, Element element, ParameterMetaData metaData, ConstructorBuilder builder) {
            var mapParameterMetaData = (MapParameterMetaData) metaData;
            Map<?,?> valueMap = (Map<?, ?>) mapParameterMetaData.value(element.value());
            if (mapParameterMetaData.isEmbeddable()) {
                executeEmbeddableMap(converter, builder, mapParameterMetaData, valueMap);
            } else {
                builder.add(valueMap);
            }
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static void executeEmbeddableMap(EntityConverter converter, ConstructorBuilder builder, MapParameterMetaData mapParameterMetaData, Map<?, ?> valueMap) {
            Class<?> type = mapParameterMetaData.valueType();
            Map<Object, Object> mapEntity = new HashMap<>();
            for (Object key : valueMap.keySet()) {
                var document = valueMap.get(key);
                if (document instanceof Map map) {
                    List<Element> embeddedColumns = new ArrayList<>();
                    for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
                        embeddedColumns.add(Element.of(entry.getKey().toString(), entry.getValue()));
                    }
                    Object entity = converter.toEntity(type, embeddedColumns);
                    mapEntity.put(key.toString(), entity);
                }  else {
                    throw new IllegalStateException("The value of the map is not embeddable, Please use a converter or a Entity or " +
                            "Embeddable type in this map, the value type is: " + mapParameterMetaData.valueType());
                }
            }
            builder.add(mapEntity);
        }

    }, ARRAY {
        @SuppressWarnings("unchecked")
        @Override
        void convert(EntityConverter converter, Element element, ParameterMetaData metaData, ConstructorBuilder builder) {
            var arrayParameterMetaData = (ArrayParameterMetaData) metaData;
            List<List<Element>> embeddable = (List<List<Element>>) element.get();
            var elements = Array.newInstance(arrayParameterMetaData.elementType(), embeddable.size());
            int index = 0;
            for (List<Element> elementsList : embeddable) {
                Object item = converter.toEntity(arrayParameterMetaData.elementType(), elementsList);
                Array.set(elements, index++, item);
            }
            builder.add(elements);
        }
    };

    abstract void convert(EntityConverter converter,
                          Element element, ParameterMetaData metaData,
                          ConstructorBuilder builder);

    static ParameterConverter of(ParameterMetaData parameter, EntitiesMetadata entities) {
        return switch (parameter.mappingType()) {
            case COLLECTION -> collectionConverter(parameter);
            case ARRAY -> arrayConverter(parameter);
            case MAP -> MAP;
            case ENTITY, EMBEDDED_GROUP, EMBEDDED -> ENTITY;
            default -> DEFAULT;
        };
    }

    private static ParameterConverter collectionConverter(ParameterMetaData parameter) {
        var genericParameter = (CollectionParameterMetaData) parameter;
        if (genericParameter.isEmbeddable()) {
            return COLLECTION;
        }
        return DEFAULT;
    }

    private static ParameterConverter arrayConverter(ParameterMetaData parameter) {
        var arrayParameterMetaData = (ArrayParameterMetaData) parameter;
        if (arrayParameterMetaData.isEmbeddable()) {
            return ARRAY;
        }
        return DEFAULT;
    }

}
