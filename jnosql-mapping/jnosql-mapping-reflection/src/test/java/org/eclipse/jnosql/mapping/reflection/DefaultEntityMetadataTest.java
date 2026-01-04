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
package org.eclipse.jnosql.mapping.reflection;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.reflection.entities.Magazine;
import org.eclipse.jnosql.mapping.reflection.entities.Person;
import org.eclipse.jnosql.mapping.reflection.entities.inheritance.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;



class DefaultEntityMetadataTest {

    private ClassConverter converter;


    @BeforeEach
    void setUp() {
        this.converter = new ReflectionClassConverter();
    }

    @Test
    void shouldToString(){
        EntityMetadata entityMetadata = converter.apply(Person.class);
        assertThat(entityMetadata.toString()).isNotBlank();
    }

    @Test
    void shouldCreateInstance(){
        EntityMetadata entityMetadata = converter.apply(Person.class);
        Object person = entityMetadata.newInstance();
        assertThat(person).isNotNull().isInstanceOf(Person.class);
    }

    @Test
    void shouldGetId(){
        EntityMetadata entityMetadata = converter.apply(Person.class);
        Optional<FieldMetadata> id = entityMetadata.id();
        assertThat(id).isNotEmpty();
        FieldMetadata fieldMetadata = id.orElseThrow();
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(fieldMetadata.isId()).isTrue();
            soft.assertThat(fieldMetadata.name()).isEqualTo("_id");
            soft.assertThat(fieldMetadata.fieldName()).isEqualTo("id");
        });
    }

    @Test
    void shouldIsInheritance(){
        EntityMetadata entityMetadata = converter.apply(Person.class);
       assertThat(entityMetadata.isInheritance()).isFalse();

        EntityMetadata entityMetadata2 = converter.apply(Notification.class);
        assertThat(entityMetadata2.isInheritance()).isTrue();
    }

    @Test
    void shouldGroupByName(){
        EntityMetadata entityMetadata = converter.apply(Person.class);
        Map<String, FieldMetadata> map = entityMetadata.fieldsGroupByName();

        assertThat(map).isNotEmpty()
                .hasSize(5)
                .containsKeys("_id", "name", "age", "phones", "mobile");
    }

    @Test
    void shouldGetClass(){
        EntityMetadata entityMetadata = converter.apply(Person.class);
        assertThat(entityMetadata.type()).isEqualTo(Person.class);
    }

    @Test
    void shouldName(){
        EntityMetadata entityMetadata = converter.apply(Person.class);
        assertThat(entityMetadata.name()).isEqualTo(Person.class.getSimpleName());
    }

    @Test
    void shouldClassName(){
        EntityMetadata entityMetadata = converter.apply(Person.class);
        assertThat(entityMetadata.className()).isEqualTo(Person.class.getName());
    }

    @Test
    void shouldSimpleName(){
        EntityMetadata entityMetadata = converter.apply(Person.class);
        assertThat(entityMetadata.simpleName()).isEqualTo(Person.class.getSimpleName());
    }

    @Test
    void shouldNewInstance(){
        EntityMetadata entityMetadata = converter.apply(Person.class);
        Object instance = entityMetadata.newInstance();
        assertThat(instance).isNotNull().isInstanceOf(Person.class);
    }

    @Test
    void shouldHashCodeEquals(){
        EntityMetadata entityMetadata = converter.apply(Person.class);
        assertThat(entityMetadata).isEqualTo(converter.apply(Person.class));
        assertThat(entityMetadata).hasSameHashCodeAs(entityMetadata);
    }

    @Test
    void shouldMappingName(){
        EntityMetadata entityMetadata = converter.apply(Person.class);
        assertThat(entityMetadata.mappingName()).isEqualTo("Person");
    }

    @Test
    void shouldMappingInheritance(){
        EntityMetadata entityMetadata = converter.apply(Notification.class);
        assertThat(entityMetadata.mappingName()).isEqualTo("Notification");
    }

    @Test
    void shouldOverwriteMappingName(){
        EntityMetadata entityMetadata = converter.apply(Magazine.class);
        assertThat(entityMetadata.mappingName()).isEqualTo("legacyMagazine");
    }

}
