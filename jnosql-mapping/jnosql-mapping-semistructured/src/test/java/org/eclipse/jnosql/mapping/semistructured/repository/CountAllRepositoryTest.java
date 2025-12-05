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
import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("The scenarios to test the feature count all")
@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
public class CountAllRepositoryTest {

    @Inject
    private SemistructuredRepositoryProducer producer;

    private ComicBookRepository comicBookRepository;

    private ComicBookBookStore bookStore;

    private SemiStructuredTemplate template;

    @BeforeEach
    void setUP() {
        this.template = Mockito.mock(SemiStructuredTemplate.class);
        this.comicBookRepository = producer.get(ComicBookRepository.class, template);
        this.bookStore = producer.get(ComicBookBookStore.class, template);
    }

    @Test
    @DisplayName("Should count all using built-in Repository")
    void shouldCountAll() {
        Mockito.when(template.count(ComicBook.class)).thenReturn(1L);
        long result = comicBookRepository.countAll();
        Assertions.assertThat(result).isEqualTo(1L);
        Mockito.verify(template).count(ComicBook.class);
    }

    @Test
    @DisplayName("Should count all using built-in Repository")
    void shouldCountCustomAll() {
        Mockito.when(template.count(ComicBook.class)).thenReturn(1L);
        long result = bookStore.countAll();
        Assertions.assertThat(result).isEqualTo(1L);
        Mockito.verify(template).count(ComicBook.class);
    }
}
