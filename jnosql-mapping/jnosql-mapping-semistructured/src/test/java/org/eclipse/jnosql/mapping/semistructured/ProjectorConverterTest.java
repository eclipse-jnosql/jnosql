/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.entities.Book;
import org.eclipse.jnosql.mapping.semistructured.entities.BookView;
import org.eclipse.jnosql.mapping.semistructured.entities.Citizen;
import org.eclipse.jnosql.mapping.semistructured.entities.CitizenGeographySummary;
import org.eclipse.jnosql.mapping.semistructured.entities.City;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class ProjectorConverterTest {

    @Inject
    private ProjectorConverter converter;

    @Inject
    private EntitiesMetadata entitiesMetadata;

    @Test
    @DisplayName("Should have a default constructor for CDI ")
    void shouldHaveDefaultConstructor() {
        ProjectorConverter converter = new ProjectorConverter();
        assertNotNull(converter);
    }

    @Test
    void shouldConvertEntityToProjection() {
        var projection = entitiesMetadata.projection(BookView.class).orElseThrow();
        Book book = Book.builder().withId(1L).withName("Effective Java").withAge(20).build();

        BookView bookView = converter.map(book, projection);

        SoftAssertions.assertSoftly(softly -> {
           softly.assertThat(bookView).isNotNull();
              softly.assertThat(bookView.name()).isEqualTo("Effective Java");
              softly.assertThat(bookView.edition()).isEqualTo(20);
        });
    }

    @Test
    void shouldConvertWhenThereIsEmbeddedProjection() {
        var projection = entitiesMetadata.projection(CitizenGeographySummary.class).orElseThrow();
        City city = City.of("1", "London");
        Citizen citizen = Citizen.of("1", "Ada Lovelace", city);

        CitizenGeographySummary summary = converter.map(citizen, projection);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(summary).isNotNull();
            softly.assertThat(summary.name()).isEqualTo(city.getName());
            softly.assertThat(summary.city()).isEqualTo(city);
        });
    }

    @Test
    void shouldDontBreakWhenEmbeddedIsNull(){
        var projection = entitiesMetadata.projection(CitizenGeographySummary.class).orElseThrow();
        Citizen citizen = Citizen.of("1", "Ada Lovelace");

        CitizenGeographySummary summary = converter.map(citizen, projection);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(summary).isNotNull();
            softly.assertThat(summary.name()).isNull();
            softly.assertThat(summary.city()).isNull();
        });
    }

    @Test
    void shouldReturnErrorWhenListIsDifferent() {
        var projection = entitiesMetadata.projection(CitizenGeographySummary.class).orElseThrow();
        Citizen citizen = Citizen.of("1", "Ada Lovelace");
        assertThrows(IllegalArgumentException.class, () -> converter.map(citizen, projection, Collections.singletonList("name")));
    }

    @Test
    void shouldReturnErrorWhenEntityIsInvalid() {
        var projection = entitiesMetadata.projection(BookView.class).orElseThrow();
        assertThrows(IllegalArgumentException.class, () -> converter.map("citizen", projection, Collections.singletonList("name")));
    }

    @Test
    void shouldReturnErrorWhenEntityIsInvalid2() {
        var projection = entitiesMetadata.projection(BookView.class).orElseThrow();
        assertThrows(IllegalArgumentException.class, () -> converter.map("citizen", projection));
    }

    @Test
    void shouldConvertEntityToProjectionUsingList() {
        var projection = entitiesMetadata.projection(BookView.class).orElseThrow();
        Book book = Book.builder().withId(1L).withName("Effective Java").withAge(20).build();

        BookView bookView = converter.map(book, projection, List.of("name", "age"));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(bookView).isNotNull();
            softly.assertThat(bookView.name()).isEqualTo("Effective Java");
            softly.assertThat(bookView.edition()).isEqualTo(20);
        });
    }

}