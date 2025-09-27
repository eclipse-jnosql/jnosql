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
 *
 */
package org.eclipse.jnosql.communication;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Collections.unmodifiableMap;

final class DefaultSettings implements Settings {

    static final Settings EMPTY = new DefaultSettings(Collections.emptyMap());
    private static final String KEY_IS_REQUIRED = "key is required";
    private static final String SUPPLIER_IS_REQUIRED_MESSAGE = "supplier is required";

    private final Map<String, Object> configurations;

    DefaultSettings(Map<String, Object> configurations) {
        this.configurations = configurations.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue));
    }


    @Override
    public int size() {
        return configurations.size();
    }

    @Override
    public boolean isEmpty() {
        return configurations.isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return configurations.containsKey(key);
    }

    @Override
    public Optional<Object> get(String key) {
        Objects.requireNonNull(key, KEY_IS_REQUIRED);
        return Optional.ofNullable(configurations.get(key));
    }

    @Override
    public Optional<Object> get(Supplier<String> supplier) {
        Objects.requireNonNull(supplier, SUPPLIER_IS_REQUIRED_MESSAGE);
        return get(supplier.get());
    }

    @Override
    public Optional<Object> getSupplier(Iterable<Supplier<String>> suppliers) {
        Objects.requireNonNull(suppliers, SUPPLIER_IS_REQUIRED_MESSAGE);
        List<String> keys = StreamSupport.stream(suppliers.spliterator(), false)
                .map(Supplier::get).toList();
        return get(keys);
    }

    @Override
    public Optional<Object> get(Iterable<String> keys) {
        Objects.requireNonNull(keys, "keys is required");

        Predicate<Map.Entry<String, Object>> equals =
                StreamSupport.stream(keys.spliterator(), false)
                        .map(prefix -> (Predicate<Map.Entry<String, Object>>) e -> e.getKey().equals(prefix))
                        .reduce(Predicate::or).orElse(e -> false);

        return configurations.entrySet().stream()
                .filter(equals)
                .findFirst()
                .map(Map.Entry::getValue);
    }

    @Override
    public List<Object> prefix(String prefix) {
        Objects.requireNonNull(prefix, "prefix is required");
        return configurations.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public List<Object> prefix(Supplier<String> supplier) {
        Objects.requireNonNull(supplier, SUPPLIER_IS_REQUIRED_MESSAGE);
        return prefix(supplier.get());
    }

    @Override
    public List<Object> prefixSupplier(Iterable<Supplier<String>> suppliers) {
        Objects.requireNonNull(suppliers, "suppliers is required");
        Iterable<String> prefixes = StreamSupport.stream(suppliers.spliterator(), false)
                .map(Supplier::get)
                .toList();
        return prefix(prefixes);
    }

    @Override
    public List<Object> prefix(Iterable<String> prefixes) {
        Objects.requireNonNull(prefixes, "prefixes is required");
        List<String> values = StreamSupport.stream(prefixes.spliterator(), false)
                .toList();
        if (values.isEmpty()) {
            return Collections.emptyList();
        }
        Predicate<Map.Entry<String, Object>> prefixCondition = values.stream()
                .map(prefix -> (Predicate<Map.Entry<String, Object>>) e -> e.getKey().startsWith(prefix))
                .reduce(Predicate::or).orElse(e -> false);

        return configurations.entrySet().stream()
                .filter(prefixCondition)
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        Objects.requireNonNull(key, KEY_IS_REQUIRED);
        Objects.requireNonNull(type, "type is required");
        return get(key).map(Value::of).map(v -> v.get(type));
    }

    @Override
    public <T> Optional<T> get(Supplier<String> supplier, Class<T> type) {
        Objects.requireNonNull(supplier, SUPPLIER_IS_REQUIRED_MESSAGE);
        Objects.requireNonNull(type, "type is required");
        return get(supplier.get(), type);
    }

    @Override
    public <T> T getOrDefault(String key, T defaultValue) {
        Objects.requireNonNull(key, KEY_IS_REQUIRED);
        Objects.requireNonNull(defaultValue, "defaultValue is required");
        Class<T> type = (Class<T>) defaultValue.getClass();
        return get(key, type).orElse(defaultValue);
    }

    @Override
    public <T> T getOrDefault(Supplier<String> supplier, T defaultValue) {
        Objects.requireNonNull(supplier, SUPPLIER_IS_REQUIRED_MESSAGE);
        Objects.requireNonNull(defaultValue, "defaultValue is required");
        return getOrDefault(supplier.get(), defaultValue);
    }


    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(configurations.keySet());
    }

    @Override
    public Map<String, Object> toMap() {
        return unmodifiableMap(configurations);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultSettings that = (DefaultSettings) o;
        return Objects.equals(configurations, that.configurations);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(configurations);
    }

    @Override
    public String toString() {
        return "DefaultSettings{" +
                "configurations=" + configurations +
                '}';
    }

    static DefaultSettings of(Map<String, Object> params) {
        Objects.requireNonNull(params, "params is required");
        return new DefaultSettings(params);
    }
}