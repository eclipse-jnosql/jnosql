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
package org.eclipse.jnosql.mapping.keyvalue.configuration;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueDatabase;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueDatabaseQualifier;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueEntityConverter;
import org.eclipse.jnosql.mapping.keyvalue.MockProducer;
import org.eclipse.jnosql.mapping.keyvalue.spi.KeyValueExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.core.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.KEY_VALUE_PROVIDER;

@EnableAutoWeld
@AddPackages(value = {Converters.class, KeyValueEntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, KeyValueExtension.class})
class CollectionSupplierTest {

    @Inject
    @KeyValueDatabase("names")
    private List<String> names;

    @Inject
    @KeyValueDatabase("fruits")
    private Set<String> fruits;

    @Inject
    @KeyValueDatabase("orders")
    private Queue<String> orders;

    @Inject
    @KeyValueDatabase("orders")
    private Map<String, String> map;

    @Inject
    private CollectionStructure structure;


    @BeforeAll
    static void beforeAll(){
        System.clearProperty(KEY_VALUE_PROVIDER.get());
        System.setProperty(KEY_VALUE_PROVIDER.get(), KeyValueConfigurationMock.class.getName());
    }

    @AfterAll
    static void afterAll(){
        System.clearProperty(KEY_VALUE_PROVIDER.get());
    }


    @Test
    void shouldGetList() {
        Assertions.assertNotNull(names);
        assertThat(names)
                .isInstanceOf(ArrayList.class);
    }

    @Test
    void shouldGetMap() {
        Assertions.assertNotNull(map);
        assertThat(map)
                .isInstanceOf(HashMap.class);
    }

    @Test
    void shouldGetQueue() {
        Assertions.assertNotNull(orders);
        assertThat(orders)
                .isInstanceOf(LinkedList.class);
    }

    @Test
    void shouldGetSet() {
        Assertions.assertNotNull(fruits);
        assertThat(fruits)
                .isInstanceOf(HashSet.class);
    }

    @Test
    void shouldStructure() {
        Assertions.assertNotNull(structure);
    }


    @Test
    void shouldGetFromQualifier() {
        CDI<Object> current = CDI.current();
        TypeLiteral<List<Integer>> literal = new TypeLiteral<>(){};
        List<Integer> integers = current.select(literal, KeyValueDatabaseQualifier.of("numbers")).get();
        Assertions.assertNotNull(integers);
        assertThat(integers)
                .isInstanceOf(ArrayList.class);
    }


}