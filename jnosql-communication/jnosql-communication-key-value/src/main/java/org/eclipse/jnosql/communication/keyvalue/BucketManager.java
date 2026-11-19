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
package org.eclipse.jnosql.communication.keyvalue;


import org.eclipse.jnosql.communication.Value;

import java.time.Duration;
import java.util.Optional;

/**
 * Stores and retrieves values by key in one bucket of a key-value database.
 * Keys are given as plain Java objects or wrapped in a {@link KeyValueEntity}; values are returned as
 * {@link Value}, which converts them to the requested type.
 * Obtain an instance from {@link BucketManagerFactory} and close it when it is no longer needed.
 *
 * <pre>{@code
 * bucket.put(KeyValueEntity.of("ada", "Ada Lovelace"));
 * Optional<Value> name = bucket.get("ada");
 * String text = name.map(v -> v.get(String.class)).orElse("unknown");
 * bucket.delete("ada");
 * }</pre>
 *
 * @see KeyValueEntity
 * @see BucketManagerFactory
 */
public interface BucketManager extends AutoCloseable {

    /**
     * Returns the bucket name of this {@link BucketManager}
     *
     * @return the bucket name
     */
    String name();

    /**
     * Associates the specified value with the specified key and stores it.
     *
     * @param key   the key
     * @param value the value
     * @param <K>   the key type
     * @param <V>   the value type
     * @throws NullPointerException when either key or value are null
     */
    <K, V> void put(K key, V value);

    /**
     * Saves the {@link KeyValueEntity}
     *
     * @param entity the entity to be inserted
     * @throws NullPointerException when entity is null
     */
    void put(KeyValueEntity entity);

    /**
     * Saves the {@link KeyValueEntity} with time to live
     *
     * @param entity the entity to be inserted
     * @param ttl    the defined time to live
     * @throws NullPointerException          when entity is null
     * @throws UnsupportedOperationException when expired time is not supported
     */
    void put(KeyValueEntity entity, Duration ttl);

    /**
     * Saves the {@link Iterable} of keys
     *
     * @param entities keys to be inserted
     * @throws NullPointerException when the iterable is null
     */
    void put(Iterable<KeyValueEntity> entities);

    /**
     * Saves the {@link Iterable} of keys with a defined time to live
     *
     * @param entities keys to be inserted
     * @param ttl      the time to entity expire
     * @throws NullPointerException          when the iterable is null
     * @throws UnsupportedOperationException when expired time is not supported
     */
    void put(Iterable<KeyValueEntity> entities, Duration ttl);

    /**
     * Finds the value associated with a key.
     *
     * @param key the key
     * @param <K> the key type
     * @return the {@link Value}, or {@link Optional#empty()} when the key is not found
     * @throws NullPointerException when the key is null
     */
    <K> Optional<Value> get(K key);

    /**
     * Finds the values associated with the given keys.
     *
     * @param keys the keys to be used in this query
     * @param <K>  the key type
     * @return the values found for the keys
     * @throws NullPointerException when the keys are null
     */
    <K> Iterable<Value> get(Iterable<K> keys);

    /**
     * Removes an entity from key
     *
     * @param key the key bo be used
     * @param <K> the key type
     * @throws NullPointerException when the key is null
     */
    <K> void delete(K key);

    /**
     * Removes entities from keys
     *
     * @param keys the keys to be used
     * @param <K>  the key type
     * @throws NullPointerException when the key is null
     */
    <K> void delete(Iterable<K> keys);

    /**
     * closes a resource
     */
    @Override
    void close();

}
