/*
 *  Copyright (c) 2018 Otávio Santana and others
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

import jakarta.nosql.tck.entities.Actor;
import jakarta.nosql.tck.entities.Download;
import jakarta.nosql.tck.entities.Movie;
import jakarta.nosql.tck.entities.Person;
import jakarta.nosql.tck.entities.Vendor;
import jakarta.nosql.tck.entities.inheritance.EmailNotification;
import jakarta.nosql.tck.entities.inheritance.LargeProject;
import jakarta.nosql.tck.entities.inheritance.Notification;
import jakarta.nosql.tck.entities.inheritance.Project;
import jakarta.nosql.tck.entities.inheritance.SmallProject;
import jakarta.nosql.tck.entities.inheritance.SmsNotification;
import jakarta.nosql.tck.entities.inheritance.SocialMediaNotification;
import jakarta.nosql.tck.test.CDIExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;


@CDIExtension
public class ReflectionsTest {


    @Inject
    private Reflections reflections;

    @Test
    public void shouldReturnsEntityName() {
        assertEquals("Person", reflections.getEntityName(Person.class));
        assertEquals("movie", reflections.getEntityName(Movie.class));
    }

    @Test
    public void shouldListFields() {

        assertEquals(4, reflections.getFields(Person.class).size());
        assertEquals(6, reflections.getFields(Actor.class).size());

    }

    @Test
    public void shouldReturnColumnName() throws NoSuchFieldException {
        Field phones = Person.class.getDeclaredField("phones");
        Field id = Person.class.getDeclaredField("id");

        assertEquals("phones", reflections.getColumnName(phones));
        assertEquals("id", reflections.getColumnName(id));
        assertEquals("_id", reflections.getIdName(id));
    }

    @Test
    public void shouldGetEntityNameWhenThereIsNoAnnotation(){
        String entityName = reflections.getEntityName(Person.class);
        assertEquals(Person.class.getSimpleName(), entityName);
    }

    @Test
    public void shouldGetEntityNameFromAnnotation() {
        String entityName = reflections.getEntityName(Download.class);
        assertEquals("download", entityName);
        assertEquals("vendors", reflections.getEntityName(Vendor.class));
    }

    @Test
    public void shouldGetEntityFromInheritance() {
        assertEquals("Notification", reflections.getEntityName(SocialMediaNotification.class));
        assertEquals("Notification", reflections.getEntityName(SmsNotification.class));
        assertEquals("Notification", reflections.getEntityName(EmailNotification.class));

        assertEquals("Project", reflections.getEntityName(LargeProject.class));
        assertEquals("Project", reflections.getEntityName(SmallProject.class));
    }

    @Test
    public void shouldReturnEmptyGetInheritance() {
        Optional<InheritanceClassMapping> inheritance = this.reflections.getInheritance(Person.class);
        Assertions.assertTrue(inheritance.isEmpty());
    }

    @Test
    public void shouldReturnGetInheritance() {
        Optional<InheritanceClassMapping> inheritance = this.reflections.getInheritance(LargeProject.class);
        Assertions.assertFalse(inheritance.isEmpty());
        InheritanceClassMapping project = inheritance.get();
        assertEquals("size", project.getDiscriminatorColumn());
        assertEquals("Large", project.getDiscriminatorValue());
        assertEquals(Project.class, project.getParent());
    }

    @Test
    public void shouldReturnGetInheritanceWithoutColumn() {
        Optional<InheritanceClassMapping> inheritance = this.reflections.getInheritance(SmsNotification.class);
        Assertions.assertFalse(inheritance.isEmpty());
        InheritanceClassMapping project = inheritance.get();
        assertEquals("type", project.getDiscriminatorColumn());
        assertEquals("SMS", project.getDiscriminatorValue());
        assertEquals(Notification.class, project.getParent());
    }

    @Test
    public void shouldReturnGetInheritanceWithoutDiscriminatorValue() {
        Optional<InheritanceClassMapping> inheritance = this.reflections.getInheritance(SocialMediaNotification.class);
        Assertions.assertFalse(inheritance.isEmpty());
        InheritanceClassMapping project = inheritance.get();
        assertEquals("type", project.getDiscriminatorColumn());
        assertEquals("SocialMediaNotification", project.getDiscriminatorValue());
        assertEquals(Notification.class, project.getParent());
    }


}