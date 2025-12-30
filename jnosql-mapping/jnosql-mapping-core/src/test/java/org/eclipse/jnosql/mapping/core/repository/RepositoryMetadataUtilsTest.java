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

import jakarta.data.Limit;
import jakarta.data.Order;
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
import jakarta.data.page.PageRequest;
import jakarta.data.restrict.Restriction;
import jakarta.inject.Inject;
import jakarta.nosql.Template;
import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.VetedConverter;
import org.eclipse.jnosql.mapping.core.entities.People;
import org.eclipse.jnosql.mapping.core.entities.Person;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.NameKey;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryParam;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@EnableAutoWeld
@AddPackages(value = Converters.class)
@AddPackages(value = VetedConverter.class)
@AddPackages(value = Reflections.class)
@AddExtensions(ReflectionEntityMetadataExtension.class)
class RepositoryMetadataUtilsTest {

    @Inject
    private RepositoriesMetadata repositoriesMetadata;

    @Inject
    private EntitiesMetadata entitiesMetadata;

    private RepositoryMetadata repositoryMetadata;

    @BeforeEach
    void setUp() {
        repositoryMetadata = repositoriesMetadata.get(People.class)
                .orElseThrow();
    }

    @Test
    @DisplayName("should map param empty")
    void shouldMapParamEmpty() {
        RepositoryMethod method = repositoryMetadata.find(new NameKey("query")).orElseThrow();
        var params = RepositoryMetadataUtils.INSTANCE.getParams(method, new Object[]{});
        Assertions.assertThat(params).isEmpty();
    }

    @Test
    @DisplayName("should map param empty with special parameter")
    void shouldMapParamEmptyWithParameter() {
        RepositoryMethod method = repositoryMetadata.find(new NameKey("query0")).orElseThrow();
        var params = RepositoryMetadataUtils.INSTANCE.getParams(method, new Object[]{
                Limit.of(10),
                PageRequest.ofSize(10)});

        Assertions.assertThat(params).isEmpty();
    }

    @Test
    @DisplayName("should map params by name")
    void shouldMapParamsByName() {
        RepositoryMethod method = repositoryMetadata.find(new NameKey("query1")).orElseThrow();
        var params = RepositoryMetadataUtils.INSTANCE.getParams(method, new Object[]{
                "John",
                PageRequest.ofSize(10)});

        Assertions.assertThat(params)
                .hasSize(2)
                .containsEntry("native", "John")
                .containsEntry("?1", "John");
    }



    @Test
    @DisplayName("should map params by name")
    void shouldMapParamsByName2() {
        RepositoryMethod method = repositoryMetadata.find(new NameKey("query2")).orElseThrow();
        var params = RepositoryMetadataUtils.INSTANCE.getParams(method, new Object[]{
                "John",
                PageRequest.ofSize(10)});

        Assertions.assertThat(params)
                .hasSize(2)
                .containsEntry("arg0", "John")
                .containsEntry("?1", "John");
    }

    @Test
    @DisplayName("should map params with multiple params")
    void shouldMapParamsMultipleParams() {
        RepositoryMethod method = repositoryMetadata.find(new NameKey("query3")).orElseThrow();
        var params = RepositoryMetadataUtils.INSTANCE.getParams(method, new Object[]{
                "John",
                25,
                PageRequest.ofSize(10)});

        Assertions.assertThat(params)
                .hasSize(4)
                .containsEntry("name", "John")
                .containsEntry("?1", "John")
                .containsEntry("?2", 25);
    }

    @Test
    @DisplayName("should execute")
    void shouldExecute() {
        var  method = repositoryMetadata.find(new NameKey("people")).orElseThrow();
        var entityMetadata = entitiesMetadata.findBySimpleName(Person.class.getName()).orElseThrow();
        var template = Mockito.mock(Template.class);
        var context = new RepositoryInvocationContext(method, repositoryMetadata,
                entityMetadata, template, new Object[]{});




    }


    @Nested
    class WhenGetBy {

        @ParameterizedTest
        @DisplayName("should ignore special parameters")
        @ValueSource(classes = {Limit.class, PageRequest.class, Order.class, Restriction.class,
                Sort.class})
        void shouldIgnoreSpecialParameters(Class<?> specialParameter) {
            RepositoryMethod method = Mockito.mock(RepositoryMethod.class);
            RepositoryParam param = Mockito.mock(RepositoryParam.class);
            Mockito.when(param.by()).thenReturn("limit");
            Mockito.doReturn(specialParameter).when(param).type();
            Mockito.when(method.params()).thenReturn(Collections.singletonList(param));

            Map<String, ParamValue> valueMap = RepositoryMetadataUtils.INSTANCE.getBy(method, new Object[]{"value"});
            Assertions.assertThat(valueMap).isEmpty();
        }

