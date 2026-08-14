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
package org.eclipse.jnosql.mapping.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;

import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.VetedConverter;
import org.eclipse.jnosql.mapping.core.entities.Money;
import org.eclipse.jnosql.mapping.core.entities.Person;
import org.eclipse.jnosql.mapping.core.entities.Worker;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;



@EnableAutoWeld
@AddPackages(value = Converters.class)
@AddPackages(value = VetedConverter.class)
@AddPackages(value = Reflections.class)
@AddExtensions(ReflectionEntityMetadataExtension.class)
class ConverterUtilTest {


    @Inject
    private Converters converters;

    @Inject
    private EntitiesMetadata mappings;

    @DisplayName("Should not convert")
    @Test
    void shouldNotConvert() {
        EntityMetadata mapping = mappings.get(Person.class);
        Object value = 10_000L;
        Object id = ConverterUtil.getValue(value, mapping, "id", converters);
        assertThat(id).isEqualTo(value);
    }

    @DisplayName("Should convert")
    @Test
    void shouldConvert() {
        EntityMetadata mapping = mappings.get(Person.class);
        String value = "100";
        Object id = ConverterUtil.getValue(value, mapping, "id", converters);
        assertThat(id).isEqualTo(100L);
    }

    @DisplayName("Should use attribute convert")
    @Test
    void shouldUseAttributeConvert() {
        EntityMetadata mapping = mappings.get(Worker.class);
        Object value = new Money("BRL", BigDecimal.TEN);
        Object converted = ConverterUtil.getValue(value, mapping, "salary", converters);
        assertThat(converted).isEqualTo("BRL 10");
    }
}
