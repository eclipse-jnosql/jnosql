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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;


class MappingTypeTest {

    @Nested
    @DisplayName("When the field type is mapped")
    class WhenTheFieldTypeIsMapped {

        @Test
        @DisplayName("Should map a list field to collection")
        void shouldMapAListFieldToCollection() throws NoSuchFieldException {
            Field field = Person.class.getDeclaredField("phones");

            assertThat(MappingType.of(field.getType())).isEqualTo(MappingType.COLLECTION);
        }

        @Test
        @DisplayName("Should map a set field to collection")
        void shouldMapASetFieldToCollection() throws NoSuchFieldException {
            Field field = Movie.class.getDeclaredField("actors");

            assertThat(MappingType.of(field.getType())).isEqualTo(MappingType.COLLECTION);
        }

        @Test
        @DisplayName("Should map a map field to map")
        void shouldMapAMapFieldToMap() throws NoSuchFieldException {
            Field field = Actor.class.getDeclaredField("movieCharacter");

            assertThat(MappingType.of(field.getType())).isEqualTo(MappingType.MAP);
        }

        @Test
        @DisplayName("Should map a simple field to default")
        void shouldMapASimpleFieldToDefault() throws NoSuchFieldException {
            Field field = Person.class.getDeclaredField("name");

            assertThat(MappingType.of(field.getType())).isEqualTo(MappingType.DEFAULT);
        }

        @Test
        @DisplayName("Should map an array field to array")
        void shouldMapAnArrayFieldToArray() throws NoSuchFieldException {
            Field field = Person.class.getDeclaredField("mobile");

            assertThat(MappingType.of(field.getType())).isEqualTo(MappingType.ARRAY);
        }

        @Test
        @DisplayName("Should map an entity array field to array")
        void shouldMapAnEntityArrayFieldToArray() throws NoSuchFieldException {
            Field field = Worker.class.getDeclaredField("freeLancer");

            assertThat(MappingType.of(field.getType())).isEqualTo(MappingType.ARRAY);
        }

        @Test
        @DisplayName("Should map a flat embeddable field to embedded")
        void shouldMapAFlatEmbeddableFieldToEmbedded() throws NoSuchFieldException {
            Field field = Worker.class.getDeclaredField("job");

            assertThat(MappingType.of(field.getType())).isEqualTo(MappingType.EMBEDDED);
        }

        @Test
        @DisplayName("Should map a grouping embeddable field to embedded group")
        void shouldMapAGroupingEmbeddableFieldToEmbeddedGroup() throws NoSuchFieldException {
            Field field = ForClass.class.getDeclaredField("bar2Class");

            assertThat(MappingType.of(field.getType())).isEqualTo(MappingType.EMBEDDED_GROUP);
        }

        @Test
        @DisplayName("Should map an entity field to entity")
        void shouldMapAnEntityFieldToEntity() throws NoSuchFieldException {
            Field field = Address.class.getDeclaredField("zipCode");

            assertThat(MappingType.of(field.getType())).isEqualTo(MappingType.ENTITY);
        }
    }

    @Nested
    @DisplayName("When the constructor parameter type is mapped")
    class WhenTheConstructorParameterTypeIsMapped {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should map simple parameters to default")
        void shouldMapSimpleParametersToDefault() {
            Constructor<BookUser> constructor = (Constructor<BookUser>) BookUser.class.getDeclaredConstructors()[0];
            Parameter id = constructor.getParameters()[0];
            Parameter name = constructor.getParameters()[1];

            assertSoftly(softly -> {
                softly.assertThat(MappingType.of(id.getType())).isEqualTo(MappingType.DEFAULT);
                softly.assertThat(MappingType.of(name.getType())).isEqualTo(MappingType.DEFAULT);
            });
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should map a collection parameter to collection")
        void shouldMapACollectionParameterToCollection() {
            Constructor<BookUser> constructor = (Constructor<BookUser>) BookUser.class.getDeclaredConstructors()[0];
            Parameter books = constructor.getParameters()[2];

            assertThat(MappingType.of(books.getType())).isEqualTo(MappingType.COLLECTION);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should map an entity parameter to entity")
        void shouldMapAnEntityParameterToEntity() {
            Constructor<PetOwner> constructor = (Constructor<PetOwner>) PetOwner.class.getDeclaredConstructors()[0];
            Parameter animal = constructor.getParameters()[2];

            assertThat(MappingType.of(animal.getType())).isEqualTo(MappingType.ENTITY);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should map a map parameter to map")
        void shouldMapAMapParameterToMap() {
            Constructor<ForClass> constructor = (Constructor<ForClass>) ForClass.class.getDeclaredConstructors()[0];
            Parameter map = constructor.getParameters()[0];

            assertThat(MappingType.of(map.getType())).isEqualTo(MappingType.MAP);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should map a flat embeddable parameter to embedded")
        void shouldMapAFlatEmbeddableParameterToEmbedded() {
            Constructor<ForClass> constructor = (Constructor<ForClass>) ForClass.class.getDeclaredConstructors()[0];
            Parameter map = constructor.getParameters()[1];

            assertThat(MappingType.of(map.getType())).isEqualTo(MappingType.EMBEDDED);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should map a grouping embeddable parameter to embedded group")
        void shouldMapAGroupingEmbeddableParameterToEmbeddedGroup() {
            Constructor<ForClass> constructor = (Constructor<ForClass>) ForClass.class.getDeclaredConstructors()[0];
            Parameter map = constructor.getParameters()[2];

            assertThat(MappingType.of(map.getType())).isEqualTo(MappingType.EMBEDDED_GROUP);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should map an array parameter to array")
        void shouldMapAnArrayParameterToArray() {
            Constructor<Contacts> constructor = (Constructor<Contacts>) Contacts.class.getDeclaredConstructors()[0];
            Parameter map = constructor.getParameters()[1];

            assertThat(MappingType.of(map.getType())).isEqualTo(MappingType.ARRAY);
        }
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