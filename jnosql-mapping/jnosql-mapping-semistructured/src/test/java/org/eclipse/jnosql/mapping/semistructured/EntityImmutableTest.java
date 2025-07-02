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
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.entities.Car;
import org.eclipse.jnosql.mapping.semistructured.entities.Failure;
import org.eclipse.jnosql.mapping.semistructured.entities.Hero;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class EntityImmutableTest {

    @Inject
    private DefaultEntityConverter converter;

    private Element[] columns;

    private Car car;

    @BeforeEach
    void init() {

        this.car = new Car("123456789", "SF90", "Ferrari", Year.now());

        columns = new Element[]{Element.of("_id", "123456789"),
                Element.of("model", "SF90"),
                Element.of("manufacturer", "Ferrari"),
                Element.of("year", Year.now())
        };
    }

    @Test
    void shouldConvertCommunicationEntity() {

        CommunicationEntity entity = converter.toCommunication(car);
        assertEquals("Car", entity.name());
        assertEquals(4, entity.size());
        assertThat(entity.elements()).contains(Element.of("_id", "123456789"),
                Element.of("model", "SF90"),
                Element.of("manufacturer", "Ferrari"));

    }

    @Test
    void shouldConvertCommunicationEntity2() {

        CommunicationEntity entity = converter.toCommunication(car);
        assertEquals("Car", entity.name());
        assertEquals(4, entity.size());

        assertThat(entity.elements()).contains(columns);
    }

    @Test
    void shouldConvertEntity() {
        CommunicationEntity entity = CommunicationEntity.of("Car");
        Stream.of(columns).forEach(entity::add);

        Car ferrari = converter.toEntity(Car.class, entity);
        assertNotNull(ferrari);
        assertEquals("123456789", ferrari.plate());
        assertEquals("SF90", ferrari.model());
        assertEquals("Ferrari", ferrari.manufacturer());
        assertEquals(Year.now(), ferrari.year());

    }

    @Test
    void shouldConvertExistRecord() {
        CommunicationEntity entity = CommunicationEntity.of("Car");
        Stream.of(columns).forEach(entity::add);
        Car ferrari = new Car(null, null, null, null);
        Car result = converter.toEntity(ferrari, entity);

        assertEquals("123456789", result.plate());
        assertEquals("SF90", result.model());
        assertEquals("Ferrari", result.manufacturer());
        assertEquals(Year.now(), result.year());
        assertNotSame(ferrari, car);
        assertSoftly(soft -> {
            soft.assertThat(ferrari.model()).isNull();
            soft.assertThat(ferrari.manufacturer()).isNull();
            soft.assertThat(ferrari.plate()).isNull();
            soft.assertThat(ferrari.year()).isNull();

            soft.assertThat(result.model()).isEqualTo("SF90");
            soft.assertThat(result.manufacturer()).isEqualTo("Ferrari");
            soft.assertThat(result.plate()).isEqualTo("123456789");
            soft.assertThat(result.year()).isEqualTo(Year.now());
        });
    }

    @Test
    void shouldConvertExist() {
        CommunicationEntity entity = CommunicationEntity.of("Hero");
        entity.add("_id", "2342");
        entity.add("name", "Iron man");
        Hero hero = new Hero(null, null);
        Hero result = converter.toEntity(hero, entity);
        assertSame(hero, result);
        assertSoftly(soft -> {
                    soft.assertThat(hero.id()).isEqualTo("2342");
                    soft.assertThat(hero.name()).isEqualTo("Iron man");
                }
        );
    }

    @Test
    void shouldConvertByteArray() {
        var failure = new Failure("test", new byte[]{'a','b','c','d'});
        CommunicationEntity entity = converter.toCommunication(failure);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(entity.name()).isEqualTo("Failure");
            softly.assertThat(entity.size()).isEqualTo(2);
            Object id = entity.find("_id").orElseThrow().get();
            Object data = entity.find("data").orElseThrow().get();
            softly.assertThat(id).isEqualTo("test");
            softly.assertThat(data).isEqualTo(new byte[]{'a','b','c','d'});
        });
    }

    @Test
    void shouldConvertFromByteArray() {
        var entity = CommunicationEntity.of("Failure");
        entity.add("_id", "test");
        entity.add("data", new byte[]{'a','b','c','d'});
        Failure failure = converter.toEntity(entity);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(failure.id()).isEqualTo("test");
            softly.assertThat(failure.data()).isEqualTo(new byte[]{'a','b','c','d'});
        });
    }



}
