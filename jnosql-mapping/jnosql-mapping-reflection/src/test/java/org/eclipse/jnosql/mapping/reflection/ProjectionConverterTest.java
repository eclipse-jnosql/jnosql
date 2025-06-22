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
 */
package org.eclipse.jnosql.mapping.reflection;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.metadata.ProjectionConstructorMetadata;
import org.eclipse.jnosql.mapping.reflection.entities.ComputerView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProjectionConverterTest {

    private final ProjectionConverter converter = new ProjectionConverter();


    @Test
    void shouldConvertProjection() {
        Class<?> type = ComputerView.class;
        var metadata = converter.apply(type);

        SoftAssertions.assertSoftly(soft->{
            soft.assertThat(metadata).isNotNull();
            soft.assertThat(metadata.className()).isEqualTo(ComputerView.class.getName());
            soft.assertThat(metadata.type()).isEqualTo(ComputerView.class);
            soft.assertThat(metadata.constructor()).isNotNull();
        });
    }

    @Test
    void shouldShowParameters() {
        Class<?> type = ComputerView.class;
        var metadata = converter.apply(type);
        var constructor = metadata.constructor();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(constructor).isNotNull();
            softly.assertThat(constructor.parameters()).hasSize(2);
            softly.assertThat(constructor.parameters().get(0).name()).isEqualTo("name");
            softly.assertThat(constructor.parameters().get(0).type()).isEqualTo(String.class);
            softly.assertThat(constructor.parameters().get(1).name()).isEqualTo("native");
            softly.assertThat(constructor.parameters().get(1).type()).isEqualTo(BigDecimal.class);
        });
    }

}