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
 * Handles the invocation of custom repository methods implemented outside the
 * standard Jakarta Data repository contract. The implementation is typically
 * obtained from CDI and invoked directly.
 * <p>
 * This interface is used only by the reflection-based proxy. Code-generated
 * repositories do not depend on CDI-based custom method invocation and do not
 * use this type.
 *
 */
public interface CustomRepositoryMethodInvoker {

    /**
     * Invokes a custom repository method on a CDI-managed implementation.
     *
     * @param method the method declared in the custom repository interface
     * @param params the method parameters
     * @return the method result
     * @throws Exception if invocation fails
     */
    Object invokeCustomRepository(Method method, Object[] params) throws Exception;
}