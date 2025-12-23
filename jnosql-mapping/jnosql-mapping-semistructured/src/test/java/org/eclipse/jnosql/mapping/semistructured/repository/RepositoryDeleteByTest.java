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
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.SocialMedia;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

@DisplayName("The scenarios to test the feature delete by")
@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
public class RepositoryDeleteByTest extends AbstractRepositoryTest {
    @Inject
    private SemistructuredRepositoryProducer producer;

    @Override
    SemistructuredRepositoryProducer producer() {
        return producer;
    }

    @Test
    @DisplayName("Should delete by using built-in Repository")
    void shouldDeleteBy() {

        comicBookRepository.deleteByName("The Lord of the Rings");
        Mockito.verify(template).delete(deleteQueryCaptor.capture());

        var deleteQuery = deleteQueryCaptor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(deleteQuery.name()).isEqualTo("ComicBook");
            soft.assertThat(deleteQuery.condition()).isNotEmpty();
            CriteriaCondition criteriaCondition = deleteQuery.condition().orElseThrow();
            soft.assertThat(criteriaCondition.element().get()).isEqualTo("The Lord of the Rings");
        });
    }

    @Test
    @DisplayName("should get error when return is invalid")
    void shouldGetErrorWhenReturnIsInvalid() {
        Assertions.assertThatThrownBy(() ->
        comicBookRepository.deleteByYear(10))
                .isInstanceOf(UnsupportedOperationException.class);

    }

    @Test
    @DisplayName("Should delete by using built-in Repository")
    void shouldDeleteByCustom() {

        bookStore.deleteByName("The Lord of the Rings");
        Mockito.verify(template).delete(deleteQueryCaptor.capture());

        var deleteQuery = deleteQueryCaptor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(deleteQuery.name()).isEqualTo("ComicBook");
            soft.assertThat(deleteQuery.condition()).isNotEmpty();
            var criteriaCondition = deleteQuery.condition().orElseThrow();
            soft.assertThat(criteriaCondition.element().get()).isEqualTo("The Lord of the Rings");
        });
    }

    @Test
    @DisplayName("Should delete by using built-in Repository Inheritance PhotoMedia")
    void shouldInheritancePhotoMediaDeleteBy() {

        photoSocialMediaRepository.deleteByName("The Lord of the Rings");
        Mockito.verify(template).delete(deleteQueryCaptor.capture());

        var deleteQuery = deleteQueryCaptor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(deleteQuery.name()).isEqualTo(SocialMedia.class.getSimpleName());
            soft.assertThat(deleteQuery.condition()).isNotEmpty();
            var criteriaCondition = deleteQuery.condition().orElseThrow();
            Element element = criteriaCondition.element();
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.AND);
            var conditions = element.get(new TypeReference<List<CriteriaCondition>>() {
            });
            soft.assertThat(conditions).hasSize(2);
            var fromSystem = conditions.get(0);
            var fromUser = conditions.get(1);
            soft.assertThat(fromSystem).isEqualTo(CriteriaCondition.eq(Element.of("dtype", "photo")));
            soft.assertThat(fromUser.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(fromUser.element().get()).isEqualTo("The Lord of the Rings");
        });
    }

    @Test
    @DisplayName("Should delete by using built-in Repository Inheritance VideoSocialMedia")
    void shouldInheritanceVideoMediaDeleteBy() {

        videoSocialMediaRepository.deleteByName("The Lord of the Rings");
        Mockito.verify(template).delete(deleteQueryCaptor.capture());

        var deleteQuery = deleteQueryCaptor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(deleteQuery.name()).isEqualTo(SocialMedia.class.getSimpleName());
            soft.assertThat(deleteQuery.condition()).isNotEmpty();
            var criteriaCondition = deleteQuery.condition().orElseThrow();
            Element element = criteriaCondition.element();
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.AND);
            var conditions = element.get(new TypeReference<List<CriteriaCondition>>() {
            });
            soft.assertThat(conditions).hasSize(2);
            var fromSystem = conditions.get(0);
            var fromUser = conditions.get(1);
            soft.assertThat(fromSystem).isEqualTo(CriteriaCondition.eq(Element.of("dtype", "video")));
            soft.assertThat(fromUser.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(fromUser.element().get()).isEqualTo("The Lord of the Rings");
        });
    }

    @Test
    @DisplayName("Should delete by using built-in Repository Inheritance VideoSocialMedia without no parameter")
    void shouldInheritanceVideoMediaDeleteByNoParameter() {

        videoSocialMediaRepository.deleteBy();
        Mockito.verify(template).delete(deleteQueryCaptor.capture());

        var deleteQuery = deleteQueryCaptor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(deleteQuery.name()).isEqualTo(SocialMedia.class.getSimpleName());
            soft.assertThat(deleteQuery.condition()).isNotEmpty();
            var criteriaCondition = deleteQuery.condition().orElseThrow();
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(criteriaCondition.element()).isEqualTo(Element.of("dtype", "video"));
        });
    }

    @Test
    @DisplayName("Should delete by using built-in Repository Inheritance PhotoSocialMedia without no parameter")
    void shouldInheritancePhotoMediaCountByNoParameter() {

        photoSocialMediaRepository.deleteBy();
        Mockito.verify(template).delete(deleteQueryCaptor.capture());

        var deleteQuery = deleteQueryCaptor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(deleteQuery.name()).isEqualTo(SocialMedia.class.getSimpleName());
            soft.assertThat(deleteQuery.condition()).isNotEmpty();
            var criteriaCondition = deleteQuery.condition().orElseThrow();
            soft.assertThat(criteriaCondition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(criteriaCondition.element()).isEqualTo(Element.of("dtype", "photo"));
        });
    }


}
