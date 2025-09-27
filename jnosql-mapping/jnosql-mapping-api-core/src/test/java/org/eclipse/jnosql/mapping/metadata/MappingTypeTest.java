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
package org.eclipse.jnosql.mapping.metadata;

import jakarta.nosql.Column;
import jakarta.nosql.Embeddable;
import org.eclipse.jnosql.mapping.core.entities.Actor;
import org.eclipse.jnosql.mapping.core.entities.Address;
import org.eclipse.jnosql.mapping.core.entities.Movie;
import org.eclipse.jnosql.mapping.core.entities.Person;
import org.eclipse.jnosql.mapping.core.entities.Worker;
import org.eclipse.jnosql.mapping.core.entities.constructor.BookUser;
import org.eclipse.jnosql.mapping.core.entities.constructor.PetOwner;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


class MappingTypeTest {

    @Test
    void shouldReturnList() throws NoSuchFieldException {
        Field field = Person.class.getDeclaredField("phones");
        assertEquals(MappingType.COLLECTION, MappingType.of(field.getType()));
    }

    @Test
    void shouldReturnSet() throws NoSuchFieldException {
        Field field = Movie.class.getDeclaredField("actors");
        assertEquals(MappingType.COLLECTION, MappingType.of(field.getType()));
    }

    @Test
    void shouldReturnMap() throws NoSuchFieldException {
        Field field = Actor.class.getDeclaredField("movieCharacter");
        assertEquals(MappingType.MAP, MappingType.of(field.getType()));
    }

    @Test
    void shouldReturnDefault() throws NoSuchFieldException {
        Field field = Person.class.getDeclaredField("name");
        assertEquals(MappingType.DEFAULT, MappingType.of(field.getType()));
    }

    @Test
    void shouldReturnArray() throws NoSuchFieldException {
        Field field = Person.class.getDeclaredField("mobile");
        assertEquals(MappingType.ARRAY, MappingType.of(field.getType()));
    }

    @Test
    void shouldReturnArrayEntity() throws NoSuchFieldException {
        Field field = Worker.class.getDeclaredField("freeLancer");
        assertEquals(MappingType.ARRAY, MappingType.of(field.getType()));
    }

    @Test
    void shouldReturnEmbedded() throws NoSuchFieldException {
        Field field = Worker.class.getDeclaredField("job");
        assertEquals(MappingType.EMBEDDED, MappingType.of(field.getType()));
    }

    @Test
    void shouldReturnEmbeddedGroup() throws NoSuchFieldException {
        Field field = ForClass.class.getDeclaredField("bar2Class");
        assertEquals(MappingType.EMBEDDED_GROUP, MappingType.of(field.getType()));
    }

    @Test
    void shouldReturnEntity() throws NoSuchFieldException {
        Field field = Address.class.getDeclaredField("zipCode");
        assertEquals(MappingType.ENTITY, MappingType.of(field.getType()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnParameterDefault()  {
        Constructor<BookUser> constructor = (Constructor<BookUser>) BookUser.class.getDeclaredConstructors()[0];
        Parameter id = constructor.getParameters()[0];
        Parameter name = constructor.getParameters()[1];
        assertEquals(MappingType.DEFAULT, MappingType.of(id.getType()));
        assertEquals(MappingType.DEFAULT, MappingType.of(name.getType()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnParameterCollection() {
        Constructor<BookUser> constructor = (Constructor<BookUser>) BookUser.class.getDeclaredConstructors()[0];
        Parameter books = constructor.getParameters()[2];
        assertEquals(MappingType.COLLECTION, MappingType.of(books.getType()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnParameterEntity() {
        Constructor<PetOwner> constructor = (Constructor<PetOwner>) PetOwner.class.getDeclaredConstructors()[0];
        Parameter animal = constructor.getParameters()[2];
        assertEquals(MappingType.ENTITY, MappingType.of(animal.getType()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnParameterMap() {
        Constructor<ForClass> constructor = (Constructor<ForClass>) ForClass.class.getDeclaredConstructors()[0];
        Parameter map = constructor.getParameters()[0];
        assertEquals(MappingType.MAP, MappingType.of(map.getType()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnParameterEmbeddedFlat() {
        Constructor<ForClass> constructor = (Constructor<ForClass>) ForClass.class.getDeclaredConstructors()[0];
        Parameter map = constructor.getParameters()[1];
        assertEquals(MappingType.EMBEDDED, MappingType.of(map.getType()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnParameterEmbeddedGroup() {
        Constructor<ForClass> constructor = (Constructor<ForClass>) ForClass.class.getDeclaredConstructors()[0];
        Parameter map = constructor.getParameters()[2];
        assertEquals(MappingType.EMBEDDED_GROUP, MappingType.of(map.getType()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnParameterEmbeddedArray() {
        Constructor<Contacts> constructor = (Constructor<Contacts>) Contacts.class.getDeclaredConstructors()[0];
        Parameter map = constructor.getParameters()[1];
        assertEquals(MappingType.ARRAY, MappingType.of(map.getType()));
    }

    public static class ForClass {

        @Column("mapAnnotation")
        private Map<String, String> map;


        @Column
        private BarClass barClass;

        @Column
        private Bar2Class bar2Class;

        public ForClass(@Column("map") Map<String, String> map, @Column("barClass") BarClass barClass,
                        @Column("barClass") Bar2Class bar2Class) {
            this.map = map;
            this.barClass = barClass;
            this.bar2Class = bar2Class;
        }
    }

    @Embeddable
    public static class BarClass {

        @Column("integerAnnotation")
        private Integer integer;
    }

    @Embeddable(Embeddable.EmbeddableType.GROUPING)
    public static class Bar2Class {

        @Column("integerAnnotation")
        private Integer integer;
    }

    public static class Contacts {
        @Column
        private String name;
        @Column
        private String[] mobile;

        public Contacts(@Column String name, @Column String[] mobile) {
            this.name = name;
            this.mobile = mobile;
        }
    }
}