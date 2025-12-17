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


import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.ComicBook;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.Stream;

@DisplayName("The scenarios to test the feature find all")
@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
public class RepositoryFindAllTest extends AbstractRepositoryTest {

    @Inject
    private SemistructuredRepositoryProducer producer;


    @Override
    SemistructuredRepositoryProducer producer() {
        return producer;
    }


    @Test
    @DisplayName("Should find all using built-in Repository")
    void shouldFindAll() {
        var comicBook = new ComicBook("1", "Batman", 2020);
        Mockito.when(template.select(Mockito.any(SelectQuery.class))).thenReturn(Stream.of(comicBook));
        List<ComicBook> comicBooks = bookStore.findAll();

        Mockito.verify(template).select(selectQueryCaptor.capture());
        SelectQuery selectQuery = selectQueryCaptor.getValue();

        SoftAssertions.assertSoftly( softly -> {
            softly.assertThat(comicBooks).isNotNull();
            softly.assertThat(comicBooks).hasSize(1);
            softly.assertThat(comicBooks.getFirst()).isEqualTo(comicBook);

            softly.assertThat(selectQuery.name()).isEqualTo(ComicBook.class.getSimpleName());
            softly.assertThat(selectQuery.condition()).isNotNull().isEmpty();
        });
    }




}
