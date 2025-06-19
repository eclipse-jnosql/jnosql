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
package org.eclipse.jnosql.mapping.semistructured.query;

import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.OrderBy;
import jakarta.data.restrict.Restrict;
import jakarta.data.restrict.Restriction;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.PreparedStatement;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.eclipse.jnosql.mapping.semistructured.entities.Product;
import org.eclipse.jnosql.mapping.semistructured.entities.Task;
import org.eclipse.jnosql.mapping.semistructured.entities._Product;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.eclipse.jnosql.communication.Condition.EQUALS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class CustomRepositoryHandlerRestrictionTest {

    @Inject
    private EntitiesMetadata entitiesMetadata;

    private SemiStructuredTemplate template;

    @Inject
    private Converters converters;

    private ProductRepository repository;


    @BeforeEach
    void setUp() {
        template = Mockito.mock(SemiStructuredTemplate.class);
        CustomRepositoryHandler customRepositoryHandlerForPeople = CustomRepositoryHandler.builder()
                .entitiesMetadata(entitiesMetadata)
                .template(template)
                .customRepositoryType(ProductRepository.class)
                .converters(converters).build();

        repository = (ProductRepository) Proxy.newProxyInstance(ProductRepository.class.getClassLoader(), new Class[]{ProductRepository.class},
                customRepositoryHandlerForPeople);
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

    public interface ProductRepository {
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
    }
}