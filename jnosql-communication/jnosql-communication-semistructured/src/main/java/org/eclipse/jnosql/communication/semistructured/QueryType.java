/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;

import java.util.Objects;

/**
 * Enum representing the different types of queries supported in Jakarta Data.
 *
 * <p>The {@code QueryType} enum categorizes queries into three main types: {@code SELECT},
 * {@code DELETE}, and {@code UPDATE}. These types correspond to the standard operations
 * typically executed against a database. This enum is used to interpret and classify
 * queries within the Jakarta Data API, particularly in implementations like Eclipse JNoSQL.</p>
 *
 * <ul>
 *   <li>{@link #SELECT} - Represents a query that retrieves data from the database.</li>
 *   <li>{@link #DELETE} - Represents a query that removes data from the database.</li>
 *   <li>{@link #UPDATE} - Represents a query that modifies existing data in the database.</li>
 * </ul>
 *
 * <p>The {@link #parse(String)} method is provided to determine the type of a given query
 * string by extracting and evaluating its command keyword. The method returns a corresponding
 * {@code QueryType} based on the first six characters of the query, assuming that the query
 * begins with a standard SQL-like command.</p>
 *
 * <p>Note that if the query string does not contain a recognizable command (e.g., if it is
 * shorter than six characters or does not match any known command), the method defaults to
 * {@code SELECT}.</p>
 *
 * <p>This enum is particularly relevant for NoSQL implementations like Eclipse JNoSQL, where
 * the query language might differ from traditional SQL, yet still, adhere to the concepts
 * of selection, deletion, and updating of data.</p>
 */
public enum QueryType {

    /**
     * Represents a query that retrieves data from the database.
     * This is the default query type when no specific command is recognized.
     */
    SELECT,

    /**
     * Represents a query that removes data from the database.
     * Typically used to delete one or more records based on certain conditions.
     */
    DELETE,

    /**
     * Represents a query that modifies existing data in the database.
     * Typically used to update one or more records based on certain conditions.
     */
    UPDATE;

    /**
     * Parses the given query string to determine the type of query.
     *
     * @param query the query string to parse
     * @return the {@code QueryType} corresponding to the query command
     */
    public static QueryType parse(String query) {
        Objects.requireNonNull(query, "Query string cannot be null");
        String command = extractQueryCommand(query);
        return switch (command) {
            case "DELETE" -> DELETE;
            case "UPDATE" -> UPDATE;
            default -> SELECT;
        };
    }

    /**
     * Checks if the current {@code QueryType} is not a {@code SELECT} operation.
     * This method is useful for determining whether the query is intended to modify data
     * (i.e., either a {@code DELETE} or {@code UPDATE} operation) rather than retrieve it.
     * It can be employed in scenarios where different logic is applied based on whether
     * a query modifies data. For example, {@code if (queryType.isNotSelect())} can be used
     * to trigger actions specific to non-SELECT queries. This method returns {@code true}
     * if the current {@code QueryType} is either {@code DELETE} or {@code UPDATE},
     * and {@code false} if it is {@code SELECT}.
     */
    public boolean isNotSelect() {
        return this != SELECT;
    }

    /**
     * Validates the return type of method based on the type of query being executed.
     * <p>
     * This method checks whether the specified query is a {@code DELETE} or {@code UPDATE} operation
     * and ensures that the return type is {@code Void} or {@code int} or {@code long}.
     * If the query is not a {@code SELECT} operation
     * and the return type is not supporte, an {@code UnsupportedOperationException} is thrown.
     * <p>
     * This validation is necessary because {@code DELETE} and {@code UPDATE} operations typically
     * do not return a result set, and as such, they should have a {@code Void} return type or
     * return the number of deleted or updated entities, if it's supported by the data layer.
     *
     * @param returnType the return type of the method executing the query
     * @param query      the query being executed
     * @throws UnsupportedOperationException if the query is a {@code DELETE} or {@code UPDATE} operation
     *                                       and the return type is not supported
     */
    public void checkValidReturn(Class<?> returnType, String query) {
        if (isNotSelect() && !isVoid(returnType) && !isInt(returnType) && !isLong(returnType)) {
            throw new UnsupportedOperationException("The return type must be Void when the query is not a SELECT operation, due to the nature" +
                    " of DELETE and UPDATE operations. The query: " + query);
        }
    }

    boolean isVoid(Class<?> returnType) {
        return returnType == Void.class || returnType == Void.TYPE;
    }

    boolean isInt(Class<?> returnType) {
        return returnType == Integer.class || returnType == int.class;
    }

    boolean isLong(Class<?> returnType) {
        return returnType == Long.class || returnType == long.class;
    }

    private static String extractQueryCommand(String query){
        if(query.length() < 6){
            return "";
        }
        return query.substring(0, 6).toUpperCase();
    }
}
