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
 */
package org.eclipse.jnosql.mapping.keyvalue;

import jakarta.enterprise.event.Event;
import org.eclipse.jnosql.mapping.EntityPostPersist;
import org.eclipse.jnosql.mapping.EntityPrePersist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class KeyValueEventPersistManagerTest {

    @InjectMocks
    private KeyValueEventPersistManager subject;


    @Mock
    private Event<EntityPrePersist> entityPrePersistEvent;

    @Mock
    private Event<EntityPostPersist> entityPostPersistEvent;





    static class Actor {
        private String name;
    }

    @Nested
    @DisplayName("When the event manager fires events")
    class WhenTheEventManagerFiresEvents {

        @Test
        @DisplayName("Should fire pre entity")
        void shouldFirePreEntity() {
            Actor actor = new Actor();
            actor.name = "Luke";
            subject.firePreEntity(actor);
            ArgumentCaptor<EntityPrePersist> captor = ArgumentCaptor.forClass(EntityPrePersist.class);
            verify(entityPrePersistEvent).fire(captor.capture());
            EntityPrePersist value = captor.getValue();
            assertThat(value.get()).isEqualTo(actor);
        }

        @Test
        @DisplayName("Should fire post entity")
        void shouldFirePostEntity() {
            Actor actor = new Actor();
            actor.name = "Luke";
            subject.firePostEntity(actor);
            ArgumentCaptor<EntityPostPersist> captor = ArgumentCaptor.forClass(EntityPostPersist.class);
            verify(entityPostPersistEvent).fire(captor.capture());
            EntityPostPersist value = captor.getValue();
            assertThat(value.get()).isEqualTo(actor);
        }

    }

}
