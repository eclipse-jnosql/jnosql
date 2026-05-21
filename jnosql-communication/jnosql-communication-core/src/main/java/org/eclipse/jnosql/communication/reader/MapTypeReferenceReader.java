/*
 *
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.communication.reader;


import org.eclipse.jnosql.communication.Entry;
import org.eclipse.jnosql.communication.TypeReferenceReader;
import org.eclipse.jnosql.communication.TypeSupplier;
import org.eclipse.jnosql.communication.ValueReader;
import org.eclipse.jnosql.communication.ValueReaderDecorator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The {@link TypeReferenceReader} to {@link Map}
 */
@SuppressWarnings("unchecked")
public class MapTypeReferenceReader implements TypeReferenceReader {

    private static final ValueReader SERVICE_PROVIDER = ValueReaderDecorator.getInstance();
    private static final Predicate<Type> IS_CLASS = c -> c instanceof Class;

    @Override
    public boolean test(TypeSupplier<?> typeReference) {
        Type type = typeReference.get();
        if (type instanceof ParameterizedType parameterizedType) {

            return Map.class.equals(parameterizedType.getRawType()) &&
                    parameterizedType.getActualTypeArguments()[0] instanceof Class &&
                    parameterizedType.getActualTypeArguments()[1] instanceof Class;
        }
        return false;
    }

    @Override
    public <T> T convert(TypeSupplier<T> typeReference, Object value) {
        Type type = typeReference.get();
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Class<?> keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        Class<?> valueType = (Class<?>) Optional.of(parameterizedType.getActualTypeArguments()[1])
                .filter(IS_CLASS).orElse(Object.class);
        return (T) getMap(keyType, valueType, value);

    }

    private <K, V> Map<K, V> getMap(Class<K> keyClass, Class<V> valueClass, Object value) {

        if (Map.class.isInstance(value)) {
            return convertToMap(keyClass, valueClass, value);
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> collection = new ArrayList<>();
            iterable.forEach(collection::add);
            if (collection.isEmpty()) {
                return Collections.emptyMap();
            } else if (collection.stream().allMatch(Map.class::isInstance)) {
                return collection.stream().map(m -> convertToMap(keyClass, valueClass, m)).map(Map::entrySet)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            } else if (collection.stream().allMatch(Entry.class::isInstance)) {
                Map<K, V> map = new HashMap<>();
                collection.forEach(e -> convertEntryToMap(e, map));
                return map;
            }
        }
        if (Entry.class.isInstance(value)) {
            Map<K, V> map = new HashMap<>();
            convertEntryToMap(value, map);
            return map;
        }
        throw new UnsupportedOperationException("There is not supported convert" + value + " a not Map type.");
    }

    private <K, V> void convertEntryToMap(Object value, Map<K, V> map) {
        Entry entry = (Entry) value;
        Object entryValue = entry.value().get();
        if (entryValue instanceof Entry subEntry) {
            feedEntryValue(map, subEntry, entry);
        } else if(entryValue instanceof Iterable<?> iterable) {
            feedIterable(map, iterable, entry);
        } else {
            map.put((K) entry.name(), (V) entryValue);
        }
    }

    private <K, V> void feedIterable(Map<K, V> map, Iterable<?> iterable, Entry entry) {
        List<Object> collection = new ArrayList<>();
        iterable.forEach(collection::add);
        if (collection.isEmpty()) {
            map.put((K) entry.name(), (V) Collections.emptyList());
        } else if (collection.stream().allMatch(Entry.class::isInstance)) {
            Map<K, V> subMap = new HashMap<>();
            collection.forEach(e -> convertEntryToMap(e, subMap));
            map.put((K) entry.name(), (V) subMap);
        } else {
            map.put((K) entry.name(), (V) collection);
        }
    }

    private <K, V> void feedEntryValue(Map<K, V> map, Entry subEntry, Entry entry) {
        Map<String, Object> subMap = new HashMap<>();
        convertEntryToMap(subEntry, subMap);
        map.put((K) entry.name(), (V) subMap);
    }

    private <K, V> Map<K, V> convertToMap(Class<K> keyClass, Class<V> valueClass, Object value) {
        Map mapValue = Map.class.cast(value);
        return (Map<K, V>) mapValue.keySet().stream()
                .collect(Collectors.toMap(mapKeyElement(keyClass), mapValueElement(valueClass, mapValue)));
    }

    private <K> Function mapKeyElement(Class<K> keyClass) {
        return (keyElement) -> {
            if (SERVICE_PROVIDER.test(keyClass)) {
                return SERVICE_PROVIDER.read(keyClass, keyElement);
            }
            return keyElement;
        };
    }

    private <V> Function mapValueElement(Class<V> valueClass, Map mapValue) {
        return (keyElement) -> {
            Object valueElement = mapValue.get(keyElement);
            if (SERVICE_PROVIDER.test(valueClass)) {
                return SERVICE_PROVIDER.read(valueClass, valueElement);
            }
            return valueElement;
        };
    }


}
