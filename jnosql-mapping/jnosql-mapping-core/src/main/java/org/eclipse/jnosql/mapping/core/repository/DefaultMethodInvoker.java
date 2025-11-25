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

import java.lang.reflect.Method;

/**
 * Handles the invocation of default interface methods on a repository.
 * <p>
 * This interface is used only by the reflection-based repository proxy.
 * Generated repository implementations do not rely on default interface
 * method invocation and therefore do not use this type.
 *
 */
public interface DefaultMethodInvoker {

    /**
     * Invokes a default method from the repository interface using reflection.
     *
     * @param repository the repository implementation instance
     * @param method     the default method to invoke
     * @param params     the method parameters
     * @return the method result
     * @throws Exception if invocation fails
     */
    Object invokeDefault(Object repository, Method method, Object[] params) throws Exception;
}