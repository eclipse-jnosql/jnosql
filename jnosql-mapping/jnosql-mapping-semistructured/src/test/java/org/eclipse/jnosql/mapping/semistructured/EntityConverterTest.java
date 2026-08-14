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

import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.entities.Actor;
import org.eclipse.jnosql.mapping.semistructured.entities.Address;
import org.eclipse.jnosql.mapping.semistructured.entities.AppointmentBook;
import org.eclipse.jnosql.mapping.semistructured.entities.Book;
import org.eclipse.jnosql.mapping.semistructured.entities.Citizen;
import org.eclipse.jnosql.mapping.semistructured.entities.Contact;
import org.eclipse.jnosql.mapping.semistructured.entities.ContactType;
import org.eclipse.jnosql.mapping.semistructured.entities.Director;
import org.eclipse.jnosql.mapping.semistructured.entities.Download;
import org.eclipse.jnosql.mapping.semistructured.entities.ElectricEngine;
import org.eclipse.jnosql.mapping.semistructured.entities.Form;
import org.eclipse.jnosql.mapping.semistructured.entities.GasEngine;
import org.eclipse.jnosql.mapping.semistructured.entities.Job;
import org.eclipse.jnosql.mapping.semistructured.entities.Machine;
import org.eclipse.jnosql.mapping.semistructured.entities.MainStepType;
import org.eclipse.jnosql.mapping.semistructured.entities.MobileApp;
import org.eclipse.jnosql.mapping.semistructured.entities.Money;
import org.eclipse.jnosql.mapping.semistructured.entities.Movie;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.eclipse.jnosql.mapping.semistructured.entities.Program;
import org.eclipse.jnosql.mapping.semistructured.entities.SocialMediaContact;
import org.eclipse.jnosql.mapping.semistructured.entities.Transition;
import org.eclipse.jnosql.mapping.semistructured.entities.Vendor;
import org.eclipse.jnosql.mapping.semistructured.entities.Wine;
import org.eclipse.jnosql.mapping.semistructured.entities.WineFactory;
import org.eclipse.jnosql.mapping.semistructured.entities.Worker;
import org.eclipse.jnosql.mapping.semistructured.entities.WorkflowStep;
import org.eclipse.jnosql.mapping.semistructured.entities.ZipCode;
import org.eclipse.jnosql.mapping.semistructured.entities.constructor.BookBag;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.jnosql.mapping.semistructured.entities.StepTransitionReason.REPEAT;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions(ReflectionEntityMetadataExtension.class)
class EntityConverterTest {

    @Inject
    private DefaultEntityConverter converter;

    private Element[] columns;

    private final Actor actor = Actor.actorBuilder().withAge()
            .withId()
            .withName()
            .withPhones(asList("234", "2342"))
            .withMovieCharacter(Collections.singletonMap("JavaZone", "Jedi"))
            .withMovieRating(Collections.singletonMap("JavaZone", 10))
            .build();

    @BeforeEach
    void init() {

        columns = new Element[]{Element.of("_id", 12L),
                Element.of("age", 10), Element.of("name", "Otavio"),
                Element.of("phones", asList("234", "2342"))
                , Element.of("movieCharacter", Collections.singletonMap("JavaZone", "Jedi"))
                , Element.of("movieRating", Collections.singletonMap("JavaZone", 10))};
    }

    @Test
    @DisplayName("Should have a default constructor for CDI ")
    void shouldHaveDefaultConstructor() {
        DefaultEntityConverter converter = new DefaultEntityConverter();
        assertThat(converter).isNotNull();
    }

    @DisplayName("Should convert entity from column entity")
    @Test
    void shouldConvertEntityFromColumnEntity() {

        Person person = Person.builder().age()
                .id(12)
                .name("Otavio")
                .phones(asList("234", "2342")).build();

        CommunicationEntity entity = converter.toCommunication(person);
        assertThat(entity.name()).isEqualTo("Person");
        assertThat(entity.size()).isEqualTo(5);
        assertThat(entity.elements()).contains(Element.of("_id", 12L),
                Element.of("age", 10), Element.of("name", "Otavio"),
                Element.of("phones", Arrays.asList("234", "2342")));

    }

    @DisplayName("Should convert column entity from entity")
    @Test
    void shouldConvertColumnEntityFromEntity() {

        CommunicationEntity entity = converter.toCommunication(actor);
        assertThat(entity.name()).isEqualTo("Actor");
        assertThat(entity.size()).isEqualTo(7);

        assertThat(entity.elements()).contains(columns);
    }

    @DisplayName("Should convert column entity to entity")
    @Test
    void shouldConvertColumnEntityToEntity() {
        CommunicationEntity entity = CommunicationEntity.of("Actor");
        Stream.of(columns).forEach(entity::add);

        Actor actor = converter.toEntity(Actor.class, entity);
        assertThat(actor).isNotNull();
        assertThat(actor.getAge()).isEqualTo(10);
        assertThat(actor.getId()).isEqualTo(12L);
        assertThat(actor.getPhones()).isEqualTo(asList("234", "2342"));
        assertThat(actor.getMovieCharacter()).isEqualTo(Collections.singletonMap("JavaZone", "Jedi"));
        assertThat(actor.getMovieRating()).isEqualTo(Collections.singletonMap("JavaZone", 10));
    }

    @DisplayName("Should convert column entity to entity 2")
    @Test
    void shouldConvertColumnEntityToEntity2() {
        CommunicationEntity entity = CommunicationEntity.of("Actor");
        Stream.of(columns).forEach(entity::add);

        Actor actor = converter.toEntity(entity);
        assertThat(actor).isNotNull();
        assertThat(actor.getAge()).isEqualTo(10);
        assertThat(actor.getId()).isEqualTo(12L);
        assertThat(actor.getPhones()).isEqualTo(asList("234", "2342"));
        assertThat(actor.getMovieCharacter()).isEqualTo(Collections.singletonMap("JavaZone", "Jedi"));
        assertThat(actor.getMovieRating()).isEqualTo(Collections.singletonMap("JavaZone", 10));
    }

