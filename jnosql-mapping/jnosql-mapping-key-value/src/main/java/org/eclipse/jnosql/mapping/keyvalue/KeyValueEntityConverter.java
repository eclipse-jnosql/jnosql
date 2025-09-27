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

import jakarta.nosql.AttributeConverter;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.keyvalue.KeyValueEntity;
import org.eclipse.jnosql.mapping.IdNotFoundException;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * This interface represents the converter between an entity and the {@link KeyValueEntity}
 */
public abstract class KeyValueEntityConverter {

    protected abstract EntitiesMetadata getEntities();

    protected abstract Converters getConverters();

    /**
     * Converts the instance entity to {@link KeyValueEntity}
     *
     * @param entity the instance
     * @return a {@link KeyValueEntity} instance
     * @throws NullPointerException when the entity is null
     */
    public KeyValueEntity toKeyValue(Object entity) {
        requireNonNull(entity, "entity is required");
        Class<?> type = entity.getClass();

        FieldMetadata key = getId(type);
        Object value = key.read(entity);

        requireNonNull(value, String.format("The key field %s is required", key.name()));
        return KeyValueEntity.of(getKey(value, type, false), entity);
    }

    /**
     * Converts a {@link KeyValueEntity} to entity
     *
     * @param type   the entity class
     * @param entity the {@link KeyValueEntity} to be converted
     * @param <T>    the entity type
     * @return the instance from {@link KeyValueEntity}
     * @throws NullPointerException when the entityInstance is null
     */
    public <T> T toEntity(Class<T> type, KeyValueEntity entity) {
        requireNonNull(type, "type is required");
        requireNonNull(entity, "entity is required");
        T bean = entity.value(type);
        if (Objects.isNull(bean)) {
            return null;
        }

        Object key = getKey(entity.key(), type, true);
        FieldMetadata id = getId(type);
        id.write(bean, key);
        return bean;
    }

    private <T> Object getKey(Object key, Class<T> type, boolean toEntity) {
        FieldMetadata id = getId(type);
        if (id.converter().isPresent()) {
            AttributeConverter<Object, Object> attributeConverter = getConverters().get(id);
            if (toEntity) {
                return attributeConverter.convertToEntityAttribute(key);
            } else {
                return attributeConverter.convertToDatabaseColumn(key);
            }
        } else {
            return Value.of(key).get(id.type());
        }
    }

    private FieldMetadata getId(Class<?> type) {
        EntityMetadata mapping = getEntities().get(type);
        return mapping.id().orElseThrow(() -> IdNotFoundException.newInstance(type));
    }
}
