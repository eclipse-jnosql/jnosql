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
package org.eclipse.jnosql.mapping.reflection;

import jakarta.nosql.AttributeConverter;
import org.eclipse.jnosql.communication.TypeSupplier;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.mapping.metadata.MapFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

final class DefaultMapFieldMetadata extends AbstractFieldMetadata implements MapFieldMetadata {

    private final TypeSupplier<?> typeSupplier;

    private final Class<?> keyType;

    private final Class<?> valueType;

    DefaultMapFieldMetadata(MappingType type, Field field, String name, TypeSupplier<?> typeSupplier,
                            Class<? extends AttributeConverter<?, ?>> converter,
                            FieldReader reader, FieldWriter writer, String udt) {
        super(type, field, name, converter, reader, writer, udt);
        this.typeSupplier = typeSupplier;
        this.keyType = (Class<?>) ((ParameterizedType) this.field
                .getGenericType())
                .getActualTypeArguments()[0];
        this.valueType = (Class<?>) ((ParameterizedType) this.field
                .getGenericType())
                .getActualTypeArguments()[1];
    }

    @Override
    public Object value(Value value) {
        return value.get(typeSupplier);
    }

    @Override
    public boolean isId() {
        return false;
    }


    @Override
    public boolean isEmbeddable() {
        return false;
    }

    @Override
    public Class<?> keyType() {
        return keyType;
    }

    @Override
    public Class<?> valueType() {
        return valueType;
    }

    @Override
    public Map<?, ?> mapInstance() {
        return new HashMap<>();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultMapFieldMetadata that = (DefaultMapFieldMetadata) o;
        return Objects.equals(typeSupplier, that.typeSupplier) && Objects.equals(keyType, that.keyType) && Objects.equals(valueType, that.valueType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeSupplier, keyType, valueType);
    }
}
