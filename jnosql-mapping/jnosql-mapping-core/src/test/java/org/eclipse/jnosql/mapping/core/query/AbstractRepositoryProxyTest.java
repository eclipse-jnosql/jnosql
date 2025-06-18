/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.core.query;

import jakarta.data.restrict.Restriction;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AbstractRepositoryProxyTest {

    private final TestRepositoryProxy proxy = new TestRepositoryProxy();

    @Test
    void shouldInvokeExecuteFindByQuery() throws Throwable {
        Method method = TestRepository.class.getMethod("findEntityById", UUID.class);
        Object result = proxy.invoke(proxy, method, new Object[]{UUID.randomUUID()});

        assertEquals("executeFindByQuery", result);
    }

    @Test
    void shouldInvokeExecuteDeleteById() throws Throwable {
        Method method = TestRepository.class.getMethod("deleteById");
        Object result = proxy.invoke(proxy, method, new Object[]{});

        assertEquals("executeDeleteByAll", result);
    }

    @Test
    void shouldInvokeExecuteCountById() throws Throwable {
        Method method = TestRepository.class.getMethod("countBy");
        Object result = proxy.invoke(proxy, method, new Object[]{});
        assertEquals("executeCountByQuery", result);
    }

    @Test
    void shouldInvokeExecuteExistById() throws Throwable {
        Method method = TestRepository.class.getMethod("existsBy");
        Object result = proxy.invoke(proxy, method, new Object[]{});
        assertEquals("executeExistByQuery", result);
    }

    @Test
    void shouldInvokeExecuteFindAll() throws Throwable {
        Method method = TestRepository.class.getMethod("findAll");
        Object result = proxy.invoke(proxy, method, new Object[]{});
        assertEquals("executeFindAll", result);
    }

    @Test
    void shouldInvokeExecuteQuery() throws Throwable {
        Method method = TestRepository.class.getMethod("query", int.class);
        Object result = proxy.invoke(proxy, method, new Object[]{});
        assertEquals("executeQuery", result);
    }

    @Test
    void shouldInvokeExecuteCursor() throws Throwable {
        Method method = TestRepository.class.getMethod("cursor");
        Object result = proxy.invoke(proxy, method, new Object[]{});
        assertEquals("executeCursorPagination", result);
    }

    @Test
    void shouldInvokeExecuteFind() throws Throwable {
        Method method = TestRepository.class.getMethod("find");
        Object result = proxy.invoke(proxy, method, new Object[]{});
        assertEquals("executeParameterBased", result);
    }

    @Test
    void shouldInvokeThrowsMappingException() throws Throwable {
        Method method = TestRepository.class.getMethod("customMethod");

        assertThrows(UnsupportedOperationException.class, () -> proxy.invoke(proxy, method, new Object[]{}));
    }

    @Test
    void shouldExecuteDeleteRestriction() throws Throwable {
        Method method = TestRepository.class.getMethod("delete", Restriction.class);
        Object result = proxy.invoke(proxy, method, new Object[]{(Restriction<String>) () -> null});
        assertEquals("executeDeleteRestriction", result);
    }

}