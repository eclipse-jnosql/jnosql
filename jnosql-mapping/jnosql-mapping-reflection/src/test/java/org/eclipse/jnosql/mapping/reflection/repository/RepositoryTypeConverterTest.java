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
package org.eclipse.jnosql.mapping.reflection.repository;

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
import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethodType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RepositoryMethodTypeConverterTest {

    @Test
    void shouldReturnFindBy() throws NoSuchMethodException {
        assertEquals(RepositoryMethodType.FIND_BY, RepositoryMethodTypeConverter.of(getMethod(DevRepository.class, "findByName")));
    }

    @Test
    void shouldReturnFindFirstBy() throws NoSuchMethodException {
        assertEquals(RepositoryMethodType.FIND_BY, RepositoryMethodTypeConverter.of(getMethod(DevRepository.class, "findFirst10ByAge")));
    }

    @Test
    void shouldReturnSave() throws NoSuchMethodException {
        assertEquals(RepositoryMethodType.SAVE, RepositoryMethodTypeConverter.of(getMethod(DevRepository.class, "save")));
    }

    @Test
    void shouldReturnInsert() throws NoSuchMethodException {
        assertEquals(RepositoryMethodType.INSERT, RepositoryMethodTypeConverter.of(getMethod(DevRepository.class, "insert")));
    }

    @Test
    void shouldReturnDelete() throws NoSuchMethodException {
        assertEquals(RepositoryMethodType.DELETE, RepositoryMethodTypeConverter.of(getMethod(DevRepository.class, "delete")));
    }

    @Test
    void shouldReturnUpdate() throws NoSuchMethodException {
        assertEquals(RepositoryMethodType.UPDATE, RepositoryMethodTypeConverter.of(getMethod(DevRepository.class, "update")));
    }

    @Test
    void shouldReturnDeleteBy() throws NoSuchMethodException {
        assertEquals(RepositoryMethodType.DELETE_BY, RepositoryMethodTypeConverter.of(getMethod(DevRepository.class, "deleteByName")));
    }

    @Test
    void shouldReturnFindAllBy() throws NoSuchMethodException {
        assertEquals(RepositoryMethodType.FIND_ALL, RepositoryMethodTypeConverter.of(getMethod(DevRepository.class, "findAll")));
    }

    @Test
    void shouldReturnJNoSQLQuery() throws NoSuchMethodException {
        assertEquals(RepositoryMethodType.QUERY, RepositoryMethodTypeConverter.of(getMethod(DevRepository.class, "query")));
    }


    @Test
    void shouldReturnParameterBased() throws NoSuchMethodException {
        assertEquals(RepositoryMethodType.PARAMETER_BASED, RepositoryMethodTypeConverter.of(getMethod(DevRepository.class, "find")));
    }

    @Test
    void shouldReturnParameterBased2() throws NoSuchMethodException {
        assertEquals(RepositoryMethodType.PARAMETER_BASED, RepositoryMethodTypeConverter.of(getMethod(DevRepository.class, "find2")));
    }


    @Test
    void shouldReturnCountBy() throws NoSuchMethodException {
        assertEquals(RepositoryMethodType.COUNT_BY, RepositoryMethodTypeConverter.of(getMethod(DevRepository.class, "countByName")));
    }

    @Test
    void shouldReturnCountAll() throws NoSuchMethodException {
        assertEquals(RepositoryMethodType.COUNT_ALL, RepositoryMethodTypeConverter.of(getMethod(DevRepository.class, "countAll")));
    }

    @Test
    void shouldReturnExistsBy() throws NoSuchMethodException {
        assertEquals(RepositoryMethodType.EXISTS_BY, RepositoryMethodTypeConverter.of(getMethod(DevRepository.class, "existsByName")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"order", "order2", "nope"})
    void shouldReturnUnknown(String method) throws NoSuchMethodException {

        var value = RepositoryMethodTypeConverter.of(getMethod(DevRepository.class, method));
        Assertions.assertEquals(RepositoryMethodType.UNKNOWN, value);
    }

    @Test
    void shouldDefaultMethod() throws NoSuchMethodException {
        assertEquals(RepositoryMethodType.DEFAULT_METHOD, RepositoryMethodTypeConverter.of(getMethod(DevRepository.class,
                "duplicate")));
    }


    @Test
    void shouldReturnFindByNameOrderByName() throws NoSuchMethodException {
        assertEquals(RepositoryMethodType.CURSOR_PAGINATION, RepositoryMethodTypeConverter.of(getMethod(DevRepository.class, "findByNameOrderByName")));
    }

    @Test
    void shouldFindRestrictionWithFind() throws NoSuchMethodException {
        assertEquals(RepositoryMethodType.PARAMETER_BASED,
                RepositoryMethodTypeConverter.of(getMethod(DevRepository.class, "findRestriction")));
    }

    @Test
    void shouldFindRestrictionWithQuery() throws NoSuchMethodException {
        assertEquals(RepositoryMethodType.QUERY,
                RepositoryMethodTypeConverter.of(getMethod(DevRepository.class, "queryRestriction")));
    }


    private Method getMethod(Class<?> repository, String methodName) throws NoSuchMethodException {
        return Stream.of(repository.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst().get();

    }

    interface DevRepository extends CrudRepository {

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