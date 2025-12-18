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
import jakarta.data.page.PageRequest;
import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser;
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

}