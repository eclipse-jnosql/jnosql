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

import jakarta.nosql.AttributeConverter;
import jakarta.nosql.Column;
import org.eclipse.jnosql.mapping.metadata.MappingType;
import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;
import org.eclipse.jnosql.mapping.reflection.entities.Animal;
import org.eclipse.jnosql.mapping.reflection.entities.Money;
import org.eclipse.jnosql.mapping.reflection.entities.MoneyConverter;
import org.eclipse.jnosql.mapping.reflection.entities.constructor.BookUser;
import org.eclipse.jnosql.mapping.reflection.entities.constructor.Computer;
import org.eclipse.jnosql.mapping.reflection.entities.constructor.PetOwner;
import org.eclipse.jnosql.mapping.reflection.entities.constructor.Smartphone;
import org.eclipse.jnosql.mapping.reflection.entities.constructor.SuperHero;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParameterMetaDataBuilderTest {

    @Test
    void shouldConvertIdParameter() {
        Constructor<Computer> constructor = (Constructor<Computer>) Computer.class.getDeclaredConstructors()[0];
        ParameterMetaData id = ParameterMetaDataBuilder.of(constructor.getParameters()[0]);
        Assertions.assertNotNull(id);
        Assertions.assertTrue(id.isId());
        Assertions.assertEquals("_id", id.name());
        Assertions.assertEquals(Long.class, id.type());
        Assertions.assertEquals(MappingType.DEFAULT, id.mappingType());
        Assertions.assertTrue(id.converter().isEmpty());
    }

    @Test
    void shouldConvertDefaultParameterWithoutDefinedName() {
        Constructor<Smartphone> constructor = (Constructor<Smartphone>) Smartphone.class.getDeclaredConstructors()[0];
        ParameterMetaData name = ParameterMetaDataBuilder.of(constructor.getParameters()[1]);
        Assertions.assertNotNull(name);
        Assertions.assertFalse(name.isId());
        Assertions.assertEquals("owner", name.name());
        Assertions.assertEquals(String.class, name.type());
        Assertions.assertEquals(MappingType.DEFAULT, name.mappingType());
        Assertions.assertTrue(name.converter().isEmpty());
    }

    @Test
    void shouldConvertDefaultParameter() {
        Constructor<Computer> constructor = (Constructor<Computer>) Computer.class.getDeclaredConstructors()[0];
        ParameterMetaData name = ParameterMetaDataBuilder.of(constructor.getParameters()[1]);
        Assertions.assertNotNull(name);
        Assertions.assertFalse(name.isId());
        Assertions.assertEquals("name", name.name());
        Assertions.assertEquals(String.class, name.type());
        Assertions.assertEquals(MappingType.DEFAULT, name.mappingType());
        Assertions.assertTrue(name.converter().isEmpty());
    }

    @Test
    void shouldConvertConverterParameter() {
        Constructor<Computer> constructor = (Constructor<Computer>) Computer.class.getDeclaredConstructors()[0];
        ParameterMetaData price = ParameterMetaDataBuilder.of(constructor.getParameters()[4]);
        Assertions.assertNotNull(price);
        Assertions.assertFalse(price.isId());
        Assertions.assertEquals("price", price.name());
        Assertions.assertEquals(Money.class, price.type());
        Assertions.assertEquals(MappingType.DEFAULT, price.mappingType());
        Assertions.assertFalse(price.converter().isEmpty());
        Class<? extends AttributeConverter<Object, Object>> converter = price.converter().orElseThrow();
        assertEquals(MoneyConverter.class, converter);
    }
    //parameter wit collection
    //parameter with EMBEDDED
    //parameter with map

    @Test
    void shouldConvertEntityParameter() {
        Constructor<PetOwner> constructor = (Constructor<PetOwner>) PetOwner.class.getDeclaredConstructors()[0];
        ParameterMetaData animal = ParameterMetaDataBuilder.of(constructor.getParameters()[2]);
        Assertions.assertNotNull(animal);
        Assertions.assertFalse(animal.isId());
        Assertions.assertEquals("animal", animal.name());
        Assertions.assertEquals(Animal.class, animal.type());
        Assertions.assertEquals(MappingType.ENTITY, animal.mappingType());
        Assertions.assertTrue(animal.converter().isEmpty());
    }

    @Test
    void shouldConvertCollectionParameter() {
        Constructor<BookUser> constructor = (Constructor<BookUser>) BookUser.class.getDeclaredConstructors()[0];
        ParameterMetaData books = ParameterMetaDataBuilder.of(constructor.getParameters()[2]);
        Assertions.assertNotNull(books);
        Assertions.assertFalse(books.isId());
        Assertions.assertEquals("books", books.name());
        Assertions.assertEquals(List.class, books.type());
        Assertions.assertEquals(MappingType.COLLECTION, books.mappingType());
        Assertions.assertTrue(books.converter().isEmpty());
        assertEquals(DefaultCollectionParameterMetaData.class, books.getClass());

    }

    @Test
    void shouldConvertMapParameter() {
        Constructor<Foo> constructor = (Constructor<Foo>) Foo.class.getDeclaredConstructors()[0];
        ParameterMetaData map = ParameterMetaDataBuilder.of(constructor.getParameters()[0]);
        Assertions.assertNotNull(map);
        Assertions.assertFalse(map.isId());
        Assertions.assertEquals("map", map.name());
        Assertions.assertEquals(Map.class, map.type());
        Assertions.assertEquals(MappingType.MAP, map.mappingType());
        Assertions.assertTrue(map.converter().isEmpty());
        assertEquals(DefaultMapParameterMetaData.class, map.getClass());
    }

    @Test
    void shouldConvertListParameter() {
        Constructor<Foo> constructor = (Constructor<Foo>) SuperHero.class.getDeclaredConstructors()[0];
        ParameterMetaData powers = ParameterMetaDataBuilder.of(constructor.getParameters()[2]);
        Assertions.assertNotNull(powers);
        Assertions.assertFalse(powers.isId());
        Assertions.assertEquals("powers", powers.name());
        Assertions.assertEquals(List.class, powers.type());
        Assertions.assertEquals(MappingType.COLLECTION, powers.mappingType());
        Assertions.assertTrue(powers.converter().isEmpty());
        assertEquals(DefaultCollectionParameterMetaData.class, powers.getClass());
    }


    static class Foo{
        private Map<String, String> map;

        public Foo(@Column("map") Map<String, String> map) {
            this.map = map;
        }
    }


}