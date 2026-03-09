/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.mapping.keyvalue;

import org.eclipse.jnosql.mapping.EntityPostPersist;
import org.eclipse.jnosql.mapping.EntityPrePersist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class KeyValueEventPersistManagerTest {

    private KeyValueEventPersistManager subject;

    @Mock
    private Consumer<EntityPrePersist> entityPrePersistEvent;

    @Mock
    private Consumer<EntityPostPersist> entityPostPersistEvent;

    @BeforeEach
    void setUp() {
        subject = new KeyValueEventPersistManager(entityPrePersistEvent, entityPostPersistEvent);
    }

    @Test
    void shouldFirePreEntity() {
        Actor actor = new Actor();
        actor.name = "Luke";
        subject.firePreEntity(actor);
        ArgumentCaptor<EntityPrePersist> captor = ArgumentCaptor.forClass(EntityPrePersist.class);
        verify(entityPrePersistEvent).accept(captor.capture());
        EntityPrePersist value = captor.getValue();
        assertEquals(actor, value.get());
    }

    @Test
    void shouldFirePostEntity() {
        Actor actor = new Actor();
        actor.name = "Luke";
        subject.firePostEntity(actor);
        ArgumentCaptor<EntityPostPersist> captor = ArgumentCaptor.forClass(EntityPostPersist.class);
        verify(entityPostPersistEvent).accept(captor.capture());
        EntityPostPersist value = captor.getValue();
        assertEquals(actor, value.get());
    }

    // -----------------------------------------------------------------------
    // Tests for the package-private Consumer-based constructor (non-CDI path)
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Package-private constructor with Consumer delegates")
    class ConsumerConstructorTests {

        @Test
        @DisplayName("Should firePreEntity invoke the pre-persist consumer with the correct entity")
        void shouldFirePreEntityInvokeConsumer() {
            Actor actor = new Actor();
            actor.name = "Leia";

            AtomicReference<EntityPrePersist> captured = new AtomicReference<>();
            Consumer<EntityPrePersist> prePersistConsumer = captured::set;
            Consumer<EntityPostPersist> noOp = e -> {};

            KeyValueEventPersistManager manager =
                    new KeyValueEventPersistManager(prePersistConsumer, noOp);

            manager.firePreEntity(actor);

            assertSoftly(softly -> {
                softly.assertThat(captured.get())
                        .as("pre-persist event was captured")
                        .isNotNull();
                softly.assertThat(captured.get().get())
                        .as("event holds the original entity")
                        .isSameAs(actor);
            });
        }

        @Test
        @DisplayName("Should firePostEntity invoke the post-persist consumer with the correct entity")
        void shouldFirePostEntityInvokeConsumer() {
            Actor actor = new Actor();
            actor.name = "Han";

            Consumer<EntityPrePersist> noOp = e -> {};
            AtomicReference<EntityPostPersist> captured = new AtomicReference<>();
            Consumer<EntityPostPersist> postPersistConsumer = captured::set;

            KeyValueEventPersistManager manager =
                    new KeyValueEventPersistManager(noOp, postPersistConsumer);

            manager.firePostEntity(actor);

            assertSoftly(softly -> {
                softly.assertThat(captured.get())
                        .as("post-persist event was captured")
                        .isNotNull();
                softly.assertThat(captured.get().get())
                        .as("event holds the original entity")
                        .isSameAs(actor);
            });
        }

        @Test
        @DisplayName("Should firePreEntity invoke pre-persist consumer and not post-persist consumer")
        void shouldFirePreEntityInvokeOnlyPreConsumer() {
            Actor actor = new Actor();
            actor.name = "Yoda";

            AtomicReference<EntityPrePersist> preCaptured = new AtomicReference<>();
            AtomicReference<EntityPostPersist> postCaptured = new AtomicReference<>();

            KeyValueEventPersistManager manager = new KeyValueEventPersistManager(
                    preCaptured::set,
                    postCaptured::set
            );

            manager.firePreEntity(actor);

            assertSoftly(softly -> {
                softly.assertThat(preCaptured.get()).as("pre-persist consumer was invoked").isNotNull();
                softly.assertThat(postCaptured.get()).as("post-persist consumer was NOT invoked").isNull();
            });
        }

        @Test
        @DisplayName("Should firePostEntity invoke post-persist consumer and not pre-persist consumer")
        void shouldFirePostEntityInvokeOnlyPostConsumer() {
            Actor actor = new Actor();
            actor.name = "Obi-Wan";

            AtomicReference<EntityPrePersist> preCaptured = new AtomicReference<>();
            AtomicReference<EntityPostPersist> postCaptured = new AtomicReference<>();

            KeyValueEventPersistManager manager = new KeyValueEventPersistManager(
                    preCaptured::set,
                    postCaptured::set
            );

            manager.firePostEntity(actor);

            assertSoftly(softly -> {
                softly.assertThat(preCaptured.get()).as("pre-persist consumer was NOT invoked").isNull();
                softly.assertThat(postCaptured.get()).as("post-persist consumer was invoked").isNotNull();
            });
        }

        @Test
        @DisplayName("Should both consumers be invokable independently in sequence")
        void shouldBothConsumersBeInvokableInSequence() {
            Actor actor = new Actor();
            actor.name = "R2-D2";

            AtomicReference<EntityPrePersist> preCaptured = new AtomicReference<>();
            AtomicReference<EntityPostPersist> postCaptured = new AtomicReference<>();

            KeyValueEventPersistManager manager = new KeyValueEventPersistManager(
                    preCaptured::set,
                    postCaptured::set
            );

            manager.firePreEntity(actor);
            manager.firePostEntity(actor);

            assertSoftly(softly -> {
                softly.assertThat(preCaptured.get()).as("pre-persist event captured").isNotNull();
                softly.assertThat(preCaptured.get().get()).as("pre-persist entity matches").isSameAs(actor);
                softly.assertThat(postCaptured.get()).as("post-persist event captured").isNotNull();
                softly.assertThat(postCaptured.get().get()).as("post-persist entity matches").isSameAs(actor);
            });
        }
    }


    static class Actor {
        private String name;
    }

}
