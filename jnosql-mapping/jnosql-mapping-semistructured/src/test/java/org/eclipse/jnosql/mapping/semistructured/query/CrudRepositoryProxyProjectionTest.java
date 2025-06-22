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
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Select;
import jakarta.data.restrict.Restriction;
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

import java.awt.print.Pageable;
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

        var productNames = repository.names(_Product.name.equalTo("Mac"));
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

        var name = repository.name();

        SoftAssertions.assertSoftly(softly -> softly.assertThat(name).isEqualTo("Mac"));

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
        Page<String> page(Restriction<Product> restriction, PageRequest pageRequest);

        @Find
        @Select(_Product.NAME)
        CursoredPage<String> cursor(Restriction<Product> restriction, PageRequest pageRequest);
    }
}
