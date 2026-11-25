/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query;

/**
 * Enum representing the types of values supported by a query syntax in NoSQL databases.
 * This enum categorizes the types of data that can be used within queries,
 * facilitating type-specific processing and validation across various NoSQL systems.
 */
public enum ValueType {
    /**
     * Represents a logical condition used within queries. Conditions are
     * expressions that evaluate to true or false, determining the flow or
     * outcomes of query operations in NoSQL databases.
     */
    CONDITION,

    /**
     * Represents numeric values. This type is for values that implement
     * the {@link Number} interface, such as integers, floats, and doubles,
     * typically used in mathematical or comparative operations within queries.
     */
    NUMBER,

    /**
     * Represents textual data. This type is for values that implement
     * the {@link CharSequence} interface, generally instantiated as {@link String}.
     * Used for operations involving textual manipulation or comparison in NoSQL queries.
     */
    STRING,

    /**
     * Represents a parameter in a dynamically constructed query.
     * Parameters are placeholders in queries that are bound to actual
     * values at runtime, enhancing flexibility and security in NoSQL databases.
     */
    PARAMETER,

    /**
     * Represents an array of values. Arrays are collections of elements, each
     * stored at contiguous memory locations, allowing for indexed access.
     * Used in NoSQL queries to handle multiple values efficiently.
     */
    ARRAY,

    /**
     * Represents a function to be executed as part of the query. Functions
     * might involve calculations, data transformation, or other operations
     * that are executed during the query processing phase in NoSQL systems.
     */
    FUNCTION,

    /**
     * Represents an enumeration type. Enums are special data types that
     * define a set of constants. Used in NoSQL queries to ensure data integrity
     * by restricting values to predefined options.
     */
    ENUM,

    /**
     * Represents a Boolean value. In computer science, a Boolean is a data
     * type that can have only the values true or false. Used in NoSQL queries to
     * handle logical branching and conditional checks.
     */
    BOOLEAN,
    /**
     * Represents a null value. Null is a special value in NoSQL databases that
     * indicates the absence of a value. Used in queries to handle missing or
     * unknown data, or to represent the absence of a value in a specific context.
     */
    NULL,
    /**
     * Represents a temporal special expression such as {@code LOCAL DATE}, {@code LOCAL TIME}
     * or {@code LOCAL DATETIME}. The expression is not a literal: it is resolved to an ordinary
     * {@link java.time.LocalDate}, {@link java.time.LocalTime} or {@link java.time.LocalDateTime}
     * each time the query is executed.
     *
     * @see TemporalQueryValue
     */
    TEMPORAL;
}
