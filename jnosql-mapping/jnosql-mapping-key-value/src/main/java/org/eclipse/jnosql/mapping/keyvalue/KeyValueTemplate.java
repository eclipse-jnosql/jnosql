/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
 *
 * <p>
 * The inherited {@code insert}, {@code update}, {@code find} and {@code delete} operations are mapped onto the
 * key-based methods of this interface. The fluent {@code update(Class)} API and {@code typedQuery} are not
 * supported and throw {@link UnsupportedOperationException}.
 * </p>
 *
 * <pre>{@code
 * @Inject
 * KeyValueTemplate template;
 *
 * template.put(session, Duration.ofMinutes(10));
 * Optional<Session> found = template.get("session-id", Session.class);
 * template.deleteByKey("session-id");
 * }</pre>
 */
public interface KeyValueTemplate extends Template {
    /**
     * Saves an entity under the key of its {@link jakarta.nosql.Id} attribute, replacing any existing value.
     * {@code insert(T)} and {@code update(T)} inherited from {@link Template} behave the same way.
     *
     * <pre>{@code
     * Session saved = template.put(new Session("session-id", "ada"));
     * }</pre>
     *
     * @param entity the entity to be saved
     * @param <T>    the entity type
     * @return the saved entity
     * @throws NullPointerException when the entity is null
     */
    <T> T put(T entity);

    /**
     * Saves an entity that expires after the given time to live (TTL).
     *
     * <pre>{@code
     * template.put(new Session("session-id", "ada"), Duration.ofMinutes(10));
     * }</pre>
     *
     * @param entity the entity to be saved
     * @param ttl    the time to live
     * @param <T>    the entity type
     * @return the saved entity
     * @throws NullPointerException          when the entity or ttl is null
     * @throws UnsupportedOperationException when expired time is not supported by the provider
     */
    <T> T put(T entity, Duration ttl);

    /**
     * Saves each entity of the {@link Iterable} with {@link #put(Object)}.
     *
     * <pre>{@code
     * Iterable<Session> saved = template.put(List.of(first, second));
     * }</pre>
     *
     * @param entities the entities to be saved
     * @param <T>      the entity type
     * @return the saved entities, in iteration order
     * @throws NullPointerException when the iterable is null
     */
    default <T> Iterable<T> put(Iterable<T> entities) {
        Objects.requireNonNull(entities, "entities is required");
        return StreamSupport.stream(entities.spliterator(), false).map(this::put).toList();
    }

    /**
     * Saves each entity of the {@link Iterable} with {@link #put(Object, Duration)}, all with the same TTL.
     *
     * <pre>{@code
     * template.put(List.of(first, second), Duration.ofHours(1));
     * }</pre>
     *
     * @param entities the entities to be saved
     * @param ttl      the time to live
     * @param <T>      the entity type
     * @return the saved entities, in iteration order
     * @throws NullPointerException          when the iterable or ttl is null
     * @throws UnsupportedOperationException when expired time is not supported by the provider
     */
    default <T> Iterable<T> put(Iterable<T> entities, Duration ttl) {
        Objects.requireNonNull(entities, "entities is required");
        Objects.requireNonNull(ttl, "ttl is required");
        return StreamSupport.stream(entities.spliterator(), false).map(d -> put(d, ttl)).toList();
    }

    /**
     * Finds the entity stored under a key.
     * {@code find(Class, Object)} inherited from {@link Template} behaves the same way.
     *
     * <pre>{@code
     * Optional<Session> session = template.get("session-id", Session.class);
     * }</pre>
     *
     * @param key  the key
     * @param <K>  the key type
     * @param <T>  the entity type
     * @param type the entity class to convert the result
     * @return an {@link Optional} containing the entity, or {@link Optional#empty()} if the key is not found
     * @throws NullPointerException when the key or type is null
     */
    <K, T> Optional<T> get(K key, Class<T> type);

    /**
     * Finds the entities stored under the given keys. Keys with no stored value are skipped.
     *
     * <pre>{@code
     * Iterable<Session> sessions = template.get(List.of("a", "b"), Session.class);
     * }</pre>
     *
     * @param keys the keys to be used in this query
     * @param type the entity class
     * @param <K>  the key type
     * @param <T>  the entity type
     * @return the entities found, in key order; empty when none is found
     * @throws NullPointerException when the keys or type is null
     */
    <K, T> Iterable<T> get(Iterable<K> keys, Class<T> type);

    /**
     * Removes the entity stored under the key.
     *
     * <pre>{@code
     * template.deleteByKey("session-id");
     * }</pre>
     *
     * @param key the key to be removed
     * @param <K> the key type
     * @throws NullPointerException when the key is null
     */
    <K> void deleteByKey(K key);

    /**
     * Removes the entities stored under the keys.
     *
     * <pre>{@code
     * template.deleteByKeys(List.of("a", "b"));
     * }</pre>
     *
     * @param keys the keys to be removed
     * @param <K>  the key type
     * @throws NullPointerException when the keys are null
     */
    <K> void deleteByKeys(Iterable<K> keys);

}