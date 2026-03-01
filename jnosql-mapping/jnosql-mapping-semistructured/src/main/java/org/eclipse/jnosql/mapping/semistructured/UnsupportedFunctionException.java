/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *   Matheus Oliveira
 */
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.nosql.MappingException;

/**
 * Exception thrown when a query {@link Function} is not supported by the underlying NoSQL database.
 *
 * <p>Most NoSQL databases do not natively support scalar functions. This exception signals
 * that the database provider cannot execute the requested function expression.</p>
 *
 * @since 1.1.0
 * @see Function
 */
public class UnsupportedFunctionException extends MappingException {

    /**
     * Constructs a new exception for an unsupported function on a specific database.
     *
     * @param functionName the name of the unsupported function
     * @param databaseName the name of the database
     */
    public UnsupportedFunctionException(String functionName, String databaseName) {
        super(String.format(
                "Function '%s' is not supported by %s. " +
                        "Consider using a database with SQL-compatible query support " +
                        "(e.g., Couchbase, Neo4j, Oracle NoSQL) " +
                        "or implement filtering at the application level.",
                functionName, databaseName));
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public UnsupportedFunctionException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public UnsupportedFunctionException(String message, Throwable cause) {
        super(message, cause);
    }
}
