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
package org.eclipse.jnosql.mapping.graph;

import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class DefaultEdgeBuilderTest {
    private EdgeBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new DefaultEdgeBuilder<>();
    }

    @Nested
    @DisplayName("When the edge is built")
    class WhenTheEdgeIsBuilt {

        @Test
        @DisplayName("Should create an edge with source, target, label, and properties")
        void shouldCreateEdgeSuccessfully() {
            var source = CommunicationEntity.of("Person");
            source.add(Element.of("name", "Alice"));

            var target = CommunicationEntity.of("Book");
            target.add(Element.of("title", "DDD"));

            var edge = builder.source(source)
                    .label("READS")
                    .target(target)
                    .property("since", 2019)
                    .property("format", "kindle")
                    .build();

            assertSoftly(softly -> {
                softly.assertThat(edge).isNotNull();
                softly.assertThat(edge.label()).isEqualTo("READS");
                softly.assertThat(edge.source()).isEqualTo(source);
                softly.assertThat(edge.target()).isEqualTo(target);
                softly.assertThat(edge.properties()).containsEntry("since", 2019);
                softly.assertThat(edge.properties()).containsEntry("format", "kindle");
            });
        }

        @Test
        @DisplayName("Should build an edge without properties")
        void shouldBuildEdgeWithoutProperties() {
            var source = CommunicationEntity.of("Person");
            var target = CommunicationEntity.of("Book");

            var edge = builder.source(source)
                    .label("READS")
                    .target(target)
                    .build();

            assertSoftly(softly -> {
                softly.assertThat(edge).isNotNull();
                softly.assertThat(edge.label()).isEqualTo("READS");
                softly.assertThat(edge.source()).isEqualTo(source);
                softly.assertThat(edge.target()).isEqualTo(target);
                softly.assertThat(edge.properties()).isEmpty();
            });
        }

        @Test
        @DisplayName("Should add multiple properties")
        void shouldAddMultipleProperties() {
            var source = CommunicationEntity.of("Person");
            var target = CommunicationEntity.of("Book");

            var edge = builder.source(source)
                    .label("READS")
                    .target(target)
                    .property("since", 2020)
                    .property("rating", 5)
                    .build();

            assertThat(edge.properties()).containsExactlyInAnyOrderEntriesOf(Map.of(
                    "since", 2020,
                    "rating", 5
            ));
        }
    }

    @Nested
    @DisplayName("When the label is supplied")
    class WhenTheLabelIsSupplied {

        @Test
        @DisplayName("Should allow dynamic label using supplier")
        void shouldAllowDynamicLabelUsingSupplier() {
            var source = CommunicationEntity.of("Person");
            var target = CommunicationEntity.of("Book");

            var edge = builder.source(source)
                    .label(() -> "READS")
                    .target(target)
                    .build();

            assertThat(edge.label()).isEqualTo("READS");
        }
    }

    @Nested
    @DisplayName("When the builder receives null values")
    class WhenTheBuilderReceivesNullValues {

        @Test
        @DisplayName("Should throw an exception when source is null")
        void shouldThrowExceptionWhenSourceIsNull() {
            assertThatNullPointerException().isThrownBy(() -> builder.source(null));
        }

        @Test
        @DisplayName("Should throw an exception when label is null")
        void shouldThrowExceptionWhenLabelIsNull() {
            var source = CommunicationEntity.of("Person");
            assertThatNullPointerException().isThrownBy(() -> builder.source(source).label((String) null));
        }

        @Test
        @DisplayName("Should throw an exception when label supplier is null")
        void shouldThrowExceptionWhenLabelSupplierIsNull() {
            var source = CommunicationEntity.of("Person");
            assertThatNullPointerException().isThrownBy(() -> builder.source(source).label((Supplier<String>) null));
        }

        @Test
        @DisplayName("Should throw an exception when target is null")
        void shouldThrowExceptionWhenTargetIsNull() {
            var source = CommunicationEntity.of("Person");
            var labelStep = builder.source(source).label("READS");

            assertThatNullPointerException().isThrownBy(() -> labelStep.target(null));
        }
    }
}