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
package org.eclipse.jnosql.mapping.keyvalue;

import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.keyvalue.KeyValueEntity;
import org.eclipse.jnosql.mapping.IdNotFoundException;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.keyvalue.entities.Car;
import org.eclipse.jnosql.mapping.keyvalue.entities.Person;
import org.eclipse.jnosql.mapping.keyvalue.entities.Plate;
import org.eclipse.jnosql.mapping.keyvalue.entities.User;
import org.eclipse.jnosql.mapping.keyvalue.entities.Worker;
import org.eclipse.jnosql.mapping.keyvalue.spi.KeyValueExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@EnableAutoWeld
@AddPackages(value = {Converters.class, KeyValueEntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, KeyValueExtension.class})
class DefaultKeyValueEntityConverterTest {

    @Inject
    private KeyValueEntityConverter converter;

    @Nested
    @DisplayName("When the converter converts entities")
    class WhenTheConverterConvertsEntities {

        @Test
        @DisplayName("Should return NullPointerException when entity is null")
        void shouldReturnNPEWhenEntityIsNull() {
            assertThatThrownBy(() -> converter.toKeyValue(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should return error when there is no key annotation")
        void shouldReturnErrorWhenThereIsNotKeyAnnotation() {
            assertThatThrownBy(() -> converter.toKeyValue(new Worker())).isInstanceOf(IdNotFoundException.class);
        }

        @Test
        @DisplayName("Should return error when the key is null")
        void shouldReturnErrorWhenTheKeyIsNull() {
            assertThatThrownBy(() -> {
                User user = new User(null, "name", 24);
                converter.toKeyValue(user);
            }).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should convert to key-value")
        void shouldConvertToKeyValue() {
            User user = new User("nickname", "name", 24);
            KeyValueEntity keyValueEntity = converter.toKeyValue(user);
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(keyValueEntity.key()).isEqualTo("nickname");
                softly.assertThat(keyValueEntity.value()).isEqualTo(user);
            });
        }

        @Test
        @DisplayName("Should return NullPointerException when key-value is null")
        void shouldReturnNPEWhenKeyValueIsNull() {
            assertThatThrownBy(() -> converter.toEntity(User.class, null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should return NullPointerException when class is null")
        void shouldReturnNPEWhenClassIsNull() {
            assertThatThrownBy(() -> converter.toEntity(null,
                    KeyValueEntity.of("user", new User("nickname", "name", 21)))).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should return error when the key is missing")
        void shouldReturnErrorWhenTheKeyIsMissing() {
            assertThatThrownBy(() -> converter.toEntity(Worker.class,
                    KeyValueEntity.of("worker", new Worker()))).isInstanceOf(IdNotFoundException.class);
        }

        @Test
        @DisplayName("Should convert to entity")
        void shouldConvertToEntity() {
            User expectedUser = new User("nickname", "name", 21);
            User user = converter.toEntity(User.class,
                    KeyValueEntity.of("user", expectedUser));
            assertThat(user).isEqualTo(expectedUser);
        }

        @Test
        @DisplayName("Should convert and feed the key-value")
        void shouldConvertAndFeedTheKeyValue() {
            User expectedUser = new User("nickname", "name", 21);
            User user = converter.toEntity(User.class,
                    KeyValueEntity.of("nickname", new User(null, "name", 21)));
            assertThat(user).isEqualTo(expectedUser);
        }

        @Test
        @DisplayName("Should convert and feed the key-value if key and field are different")
        void shouldConvertAndFeedTheKeyValueIfKeyAndFieldAreDifferent() {
            User expectedUser = new User("nickname", "name", 21);
            User user = converter.toEntity(User.class,
                    KeyValueEntity.of("nickname", new User("newName", "name", 21)));
            assertThat(user).isEqualTo(expectedUser);
        }

        @Test
        @DisplayName("Should convert value to entity")
        void shouldConvertValueToEntity() {
            User expectedUser = new User("nickname", "name", 21);
            User user = converter.toEntity(User.class, KeyValueEntity.of("nickname", Value.of(expectedUser)));
            assertThat(user).isEqualTo(expectedUser);
        }

        @Test
        @DisplayName("Should convert to entity key when there is converter annotation")
        void shouldConvertToEntityKeyWhenThereIsConverterAnnotation() {
            Car car = new Car();
            car.setName("Ferrari");

            Car ferrari = converter.toEntity(Car.class, KeyValueEntity.of("123-BRL", car));
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(ferrari.getPlate()).isEqualTo(Plate.of("123-BRL"));
                softly.assertThat(ferrari.getName()).isEqualTo(car.getName());
            });
        }

        @Test
        @DisplayName("Should convert to key when there is converter annotation")
        void shouldConvertToKeyWhenThereIsConverterAnnotation() {
            Car car = new Car();
            car.setPlate(Plate.of("123-BRL"));
            car.setName("Ferrari");
            KeyValueEntity entity = converter.toKeyValue(car);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(entity.key()).isEqualTo("123-BRL");
                softly.assertThat(entity.value()).isEqualTo(car);
            });
        }

        @Test
        @DisplayName("Should convert to entity key when key type is different")
        void shouldConvertToEntityKeyWhenKeyTypeIsDifferent() {

            Person person = Person.builder().withName("Ada").build();
            Person ada = converter.toEntity(Person.class, KeyValueEntity.of("123", person));

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(ada.getId()).isEqualTo(123L);
                softly.assertThat(person.getName()).isEqualTo(ada.getName());
            });
        }

        @Test
        @DisplayName("Should convert to key when key type is different")
        void shouldConvertToKeyWhenKeyTypeIsDifferent() {
            Person person = Person.builder().withId(123L).withName("Ada").build();
            KeyValueEntity entity = converter.toKeyValue(person);
            assertThat(entity.key()).isEqualTo(123L);
        }

    }

}