    @DisplayName("Should convert column entity to exist entity")
    @Test
    void shouldConvertColumnEntityToExistEntity() {
        CommunicationEntity entity = CommunicationEntity.of("Actor");
        Stream.of(columns).forEach(entity::add);
        Actor actor = Actor.actorBuilder().build();
        Actor result = converter.toEntity(actor, entity);

        assertThat(result).isSameAs(actor);
        assertThat(actor.getAge()).isEqualTo(10);
        assertThat(actor.getId()).isEqualTo(12L);
        assertThat(actor.getPhones()).isEqualTo(asList("234", "2342"));
        assertThat(actor.getMovieCharacter()).isEqualTo(Collections.singletonMap("JavaZone", "Jedi"));
        assertThat(actor.getMovieRating()).isEqualTo(Collections.singletonMap("JavaZone", 10));
    }

    @DisplayName("Should return error when to entity is null")
    @Test
    void shouldReturnErrorWhenToEntityIsNull() {
        CommunicationEntity entity = CommunicationEntity.of("Actor");
        Stream.of(columns).forEach(entity::add);
        Actor actor = Actor.actorBuilder().build();

        assertThatThrownBy(() -> converter.toEntity(null, entity)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> converter.toEntity(actor, null)).isInstanceOf(NullPointerException.class);
    }


    @DisplayName("Should convert entity to column entity 2")
    @Test
    void shouldConvertEntityToColumnEntity2() {

        Movie movie = new Movie("Matrix", 2012, Collections.singleton("Actor"));
        Director director = Director.builderDirector().withAge(12)
                .withId(12)
                .withName("Otavio")
                .withPhones(asList("234", "2342")).withMovie(movie).build();

        CommunicationEntity entity = converter.toCommunication(director);
        assertThat(entity.size()).isEqualTo(6);

        assertThat(director.getName()).isEqualTo(getValue(entity.find("name")));
        assertThat(director.getAge()).isEqualTo(getValue(entity.find("age")));
        assertThat(director.getId()).isEqualTo(getValue(entity.find("_id")));
        assertThat(director.getPhones()).isEqualTo(getValue(entity.find("phones")));


        Element subColumn = entity.find("movie").get();
        List<Element> columns = subColumn.get(new TypeReference<>() {
        });

        assertThat(columns.size()).isEqualTo(3);
        assertThat(subColumn.name()).isEqualTo("movie");
        assertThat(columns.stream().filter(c -> "title".equals(c.name())).findFirst().get().get()).isEqualTo(movie.getTitle());
        assertThat(columns.stream().filter(c -> "year".equals(c.name())).findFirst().get().get()).isEqualTo(movie.getYear());
        assertThat(columns.stream().filter(c -> "actors".equals(c.name())).findFirst().get().get()).isEqualTo(movie.getActors());


    }

    @DisplayName("Should convert to embedded class when has sub column")
    @Test
    void shouldConvertToEmbeddedClassWhenHasSubColumn() {
        Movie movie = new Movie("Matrix", 2012, Collections.singleton("Actor"));
        Director director = Director.builderDirector().withAge(12)
                .withId(12)
                .withName("Otavio")
                .withPhones(asList("234", "2342")).withMovie(movie).build();

        CommunicationEntity entity = converter.toCommunication(director);
        Director director1 = converter.toEntity(entity);

        assertThat(director1.getMovie()).isEqualTo(movie);
        assertThat(director1.getName()).isEqualTo(director.getName());
        assertThat(director1.getAge()).isEqualTo(director.getAge());
        assertThat(director1.getId()).isEqualTo(director.getId());
    }

    @DisplayName("Should convert to embedded class when has sub column 2")
    @Test
    void shouldConvertToEmbeddedClassWhenHasSubColumn2() {
        Movie movie = new Movie("Matrix", 2012, singleton("Actor"));
        Director director = Director.builderDirector().withAge(12)
                .withId(12)
                .withName("Otavio")
                .withPhones(asList("234", "2342")).withMovie(movie).build();

        CommunicationEntity entity = converter.toCommunication(director);
        entity.remove("movie");
        entity.add(Element.of("movie", Arrays.asList(Element.of("title", "Matrix"),
                Element.of("year", 2012), Element.of("actors", singleton("Actor")))));
        Director director1 = converter.toEntity(entity);

        assertThat(director1.getMovie()).isEqualTo(movie);
        assertThat(director1.getName()).isEqualTo(director.getName());
        assertThat(director1.getAge()).isEqualTo(director.getAge());
        assertThat(director1.getId()).isEqualTo(director.getId());
    }

    @DisplayName("Should convert to embedded class when has sub column 3")
    @Test
    void shouldConvertToEmbeddedClassWhenHasSubColumn3() {
        Movie movie = new Movie("Matrix", 2012, singleton("Actor"));
        Director director = Director.builderDirector().withAge(12)
                .withId(12)
                .withName("Otavio")
                .withPhones(asList("234", "2342")).withMovie(movie).build();

        CommunicationEntity entity = converter.toCommunication(director);
        entity.remove("movie");
        Map<String, Object> map = new HashMap<>();
        map.put("title", "Matrix");
        map.put("year", 2012);
        map.put("actors", singleton("Actor"));

        entity.add(Element.of("movie", map));
        Director director1 = converter.toEntity(entity);

        assertThat(director1.getMovie()).isEqualTo(movie);
        assertThat(director1.getName()).isEqualTo(director.getName());
        assertThat(director1.getAge()).isEqualTo(director.getAge());
        assertThat(director1.getId()).isEqualTo(director.getId());
    }

