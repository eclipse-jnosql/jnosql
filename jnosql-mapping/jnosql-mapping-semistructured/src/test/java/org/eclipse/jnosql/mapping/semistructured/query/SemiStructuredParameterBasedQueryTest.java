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
package org.eclipse.jnosql.mapping.semistructured.query;

import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.mapping.core.repository.ParamValue;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class SemiStructuredParameterBasedQueryTest {

    @Inject
    private EntitiesMetadata entitiesMetadata;

    private EntityMetadata metadata;

    @BeforeEach
    void setUp() {
        this.metadata = entitiesMetadata.get(Person.class);
    }

    @Test
    void shouldCreateQuerySingleParameter() {
        Map<String, ParamValue> params = Map.of("name", new ParamValue(Condition.EQUALS, "Ada", false));
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, Collections.emptyList(), metadata);

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

    @Test
    void shouldCreateQueryGreaterThan() {
        Map<String, ParamValue> params = Map.of("name", new ParamValue(Condition.GREATER_THAN, "Ada", false));
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, Collections.emptyList(), metadata);

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

    @Test
    void shouldCreateQuerySingleParameterWithNot() {
        Map<String, ParamValue> params = Map.of("name", new ParamValue(Condition.EQUALS, "Ada", true));
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, Collections.emptyList(), metadata);

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

    @Test
    void shouldCreateQueryMultipleParams() {
        Map<String, ParamValue> params = Map.of("name", new ParamValue(Condition.EQUALS, "Ada", false),
                "age", new ParamValue(Condition.EQUALS, 10, false));
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, Collections.emptyList(), metadata);

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

    @Test
    void shouldCreateQueryEmptyParams() {
        Map<String, ParamValue> params = Collections.emptyMap();
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, Collections.emptyList(), metadata);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.limit()).isEqualTo(0L);
            soft.assertThat(query.skip()).isEqualTo(0L);
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.condition()).isEmpty();
        });
    }

    @Test
    void shouldAddSort() {
        Map<String, ParamValue> params = Collections.emptyMap();
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(params, List.of(Sort.asc("name")), metadata);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.limit()).isEqualTo(0L);
            soft.assertThat(query.skip()).isEqualTo(0L);
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.sorts()).contains(Sort.asc("name"));
            soft.assertThat(query.condition()).isEmpty();
        });
    }

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
}