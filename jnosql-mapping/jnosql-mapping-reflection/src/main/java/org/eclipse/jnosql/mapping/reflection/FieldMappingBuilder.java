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
package org.eclipse.jnosql.mapping.reflection;

import jakarta.nosql.AttributeConverter;
import org.eclipse.jnosql.communication.TypeSupplier;
import org.eclipse.jnosql.mapping.metadata.ArrayFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.CollectionFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MapFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;

import java.lang.reflect.Field;

class FieldMappingBuilder {

    private MappingType type;

    private Field field;

    private String name;

    private String entityName;

    private TypeSupplier<?> typeSupplier;

    private Class<? extends AttributeConverter<?, ?>> converter;

    private boolean id;

    private FieldReader reader;

    private FieldWriter writer;

    private String udt;

    private Class<?> elementType;


    /**
     * Sets the mapping type.
     *
     * @param type the mapping type
     * @return this builder
     */
    FieldMappingBuilder type(MappingType type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the Java field.
     *
     * @param field the Java field
     * @return this builder
     */
    FieldMappingBuilder field(Field field) {
        this.field = field;
        return this;
    }

    /**
     * Sets the mapped field name.
     *
     * @param name the mapped field name
     * @return this builder
     */
    FieldMappingBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the field type supplier.
     *
     * @param typeSupplier the type supplier
     * @return this builder
     */
    FieldMappingBuilder typeSupplier(TypeSupplier<?> typeSupplier) {
        this.typeSupplier = typeSupplier;
        return this;
    }

    /**
     * Sets the embedded entity name.
     *
     * @param entityName the entity name
     * @return this builder
     */
    FieldMappingBuilder entityName(String entityName) {
        this.entityName = entityName;
        return this;
    }

    /**
     * Sets the attribute converter type.
     *
     * @param converter the converter type
     * @return this builder
     */
    FieldMappingBuilder converter(Class<? extends AttributeConverter<?, ?>> converter) {
        this.converter = converter;
        return this;
    }

    /**
     * Sets whether the field is an identifier.
     *
     * @param id whether the field is an identifier
     * @return this builder
     */
    FieldMappingBuilder id(boolean id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the field writer.
     *
     * @param writer the field writer
     * @return this builder
     */
    FieldMappingBuilder writer(FieldWriter writer) {
        this.writer = writer;
        return this;
    }

    /**
     * Sets the user-defined type name.
     *
     * @param udt the user-defined type name
     * @return this builder
     */
    FieldMappingBuilder udt(String udt) {
        this.udt = udt;
        return this;
    }

    /**
     * Sets the field reader.
     *
     * @param reader the field reader
     * @return this builder
     */
    FieldMappingBuilder reader(FieldReader reader) {
        this.reader = reader;
        return this;
    }

    /**
     * Sets the array element type.
     *
     * @param elementType the element type
     * @return this builder
     */
    FieldMappingBuilder elementType(Class<?> elementType) {
        this.elementType = elementType;
        return this;
    }

    /**
     * Builds default field metadata.
     *
     * @return the field metadata
     */
    DefaultFieldMetadata buildDefault() {
        return new DefaultFieldMetadata(type, field, name, converter, id, reader, writer, udt);
    }

    /**
     * Builds collection field metadata.
     *
     * @return the collection field metadata
     */
    CollectionFieldMetadata buildCollection() {
        return new DefaultCollectionFieldMetadata(type, field, name, typeSupplier, converter, reader, writer, udt);
    }

    /**
     * Builds map field metadata.
     *
     * @return the map field metadata
     */
    MapFieldMetadata buildMap() {
        return new DefaultMapFieldMetadata(type, field, name, typeSupplier, converter, reader, writer, udt);
    }

    /**
     * Builds embedded field metadata.
     *
     * @return the embedded field metadata
     */
    EmbeddedFieldMetadata buildEmbedded() {
        return new EmbeddedFieldMetadata(type, field, name, entityName, reader, writer, udt);
    }

    /**
     * Builds array field metadata.
     *
     * @return the array field metadata
     */
    ArrayFieldMetadata buildArray() {
        return new DefaultArrayFieldMetadata(type, field, name, elementType, converter, reader, writer, udt);
    }

}
