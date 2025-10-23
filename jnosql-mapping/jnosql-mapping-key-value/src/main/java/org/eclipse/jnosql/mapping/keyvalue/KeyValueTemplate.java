/*
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
 */
package org.eclipse.jnosql.mapping.keyvalue;

import jakarta.nosql.Template;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * {@link Template} specialization for Key-Value databases.
 *
 * <p>
 * These databases store data as key-value pairs, where each key represents a unique identifier
 * for a piece of data.
 * </p>
 *
 * <p>
 * This interface provides some methods that accept queries in a text format to retrieve data from the database, but
 * <b>the query syntax belongs to each provider, thus, it is not within Jakarta NoSQL's scope to define it.
 * Accordingly, it might vary between implementations and NoSQL providers.</b>
 * </p>
 */
public interface KeyValueTemplate extends Template {
    /**
     * Saves an entity.
     *
     * @param entity the entity to be inserted
     * @param <T>    the entity type
     * @return the saved entity
     * @throws NullPointerException when the entity is null
     */
    <T> T put(T entity);

    /**
     * Saves an entity with a specified time to live (TTL).
     *
     * @param entity the entity to be inserted
     * @param ttl    the time to live
     * @param <T>    the entity type
     * @return the saved entity
     * @throws NullPointerException          when the entity is null
     * @throws UnsupportedOperationException when expired time is not supported
     */
    <T> T put(T entity, Duration ttl);

    /**
     * Saves an {@link Iterable} of entities.
     *
     * @param entities the entities to be inserted
     * @param <T>      the entity type
     * @return the saved entities
     * @throws NullPointerException when the iterable is null
     */
    default <T> Iterable<T> put(Iterable<T> entities) {
        Objects.requireNonNull(entities, "entities is required");
        return StreamSupport.stream(entities.spliterator(), false).map(this::put).toList();
    }

    /**
     * Saves an {@link Iterable} of entities with a specified time to live (TTL).
     *
     * @param entities the entities to be inserted
     * @param ttl      the time to live
     * @param <T>      the entity type
     * @return the saved entities
     * @throws NullPointerException          when the iterable or ttl is null
     * @throws UnsupportedOperationException when expired time is not supported
     */
    default <T> Iterable<T> put(Iterable<T> entities, Duration ttl) {
        Objects.requireNonNull(entities, "entities is required");
        Objects.requireNonNull(ttl, "ttl is required");
        return StreamSupport.stream(entities.spliterator(), false).map(d -> put(d, ttl)).toList();
    }

    /**
     * Finds the value associated with a key.
     *
     * @param key  the key
     * @param <K>  the key type
     * @param <T>  the entity type
     * @param type the entity class to convert the result
     * @return an {@link Optional} containing the value, or {@link Optional#empty()} if not found
     * @throws NullPointerException when the key is null
     */
    <K, T> Optional<T> get(K key, Class<T> type);

    /**
     * Finds a list of values associated with the specified keys.
     *
     * @param type the entity class
     * @param keys the keys to be used in this query
     * @param <K>  the key type
     * @param <T>  the entity type
     * @return a list of results
     * @throws NullPointerException when either the keys or the entity values are null
     */
    <K, T> Iterable<T> get(Iterable<K> keys, Class<T> type);

    /**
     * Removes an entity associated with the specified key.
     *
     * @param key the key to be used
     * @param <K> the key type
     * @throws NullPointerException when the key is null
     */
    <K> void delete(K key);

    /**
     * Removes entities associated with the specified keys.
     *
     * @param keys the keys to be used
     * @param <K>  the key type
     * @throws NullPointerException when the key is null
     */
    <K> void deleteByKeys(Iterable<K> keys);

}