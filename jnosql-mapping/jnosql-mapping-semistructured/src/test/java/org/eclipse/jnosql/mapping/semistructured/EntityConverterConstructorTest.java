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
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.entities.Animal;
import org.eclipse.jnosql.mapping.semistructured.entities.Book;
import org.eclipse.jnosql.mapping.semistructured.entities.BookRelease;
import org.eclipse.jnosql.mapping.semistructured.entities.Money;
import org.eclipse.jnosql.mapping.semistructured.entities.constructor.Beer;
import org.eclipse.jnosql.mapping.semistructured.entities.constructor.BeerFactory;
import org.eclipse.jnosql.mapping.semistructured.entities.constructor.BookBag;
import org.eclipse.jnosql.mapping.semistructured.entities.constructor.BookUser;
import org.eclipse.jnosql.mapping.semistructured.entities.constructor.Computer;
import org.eclipse.jnosql.mapping.semistructured.entities.constructor.Guest;
import org.eclipse.jnosql.mapping.semistructured.entities.constructor.MobileAppRecord;
import org.eclipse.jnosql.mapping.semistructured.entities.constructor.PetOwner;
import org.eclipse.jnosql.mapping.semistructured.entities.constructor.ProgramRecord;
import org.eclipse.jnosql.mapping.semistructured.entities.constructor.Room;
import org.eclipse.jnosql.mapping.semistructured.entities.constructor.SocialMediaFollowers;
import org.eclipse.jnosql.mapping.semistructured.entities.constructor.SocialMediaFollowersRecord;
import org.eclipse.jnosql.mapping.semistructured.entities.constructor.SuperHero;
import org.eclipse.jnosql.mapping.semistructured.entities.constructor.Survey;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class EntityConverterConstructorTest {

    @Inject
    private EntityConverter converter;

    @Test
    void shouldConverterEntityComputer() {
        CommunicationEntity communication = CommunicationEntity.of("Computer");
        communication.add("_id", 10L);
        communication.add("name", "Dell");
        communication.add("age", 2020);
        communication.add("model", "Dell 2020");
        communication.add("price", "USD 20");
        Computer computer = this.converter.toEntity(communication);
        assertNotNull(computer);
        assertEquals(10L, computer.getId());
        assertEquals("Dell", computer.getName());
        assertEquals(2020, computer.getAge());
        assertEquals("Dell 2020", computer.getModel());
        assertEquals(Money.parse("USD 20"), computer.getPrice());
    }

    @Test
    void shouldConvertComputerToCommunication() {
        Computer computer = new Computer(10L, "Dell", 2020, "Dell 2020",
                Money.parse("USD 20"));
        CommunicationEntity communication = this.converter.toCommunication(computer);
        assertNotNull(communication);

        assertEquals(computer.getId(), communication.find("_id", Long.class).get());
        assertEquals(computer.getName(), communication.find("name", String.class).get());
        assertEquals(computer.getAge(), communication.find("age", int.class).get());
        assertEquals(computer.getModel(), communication.find("model", String.class).get());
        assertEquals(computer.getPrice().toString(), communication.find("price", String.class).get());
    }

    @Test
    void shouldConvertPetOwner() {
        CommunicationEntity communication = CommunicationEntity.of("PetOwner");
        communication.add("_id", 10L);
        communication.add("name", "Otavio");
        communication.add("animal", Arrays.asList(Element.of("_id", 23)
                , Element.of("name", "Ada")));

        PetOwner petOwner = this.converter.toEntity(communication);
        assertNotNull(petOwner);
        assertEquals(10L, petOwner.getId());
        assertEquals("Otavio", petOwner.getName());
        Animal animal = petOwner.getAnimal();
        assertEquals(23L, animal.getId());
        assertEquals("Ada", animal.getName());
    }

    @Test
    void shouldConvertPetOwnerCommunication() {
        Animal ada = new Animal("Ada");
        PetOwner petOwner = new PetOwner(10L, "Poliana", ada);
        CommunicationEntity communication = this.converter.toCommunication(petOwner);
        assertNotNull(communication);
        assertEquals(10L, communication.find("_id", Long.class).get());
        assertEquals("Poliana", communication.find("name", String.class).get());
        List<Element> columns = communication.find("animal", new TypeReference<List<Element>>() {
                })
                .get();
        assertThat(columns).contains(Element.of("name", "Ada"));
    }

    @Test
    void shouldConvertBookUser() {
        CommunicationEntity communication = CommunicationEntity.of("BookUser");
        communication.add("_id", "otaviojava");
        communication.add("native_name", "Otavio Santana");
        List<List<Element>> columns = new ArrayList<>();
        columns.add(Arrays.asList(Element.of("_id", 10), Element.of("name", "Effective Java")));
        columns.add(Arrays.asList(Element.of("_id", 12), Element.of("name", "Clean Code")));
        communication.add("books", columns);

        BookUser bookUser = this.converter.toEntity(communication);
        assertNotNull(bookUser);
        assertEquals("Otavio Santana", bookUser.getName());
        assertEquals("otaviojava", bookUser.getNickname());
        assertEquals(2, bookUser.getBooks().size());
        List<String> names = bookUser.getBooks().stream().map(Book::getName).toList();
        assertThat(names).contains("Effective Java", "Clean Code");

    }

    @Test
    void shouldConverterFieldsOnEntityComputer() {
        CommunicationEntity communication = CommunicationEntity.of("Computer");
        communication.add("_id", "10");
        communication.add("name", "Dell");
        communication.add("age", "2020");
        communication.add("model", "Dell 2020");
        communication.add("price", "USD 20");
        Computer computer = this.converter.toEntity(communication);
        assertNotNull(computer);
        assertEquals(10L, computer.getId());
        assertEquals("Dell", computer.getName());
        assertEquals(2020, computer.getAge());
        assertEquals("Dell 2020", computer.getModel());
        assertEquals(Money.parse("USD 20"), computer.getPrice());
    }

    @Test
    void shouldConverterEntityBookRelease() {
        CommunicationEntity communication = CommunicationEntity.of("BookRelease");
        communication.add("isbn", "9780132345286");
        communication.add("title", "Effective Java");
        communication.add("author", "Joshua Bloch");
        communication.add("year", Year.of(2001));
        BookRelease book = this.converter.toEntity(communication);
        assertNotNull(book);
        assertEquals("9780132345286", book.getIsbn());
        assertEquals("Effective Java", book.getTitle());
        assertEquals("Joshua Bloch", book.getAuthor());
        assertEquals(Year.of(2001), book.getYear());
    }

    @Test
    void shouldConverterEntityBookReleaseOnStringYear() {
        CommunicationEntity communication = CommunicationEntity.of("BookRelease");
        communication.add("isbn", "9780132345286");
        communication.add("title", "Effective Java");
        communication.add("author", "Joshua Bloch");
        communication.add("year", "2001");
        BookRelease book = this.converter.toEntity(communication);
        assertNotNull(book);
        assertEquals("9780132345286", book.getIsbn());
        assertEquals("Effective Java", book.getTitle());
        assertEquals("Joshua Bloch", book.getAuthor());
        assertEquals(Year.of(2001), book.getYear());
    }

    @Test
    void shouldConvertHero() {
        CommunicationEntity communication = CommunicationEntity.of("SuperHero");
        communication.add("_id", "10L");
        communication.add("name", "Otavio");
        communication.add("powers", List.of("speed", "strength"));

        SuperHero hero = this.converter.toEntity(communication);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(hero.id()).isEqualTo("10L");
            softly.assertThat(hero.name()).isEqualTo("Otavio");
            softly.assertThat(hero.powers()).contains("speed", "strength");
        });
    }

    @Test
    void shouldIgnoreWhenNullAtConstructor() {
        CommunicationEntity entity = CommunicationEntity.of("SocialMediaFollowers");
        entity.add("_id", "id");
        entity.add("followers", null);

        SocialMediaFollowers socialMediaContact = converter.toEntity(entity);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(socialMediaContact).isNotNull();
            soft.assertThat(socialMediaContact.getId()).isEqualTo("id");
        });
    }

    @Test
    void shouldIgnoreWhenNullAtRecord() {
        CommunicationEntity entity = CommunicationEntity.of("SocialMediaFollowersRecord");
        entity.add("_id", "id");
        entity.add("followers", null);

        SocialMediaFollowersRecord socialMediaContact = converter.toEntity(entity);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(socialMediaContact).isNotNull();
            soft.assertThat(socialMediaContact.id()).isEqualTo("id");
        });
    }

    @Test
    void shouldConvertGroupEmbeddable() {
        CommunicationEntity entity = CommunicationEntity.of("Beer");
        entity.add("_id", "id");
        entity.add("name", "Vin Blanc");
        entity.add("factory", List.of(Element.of("name", "Napa Valley Factory"),
                Element.of("location", "Napa Valley")));

        Beer beer = converter.toEntity(entity);

        SoftAssertions.assertSoftly(soft -> {
            var factory = beer.factory();
            soft.assertThat(beer).isNotNull();
            soft.assertThat(beer.id()).isEqualTo("id");
            soft.assertThat(beer.name()).isEqualTo("Vin Blanc");
            soft.assertThat(factory).isNotNull();
            soft.assertThat(factory.name()).isEqualTo("Napa Valley Factory");
            soft.assertThat(factory.location()).isEqualTo("Napa Valley");
        });
    }

    @Test
    void shouldIgnoreNullEmbeddable() {
        CommunicationEntity entity = CommunicationEntity.of("Beer");
        entity.add("_id", "id");
        entity.add("name", "Vin Blanc");
        entity.add("factory", null);

        Beer beer = converter.toEntity(entity);

        SoftAssertions.assertSoftly(soft -> {
            var factory = beer.factory();
            soft.assertThat(beer).isNotNull();
            soft.assertThat(beer.id()).isEqualTo("id");
            soft.assertThat(beer.name()).isEqualTo("Vin Blanc");
            soft.assertThat(factory).isNull();
        });
    }

    @Test
    void shouldConvertGroupEmbeddableToCommunication() {

        var wine = Beer.of("id", "Vin Blanc", BeerFactory.of("Napa Valley Factory", "Napa Valley"));


        var communication = converter.toCommunication(wine);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(communication).isNotNull();
            soft.assertThat(communication.name()).isEqualTo("Beer");
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
        CommunicationEntity communication = CommunicationEntity.of("Survey");
        communication.add("_id", "form");
        communication.add("questions", Arrays.asList(
                Element.of("question1", true),
                Element.of("question2", false),
                Element.of("question3", List.of(Element.of("advanced", true),
                        Element.of("visible", "true")))
        ));

        Survey survey = converter.toEntity(communication);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(survey.id()).isEqualTo("form");
            softly.assertThat(survey.questions()).containsEntry("question1", true);
            softly.assertThat(survey.questions()).containsEntry("question2", false);
            softly.assertThat(survey.questions()).containsEntry("question3", Map.of("advanced", true, "visible", "true"));
        });

    }

    @Test
    void shouldConvertArrayTypes() {

        var effectiveJava = Book.builder()
                .withId(10L)
                .withName("Effective Java")
                .withAge(Year.now().minusYears(2018).getValue())
                .build();

        var cleanCode = Book.builder()
                .withId(12L)
                .withName("Clean Code")
                .withAge(Year.now().minusYears(2008).getValue())
                .build();

        CommunicationEntity communication = CommunicationEntity.of(BookBag.class.getSimpleName());
        communication.add("_id", "Max");
        List<List<Element>> columns = new ArrayList<>();
        columns.add(Arrays.asList(
                Element.of("_id", effectiveJava.getId()),
                Element.of("name", effectiveJava.getName()),
                Element.of("age", effectiveJava.getAge())));
        columns.add(Arrays.asList(
                Element.of("_id", cleanCode.getId()),
                Element.of("name", cleanCode.getName()),
                Element.of("age", cleanCode.getAge())));

        communication.add("books", columns);

        BookBag bookBag = converter.toEntity(communication);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(bookBag.owner())
                    .as("invalid first level String field conversion")
                    .isEqualTo("Max");

            List<Book> names = Arrays.asList(bookBag.books());

            Book[] books = bookBag.books();

            softly.assertThat(books)
                    .as("invalid first level Array field conversion: check size")
                    .isNotNull()
                    .hasSize(2);

            softly.assertThat(books[0])
                    .as("invalid first level Array field conversion: check first element")
                    .usingRecursiveComparison()
                    .isEqualTo(effectiveJava);

            softly.assertThat(books[1])
                    .as("invalid first level Array field conversion: check second element")
                    .usingRecursiveComparison()
                    .isEqualTo(cleanCode);

        });

    }

    @Test
    void shouldConvertFromFlatCommunicationFromEntity() {

        CommunicationEntity communication = CommunicationEntity.of(Room.class.getSimpleName());
        communication.add("_id", 12);
        communication.add("documentNumber", "123");
        communication.add("name", "Ada");
        Room entity = converter.toEntity(communication);

        SoftAssertions.assertSoftly(softly->{
            softly.assertThat(entity).isNotNull();
            softly.assertThat(entity.guest()).isNotNull();
            softly.assertThat(entity.guest().documentNumber()).isEqualTo("123");
            softly.assertThat(entity.guest().name()).isEqualTo("Ada");
            softly.assertThat(entity.number()).isEqualTo(12);
        });
    }

    @Test
    void shouldConvertFromFlatCommunicationFromEntityToCommunication() {
        var room = new Room(12, new Guest("123", "Ada"));
        CommunicationEntity communication = converter.toCommunication(room);

        SoftAssertions.assertSoftly(softly->{
            softly.assertThat(communication).isNotNull();
            softly.assertThat(communication.find("_id").orElseThrow().get()).isEqualTo(12);
            softly.assertThat(communication.find("documentNumber").orElseThrow().get()).isEqualTo("123");
            softly.assertThat(communication.find("name").orElseThrow().get()).isEqualTo("Ada");
        });
    }


    @Test
    void shouldConvertFromMap() {
        var program = new ProgramRecord(
                "Renamer",
                Map.of("twitter", "x")
        );
        var computer = new MobileAppRecord("Computer",Map.of("Renamer", program));

        var entity = converter.toCommunication(computer);

        SoftAssertions.assertSoftly(softly->{
            softly.assertThat(entity).isNotNull();
            softly.assertThat(entity.name()).isEqualTo("MobileAppRecord");
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

    @Test
    void shouldConvertToMap() {

        var communication = CommunicationEntity.of("MobileAppRecord");
        communication.add("_id", "Computer");
        communication.add("programs", List.of(
                Element.of("Renamer", List.of(
                        Element.of("_id", "Renamer"),
                        Element.of("socialMedia", Map.of("twitter", "x"))
                ))
        ));

        MobileAppRecord entity = converter.toEntity(communication);

        SoftAssertions.assertSoftly(softly->{
            softly.assertThat(entity).isNotNull();
            softly.assertThat(entity.name()).isEqualTo("Computer");
            softly.assertThat(entity.programs()).isNotNull();
            softly.assertThat(entity.programs()).hasSize(1);
            ProgramRecord renamer = entity.programs().get("Renamer");
            softly.assertThat(renamer).isNotNull();
            softly.assertThat(renamer.name()).isEqualTo("Renamer");
            softly.assertThat(renamer.socialMedia()).isNotNull();
        });
    }

    @Test
    void shouldConvertFromMaps() {
        var program = new ProgramRecord(
                "Renamer",
                Map.of("twitter", "x")
        );
        var program2 = new ProgramRecord(
                "Java",
                Map.of("Instagram", "insta")
        );
        var computer = new MobileAppRecord("Computer",Map.of("Renamer", program, "Java", program2));

        var entity = converter.toCommunication(computer);

        SoftAssertions.assertSoftly(softly->{
            softly.assertThat(entity).isNotNull();
            softly.assertThat(entity.name()).isEqualTo("MobileAppRecord");
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

        var communication = CommunicationEntity.of("MobileAppRecord");
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

        MobileAppRecord entity = converter.toEntity(communication);

        SoftAssertions.assertSoftly(softly->{
            softly.assertThat(entity).isNotNull();
            softly.assertThat(entity.name()).isEqualTo("Computer");
            softly.assertThat(entity.programs()).isNotNull();
            softly.assertThat(entity.programs()).hasSize(2);
            var renamer = entity.programs().get("Renamer");
            softly.assertThat(renamer).isNotNull();
            softly.assertThat(renamer.name()).isEqualTo("Renamer");
            softly.assertThat(renamer.socialMedia()).isNotNull();

            var java = entity.programs().get("Java");
            softly.assertThat(java).isNotNull();
            softly.assertThat(java.name()).isEqualTo("Java");
            softly.assertThat(java.socialMedia()).isNotNull();
        });
    }

}
