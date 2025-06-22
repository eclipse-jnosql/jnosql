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
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

import java.util.Optional;


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




}