    @DisplayName("Should convert to column when ha converter")
    @Test
    void shouldConvertToColumnWhenHaConverter() {
        Worker worker = new Worker();
        Job job = new Job();
        job.setCity("Sao Paulo");
        job.setDescription("Java Developer");
        worker.setName("Bob");
        worker.setSalary(new Money("BRL", BigDecimal.TEN));
        worker.setJob(job);
        CommunicationEntity entity = converter.toCommunication(worker);
        assertThat(entity.name()).isEqualTo("Worker");
        assertThat(entity.find("name").get().get()).isEqualTo("Bob");
        assertThat(entity.find("city").get().get()).isEqualTo("Sao Paulo");
        assertThat(entity.find("description").get().get()).isEqualTo("Java Developer");
        assertThat(entity.find("money").get().get()).isEqualTo("BRL 10");
    }

    @DisplayName("Should convert to entity when has converter")
    @Test
    void shouldConvertToEntityWhenHasConverter() {
        Worker worker = new Worker();
        Job job = new Job();
        job.setCity("Sao Paulo");
        job.setDescription("Java Developer");
        worker.setName("Bob");
        worker.setSalary(new Money("BRL", BigDecimal.TEN));
        worker.setJob(job);
        CommunicationEntity entity = converter.toCommunication(worker);
        Worker worker1 = converter.toEntity(entity);
        assertThat(worker1.getSalary()).isEqualTo(worker.getSalary());
        assertThat(worker1.getJob().getCity()).isEqualTo(job.getCity());
        assertThat(worker1.getJob().getDescription()).isEqualTo(job.getDescription());
    }

    @DisplayName("Should convert embeddable lazily")
    @Test
    void shouldConvertEmbeddableLazily() {
        CommunicationEntity entity = CommunicationEntity.of("Worker");
        entity.add("name", "Otavio");
        entity.add("money", "BRL 10");

        Worker worker = converter.toEntity(entity);
        assertThat(worker.getName()).isEqualTo("Otavio");
        assertThat(worker.getSalary()).isEqualTo(new Money("BRL", BigDecimal.TEN));
        assertThat(worker.getJob()).isNull();

    }


    @DisplayName("Should convert to list embeddable")
    @Test
    void shouldConvertToListEmbeddable() {
        AppointmentBook appointmentBook = new AppointmentBook("ids");
        appointmentBook.add(Contact.builder().withType(ContactType.EMAIL)
                .withName("Ada").withInformation("ada@lovelace.com").build());
        appointmentBook.add(Contact.builder().withType(ContactType.MOBILE)
                .withName("Ada").withInformation("11 1231231 123").build());
        appointmentBook.add(Contact.builder().withType(ContactType.PHONE)
                .withName("Ada").withInformation("12 123 1231 123123").build());

        CommunicationEntity entity = converter.toCommunication(appointmentBook);
        Element contacts = entity.find("contacts").get();
        assertThat(appointmentBook.getId()).isEqualTo("ids");
        List<List<Element>> columns = (List<List<Element>>) contacts.get();

        assertThat(columns.stream().flatMap(Collection::stream)
                .filter(c -> c.name().equals("contact_name"))
                .count()).isEqualTo(3L);
    }

    @DisplayName("Should convert from list embeddable")
    @Test
    void shouldConvertFromListEmbeddable() {
        CommunicationEntity entity = CommunicationEntity.of("AppointmentBook");
        entity.add(Element.of("_id", "ids"));
        List<List<Element>> columns = new ArrayList<>();

        columns.add(asList(Element.of("contact_name", "Ada"), Element.of("type", ContactType.EMAIL),
                Element.of("information", "ada@lovelace.com")));

        columns.add(asList(Element.of("contact_name", "Ada"), Element.of("type", ContactType.MOBILE),
                Element.of("information", "11 1231231 123")));

        columns.add(asList(Element.of("contact_name", "Ada"), Element.of("type", ContactType.PHONE),
                Element.of("information", "phone")));

        entity.add(Element.of("contacts", columns));

        AppointmentBook appointmentBook = converter.toEntity(entity);

        List<Contact> contacts = appointmentBook.getContacts();
        assertThat(appointmentBook.getId()).isEqualTo("ids");
        assertThat(contacts.stream().map(Contact::getName).distinct().findFirst().get()).isEqualTo("Ada");

    }


    @DisplayName("Should convert sub entity")
    @Test
    void shouldConvertSubEntity() {
        ZipCode zipcode = new ZipCode();
        zipcode.setZip("12321");
        zipcode.setPlusFour("1234");

        Address address = new Address();
        address.setCity("Salvador");
        address.setState("Bahia");
        address.setStreet("Rua Engenheiro Jose Anasoh");
        address.setZipCode(zipcode);

        CommunicationEntity columnEntity = converter.toCommunication(address);
        List<Element> columns = columnEntity.elements();
        assertThat(columnEntity.name()).isEqualTo("Address");
        assertThat(columns.size()).isEqualTo(4);
        List<Element> zip = columnEntity.find("zipCode").map(d -> d.get(new TypeReference<List<Element>>() {
        })).orElse(Collections.emptyList());

        assertThat(getValue(columnEntity.find("street"))).isEqualTo("Rua Engenheiro Jose Anasoh");
        assertThat(getValue(columnEntity.find("city"))).isEqualTo("Salvador");
        assertThat(getValue(columnEntity.find("state"))).isEqualTo("Bahia");
        assertThat(getValue(zip.stream().filter(d -> d.name().equals("zip")).findFirst())).isEqualTo("12321");
        assertThat(getValue(zip.stream().filter(d -> d.name().equals("plusFour")).findFirst())).isEqualTo("1234");
    }

