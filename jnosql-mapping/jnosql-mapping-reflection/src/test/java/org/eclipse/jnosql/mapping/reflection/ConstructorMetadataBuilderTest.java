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
package org.eclipse.jnosql.mapping.reflection;

import jakarta.inject.Inject;
import jakarta.nosql.Convert;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;
import org.eclipse.jnosql.mapping.reflection.entities.Person;
import org.eclipse.jnosql.mapping.reflection.entities.Worker;
import org.eclipse.jnosql.mapping.reflection.entities.constructor.BookUser;
import org.eclipse.jnosql.mapping.reflection.entities.constructor.Computer;
import org.eclipse.jnosql.mapping.reflection.entities.constructor.PetOwner;
import org.eclipse.jnosql.mapping.reflection.entities.constructor.Smartphone;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableAutoWeld
@AddPackages(value = Convert.class)
@AddPackages(value = FieldReader.class)
class ConstructorMetadataBuilderTest {

    @Inject
    private Reflections reflections;

    private ConstructorMetadataBuilder builder;

    @BeforeEach
    void setUp() {
        this.builder = new ConstructorMetadataBuilder(reflections);
    }

    @Test
    void shouldReturnEmptyMetadata() {
        ConstructorMetadata metadata = builder.build(Person.class);
        Assertions.assertNotNull(metadata);
        Assertions.assertTrue(metadata.parameters().isEmpty());
    }

    @Test
    void shouldReturnEmptyDefaultConstructor() {
        ConstructorMetadata metadata = builder.build(Worker.class);
        Assertions.assertNotNull(metadata);
        Assertions.assertTrue(metadata.parameters().isEmpty());
    }

    @Test
    void shouldReturnComputerEntityConstructor() {
        ConstructorMetadata metadata = builder.build(Computer.class);
        List<ParameterMetaData> parameters = metadata.parameters();
        assertEquals(5, parameters.size());
        List<String> names = parameters.stream()
                .map(ParameterMetaData::name)
                .toList();

        assertThat(names).contains("_id", "name", "age", "model", "price");
    }

    @Test
    void shouldReturnBookUserEntityConstructor() {
        ConstructorMetadata metadata = builder.build(BookUser.class);
        List<ParameterMetaData> parameters = metadata.parameters();
        assertEquals(4, parameters.size());
        List<String> names = parameters.stream()
                .map(ParameterMetaData::name)
                .toList();

        assertThat(names).contains("_id", "native_name", "books");
    }

    @Test
    void shouldReturnSmartphoneEntityConstructor() {
        ConstructorMetadata metadata = builder.build(Smartphone.class);
        List<ParameterMetaData> parameters = metadata.parameters();
        assertEquals(2, parameters.size());
        List<String> names = parameters.stream()
                .map(ParameterMetaData::name)
                .toList();

        assertThat(names).contains("_id", "owner");
    }

    @Test
    void shouldReturnPetOwnerEntityConstructor() {
        ConstructorMetadata metadata = builder.build(PetOwner.class);
        List<ParameterMetaData> parameters = metadata.parameters();
        assertEquals(3, parameters.size());
        List<String> names = parameters.stream()
                .map(ParameterMetaData::name)
                .toList();

        assertThat(names).contains("_id", "name", "animal");
    }
}