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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.EmailNotification;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.LargeProject;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.Notification;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.NotificationReader;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.Project;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.ProjectManager;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.SmallProject;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.SmsNotification;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.SocialMediaNotification;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class EntityConverterInheritanceTest {

    @Inject
    private EntityConverter converter;

    @DisplayName("Should convert project to small project")
    @Test
    void shouldConvertProjectToSmallProject() {
        CommunicationEntity entity = CommunicationEntity.of("Project");
        entity.add("_id", "Small Project");
        entity.add("investor", "Otavio Santana");
        entity.add("size", "Small");
        Project project = this.converter.toEntity(entity);
        assertThat(project.getName()).isEqualTo("Small Project");
        assertThat(project.getClass()).isEqualTo(SmallProject.class);
        SmallProject smallProject = SmallProject.class.cast(project);
        assertThat(smallProject.getInvestor()).isEqualTo("Otavio Santana");
    }

    @DisplayName("Should convert project to large project")
    @Test
    void shouldConvertProjectToLargeProject() {
        CommunicationEntity entity = CommunicationEntity.of("Project");
        entity.add("_id", "Large Project");
        entity.add("budget", BigDecimal.TEN);
        entity.add("size", "Large");
        Project project = this.converter.toEntity(entity);
        assertThat(project.getName()).isEqualTo("Large Project");
        assertThat(project.getClass()).isEqualTo(LargeProject.class);
        LargeProject smallProject = LargeProject.class.cast(project);
        assertThat(smallProject.getBudget()).isEqualTo(BigDecimal.TEN);
    }

    @DisplayName("Should convert large project to communication entity")
    @Test
    void shouldConvertLargeProjectToCommunicationEntity() {
        LargeProject project = new LargeProject();
        project.setName("Large Project");
        project.setBudget(BigDecimal.TEN);
        CommunicationEntity entity = this.converter.toCommunication(project);
        assertThat(entity).isNotNull();
        assertThat(entity.name()).isEqualTo("Project");
        assertThat(entity.find("_id", String.class).get()).isEqualTo(project.getName());
        assertThat(entity.find("budget", BigDecimal.class).get()).isEqualTo(project.getBudget());
        assertThat(entity.find("size", String.class).get()).isEqualTo("Large");
    }

    @DisplayName("Should convert small project to communication entity")
    @Test
    void shouldConvertSmallProjectToCommunicationEntity() {
        SmallProject project = new SmallProject();
        project.setName("Small Project");
        project.setInvestor("Otavio Santana");
        CommunicationEntity entity = this.converter.toCommunication(project);
        assertThat(entity).isNotNull();
        assertThat(entity.name()).isEqualTo("Project");
        assertThat(entity.find("_id", String.class).get()).isEqualTo(project.getName());
        assertThat(entity.find("investor", String.class).get()).isEqualTo(project.getInvestor());
        assertThat(entity.find("size", String.class).get()).isEqualTo("Small");
    }

    @DisplayName("Should convert project")
    @Test
    void shouldConvertProject() {
        CommunicationEntity entity = CommunicationEntity.of("Project");
        entity.add("_id", "Project");
        entity.add("size", "Project");
        Project project = this.converter.toEntity(entity);
        assertThat(project.getName()).isEqualTo("Project");
    }

    @DisplayName("Should convert project to communication entity")
    @Test
    void shouldConvertProjectToCommunicationEntity() {
        Project project = new Project();
        project.setName("Large Project");
        CommunicationEntity entity = this.converter.toCommunication(project);
        assertThat(entity).isNotNull();
        assertThat(entity.name()).isEqualTo("Project");
        assertThat(entity.find("_id", String.class).get()).isEqualTo(project.getName());
        assertThat(entity.find("size", String.class).get()).isEqualTo("Project");
    }

    @DisplayName("Should convert column entity to social media")
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
        assertThat(notification.getId()).isEqualTo(100L);
        assertThat(notification.getName()).isEqualTo("Social Media");
        assertThat(notification.getNickname()).isEqualTo("otaviojava");
        assertThat(notification.getCreatedOn()).isEqualTo(date);
    }

    @DisplayName("Should convert column entity to sms")
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
        assertThat(notification.getId()).isEqualTo(100L);
        assertThat(notification.getName()).isEqualTo("SMS Notification");
        assertThat(notification.getPhone()).isEqualTo("+351987654123");
        assertThat(notification.getCreatedOn()).isEqualTo(date);
    }

    @DisplayName("Should convert column entity to email")
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
        assertThat(notification.getId()).isEqualTo(100L);
        assertThat(notification.getName()).isEqualTo("Email Notification");
        assertThat(notification.getEmail()).isEqualTo("otavio@otavio.test");
        assertThat(notification.getCreatedOn()).isEqualTo(date);
    }

    @DisplayName("Should convert social media to communication entity")
    @Test
    void shouldConvertSocialMediaToCommunicationEntity(){
        SocialMediaNotification notification = new SocialMediaNotification();
        notification.setId(100L);
        notification.setName("Social Media");
        notification.setCreatedOn(LocalDate.now());
        notification.setNickname("otaviojava");
        CommunicationEntity entity = this.converter.toCommunication(notification);
        assertThat(entity).isNotNull();
        assertThat(entity.name()).isEqualTo("Notification");
        assertThat(entity.find("_id", Long.class).get()).isEqualTo(notification.getId());
        assertThat(entity.find("name", String.class).get()).isEqualTo(notification.getName());
        assertThat(entity.find("nickname", String.class).get()).isEqualTo(notification.getNickname());
        assertThat(entity.find("createdOn", LocalDate.class).get()).isEqualTo(notification.getCreatedOn());
    }

    @DisplayName("Should convert sms to communication entity")
    @Test
    void shouldConvertSmsToCommunicationEntity(){
        SmsNotification notification = new SmsNotification();
        notification.setId(100L);
        notification.setName("SMS");
        notification.setCreatedOn(LocalDate.now());
        notification.setPhone("+351123456987");
        CommunicationEntity entity = this.converter.toCommunication(notification);
        assertThat(entity).isNotNull();
        assertThat(entity.name()).isEqualTo("Notification");
        assertThat(entity.find("_id", Long.class).get()).isEqualTo(notification.getId());
        assertThat(entity.find("name", String.class).get()).isEqualTo(notification.getName());
        assertThat(entity.find("phone", String.class).get()).isEqualTo(notification.getPhone());
        assertThat(entity.find("createdOn", LocalDate.class).get()).isEqualTo(notification.getCreatedOn());
    }

    @DisplayName("Should convert email to communication entity")
    @Test
    void shouldConvertEmailToCommunicationEntity(){
        EmailNotification notification = new EmailNotification();
        notification.setId(100L);
        notification.setName("Email Media");
        notification.setCreatedOn(LocalDate.now());
        notification.setEmail("otavio@otavio.test.com");
        CommunicationEntity entity = this.converter.toCommunication(notification);
        assertThat(entity).isNotNull();
        assertThat(entity.name()).isEqualTo("Notification");
        assertThat(entity.find("_id", Long.class).get()).isEqualTo(notification.getId());
        assertThat(entity.find("name", String.class).get()).isEqualTo(notification.getName());
        assertThat(entity.find("email", String.class).get()).isEqualTo(notification.getEmail());
        assertThat(entity.find("createdOn", LocalDate.class).get()).isEqualTo(notification.getCreatedOn());
    }

    @DisplayName("Should return error when convert missing column")
    @Test
    void shouldReturnErrorWhenConvertMissingColumn(){
        LocalDate date = LocalDate.now();
        CommunicationEntity entity = CommunicationEntity.of("Notification");
        entity.add("_id", 100L);
        entity.add("name", "SMS Notification");
        entity.add("phone", "+351987654123");
        entity.add("createdOn", date);
        assertThatThrownBy(()-> this.converter.toEntity(entity)).isInstanceOf(MappingException.class);
    }

    @DisplayName("Should return error when mismatch field")
    @Test
    void shouldReturnErrorWhenMismatchField() {
        LocalDate date = LocalDate.now();
        CommunicationEntity entity = CommunicationEntity.of("Notification");
        entity.add("_id", 100L);
        entity.add("name", "Email Notification");
        entity.add("email", "otavio@otavio.test");
        entity.add("createdOn", date);
        entity.add("dtype", "Wrong");
        assertThatThrownBy(()-> this.converter.toEntity(entity)).isInstanceOf(MappingException.class);
    }


    @DisplayName("Should convert communication notification reader email")
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
        assertThat(notificationReader).isNotNull();
        assertThat(notificationReader.getNickname()).isEqualTo("poli");
        assertThat(notificationReader.getName()).isEqualTo("Poliana Santana");
        Notification notification = notificationReader.getNotification();
        assertThat(notification).isNotNull();
        assertThat(notification.getClass()).isEqualTo(EmailNotification.class);
        EmailNotification email = (EmailNotification) notification;
        assertThat(email.getId()).isEqualTo(10L);
        assertThat(email.getName()).isEqualTo("News");
        assertThat(email.getEmail()).isEqualTo("otavio@email.com");
    }

    @DisplayName("Should convert communication notification reader sms")
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
        assertThat(notificationReader).isNotNull();
        assertThat(notificationReader.getNickname()).isEqualTo("poli");
        assertThat(notificationReader.getName()).isEqualTo("Poliana Santana");
        Notification notification = notificationReader.getNotification();
        assertThat(notification).isNotNull();
        assertThat(notification.getClass()).isEqualTo(SmsNotification.class);
        SmsNotification sms = (SmsNotification) notification;
        assertThat(sms.getId()).isEqualTo(10L);
        assertThat(sms.getName()).isEqualTo("News");
        assertThat(sms.getPhone()).isEqualTo("123456789");
    }

    @DisplayName("Should convert communication notification reader social")
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
        assertThat(notificationReader).isNotNull();
        assertThat(notificationReader.getNickname()).isEqualTo("poli");
        assertThat(notificationReader.getName()).isEqualTo("Poliana Santana");
        Notification notification = notificationReader.getNotification();
        assertThat(notification).isNotNull();
        assertThat(notification.getClass()).isEqualTo(SocialMediaNotification.class);
        SocialMediaNotification social = (SocialMediaNotification) notification;
        assertThat(social.getId()).isEqualTo(10L);
        assertThat(social.getName()).isEqualTo("News");
        assertThat(social.getNickname()).isEqualTo("123456789");
    }

    @DisplayName("Should convert social communication")
    @Test
    void shouldConvertSocialCommunication() {
        SocialMediaNotification notification = new SocialMediaNotification();
        notification.setId(10L);
        notification.setName("Ada");
        notification.setNickname("ada.lovelace");
        NotificationReader reader = new NotificationReader("otavio", "Otavio", notification);

        CommunicationEntity entity = this.converter.toCommunication(reader);
        assertThat(entity).isNotNull();

        assertThat(entity.name()).isEqualTo("NotificationReader");
        assertThat(entity.find("_id", String.class).get()).isEqualTo("otavio");
        assertThat(entity.find("name", String.class).get()).isEqualTo("Otavio");
        List<Element> elements = entity.find("notification", new TypeReference<List<Element>>() {
        }).get();

        assertThat(elements).contains(Element.of("_id", 10L),
                        Element.of("name", "Ada"),
                        Element.of("dtype", "SocialMediaNotification"),
                        Element.of("nickname", "ada.lovelace"));
    }

    @DisplayName("Should convert convert project manager communication")
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
        assertThat(entity).isNotNull();

        assertThat(entity.name()).isEqualTo("ProjectManager");
        assertThat(entity.find("_id", Long.class).get()).isEqualTo(10L);
        assertThat(entity.find("name", String.class).get()).isEqualTo("manager");

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

    @DisplayName("Should convert convert communication project manager")
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
        assertThat(manager).isNotNull();

        assertThat(manager.getId()).isEqualTo(10L);
        assertThat(manager.getName()).isEqualTo("manager");

        List<Project> projects = manager.getProjects();
        assertThat(projects.size()).isEqualTo(2);
        SmallProject small = (SmallProject) projects.get(0);
        LargeProject large = (LargeProject) projects.get(1);
        assertThat(small).isNotNull();
        assertThat(small.getName()).isEqualTo("small-project");
        assertThat(small.getInvestor()).isEqualTo("investor");

        assertThat(large).isNotNull();
        assertThat(large.getName()).isEqualTo("large-project");
        assertThat(large.getBudget()).isEqualTo(BigDecimal.TEN);
    }


    @Nested
    @DisplayName("When the entity converter inheritance is tested")
    class WhenTheEntityConverterInheritanceIsTested {
    }
}