    @DisplayName("Should convert column in sub entity")
    @Test
    void shouldConvertColumnInSubEntity() {

        CommunicationEntity entity = CommunicationEntity.of("Address");

        entity.add(Element.of("street", "Rua Engenheiro Jose Anasoh"));
        entity.add(Element.of("city", "Salvador"));
        entity.add(Element.of("state", "Bahia"));
        entity.add(Element.of("zipCode", Arrays.asList(
                Element.of("zip", "12321"),
                Element.of("plusFour", "1234"))));
        Address address = converter.toEntity(entity);

        assertThat(address.getStreet()).isEqualTo("Rua Engenheiro Jose Anasoh");
        assertThat(address.getCity()).isEqualTo("Salvador");
        assertThat(address.getState()).isEqualTo("Bahia");
        assertThat(address.getZipCode().getZip()).isEqualTo("12321");
        assertThat(address.getZipCode().getPlusFour()).isEqualTo("1234");

    }

    @DisplayName("Should return null when there is not sub entity")
    @Test
    void shouldReturnNullWhenThereIsNotSubEntity() {
        CommunicationEntity entity = CommunicationEntity.of("Address");

        entity.add(Element.of("street", "Rua Engenheiro Jose Anasoh"));
        entity.add(Element.of("city", "Salvador"));
        entity.add(Element.of("state", "Bahia"));
        entity.add(Element.of("zip", "12321"));
        entity.add(Element.of("plusFour", "1234"));

        Address address = converter.toEntity(entity);

        assertThat(address.getStreet()).isEqualTo("Rua Engenheiro Jose Anasoh");
        assertThat(address.getCity()).isEqualTo("Salvador");
        assertThat(address.getState()).isEqualTo("Bahia");
        assertThat(address.getZipCode()).isNull();
    }

    @DisplayName("Should convert and do not use unmodifiable collection")
    @Test
    void shouldConvertAndDoNotUseUnmodifiableCollection() {
        CommunicationEntity entity = CommunicationEntity.of("vendors");
        entity.add("name", "name");
        entity.add("prefixes", Arrays.asList("value", "value2"));

        Vendor vendor = converter.toEntity(entity);
        vendor.add("value3");

        assertThat(vendor.getPrefixes().size()).isEqualTo(3);

    }

    @DisplayName("Should convert entity to document with array")
    @Test
    void shouldConvertEntityToDocumentWithArray() {
        byte[] contents = {1, 2, 3, 4, 5, 6};

        CommunicationEntity entity = CommunicationEntity.of("download");
        entity.add("_id", 1L);
        entity.add("contents", contents);

        Download download = converter.toEntity(entity);
        assertThat(download.getId()).isEqualTo(1L);
        assertThat(download.getContents()).isEqualTo(contents);
    }

    @DisplayName("Should convert document to entity with array")
    @Test
    void shouldConvertDocumentToEntityWithArray() {
        byte[] contents = {1, 2, 3, 4, 5, 6};

        Download download = new Download();
        download.setId(1L);
        download.setContents(contents);

        CommunicationEntity entity = converter.toCommunication(download);

        assertThat(entity.find("_id").get().get()).isEqualTo(1L);
        final byte[] bytes = entity.find("contents").map(v -> v.get(byte[].class)).orElse(new byte[0]);
        assertThat(bytes).isEqualTo(contents);
    }

    @DisplayName("Should create user scope")
    @Test
    void shouldCreateUserScope() {
        CommunicationEntity entity = CommunicationEntity.of("UserScope");
        entity.add("_id", "userName");
        entity.add("scope", "scope");
        entity.add("properties", Collections.singletonList(Element.of("halo", "weld")));

        UserScope user = converter.toEntity(entity);
        assertThat(user).isNotNull();
        assertThat(user.getUserName()).isEqualTo("userName");
        assertThat(user.getScope()).isEqualTo("scope");
        assertThat(user.getProperties()).isEqualTo(Collections.singletonMap("halo", "weld"));

    }

    @DisplayName("Should create user scope 2")
    @Test
    void shouldCreateUserScope2() {
        CommunicationEntity entity = CommunicationEntity.of("UserScope");
        entity.add("_id", "userName");
        entity.add("scope", "scope");
        entity.add("properties", Element.of("halo", "weld"));

        UserScope user = converter.toEntity(entity);
        assertThat(user).isNotNull();
        assertThat(user.getUserName()).isEqualTo("userName");
        assertThat(user.getScope()).isEqualTo("scope");
        assertThat(user.getProperties()).isEqualTo(Collections.singletonMap("halo", "weld"));

    }

    @DisplayName("Should create lazily entity")
    @Test
    void shouldCreateLazilyEntity() {
        CommunicationEntity entity = CommunicationEntity.of("Citizen");
        entity.add("id", "10");
        entity.add("name", "Salvador");

        Citizen citizen = converter.toEntity(entity);
        assertThat(citizen).isNotNull();
        assertThat(citizen.getCity()).isNull();
    }


