/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.core.repository.operations;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.mapping.core.repository.ObjectMethodOperator;

import java.lang.reflect.Method;

@ApplicationScoped
public class DefaultObjectMethodOperator implements ObjectMethodOperator {
    @Override
    public Object invokeObjectMethod(Object proxy, Method method, Object[] params) throws Exception {
        return method.invoke(this, params);
    }
}
