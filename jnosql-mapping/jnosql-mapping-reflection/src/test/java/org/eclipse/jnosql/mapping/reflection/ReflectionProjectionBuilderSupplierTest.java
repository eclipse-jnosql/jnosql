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
import org.eclipse.jnosql.mapping.metadata.ProjectionBuilder;
import org.eclipse.jnosql.mapping.metadata.ProjectionConstructorMetadata;
import org.eclipse.jnosql.mapping.reflection.entities.ComputerView;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;


@EnableAutoWeld
@AddPackages(value = Convert.class)
@AddPackages(value = FieldReader.class)
@AddExtensions(ReflectionEntityMetadataExtension.class)
class ReflectionProjectionBuilderSupplierTest {

    private final ProjectionConverter converter = new ProjectionConverter();

    @Test
   void shouldCreateProjectionBuilder() {
        Class<?> type = ComputerView.class;
        var metadata = converter.apply(type);
        ProjectionConstructorMetadata constructorMetadata = metadata.constructor();
        ProjectionBuilder supplier = ProjectionBuilder.of(constructorMetadata);
        SoftAssertions.assertSoftly(softly -> softly.assertThat(supplier).isNotNull());
    }
}