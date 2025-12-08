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
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.ComicBookBookStore;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.ComicBookRepository;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

@DisplayName("The scenarios to test the feature count by")
@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
public class RepositoryCountByTest extends AbstractRepositoryTest {

    @Inject
    private SemistructuredRepositoryProducer producer;

    @Override
    SemistructuredRepositoryProducer producer() {
        return producer;
    }

    @Test
    @DisplayName("Should count by using built-in Repository")
    void shouldCountBy() {
        Mockito.when(template.exists(Mockito.any(SelectQuery.class)))
                .thenReturn(true);
        boolean result = comicBookRepository.existsByName("The Lord of the Rings");
        Mockito.verify(template).exists(captor.capture());
        Assertions.assertThat(result).isTrue();

        SelectQuery selectQuery = captor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(selectQuery.name()).isEqualTo("ComicBook");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isEmpty();
            soft.assertThat(selectQuery.isCount()).isTrue();
            CriteriaCondition criteriaCondition = selectQuery.condition().orElseThrow();
            soft.assertThat(criteriaCondition.element().get()).isEqualTo("The Lord of the Rings");
        });
    }

    @Test
    @DisplayName("Should count by using built-in Repository")
    void shouldCountByCustom() {
        Mockito.when(template.exists(Mockito.any(SelectQuery.class)))
                .thenReturn(true);
        boolean result = bookStore.existsByName("The Lord of the Rings");
        Mockito.verify(template).exists(captor.capture());
        Assertions.assertThat(result).isTrue();

        SelectQuery selectQuery = captor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(selectQuery.name()).isEqualTo("ComicBook");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isEmpty();
            soft.assertThat(selectQuery.isCount()).isTrue();
            CriteriaCondition criteriaCondition = selectQuery.condition().orElseThrow();
            soft.assertThat(criteriaCondition.element().get()).isEqualTo("The Lord of the Rings");
        });
    }

    @Test
    @DisplayName("Should count by using built-in Repository Inheritance PhotoMedia")
    void shouldInheritancePhotoMediaCountBy() {
        Mockito.when(template.exists(Mockito.any(SelectQuery.class)))
                .thenReturn(true);
        boolean result = photoSocialMediaRepository.existsByName("The Lord of the Rings");
        Mockito.verify(template).exists(captor.capture());
        Assertions.assertThat(result).isTrue();

        SelectQuery selectQuery = captor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(selectQuery.name()).isEqualTo("SocialMedia");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isEmpty();
            soft.assertThat(selectQuery.isCount()).isTrue();
            var criteriaCondition = selectQuery.condition().orElseThrow();
            Element element = criteriaCondition.element();
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.AND);
            var conditions = element.get(new TypeReference<List<CriteriaCondition>>() {
            });
            soft.assertThat(conditions).hasSize(2);

        });
    }


}
