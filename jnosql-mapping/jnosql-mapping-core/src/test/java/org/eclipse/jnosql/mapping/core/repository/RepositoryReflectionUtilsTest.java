/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import jakarta.data.Sort;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.By;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import org.eclipse.jnosql.mapping.core.entities.Person;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RepositoryReflectionUtilsTest {

    final Class<?> PERSON_REPOSITORY_COMPILED_WITH_PARAMETERS_CLASS;

    {
        try {
            PERSON_REPOSITORY_COMPILED_WITH_PARAMETERS_CLASS = Class.forName(this.getClass().getPackageName() + ".PersonRepositoryCompiledWithParameters");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    void shouldGetParamsWithoutSpecialParams() {
        Method method = Arrays.stream(PersonRepository.class.getDeclaredMethods()).filter(m -> m.getName().equals("query"))
                .findFirst().orElseThrow();
        final Sort<Object> SPECIAL_PARAM = Sort.asc("");
        Map<String, Object> params = RepositoryReflectionUtils.INSTANCE.getParams(method, new Object[]{"Ada", SPECIAL_PARAM});
        assertThat(params)
                .hasSize(1)
                .containsEntry("name", "Ada");

    }

    @Test
    void shouldQuery() {
        Method method = Arrays.stream(PersonRepository.class.getDeclaredMethods()).filter(m -> m.getName().equals("query"))
                .findFirst().orElseThrow();
        String query = RepositoryReflectionUtils.INSTANCE.getQuery(method);
        assertEquals("FROM Person WHERE name = :name", query);
    }

    @Test
    void shouldByWithoutSpecialParams() {
        Method method = Arrays.stream(PersonRepository.class.getDeclaredMethods()).filter(m -> m.getName().equals("query"))
                .findFirst().orElseThrow();
        final Sort<Object> SPECIAL_PARAM = Sort.asc("");
        Map<String, Object> params = RepositoryReflectionUtils.INSTANCE.getBy(method, new Object[]{"Ada", SPECIAL_PARAM});
        assertThat(params)
                .hasSize(1)
                .containsEntry("name", "Ada");
    }

    @Test
    // for code compiled without -parameters
    void shouldFindByAgeWithoutParams() {
        Method method = Stream.of(PersonRepository.class.getDeclaredMethods()).filter(m -> m.getName().equals("findAge"))
                .findFirst().orElseThrow();
        Map<String, Object> params = RepositoryReflectionUtils.INSTANCE.getParams(method, new Object[]{10});
        assertThat(method.getParameters()[0].isNamePresent()).isFalse();
        assertThat(params)
                .hasSize(1)
                .containsEntry("?1", 10);
    }

    @Test
    // for code compiled with -parameters
    void shouldFindByAgeWithParams() throws ClassNotFoundException {
        Method method = Stream.of(PERSON_REPOSITORY_COMPILED_WITH_PARAMETERS_CLASS.getDeclaredMethods()).filter(m -> m.getName().equals("findAge"))
                .findFirst().orElseThrow();
        Map<String, Object> params = RepositoryReflectionUtils.INSTANCE.getParams(method, new Object[]{10});
        assertThat(method.getParameters()[0].isNamePresent()).isTrue();
        assertThat(params)
                .hasSize(2)
                .containsEntry("?1", 10)
                .containsEntry("age", 10);
    }

    @Test
    // for code compiled without -parameters
    void shouldFindByAgeAndNameWithoutParams() {
        Method method = Stream.of(PersonRepository.class.getDeclaredMethods()).filter(m -> m.getName().equals("findAgeAndName"))
                .findFirst().orElseThrow();
        Map<String, Object> params = RepositoryReflectionUtils.INSTANCE.getParams(method, new Object[]{10, "Ada"});
        assertThat(params)
                .hasSize(2)
                .containsEntry("?1", 10)
                .containsEntry("?2", "Ada");
    }

    @Test
    // for code compiled with -parameters
    void shouldFindByAgeAndNameWithParams() {
        Method method = Stream.of(PERSON_REPOSITORY_COMPILED_WITH_PARAMETERS_CLASS.getDeclaredMethods()).filter(m -> m.getName().equals("findAgeAndName"))
                .findFirst().orElseThrow();
        Map<String, Object> params = RepositoryReflectionUtils.INSTANCE.getParams(method, new Object[]{10, "Ada"});
        assertThat(params)
                .hasSize(4)
                .containsEntry("?1", 10)
                .containsEntry("?2", "Ada")
                .containsEntry("age", 10)
                .containsEntry("name", "Ada");
    }

    interface PersonRepository extends BasicRepository<Person, String> {

        @Query("FROM Person WHERE name = :name")
        List<Person> query(@Param("name") @By("name")  String name, Sort sort);

        @Query("FROM Person WHERE age = ?1")
        List<Person> findAge(int age);

        @Query("FROM Person WHERE age = ?1 AND name = ?2")
        List<Person> findAgeAndName(int age, String name);
    }
}
