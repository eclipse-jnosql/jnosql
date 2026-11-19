/*
 *
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 *
 */
package org.eclipse.jnosql.communication;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * It represents a configuration in a NoSQL database, such as user, password, credential, and so on.
 * It is an immutable class; create it with {@link #builder()} or {@link #of(Map)}, using the keys of
 * {@link Configurations} or driver-specific keys.
 *
 * <pre>{@code
 * Settings settings = Settings.builder()
 *         .put(Configurations.HOST, "localhost:27017")
 *         .put(Configurations.USER, "admin")
 *         .build();
 * Optional<String> host = settings.get(Configurations.HOST, String.class);
 * }</pre>
 * <p>
 * It is a temporary solution, and as soon as the Jakarta Configuration has been created,
 * this interface will be deprecated and removed.
 *
 * @see Settings#of(Map[])
 */
public interface Settings {

    /**
     * Returns a Set view of the keys contained in this map. The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa. If the map is modified while an iteration over
     * the set is in progress (except through the iterator's own remove operation), the results of the iteration are undefined.
     * The set supports element removal, which removes the corresponding mapping from the map, via the Iterator.remove,
     * Set.remove, removeAll, retainAll, and clear operations. It does not support the add or addAll operations.
     *
     * @return a set view of the keys contained in this map
     */
    Set<String> keySet();

    /**
     * Converts the settings to {@link Map}
     *
     * @return a {@link Map} instance from {@link Settings}
     */
    Map<String, Object> toMap();

    /**
     * Returns the value to which the specified key is mapped, or {@link Optional#empty()} if this map contains no mapping for the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@link Optional#empty()} if this map contains no mapping for the key
     * @throws NullPointerException when key is null
     */
    Optional<Object> get(String key);

    /**
     * Returns the value to which the specified key is mapped, or {@link Optional#empty()} if this map contains no mapping for the key.
     *
     * @param supplier the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@link Optional#empty()} if this map contains no mapping for the key
     * @throws NullPointerException when key is null
     */
    Optional<Object> get(Supplier<String> supplier);

    /**
     * Returns the value to which the specified from one of these keys is mapped, or {@link Optional#empty()}
     * if this map contains no mapping for the key.
     *
     * @param keys the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@link Optional#empty()}
     * if this map contains no mapping for the key
     * @throws NullPointerException when keys is null
     */
    Optional<Object> get(Iterable<String> keys);

    /**
     * Returns the value to which the specified from one of these keys is mapped, or {@link Optional#empty()}
     * if this map contains no mapping for the key.
     *
     * @param suppliers the key suppliers whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@link Optional#empty()}
     * if this map contains no mapping for the key
     * @throws NullPointerException when keys is null
     */
    Optional<Object> getSupplier(Iterable<Supplier<String>> suppliers);

    /**
     * Finds all keys that have the parameter as a prefix
     *
     * @param prefix the prefix
     * @return all the keys from prefix
     * @throws NullPointerException when prefix is null
     */
    List<Object> prefix(String prefix);


    /**
     * Finds all keys that have the parameter as a prefix
     *
     * @param supplier the prefix supplier
     * @return all the keys from prefix
     * @throws NullPointerException when prefix is null
     */
    List<Object> prefix(Supplier<String> supplier);

    /**
     * Finds all keys that have the parameter as a prefix
     *
     * @param prefixes the list of prefixes
     * @return all the keys from prefix
     * @throws NullPointerException when prefixes is null
     */
    List<Object> prefix(Iterable<String> prefixes);

    /**
     * Finds all keys that have the parameter as a prefix
     *
     * @param suppliers the list of prefixes
     * @return all the keys from prefix
     * @throws NullPointerException when prefixes is null
     */
    List<Object> prefixSupplier(Iterable<Supplier<String>> suppliers);

    /**
     * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
     *
     * @param key  the key whose associated value is to be returned
     * @param type the type be used as {@link Value#get(Class)}
     * @param <T>  the type value
     * @return the value to which the specified key is mapped, or {@link Optional#empty()} if this map contains no mapping for the key
     * @throws NullPointerException when there are null parameters
     */
    <T> Optional<T> get(String key, Class<T> type);

    /**
     * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
     *
     * @param supplier the key whose associated value is to be returned
     * @param type     the type be used as {@link Value#get(Class)}
     * @param <T>      the type value
     * @return the value to which the specified key is mapped, or {@link Optional#empty()} if this map contains no mapping for the key
     * @throws NullPointerException when there are null parameters
     */
    <T> Optional<T> get(Supplier<String> supplier, Class<T> type);

    /**
     * Returns the value to which the specified key is mapped, or defaultValue if this map contains no mapping for the key.
     *
     * @param <T>          the type
     * @param key          the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return the value to which the specified key is mapped, or defaultValue if this map contains no mapping for the key
     */
    <T> T getOrDefault(String key, T defaultValue);

    /**
     * Returns the value to which the specified key is mapped, or defaultValue if this map contains no mapping for the key.
     *
     * @param <T>          the type
     * @param supplier     the key's supplier whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return the value to which the specified key is mapped, or defaultValue if this map contains no mapping for the key
     */

    <T> T getOrDefault(Supplier<String> supplier, T defaultValue);

    /**
     * @return Returns true if this map contains no key-value mappings.
     */
    boolean isEmpty();

    /**
     * @return Returns the number of key-value mappings in this map.
     */
    int size();

    /**
     * Returns true if this map contains a mapping for the specified key.
     *
     * @param key key whose presence in this map is to be tested
     * @return true if this map contains a mapping for the specified key
     * @throws NullPointerException when key is null
     */
    boolean containsKey(String key);

    /**
     * Creates a {@link SettingsBuilder}
     *
     * @return a {@link SettingsBuilder} instance
     */
    static SettingsBuilder builder() {
        return new SettingsBuilder();
    }

    /**
     * Creates a {@link Settings}
     *
     * @return a {@link Settings} instance
     */
    static Settings settings() {
        return DefaultSettings.EMPTY;
    }

    /**
     * Creates a settings from maps
     *
     * @param settings the setting
     * @return the new {@link Settings} instance
     * @throws NullPointerException when either the parameter is null or there key or value null
     */
    static Settings of(Map<String, Object> settings) {

        requireNonNull(settings, "settings is required");
        SettingsBuilder builder = builder();
        builder.putAll(settings);
        return builder.build();
    }

    /**
     * Creates a settings from maps
     *
     * @param settings the setting
     * @return the new {@link Settings} instance
     * @throws NullPointerException when either the parameter is null or there key or value null
     */
    @SafeVarargs
    static Settings of(Map<String, Object>... settings) {
        Stream.of(settings).forEach(s -> requireNonNull(s, "Settings is required"));

        SettingsBuilder builder = builder();
        Stream.of(settings).forEach(builder::putAll);
        return builder.build();
    }

}
