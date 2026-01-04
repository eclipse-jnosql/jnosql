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
package org.eclipse.jnosql.mapping.metadata;

import jakarta.nosql.Id;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This instance is the meta-info of a loaded class that has the  {@link jakarta.nosql.Entity} annotation.
 * It represents the information of an entity on Jakarta NoSQL as metadata.
 */
public interface EntityMetadata {


    /**
     * @return the {@link jakarta.nosql.Entity#value()} name
     */
    String name();

    /**
     * @return the data structure type {@link jakarta.nosql.Entity#name()}
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
     * @return the fields name
     */
    List<String> fieldsName();

    /**
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
     * @return true if the entity class has the {@link jakarta.nosql.Inheritance} annotation
     */
    boolean isInheritance();

    /**
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
     * Gets the native column name from the Java field name
     *
     * @param javaField the java field
     * @return the column name or column
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
