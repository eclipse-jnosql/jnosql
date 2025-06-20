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

import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.restrict.Restriction;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.eclipse.jnosql.mapping.PreparedStatement;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.entities.Address;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.eclipse.jnosql.mapping.semistructured.entities.Product;
import org.eclipse.jnosql.mapping.semistructured.entities.Vendor;
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
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.communication.Condition.AND;
import static org.eclipse.jnosql.communication.Condition.BETWEEN;
import static org.eclipse.jnosql.communication.Condition.EQUALS;
import static org.eclipse.jnosql.communication.Condition.GREATER_THAN;
import static org.eclipse.jnosql.communication.Condition.IN;
import static org.eclipse.jnosql.communication.Condition.LESSER_EQUALS_THAN;
import static org.eclipse.jnosql.communication.Condition.LESSER_THAN;
import static org.eclipse.jnosql.communication.Condition.LIKE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class CrudRepositoryProxyRestrictionTest {

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
    void shouldRestrict() {

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(new Product()));

        var products = repository.restriction(_Product.name.equalTo("Mac"));
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
    void shouldRestrictPage() {

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(new Product()));

        Page<Product> products = repository.restriction(_Product.name.equalTo("Mac"), PageRequest.ofSize(2));
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
            softly.assertThat(products.pageRequest()).isEqualTo(PageRequest.ofSize(2));
            softly.assertThat(products.nextPageRequest()).isEqualTo(PageRequest.ofSize(2).page(2));
        });
    }

    @Test
    void shouldRestrictSort() {

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(new Product()));

        repository.restriction(_Product.name.equalTo("Mac"), _Product.name.asc());
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
            softly.assertThat(query.sorts()).hasSize(1);
            softly.assertThat(query.sorts()).contains( _Product.name.asc());
        });
    }
    @Test
    void shouldRestrictOrder() {

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(new Product()));

        repository.restriction(_Product.name.equalTo("Mac"), Order.by(_Product.name.asc(), _Product.price.asc()));
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
            softly.assertThat(query.sorts()).hasSize(2);
            softly.assertThat(query.sorts()).contains(_Product.name.asc(), _Product.price.asc());
        });
    }


    @Test
    void shouldRestrictSortByAnnotation() {

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(new Product()));

        repository.restrictionOrderByPriceAsc(_Product.name.equalTo("Mac"));
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
            softly.assertThat(query.sorts()).hasSize(1);
            softly.assertThat(query.sorts()).contains( _Product.price.asc());
        });
    }

    @SuppressWarnings("rawtypes")
    @Test
    void shouldHaveCursor() {

        CursoredPage mock = Mockito.mock(CursoredPage.class);
        when(mock.content()).thenReturn(List.of(new Product()));

        when(template.selectCursor(any(SelectQuery.class), any(PageRequest.class))).thenReturn(mock);

        CursoredPage<Product> cursor = repository.cursor(_Product.name.equalTo("Mac"), PageRequest.ofSize(10));
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).selectCursor(captor.capture(), Mockito.any(PageRequest.class));
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(query.name()).isEqualTo("Product");
            softly.assertThat(query.condition()).isPresent();
            softly.assertThat(cursor.content()).isNotEmpty().isNotNull();
            CriteriaCondition condition = query.condition().orElseThrow();
            softly.assertThat(condition).isInstanceOf(CriteriaCondition.class);
            softly.assertThat(condition.condition()).isEqualTo(EQUALS);
            softly.assertThat(condition.element()).isEqualTo(Element.of(_Product.NAME, "Mac"));
            softly.assertThat(query.sorts()).hasSize(1);
            softly.assertThat(query.sorts()).contains( _Product.price.asc());
        });
    }

    @Test
    void shouldShouldCombineRestrictionWithFind() {

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(new Product()));

        List<Product> products = repository.findAll("Mac", _Product.price.greaterThan(BigDecimal.TEN));
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(query.name()).isEqualTo("Product");
            softly.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            softly.assertThat(condition).isInstanceOf(CriteriaCondition.class);
            softly.assertThat(condition.condition()).isEqualTo(AND);
            List<CriteriaCondition> conditions = condition.element().get(new TypeReference<>() {
            });
            softly.assertThat(conditions).hasSize(2);
            CriteriaCondition equals = conditions.get(0);
            CriteriaCondition greaterThan = conditions.get(1);
            softly.assertThat(equals.element()).isEqualTo(Element.of(_Product.NAME, "Mac"));
            softly.assertThat(greaterThan.element()).isEqualTo(Element.of(_Product.PRICE,BigDecimal.TEN));
            softly.assertThat(equals.condition()).isEqualTo(EQUALS);
            softly.assertThat(greaterThan.condition()).isEqualTo(GREATER_THAN);
            softly.assertThat(products).hasSize(1);
        });
    }

    @Test
    void shouldShouldCombineRestrictionWithQuery() {

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(new Product()));
        when(template.prepare(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class));
        List<Product> products = repository.query("Mac", _Product.price.greaterThan(BigDecimal.TEN));

    }

    @Test
    void shouldDelete() {

        repository.delete(_Product.price.greaterThan(BigDecimal.TEN));

        ArgumentCaptor<DeleteQuery> captor = ArgumentCaptor.forClass(DeleteQuery.class);
        verify(template).delete(captor.capture());

        SoftAssertions.assertSoftly(softly -> {
            DeleteQuery value = captor.getValue();
            softly.assertThat(value.name()).isEqualTo("Product");
            softly.assertThat(value.condition()).isPresent();
            CriteriaCondition condition = value.condition().orElseThrow();
            softly.assertThat(condition).isInstanceOf(CriteriaCondition.class);
            softly.assertThat(condition.condition()).isEqualTo(GREATER_THAN);
            softly.assertThat(condition.element()).isEqualTo(Element.of(_Product.PRICE, BigDecimal.TEN));
        });

    }


    public interface ProductRepository extends CrudRepository<Product, String> {
        @Find
        List<Product> restriction(Restriction<Product> restriction);
        @Find
        Page<Product> restriction(Restriction<Product> restriction, PageRequest pageRequest);

        @Find
        List<Product> restriction(Restriction<Product> restriction, Sort<Product> order);
        @Find
        List<Product> restriction(Restriction<Product> restriction, Order<Product> order);

        @OrderBy(_Product.PRICE)
        @Find
        List<Product> restrictionOrderByPriceAsc(Restriction<Product> restriction);

        @OrderBy(_Product.PRICE)
        @Find
        CursoredPage<Product> cursor(Restriction<Product> restriction, PageRequest pageRequest);


        @Find
        List<Product> findAll(@By("name") String name, Restriction<Product> restriction);

        @Query("where name = :name")
        List<Product> query(@Param("name") String name, Restriction<Product> restriction);

        @Delete
        void delete(Restriction<Product> restriction);
    }
}
