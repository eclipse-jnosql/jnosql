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

import jakarta.inject.Inject;
import jakarta.nosql.Convert;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;
import org.eclipse.jnosql.mapping.reflection.entities.Actor;
import org.eclipse.jnosql.mapping.reflection.entities.Download;
import org.eclipse.jnosql.mapping.reflection.entities.Movie;
import org.eclipse.jnosql.mapping.reflection.entities.Person;
import org.eclipse.jnosql.mapping.reflection.entities.Vendor;
import org.eclipse.jnosql.mapping.reflection.entities.Worker;
import org.eclipse.jnosql.mapping.reflection.entities.constructor.Smartphone;
import org.eclipse.jnosql.mapping.reflection.entities.constructor.Tablet;
import org.eclipse.jnosql.mapping.reflection.entities.inheritance.EmailNotification;
import org.eclipse.jnosql.mapping.reflection.entities.inheritance.LargeProject;
import org.eclipse.jnosql.mapping.reflection.entities.inheritance.Notification;
import org.eclipse.jnosql.mapping.reflection.entities.inheritance.Project;
import org.eclipse.jnosql.mapping.reflection.entities.inheritance.SmallProject;
import org.eclipse.jnosql.mapping.reflection.entities.inheritance.SmsNotification;
import org.eclipse.jnosql.mapping.reflection.entities.inheritance.SocialMediaNotification;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Optional;

import static jakarta.nosql.DiscriminatorColumn.DEFAULT_DISCRIMINATOR_COLUMN;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@EnableAutoWeld
@AddPackages(value = Convert.class)
@AddPackages(value = DefaultGroupEntityMetadata.class)
class ReflectionsTest {

    @Inject
    private Reflections reflections;

    @Test
    void shouldReturnsEntityName() {
        assertSoftly(softly -> {
            softly.assertThat(reflections.getEntityName(Person.class))
                    .as("getting entity name from annotated class with @Entity without name")
                    .isEqualTo("Person");
            softly.assertThat(reflections.getEntityName(Movie.class))
                    .as("getting entity name from annotated class with @Entity with defined name")
                    .isEqualTo("movie");
            softly.assertThat(reflections.getEntityName(Smartphone.class))
                    .as("getting entity name from annotated record class with @Entity without name")
                    .isEqualTo("Smartphone");
            softly.assertThat(reflections.getEntityName(Tablet.class))
                    .as("getting entity name from annotated record class with @Entity with defined name")
                    .isEqualTo("tablet");
        });
    }

    @Test
    void shouldReturnsConstructor() {
        assertSoftly(softly -> {

            Constructor<Person> personConstructor = reflections.getConstructor(Person.class);
            softly.assertThat(personConstructor)
                    .as("getting an non-args constructor from annotated class " +
                            "with @Entity")
                    .isNotNull();

            Constructor<Smartphone> smartphoneConstructor = reflections.getConstructor(Smartphone.class);
            softly.assertThat(smartphoneConstructor)
                    .as("getting constructor from annotated entity record class " +
                            "with @Entity with all field annotated or @Id or @Column")
                    .isNotNull();

            Constructor<Tablet> tableConstructor = reflections.getConstructor(Tablet.class);
            softly.assertThat(tableConstructor)
                    .as("getting constructor from annotated entity record class " +
                            "with @Entity with field not annotated with @Column")
                    .isNotNull();
        });
    }

    @Test
    void shouldListFields() {
        assertSoftly(softly -> {
            softly.assertThat(reflections.getFields(Person.class))
                    .as("list fields from a class with field not annotated with @Column")
                    .hasSize(5);
            softly.assertThat(reflections.getFields(Actor.class))
                    .as("list fields from a class that extends a class with field not annotated with @Column")
                    .hasSize(7);
            softly.assertThat(reflections.getFields(Smartphone.class))
                    .as("list fields from a record class with all fields annotated with @Id or @Column")
                    .hasSize(2);
            softly.assertThat(reflections.getFields(Tablet.class))
                    .as("list fields from a record class with field not annotated with @Id or @Column")
                    .hasSize(2);
        });
    }