    @DisplayName("Should return null value present")
    @Test
    void shouldReturnNullValuePresent() {
        Person person = Person.builder().build();

        CommunicationEntity entity = converter.toCommunication(person);
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(entity.find("name")).isPresent();
            soft.assertThat(entity.find("age")).isPresent();
            soft.assertThat(entity.find("phones")).isPresent();
            soft.assertThat(entity.find("ignore")).isNotPresent();

            soft.assertThat(entity.find("name", String.class)).isNotPresent();
            soft.assertThat(entity.find("phones", String.class)).isNotPresent();
        });
    }

    @DisplayName("Should convert workflow")
    @Test
    void shouldConvertWorkflow() {
        var workflowStep = WorkflowStep.builder()
                .id("id")
                .key("key")
                .workflowSchemaKey("workflowSchemaKey")
                .stepName("stepName")
                .mainStepType(MainStepType.MAIN)
                .stepNo(1)
                .componentConfigurationKey("componentConfigurationKey")
                .relationTypeKey("relationTypeKey")
                .availableTransitions(List.of(new Transition("TEST_WORKFLOW_STEP_KEY", REPEAT,
                        null, List.of("ADMIN"))))
                .build();

        var document = this.converter.toCommunication(workflowStep);
        WorkflowStep result = this.converter.toEntity(document);
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(result).isNotNull();
            soft.assertThat(result.id()).isEqualTo("id");
            soft.assertThat(result.key()).isEqualTo("key");
            soft.assertThat(result.workflowSchemaKey()).isEqualTo("workflowSchemaKey");
            soft.assertThat(result.stepName()).isEqualTo("stepName");
            soft.assertThat(result.mainStepType()).isEqualTo(MainStepType.MAIN);
            soft.assertThat(result.stepNo()).isEqualTo(1L);
            soft.assertThat(result.componentConfigurationKey()).isEqualTo("componentConfigurationKey");
            soft.assertThat(result.relationTypeKey()).isEqualTo("relationTypeKey");
            soft.assertThat(result.availableTransitions()).hasSize(1);
            soft.assertThat(result.availableTransitions().getFirst().targetWorkflowStepKey()).isEqualTo("TEST_WORKFLOW_STEP_KEY");
            soft.assertThat(result.availableTransitions().getFirst().stepTransitionReason()).isEqualTo(REPEAT);
            soft.assertThat(result.availableTransitions().getFirst().mailTemplateKey()).isNull();
            soft.assertThat(result.availableTransitions().getFirst().restrictedRoleGroups()).hasSize(1);
            soft.assertThat(result.availableTransitions().getFirst().restrictedRoleGroups().getFirst()).isEqualTo("ADMIN");
        });

    }

    @DisplayName("Should update embeddable 2")
    @Test
    void shouldUpdateEmbeddable2() {
        var workflowStep = WorkflowStep.builder()
                .id("id")
                .key("key")
                .workflowSchemaKey("workflowSchemaKey")
                .stepName("stepName")
                .mainStepType(MainStepType.MAIN)
                .stepNo(null)
                .componentConfigurationKey("componentConfigurationKey")
                .relationTypeKey("relationTypeKey")
                .availableTransitions(null)
                .build();
        var document = this.converter.toCommunication(workflowStep);
        WorkflowStep result = this.converter.toEntity(document);
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(result).isNotNull();
            soft.assertThat(result.id()).isEqualTo("id");
            soft.assertThat(result.key()).isEqualTo("key");
            soft.assertThat(result.workflowSchemaKey()).isEqualTo("workflowSchemaKey");
            soft.assertThat(result.stepName()).isEqualTo("stepName");
            soft.assertThat(result.mainStepType()).isEqualTo(MainStepType.MAIN);
            soft.assertThat(result.stepNo()).isNull();
            soft.assertThat(result.componentConfigurationKey()).isEqualTo("componentConfigurationKey");
            soft.assertThat(result.relationTypeKey()).isEqualTo("relationTypeKey");
            soft.assertThat(result.availableTransitions()).isNull();

        });

    }

    @DisplayName("Should ignore when null")
    @Test
    void shouldIgnoreWhenNull() {
        CommunicationEntity entity = CommunicationEntity.of("SocialMediaContact");
        entity.add("_id", "id");
        entity.add("name", "Twitter");
        entity.add("users", null);

        SocialMediaContact socialMediaContact = converter.toEntity(entity);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(socialMediaContact).isNotNull();
            soft.assertThat(socialMediaContact.getId()).isEqualTo("id");
            soft.assertThat(socialMediaContact.getName()).isEqualTo("Twitter");
            soft.assertThat(socialMediaContact.getUsers()).isNull();
        });
    }

    @DisplayName("Should convert group embeddable")
    @Test
    void shouldConvertGroupEmbeddable() {
        CommunicationEntity entity = CommunicationEntity.of("Wine");
        entity.add("_id", "id");
        entity.add("name", "Vin Blanc");
        entity.add("factory", List.of(Element.of("name", "Napa Valley Factory"),
                Element.of("location", "Napa Valley")));

        Wine wine = converter.toEntity(entity);

        SoftAssertions.assertSoftly(soft -> {
            WineFactory factory = wine.getFactory();
            soft.assertThat(wine).isNotNull();
            soft.assertThat(wine.getId()).isEqualTo("id");
            soft.assertThat(wine.getName()).isEqualTo("Vin Blanc");
            soft.assertThat(factory).isNotNull();
            soft.assertThat(factory.getName()).isEqualTo("Napa Valley Factory");
            soft.assertThat(factory.getLocation()).isEqualTo("Napa Valley");
        });
    }

    @DisplayName("Should convert group embeddable to communication")
    @Test
    void shouldConvertGroupEmbeddableToCommunication() {

        Wine wine = Wine.of("id", "Vin Blanc", WineFactory.of("Napa Valley Factory", "Napa Valley"));


        var communication = converter.toCommunication(wine);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(communication).isNotNull();
            soft.assertThat(communication.name()).isEqualTo("Wine");
            soft.assertThat(communication.find("_id").orElseThrow().get()).isEqualTo("id");
            soft.assertThat(communication.find("name").orElseThrow().get()).isEqualTo("Vin Blanc");
            communication.find("factory").ifPresent(e -> {
                List<Element> elements = e.get(new TypeReference<>() {
                });
                soft.assertThat(elements).hasSize(2);
                soft.assertThat(elements.stream().filter(c -> "name".equals(c.name())).findFirst().orElseThrow().get())
                        .isEqualTo("Napa Valley Factory");
                soft.assertThat(elements.stream().filter(c -> "location".equals(c.name())).findFirst().orElseThrow().get())
                        .isEqualTo("Napa Valley");
            });

        });
    }


    @DisplayName("Should convert generic types")
    @Test
    void shouldConvertGenericTypes() {
        CommunicationEntity communication = CommunicationEntity.of("Form");
        communication.add("_id", "form");
        communication.add("questions", Arrays.asList(
                Element.of("question1", true),
                Element.of("question2", false),
                Element.of("question3", List.of(Element.of("advanced", true),
                        Element.of("visible", "true")))
        ));

        Form form = converter.toEntity(communication);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(form.getId()).isEqualTo("form");
            softly.assertThat(form.getQuestions()).containsEntry("question1", true);
            softly.assertThat(form.getQuestions()).containsEntry("question2", false);
            softly.assertThat(form.getQuestions()).containsEntry("question3", Map.of("advanced", true, "visible", "true"));
        });
    }

    @DisplayName("Should convert generic types with converter as electric")
    @Test
    void shouldConvertGenericTypesWithConverterAsElectric() {
        var communication = CommunicationEntity.of("Machine");
        communication.add("_id", UUID.randomUUID().toString());
        communication.add("manufacturer", "Tesla");
        communication.add("year", 2022);
        communication.add("engine", Arrays.asList(
                Element.of("type", "electric"),
                Element.of("horsepower", 300)
        ));

        Machine machine = converter.toEntity(communication);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(machine.getId()).isNotNull();
            softly.assertThat(machine.getManufacturer()).isEqualTo("Tesla");
            softly.assertThat(machine.getYear()).isEqualTo(2022);
            softly.assertThat(machine.getEngine()).isNotNull();
            softly.assertThat(machine.getEngine().getHorsepower()).isEqualTo(300);
            softly.assertThat(machine.getEngine()).isInstanceOf(ElectricEngine.class);
        });
    }

    @DisplayName("Should convert generic types with converter gas")
    @Test
    void shouldConvertGenericTypesWithConverterGas() {
        var communication = CommunicationEntity.of("Machine");
        communication.add("_id", UUID.randomUUID().toString());
        communication.add("manufacturer", "Mustang");
        communication.add("year", 2021);
        communication.add("engine", Arrays.asList(
                Element.of("type", "gas"),
                Element.of("horsepower", 450)
        ));

        Machine machine = converter.toEntity(communication);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(machine.getId()).isNotNull();
            softly.assertThat(machine.getManufacturer()).isEqualTo("Mustang");
            softly.assertThat(machine.getYear()).isEqualTo(2021);
            softly.assertThat(machine.getEngine()).isNotNull();
            softly.assertThat(machine.getEngine().getHorsepower()).isEqualTo(450);
            softly.assertThat(machine.getEngine()).isInstanceOf(GasEngine.class);
        });
    }

    @DisplayName("Should convert to array")
    @Test
    void shouldConvertToArray() {
        CommunicationEntity entity = CommunicationEntity.of("Person");
        entity.add("_id", 12L);
        entity.add("name", "Otavio");
        entity.add("age", 10);
        entity.add("phones", asList("234", "2342"));
        entity.add("mobiles", asList("234", "2342"));

        Person person = this.converter.toEntity(entity);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(person.getId()).isEqualTo(12L);
            softly.assertThat(person.getName()).isEqualTo("Otavio");
            softly.assertThat(person.getAge()).isEqualTo(10);
            softly.assertThat(person.getPhones()).containsExactly("234", "2342");
            softly.assertThat(person.getMobiles()).containsExactly("234", "2342");
        });
    }

    @DisplayName("Should convert to array in array")
    @Test
    void shouldConvertToArrayInArray() {
        CommunicationEntity entity = CommunicationEntity.of("Person");
        entity.add("_id", 12L);
        entity.add("name", "Otavio");
        entity.add("age", 10);
        entity.add("phones", asList("234", "2342"));
        entity.add("mobiles", new String[]{"234", "2342"});

        Person person = this.converter.toEntity(entity);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(person.getId()).isEqualTo(12L);
            softly.assertThat(person.getName()).isEqualTo("Otavio");
            softly.assertThat(person.getAge()).isEqualTo(10);
            softly.assertThat(person.getPhones()).containsExactly("234", "2342");
            softly.assertThat(person.getMobiles()).containsExactly("234", "2342");
        });
    }

    @DisplayName("Should convert from array embeddable")
    @Test
    void shouldConvertFromArrayEmbeddable() {
        CommunicationEntity entity = CommunicationEntity.of("AppointmentBook");
        entity.add(Element.of("_id", "ids"));
        List<List<Element>> columns = new ArrayList<>();

        columns.add(asList(Element.of("contact_name", "Ada"), Element.of("type", ContactType.EMAIL),
                Element.of("information", "ada@lovelace.com")));

        columns.add(asList(Element.of("contact_name", "Ada"), Element.of("type", ContactType.MOBILE),
                Element.of("information", "11 1231231 123")));

        columns.add(asList(Element.of("contact_name", "Ada"), Element.of("type", ContactType.PHONE),
                Element.of("information", "phone")));

        entity.add(Element.of("contacts", columns));
        entity.add(Element.of("network", columns));

        AppointmentBook appointmentBook = converter.toEntity(entity);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(appointmentBook.getId()).isEqualTo("ids");
            softly.assertThat(appointmentBook.getContacts()).hasSize(3);
            softly.assertThat(appointmentBook.getNetwork()).hasSize(3);
        });
    }

    @DisplayName("Should convert to array embeddable")
    @Test
    void shouldConvertToArrayEmbeddable() {
        var email = Contact.builder().withType(ContactType.EMAIL)
                .withName("Ada").withInformation("ada@lovelace.com").build();
        var mobile = Contact.builder().withType(ContactType.MOBILE)
                .withName("Ada").withInformation("11 1231231 123").build();
        var ada = Contact.builder().withType(ContactType.PHONE)
                .withName("Ada").withInformation("12 123 1231 123123").build();
        AppointmentBook appointmentBook = new AppointmentBook("ids");
        appointmentBook.add(ada);
        appointmentBook.add(email);
        appointmentBook.add(mobile);
        appointmentBook.setNetwork(new Contact[]{ada, email, mobile});

        CommunicationEntity entity = converter.toCommunication(appointmentBook);
        Element contacts = entity.find("contacts").get();
        Element network = entity.find("network").get();
        assertThat(appointmentBook.getId()).isEqualTo("ids");
        List<List<Element>> columns = (List<List<Element>>) contacts.get();

        assertThat(columns.stream().flatMap(Collection::stream)
                .filter(c -> c.name().equals("contact_name"))
                .count()).isEqualTo(3L);

        List<List<Element>> columns2 = (List<List<Element>>) network.get();

        assertThat(columns2.stream().flatMap(Collection::stream)
                .filter(c -> c.name().equals("contact_name"))
                .count()).isEqualTo(3L);
    }

    @DisplayName("Should convert entity from column entity with array")
    @Test
    void shouldConvertEntityFromColumnEntityWithArray() {

        var person = Person.builder().age()
                .id(12)
                .name("Otavio")
                .phones(asList("234", "2342"))
                .mobiles(new String[]{"234", "2342"})
                .build();

        var entity = converter.toCommunication(person);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(entity).isNotNull();
            softly.assertThat(entity.name()).isEqualTo("Person");
            softly.assertThat(entity.size()).isEqualTo(5);
            softly.assertThat(entity.find("_id").orElseThrow().get()).isEqualTo(12L);
            softly.assertThat(entity.find("age").orElseThrow().get()).isEqualTo(10);
            softly.assertThat(entity.find("name").orElseThrow().get()).isEqualTo("Otavio");
            softly.assertThat(entity.find("phones").orElseThrow().get()).isEqualTo(asList("234", "2342"));
            softly.assertThat(entity.find("mobiles", new TypeReference<List<String>>() {
            }).orElseThrow()).contains("234", "2342");
        });


    }

    @DisplayName("Should convert entity from record entity with column array")
    @Test
    void shouldConvertEntityFromRecordEntityWithColumnArray() {

        var effectiveJava = Book.builder()
                .withId(10L)
                .withName("Effective Java")
                .withAge(2018 - Year.now().getValue())
                .build();
        var cleanCode = Book.builder()
                .withId(1L)
                .withName("Clen Code")
                .withAge(2008 - Year.now().getValue())
                .build();

        var bagBook = new BookBag("Max",
                new Book[]{effectiveJava, cleanCode});

        var entity = converter.toCommunication(bagBook);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(entity).isNotNull();
            softly.assertThat(entity.name()).isEqualTo(BookBag.class.getSimpleName());
            softly.assertThat(entity.size()).isEqualTo(2);
            softly.assertThat(entity.find("_id").orElseThrow().get()).isEqualTo(bagBook.owner());

            var books = entity.find("books", new TypeReference<List<List>>() {
            }).orElseThrow();

            softly.assertThat(books)
                    .hasSize(2);

            BiConsumer<CommunicationEntity, Book> itemsAssertions = (actualBook, expectedBook) -> {

                softly.assertThat(actualBook.find("_id"))
                        .as("should found the entity's _id field")
                        .isPresent()
                        .get()
                        .as("invalid field type of the entity's _id field")
                        .isInstanceOf(Element.class)
                        .extracting(Element::get)
                        .as("invalid Book's id")
                        .isEqualTo(expectedBook.getId());

                softly.assertThat(actualBook.find("name"))
                        .as("should found the entity's name field")
                        .isPresent()
                        .get()
                        .as("invalid field type of the entity's name field")
                        .isInstanceOf(Element.class)
                        .extracting(Element::get)
                        .as("invalid Book's name")
                        .isEqualTo(expectedBook.getName());

                softly.assertThat(actualBook.find("age"))
                        .as("should found the entity's age field")
                        .isPresent()
                        .get()
                        .as("invalid field type of the entity's age field")
                        .isInstanceOf(Element.class)
                        .extracting(Element::get)
                        .as("invalid Book's age")
                        .isEqualTo(expectedBook.getAge());
            };

            itemsAssertions.accept(CommunicationEntity.of("effectiveJava", books.get(0)), effectiveJava);
            itemsAssertions.accept(CommunicationEntity.of("cleanCode", books.get(1)), cleanCode);

        });

    }

    @DisplayName("Should convert from flat communication from entity")
    @Test
    void shouldConvertFromFlatCommunicationFromEntity() {

        CommunicationEntity communication = CommunicationEntity.of(Course.class.getSimpleName());
        communication.add("_id", 12);
        communication.add("studentId", "123");
        communication.add("fullName", "Ada");
        Course entity = converter.toEntity(communication);

        SoftAssertions.assertSoftly(softly->{
            softly.assertThat(entity).isNotNull();
            softly.assertThat(entity.getStudent()).isNotNull();
            softly.assertThat(entity.getStudent().getStudentId()).isEqualTo("123");
            softly.assertThat(entity.getStudent().getFullName()).isEqualTo("Ada");
            softly.assertThat(entity.getId()).isEqualTo("12");
        });
    }

    @DisplayName("Should convert from flat communication from entity to communication")
    @Test
    void shouldConvertFromFlatCommunicationFromEntityToCommunication() {
        var course = new Course("12", new Student("123", "Ada"));
        CommunicationEntity communication = converter.toCommunication(course);

        SoftAssertions.assertSoftly(softly->{
            softly.assertThat(communication).isNotNull();
            softly.assertThat(communication.find("_id").orElseThrow().get()).isEqualTo("12");
            softly.assertThat(communication.find("studentId").orElseThrow().get()).isEqualTo("123");
            softly.assertThat(communication.find("fullName").orElseThrow().get()).isEqualTo("Ada");
        });
    }

    @DisplayName("Should convert from map")
    @Test
    void shouldConvertFromMap() {
        var program = Program.of(
                "Renamer",
                Map.of("twitter", "x")
        );
        var computer = MobileApp.of("Computer",Map.of("Renamer", program));

        var entity = converter.toCommunication(computer);

        SoftAssertions.assertSoftly(softly->{
           softly.assertThat(entity).isNotNull();
            softly.assertThat(entity.name()).isEqualTo("MobileApp");
            softly.assertThat(entity.size()).isEqualTo(2);
            softly.assertThat(entity.find("_id").orElseThrow().get()).isEqualTo("Computer");
            var programs = entity.find("programs").orElseThrow();
            var elements = programs.get(new TypeReference<List<Element>>() {});
            softly.assertThat(elements).hasSize(1);
            Element element = elements.getFirst();
            softly.assertThat(element.name()).isEqualTo("Renamer");
            var subDocument = element.get(new TypeReference<List<Element>>() {});
            softly.assertThat(subDocument).isNotNull().hasSize(2);
            softly.assertThat(subDocument.get(0).name()).isEqualTo("_id");
            softly.assertThat(subDocument.get(1).name()).isEqualTo("socialMedia");
        });
    }

    @DisplayName("Should convert to map")
    @Test
    void shouldConvertToMap() {

        var communication = CommunicationEntity.of("MobileApp");
        communication.add("_id", "Computer");
        communication.add("programs", List.of(
                Element.of("Renamer", List.of(
                        Element.of("_id", "Renamer"),
                        Element.of("socialMedia", Map.of("twitter", "x"))
                ))
        ));

        MobileApp entity = converter.toEntity(communication);

        SoftAssertions.assertSoftly(softly->{
           softly.assertThat(entity).isNotNull();
            softly.assertThat(entity.getName()).isEqualTo("Computer");
            softly.assertThat(entity.getPrograms()).isNotNull();
            softly.assertThat(entity.getPrograms()).hasSize(1);
            Program renamer = entity.getPrograms().get("Renamer");
            softly.assertThat(renamer).isNotNull();
            softly.assertThat(renamer.getName()).isEqualTo("Renamer");
            softly.assertThat(renamer.getSocialMedia()).isNotNull();
        });
    }

    @DisplayName("Should convert from maps")
    @Test
    void shouldConvertFromMaps() {
        var program = Program.of(
                "Renamer",
                Map.of("twitter", "x")
        );
        var program2 = Program.of(
                "Java",
                Map.of("Instagram", "insta")
        );
        var computer = MobileApp.of("Computer",Map.of("Renamer", program, "Java", program2));

        var entity = converter.toCommunication(computer);

        SoftAssertions.assertSoftly(softly->{
            softly.assertThat(entity).isNotNull();
            softly.assertThat(entity.name()).isEqualTo("MobileApp");
            softly.assertThat(entity.size()).isEqualTo(2);
            softly.assertThat(entity.find("_id").orElseThrow().get()).isEqualTo("Computer");
            var programs = entity.find("programs").orElseThrow();
            var elements = programs.get(new TypeReference<List<Element>>() {});
            softly.assertThat(elements).hasSize(2);
            var element = elements.stream().filter(e -> e.name().equals("Renamer")).findFirst().orElseThrow();
            softly.assertThat(element.name()).isEqualTo("Renamer");
            var subDocument = element.get(new TypeReference<List<Element>>() {});
            softly.assertThat(subDocument).isNotNull().hasSize(2);
            softly.assertThat(subDocument.get(0).name()).isEqualTo("_id");
            softly.assertThat(subDocument.get(1).name()).isEqualTo("socialMedia");

            var element2 = elements.stream().filter(e -> e.name().equals("Java")).findFirst().orElseThrow();
            softly.assertThat(element2.name()).isEqualTo("Java");
            var subDocument2 = element2.get(new TypeReference<List<Element>>() {});
            softly.assertThat(subDocument2).isNotNull().hasSize(2);
            softly.assertThat(subDocument2.get(0).name()).isEqualTo("_id");
            softly.assertThat(subDocument2.get(1).name()).isEqualTo("socialMedia");
        });
    }

    @DisplayName("Should convert to maps")
    @Test
    void shouldConvertToMaps() {

        var communication = CommunicationEntity.of("MobileApp");
        communication.add("_id", "Computer");
        communication.add("programs", List.of(
                Element.of("Renamer", List.of(
                        Element.of("_id", "Renamer"),
                        Element.of("socialMedia", Map.of("twitter", "x"))
                )),
                Element.of("Java", List.of(
                        Element.of("_id", "Java"),
                        Element.of("socialMedia", Map.of("instagram", "insta"))
                ))
        ));

        MobileApp entity = converter.toEntity(communication);

        SoftAssertions.assertSoftly(softly->{
            softly.assertThat(entity).isNotNull();
            softly.assertThat(entity.getName()).isEqualTo("Computer");
            softly.assertThat(entity.getPrograms()).isNotNull();
            softly.assertThat(entity.getPrograms()).hasSize(2);
            var renamer = entity.getPrograms().get("Renamer");
            softly.assertThat(renamer).isNotNull();
            softly.assertThat(renamer.getName()).isEqualTo("Renamer");
            softly.assertThat(renamer.getSocialMedia()).isNotNull();

            var java = entity.getPrograms().get("Java");
            softly.assertThat(java).isNotNull();
            softly.assertThat(java.getName()).isEqualTo("Java");
            softly.assertThat(java.getSocialMedia()).isNotNull();
        });
    }


    private Object getValue(Optional<Element> column) {
        return column.map(Element::value).map(Value::get).orElse(null);
    }


    @Nested
    @DisplayName("When the entity converter is tested")
    class WhenTheEntityConverterIsTested {
    }
}
