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
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.data.exceptions.MappingException;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.EmailNotification;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.LargeProject;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.Notification;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.NotificationReader;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.Project;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.ProjectManager;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.SmallProject;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.SmsNotification;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.SocialMediaNotification;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.core.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class EntityConverterInheritanceTest {

    @Inject
    private EntityConverter converter;

    @Test
    void shouldConvertProjectToSmallProject() {
        CommunicationEntity entity = CommunicationEntity.of("Project");
        entity.add("_id", "Small Project");
        entity.add("investor", "Otavio Santana");
        entity.add("size", "Small");
        Project project = this.converter.toEntity(entity);
        assertEquals("Small Project", project.getName());
        assertEquals(SmallProject.class, project.getClass());
        SmallProject smallProject = SmallProject.class.cast(project);
        assertEquals("Otavio Santana", smallProject.getInvestor());
    }

    @Test
    void shouldConvertProjectToLargeProject() {
        CommunicationEntity entity = CommunicationEntity.of("Project");
        entity.add("_id", "Large Project");
        entity.add("budget", BigDecimal.TEN);
        entity.add("size", "Large");
        Project project = this.converter.toEntity(entity);
        assertEquals("Large Project", project.getName());
        assertEquals(LargeProject.class, project.getClass());
        LargeProject smallProject = LargeProject.class.cast(project);
        assertEquals(BigDecimal.TEN, smallProject.getBudget());
    }

    @Test
    void shouldConvertLargeProjectToCommunicationEntity() {
        LargeProject project = new LargeProject();
        project.setName("Large Project");
        project.setBudget(BigDecimal.TEN);
        CommunicationEntity entity = this.converter.toCommunication(project);
        assertNotNull(entity);
        assertEquals("Project", entity.name());
        assertEquals(project.getName(), entity.find("_id", String.class).get());
        assertEquals(project.getBudget(), entity.find("budget", BigDecimal.class).get());
        assertEquals("Large", entity.find("size", String.class).get());
    }

    @Test
    void shouldConvertSmallProjectToCommunicationEntity() {
        SmallProject project = new SmallProject();
        project.setName("Small Project");
        project.setInvestor("Otavio Santana");
        CommunicationEntity entity = this.converter.toCommunication(project);
        assertNotNull(entity);
        assertEquals("Project", entity.name());
        assertEquals(project.getName(), entity.find("_id", String.class).get());
        assertEquals(project.getInvestor(), entity.find("investor", String.class).get());
        assertEquals("Small", entity.find("size", String.class).get());
    }

    @Test
    void shouldConvertProject() {
        CommunicationEntity entity = CommunicationEntity.of("Project");
        entity.add("_id", "Project");
        entity.add("size", "Project");
        Project project = this.converter.toEntity(entity);
        assertEquals("Project", project.getName());
    }

    @Test
    void shouldConvertProjectToCommunicationEntity() {
        Project project = new Project();
        project.setName("Large Project");
        CommunicationEntity entity = this.converter.toCommunication(project);
        assertNotNull(entity);
        assertEquals("Project", entity.name());
        assertEquals(project.getName(), entity.find("_id", String.class).get());
        assertEquals("Project", entity.find("size", String.class).get());
    }

    @Test
    void shouldConvertColumnEntityToSocialMedia(){
        LocalDate date = LocalDate.now();
        CommunicationEntity entity = CommunicationEntity.of("Notification");
        entity.add("_id", 100L);
        entity.add("name", "Social Media");
        entity.add("nickname", "otaviojava");
        entity.add("createdOn",date);
        entity.add("dtype", SocialMediaNotification.class.getSimpleName());
        SocialMediaNotification notification = this.converter.toEntity(entity);
        assertEquals(100L, notification.getId());
        assertEquals("Social Media", notification.getName());
        assertEquals("otaviojava", notification.getNickname());
        assertEquals(date, notification.getCreatedOn());
    }

    @Test
    void shouldConvertColumnEntityToSms(){
        LocalDate date = LocalDate.now();
        CommunicationEntity entity = CommunicationEntity.of("Notification");
        entity.add("_id", 100L);
        entity.add("name", "SMS Notification");
        entity.add("phone", "+351987654123");
        entity.add("createdOn", date);
        entity.add("dtype", "SMS");
        SmsNotification notification = this.converter.toEntity(entity);
        Assertions.assertEquals(100L, notification.getId());
        Assertions.assertEquals("SMS Notification", notification.getName());
        Assertions.assertEquals("+351987654123", notification.getPhone());
        assertEquals(date, notification.getCreatedOn());
    }

    @Test
    void shouldConvertColumnEntityToEmail(){
        LocalDate date = LocalDate.now();
        CommunicationEntity entity = CommunicationEntity.of("Notification");
        entity.add("_id", 100L);
        entity.add("name", "Email Notification");
        entity.add("email", "otavio@otavio.test");
        entity.add("createdOn", date);
        entity.add("dtype", "Email");
        EmailNotification notification = this.converter.toEntity(entity);
        Assertions.assertEquals(100L, notification.getId());
        Assertions.assertEquals("Email Notification", notification.getName());
        Assertions.assertEquals("otavio@otavio.test", notification.getEmail());
        assertEquals(date, notification.getCreatedOn());
    }

    @Test
    void shouldConvertSocialMediaToCommunicationEntity(){
        SocialMediaNotification notification = new SocialMediaNotification();
        notification.setId(100L);
        notification.setName("Social Media");
        notification.setCreatedOn(LocalDate.now());
        notification.setNickname("otaviojava");
        CommunicationEntity entity = this.converter.toCommunication(notification);
        assertNotNull(entity);
        assertEquals("Notification", entity.name());
        assertEquals(notification.getId(), entity.find("_id", Long.class).get());
        assertEquals(notification.getName(), entity.find("name", String.class).get());
        assertEquals(notification.getNickname(), entity.find("nickname", String.class).get());
        assertEquals(notification.getCreatedOn(), entity.find("createdOn", LocalDate.class).get());
    }

    @Test
    void shouldConvertSmsToCommunicationEntity(){
        SmsNotification notification = new SmsNotification();
        notification.setId(100L);
        notification.setName("SMS");
        notification.setCreatedOn(LocalDate.now());
        notification.setPhone("+351123456987");
        CommunicationEntity entity = this.converter.toCommunication(notification);
        assertNotNull(entity);
        assertEquals("Notification", entity.name());
        assertEquals(notification.getId(), entity.find("_id", Long.class).get());
        assertEquals(notification.getName(), entity.find("name", String.class).get());
        assertEquals(notification.getPhone(), entity.find("phone", String.class).get());
        assertEquals(notification.getCreatedOn(), entity.find("createdOn", LocalDate.class).get());
    }

    @Test
    void shouldConvertEmailToCommunicationEntity(){
        EmailNotification notification = new EmailNotification();
        notification.setId(100L);
        notification.setName("Email Media");
        notification.setCreatedOn(LocalDate.now());
        notification.setEmail("otavio@otavio.test.com");
        CommunicationEntity entity = this.converter.toCommunication(notification);
        assertNotNull(entity);
        assertEquals("Notification", entity.name());
        assertEquals(notification.getId(), entity.find("_id", Long.class).get());
        assertEquals(notification.getName(), entity.find("name", String.class).get());
        assertEquals(notification.getEmail(), entity.find("email", String.class).get());
        assertEquals(notification.getCreatedOn(), entity.find("createdOn", LocalDate.class).get());
    }

    @Test
    void shouldReturnErrorWhenConvertMissingColumn(){
        LocalDate date = LocalDate.now();
        CommunicationEntity entity = CommunicationEntity.of("Notification");
        entity.add("_id", 100L);
        entity.add("name", "SMS Notification");
        entity.add("phone", "+351987654123");
        entity.add("createdOn", date);
        Assertions.assertThrows(MappingException.class, ()-> this.converter.toEntity(entity));
    }

    @Test
    void shouldReturnErrorWhenMismatchField() {
        LocalDate date = LocalDate.now();
        CommunicationEntity entity = CommunicationEntity.of("Notification");
        entity.add("_id", 100L);
        entity.add("name", "Email Notification");
        entity.add("email", "otavio@otavio.test");
        entity.add("createdOn", date);
        entity.add("dtype", "Wrong");
        Assertions.assertThrows(MappingException.class, ()-> this.converter.toEntity(entity));
    }



    @Test
    void shouldConvertCommunicationNotificationReaderEmail() {
        CommunicationEntity entity = CommunicationEntity.of("NotificationReader");
        entity.add("_id", "poli");
        entity.add("name", "Poliana Santana");
        entity.add("notification", Arrays.asList(
                Element.of("_id", 10L),
                Element.of("name", "News"),
                Element.of("email", "otavio@email.com"),
                Element.of("_id", LocalDate.now()),
                Element.of("dtype", "Email")
        ));

        NotificationReader notificationReader = converter.toEntity(entity);
        assertNotNull(notificationReader);
        Assertions.assertEquals("poli", notificationReader.getNickname());
        Assertions.assertEquals("Poliana Santana", notificationReader.getName());
        Notification notification = notificationReader.getNotification();
        assertNotNull(notification);
        Assertions.assertEquals(EmailNotification.class, notification.getClass());
        EmailNotification email = (EmailNotification) notification;
        Assertions.assertEquals(10L, email.getId());
        Assertions.assertEquals("News", email.getName());
        Assertions.assertEquals("otavio@email.com", email.getEmail());
    }

    @Test
    void shouldConvertCommunicationNotificationReaderSms() {
        CommunicationEntity entity = CommunicationEntity.of("NotificationReader");
        entity.add("_id", "poli");
        entity.add("name", "Poliana Santana");
        entity.add("notification", Arrays.asList(
                Element.of("_id", 10L),
                Element.of("name", "News"),
                Element.of("phone", "123456789"),
                Element.of("_id", LocalDate.now()),
                Element.of("dtype", "SMS")
        ));

        NotificationReader notificationReader = converter.toEntity(entity);
        assertNotNull(notificationReader);
        Assertions.assertEquals("poli", notificationReader.getNickname());
        Assertions.assertEquals("Poliana Santana", notificationReader.getName());
        Notification notification = notificationReader.getNotification();
        assertNotNull(notification);
        Assertions.assertEquals(SmsNotification.class, notification.getClass());
        SmsNotification sms = (SmsNotification) notification;
        Assertions.assertEquals(10L, sms.getId());
        Assertions.assertEquals("News", sms.getName());
        Assertions.assertEquals("123456789", sms.getPhone());
    }

    @Test
    void shouldConvertCommunicationNotificationReaderSocial() {
        CommunicationEntity entity = CommunicationEntity.of("NotificationReader");
        entity.add("_id", "poli");
        entity.add("name", "Poliana Santana");
        entity.add("notification", Arrays.asList(
                Element.of("_id", 10L),
                Element.of("name", "News"),
                Element.of("nickname", "123456789"),
                Element.of("_id", LocalDate.now()),
                Element.of("dtype", "SocialMediaNotification")
        ));

        NotificationReader notificationReader = converter.toEntity(entity);
        assertNotNull(notificationReader);
        Assertions.assertEquals("poli", notificationReader.getNickname());
        Assertions.assertEquals("Poliana Santana", notificationReader.getName());
        Notification notification = notificationReader.getNotification();
        assertNotNull(notification);
        Assertions.assertEquals(SocialMediaNotification.class, notification.getClass());
        SocialMediaNotification social = (SocialMediaNotification) notification;
        Assertions.assertEquals(10L, social.getId());
        Assertions.assertEquals("News", social.getName());
        Assertions.assertEquals("123456789", social.getNickname());
    }

    @Test
    void shouldConvertSocialCommunication() {
        SocialMediaNotification notification = new SocialMediaNotification();
        notification.setId(10L);
        notification.setName("Ada");
        notification.setNickname("ada.lovelace");
        NotificationReader reader = new NotificationReader("otavio", "Otavio", notification);

        CommunicationEntity entity = this.converter.toCommunication(reader);
        assertNotNull(entity);

        assertEquals("NotificationReader", entity.name());
        assertEquals("otavio", entity.find("_id", String.class).get());
        assertEquals("Otavio", entity.find("name", String.class).get());
        List<Element> elements = entity.find("notification", new TypeReference<List<Element>>() {
        }).get();

        assertThat(elements).contains(Element.of("_id", 10L),
                        Element.of("name", "Ada"),
                        Element.of("dtype", "SocialMediaNotification"),
                        Element.of("nickname", "ada.lovelace"));
    }

    @Test
    void shouldConvertConvertProjectManagerCommunication() {
        LargeProject large = new LargeProject();
        large.setBudget(BigDecimal.TEN);
        large.setName("large");

        SmallProject small = new SmallProject();
        small.setInvestor("new investor");
        small.setName("Start up");

        List<Project> projects = new ArrayList<>();
        projects.add(large);
        projects.add(small);

        ProjectManager manager = ProjectManager.of(10L, "manager", projects);
        CommunicationEntity entity = this.converter.toCommunication(manager);
        assertNotNull(entity);

        assertEquals("ProjectManager", entity.name());
        assertEquals(10L, entity.find("_id", Long.class).get());
        assertEquals("manager", entity.find("name", String.class).get());

        List<List<Element>> elements = (List<List<Element>>) entity.find("projects").get().get();

        List<Element> largeCommunication = elements.get(0);
        List<Element> smallCommunication = elements.get(1);
        assertThat(largeCommunication).contains(
                Element.of("_id", "large"),
                Element.of("size", "Large"),
                Element.of("budget", BigDecimal.TEN)
        );

        assertThat(smallCommunication).contains(
                Element.of("size", "Small"),
                Element.of("investor", "new investor"),
                Element.of("_id", "Start up")
        );

    }

    @Test
    void shouldConvertConvertCommunicationProjectManager() {
        CommunicationEntity communication = CommunicationEntity.of("ProjectManager");
        communication.add("_id", 10L);
        communication.add("name", "manager");
        List<List<Element>> elements = new ArrayList<>();
        elements.add(Arrays.asList(
                Element.of("_id","small-project"),
                Element.of("size","Small"),
                Element.of("investor","investor")
        ));
        elements.add(Arrays.asList(
                Element.of("_id","large-project"),
                Element.of("size","Large"),
                Element.of("budget",BigDecimal.TEN)
        ));
        communication.add("projects", elements);

        ProjectManager manager = converter.toEntity(communication);
        assertNotNull(manager);

        assertEquals(10L, manager.getId());
        assertEquals("manager", manager.getName());

        List<Project> projects = manager.getProjects();
        assertEquals(2, projects.size());
        SmallProject small = (SmallProject) projects.get(0);
        LargeProject large = (LargeProject) projects.get(1);
        assertNotNull(small);
        assertEquals("small-project", small.getName());
        assertEquals("investor", small.getInvestor());

        assertNotNull(large);
        assertEquals("large-project", large.getName());
        assertEquals(BigDecimal.TEN, large.getBudget());
    }

}
