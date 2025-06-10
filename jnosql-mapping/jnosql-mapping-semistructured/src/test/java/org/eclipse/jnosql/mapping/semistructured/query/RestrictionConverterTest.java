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

import jakarta.data.restrict.Restriction;
import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.entities.Product;
import org.eclipse.jnosql.mapping.semistructured.entities._Product;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;


@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class RestrictionConverterTest {

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    private EntityMetadata entityMetadata;

    @BeforeEach
    void setUp() {
        entityMetadata = entities.get(Product.class);
    }


    @Test
    void shouldExecuteEqualsCondition() {
        Restriction<Product> equalTo = _Product.name.equalTo("Macbook Pro");

        Optional<CriteriaCondition> optional = RestrictionConverter.INSTANCE.parser(equalTo, entityMetadata, converters);

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(optional).isPresent();
            var condition = optional.orElseThrow();
            var element = condition.element();

            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(element.name()).isEqualTo(_Product.NAME);
            soft.assertThat(element.get()).isEqualTo("Macbook Pro");
        });
    }

}