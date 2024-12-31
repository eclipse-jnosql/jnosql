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

import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;
import org.eclipse.jnosql.mapping.reflection.entities.Actor;
import org.eclipse.jnosql.mapping.reflection.entities.Director;
import org.eclipse.jnosql.mapping.reflection.entities.JsonContainer;
import org.eclipse.jnosql.mapping.reflection.entities.Machine;
import org.eclipse.jnosql.mapping.reflection.entities.NoConstructorEntity;
import org.eclipse.jnosql.mapping.reflection.entities.Person;
import org.eclipse.jnosql.mapping.reflection.entities.User;
import org.eclipse.jnosql.mapping.reflection.entities.Worker;
import org.eclipse.jnosql.mapping.reflection.entities.constructor.Computer;
import org.eclipse.jnosql.mapping.reflection.entities.inheritance.EmailNotification;
import org.eclipse.jnosql.mapping.reflection.entities.inheritance.Notification;
import org.eclipse.jnosql.mapping.reflection.entities.inheritance.Project;
import org.eclipse.jnosql.mapping.reflection.entities.inheritance.SmallProject;
import org.eclipse.jnosql.mapping.reflection.entities.inheritance.SocialMediaNotification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static jakarta.nosql.DiscriminatorColumn.DEFAULT_DISCRIMINATOR_COLUMN;
import static org.junit.jupiter.api.Assertions.*;

class ReflectionClassConverterTest {

    private ClassConverter converter;


    @BeforeEach
    void setUp() {
        this.converter = new ReflectionClassConverter();
    }

    @Test
    void shouldCreateEntityMetadata() {
        EntityMetadata entityMetadata = converter.apply(Person.class);

        assertEquals("Person", entityMetadata.name());
        assertEquals(Person.class, entityMetadata.type());
        assertEquals(5, entityMetadata.fields().size());
        assertThat(entityMetadata.fieldsName()).contains("_id", "name", "age", "phones");
        ConstructorMetadata constructor = entityMetadata.constructor();
        assertNotNull(constructor);
        assertTrue(constructor.isDefault());

    }

    @Test
    void shouldEntityMetadata2() {
        EntityMetadata entityMetadata = converter.apply(Actor.class);

        assertEquals("Actor", entityMetadata.name());
        assertEquals(Actor.class, entityMetadata.type());

        assertThat(entityMetadata.fieldsName())
                .hasSize(7)
                .contains("_id", "name", "age", "phones", "movieCharacter", "movieRating", "mobile");

    }

    @Test
    void shouldCreateEntityMetadataWithEmbeddedClass() {
        EntityMetadata entityMetadata = converter.apply(Director.class);
        assertEquals("Director", entityMetadata.name());
        assertEquals(Director.class, entityMetadata.type());
        assertEquals(6, entityMetadata.fields().size());
        assertThat(entityMetadata.fieldsName()).contains("_id", "name", "age", "phones", "movie");

    }

    @Test
    void shouldReturnFalseWhenThereIsNotKey() {
        EntityMetadata entityMetadata = converter.apply(Worker.class);
        boolean allMatch = entityMetadata.fields().stream().noneMatch(FieldMetadata::isId);
        assertTrue(allMatch);
    }


    @Test
    void shouldReturnTrueWhenThereIsKey() {
        EntityMetadata entityMetadata = converter.apply(User.class);
        List<FieldMetadata> fields = entityMetadata.fields();

        Predicate<FieldMetadata> hasKeyAnnotation = FieldMetadata::isId;
        assertTrue(fields.stream().anyMatch(hasKeyAnnotation));
        FieldMetadata fieldMetadata = fields.stream().filter(hasKeyAnnotation).findFirst().get();
        assertEquals("_id", fieldMetadata.name());
        assertEquals(MappingType.DEFAULT, fieldMetadata.mappingType());

    }

