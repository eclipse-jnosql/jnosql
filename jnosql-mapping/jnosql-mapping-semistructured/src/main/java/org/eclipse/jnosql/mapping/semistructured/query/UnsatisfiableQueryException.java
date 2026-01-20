/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.semistructured.query;

import jakarta.nosql.MappingException;

/**
 * Thrown when a query is statically determined to be unsatisfiable.
 * <p>
 * An unsatisfiable query is a valid query whose predicates can never be
 * satisfied (for example, an always-false restriction). In such cases,
 * execution can be safely short-circuited and an empty result returned
 * without interacting with the database.
 * <p>
 * This exception is intended to be used internally by the mapping or
 * query translation layers to normalize behavior across different
 * database providers.
 */
public class UnsatisfiableQueryException extends MappingException {

    /**
     * Creates a new {@code UnsatisfiableQueryException} with the given detail message.
     *
     * @param message a description of why the query is unsatisfiable
     */
    public UnsatisfiableQueryException(String message) {
        super(message);
    }
}
