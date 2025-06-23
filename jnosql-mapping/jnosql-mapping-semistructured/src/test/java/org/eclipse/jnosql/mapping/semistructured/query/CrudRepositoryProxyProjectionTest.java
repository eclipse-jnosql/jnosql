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

import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.page.impl.CursoredPageRecord;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Query;
import jakarta.data.repository.Select;
import jakarta.data.restrict.Restriction;
import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
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
import org.eclipse.jnosql.mapping.semistructured.entities.Citizen;
import org.eclipse.jnosql.mapping.semistructured.entities.City;
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
import java.util.Optional;
import java.util.stream.Stream;

import static org.eclipse.jnosql.communication.Condition.EQUALS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class CrudRepositoryProxyProjectionTest {

    private SemiStructuredTemplate template;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    private ProductRepository productRepository;

    private CitizenRepository citizenRepository;


    @BeforeEach
    public void setUp() {
        this.template = Mockito.mock(SemiStructuredTemplate.class);

        var productHandler = new SemiStructuredRepositoryProxy<>(template,
                entities, ProductRepository.class, converters);

        var cityHandler = new SemiStructuredRepositoryProxy<>(template,
                entities, CitizenRepository.class, converters);


        productRepository = (ProductRepository) Proxy.newProxyInstance(ProductRepository.class.getClassLoader(),
                new Class[]{ProductRepository.class},
                productHandler);

        citizenRepository = (CitizenRepository) Proxy.newProxyInstance(ProductRepository.class.getClassLoader(),
                new Class[]{CitizenRepository.class},
                cityHandler);
    }


    @Test
    void shouldReturnNamesOnly() {

        var mac = new Product();
        mac.setName("Mac");
        mac.setPrice(BigDecimal.valueOf(1000));
        mac.setType(Product.ProductType.ELECTRONICS);

        var sofa = new Product();
        sofa.setName("Sofa");
        sofa.setPrice(BigDecimal.valueOf(100));
        sofa.setType(Product.ProductType.FURNITURE);

        var tshirt = new Product();
        tshirt.setName("T-Shirt");
        tshirt.setPrice(BigDecimal.valueOf(20));
        tshirt.setType(Product.ProductType.CLOTHING);

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(mac, sofa, tshirt));

        var productNames = productRepository.names(_Product.name.equalTo("Mac"));
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

            softly.assertThat(productNames).contains("Mac", "Sofa", "T-Shirt");
        });

    }

    @Test
    void shouldReturnName() {

        var mac = new Product();
        mac.setName("Mac");
        mac.setPrice(BigDecimal.valueOf(1000));
        mac.setType(Product.ProductType.ELECTRONICS);

        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional.of(mac));

        var name = productRepository.name();

        SoftAssertions.assertSoftly(softly -> softly.assertThat(name).isEqualTo("Mac"));

    }

    @Test
    void shouldReturnOptionalName() {

        var mac = new Product();
        mac.setName("Mac");
        mac.setPrice(BigDecimal.valueOf(1000));
        mac.setType(Product.ProductType.ELECTRONICS);

        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional.of(mac));

        var name = productRepository.optionalName();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(name).isPresent();
            softly.assertThat(name).get().isEqualTo("Mac");
        });

    }

    @Test
    void shouldReturnPage() {

        var mac = new Product();
        mac.setName("Mac");
        mac.setPrice(BigDecimal.valueOf(1000));
        mac.setType(Product.ProductType.ELECTRONICS);

        when(template.select(any(SelectQuery.class))).thenReturn(Stream.of(mac));
        ;
        var name = productRepository.page(PageRequest.ofSize(10));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(name).isNotNull();
            softly.assertThat(name.content()).containsExactly("Mac");
        });

    }

    @Test
    void shouldReturnCursor() {

        Product mac = new Product();
        mac.setName("Mac");
        mac.setPrice(BigDecimal.valueOf(1000));
        mac.setType(Product.ProductType.ELECTRONICS);

        List<Product> content = List.of(mac);
        List<PageRequest.Cursor> cursors= List.of(PageRequest.Cursor.forKey(1, 2, 3));
        PageRequest pageRequest= PageRequest.ofSize(10);
        boolean nextPageRequest= false;
        boolean previousPageRequest= false;

        CursoredPage<Product> cursor = new CursoredPageRecord<>(content, cursors, 1L, pageRequest, nextPageRequest, previousPageRequest);
        when(template.<Product>selectCursor(any(SelectQuery.class), any())).thenReturn(cursor);

        var name = productRepository.cursor(PageRequest.ofSize(10).afterCursor(PageRequest.Cursor.forKey("1", "2")));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(name).isNotNull();
            softly.assertThat(name.content()).containsExactly("Mac");
        });

    }

    @Test
    void shouldNameFromQuery() {

        var mac = new Product();
        mac.setName("Mac");
        mac.setPrice(BigDecimal.valueOf(1000));
        mac.setType(Product.ProductType.ELECTRONICS);

        var sofa = new Product();
        sofa.setName("Sofa");
        sofa.setPrice(BigDecimal.valueOf(100));
        sofa.setType(Product.ProductType.FURNITURE);

        var tshirt = new Product();
        tshirt.setName("T-Shirt");
        tshirt.setPrice(BigDecimal.valueOf(20));
        tshirt.setType(Product.ProductType.CLOTHING);

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(mac, sofa, tshirt));

        PreparedStatement prepare = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        when(prepare.result()).thenReturn(Stream.of(mac, sofa, tshirt));

        when(template.prepare(Mockito.anyString(), Mockito.anyString())).thenReturn(prepare);

        var productNames = productRepository.query();

        SoftAssertions.assertSoftly(softly -> softly.assertThat(productNames).contains("Mac", "Sofa", "T-Shirt"));
    }

    @Test
    void shouldKeepTheSameResult() {

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of("Mac", "Sofa", "T-Shirt"));

        PreparedStatement prepare = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        when(prepare.result()).thenReturn(Stream.of("Mac", "Sofa", "T-Shirt"));

        when(template.prepare(Mockito.anyString(), Mockito.anyString())).thenReturn(prepare);

        var productNames = productRepository.query();

        SoftAssertions.assertSoftly(softly -> softly.assertThat(productNames).contains("Mac", "Sofa", "T-Shirt"));
    }

    @Test
    void shouldReturnCities() {

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(Citizen.of("1", "John Doe", City.of("1", "New York")),
                        Citizen.of("2", "Ada Doe", City.of("2", "London"))));

        var cities = citizenRepository.cities();
        SoftAssertions.assertSoftly(softly -> softly.assertThat(cities).contains("New York", "London"));
    }


    public interface ProductRepository extends CrudRepository<Product, String> {
        @Find
        @Select(_Product.NAME)
        List<String> names(Restriction<Product> restriction);

        @Find
        @Select(_Product.NAME)
        String name();

        @Find
        @Select(_Product.NAME)
        Optional<String> optionalName();

        @Find
        @Select(_Product.NAME)
        Page<String> page(PageRequest pageRequest);

        @Find
        @Select(_Product.NAME)
        CursoredPage<String> cursor(PageRequest pageRequest);

        @Query("FROM Product p WHERE p.type = 'ELECTRONICS'")
        @Select(_Product.NAME)
        List<String> query();
    }

    public interface CitizenRepository extends CrudRepository<Citizen, String> {
        @Find
        @Select("city.name")
        List<String> cities();
    }
}
