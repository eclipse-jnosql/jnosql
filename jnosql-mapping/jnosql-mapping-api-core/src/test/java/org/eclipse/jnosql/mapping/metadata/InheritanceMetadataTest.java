/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.metadata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;


class InheritanceMetadataTest {

    @Nested
    @DisplayName("When the metadata is created")
    class WhenTheMetadataIsCreated {

        @Test
        @DisplayName("Should expose constructor values")
        void shouldExposeConstructorValues() {
            String discriminatorValue = "testValue";
            String discriminatorColumn = "testColumn";
            Class<?> parentClass = Object.class;
            Class<?> entityClass = String.class;

            InheritanceMetadata metadata = new InheritanceMetadata(discriminatorValue, discriminatorColumn, parentClass, entityClass);

            assertSoftly(softly -> {
                softly.assertThat(metadata.discriminatorValue()).as("unexpected discriminatorValue value").isEqualTo(discriminatorValue);
                softly.assertThat(metadata.discriminatorColumn()).as("unexpected discriminatorColumn value").isEqualTo(discriminatorColumn);
                softly.assertThat(metadata.parent()).as("unexpected parent value").isEqualTo(parentClass);
                softly.assertThat(metadata.entity()).as("unexpected entity value").isEqualTo(entityClass);
            });
        }
    }

    @Nested
    @DisplayName("When the metadata is compared")
    class WhenTheMetadataIsCompared {

        @Test
        @DisplayName("Should use discriminator value, column, and parent for equality")
        void shouldUseDiscriminatorValueColumnAndParentForEquality() {
            InheritanceMetadata metadata1 = new InheritanceMetadata("value1", "column1", String.class, Integer.class);
            InheritanceMetadata metadata2 = new InheritanceMetadata("value1", "column1", String.class, Integer.class);
            InheritanceMetadata metadata3 = new InheritanceMetadata("value2", "column1", String.class, Integer.class);
            InheritanceMetadata metadata4 = new InheritanceMetadata("value1", "column2", String.class, Integer.class);
            InheritanceMetadata metadata5 = new InheritanceMetadata("value1", "column1", Integer.class, Integer.class);
            InheritanceMetadata metadata6 = new InheritanceMetadata("value1", "column1", String.class, String.class);

            assertSoftly(softly -> {
                softly.assertThat(metadata1).as("it should be reflexive").isEqualTo(metadata1);
                softly.assertThat(metadata1).as("it should be symmetric").isEqualTo(metadata2);
                softly.assertThat(metadata2).as("it should be symmetric").isEqualTo(metadata1);
                softly.assertThat(metadata1).as("entity is ignored by equals").isEqualTo(metadata6);
                softly.assertThat(metadata6).as("entity is ignored by equals").isEqualTo(metadata1);
                softly.assertThat(metadata1).isNotEqualTo(metadata3);
                softly.assertThat(metadata1).isNotEqualTo(metadata4);
                softly.assertThat(metadata1).isNotEqualTo(metadata5);
                softly.assertThat(metadata1).isNotEqualTo(null);
                softly.assertThat(metadata1).isNotEqualTo(new Object());
                softly.assertThat(metadata1).hasSameHashCodeAs(metadata2);
            });
        }

        @Test
        @DisplayName("Should identify whether a class is the parent")
        void shouldIdentifyWhetherAClassIsTheParent() {
            Class<?> parentClass = String.class;
            InheritanceMetadata metadata = new InheritanceMetadata("value", "column", parentClass, Integer.class);

            assertSoftly(softly -> {
                softly.assertThat(metadata.isParent(parentClass)).isTrue();
                softly.assertThat(metadata.isParent(Integer.class)).isFalse();
            });
        }

        @Test
        @DisplayName("Should reject a null parent comparison")
        void shouldRejectANullParentComparison() {
            InheritanceMetadata metadata = new InheritanceMetadata("value", "column", String.class, Integer.class);

            assertThatNullPointerException().isThrownBy(() -> metadata.isParent(null));
        }
    }

    @Nested
    @DisplayName("When the metadata is represented as text")
    class WhenTheMetadataIsRepresentedAsText {

        @Test
        @DisplayName("Should include discriminator values and parent")
        void shouldIncludeDiscriminatorValuesAndParent() {
            InheritanceMetadata metadata = new InheritanceMetadata("testValue", "testColumn", String.class, Integer.class);
            String expected = "InheritanceMetadata{discriminatorValue='testValue', discriminatorColumn='testColumn', parent=class java.lang.String}";

            assertThat(metadata).hasToString(expected);
        }
    }
}