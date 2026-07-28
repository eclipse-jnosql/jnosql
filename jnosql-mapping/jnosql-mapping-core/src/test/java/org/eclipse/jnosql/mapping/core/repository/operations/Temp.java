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
package org.eclipse.jnosql.mapping.core.repository.operations;

import jakarta.data.event.PostInsertEvent;
import jakarta.data.event.PreInsertEvent;
import jakarta.data.event.PreUpdateEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.entities.Book;

@ApplicationScoped
public class Temp {

    @Inject
    private Event<Object> events;

    void preInsert(Book book) {
        events.select(BookLifecycleEventTypes.PRE_INSERT)
                .fire(new PreInsertEvent<>(book));
    }

    final class BookLifecycleEventTypes {

        static final TypeLiteral<PreInsertEvent<Book>> PRE_INSERT =
                new TypeLiteral<>() {};

        static final TypeLiteral<PostInsertEvent<Book>> POST_INSERT =
                new TypeLiteral<>() {};

        static final TypeLiteral<PreUpdateEvent<Book>> PRE_UPDATE =
                new TypeLiteral<>() {};

        // Remaining lifecycle types
    }
}
