/*
 *  Copyright (c) 2023,2026 Contributors to the Eclipse Foundation
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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.mapping.semistructured.query;

import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.repository.ParamValue;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class SemiStructuredParameterBasedQueryTest {

    @Inject
    private EntitiesMetadata entitiesMetadata;

    @Inject
    private Converters converters;

    private EntityMetadata metadata;

    @BeforeEach
    void setUp() {
        this.metadata = entitiesMetadata.get(Person.class);
    }

    @DisplayName("Should create query single parameter")
    @Test
    void shouldCreateQuerySingleParameter() {
        Map<String, ParamValue> params = Map.of("name", new ParamValue(Condition.EQUALS, "Ada", false));
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, Collections.emptyList(), metadata, converters);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.limit()).isEqualTo(0L);
            soft.assertThat(query.skip()).isEqualTo(0L);
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.condition()).isNotEmpty();
            var condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(query.condition()).get().isEqualTo(CriteriaCondition.eq(Element.of("name", "Ada")));
        });
    }

    @DisplayName("Should create query greater than")
    @Test
    void shouldCreateQueryGreaterThan() {
        Map<String, ParamValue> params = Map.of("name", new ParamValue(Condition.GREATER_THAN, "Ada", false));
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, Collections.emptyList(), metadata, converters);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.limit()).isEqualTo(0L);
            soft.assertThat(query.skip()).isEqualTo(0L);
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.condition()).isNotEmpty();
            var condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(Condition.GREATER_THAN);
            soft.assertThat(condition.element()).isEqualTo(Element.of("name","Ada"));
        });
    }

    @DisplayName("Should update parameter based on simple query")
    @ParameterizedTest(name = "Executing parameter query: {index} - {0}")
    @EnumSource(value = Condition.class, names = {"IN", "BETWEEN", "OR", "AND", "NOT"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldUpdateParameterBasedOnSimpleQuery(Condition condition) {
        Map<String, ParamValue> params = Map.of("name", new ParamValue(condition,"Ada", false));
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, Collections.emptyList(), metadata, converters);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.limit()).isEqualTo(0L);
            soft.assertThat(query.skip()).isEqualTo(0L);
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.condition()).isNotEmpty();
            var criteriaCondition = query.condition().orElseThrow();
            soft.assertThat(criteriaCondition.condition()).isEqualTo(condition);
            soft.assertThat(criteriaCondition.element()).isEqualTo(Element.of("name", "Ada"));
        });
    }

    @DisplayName("Should not allow iterable on simple query")
    @ParameterizedTest(name = "Executing invalid iterable to parameter query: {index} - {0}")
    @EnumSource(value = Condition.class, names = {"IN", "BETWEEN", "OR", "AND", "NOT"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldNotAllowIterableOnSimpleQuery(Condition condition) {
        Map<String, ParamValue> params = Map.of("name", new ParamValue(condition,List.of("Ada"), false));
        Assertions.assertThatThrownBy(() -> SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, Collections.emptyList(), metadata, converters))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("Should not allow array on simple query")
    @ParameterizedTest(name = "Executing invalid array to parameter query: {index} - {0}")
    @EnumSource(value = Condition.class, names = {"IN", "BETWEEN", "OR", "AND", "NOT"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldNotAllowArrayOnSimpleQuery(Condition condition) {
        Map<String, ParamValue> params = Map.of("name", new ParamValue(condition, new String[] {"Ada"}, false));
        Assertions.assertThatThrownBy(() -> SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, Collections.emptyList(), metadata, converters))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("Should not allow not array and iterable")
    @ParameterizedTest(name = "Executing invalid iterable to parameter query: {index} - {0}")
    @EnumSource(value = Condition.class, names = {"IN", "BETWEEN"}, mode = EnumSource.Mode.INCLUDE)
    void shouldNotAllowNotArrayAndIterable(Condition condition) {
        Map<String, ParamValue> params = Map.of("name", new ParamValue(condition,"Ada", false));
        Assertions.assertThatThrownBy(() -> SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, Collections.emptyList(), metadata, converters))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("Should update parameter based on query that needs iterable")
    @ParameterizedTest(name = "Executing parameter query: {index} - {0}")
    @EnumSource(value = Condition.class, names = {"IN", "BETWEEN"}, mode = EnumSource.Mode.INCLUDE)
    void shouldUpdateParameterBasedOnQueryThatNeedsIterable(Condition condition) {
        Map<String, ParamValue> params = Map.of("age", new ParamValue(condition, List.of(10, 20), false));
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, Collections.emptyList(), metadata, converters);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.limit()).isEqualTo(0L);
            soft.assertThat(query.skip()).isEqualTo(0L);
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.condition()).isNotEmpty();
            var criteriaCondition = query.condition().orElseThrow();
            soft.assertThat(criteriaCondition.condition()).isEqualTo(condition);
            soft.assertThat(criteriaCondition.element()).isEqualTo(Element.of("age", List.of(10, 20)));
        });
    }

    @DisplayName("Should not allow between with single value")
    @Test
    void shouldNotAllowBetweenWithSingleValue() {
        Map<String, ParamValue> params = Map.of("age", new ParamValue(Condition.BETWEEN, List.of(10), false));
        Assertions.assertThatThrownBy(() -> SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, Collections.emptyList(), metadata, converters))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("Should update parameter based on query that needs array")
    @ParameterizedTest(name = "Executing parameter query: {index} - {0}")
    @EnumSource(value = Condition.class, names = {"IN", "BETWEEN"}, mode = EnumSource.Mode.INCLUDE)
    void shouldUpdateParameterBasedOnQueryThatNeedsArray(Condition condition) {
        Map<String, ParamValue> params = Map.of("age", new ParamValue(condition, new int[]{10, 20}, false));
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, Collections.emptyList(), metadata, converters);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.limit()).isEqualTo(0L);
            soft.assertThat(query.skip()).isEqualTo(0L);
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.condition()).isNotEmpty();
            var criteriaCondition = query.condition().orElseThrow();
            soft.assertThat(criteriaCondition.condition()).isEqualTo(condition);
            soft.assertThat(criteriaCondition.element()).isEqualTo(Element.of("age", List.of(10, 20)));
        });
    }

    @DisplayName("Should create query single parameter with not")
    @Test
    void shouldCreateQuerySingleParameterWithNot() {
        Map<String, ParamValue> params = Map.of("name", new ParamValue(Condition.EQUALS, "Ada", true));
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, Collections.emptyList(), metadata, converters);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.limit()).isEqualTo(0L);
            soft.assertThat(query.skip()).isEqualTo(0L);
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.condition()).isNotEmpty();
            var condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(Condition.NOT);
            var criteriaCondition = condition.element().get(CriteriaCondition.class);
            soft.assertThat(criteriaCondition).isEqualTo(CriteriaCondition.eq(Element.of("name", "Ada")));
        });
    }

    @DisplayName("Should create query multiple params")
    @Test
    void shouldCreateQueryMultipleParams() {
        Map<String, ParamValue> params = Map.of("name", new ParamValue(Condition.EQUALS, "Ada", false),
                "age", new ParamValue(Condition.EQUALS, 10, false));
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, Collections.emptyList(), metadata, converters);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.limit()).isEqualTo(0L);
            soft.assertThat(query.skip()).isEqualTo(0L);
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.condition()).isNotEmpty();
            var condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(Condition.AND);
            soft.assertThat(condition.element().get(new TypeReference<List<CriteriaCondition>>() {
            })).contains(CriteriaCondition.eq(Element.of("name", "Ada")),
                    CriteriaCondition.eq(Element.of("age", 10)));
        });

    }

    @DisplayName("Should create query empty params")
    @Test
    void shouldCreateQueryEmptyParams() {
        Map<String, ParamValue> params = Collections.emptyMap();
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, Collections.emptyList(), metadata, converters);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.limit()).isEqualTo(0L);
            soft.assertThat(query.skip()).isEqualTo(0L);
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.condition()).isEmpty();
        });
    }

    @DisplayName("Should add sort")
    @Test
    void shouldAddSort() {
        Map<String, ParamValue> params = Collections.emptyMap();
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, List.of(Sort.asc("name")), metadata, converters);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.limit()).isEqualTo(0L);
            soft.assertThat(query.skip()).isEqualTo(0L);
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.sorts()).contains(Sort.asc("name"));
            soft.assertThat(query.condition()).isEmpty();
        });
    }

    @DisplayName("Should create query single parameter native")
    @Test
    void shouldCreateQuerySingleParameterNative() {
        Map<String, Object> params = Map.of("name", "Ada");
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQueryNative(params, Collections.emptyList(), null, metadata);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.limit()).isEqualTo(0L);
            soft.assertThat(query.skip()).isEqualTo(0L);
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.condition()).isNotEmpty();
            soft.assertThat(query.condition()).get().isEqualTo(CriteriaCondition.eq(Element.of("name", "Ada")));
        });
    }

    @DisplayName("Should create query multiple params native")
    @Test
    void shouldCreateQueryMultipleParamsNative() {
        Map<String, Object> params = Map.of("name", "Ada", "age", 10);
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQueryNative(params, Collections.emptyList(), null, metadata);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.limit()).isEqualTo(0L);
            soft.assertThat(query.skip()).isEqualTo(0L);
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.condition()).isNotEmpty();
            var condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(Condition.AND);
            soft.assertThat(condition.element().get(new TypeReference<List<CriteriaCondition>>() {
            })).contains(CriteriaCondition.eq(Element.of("name", "Ada")),
                    CriteriaCondition.eq(Element.of("age", 10)));
        });

    }

    @DisplayName("Should create query empty params native")
    @Test
    void shouldCreateQueryEmptyParamsNative() {
        Map<String, Object> params = Collections.emptyMap();
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQueryNative(params, Collections.emptyList(), null, metadata);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.limit()).isEqualTo(0L);
            soft.assertThat(query.skip()).isEqualTo(0L);
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.condition()).isEmpty();
        });
    }

    @DisplayName("Should add sort native")
    @Test
    void shouldAddSortNative() {
        Map<String, Object> params = Collections.emptyMap();
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQueryNative(params, List.of(Sort.asc("name")), null, metadata);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.limit()).isEqualTo(0L);
            soft.assertThat(query.skip()).isEqualTo(0L);
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.sorts()).contains(Sort.asc("name"));
            soft.assertThat(query.condition()).isEmpty();
        });
    }

    @DisplayName("Should include page request")
    @Test
    void shouldIncludePageRequest(){
        Map<String, Object> params = Collections.emptyMap();
        PageRequest pageRequest = PageRequest.ofPage(2).size(10);
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQueryNative(params, Collections.emptyList(), pageRequest, metadata);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.limit()).isEqualTo(10L);
            soft.assertThat(query.skip()).isEqualTo(10L);
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.condition()).isEmpty();
        });
    }

    @Nested
    @DisplayName("When the semi structured parameter based query is tested")
    class WhenTheSemiStructuredParameterBasedQueryIsTested {
    }
}