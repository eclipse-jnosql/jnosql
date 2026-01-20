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
package org.eclipse.jnosql.mapping.semistructured.repository;


import jakarta.data.constraint.EqualTo;
import jakarta.data.page.PageRequest;
import jakarta.data.restrict.Restrict;
import jakarta.data.restrict.Restriction;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.ComicBook;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.PhotoSocialMedia;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.SocialMediaSummary;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.Stream;

@DisplayName("The scenarios to test the feature parameter based")
@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
public class RepositoryParameterBasedTest extends AbstractRepositoryTest {

    @Inject
    private SemistructuredRepositoryProducer producer;

    @Override
    SemistructuredRepositoryProducer producer() {
        return producer;
    }

    @Test
    @DisplayName("Should findByName using built-in Repository")
    void shouldFindByName() {
        ComicBook comicBook = new ComicBook("1", "The Lord of the Rings", 1954);
        Mockito.when(template.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.of(comicBook));

        var result = bookStore.find("The Lord of the Rings");
        Mockito.verify(template).select(selectQueryCaptor.capture());
        Assertions.assertThat(result).isNotNull().containsExactly(comicBook);

        SelectQuery selectQuery = selectQueryCaptor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.name()).isEqualTo("ComicBook");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isEmpty();
            CriteriaCondition criteriaCondition = selectQuery.condition().orElseThrow();
            soft.assertThat(criteriaCondition.element().get()).isEqualTo("The Lord of the Rings");
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(criteriaCondition.element().name()).isEqualTo("name");
        });
    }

    @Test
    @DisplayName("Should id function using built-in Repository")
    void shouldFindSpecialId() {
        ComicBook comicBook = new ComicBook("1", "The Lord of the Rings", 1954);
        Mockito.when(template.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.of(comicBook));

        var result = bookStore.findIdSpecial("The Lord of the Rings");
        Mockito.verify(template).select(selectQueryCaptor.capture());
        Assertions.assertThat(result).isNotNull().containsExactly(comicBook);

        SelectQuery selectQuery = selectQueryCaptor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.name()).isEqualTo("ComicBook");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isEmpty();
            CriteriaCondition criteriaCondition = selectQuery.condition().orElseThrow();
            soft.assertThat(criteriaCondition.element().get()).isEqualTo("The Lord of the Rings");
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(criteriaCondition.element().name()).isEqualTo("_id");
        });
    }



    @Test
    @DisplayName("Should findByAge using built-in Repository")
    void shouldFindByAge() {
        ComicBook comicBook = new ComicBook("1", "The Lord of the Rings", 1954);
        Mockito.when(template.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.of(comicBook));

        var result = bookStore.findAge(35);
        Mockito.verify(template).select(selectQueryCaptor.capture());
        Assertions.assertThat(result).isNotNull().containsExactly(comicBook);

        SelectQuery selectQuery = selectQueryCaptor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.name()).isEqualTo("ComicBook");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isEmpty();
            CriteriaCondition criteriaCondition = selectQuery.condition().orElseThrow();
            soft.assertThat(criteriaCondition.element().get()).isEqualTo(35);
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.GREATER_THAN);
            soft.assertThat(criteriaCondition.element().name()).isEqualTo("age");
        });
    }

    @Test
    @DisplayName("Should findByName using Constraint Repository")
    void shouldFindByNameConstraint() {
        ComicBook comicBook = new ComicBook("1", "The Lord of the Rings", 1954);
        Mockito.when(template.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.of(comicBook));

        var result = bookStore.find(EqualTo.value("The Lord of the Rings"));
        Mockito.verify(template).select(selectQueryCaptor.capture());
        Assertions.assertThat(result).isNotNull().containsExactly(comicBook);

        SelectQuery selectQuery = selectQueryCaptor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.name()).isEqualTo("ComicBook");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isEmpty();
            CriteriaCondition criteriaCondition = selectQuery.condition().orElseThrow();
            soft.assertThat(criteriaCondition.element().get()).isEqualTo("The Lord of the Rings");
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(criteriaCondition.element().name()).isEqualTo("name");
        });
    }



    @Test
    @DisplayName("Should findByName with Pagination")
    void shouldFindByNameWithPagination() {
        ComicBook comicBook = new ComicBook("1", "The Lord of the Rings", 1954);
        Mockito.when(template.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.of(comicBook));

        var result = bookStore.find("The Lord of the Rings", PageRequest.ofSize(10));
        Mockito.verify(template).select(selectQueryCaptor.capture());
        Assertions.assertThat(result).isNotNull().containsExactly(comicBook);

        SelectQuery selectQuery = selectQueryCaptor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.name()).isEqualTo("ComicBook");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isEmpty();
            CriteriaCondition criteriaCondition = selectQuery.condition().orElseThrow();
            soft.assertThat(criteriaCondition.element().get()).isEqualTo("The Lord of the Rings");
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(criteriaCondition.element().name()).isEqualTo("name");
        });
    }

    @Test
    @DisplayName("Should overwrite by using Find annotation")
    void shouldOverwriteByUsingFindAnnotation() {
        Person person = Person.builder().build();
        Mockito.when(template.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.of(person));

        var result = bookStore.findPerson("Ada");
        Mockito.verify(template).select(selectQueryCaptor.capture());
        SelectQuery selectQuery = selectQueryCaptor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(result).isNotNull().containsExactly(person);
            soft.assertThat(selectQuery.name()).isEqualTo("Person");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isEmpty();
            CriteriaCondition criteriaCondition = selectQuery.condition().orElseThrow();
            soft.assertThat(criteriaCondition.element().get()).isEqualTo("Ada");
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(criteriaCondition.element().name()).isEqualTo("name");
        });
    }

    @Test
    @DisplayName("Should mapper by Select annotation")
    void shouldMapperBySelectAnnotation() {
        var photoSocialMedia = PhotoSocialMedia.of("1", "The Lord of the Rings", "http://image.com/1");

        Mockito.when(template.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.of(photoSocialMedia));

        var result = photoSocialMediaRepository.find("The Lord of the Rings");
        Mockito.verify(template).select(selectQueryCaptor.capture());

        Assertions.assertThat(result).isNotNull().containsExactly(new SocialMediaSummary("1", "The Lord of the Rings"));

        SelectQuery selectQuery = selectQueryCaptor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.name()).isEqualTo("SocialMedia");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isEmpty();
        });
    }

    @Test
    @DisplayName("Should return all elements from unrestricted filter restriction")
    void shouldReturnAllElementsFromUnrestrictedFilterRestriction() {
        var comicBook = new ComicBook("1", "The Lord of the Rings", 1954);

        Mockito.when(template.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.of(comicBook));

        List<ComicBook> comicBooks = bookStore.filter(Restrict.unrestricted());
        Mockito.verify(template).select(selectQueryCaptor.capture());
        Assertions.assertThat(comicBooks).isNotNull().hasSize(1);
        SelectQuery selectQuery = selectQueryCaptor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.name()).isEqualTo("ComicBook");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isEmpty();
        });
    }

    @Test
    @DisplayName("Should return all elements from restricted filter restriction")
    void shouldCaptureAlwaysEmptyWhenTheQueryIsAlwaysFalse() {
        var comicBook = new ComicBook("1", "The Lord of the Rings", 1954);

        Mockito.when(template.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.of(comicBook));

        List<ComicBook> comicBooks = bookStore.filter(Restrict.not(Restrict.unrestricted()));
        Mockito.verify(template, Mockito.never()).select(selectQueryCaptor.capture());
        Assertions.assertThat(comicBooks).isNotNull().isEmpty();



    }
}
