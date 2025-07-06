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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.mapping.semistructured.entities.StepTransitionReason.REPEAT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void shouldConvertEntityFromColumnEntity() {

        Person person = Person.builder().age()
                .id(12)
                .name("Otavio")
                .phones(asList("234", "2342")).build();

        CommunicationEntity entity = converter.toCommunication(person);
        assertEquals("Person", entity.name());
        assertEquals(5, entity.size());
        assertThat(entity.elements()).contains(Element.of("_id", 12L),
                Element.of("age", 10), Element.of("name", "Otavio"),
                Element.of("phones", Arrays.asList("234", "2342")));

    }

    @Test
    void shouldConvertColumnEntityFromEntity() {

        CommunicationEntity entity = converter.toCommunication(actor);
        assertEquals("Actor", entity.name());
        assertEquals(7, entity.size());

        assertThat(entity.elements()).contains(columns);
    }

    @Test
    void shouldConvertColumnEntityToEntity() {
        CommunicationEntity entity = CommunicationEntity.of("Actor");
        Stream.of(columns).forEach(entity::add);

        Actor actor = converter.toEntity(Actor.class, entity);
        assertNotNull(actor);
        assertEquals(10, actor.getAge());
        assertEquals(12L, actor.getId());
        assertEquals(asList("234", "2342"), actor.getPhones());
        assertEquals(Collections.singletonMap("JavaZone", "Jedi"), actor.getMovieCharacter());
        assertEquals(Collections.singletonMap("JavaZone", 10), actor.getMovieRating());
    }

    @Test
    void shouldConvertColumnEntityToEntity2() {
        CommunicationEntity entity = CommunicationEntity.of("Actor");
        Stream.of(columns).forEach(entity::add);

        Actor actor = converter.toEntity(entity);
        assertNotNull(actor);
        assertEquals(10, actor.getAge());
        assertEquals(12L, actor.getId());
        assertEquals(asList("234", "2342"), actor.getPhones());
        assertEquals(Collections.singletonMap("JavaZone", "Jedi"), actor.getMovieCharacter());
        assertEquals(Collections.singletonMap("JavaZone", 10), actor.getMovieRating());
    }

    @Test
    void shouldConvertColumnEntityToExistEntity() {
        CommunicationEntity entity = CommunicationEntity.of("Actor");
        Stream.of(columns).forEach(entity::add);
        Actor actor = Actor.actorBuilder().build();
        Actor result = converter.toEntity(actor, entity);

        assertSame(actor, result);
        assertEquals(10, actor.getAge());
        assertEquals(12L, actor.getId());
        assertEquals(asList("234", "2342"), actor.getPhones());
        assertEquals(Collections.singletonMap("JavaZone", "Jedi"), actor.getMovieCharacter());
        assertEquals(Collections.singletonMap("JavaZone", 10), actor.getMovieRating());
    }

    @Test
    void shouldReturnErrorWhenToEntityIsNull() {
        CommunicationEntity entity = CommunicationEntity.of("Actor");
        Stream.of(columns).forEach(entity::add);
        Actor actor = Actor.actorBuilder().build();

        assertThrows(NullPointerException.class, () -> converter.toEntity(null, entity));

        assertThrows(NullPointerException.class, () -> converter.toEntity(actor, null));
    }


    @Test
    void shouldConvertEntityToColumnEntity2() {

        Movie movie = new Movie("Matrix", 2012, Collections.singleton("Actor"));
        Director director = Director.builderDirector().withAge(12)
                .withId(12)
                .withName("Otavio")
                .withPhones(asList("234", "2342")).withMovie(movie).build();

        CommunicationEntity entity = converter.toCommunication(director);
        assertEquals(6, entity.size());

        assertEquals(getValue(entity.find("name")), director.getName());
        assertEquals(getValue(entity.find("age")), director.getAge());
        assertEquals(getValue(entity.find("_id")), director.getId());
        assertEquals(getValue(entity.find("phones")), director.getPhones());


        Element subColumn = entity.find("movie").get();
        List<Element> columns = subColumn.get(new TypeReference<>() {
        });

        assertEquals(3, columns.size());
        assertEquals("movie", subColumn.name());
        assertEquals(movie.getTitle(), columns.stream().filter(c -> "title".equals(c.name())).findFirst().get().get());
        assertEquals(movie.getYear(), columns.stream().filter(c -> "year".equals(c.name())).findFirst().get().get());
        assertEquals(movie.getActors(), columns.stream().filter(c -> "actors".equals(c.name())).findFirst().get().get());


    }

    @Test
    void shouldConvertToEmbeddedClassWhenHasSubColumn() {
        Movie movie = new Movie("Matrix", 2012, Collections.singleton("Actor"));
        Director director = Director.builderDirector().withAge(12)
                .withId(12)
                .withName("Otavio")
                .withPhones(asList("234", "2342")).withMovie(movie).build();

        CommunicationEntity entity = converter.toCommunication(director);
        Director director1 = converter.toEntity(entity);

        assertEquals(movie, director1.getMovie());
        assertEquals(director.getName(), director1.getName());
        assertEquals(director.getAge(), director1.getAge());
        assertEquals(director.getId(), director1.getId());
    }

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

        assertEquals(movie, director1.getMovie());
        assertEquals(director.getName(), director1.getName());
        assertEquals(director.getAge(), director1.getAge());
        assertEquals(director.getId(), director1.getId());
    }

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

        assertEquals(movie, director1.getMovie());
        assertEquals(director.getName(), director1.getName());
        assertEquals(director.getAge(), director1.getAge());
        assertEquals(director.getId(), director1.getId());
    }

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
        assertEquals("Worker", entity.name());
        assertEquals("Bob", entity.find("name").get().get());
        assertEquals("Sao Paulo", entity.find("city").get().get());
        assertEquals("Java Developer", entity.find("description").get().get());
        assertEquals("BRL 10", entity.find("money").get().get());
    }

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
        assertEquals(worker.getSalary(), worker1.getSalary());
        assertEquals(job.getCity(), worker1.getJob().getCity());
        assertEquals(job.getDescription(), worker1.getJob().getDescription());
    }

    @Test
    void shouldConvertEmbeddableLazily() {
        CommunicationEntity entity = CommunicationEntity.of("Worker");
        entity.add("name", "Otavio");
        entity.add("money", "BRL 10");

        Worker worker = converter.toEntity(entity);
        assertEquals("Otavio", worker.getName());
        assertEquals(new Money("BRL", BigDecimal.TEN), worker.getSalary());
        Assertions.assertNull(worker.getJob());

    }


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
        assertEquals("ids", appointmentBook.getId());
        List<List<Element>> columns = (List<List<Element>>) contacts.get();

        assertEquals(3L, columns.stream().flatMap(Collection::stream)
                .filter(c -> c.name().equals("contact_name"))
                .count());
    }

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
        assertEquals("ids", appointmentBook.getId());
        assertEquals("Ada", contacts.stream().map(Contact::getName).distinct().findFirst().get());

    }


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
        assertEquals("Address", columnEntity.name());
        assertEquals(4, columns.size());
        List<Element> zip = columnEntity.find("zipCode").map(d -> d.get(new TypeReference<List<Element>>() {
        })).orElse(Collections.emptyList());

        assertEquals("Rua Engenheiro Jose Anasoh", getValue(columnEntity.find("street")));
        assertEquals("Salvador", getValue(columnEntity.find("city")));
        assertEquals("Bahia", getValue(columnEntity.find("state")));
        assertEquals("12321", getValue(zip.stream().filter(d -> d.name().equals("zip")).findFirst()));
        assertEquals("1234", getValue(zip.stream().filter(d -> d.name().equals("plusFour")).findFirst()));
    }

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

        assertEquals("Rua Engenheiro Jose Anasoh", address.getStreet());
        assertEquals("Salvador", address.getCity());
        assertEquals("Bahia", address.getState());
        assertEquals("12321", address.getZipCode().getZip());
        assertEquals("1234", address.getZipCode().getPlusFour());

    }

    @Test
    void shouldReturnNullWhenThereIsNotSubEntity() {
        CommunicationEntity entity = CommunicationEntity.of("Address");

        entity.add(Element.of("street", "Rua Engenheiro Jose Anasoh"));
        entity.add(Element.of("city", "Salvador"));
        entity.add(Element.of("state", "Bahia"));
        entity.add(Element.of("zip", "12321"));
        entity.add(Element.of("plusFour", "1234"));

        Address address = converter.toEntity(entity);

        assertEquals("Rua Engenheiro Jose Anasoh", address.getStreet());
        assertEquals("Salvador", address.getCity());
        assertEquals("Bahia", address.getState());
        assertNull(address.getZipCode());
    }

    @Test
    void shouldConvertAndDoNotUseUnmodifiableCollection() {
        CommunicationEntity entity = CommunicationEntity.of("vendors");
        entity.add("name", "name");
        entity.add("prefixes", Arrays.asList("value", "value2"));

        Vendor vendor = converter.toEntity(entity);
        vendor.add("value3");

        Assertions.assertEquals(3, vendor.getPrefixes().size());

    }

    @Test
    void shouldConvertEntityToDocumentWithArray() {
        byte[] contents = {1, 2, 3, 4, 5, 6};

        CommunicationEntity entity = CommunicationEntity.of("download");
        entity.add("_id", 1L);
        entity.add("contents", contents);

        Download download = converter.toEntity(entity);
        Assertions.assertEquals(1L, download.getId());
        Assertions.assertArrayEquals(contents, download.getContents());
    }

    @Test
    void shouldConvertDocumentToEntityWithArray() {
        byte[] contents = {1, 2, 3, 4, 5, 6};

        Download download = new Download();
        download.setId(1L);
        download.setContents(contents);

        CommunicationEntity entity = converter.toCommunication(download);

        Assertions.assertEquals(1L, entity.find("_id").get().get());
        final byte[] bytes = entity.find("contents").map(v -> v.get(byte[].class)).orElse(new byte[0]);
        Assertions.assertArrayEquals(contents, bytes);
    }

    @Test
    void shouldCreateUserScope() {
        CommunicationEntity entity = CommunicationEntity.of("UserScope");
        entity.add("_id", "userName");
        entity.add("scope", "scope");
        entity.add("properties", Collections.singletonList(Element.of("halo", "weld")));

        UserScope user = converter.toEntity(entity);
        Assertions.assertNotNull(user);
        Assertions.assertEquals("userName", user.getUserName());
        Assertions.assertEquals("scope", user.getScope());
        Assertions.assertEquals(Collections.singletonMap("halo", "weld"), user.getProperties());

    }

    @Test
    void shouldCreateUserScope2() {
        CommunicationEntity entity = CommunicationEntity.of("UserScope");
        entity.add("_id", "userName");
        entity.add("scope", "scope");
        entity.add("properties", Element.of("halo", "weld"));

        UserScope user = converter.toEntity(entity);
        Assertions.assertNotNull(user);
        Assertions.assertEquals("userName", user.getUserName());
        Assertions.assertEquals("scope", user.getScope());
        Assertions.assertEquals(Collections.singletonMap("halo", "weld"), user.getProperties());

    }

    @Test
    void shouldCreateLazilyEntity() {
        CommunicationEntity entity = CommunicationEntity.of("Citizen");
        entity.add("id", "10");
        entity.add("name", "Salvador");

        Citizen citizen = converter.toEntity(entity);
        Assertions.assertNotNull(citizen);
        Assertions.assertNull(citizen.getCity());
    }


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
            soft.assertThat(result.availableTransitions().get(0).targetWorkflowStepKey()).isEqualTo("TEST_WORKFLOW_STEP_KEY");
            soft.assertThat(result.availableTransitions().get(0).stepTransitionReason()).isEqualTo(REPEAT);
            soft.assertThat(result.availableTransitions().get(0).mailTemplateKey()).isNull();
            soft.assertThat(result.availableTransitions().get(0).restrictedRoleGroups()).hasSize(1);
            soft.assertThat(result.availableTransitions().get(0).restrictedRoleGroups().get(0)).isEqualTo("ADMIN");
        });

    }

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
        assertEquals("ids", appointmentBook.getId());
        List<List<Element>> columns = (List<List<Element>>) contacts.get();

        assertEquals(3L, columns.stream().flatMap(Collection::stream)
                .filter(c -> c.name().equals("contact_name"))
                .count());

        List<List<Element>> columns2 = (List<List<Element>>) network.get();

        assertEquals(3L, columns2.stream().flatMap(Collection::stream)
                .filter(c -> c.name().equals("contact_name"))
                .count());
    }

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
            Element element = elements.get(0);
            softly.assertThat(element.name()).isEqualTo("Renamer");
            var subDocument = element.get(new TypeReference<List<Element>>() {});
            softly.assertThat(subDocument).isNotNull().hasSize(2);
            softly.assertThat(subDocument.get(0).name()).isEqualTo("_id");
            softly.assertThat(subDocument.get(1).name()).isEqualTo("socialMedia");
        });
    }

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

}
