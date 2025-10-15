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
package org.eclipse.jnosql.mapping.keyvalue;


import jakarta.nosql.QueryMapper;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.eclipse.jnosql.communication.keyvalue.KeyValueEntity;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

/**
 * This class provides a skeletal implementation of the {@link KeyValueTemplate} interface,
 * to minimize the effort required to implement this interface.
 */
public abstract class AbstractKeyValueTemplate implements KeyValueTemplate {

    protected abstract KeyValueEntityConverter getConverter();

    protected abstract BucketManager getManager();

    protected abstract KeyValueEventPersistManager getEventManager();

    @Override
    public <T> T put(T entity) {
        requireNonNull(entity, "entity is required");
        return persist(entity, (keyValueEntity) -> getManager().put(keyValueEntity));
    }

    @Override
    public <T> T put(T entity, Duration ttl) {
        requireNonNull(entity, "entity is required");
        requireNonNull(ttl, "ttl class is required");
        return persist(entity, (keyValueEntity) -> getManager().put(keyValueEntity, ttl));
    }

    @Override
    public <T> Iterable<T> insert(Iterable<T> entities) {
        return put(entities);
    }

    @Override
    public <T> Iterable<T> insert(Iterable<T> entities, Duration ttl) {
        return put(entities, ttl);
    }

    @Override
    public <T> T update(T entity) {
        return put(entity);
    }

    @Override
    public <T> Iterable<T> update(Iterable<T> entities) {
        return put(entities);
    }

    @Override
    public <T> T insert(T entity) {
        return put(entity);
    }

    @Override
    public <T> T insert(T entity, Duration ttl) {
        return put(entity, ttl);
    }

    @Override
    public <K, T> Optional<T> get(K key, Class<T> type) {
        requireNonNull(key, "key is required");
        requireNonNull(type, "entity class is required");

        Optional<Value> value = getManager().get(key);
        return value.map(v -> getConverter().toEntity(type, KeyValueEntity.of(key, v)))
                .filter(Objects::nonNull).map(e -> {
                    getEventManager().firePostEntity(e);
                    return e;
                });
    }

    @Override
    public <K, T> Iterable<T> get(Iterable<K> keys, Class<T> type) {
        requireNonNull(keys, "keys is required");
        requireNonNull(type, "type class is required");
        return StreamSupport.stream(keys.spliterator(), false)
                .map(k -> getManager().get(k)
                        .map(v -> KeyValueEntity.of(k, v)))
                .filter(Optional::isPresent)
                .map(e -> getConverter().toEntity(type, e.get()))
                .collect(Collectors.toList());
    }


    @Override
    public <K> void delete(K key) {
        requireNonNull(key, "key is required");
        getManager().delete(key);
    }

    @Override
    public <K> void delete(Iterable<K> keys) {
        requireNonNull(keys, "keys is required");
        getManager().delete(keys);
    }

    @Override
    public <T, K> Optional<T> find(Class<T> type, K id) {
        return this.get(id, type);
    }

    @Override
    public <T, K> void delete(Class<T> type, K id) {
        this.delete(id);
    }

    @Override
    public <T> QueryMapper.MapperFrom select(Class<T> type) {
        Objects.requireNonNull(type, "type is required");
        var converter = this.getConverter();
        var entities = converter.getEntities();
        var mapping = entities.get(type);
        return new MapperSelect(mapping, converter.getConverters(), this);
    }

    @Override
    public <T> QueryMapper.MapperDeleteFrom delete(Class<T> type) {
        Objects.requireNonNull(type, "type is required");
        var converter = this.getConverter();
        var entities = converter.getEntities();
        var mapping = entities.get(type);
        return new MapperDelete(mapping, converter.getConverters(), this);
    }

    protected <T> T persist(T entity, Consumer<KeyValueEntity> persistAction) {
        return Stream.of(entity)
                .map(toUnary(getEventManager()::firePreEntity))
                .map(getConverter()::toKeyValue)
                .map(toUnary(persistAction))
                .map(it -> getConverter().toEntity((Class<T>) entity.getClass(), it))
                .map(toUnary(getEventManager()::firePostEntity))
                .findFirst()
                .orElseThrow();
    }

    private <T> UnaryOperator<T> toUnary(Consumer<T> consumer) {
        return t -> {
            consumer.accept(t);
            return t;
        };
    }
}
