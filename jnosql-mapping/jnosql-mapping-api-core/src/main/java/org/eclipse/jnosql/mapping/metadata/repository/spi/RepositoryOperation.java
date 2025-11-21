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
package org.eclipse.jnosql.mapping.metadata.repository.spi;

/**
 * Represents the abstraction for executing a repository operation. Each implementation
 * corresponds to a specific semantic action derived from a repository method—such as
 * a find, save, delete, update, or custom query—and performs that action using the
 * metadata and arguments supplied through a {@link RepositoryInvocationContext}. This
 * interface provides a uniform execution contract that supports both reflection-based
 * and annotation-processor–generated repository models.
 */
public interface RepositoryOperation {

    /**
     * Executes the repository operation described by the given invocation context.
     * Implementations use the method metadata and supplied arguments to perform the
     * appropriate action and return a result of the method’s declared type. The cast
     * is handled internally by the operation implementation, allowing callers to rely
     * on the generic return value without performing manual type checks.
     *
     * @param context the invocation context containing metadata and argument values
     * @param <T> the expected result type of the repository method
     * @return the result of executing the repository operation
     */
    <T> T execute(RepositoryInvocationContext context);
}