    @Test
    void shouldReturnErrorWhenThereIsNotConstructor() {
        Assertions.assertThrows(ConstructorException.class, () -> converter.apply(NoConstructorEntity.class));
    }

    @Test
    void shouldReturnWhenIsDefaultConstructor() {
        EntityMetadata entityMetadata = converter.apply(Machine.class);
        List<FieldMetadata> fields = entityMetadata.fields();
        assertEquals(1, fields.size());
    }

    @Test
    void shouldReturnEmptyInheritance() {
        EntityMetadata entityMetadata = converter.apply(Person.class);
        Optional<InheritanceMetadata> inheritance = entityMetadata.inheritance();
        Assertions.assertTrue(inheritance.isEmpty());
    }

    @Test
    void shouldInheritance() {
        EntityMetadata entity = converter.apply(SmallProject.class);
        Assertions.assertEquals(2, entity.fields().size());
        Assertions.assertEquals(SmallProject.class, entity.type());

        InheritanceMetadata inheritance = entity.inheritance()
                .orElseThrow(RuntimeException::new);

        assertEquals("size", inheritance.discriminatorColumn());
        assertEquals("Small", inheritance.discriminatorValue());
        assertEquals(Project.class, inheritance.parent());
    }

    @Test
    void shouldInheritanceNoDiscriminatorValue() {
        EntityMetadata entity = converter.apply(SocialMediaNotification.class);
        Assertions.assertEquals(4, entity.fields().size());
        Assertions.assertEquals(SocialMediaNotification.class, entity.type());

        InheritanceMetadata inheritance = entity.inheritance()
                .orElseThrow(RuntimeException::new);

        assertEquals(DEFAULT_DISCRIMINATOR_COLUMN, inheritance.discriminatorColumn());
        assertEquals("SocialMediaNotification", inheritance.discriminatorValue());
        assertEquals(Notification.class, inheritance.parent());
    }

    @Test
    void shouldInheritanceNoDiscriminatorColumn() {
        EntityMetadata entity = converter.apply(EmailNotification.class);
        Assertions.assertEquals(4, entity.fields().size());
        Assertions.assertEquals(EmailNotification.class, entity.type());

        InheritanceMetadata inheritance = entity.inheritance()
                .orElseThrow(RuntimeException::new);

        assertEquals(DEFAULT_DISCRIMINATOR_COLUMN, inheritance.discriminatorColumn());
        assertEquals("Email", inheritance.discriminatorValue());
        assertEquals(Notification.class, inheritance.parent());
    }

    @Test
    void shouldInheritanceSameParent() {
        EntityMetadata entity = converter.apply(Project.class);
        Assertions.assertEquals(1, entity.fields().size());
        Assertions.assertEquals(Project.class, entity.type());

        InheritanceMetadata inheritance = entity.inheritance()
                .orElseThrow(RuntimeException::new);

        assertEquals("size", inheritance.discriminatorColumn());
        assertEquals("Project", inheritance.discriminatorValue());
        assertEquals(Project.class, inheritance.parent());
        assertEquals(Project.class, inheritance.entity());
    }


    @Test
    void shouldCreateEntityMetadataWithConstructor() {
        EntityMetadata entityMetadata = converter.apply(Computer.class);

        assertEquals("Computer", entityMetadata.name());
        assertEquals(Computer.class, entityMetadata.type());
        assertEquals(5, entityMetadata.fields().size());
        assertThat(entityMetadata.fieldsName()).contains("_id", "name", "age", "model", "price");
        ConstructorMetadata constructor = entityMetadata.constructor();
        assertNotNull(constructor);
        assertFalse(constructor.isDefault());
        assertEquals(5, constructor.parameters().size());
    }

    
    @Test
    void shouldHandleCollectionInterfaceChildren() {
        ClassConverter converter = new ReflectionClassConverter();
        assertDoesNotThrow(() -> converter.apply(JsonContainer.class));
    }
}