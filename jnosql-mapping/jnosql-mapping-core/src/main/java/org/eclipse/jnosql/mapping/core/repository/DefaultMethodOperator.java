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
package org.eclipse.jnosql.mapping.core.repository;

import org.eclipse.jnosql.mapping.core.query.AbstractRepository;

import java.lang.reflect.Method;


/**
 * Operator responsible for invoking {@code default} methods declared in
 * Jakarta Data repository interfaces. Default methods are implemented
 * directly on the interface and must be executed outside the semantic
 * repository operation pipeline, using the JVM's default-method invocation
 * mechanism exposed through {@link java.lang.invoke.MethodHandles}.
 */
public interface DefaultMethodOperator {

    /**
     * Invokes a default method declared on a repository interface.
     *
     * @param repository the repository instance used as the invocation target
     * @param method     the default method being executed
     * @param params     the method arguments
     * @return the result returned by the default method
     * @throws Exception if the default method invocation fails
     */
    Object invokeDefault(AbstractRepository<?, ?> repository,
                         Method method,
                         Object[] params) throws Exception;
}