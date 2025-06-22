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
package org.eclipse.jnosql.mapping.reflection;

import jakarta.nosql.Convert;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.reflection.entities.ComputerView;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@EnableAutoWeld
@AddPackages(value = Convert.class)
@AddPackages(value = FieldReader.class)
@AddExtensions(ReflectionEntityMetadataExtension.class)
class ReflectionProjectionBuilderTest {

    private final ProjectionConverter converter = new ProjectionConverter();

    @Test
    void shouldShowParameters(){
        Class<?> type = ComputerView.class;
        var metadata = converter.apply(type);
        var reflectionProjectionBuilder = new ReflectionProjectionBuilder(metadata.constructor());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reflectionProjectionBuilder.parameters()).hasSize(2);
        });
    }

    @Test
    void shouldAddParameters() {
        var metadata = converter.apply(ComputerView.class);
        var reflectionProjectionBuilder = new ReflectionProjectionBuilder(metadata.constructor());
        reflectionProjectionBuilder.add("Test Computer");
        reflectionProjectionBuilder.add(BigDecimal.TEN);
        SoftAssertions.assertSoftly(softly -> softly.assertThat(reflectionProjectionBuilder.parameters()).hasSize(2));
    }

    @Test
    void shouldBuild() {
        var metadata = converter.apply(ComputerView.class);
        var reflectionProjectionBuilder = new ReflectionProjectionBuilder(metadata.constructor());
        reflectionProjectionBuilder.add("Test Computer");
        reflectionProjectionBuilder.add(BigDecimal.TEN);
        ComputerView instance = reflectionProjectionBuilder.build();

        SoftAssertions.assertSoftly(softly -> {
           softly.assertThat(instance).isNotNull();
            softly.assertThat(instance).isInstanceOf(ComputerView.class);
            softly.assertThat(instance.name()).isEqualTo("Test Computer");
            softly.assertThat(instance.value()).isEqualTo(BigDecimal.TEN);
        });
    }

    @Test
    void shouldEmptyParameter() {
        var metadata = converter.apply(ComputerView.class);
        var reflectionProjectionBuilder = new ReflectionProjectionBuilder(metadata.constructor());
        reflectionProjectionBuilder.addEmptyParameter();
        reflectionProjectionBuilder.addEmptyParameter();
        ComputerView instance = reflectionProjectionBuilder.build();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(instance.name()).isNull();
            softly.assertThat(instance.value()).isNull();
        });
    }

    @Test
    void shouldEmptyParameterPrimitive() {
        var metadata = converter.apply(PrimitiveProjection.class);
        var reflectionProjectionBuilder = new ReflectionProjectionBuilder(metadata.constructor());
        reflectionProjectionBuilder.addEmptyParameter();
        reflectionProjectionBuilder.addEmptyParameter();
        reflectionProjectionBuilder.addEmptyParameter();
        reflectionProjectionBuilder.addEmptyParameter();
        reflectionProjectionBuilder.addEmptyParameter();
        PrimitiveProjection instance = reflectionProjectionBuilder.build();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(instance.name()).isNull();
            softly.assertThat(instance.id()).isEqualTo(0);
            softly.assertThat(instance.bool()).isFalse();
            softly.assertThat(instance.character()).isEqualTo('\u0000');
            softly.assertThat(instance.b()).isEqualTo((byte) 0);
        });
    }


    record PrimitiveProjection(int id, boolean bool, char character, byte b, String name) {
    }
}