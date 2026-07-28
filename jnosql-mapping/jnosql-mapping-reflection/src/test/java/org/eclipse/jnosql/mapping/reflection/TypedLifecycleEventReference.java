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
package org.eclipse.jnosql.mapping.reflection;

import jakarta.data.event.PreInsertEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.reflection.entities.Book;

import java.util.function.Function;

/**
 * Reference implementation for firing CDI lifecycle events with a typed event reference.
 * <p>
 * This class documents how repository operations can keep the CDI {@link TypeLiteral}
 * for a Jakarta Data lifecycle event together with the function that creates the event
 * instance. It is kept as a guide for projects that need strongly typed event selection
 * before the same pattern is implemented elsewhere.
 */
@ApplicationScoped
public class TypedLifecycleEventReference {

    @Inject
    private Event<Object> events;

    void preInsert(Object book) {
        events.select(BookLifecycleEventTypes.PRE_INSERT)
                .fire(BookLifecycleEventTypes.PRE_INSERT_FUNCTION.apply(book));
    }

    /**
     * Groups typed lifecycle event metadata for {@link Book} events.
     */
    final class BookLifecycleEventTypes {

        static final TypeLiteral<PreInsertEvent<Book>> PRE_INSERT =
                new TypeLiteral<>() {};

        static final Function<Object, PreInsertEvent<Book>> PRE_INSERT_FUNCTION =
                entity -> new PreInsertEvent<>((Book) entity);

        // Remaining lifecycle types
    }
}