        @Test
        @DisplayName("should map equals as default")
        void shouldMapEqualsAsDefault() {
            RepositoryMethod method = Mockito.mock(RepositoryMethod.class);
            RepositoryParam param = Mockito.mock(RepositoryParam.class);
            Mockito.when(param.by()).thenReturn("age");
            Mockito.doReturn(Integer.class).when(param).type();
            Mockito.when(method.params()).thenReturn(Collections.singletonList(param));
            Map<String, ParamValue> valueMap = RepositoryMetadataUtils.INSTANCE.getBy(method, new Object[]{10});

            Assertions.assertThat(valueMap)
                    .hasSize(1)
                    .containsEntry("age", new ParamValue(Condition.EQUALS, 10, false));
        }


        @ParameterizedTest
        @MethodSource("isMappingProvider")
        void shouldMapUsingIsAnnotation(Class<? extends Constraint<?>> constraintType, Condition condition, boolean negate) {
            RepositoryMethod method = Mockito.mock(RepositoryMethod.class);
            RepositoryParam param = Mockito.mock(RepositoryParam.class);
            Mockito.when(param.by()).thenReturn("age");
            Mockito.doReturn(Integer.class).when(param).type();
            Mockito.doReturn(Optional.of(constraintType)).when(param).is();
            Mockito.when(method.params()).thenReturn(Collections.singletonList(param));
            Map<String, ParamValue> valueMap = RepositoryMetadataUtils.INSTANCE.getBy(method, new Object[]{10});

            Assertions.assertThat(valueMap)
                    .hasSize(1)
                    .containsEntry("age", new ParamValue(condition, 10, negate));
        }

        @ParameterizedTest
        @MethodSource("constraintProvider")
        void shouldMapUsingConstraintInstance(Constraint<?> constraint, Condition condition, boolean negate, Object expected) {
            RepositoryMethod method = Mockito.mock(RepositoryMethod.class);
            RepositoryParam param = Mockito.mock(RepositoryParam.class);
            Mockito.when(param.by()).thenReturn("age");
            Mockito.doReturn(Constraint.class).when(param).type();
            Mockito.when(method.params()).thenReturn(Collections.singletonList(param));
            Map<String, ParamValue> valueMap = RepositoryMetadataUtils.INSTANCE.getBy(method, new Object[]{constraint});

            Assertions.assertThat(valueMap)
                    .hasSize(1)
                    .containsEntry("age", new ParamValue(condition, expected, negate));
        }


        private static Stream<Arguments> isMappingProvider() {
            return Stream.of(
                    Arguments.of(EqualTo.class, Condition.EQUALS, false),
                    Arguments.of(AtLeast.class, Condition.GREATER_EQUALS_THAN, false),
                    Arguments.of(AtMost.class, Condition.LESSER_EQUALS_THAN, false),
                    Arguments.of(Between.class, Condition.BETWEEN, false),
                    Arguments.of(GreaterThan.class, Condition.GREATER_THAN, false),
                    Arguments.of(In.class, Condition.IN, false),
                    Arguments.of(LessThan.class, Condition.LESSER_THAN, false),
                    Arguments.of(Like.class, Condition.LIKE, false),

                    Arguments.of(NotBetween.class, Condition.BETWEEN, true),
                    Arguments.of(NotEqualTo.class, Condition.EQUALS, true),
                    Arguments.of(NotIn.class, Condition.IN, true),
                    Arguments.of(NotLike.class, Condition.LIKE, true)
            );
        }

        public static Stream<Arguments> constraintProvider() {
            return Stream.of(
                    Arguments.of(EqualTo.value(10), Condition.EQUALS, false, 10),
                    Arguments.of(AtLeast.min(10), Condition.GREATER_EQUALS_THAN, false, 10),
                    Arguments.of(AtMost.max(10), Condition.LESSER_EQUALS_THAN, false, 10),
                    Arguments.of(Between.bounds(10, 10), Condition.BETWEEN, false, List.of(10, 10)),
                    Arguments.of(GreaterThan.bound(10), Condition.GREATER_THAN, false, 10),
                    Arguments.of(In.values(Collections.singletonList(10)), Condition.IN, false, Collections.singletonList(10)),
                    Arguments.of(LessThan.bound(10), Condition.LESSER_THAN, false, 10),
                    Arguments.of(Like.literal("ada"), Condition.LIKE, false, "ada"),

                    Arguments.of(NotBetween.bounds(10,10), Condition.BETWEEN, true, List.of(10, 10)),
                    Arguments.of(NotEqualTo.value(10), Condition.EQUALS, true, 10),
                    Arguments.of(NotIn.values(Collections.singletonList(10)), Condition.IN, true, Collections.singletonList(10)),
                    Arguments.of(NotLike.literal("ada"), Condition.LIKE, true, "ada")
            );
        }
    }



}