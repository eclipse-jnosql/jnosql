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
package org.eclipse.jnosql.mapping.column;

import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.column.Column;
import org.eclipse.jnosql.communication.column.ColumnEntity;
import org.eclipse.jnosql.mapping.Convert;
import org.eclipse.jnosql.mapping.column.entities.Car;
import org.eclipse.jnosql.mapping.column.entities.Hero;
import org.eclipse.jnosql.mapping.column.spi.ColumnExtension;
import org.eclipse.jnosql.mapping.reflection.EntityMetadataExtension;
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
import static org.junit.jupiter.api.Assertions.assertSame;

@EnableAutoWeld
@AddPackages(value = {Convert.class, ColumnWorkflow.class})
@AddPackages(MockProducer.class)
@AddExtensions({EntityMetadataExtension.class, ColumnExtension.class})
public class ColumnEntityImmutableTest {


    @Inject
    private DefaultColumnEntityConverter converter;

    private Column[] columns;

    private Car car;

    @BeforeEach
    public void init() {

        this.car = new Car("123456789", "SF90", "Ferrari", Year.now());

        columns = new Column[]{Column.of("_id", "123456789"),
                Column.of("model", "SF90"),
                Column.of("manufacturer", "Ferrari"),
                Column.of("year", Year.now())
        };
    }

    @Test
    public void shouldConvertEntityFromColumnEntity() {

        ColumnEntity entity = converter.toColumn(car);
        assertEquals("Car", entity.name());
        assertEquals(4, entity.size());
        assertThat(entity.columns()).contains(Column.of("_id", "123456789"),
                Column.of("model", "SF90"),
                Column.of("manufacturer", "Ferrari"));

    }

    @Test
    public void shouldConvertColumnEntityFromEntity() {

        ColumnEntity entity = converter.toColumn(car);
        assertEquals("Car", entity.name());
        assertEquals(4, entity.size());

        assertThat(entity.columns()).contains(columns);
    }

    @Test
    public void shouldConvertColumnEntityToEntity() {
        ColumnEntity entity = ColumnEntity.of("Car");
        Stream.of(columns).forEach(entity::add);

        Car ferrari = converter.toEntity(Car.class, entity);
        assertNotNull(ferrari);
        assertEquals("123456789", ferrari.plate());
        assertEquals("SF90", ferrari.model());
        assertEquals("Ferrari", ferrari.manufacturer());
        assertEquals(Year.now(), ferrari.year());

    }

    @Test
    public void shouldConvertColumnEntityToExistRecordEntity() {
        ColumnEntity entity = ColumnEntity.of("Car");
        Stream.of(columns).forEach(entity::add);
        Car ferrari = new Car(null, null, null, null);
        Car result = converter.toEntity(ferrari, entity);

        assertEquals("123456789", result.plate());
        assertEquals("SF90", result.model());
        assertEquals("Ferrari", result.manufacturer());
        assertEquals(Year.now(), result.year());

        assertSoftly(soft -> {
            soft.assertThat(ferrari.model()).isNull();
            soft.assertThat(ferrari.manufacturer()).isNull();
            soft.assertThat(ferrari.plate()).isNull();
            soft.assertThat(ferrari.year()).isNull();
        });
    }

    @Test
    public void shouldConvertColumnEntityToExistEntity2() {
        ColumnEntity entity = ColumnEntity.of("Hero");
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


}