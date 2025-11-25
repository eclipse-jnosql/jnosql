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
 * Handles the invocation of methods inherited from {@code java.lang.Object}
 * on the proxy instance, such as {@code toString}, {@code equals}, and
 * {@code hashCode}.
 * <p>
 * This interface is used exclusively by the reflection-based proxy layer.
 * It is not required for generated repository implementations.
 *
 */
public interface ObjectMethodOperator {

    /**
     * Invokes a method from {@code Object} on the proxy instance.
     *
     * @param proxy  the dynamic proxy instance
     * @param method the {@code Object} method being invoked
     * @param params the method parameters
     * @return the method result
     * @throws Exception if invocation fails
     */
    Object invokeObjectMethod(Object proxy, Method method, Object[] params) throws Exception;
}