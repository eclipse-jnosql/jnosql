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
package org.eclipse.jnosql.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class EntityPostPersistTest {

    @Nested
    @DisplayName("When the event is created")
    class WhenTheEventIsCreated {

        @Test
        @DisplayName("Should expose the wrapped value")
        void shouldExposeTheWrappedValue() {
            Object value = new Object();
            EntityPostPersist entity = new EntityPostPersist(value);

            assertThat(entity.get()).isSameAs(value);
        }

        @Test
        @DisplayName("Should create an event from a non-null value")
        void shouldCreateAnEventFromANonNullValue() {
            Object value = new Object();
            EntityPostPersist entity = EntityPostPersist.of(value);

            assertThat(entity.get()).isSameAs(value);
        }

        @Test
        @DisplayName("Should reject a null value")
        void shouldRejectANullValue() {
            assertThatNullPointerException().isThrownBy(() -> EntityPostPersist.of(null));
        }
    }

    @Nested
    @DisplayName("When the event is compared")
    class WhenTheEventIsCompared {

        @Test
        @DisplayName("Should use the wrapped value for equality")
        void shouldUseTheWrappedValueForEquality() {
            Object value1 = new Object();
            Object value2 = new Object();

            EntityPostPersist entity1 = new EntityPostPersist(value1);
            EntityPostPersist entity2 = new EntityPostPersist(value1);
            EntityPostPersist entity3 = new EntityPostPersist(value2);

            assertSoftly(softly -> {
                softly.assertThat(entity1).isEqualTo(entity1);
                softly.assertThat(entity1).isEqualTo(entity2);
                softly.assertThat(entity2).isEqualTo(entity1);
                softly.assertThat(entity1).isNotEqualTo(entity3);
                softly.assertThat(entity1).isNotEqualTo(null);
            });
        }
    }

    @Nested
    @DisplayName("When the event is represented as text")
    class WhenTheEventIsRepresentedAsText {

        @Test
        @DisplayName("Should include the wrapped value")
        void shouldIncludeTheWrappedValue() {
            Object value = new Object();
            EntityPostPersist entity = new EntityPostPersist(value);
            String expected = "EntityPostPersist{value=" + value + "}";

            assertThat(entity).hasToString(expected);
        }
    }
}
