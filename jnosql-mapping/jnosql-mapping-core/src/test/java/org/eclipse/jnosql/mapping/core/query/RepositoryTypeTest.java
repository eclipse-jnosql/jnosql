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


import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
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

@SuppressWarnings("removal")
class RepositoryTypeTest {
































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

    @Nested
    @DisplayName("When the repository type operates")
    class WhenTheRepositoryTypeOperates {

        @DisplayName("Should return default at basic repository")
        @ParameterizedTest
        @MethodSource("org.eclipse.jnosql.mapping.core.query.RepositoryTypeTest#getBasicRepositoryMethods")
        void shouldReturnDefaultAtBasicRepository(Method method)  {
            var type = RepositoryType.of(method, BasicRepository.class);
            assertThat(type).isEqualTo(RepositoryType.DEFAULT);
        }
        @DisplayName("Should return default at crud repository")
        @ParameterizedTest
        @MethodSource("org.eclipse.jnosql.mapping.core.query.RepositoryTypeTest#getCrudRepositoryMethods")
        void shouldReturnDefaultAtCrudRepository(Method method)  {
            var type = RepositoryType.of(method, BasicRepository.class);
            assertThat(type).isEqualTo(RepositoryType.DEFAULT);
        }
        @DisplayName("Should return default at pageable repository")
        @ParameterizedTest
        @MethodSource("org.eclipse.jnosql.mapping.core.query.RepositoryTypeTest#getNoSQLRepositoryMethods")
        void shouldReturnDefaultAtPageableRepository(Method method)  {
            var type = RepositoryType.of(method, BasicRepository.class);
            assertThat(type).isEqualTo(RepositoryType.DEFAULT);
        }
        @DisplayName("Should return object method")
        @Test
        void shouldReturnObjectMethod() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(Object.class, "equals"), CrudRepository.class)).isEqualTo(RepositoryType.OBJECT_METHOD);
            assertThat(RepositoryType.of(getMethod(Object.class, "hashCode"), CrudRepository.class)).isEqualTo(RepositoryType.OBJECT_METHOD);
        }
        @DisplayName("Should return find by")
        @Test
        void shouldReturnFindBy() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "findByName"), CrudRepository.class)).isEqualTo(RepositoryType.FIND_BY);
        }
        @DisplayName("Should return find first by")
        @Test
        void shouldReturnFindFirstBy() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "findFirst10ByAge"), CrudRepository.class)).isEqualTo(RepositoryType.FIND_BY);
        }
        @DisplayName("Should return save")
        @Test
        void shouldReturnSave() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "save"), DevRepository.class)).isEqualTo(RepositoryType.SAVE);
        }
        @DisplayName("Should return insert")
        @Test
        void shouldReturnInsert() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "insert"), DevRepository.class)).isEqualTo(RepositoryType.INSERT);
        }
        @DisplayName("Should return delete")
        @Test
        void shouldReturnDelete() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "delete"), DevRepository.class)).isEqualTo(RepositoryType.DELETE);
        }
        @DisplayName("Should return update")
        @Test
        void shouldReturnUpdate() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "update"), DevRepository.class)).isEqualTo(RepositoryType.UPDATE);
        }
        @DisplayName("Should return delete by")
        @Test
        void shouldReturnDeleteBy() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "deleteByName"), CrudRepository.class)).isEqualTo(RepositoryType.DELETE_BY);
        }
        @DisplayName("Should return find all by")
        @Test
        void shouldReturnFindAllBy() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "findAll"), CrudRepository.class)).isEqualTo(RepositoryType.FIND_ALL);
        }
        @DisplayName("Should return jno sqlquery")
        @Test
        void shouldReturnJNoSQLQuery() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "query"), CrudRepository.class)).isEqualTo(RepositoryType.QUERY);
        }
        @DisplayName("Should return unknown")
        @Test
        void shouldReturnUnknown() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "nope"), CrudRepository.class)).isEqualTo(RepositoryType.UNKNOWN);
        }
        @DisplayName("Should return parameter based")
        @Test
        void shouldReturnParameterBased() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "find"), CrudRepository.class)).isEqualTo(RepositoryType.PARAMETER_BASED);
        }
        @DisplayName("Should return parameter based2")
        @Test
        void shouldReturnParameterBased2() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "find2"), CrudRepository.class)).isEqualTo(RepositoryType.PARAMETER_BASED);
        }
        @DisplayName("Should return count by")
        @Test
        void shouldReturnCountBy() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "countByName"), CrudRepository.class)).isEqualTo(RepositoryType.COUNT_BY);
        }
        @DisplayName("Should return count all")
        @Test
        void shouldReturnCountAll() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "countAll"), CrudRepository.class)).isEqualTo(RepositoryType.COUNT_ALL);
        }
        @DisplayName("Should return exists by")
        @Test
        void shouldReturnExistsBy() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "existsByName"), CrudRepository.class)).isEqualTo(RepositoryType.EXISTS_BY);
        }
        @DisplayName("Should return order")
        @Test
        void shouldReturnOrder() throws NoSuchMethodException {

            assertThat(RepositoryType.of(getMethod(DevRepository.class, "order"), CrudRepository.class)).isEqualTo(RepositoryType.UNKNOWN);
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "order2"), CrudRepository.class)).isEqualTo(RepositoryType.UNKNOWN);
        }
        @DisplayName("Should default method")
        @Test
        void shouldDefaultMethod() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class,
                    "duplicate"), CrudRepository.class)).isEqualTo(RepositoryType.DEFAULT_METHOD);
        }
        @DisplayName("Should return custom")
        @Test
        void shouldReturnCustom() throws NoSuchMethodException {
            try (MockedStatic<CDI> cdi = Mockito.mockStatic(CDI.class)) {
                CDI<Object> current = Mockito.mock(CDI.class);
                Instance<Calculate> instance = Mockito.mock(Instance.class);
                Mockito.when(instance.isResolvable()).thenReturn(true);
                cdi.when(CDI::current).thenReturn(current);
                Mockito.when(current.select(Calculate.class)).thenReturn(instance);
                assertThat(RepositoryType.of(getMethod(Calculate.class,
                        "sum"), CrudRepository.class)).isEqualTo(RepositoryType.CUSTOM_REPOSITORY);
            }
        }
        @DisplayName("Should return find by custom")
        @Test
        void shouldReturnFindByCustom() throws NoSuchMethodException {
            try (MockedStatic<CDI> cdi = Mockito.mockStatic(CDI.class)) {
                CDI<Object> current = Mockito.mock(CDI.class);
                Instance<Calculate> instance = Mockito.mock(Instance.class);
                Mockito.when(instance.isResolvable()).thenReturn(true);
                cdi.when(CDI::current).thenReturn(current);
                Mockito.when(current.select(Calculate.class)).thenReturn(instance);
                assertThat(RepositoryType.of(getMethod(Calculate.class,
                        "findBySum"), CrudRepository.class)).isEqualTo(RepositoryType.CUSTOM_REPOSITORY);
            }
        }
        @DisplayName("Should return find by custom2")
        @Test
        void shouldReturnFindByCustom2() throws NoSuchMethodException {
            try (MockedStatic<CDI> cdi = Mockito.mockStatic(CDI.class)) {
                CDI<Object> current = Mockito.mock(CDI.class);
                Instance<Calculate> instance = Mockito.mock(Instance.class);
                Mockito.when(instance.isResolvable()).thenReturn(true);
                cdi.when(CDI::current).thenReturn(current);
                Mockito.when(current.select(Calculate.class)).thenReturn(instance);
                assertThat(RepositoryType.of(getMethod(Calculate.class,
                        "findBySum"), Calculate.class)).isEqualTo(RepositoryType.FIND_BY);
            }
        }
        @DisplayName("Should return find by name order by name")
        @Test
        void shouldReturnFindByNameOrderByName() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "findByNameOrderByName"), CrudRepository.class)).isEqualTo(RepositoryType.CURSOR_PAGINATION);
        }
        @DisplayName("Should find restriction with find")
        @Test
        void shouldFindRestrictionWithFind() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "findRestriction"), CrudRepository.class)).isEqualTo(RepositoryType.PARAMETER_BASED);
        }
        @DisplayName("Should find restriction with query")
        @Test
        void shouldFindRestrictionWithQuery() throws NoSuchMethodException {
            assertThat(RepositoryType.of(getMethod(DevRepository.class, "queryRestriction"), CrudRepository.class)).isEqualTo(RepositoryType.QUERY);
        }
    }
}
