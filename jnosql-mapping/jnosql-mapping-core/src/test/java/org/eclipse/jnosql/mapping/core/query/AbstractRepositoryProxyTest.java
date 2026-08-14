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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;

import jakarta.data.restrict.Restriction;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.UUID;


class AbstractRepositoryProxyTest {

    private final TestRepositoryProxy proxy = new TestRepositoryProxy();

    @DisplayName("Should invoke execute find by query")
    @Test
    void shouldInvokeExecuteFindByQuery() throws Throwable {
        Method method = TestRepository.class.getMethod("findEntityById", UUID.class);
        Object result = proxy.invoke(proxy, method, new Object[]{UUID.randomUUID()});

        assertThat(result).isEqualTo("executeFindByQuery");
    }

    @DisplayName("Should invoke execute delete by id")
    @Test
    void shouldInvokeExecuteDeleteById() throws Throwable {
        Method method = TestRepository.class.getMethod("deleteById");
        Object result = proxy.invoke(proxy, method, new Object[]{});

        assertThat(result).isEqualTo("executeDeleteByAll");
    }

    @DisplayName("Should invoke execute count by id")
    @Test
    void shouldInvokeExecuteCountById() throws Throwable {
        Method method = TestRepository.class.getMethod("countBy");
        Object result = proxy.invoke(proxy, method, new Object[]{});
        assertThat(result).isEqualTo("executeCountByQuery");
    }

    @DisplayName("Should invoke execute exist by id")
    @Test
    void shouldInvokeExecuteExistById() throws Throwable {
        Method method = TestRepository.class.getMethod("existsBy");
        Object result = proxy.invoke(proxy, method, new Object[]{});
        assertThat(result).isEqualTo("executeExistByQuery");
    }

    @DisplayName("Should invoke execute find all")
    @Test
    void shouldInvokeExecuteFindAll() throws Throwable {
        Method method = TestRepository.class.getMethod("findAll");
        Object result = proxy.invoke(proxy, method, new Object[]{});
        assertThat(result).isEqualTo("executeFindAll");
    }

    @DisplayName("Should invoke execute query")
    @Test
    void shouldInvokeExecuteQuery() throws Throwable {
        Method method = TestRepository.class.getMethod("query", int.class);
        Object result = proxy.invoke(proxy, method, new Object[]{});
        assertThat(result).isEqualTo("executeQuery");
    }

    @DisplayName("Should invoke execute cursor")
    @Test
    void shouldInvokeExecuteCursor() throws Throwable {
        Method method = TestRepository.class.getMethod("cursor");
        Object result = proxy.invoke(proxy, method, new Object[]{});
        assertThat(result).isEqualTo("executeCursorPagination");
    }

    @DisplayName("Should invoke execute find")
    @Test
    void shouldInvokeExecuteFind() throws Throwable {
        Method method = TestRepository.class.getMethod("find");
        Object result = proxy.invoke(proxy, method, new Object[]{});
        assertThat(result).isEqualTo("executeParameterBased");
    }

    @DisplayName("Should invoke throws mapping exception")
    @Test
    void shouldInvokeThrowsMappingException() throws Throwable {
        Method method = TestRepository.class.getMethod("customMethod");

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> proxy.invoke(proxy, method, new Object[]{}));
    }

    @DisplayName("Should execute delete restriction")
    @Test
    void shouldExecuteDeleteRestriction() throws Throwable {
        Method method = TestRepository.class.getMethod("delete", Restriction.class);
        Object result = proxy.invoke(proxy, method, new Object[]{(Restriction<String>) () -> null});
        assertThat(result).isEqualTo("executeDeleteRestriction");
    }

}
