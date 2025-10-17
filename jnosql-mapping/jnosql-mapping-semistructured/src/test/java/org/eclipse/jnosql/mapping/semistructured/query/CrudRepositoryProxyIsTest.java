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
package org.eclipse.jnosql.mapping.semistructured.query;

import jakarta.data.constraint.GreaterThan;
import jakarta.data.constraint.In;
import jakarta.data.constraint.LessThan;
import jakarta.data.constraint.NotIn;
import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Is;
import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.entities.Product;
import org.eclipse.jnosql.mapping.semistructured.entities._Product;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.eclipse.jnosql.communication.Condition.EQUALS;
import static org.eclipse.jnosql.communication.Condition.GREATER_THAN;
import static org.eclipse.jnosql.communication.Condition.IN;
import static org.eclipse.jnosql.communication.Condition.LESSER_THAN;
import static org.eclipse.jnosql.communication.Condition.NOT;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class CrudRepositoryProxyIsTest {

    private SemiStructuredTemplate template;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    private ProductRepository repository;


    @BeforeEach
    public void setUp() {
        this.template = Mockito.mock(SemiStructuredTemplate.class);

        var productHandler = new SemiStructuredRepositoryProxy<>(template,
                entities, ProductRepository.class, converters);



        repository = (ProductRepository) Proxy.newProxyInstance(ProductRepository.class.getClassLoader(),
                new Class[]{ProductRepository.class},
                productHandler);
    }


    @Test
    void shouldEquals() {

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(new Product()));

        repository.equals("Mac");
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(query.name()).isEqualTo("Product");
            softly.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            softly.assertThat(condition).isInstanceOf(CriteriaCondition.class);
            softly.assertThat(condition.condition()).isEqualTo(EQUALS);
            softly.assertThat(condition.element()).isEqualTo(Element.of(_Product.NAME, "Mac"));
        });
    }


    @Test
    void shouldDefaultMethod() {

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(new Product()));

        repository.defaultMethod("Mac");
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(query.name()).isEqualTo("Product");
            softly.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            softly.assertThat(condition).isInstanceOf(CriteriaCondition.class);
            softly.assertThat(condition.condition()).isEqualTo(EQUALS);
            softly.assertThat(condition.element()).isEqualTo(Element.of(_Product.NAME, "Mac"));
        });
    }

    @Test
    void shouldAtLeast() {

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(new Product()));

        repository.greaterThan(BigDecimal.TEN);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(query.name()).isEqualTo("Product");
            softly.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            softly.assertThat(condition).isInstanceOf(CriteriaCondition.class);
            softly.assertThat(condition.condition()).isEqualTo(GREATER_THAN);
            softly.assertThat(condition.element()).isEqualTo(Element.of(_Product.PRICE, BigDecimal.TEN));
        });
    }

    @Test
    void shouldLesser() {

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(new Product()));

        repository.lesserThan(BigDecimal.TEN);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(query.name()).isEqualTo("Product");
            softly.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            softly.assertThat(condition).isInstanceOf(CriteriaCondition.class);
            softly.assertThat(condition.condition()).isEqualTo(LESSER_THAN);
            softly.assertThat(condition.element()).isEqualTo(Element.of(_Product.PRICE, BigDecimal.TEN));
        });
    }

    @Test
    void shouldIn() {

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(new Product()));

        repository.in(List.of("Mac", "Iphone"));
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(query.name()).isEqualTo("Product");
            softly.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            softly.assertThat(condition).isInstanceOf(CriteriaCondition.class);
            softly.assertThat(condition.condition()).isEqualTo(IN);
            softly.assertThat(condition.element()).isEqualTo(Element.of(_Product.NAME, List.of("Mac", "Iphone")));
        });
    }

    @Test
    void shouldNotIn() {

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(new Product()));

        repository.notIn(List.of("Mac", "Iphone"));
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(query.name()).isEqualTo("Product");
            softly.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            softly.assertThat(condition).isInstanceOf(CriteriaCondition.class);
            softly.assertThat(condition.condition()).isEqualTo(NOT);
            var criteriaCondition = condition.element().get(CriteriaCondition.class);
            softly.assertThat(criteriaCondition.condition()).isEqualTo(IN);
            softly.assertThat(criteriaCondition.element()).isEqualTo(Element.of(_Product.NAME, List.of("Mac", "Iphone")));
        });
    }



    public interface ProductRepository extends CrudRepository<Product, String> {
        @Find
        List<Product> defaultMethod(@By(_Product.NAME) String name);

        @Find
        List<Product> equals(@By(_Product.NAME) @Is String name);

        @Find
        List<Product> greaterThan(@By(_Product.PRICE) @Is(GreaterThan.class) BigDecimal price);

        @Find
        List<Product> lesserThan(@By(_Product.PRICE) @Is(LessThan.class) BigDecimal price);

        @Find
        List<Product> in(@By(_Product.NAME) @Is(In.class) List<String> names);

        @Find
        List<Product> notIn(@By(_Product.NAME) @Is(NotIn.class) List<String> names);
    }

}
