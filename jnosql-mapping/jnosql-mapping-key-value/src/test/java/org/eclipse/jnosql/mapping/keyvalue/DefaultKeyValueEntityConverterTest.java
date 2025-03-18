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
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.keyvalue.KeyValueEntity;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.IdNotFoundException;
import org.eclipse.jnosql.mapping.keyvalue.spi.KeyValueExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.core.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.keyvalue.entities.Car;
import org.eclipse.jnosql.mapping.keyvalue.entities.Person;
import org.eclipse.jnosql.mapping.keyvalue.entities.Plate;
import org.eclipse.jnosql.mapping.keyvalue.entities.User;
import org.eclipse.jnosql.mapping.keyvalue.entities.Worker;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableAutoWeld
@AddPackages(value = {Converters.class, KeyValueEntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, KeyValueExtension.class})
class DefaultKeyValueEntityConverterTest {

    @Inject
    private KeyValueEntityConverter converter;

    @Test
    void shouldReturnNPEWhenEntityIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> converter.toKeyValue(null));
    }

    @Test
    void shouldReturnErrorWhenThereIsNotKeyAnnotation() {
        Assertions.assertThrows(IdNotFoundException.class, () -> converter.toKeyValue(new Worker()));
    }

    @Test
    void shouldReturnErrorWhenTheKeyIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            User user = new User(null, "name", 24);
            converter.toKeyValue(user);
        });
    }

    @Test
    void shouldConvertToKeyValue() {
        User user = new User("nickname", "name", 24);
        KeyValueEntity keyValueEntity = converter.toKeyValue(user);
        assertEquals("nickname", keyValueEntity.key());
        assertEquals(user, keyValueEntity.value());
    }

    @Test
    void shouldReturnNPEWhenKeyValueIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> converter.toEntity(User.class, null));
    }

    @Test
    void shouldReturnNPEWhenClassIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> converter.toEntity(null,
                KeyValueEntity.of("user", new User("nickname", "name", 21))));
    }

    @Test
    void shouldReturnErrorWhenTheKeyIsMissing() {
        Assertions.assertThrows(IdNotFoundException.class, () -> converter.toEntity(Worker.class,
                KeyValueEntity.of("worker", new Worker())));
    }

    @Test
    void shouldConvertToEntity() {
        User expectedUser = new User("nickname", "name", 21);
        User user = converter.toEntity(User.class,
                KeyValueEntity.of("user", expectedUser));
        assertEquals(expectedUser, user);
    }

    @Test
    void shouldConvertAndFeedTheKeyValue() {
        User expectedUser = new User("nickname", "name", 21);
        User user = converter.toEntity(User.class,
                KeyValueEntity.of("nickname", new User(null, "name", 21)));
        assertEquals(expectedUser, user);
    }

    @Test
    void shouldConvertAndFeedTheKeyValueIfKeyAndFieldAreDifferent() {
        User expectedUser = new User("nickname", "name", 21);
        User user = converter.toEntity(User.class,
                KeyValueEntity.of("nickname", new User("newName", "name", 21)));
        assertEquals(expectedUser, user);
    }

    @Test
    void shouldConvertValueToEntity() {
        User expectedUser = new User("nickname", "name", 21);
        User user = converter.toEntity(User.class, KeyValueEntity.of("nickname", Value.of(expectedUser)));
        assertEquals(expectedUser, user);
    }

    @Test
    void shouldConvertToEntityKeyWhenThereIsConverterAnnotation() {
        Car car = new Car();
        car.setName("Ferrari");

        Car ferrari = converter.toEntity(Car.class, KeyValueEntity.of("123-BRL", car));
        assertEquals(Plate.of("123-BRL"), ferrari.getPlate());
        assertEquals(car.getName(), ferrari.getName());
    }

    @Test
    void shouldConvertToKeyWhenThereIsConverterAnnotation() {
        Car car = new Car();
        car.setPlate(Plate.of("123-BRL"));
        car.setName("Ferrari");
        KeyValueEntity entity = converter.toKeyValue(car);

        Assertions.assertEquals("123-BRL", entity.key());
        Assertions.assertEquals(car, entity.value());
    }

    @Test
    void shouldConvertToEntityKeyWhenKeyTypeIsDifferent() {

        Person person = Person.builder().withName("Ada").build();
        Person ada = converter.toEntity(Person.class, KeyValueEntity.of("123", person));

        Assertions.assertEquals(123L, ada.getId());
        Assertions.assertEquals(ada.getName(), person.getName());
    }

    @Test
    void shouldConvertToKeyWhenKeyTypeIsDifferent() {
        Person person = Person.builder().withId(123L).withName("Ada").build();
        KeyValueEntity entity = converter.toKeyValue(person);
        Assertions.assertEquals(123L, entity.key());
    }

}
