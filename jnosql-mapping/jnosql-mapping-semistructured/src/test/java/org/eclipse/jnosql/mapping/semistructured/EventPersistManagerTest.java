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
package org.eclipse.jnosql.mapping.semistructured;

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
class EventPersistManagerTest {


    @InjectMocks
    private EventPersistManager subject;


    @Mock
    private Event<EntityPrePersist> entityPrePersistEvent;

    @Mock
    private Event<EntityPostPersist> entityPostPersistEvent;


    @DisplayName("Should fire pre entity")
    @Test
    void shouldFirePreEntity() {
        Jedi jedi = new Jedi();
        jedi.name = "Luke";
        subject.firePreEntity(jedi);
        ArgumentCaptor<EntityPrePersist> captor = ArgumentCaptor.forClass(EntityPrePersist.class);
        verify(entityPrePersistEvent).fire(captor.capture());
        EntityPrePersist value = captor.getValue();
        assertThat(value.get()).isEqualTo(jedi);
    }

    @DisplayName("Should fire post entity")
    @Test
    void shouldFirePostEntity() {
        Jedi jedi = new Jedi();
        jedi.name = "Luke";
        subject.firePostEntity(jedi);
        ArgumentCaptor<EntityPostPersist> captor = ArgumentCaptor.forClass(EntityPostPersist.class);
        verify(entityPostPersistEvent).fire(captor.capture());
        EntityPostPersist value = captor.getValue();
        assertThat(value.get()).isEqualTo(jedi);
    }


    static class Jedi {
        private String name;
    }

    @Nested
    @DisplayName("When the event persist manager is tested")
    class WhenTheEventPersistManagerIsTested {
    }
}