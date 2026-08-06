/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import jakarta.nosql.NoSQLException;


/**
 * This exception class represents an exception related to metadata operations in the context of a NoSQL database.
 * It extends the {@link NoSQLException} class and is used to signal errors that occur during metadata-related operations,
 * such as schema management or entity mapping.
 *
 * <p>The {@code MetadataException} class provides constructors for creating instances with different levels of detail,
 * including specifying custom messages and associated causes. It inherits constructors from its superclass,
 * {@link NoSQLException}, allowing for flexible exception handling and propagation of error information.
 *
 * <p>This exception typically occurs when there are issues with handling or processing metadata information,
 * such as during database schema initialization, entity mapping, or other metadata-related tasks.
 * Developers can catch instances of this exception to handle metadata-related errors gracefully,
 * potentially taking corrective actions or providing more informative error messages to users.
 *
 * <p>When using the {@code MetadataException}, it's recommended to consult the relevant documentation of the
 * NoSQL database or data mapping framework being used to understand the specific scenarios in which this
 * exception may be thrown and the appropriate ways to handle it.
 */
public class MetadataException extends NoSQLException {

    /**
     * Constructs a new runtime exception with null as its detail message.
     */
    public MetadataException() {
        super();
    }

    /**
     * Constructs a new runtime exception with the specified detail message.
     *
     * @param message the message
     */
    public MetadataException(String message) {
        super(message);
    }

    /**
     * Constructs a new runtime exception with the specified detail message and cause.
     *
     * @param message the message
     * @param cause   the cause
     */
    public MetadataException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new runtime exception with the specified cause and a detail
     * message of (cause==null ? null : cause.toString()) (which typically contains
     * the class and detail message of cause).
     *
     * @param cause the cause
     */
    public MetadataException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new runtime exception with the specified detail
     * message, cause, suppression enabled or disabled, and writable stack
     * trace enabled or disabled.
     *
     * @param message            the message
     * @param cause              the cause
     * @param enableSuppression  whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    protected MetadataException(String message, Throwable cause,
                             boolean enableSuppression,
                             boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
