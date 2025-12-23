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


import jakarta.data.metamodel.BasicAttribute;
import jakarta.data.restrict.Restriction;
import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
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

import java.util.UUID;

@DisplayName("The scenarios to test the feature delete")
@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
public class RepositoryDeleteTest extends AbstractRepositoryTest {
    @Inject
    private SemistructuredRepositoryProducer producer;

    @Override
    SemistructuredRepositoryProducer producer() {
        return producer;
    }

    @Test
    @DisplayName("Should delete by using built-in Repository")
    void shouldDeleteBy() {

        ComicBook comicBook = new ComicBook(UUID.randomUUID().toString(), "The Lord of the Rings", 2001);

        comicBookRepository.delete(comicBook);
        Mockito.verify(template).delete(comicBook);
    }

    @Test
    @DisplayName("Should delete by restriction using built-in Repository")
    void shouldDeleteByRestriction() {

        var attribute = BasicAttribute.of(ComicBook.class, "name", String.class);
        Restriction<ComicBook> comicBookRestriction = attribute.equalTo("The Lord of the Rings");
        comicBookRepository.delete(comicBookRestriction);

        Mockito.verify(template).delete(deleteQueryCaptor.capture());
        var deleteQuery = deleteQueryCaptor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(deleteQuery.name()).isEqualTo("ComicBook");
            soft.assertThat(deleteQuery.condition()).isNotEmpty();
            CriteriaCondition criteriaCondition = deleteQuery.condition().orElseThrow();
            soft.assertThat(criteriaCondition.element().get()).isEqualTo("The Lord of the Rings");
        });
    }

}
