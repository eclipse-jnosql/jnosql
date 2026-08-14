/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.entities.Car;
import org.eclipse.jnosql.mapping.semistructured.entities.Vendor;
import org.eclipse.jnosql.mapping.semistructured.entities.Worker;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class MapperObserverTest {

    @Inject
    private EntitiesMetadata mappings;

    private CommunicationObserverParser parser;

    @BeforeEach
    void setUp() {
        this.parser = new MapperObserver(mappings);
    }

    @DisplayName("Should fire entity")
    @Test
    void shouldFireEntity(){
        var entity = parser.fireEntity("Vendor");
        assertThat(entity).isEqualTo("vendors");
    }

    @DisplayName("Should fire from class")
    @Test
    void shouldFireFromClass(){
        var entity = parser.fireEntity(Car.class.getSimpleName());
        assertThat(entity).isEqualTo("Car");
    }

    @DisplayName("Should fire from class name")
    @Test
    void shouldFireFromClassName(){
        var entity = parser.fireEntity(Car.class.getSimpleName());
        assertThat(entity).isEqualTo("Car");
    }

    @DisplayName("Should fire field")
    @Test
    void shouldFireField(){
        var field = parser.fireSelectField("Worker", "salary");
        assertThat(field).isEqualTo("money");
    }

    @DisplayName("Should fire field from class name")
    @Test
    void shouldFireFieldFromClassName(){
        var field = parser.fireSelectField(Worker.class.getName(), "salary");
        assertThat(field).isEqualTo("money");
    }

    @DisplayName("Should fire field from simples name")
    @Test
    void shouldFireFieldFromSimplesName(){
        var field = parser.fireSelectField(Worker.class.getSimpleName(), "salary");
        assertThat(field).isEqualTo("money");
    }

    @DisplayName("Should fire field from entity")
    @Test
    void shouldFireFieldFromEntity(){
        var field = parser.fireSelectField(Vendor.class.getSimpleName(), "name");
        assertThat(field).isEqualTo("_id");
    }

    @DisplayName("Should fire condition field")
    @Test
    void shouldFireConditionField(){
        var field = parser.fireConditionField("Worker", "salary");
        assertThat(field).isEqualTo("money");
    }

    @DisplayName("Should fire condition field from class name")
    @Test
    void shouldFireConditionFieldFromClassName(){
        var field = parser.fireConditionField(Worker.class.getName(), "salary");
        assertThat(field).isEqualTo("money");
    }

    @DisplayName("Should fire condition field from simples name")
    @Test
    void shouldFireConditionFieldFromSimplesName(){
        var field = parser.fireConditionField(Worker.class.getSimpleName(), "salary");
        assertThat(field).isEqualTo("money");
    }

    @DisplayName("Should fire condition field from entity")
    @Test
    void shouldFireConditionFieldFromEntity(){
        var field = parser.fireConditionField(Vendor.class.getSimpleName(), "name");
        assertThat(field).isEqualTo("_id");
    }

    @DisplayName("Should fire sort property field")
    @Test
    void shouldFireSortPropertyField(){
        var field = parser.fireSortProperty("Worker", "salary");
        assertThat(field).isEqualTo("money");
    }

    @DisplayName("Should fire sort property field from class name")
    @Test
    void shouldFireSortPropertyFieldFromClassName(){
        var field = parser.fireSortProperty(Worker.class.getName(), "salary");
        assertThat(field).isEqualTo("money");
    }

    @DisplayName("Should fire sort property field from simples name")
    @Test
    void shouldFireSortPropertyFieldFromSimplesName(){
        var field = parser.fireSortProperty(Worker.class.getSimpleName(), "salary");
        assertThat(field).isEqualTo("money");
    }

    @DisplayName("Should fire sort property field from entity")
    @Test
    void shouldFireSortPropertyFieldFromEntity(){
        var field = parser.fireSortProperty(Vendor.class.getSimpleName(), "name");
        assertThat(field).isEqualTo("_id");
    }

    @DisplayName("Should sort id function")
    @Test
    void shouldSortIdFunction() {
        var field = parser.fireSortProperty(Vendor.class.getSimpleName(), "id(this)");
        assertThat(field).isEqualTo("_id");
    }

    @DisplayName("Should select id function")
    @Test
    void shouldSelectIdFunction() {
        var field = parser.fireSelectField(Vendor.class.getSimpleName(), "id(this)");
        assertThat(field).isEqualTo("_id");
    }

    @Nested
    @DisplayName("When the mapper observer is tested")
    class WhenTheMapperObserverIsTested {
    }
}