    @Test
    void shouldReturnColumnName() throws NoSuchFieldException {
        Field phones = Person.class.getDeclaredField("phones");
        Field id = Person.class.getDeclaredField("id");

        assertEquals("phones", reflections.getColumnName(phones));
        assertEquals("id", reflections.getColumnName(id));
        assertEquals("_id", reflections.getIdName(id));
    }

    @Test
    void shouldGetEntityNameWhenThereIsNoAnnotation() {
        String entityName = reflections.getEntityName(Person.class);
        assertEquals(Person.class.getSimpleName(), entityName);
    }

    @Test
    void shouldGetEntityNameFromAnnotation() {
        String entityName = reflections.getEntityName(Download.class);
        assertEquals("download", entityName);
        assertEquals("vendors", reflections.getEntityName(Vendor.class));
    }

    @Test
    void shouldGetEntityFromInheritance() {
        assertEquals("Notification", reflections.getEntityName(SocialMediaNotification.class));
        assertEquals("Notification", reflections.getEntityName(SmsNotification.class));
        assertEquals("Notification", reflections.getEntityName(EmailNotification.class));

        assertEquals("Project", reflections.getEntityName(LargeProject.class));
        assertEquals("Project", reflections.getEntityName(SmallProject.class));
    }

    @Test
    void shouldReturnEmptyGetInheritance() {
        Optional<InheritanceMetadata> inheritance = this.reflections.getInheritance(Person.class);
        assertTrue(inheritance.isEmpty());
    }

    @Test
    void shouldReturnGetInheritance() {
        Optional<InheritanceMetadata> inheritance = this.reflections.getInheritance(LargeProject.class);
        assertFalse(inheritance.isEmpty());
        InheritanceMetadata project = inheritance.get();
        assertEquals("size", project.discriminatorColumn());
        assertEquals("Large", project.discriminatorValue());
        assertEquals(Project.class, project.parent());
        assertEquals(LargeProject.class, project.entity());
    }

    @Test
    void shouldReturnGetInheritanceWithoutColumn() {
        Optional<InheritanceMetadata> inheritance = this.reflections.getInheritance(SmsNotification.class);
        assertFalse(inheritance.isEmpty());
        InheritanceMetadata project = inheritance.get();
        assertEquals(DEFAULT_DISCRIMINATOR_COLUMN, project.discriminatorColumn());
        assertEquals("SMS", project.discriminatorValue());
        assertEquals(Notification.class, project.parent());
        assertEquals(SmsNotification.class, project.entity());
    }

    @Test
    void shouldReturnGetInheritanceWithoutDiscriminatorValue() {
        Optional<InheritanceMetadata> inheritance = this.reflections.getInheritance(SocialMediaNotification.class);
        assertFalse(inheritance.isEmpty());
        InheritanceMetadata project = inheritance.get();
        assertEquals(DEFAULT_DISCRIMINATOR_COLUMN, project.discriminatorColumn());
        assertEquals("SocialMediaNotification", project.discriminatorValue());
        assertEquals(Notification.class, project.parent());
        assertEquals(SocialMediaNotification.class, project.entity());
    }

    @Test
    void shouldGetInheritanceParent() {
        Optional<InheritanceMetadata> inheritance = this.reflections.getInheritance(Project.class);
        assertFalse(inheritance.isEmpty());
        InheritanceMetadata project = inheritance.get();
        assertEquals("size", project.discriminatorColumn());
        assertEquals("Project", project.discriminatorValue());
        assertEquals(Project.class, project.parent());
        assertEquals(Project.class, project.entity());
    }

    @Test
    void shouldReturnHasInheritanceAnnotation() {
        assertFalse(this.reflections.hasInheritanceAnnotation(Person.class));
        assertFalse(this.reflections.hasInheritanceAnnotation(Worker.class));
        assertFalse(this.reflections.hasInheritanceAnnotation(SmsNotification.class));
        assertFalse(this.reflections.hasInheritanceAnnotation(SmallProject.class));

        assertTrue(this.reflections.hasInheritanceAnnotation(Notification.class));
        assertTrue(this.reflections.hasInheritanceAnnotation(Project.class));
    }


}