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

import jakarta.data.Limit;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.constraint.EqualTo;
import jakarta.data.expression.Expression;
import jakarta.data.metamodel.BasicAttribute;
import jakarta.data.page.PageRequest;
import jakarta.data.restrict.Restriction;
import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.NameKey;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.query.RepositorySemiStructuredObserverParser;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.ComicBook;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.ComicBookCustomRepository;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;


@DisplayName("The scenarios to test the dynamic query builder")
@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class DynamicSelectQueryBuilderTest {

    @Inject
    private EntitiesMetadata entitiesMetadata;

    @Inject
    private Converters converters;

    @Inject
    private RepositoriesMetadata repositoriesMetadata;

    private CommunicationObserverParser parser;

    private SemiStructuredTemplate template;

    private EntityMetadata entityMetadata;

    private RepositoryMetadata repositoryMetadata;

    @BeforeEach
    void setUp() {
        this.template = Mockito.mock(SemiStructuredTemplate.class);
        this.entityMetadata = entitiesMetadata.findByClassName(ComicBook.class.getName()).orElseThrow();
        this.parser = RepositorySemiStructuredObserverParser.of(entityMetadata);
        this.repositoryMetadata = repositoriesMetadata.get(ComicBookCustomRepository.class).orElseThrow();
    }

 // Test cases would go here
 // should include dynamic select columns
 // should include dynamic sorts
 // should include first parameter
 //should include as limit parameter
 //should include pageRequest parameter
 //should include inheritance type condition
 //should include restriction

    @Test
    @DisplayName("Should test without any dynamic changes")
    void shouldTestWithoutAnyDynamicChanges() {
        var query = SelectQuery.select().from(ComicBook.class.getSimpleName()).build();
        var method = repositoryMetadata.find(new NameKey("findByName")).orElseThrow();
        var parameters = new Object[]{};
        var context = new RepositoryInvocationContext(method, repositoryMetadata, entityMetadata, template, parameters);

        var updatedQuery = DynamicSelectQueryBuilder.INSTANCE.updateDynamicQuery(query, context, parser, converters);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updatedQuery).isNotNull();
            softly.assertThat(updatedQuery.name()).isEqualTo(ComicBook.class.getSimpleName());
            softly.assertThat(updatedQuery.columns()).isEmpty();
            softly.assertThat(updatedQuery.sorts()).isEmpty();
            softly.assertThat(updatedQuery.limit()).isEqualTo(0);
            softly.assertThat(updatedQuery.skip()).isEqualTo(0);
        });
    }

    @Test
    @DisplayName("Should include limit parameter")
    void shouldIncludeLimitParameter() {
        var query = SelectQuery.select().from(ComicBook.class.getSimpleName()).build();
        var method = repositoryMetadata.find(new NameKey("findByName")).orElseThrow();
        var parameters = new Object[]{Limit.range(1, 15)};
        var context = new RepositoryInvocationContext(method, repositoryMetadata, entityMetadata, template, parameters);

        var updatedQuery = DynamicSelectQueryBuilder.INSTANCE.updateDynamicQuery(query, context, parser, converters);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updatedQuery).isNotNull();
            softly.assertThat(updatedQuery.name()).isEqualTo(ComicBook.class.getSimpleName());
            softly.assertThat(updatedQuery.columns()).isEmpty();
            softly.assertThat(updatedQuery.sorts()).isEmpty();
            softly.assertThat(updatedQuery.limit()).isEqualTo(15);
            softly.assertThat(updatedQuery.skip()).isEqualTo(0);
        });
    }

    @Test
    @DisplayName("Should include pageRequest parameter")
    void shouldIncludePageRequestParameter() {
        var query = SelectQuery.select().from(ComicBook.class.getSimpleName()).build();
        var method = repositoryMetadata.find(new NameKey("findByName")).orElseThrow();
        var parameters = new Object[]{PageRequest.ofSize(10).page(2)};
        var context = new RepositoryInvocationContext(method, repositoryMetadata, entityMetadata, template, parameters);

        var updatedQuery = DynamicSelectQueryBuilder.INSTANCE.updateDynamicQuery(query, context, parser, converters);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updatedQuery).isNotNull();
            softly.assertThat(updatedQuery.name()).isEqualTo(ComicBook.class.getSimpleName());
            softly.assertThat(updatedQuery.columns()).isEmpty();
            softly.assertThat(updatedQuery.sorts()).isEmpty();
            softly.assertThat(updatedQuery.limit()).isEqualTo(10);
            softly.assertThat(updatedQuery.skip()).isEqualTo(10);
        });
    }

    @Test
    @DisplayName("Should include order parameter")
    void shouldIncludeOrderParameter() {
        var query = SelectQuery.select().from(ComicBook.class.getSimpleName()).build();
        var method = repositoryMetadata.find(new NameKey("findByName")).orElseThrow();
        var parameters = new Object[]{Order.by(Sort.asc("name"), Sort.desc("year"))};
        var context = new RepositoryInvocationContext(method, repositoryMetadata, entityMetadata, template, parameters);

        var updatedQuery = DynamicSelectQueryBuilder.INSTANCE.updateDynamicQuery(query, context, parser, converters);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updatedQuery).isNotNull();
            softly.assertThat(updatedQuery.name()).isEqualTo(ComicBook.class.getSimpleName());
            softly.assertThat(updatedQuery.columns()).isEmpty();
            softly.assertThat(updatedQuery.sorts()).hasSize(2);
            softly.assertThat(updatedQuery.sorts()).contains(Sort.asc("name"), Sort.desc("year"));
            softly.assertThat(updatedQuery.limit()).isEqualTo(0);
            softly.assertThat(updatedQuery.skip()).isEqualTo(0);
        });
    }

    @Test
    @DisplayName("Should include sort parameter")
    void shouldIncludeSortParameter() {
        var query = SelectQuery.select().from(ComicBook.class.getSimpleName()).build();
        var method = repositoryMetadata.find(new NameKey("findByName")).orElseThrow();
        var parameters = new Object[]{Sort.asc("name")};
        var context = new RepositoryInvocationContext(method, repositoryMetadata, entityMetadata, template, parameters);

        var updatedQuery = DynamicSelectQueryBuilder.INSTANCE.updateDynamicQuery(query, context, parser, converters);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updatedQuery).isNotNull();
            softly.assertThat(updatedQuery.name()).isEqualTo(ComicBook.class.getSimpleName());
            softly.assertThat(updatedQuery.columns()).isEmpty();
            softly.assertThat(updatedQuery.sorts()).hasSize(1);
            softly.assertThat(updatedQuery.sorts()).contains(Sort.asc("name"));
            softly.assertThat(updatedQuery.limit()).isEqualTo(0);
            softly.assertThat(updatedQuery.skip()).isEqualTo(0);
        });
    }

    @Test
    @DisplayName("Should include first annotation")
    void shouldIncludeFirstAnnotation() {
        var query = SelectQuery.select().from(ComicBook.class.getSimpleName()).build();
        var method = repositoryMetadata.find(new NameKey("findByName2")).orElseThrow();
        var parameters = new Object[]{};
        var context = new RepositoryInvocationContext(method, repositoryMetadata, entityMetadata, template, parameters);

        var updatedQuery = DynamicSelectQueryBuilder.INSTANCE.updateDynamicQuery(query, context, parser, converters);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updatedQuery).isNotNull();
            softly.assertThat(updatedQuery.name()).isEqualTo(ComicBook.class.getSimpleName());
            softly.assertThat(updatedQuery.columns()).isEmpty();
            softly.assertThat(updatedQuery.sorts()).isEmpty();
            softly.assertThat(updatedQuery.limit()).isEqualTo(20);
            softly.assertThat(updatedQuery.skip()).isEqualTo(0);
        });
    }

    @Test
    @DisplayName("Should include restriction parameter")
    void shouldIncludeRestrictionParameter() {
        var query = SelectQuery.select().from(ComicBook.class.getSimpleName()).build();
        var method = repositoryMetadata.find(new NameKey("findByName")).orElseThrow();
        BasicAttribute<ComicBook, Integer> age = BasicAttribute.of(ComicBook.class, "age", Integer.class);
        Restriction<ComicBook> comicBookRestriction = age.equalTo(10);

        var parameters = new Object[]{comicBookRestriction};
        var context = new RepositoryInvocationContext(method, repositoryMetadata, entityMetadata, template, parameters);
        var updatedQuery = DynamicSelectQueryBuilder.INSTANCE.updateDynamicQuery(query, context, parser, converters);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updatedQuery).isNotNull();
            softly.assertThat(updatedQuery.name()).isEqualTo(ComicBook.class.getSimpleName());
            softly.assertThat(updatedQuery.columns()).isEmpty();
            softly.assertThat(updatedQuery.sorts()).isEmpty();
            softly.assertThat(updatedQuery.limit()).isEqualTo(0);
            softly.assertThat(updatedQuery.skip()).isEqualTo(0);
            softly.assertThat(updatedQuery.condition()).isNotEmpty();
            var condition = updatedQuery.condition().orElseThrow();
            softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(condition.element()).isEqualTo(Element.of("age", 10));
        });
    }

    @Test
    @DisplayName("Should append new condition at restriction parameter")
    void shouldAppendNewConditionATRestrictionParameter() {
        var query =
                SelectQuery.select().from(ComicBook.class.getSimpleName()).where("name")
                        .eq("Sample Magazine").build();
        var method = repositoryMetadata.find(new NameKey("findByName")).orElseThrow();

        BasicAttribute<ComicBook, Integer> age = BasicAttribute.of(ComicBook.class, "age", Integer.class);
        Restriction<ComicBook> comicBookRestriction = age.equalTo(10);

        var parameters = new Object[]{comicBookRestriction};
        var context = new RepositoryInvocationContext(method, repositoryMetadata, entityMetadata, template, parameters);
        var updatedQuery = DynamicSelectQueryBuilder.INSTANCE.updateDynamicQuery(query, context, parser, converters);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updatedQuery).isNotNull();
            softly.assertThat(updatedQuery.name()).isEqualTo(ComicBook.class.getSimpleName());
            softly.assertThat(updatedQuery.columns()).isEmpty();
            softly.assertThat(updatedQuery.sorts()).isEmpty();
            softly.assertThat(updatedQuery.limit()).isEqualTo(0);
            softly.assertThat(updatedQuery.skip()).isEqualTo(0);
            softly.assertThat(updatedQuery.condition()).isNotEmpty();
            var condition = updatedQuery.condition().orElseThrow();
            softly.assertThat(condition.condition()).isEqualTo(Condition.AND);
            var conditions = condition.element().get(new TypeReference<List<CriteriaCondition>>() {
            });
            softly.assertThat(conditions.getFirst()).isEqualTo(CriteriaCondition.eq(Element.of("name", "Sample " +
                    "Magazine")));
            softly.assertThat(conditions.get(1)).isEqualTo(CriteriaCondition.eq(Element.of("age", 10)));
        });
    }



}