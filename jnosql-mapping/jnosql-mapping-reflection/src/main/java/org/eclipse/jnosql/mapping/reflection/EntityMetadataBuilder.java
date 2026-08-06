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


import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

class EntityMetadataBuilder {

    private String name;

    private String mappingName;

    private List<String> fieldsName = Collections.emptyList();

    private Class<?> type;

    private List<FieldMetadata> fields = Collections.emptyList();

    private Map<String, NativeMapping> javaFieldGroupedByColumn = emptyMap();

    private Map<String, FieldMetadata> fieldsGroupedByName = emptyMap();

    private InstanceSupplier instanceSupplier;

    private InheritanceMetadata inheritance;

    private boolean hasInheritanceAnnotation;

    private ConstructorMetadata constructor;


    /**
     * Sets the entity name.
     *
     * @param name the entity name
     * @return this builder
     */
    EntityMetadataBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the mapped entity name.
     *
     * @param mappingName the mapped entity name
     * @return this builder
     */
    EntityMetadataBuilder mappingName(String mappingName) {
        this.mappingName = mappingName;
        return this;
    }

    /**
     * Sets the field names.
     *
     * @param fieldsName the field names
     * @return this builder
     */
    EntityMetadataBuilder fieldsName(List<String> fieldsName) {
        this.fieldsName = fieldsName;
        return this;
    }

    /**
     * Sets the entity Java type.
     *
     * @param type the entity type
     * @return this builder
     */
    EntityMetadataBuilder type(Class<?> type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the field metadata entries.
     *
     * @param fields the field metadata entries
     * @return this builder
     */
    EntityMetadataBuilder fields(List<FieldMetadata> fields) {
        this.fields = fields;
        return this;
    }

    /**
     * Sets Java fields grouped by native column name.
     *
     * @param javaFieldGroupedByColumn the Java fields grouped by native column
     * @return this builder
     */
    EntityMetadataBuilder javaFieldGroupedByColumn(Map<String, NativeMapping> javaFieldGroupedByColumn) {
        this.javaFieldGroupedByColumn = javaFieldGroupedByColumn;
        return this;
    }

    /**
     * Sets fields grouped by logical name.
     *
     * @param fieldsGroupedByName the fields grouped by name
     * @return this builder
     */
    EntityMetadataBuilder fieldsGroupedByName(Map<String, FieldMetadata> fieldsGroupedByName) {
        this.fieldsGroupedByName = fieldsGroupedByName;
        return this;
    }

    /**
     * Sets the instance supplier.
     *
     * @param instanceSupplier the instance supplier
     * @return this builder
     */
    EntityMetadataBuilder instanceSupplier(InstanceSupplier instanceSupplier) {
        this.instanceSupplier = instanceSupplier;
        return this;
    }

    /**
     * Sets inheritance metadata.
     *
     * @param inheritance the inheritance metadata
     * @return this builder
     */
    EntityMetadataBuilder inheritance(InheritanceMetadata inheritance) {
        this.inheritance = inheritance;
        return this;
    }

    /**
     * Sets whether the entity declares inheritance metadata.
     *
     * @param hasInheritanceAnnotation whether inheritance is declared
     * @return this builder
     */
    EntityMetadataBuilder hasInheritanceAnnotation(boolean hasInheritanceAnnotation) {
        this.hasInheritanceAnnotation = hasInheritanceAnnotation;
        return this;
    }

    /**
     * Sets constructor metadata.
     *
     * @param constructor the constructor metadata
     * @return this builder
     */
    EntityMetadataBuilder constructor(ConstructorMetadata constructor) {
        this.constructor = constructor;
        return this;
    }


    /**
     * Builds entity metadata.
     *
     * @return the entity metadata
     */
    EntityMetadata build() {
        return new DefaultEntityMetadata(name, mappingName, fieldsName, type, fields,
                javaFieldGroupedByColumn, fieldsGroupedByName, instanceSupplier, inheritance,
                constructor, hasInheritanceAnnotation);
    }
}