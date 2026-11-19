/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.metadata;

import jakarta.nosql.Id;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This instance is the meta-info of a loaded class that has the  {@link jakarta.nosql.Entity} annotation.
 * It represents the information of an entity on Jakarta NoSQL as metadata.
 * Obtain it from {@link EntitiesMetadata}; do not implement it in application code.
 *
 * <pre>{@code
 * EntityMetadata metadata = entities.get(Person.class);
 * String entityName = metadata.name();
 * Optional<FieldMetadata> id = metadata.id();
 * String nativeName = metadata.columnField("salary");
 * }</pre>
 */
public interface EntityMetadata {


    /**
     * Returns the entity name, defined by {@link jakarta.nosql.Entity#value()}.
     *
     * @return the entity name
     */
    String name();

    /**
     * Returns the name of the data structure the entity is mapped to.
     *
     * @return the mapping name
     */
    String mappingName();

    /**
     * Returns the {@link Class#getSimpleName()} of the entity
     * @return the {@link Class#getSimpleName()} of the entity
     */
    String simpleName();

    /**
     * Returns the {@link Class#getName()}} of the entity
     * @return the {@link Class#getName()} of the entity
     */
    String className();

    /**
     * Returns the names of the mapped fields.
     *
     * @return the fields name
     */
    List<String> fieldsName();

    /**
     * Returns the entity class.
     *
     * @return a {@code Class} object identifying the declared
     * type of the entity represented by this object
     */
    Class<?> type();

    /**
     * Return the parent class of this class mapping.
     * It will check the parent class has the {@link jakarta.nosql.Inheritance} annotation.
     *
     * @return the parent annotation otherwise {@link  Optional#empty()}
     */
    Optional<InheritanceMetadata> inheritance();

    /**
     * A class that has a parent with {@link jakarta.nosql.Inheritance} annotation
     * won't use the name. It will use the parent name instead.
     *
     * @return true if it has not parent class with {@link jakarta.nosql.Inheritance} or is the parent itself
     */
    boolean hasEntityName();

    /**
     * Indicates whether the entity class is annotated with {@link jakarta.nosql.Inheritance}.
     *
     * @return true if the entity class has the {@link jakarta.nosql.Inheritance} annotation
     */
    boolean isInheritance();

    /**
     * Returns the mapped fields of this entity.
     *
     * @return The fields from this class
     */
    List<FieldMetadata> fields();


    /**
     * Creates a new instance from default constructor.
     *
     * @param <T> the instance type
     * @return a new instance of this class
     */
    <T> T newInstance();


    /**
     * Returns the {@link  ConstructorMetadata} the representation of a constructor
     * @return The {@link  ConstructorMetadata}
     */
    ConstructorMetadata constructor();


    /**
     * Gets the native column name from the Java field name.
     * A dotted path such as {@code job.city} resolves to the native name of the embedded attribute. When the
     * name refers to an embeddable or embedded attribute itself, the result is its native names joined by a comma.
     *
     * @param javaField the java field
     * @return the native column name, or {@code javaField} unchanged when it does not match a mapped field
     * @throws NullPointerException when javaField is null
     */
    String columnField(String javaField);

    /**
     * Gets the {@link FieldMetadata} from the java field name
     *
     * @param javaField the java field
     * @return the field otherwise {@link Optional#empty()}
     * @throws NullPointerException when the javaField is null
     */
    Optional<FieldMetadata> fieldMapping(String javaField);

    /**
     * Returns a Fields grouped by the name
     *
     * @return a {@link FieldMetadata} grouped by
     * {@link FieldMetadata#name()}
     * @see FieldMetadata#name()
     */
    Map<String, FieldMetadata> fieldsGroupByName();


    /**
     * Returns the field that has {@link Id} annotation
     *
     * @return the field with ID annotation
     */
    Optional<FieldMetadata> id();
}
