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


import jakarta.data.page.CursoredPage;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Query;
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;
import jakarta.data.restrict.Restriction;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RepositoryTypeTest {

    @ParameterizedTest
    @MethodSource("getBasicRepositoryMethods")
    void shouldReturnDefaultAtBasicRepository(Method method)  {
        var type = RepositoryType.of(method, BasicRepository.class);
        assertThat(type).isEqualTo(RepositoryType.DEFAULT);
    }

    @ParameterizedTest
    @MethodSource("getCrudRepositoryMethods")
    void shouldReturnDefaultAtCrudRepository(Method method)  {
        var type = RepositoryType.of(method, BasicRepository.class);
        assertThat(type).isEqualTo(RepositoryType.DEFAULT);
    }

    @ParameterizedTest
    @MethodSource("getNoSQLRepositoryMethods")
    void shouldReturnDefaultAtPageableRepository(Method method)  {
        var type = RepositoryType.of(method, BasicRepository.class);
        assertThat(type).isEqualTo(RepositoryType.DEFAULT);
    }


    @Test
    void shouldReturnObjectMethod() throws NoSuchMethodException {
        assertEquals(RepositoryType.OBJECT_METHOD, RepositoryType.of(getMethod(Object.class, "equals"), CrudRepository.class));
        assertEquals(RepositoryType.OBJECT_METHOD, RepositoryType.of(getMethod(Object.class, "hashCode"), CrudRepository.class));
    }


    @Test
    void shouldReturnFindBy() throws NoSuchMethodException {
        assertEquals(RepositoryType.FIND_BY, RepositoryType.of(getMethod(DevRepository.class, "findByName"), CrudRepository.class));
    }

    @Test
    void shouldReturnFindFirstBy() throws NoSuchMethodException {
        assertEquals(RepositoryType.FIND_BY, RepositoryType.of(getMethod(DevRepository.class, "findFirst10ByAge"), CrudRepository.class));
    }

    @Test
    void shouldReturnSave() throws NoSuchMethodException {
        assertEquals(RepositoryType.SAVE, RepositoryType.of(getMethod(DevRepository.class, "save"), DevRepository.class));
    }

    @Test
    void shouldReturnInsert() throws NoSuchMethodException {
        assertEquals(RepositoryType.INSERT, RepositoryType.of(getMethod(DevRepository.class, "insert"), DevRepository.class));
    }

    @Test
    void shouldReturnDelete() throws NoSuchMethodException {
        assertEquals(RepositoryType.DELETE, RepositoryType.of(getMethod(DevRepository.class, "delete"), DevRepository.class));
    }

    @Test
    void shouldReturnUpdate() throws NoSuchMethodException {
        assertEquals(RepositoryType.UPDATE, RepositoryType.of(getMethod(DevRepository.class, "update"), DevRepository.class));
    }

    @Test
    void shouldReturnDeleteBy() throws NoSuchMethodException {
        assertEquals(RepositoryType.DELETE_BY, RepositoryType.of(getMethod(DevRepository.class, "deleteByName"), CrudRepository.class));
    }

    @Test
    void shouldReturnFindAllBy() throws NoSuchMethodException {
        assertEquals(RepositoryType.FIND_ALL, RepositoryType.of(getMethod(DevRepository.class, "findAll"), CrudRepository.class));
    }

    @Test
    void shouldReturnJNoSQLQuery() throws NoSuchMethodException {
        assertEquals(RepositoryType.QUERY, RepositoryType.of(getMethod(DevRepository.class, "query"), CrudRepository.class));
    }

    @Test
    void shouldReturnError() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> RepositoryType.of(getMethod(DevRepository.class, "nope"), CrudRepository.class))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldReturnParameterBased() throws NoSuchMethodException {
        assertEquals(RepositoryType.PARAMETER_BASED, RepositoryType.of(getMethod(DevRepository.class, "find"), CrudRepository.class));
    }

    @Test
    void shouldReturnParameterBased2() throws NoSuchMethodException {
        assertEquals(RepositoryType.PARAMETER_BASED, RepositoryType.of(getMethod(DevRepository.class, "find2"), CrudRepository.class));
    }


    @Test
    void shouldReturnCountBy() throws NoSuchMethodException {
        assertEquals(RepositoryType.COUNT_BY, RepositoryType.of(getMethod(DevRepository.class, "countByName"), CrudRepository.class));
    }

    @Test
    void shouldReturnCountAll() throws NoSuchMethodException {
        assertEquals(RepositoryType.COUNT_ALL, RepositoryType.of(getMethod(DevRepository.class, "countAll"), CrudRepository.class));
    }

    @Test
    void shouldReturnExistsBy() throws NoSuchMethodException {
        assertEquals(RepositoryType.EXISTS_BY, RepositoryType.of(getMethod(DevRepository.class, "existsByName"), CrudRepository.class));
    }

    @Test
    void shouldReturnOrder() throws NoSuchMethodException {

        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> RepositoryType.of(getMethod(DevRepository.class, "order"), CrudRepository.class));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> RepositoryType.of(getMethod(DevRepository.class, "order2"), CrudRepository.class));
    }

    @Test
    void shouldDefaultMethod() throws NoSuchMethodException {
        assertEquals(RepositoryType.DEFAULT_METHOD, RepositoryType.of(getMethod(DevRepository.class,
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
            assertEquals(RepositoryType.CUSTOM_REPOSITORY, RepositoryType.of(getMethod(Calculate.class,
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
            assertEquals(RepositoryType.CUSTOM_REPOSITORY, RepositoryType.of(getMethod(Calculate.class,
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
            assertEquals(RepositoryType.FIND_BY, RepositoryType.of(getMethod(Calculate.class,
                    "findBySum"), Calculate.class));
        }
    }

    @Test
    void shouldReturnFindByNameOrderByName() throws NoSuchMethodException {
        assertEquals(RepositoryType.CURSOR_PAGINATION, RepositoryType.of(getMethod(DevRepository.class, "findByNameOrderByName"), CrudRepository.class));
    }

    @Test
    void shouldFindRestrictionWithFind() throws NoSuchMethodException {
        assertEquals(RepositoryType.PARAMETER_BASED,
                RepositoryType.of(getMethod(DevRepository.class, "findRestriction"), CrudRepository.class));
    }

    @Test
    void shouldFindRestrictionWithQuery() throws NoSuchMethodException {
        assertEquals(RepositoryType.QUERY,
                RepositoryType.of(getMethod(DevRepository.class, "queryRestriction"), CrudRepository.class));
    }


    private Method getMethod(Class<?> repository, String methodName) throws NoSuchMethodException {
        return Stream.of(repository.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst().get();

    }

    interface DevRepository extends CrudRepository, Calculate {

        String findByName(String name);

        String deleteByName(String name);

        String findFirst10ByAge(String name);

        Stream<String> findAll();

        @Query("query")
        String query(String query);

        Long countByName(String name);

        Long countAll();

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

        @Delete
        void delete(String name);

        @Insert
        void insert(String name);

        @Update
        void update(String name);

        @Save
        void save(String name);

        @Find
        List<String> find(String name);

        @Find
        @OrderBy("name")
        List<String> find2(String name);
        CursoredPage<String> findByNameOrderByName(String name, PageRequest pageable);

        List<String> restriction(Restriction<String> filter);

        @Find
        @OrderBy("name")
        List<String> findRestriction(String name, Restriction<String> filter);

        @Query("WHERE name = ?1")
        @OrderBy("name")
        List<String> queryRestriction(String name, Restriction<String> filter);
    }

    interface Calculate {
        BigDecimal sum();

        List<String> findBySum(String name);
    }

    private static Stream<Arguments> getBasicRepositoryMethods() {
        return Arrays.stream(BasicRepository.class.getDeclaredMethods())
                .map(Arguments::of);
    }

    private static Stream<Arguments> getCrudRepositoryMethods() {
        return Arrays.stream(CrudRepository.class.getDeclaredMethods())
                .map(Arguments::of);
    }

    private static Stream<Arguments> getNoSQLRepositoryMethods() {
        return Arrays.stream(NoSQLRepository.class.getDeclaredMethods())
                .map(Arguments::of);
    }
}