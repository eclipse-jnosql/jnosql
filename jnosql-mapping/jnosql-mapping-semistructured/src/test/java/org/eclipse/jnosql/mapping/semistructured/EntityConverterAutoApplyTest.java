/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.entities.autoconverter.BookWishList;
import org.eclipse.jnosql.mapping.semistructured.entities.autoconverter.TravelWishList;
import org.eclipse.jnosql.mapping.semistructured.entities.autoconverter.WishCollection;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class EntityConverterAutoApplyTest {

    @Inject
    private EntityConverter converter;

    @Nested
    @DisplayName("When converting entity attributes to database values using auto-apply converters")
    class WhenAutoApplyToCommunication {

    }

    @Nested
    @DisplayName("When converting database values to entity attributes using auto-apply converters")
    class WhenAutoApplyToEntity {

    }

    @Nested
    @DisplayName("When an explicit converter overrides an auto-apply converter during entity-to-database conversion")
    class WhenOverwriteAutoApplyToCommunication {

        @Test
        @DisplayName("Should overwrite by attribute converter")
        void shouldOverWriteByAttributeConverter() {
            WishCollection wishCollection = new WishCollection();
            wishCollection.addWish("Learn JNoSQL");
            wishCollection.addWish("Clean Code");
            wishCollection.addWish("Refactor Code");
            var bookWishList = BookWishList.of(wishCollection);
            var communicationEntity = converter.toCommunication(bookWishList);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(communicationEntity.name()).isEqualTo("BookWishList");
                soft.assertThat(communicationEntity.find("_id").orElseThrow().get()).isEqualTo(bookWishList.getUuid());
                soft.assertThat(communicationEntity.find("wishCollection").orElseThrow().get()).isEqualTo(
                        String.join("|", wishCollection.getWishes())
                );
            });
        }

        @Test
        @DisplayName("Should overwrite by attribute converter")
        void shouldOverWriteByAttributeConverterCustomConstructor() {
            WishCollection wishCollection = new WishCollection();
            wishCollection.addWish("Salvador");
            wishCollection.addWish("Rio de Janeiro");
            wishCollection.addWish("Amor");
            var bookWishList = TravelWishList.of(wishCollection);
            var communicationEntity = converter.toCommunication(bookWishList);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(communicationEntity.name()).isEqualTo("TravelWishList");
                soft.assertThat(communicationEntity.find("_id").orElseThrow().get()).isEqualTo(bookWishList.getUuid());
                soft.assertThat(communicationEntity.find("wishCollection").orElseThrow().get()).isEqualTo(
                        String.join("|", wishCollection.getWishes())
                );
            });
        }
    }

    @Nested
    @DisplayName("When an explicit converter overrides an auto-apply converter during database-to-entity conversion")
    class WhenOverwriteAutoApplyToEntity {

    }


}
