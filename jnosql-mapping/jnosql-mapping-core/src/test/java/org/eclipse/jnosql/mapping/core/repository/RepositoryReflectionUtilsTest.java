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
import jakarta.data.constraint.AtLeast;
import jakarta.data.constraint.AtMost;
import jakarta.data.constraint.Between;
import jakarta.data.constraint.Constraint;
import jakarta.data.constraint.EqualTo;
import jakarta.data.constraint.GreaterThan;
import jakarta.data.constraint.In;
import jakarta.data.constraint.LessThan;
import jakarta.data.constraint.Like;
import jakarta.data.constraint.NotBetween;
import jakarta.data.constraint.NotEqualTo;
import jakarta.data.constraint.NotIn;
import jakarta.data.constraint.NotLike;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.By;
import jakarta.data.repository.Is;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.mapping.core.entities.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

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
        Map<String, ParamValue> params = RepositoryReflectionUtils.INSTANCE.getBy(method, new Object[]{"Ada", SPECIAL_PARAM});
        assertThat(params)
                .hasSize(1)
                .containsEntry("name", new ParamValue(Condition.EQUALS, "Ada", false));
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


    @ParameterizedTest(name = "Testing positive {index} - {0}")
    @ValueSource(classes = {AtLeast.class, AtMost.class, GreaterThan.class, LessThan.class, Between.class,
            EqualTo.class, Like.class, In.class})
    void shouldGetParamValueByPositive(Class<? extends Constraint<?>> constraint) {
        ParamValue paramValue = RepositoryReflectionUtils.getParamValue("name", constraint);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(paramValue.value()).isEqualTo("name");
            softly.assertThat(paramValue.negate()).isFalse();
        });
    }

    @ParameterizedTest(name = "Negative positive {index} - {0}")
    @ValueSource(classes = {NotBetween.class, NotEqualTo.class, NotIn.class, NotLike.class})
    void shouldGetParamValueByNegative(Class<? extends Constraint<?>> constraint) {
        ParamValue paramValue = RepositoryReflectionUtils.getParamValue("name", constraint);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(paramValue.value()).isEqualTo("name");
            softly.assertThat(paramValue.negate()).isTrue();
        });
    }


    @ParameterizedTest(name = "Testing condition {index} - {0}")
    @MethodSource("conditions")
    void shouldReturnParam(Class<? extends Constraint<?>> constraint, boolean isNegate, Condition condition) {
        ParamValue paramValue = RepositoryReflectionUtils.getParamValue("name", constraint);

        SoftAssertions.assertSoftly(softly -> {
           softly.assertThat(paramValue.condition()).isEqualTo(condition);
              softly.assertThat(paramValue.negate()).isEqualTo(isNegate);
              softly.assertThat(paramValue.value()).isEqualTo("name");
        });
    }

    @ParameterizedTest(name = "Testing condition {index} - {1} and the result: {2}")
    @MethodSource("conditionsInstances")
    void shouldReturnParamWithInstances(boolean isNegate, Condition condition, Constraint<?> constraint, Object value) {
        ParamValue paramValue = RepositoryReflectionUtils.getParamValue(constraint, EqualTo.class);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(paramValue.condition()).isEqualTo(condition);
            softly.assertThat(paramValue.negate()).isEqualTo(isNegate);
            softly.assertThat(paramValue.value()).isEqualTo(value);
        });
    }

    @Test
    @DisplayName("Should create ParamValueEquals when the value is null")
    void shouldCreateParamValueEqualsWhenIsNull(){
        var param = RepositoryReflectionUtils.INSTANCE.condition(null, "name");
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(param).isNotNull();
            softly.assertThat(param.value()).isEqualTo("name");
            softly.assertThat(param.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(param.negate()).isFalse();
        });
    }

    @Test
    @DisplayName("Should use the isParamValue")
    void shouldUseTheIsParamValue() {
        Is is = new Is() {
            @Override
            public Class<? extends Constraint> value() {
                return Like.class;
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return Is.class;
            }
        };

        var param = RepositoryReflectionUtils.INSTANCE.condition(is, "name");
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(param).isNotNull();
            softly.assertThat(param.value()).isEqualTo("name");
            softly.assertThat(param.condition()).isEqualTo(Condition.LIKE);
            softly.assertThat(param.negate()).isFalse();
        });
    }

    public static Stream<Arguments> conditions() {
        return Stream.of(
                Arguments.of(AtLeast.class, false, Condition.GREATER_EQUALS_THAN),
                Arguments.of(AtMost.class, false, Condition.LESSER_EQUALS_THAN),
                Arguments.of(GreaterThan.class, false, Condition.GREATER_THAN),
                Arguments.of(LessThan.class, false, Condition.LESSER_THAN),
                Arguments.of(Between.class, false, Condition.BETWEEN),
                Arguments.of(EqualTo.class, false, Condition.EQUALS),
                Arguments.of(Like.class, false, Condition.LIKE),
                Arguments.of(In.class, false, Condition.IN),
                Arguments.of(NotBetween.class, true, Condition.BETWEEN),
                Arguments.of(NotEqualTo.class, true, Condition.EQUALS),
                Arguments.of(NotIn.class, true, Condition.IN),
                Arguments.of(NotLike.class, true, Condition.LIKE)
        );
    }


    public static Stream<Arguments> conditionsInstances() {
        return Stream.of(
                Arguments.of(false, Condition.GREATER_EQUALS_THAN, AtLeast.min(10), 10),
                Arguments.of(false, Condition.LESSER_EQUALS_THAN, AtMost.max(10), 10),
                Arguments.of(false, Condition.GREATER_THAN,GreaterThan.bound(10), 10),
                Arguments.of(false, Condition.LESSER_THAN, LessThan.bound(10), 10),
                Arguments.of(false, Condition.BETWEEN, Between.bounds(10, 20), List.of(10, 20)),
                Arguments.of(false, Condition.EQUALS, EqualTo.value(10), 10),
                Arguments.of(false, Condition.LIKE, Like.literal("name"), "name"),
                Arguments.of(false, Condition.IN, In.values(10, 20), List.of(10, 20)),
                Arguments.of(true, Condition.BETWEEN, NotBetween.bounds(10, 20), List.of(10, 20)),
                Arguments.of(true, Condition.EQUALS, NotEqualTo.value(10), 10),
                Arguments.of(true, Condition.IN, NotIn.values(10, 20), List.of(10, 20)),
                Arguments.of(true, Condition.LIKE, NotLike.literal("name"), "name")
        );
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
