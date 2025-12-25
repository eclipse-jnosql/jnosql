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


import jakarta.data.page.PageRequest;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.ComicBook;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.SocialMediaSummary;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.stream.Stream;

@DisplayName("The scenarios to test the feature query")
@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
public class RepositoryQueryTest extends AbstractRepositoryTest {

    @Inject
    private SemistructuredRepositoryProducer producer;

    @Override
    SemistructuredRepositoryProducer producer() {
        return producer;
    }

    @Test
    @DisplayName("should find using query without parameter")
    void shouldFindUsingQueryWithoutParameter() {
        ComicBook comicBook = new ComicBook("1", "The Lord of the Rings", 1954);
        org.eclipse.jnosql.mapping.semistructured.PreparedStatement preparedStatement =
                Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        Mockito.when(preparedStatement.result()).thenReturn(Stream.of(comicBook));

        Mockito.when(template.prepare(Mockito.anyString(), Mockito.anyString())).thenReturn(preparedStatement);

        var result = bookStore.query();
        Mockito.verify(template).prepare("FROM ComicBook WHERE year > 2000", "ComicBook");
        Assertions.assertThat(result).isNotNull().containsExactly(comicBook);
        Mockito.verify(preparedStatement, Mockito.never()).bind(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @DisplayName("should execute query with parameter")
    void shouldExecuteQueryWithParameter(){
        ComicBook comicBook = new ComicBook("1", "The Lord of the Rings", 1954);
        org.eclipse.jnosql.mapping.semistructured.PreparedStatement preparedStatement =
                Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        Mockito.when(preparedStatement.result()).thenReturn(Stream.of(comicBook));

        Mockito.when(template.prepare(Mockito.anyString(), Mockito.anyString())).thenReturn(preparedStatement);

        var result = bookStore.query(2025);
        Mockito.verify(template).prepare("FROM ComicBook WHERE year > ?1", "ComicBook");
        Assertions.assertThat(result).isNotNull().containsExactly(comicBook);
        Mockito.verify(preparedStatement).bind(Mockito.anyString(), Mockito.any(Integer.class));
    }

    @Test
    @DisplayName("should execute query with parameter and pagination")
    void shouldExecuteQueryWithParameterAndPagination(){
        ComicBook comicBook = new ComicBook("1", "The Lord of the Rings", 1954);
        var preparedStatement =
                Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);

        Mockito.when(preparedStatement.result()).thenReturn(Stream.of(comicBook));

        Mockito.when(template.prepare(Mockito.anyString(), Mockito.anyString())).thenReturn(preparedStatement);

        var result = bookStore.query(2025, PageRequest.ofSize(10));
        Mockito.verify(template).prepare("FROM ComicBook WHERE year > ?1", "ComicBook");
        Assertions.assertThat(result).isNotNull().containsExactly(comicBook);
        Mockito.verify(preparedStatement).bind(Mockito.anyString(), Mockito.any(Integer.class));
    }

    @Test
    @DisplayName("should execute query mapper")
    void shouldExecuteQueryMapper() {
        org.eclipse.jnosql.mapping.semistructured.PreparedStatement preparedStatement =
                Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        Mockito.when(preparedStatement.result()).thenReturn(Stream.of(new Object[]{new Object[]{"id", "name"}}));

        Mockito.when(template.prepare(Mockito.anyString(), Mockito.anyString())).thenReturn(preparedStatement);

        var result = photoSocialMediaRepository.query("name");
        Mockito.verify(template).prepare("SELECT id, name from PhotoSocialMedia where name = :name", "SocialMedia");
        Assertions.assertThat(result).isNotNull().containsExactly(new SocialMediaSummary("id", "name"));
    }

}
