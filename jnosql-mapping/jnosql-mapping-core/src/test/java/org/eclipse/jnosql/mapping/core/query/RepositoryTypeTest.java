/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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


import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.PageableRepository;
import jakarta.data.repository.Query;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryTypeTest {


    @ParameterizedTest
    @ValueSource(strings = {"save", "deleteById", "findById", "existsById", "count", "findAll"})
    void shouldReturnDefault(String methodName) throws NoSuchMethodException {
        Method method = getMethod(BasicRepository.class, methodName);
        var type = RepositoryType.of(method, BasicRepository.class);
        assertThat(type).isEqualTo(RepositoryType.DEFAULT);
    }


    @Test
    void shouldReturnObjectMethod() throws NoSuchMethodException {
        Assertions.assertEquals(RepositoryType.OBJECT_METHOD, RepositoryType.of(getMethod(Object.class, "equals"), CrudRepository.class));
        Assertions.assertEquals(RepositoryType.OBJECT_METHOD, RepositoryType.of(getMethod(Object.class, "hashCode"), CrudRepository.class));
    }


    @Test
    void shouldReturnFindBy() throws NoSuchMethodException {
        Assertions.assertEquals(RepositoryType.FIND_BY, RepositoryType.of(getMethod(DevRepository.class, "findByName"), CrudRepository.class));
    }

    @Test
    void shouldReturnDeleteBy() throws NoSuchMethodException {
        Assertions.assertEquals(RepositoryType.DELETE_BY, RepositoryType.of(getMethod(DevRepository.class, "deleteByName"), CrudRepository.class));
    }

    @Test
    void shouldReturnFindAllBy() throws NoSuchMethodException {
        Assertions.assertEquals(RepositoryType.FIND_ALL, RepositoryType.of(getMethod(DevRepository.class, "findAll"), CrudRepository.class));
    }

    @Test
    void shouldReturnJNoSQLQuery() throws NoSuchMethodException {
        Assertions.assertEquals(RepositoryType.QUERY, RepositoryType.of(getMethod(DevRepository.class, "query"), CrudRepository.class));
    }

    @Test
    void shouldReturnUnknown() throws NoSuchMethodException {
        Assertions.assertEquals(RepositoryType.UNKNOWN, RepositoryType.of(getMethod(DevRepository.class, "nope"), CrudRepository.class));
    }

    @Test
    void shouldReturnCountBy() throws NoSuchMethodException {
        Assertions.assertEquals(RepositoryType.COUNT_BY, RepositoryType.of(getMethod(DevRepository.class, "countByName"), CrudRepository.class));
    }

    @Test
    void shouldReturnExistsBy() throws NoSuchMethodException {
        Assertions.assertEquals(RepositoryType.EXISTS_BY, RepositoryType.of(getMethod(DevRepository.class, "existsByName"), CrudRepository.class));
    }

    @Test
    void shouldReturnOrder() throws NoSuchMethodException {
        Assertions.assertEquals(RepositoryType.ORDER_BY, RepositoryType.of(getMethod(DevRepository.class,
                "order"), CrudRepository.class));

        Assertions.assertEquals(RepositoryType.ORDER_BY, RepositoryType.of(getMethod(DevRepository.class,
                "order2"), CrudRepository.class));
    }

    @Test
    void shouldDefaultMethod() throws NoSuchMethodException {
        Assertions.assertEquals(RepositoryType.DEFAULT_METHOD, RepositoryType.of(getMethod(DevRepository.class,
                "duplicate"), CrudRepository.class));
    }

    @Test
    void shouldReturnCustom() throws NoSuchMethodException {
        try (MockedStatic<CDI> cdi = Mockito.mockStatic(CDI.class)) {
            CDI<Object> current = Mockito.mock(CDI.class);
            Instance<Calculate> instance = Mockito.mock(Instance.class);
            Mockito.when(instance.isResolvable()).thenReturn(true);
            cdi.when(CDI::current).thenReturn(current);
            Mockito.when(current.select(Calculate.class)).thenReturn(instance);
            Assertions.assertEquals(RepositoryType.CUSTOM_REPOSITORY, RepositoryType.of(getMethod(Calculate.class,
                    "sum"), CrudRepository.class));
        }
    }

    @Test
    void shouldReturnFindByCustom() throws NoSuchMethodException {
        try (MockedStatic<CDI> cdi = Mockito.mockStatic(CDI.class)) {
            CDI<Object> current = Mockito.mock(CDI.class);
            Instance<Calculate> instance = Mockito.mock(Instance.class);
            Mockito.when(instance.isResolvable()).thenReturn(true);
            cdi.when(CDI::current).thenReturn(current);
            Mockito.when(current.select(Calculate.class)).thenReturn(instance);
            Assertions.assertEquals(RepositoryType.CUSTOM_REPOSITORY, RepositoryType.of(getMethod(Calculate.class,
                    "findBySum"), CrudRepository.class));
        }
    }

    @Test
    void shouldReturnFindByCustom2() throws NoSuchMethodException {
        try (MockedStatic<CDI> cdi = Mockito.mockStatic(CDI.class)) {
            CDI<Object> current = Mockito.mock(CDI.class);
            Instance<Calculate> instance = Mockito.mock(Instance.class);
            Mockito.when(instance.isResolvable()).thenReturn(true);
            cdi.when(CDI::current).thenReturn(current);
            Mockito.when(current.select(Calculate.class)).thenReturn(instance);
            Assertions.assertEquals(RepositoryType.FIND_BY, RepositoryType.of(getMethod(Calculate.class,
                    "findBySum"), Calculate.class));
        }
    }
    private Method getMethod(Class<?> repository, String methodName) throws NoSuchMethodException {
        return Stream.of(repository.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst().get();

    }

    interface DevRepository extends CrudRepository, Calculate {

        String findByName(String name);

        String deleteByName(String name);

        Stream<String> findAll();

        @Query("query")
        String query(String query);

        Long countByName(String name);

        Long existsByName(String name);

        void nope();

        @OrderBy("sample")
        String order();

        @OrderBy("sample")
        @OrderBy("test")
        String order2();

        default int duplicate(int value) {
            return value * 2;
        }
    }

    interface Calculate {
        BigDecimal sum();

        List<String> findBySum(String name);
    }

}