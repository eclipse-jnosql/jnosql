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
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.ComicBook;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.SocialMedia;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@DisplayName("The scenarios to test the feature find by")
@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
public class RepositoryFindByTest extends AbstractRepositoryTest {

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

        var result = bookStore.findByName("The Lord of the Rings");
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
    @DisplayName("Should order by sort annotations")
    void shouldOrderBySortAnnotations() {
        ComicBook comicBook = new ComicBook("1", "The Lord of the Rings", 1954);
        Mockito.when(template.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.of(comicBook));

        var result = bookStore.findByNameAndYear("The Lord of the Rings", 2012);
        Mockito.verify(template).select(selectQueryCaptor.capture());
        Assertions.assertThat(result).isNotNull().containsExactly("The Lord of the Rings");

        SelectQuery selectQuery = selectQueryCaptor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.name()).isEqualTo("ComicBook");
            soft.assertThat(selectQuery.columns()).containsExactly("name");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            CriteriaCondition criteriaCondition = selectQuery.condition().orElseThrow();
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.AND);
            var conditions = criteriaCondition.element().get(new TypeReference<List<CriteriaCondition>>() {
            });
            soft.assertThat(conditions).hasSize(2);
            soft.assertThat(selectQuery.sorts()).hasSize(2);
            soft.assertThat(selectQuery.sorts().get(0).property()).isEqualTo("name");
            soft.assertThat(selectQuery.sorts().get(0).isAscending()).isTrue();
            soft.assertThat(selectQuery.sorts().get(1).property()).isEqualTo("year");
            soft.assertThat(selectQuery.sorts().get(1).isAscending()).isFalse();
        });
    }

    @Test
    @DisplayName("Should find single result by id")
    void shouldFindSingleResult() {
        ComicBook comicBook = new ComicBook("1", "The Lord of the Rings", 1954);
        Mockito.when(template.singleResult(Mockito.any(SelectQuery.class)))
                .thenReturn(Optional.of(comicBook));

        var result = bookStore.findById("The Lord of the Rings");
        Mockito.verify(template).singleResult(selectQueryCaptor.capture());
        Assertions.assertThat(result).isNotEmpty();

        SelectQuery selectQuery = selectQueryCaptor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.name()).isEqualTo("ComicBook");
            soft.assertThat(selectQuery.columns()).isEmpty();
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            CriteriaCondition criteriaCondition = selectQuery.condition().orElseThrow();
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
        });
    }

    @Test
    @DisplayName("Should find using pagination")
    void shouldFindUsingPagination() {
        ComicBook comicBook = new ComicBook("1", "The Lord of the Rings", 1954);
        Mockito.when(template.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.of(comicBook));

        var result = bookStore.findByName("The Lord of the Rings", PageRequest.ofSize(10));
        Mockito.verify(template).select(selectQueryCaptor.capture());
        Assertions.assertThat(result).isNotNull();

        SelectQuery selectQuery = selectQueryCaptor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.name()).isEqualTo("ComicBook");
            soft.assertThat(selectQuery.columns()).isEmpty();
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            CriteriaCondition criteriaCondition = selectQuery.condition().orElseThrow();
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
        });
    }


}
