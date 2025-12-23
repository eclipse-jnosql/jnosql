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


import jakarta.data.page.CursoredPage;
import jakarta.data.page.PageRequest;
import jakarta.data.page.impl.CursoredPageRecord;
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
import org.eclipse.jnosql.mapping.semistructured.PreparedStatement;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.ComicBook;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@DisplayName("The scenarios to test the feature cursor pagination")
@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
public class RepositoryCursorPaginationTest extends AbstractRepositoryTest {

    @Inject
    private SemistructuredRepositoryProducer producer;



    @Override
    SemistructuredRepositoryProducer producer() {
        return producer;
    }


    @Test
    @DisplayName("Should throw IllegalArgumentException when method signature is invalid")
    void shouldGetInvalidCursor() {
        Assertions.assertThatThrownBy(() ->  comicBookRepository.invalidCursor("cursor"))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    @DisplayName("should find cursor using method by query")
    void shouldFindCursorUsingMethodByQuery() {
        var comicBook = new ComicBook("1", "Batman", 2020);

        CursoredPage<Object> cursor = new CursoredPageRecord<>(List.of(comicBook),
                Collections.singletonList(PageRequest.Cursor.forKey("id")),
                1L, PageRequest.ofSize(10),
                false, false
                );

        Mockito.when(template.selectCursor(Mockito.any(SelectQuery.class), Mockito.any(PageRequest.class)))
                .thenReturn(cursor);
        CursoredPage<ComicBook> comicBooks = comicBookRepository.findByName("Batman", PageRequest.ofSize(10));

        Mockito.verify(template).selectCursor(selectQueryCaptor.capture(), Mockito.eq(PageRequest.ofSize(10)));

        SelectQuery selectQuery = selectQueryCaptor.getValue();

        SoftAssertions.assertSoftly( softly -> {
            softly.assertThat(comicBooks).isNotNull();
            softly.assertThat(comicBooks.content()).hasSize(1);
            softly.assertThat(selectQuery.name()).isEqualTo(ComicBook.class.getSimpleName());
            softly.assertThat(selectQuery.condition()).isNotEmpty();
            CriteriaCondition condition = selectQuery.condition().orElseThrow();
            softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(condition.element().name()).isEqualTo("name");
            softly.assertThat(condition.element().get()).isEqualTo("Batman");

        });
    }

    @Test
    @DisplayName("should find cursor using Find annotation")
    void shouldCursorUsingFindAnnotation() {
        var comicBook = new ComicBook("1", "Batman", 2020);

        CursoredPage<Object> cursor = new CursoredPageRecord<>(List.of(comicBook),
                Collections.singletonList(PageRequest.Cursor.forKey("id")),
                1L, PageRequest.ofSize(10),
                false, false
        );

        Mockito.when(template.selectCursor(Mockito.any(SelectQuery.class), Mockito.any(PageRequest.class)))
                .thenReturn(cursor);
        CursoredPage<ComicBook> comicBooks = comicBookRepository.findByNameUsingFind("Batman", PageRequest.ofSize(10));

        Mockito.verify(template).selectCursor(selectQueryCaptor.capture(), Mockito.eq(PageRequest.ofSize(10)));

        SelectQuery selectQuery = selectQueryCaptor.getValue();

        SoftAssertions.assertSoftly( softly -> {
            softly.assertThat(comicBooks).isNotNull();
            softly.assertThat(comicBooks.content()).hasSize(1);
            softly.assertThat(selectQuery.name()).isEqualTo(ComicBook.class.getSimpleName());
            softly.assertThat(selectQuery.condition()).isNotEmpty();
            CriteriaCondition condition = selectQuery.condition().orElseThrow();
            softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(condition.element().name()).isEqualTo("name");
            softly.assertThat(condition.element().get()).isEqualTo("Batman");

        });
    }

    @Test
    @DisplayName("should find cursor using query annotation")
    void shouldFindUsingQuery() {
        var comicBook = new ComicBook("1", "Batman", 2020);

        CursoredPage<Object> cursor = new CursoredPageRecord<>(List.of(comicBook),
                Collections.singletonList(PageRequest.Cursor.forKey("id")),
                1L, PageRequest.ofSize(10),
                false, false
        );
        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(preparedStatement.selectQuery())
                .thenReturn(Optional.of(SelectQuery.select().from(ComicBook.class.getSimpleName()).where("name").eq("Batman").build()));

        Mockito.when(template.prepare(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(preparedStatement);

        Mockito.when(template.selectCursor(Mockito.any(SelectQuery.class), Mockito.any(PageRequest.class)))
                .thenReturn(cursor);
        CursoredPage<ComicBook> comicBooks = comicBookRepository.query("Batman", PageRequest.ofSize(10));

        Mockito.verify(template).selectCursor(selectQueryCaptor.capture(), Mockito.eq(PageRequest.ofSize(10)));

        SelectQuery selectQuery = selectQueryCaptor.getValue();

        SoftAssertions.assertSoftly( softly -> {
            softly.assertThat(comicBooks).isNotNull();
            softly.assertThat(comicBooks.content()).hasSize(1);
            softly.assertThat(selectQuery.name()).isEqualTo(ComicBook.class.getSimpleName());
            softly.assertThat(selectQuery.condition()).isNotEmpty();
            CriteriaCondition condition = selectQuery.condition().orElseThrow();
            softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(condition.element().name()).isEqualTo("name");
            softly.assertThat(condition.element().get()).isEqualTo("Batman");

        });
    }



}
